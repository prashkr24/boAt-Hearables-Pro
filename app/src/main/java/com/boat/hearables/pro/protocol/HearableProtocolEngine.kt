package com.boat.hearables.pro.protocol

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import com.boat.hearables.pro.data.BoatDeviceCatalog
import com.boat.hearables.pro.data.LuxeLedMode
import com.boat.hearables.pro.data.LuxeSoundMode
import com.boat.hearables.pro.data.RoomPlacement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

data class LuxeSpeakerTelemetry(
    val isDetectedInBluetooth: Boolean = false,
    val isConnected: Boolean = false,
    val isScanning: Boolean = false,
    val speakerName: String = "boAt Nirvana Luxe",
    val macAddress: String = "Not Detected",
    val transportChannel: String = "Waiting for Nirvana Luxe...",
    val firmwareVersion: String = "BES-LUXE-v1.9",
    val audioCodec: String = "LDAC / AAC Hi-Res",
    val rssiDbm: Int = -50,
    val speakerBatteryPercent: Int = 85,
    val isCharging: Boolean = false
)

data class ProtocolPacketLog(
    val timestamp: String,
    val direction: String, // "TX" or "RX"
    val commandTitle: String,
    val hexPayload: String
)

/**
 * Dedicated Bluetooth Classic (RFCOMM SPP) + BLE GATT protocol engine
 * exclusively for the boAt Nirvana Luxe speaker (`BES_SDK` / `Lh80` / `Lzu1` / `Lcr1`).
 *
 * Strictly ignores all non-Nirvana-Luxe Bluetooth devices.
 */
@SuppressLint("MissingPermission")
class HearableProtocolEngine(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val btAdapter: BluetoothAdapter? get() = btManager?.adapter

    private var sppSocket: BluetoothSocket? = null
    private var sppOut: OutputStream? = null
    private var sppIn: InputStream? = null
    private var activeGatt: BluetoothGatt? = null
    private var activeWriteChar: BluetoothGattCharacteristic? = null

    private val _telemetry = MutableStateFlow(LuxeSpeakerTelemetry())
    val telemetry: StateFlow<LuxeSpeakerTelemetry> = _telemetry.asStateFlow()

    private val _packetLogs = MutableStateFlow<List<ProtocolPacketLog>>(emptyList())
    val packetLogs: StateFlow<List<ProtocolPacketLog>> = _packetLogs.asStateFlow()

    private val bleScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val name = runCatching { device.name }.getOrNull() ?: result.scanRecord?.deviceName
            if (BoatDeviceCatalog.isNirvanaLuxeSpeaker(name)) {
                stopBleScan()
                _telemetry.value = _telemetry.value.copy(
                    isDetectedInBluetooth = true,
                    speakerName = name ?: "boAt Nirvana Luxe",
                    macAddress = device.address,
                    rssiDbm = result.rssi
                )
                connectToNirvanaLuxe(device)
            }
        }
    }

    init {
        detectAndConnectNirvanaLuxe()
    }

    /**
     * Checks paired Bluetooth devices STRICTLY for `boAt Nirvana Luxe`.
     * All other Bluetooth devices (watches, earbuds, cars, TVs) are ignored.
     */
    fun detectAndConnectNirvanaLuxe(): Boolean {
        val adapter = btAdapter
        if (adapter == null || !adapter.isEnabled) {
            _telemetry.value = _telemetry.value.copy(
                isDetectedInBluetooth = false,
                isConnected = false,
                transportChannel = "Bluetooth is turned off"
            )
            return false
        }

        val bonded = runCatching { adapter.bondedDevices.orEmpty() }.getOrDefault(emptySet())
        // STRICT FILTER: Only match boAt Nirvana Luxe
        val luxeDevice = bonded.firstOrNull { dev ->
            val name = runCatching { dev.name }.getOrNull()
            BoatDeviceCatalog.isNirvanaLuxeSpeaker(name)
        }

        if (luxeDevice != null) {
            val isAclConnected = isDeviceConnected(luxeDevice)
            val batt = getDeviceBatteryLevel(luxeDevice).takeIf { it in 1..100 } ?: _telemetry.value.speakerBatteryPercent
            _telemetry.value = _telemetry.value.copy(
                isDetectedInBluetooth = true,
                isConnected = isAclConnected,
                speakerName = runCatching { luxeDevice.name }.getOrNull() ?: "boAt Nirvana Luxe",
                macAddress = luxeDevice.address ?: "00:00:00:00:00:00",
                speakerBatteryPercent = batt,
                transportChannel = if (isAclConnected) "Connected • BES Audio + DSP" else "Paired • Tap Connect to Link Speaker"
            )
            if (isAclConnected && sppSocket?.isConnected != true && activeGatt == null) {
                connectToNirvanaLuxe(luxeDevice)
            }
            return true
        } else {
            _telemetry.value = _telemetry.value.copy(
                isDetectedInBluetooth = false,
                isConnected = false,
                macAddress = "Nirvana Luxe Not Paired",
                transportChannel = "Pair 'Nirvana Luxe' in Bluetooth or tap Scan"
            )
            return false
        }
    }

    /**
     * Performs a dedicated BLE + Bonded scan exclusively looking for `Nirvana Luxe`.
     */
    fun scanForNirvanaLuxe() {
        if (detectAndConnectNirvanaLuxe() && _telemetry.value.isConnected) return
        val adapter = btAdapter ?: return
        val scanner = runCatching { adapter.bluetoothLeScanner }.getOrNull() ?: return

        scope.launch {
            _telemetry.value = _telemetry.value.copy(
                isScanning = true,
                transportChannel = "Scanning exclusively for boAt Nirvana Luxe..."
            )
            runCatching {
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()
                scanner.startScan(null, settings, bleScanCallback)
            }
            delay(8000)
            stopBleScan()
            if (!_telemetry.value.isDetectedInBluetooth) {
                detectAndConnectNirvanaLuxe()
            }
        }
    }

    private fun stopBleScan() {
        runCatching { btAdapter?.bluetoothLeScanner?.stopScan(bleScanCallback) }
        _telemetry.value = _telemetry.value.copy(isScanning = false)
    }

    fun connectToNirvanaLuxe(device: BluetoothDevice? = null) {
        scope.launch {
            val adapter = btAdapter ?: return@launch
            val targetDevice = device ?: runCatching {
                adapter.bondedDevices.orEmpty().firstOrNull {
                    BoatDeviceCatalog.isNirvanaLuxeSpeaker(runCatching { it.name }.getOrNull())
                }
            }.getOrNull()

            if (targetDevice == null) {
                scanForNirvanaLuxe()
                return@launch
            }

            disconnectCurrent()

            // 1. Connect via BES RFCOMM SPP (`00001101-0000-1000-8000-00805F9B34FB`, matching Lh80.u -> Lcj0$a.SPP)
            val sppOk = runCatching {
                val uuid = UUID.fromString(BoatDeviceCatalog.BES_SPP_UUID)
                val socket = targetDevice.createInsecureRfcommSocketToServiceRecord(uuid)
                runCatching { adapter.cancelDiscovery() }
                socket.connect()
                sppSocket = socket
                sppOut = socket.outputStream
                sppIn = socket.inputStream
                startSppReaderLoop()
                true
            }.getOrDefault(false)

            val batt = getDeviceBatteryLevel(targetDevice).takeIf { it in 1..100 } ?: 88

            if (sppOk) {
                _telemetry.value = _telemetry.value.copy(
                    isDetectedInBluetooth = true,
                    isConnected = true,
                    speakerName = runCatching { targetDevice.name }.getOrNull() ?: "boAt Nirvana Luxe",
                    macAddress = targetDevice.address,
                    speakerBatteryPercent = batt,
                    transportChannel = "BES RFCOMM SPP + A2DP Active"
                )
                sendLuxeHandshakePoll()
                return@launch
            }

            // 2. Fallback to BES / Excelpoint BLE GATT (`66666666-...` or `65786365-...`)
            runCatching {
                activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    targetDevice.connectGatt(context, false, createBesGattCallback(), BluetoothDevice.TRANSPORT_LE)
                } else {
                    targetDevice.connectGatt(context, false, createBesGattCallback())
                }
            }

            // If standard A2DP audio is connected even without GATT/SPP open yet
            if (isDeviceConnected(targetDevice)) {
                _telemetry.value = _telemetry.value.copy(
                    isDetectedInBluetooth = true,
                    isConnected = true,
                    speakerName = runCatching { targetDevice.name }.getOrNull() ?: "boAt Nirvana Luxe",
                    macAddress = targetDevice.address,
                    speakerBatteryPercent = batt,
                    transportChannel = "A2DP Audio + BES BLE Link"
                )
            }
        }
    }

    private fun startSppReaderLoop() {
        scope.launch {
            val buf = ByteArray(512)
            val input = sppIn ?: return@launch
            while (sppSocket?.isConnected == true) {
                val n = runCatching { input.read(buf) }.getOrDefault(-1)
                if (n <= 0) break
                handleIncomingBesPacket(buf.copyOf(n))
            }
        }
    }

    private fun createBesGattCallback(): BluetoothGattCallback {
        return object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    _telemetry.value = _telemetry.value.copy(
                        isDetectedInBluetooth = true,
                        isConnected = true,
                        macAddress = gatt.device.address,
                        transportChannel = "BES BLE GATT (0x0413 CommandSet)"
                    )
                    gatt.discoverServices()
                    gatt.readRemoteRssi()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    val stillA2dp = isDeviceConnected(gatt.device)
                    _telemetry.value = _telemetry.value.copy(
                        isConnected = stillA2dp,
                        transportChannel = if (stillA2dp) "A2DP Audio Active" else "Disconnected"
                    )
                }
            }

            override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    _telemetry.value = _telemetry.value.copy(rssiDbm = rssi)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) return
                val besService = gatt.getService(UUID.fromString(BoatDeviceCatalog.BES_BLE_SERVICE_UUID))
                    ?: gatt.getService(UUID.fromString(BoatDeviceCatalog.EXCELPOINT_SERVICE_UUID))
                    ?: gatt.services.firstOrNull {
                        val u = it.uuid.toString().lowercase()
                        u.contains("6666") || u.contains("65786365") || u.contains("01000100")
                    }
                if (besService != null) {
                    activeWriteChar = besService.getCharacteristic(UUID.fromString(BoatDeviceCatalog.BES_BLE_CHAR_UUID))
                        ?: besService.getCharacteristic(UUID.fromString(BoatDeviceCatalog.EXCELPOINT_WRITE_UUID))
                        ?: besService.characteristics.firstOrNull {
                            (it.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0
                        }
                    val notifyChar = besService.getCharacteristic(UUID.fromString(BoatDeviceCatalog.BES_BLE_CHAR_UUID))
                        ?: besService.getCharacteristic(UUID.fromString(BoatDeviceCatalog.EXCELPOINT_NOTIFY_UUID))
                        ?: besService.characteristics.firstOrNull {
                            (it.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
                        }
                    if (notifyChar != null) {
                        gatt.setCharacteristicNotification(notifyChar, true)
                        val cccd = notifyChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        if (cccd != null) {
                            cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(cccd)
                        }
                    }
                    sendLuxeHandshakePoll()
                }
            }

            @Deprecated("Deprecated in API 33")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                val data = characteristic.value ?: return
                handleIncomingBesPacket(data)
            }
        }
    }

    /**
     * Parses incoming BES responses (`Lzu1.Q` & `Lh80.z`) from the Nirvana Luxe speaker.
     * Frame header: `[0x12/0x13, 0x04, len_lo, len_hi, cmdOpcode, ...]`
     */
    private fun handleIncomingBesPacket(data: ByteArray) {
        appendLog("RX", "Nirvana Luxe BES Frame", data.toHex())
        if (data.size < 5) return
        val opcode = data[4].toInt() and 0xFF
        when (opcode) {
            0x80 -> { // Battery response (`Lh80.t` -> cmd 576 in Lzu1.Q)
                if (data.size >= 7) {
                    val batt = (data[6].toInt() and 0xFF).coerceIn(0, 100)
                    _telemetry.value = _telemetry.value.copy(speakerBatteryPercent = batt)
                }
            }
            0x07 -> { // Firmware version (`Lh80.t0` -> cmd 535 in Lzu1.Q)
                if (data.size > 6) {
                    val fw = data.copyOfRange(6, data.size).decodeToString().trim { it <= ' ' || it == '\u0000' }
                    if (fw.isNotEmpty()) {
                        _telemetry.value = _telemetry.value.copy(firmwareVersion = fw)
                    }
                }
            }
        }
    }

    // =========================================================================
    // AUTHENTIC BES 0x0413 (`13 04`) COMMAND ENCODER FOR BOAT NIRVANA LUXE
    // Extracted from `Lh80.i1` -> `Lav1.H` -> `Lzu1.k` -> `Lcr1((short) 1043)`
    // =========================================================================

    /**
     * Encodes a BES CommandSet frame (`Lcr1((short) 0x0413, [cmdType, ...payload]).a()`):
     * `[0x13, 0x04, len_lo, len_hi, cmdType, ...payload]`
     */
    private fun encodeBesFrame(cmdType: Int, isRead: Boolean, payload: ByteArray = byteArrayOf()): ByteArray {
        val body = if (isRead) {
            byteArrayOf((cmdType and 0xFF).toByte())
        } else {
            ByteArray(1 + payload.size).also {
                it[0] = (cmdType and 0xFF).toByte()
                System.arraycopy(payload, 0, it, 1, payload.size)
            }
        }
        val len = body.size
        return ByteArray(4 + len).also { out ->
            out[0] = 0x13 // 1043 & 0xFF
            out[1] = 0x04 // (1043 >> 8) & 0xFF
            out[2] = (len and 0xFF).toByte()
            out[3] = ((len >> 8) and 0xFF).toByte()
            System.arraycopy(body, 0, out, 4, len)
        }
    }

    /**
     * 1. Initial Nirvana Luxe Handshake Poll (`Lh80.t`, `Lh80.t0`, `Lh80.L0`, `Lh80.B0`, `Lh80.K`)
     */
    fun sendLuxeHandshakePoll() {
        val polls = listOf(
            "Poll Battery (0x80)" to encodeBesFrame(0x80, isRead = true),
            "Poll Firmware (0x07)" to encodeBesFrame(0x07, isRead = true),
            "Poll Sound Profile (0x1D)" to encodeBesFrame(0x1D, isRead = false, byteArrayOf(0x01, 0x00)),
            "Poll LED Light State (0x1C)" to encodeBesFrame(0x1C, isRead = false, byteArrayOf(0x01, 0x00)),
            "Poll 7-Band Hardware EQ (0x12)" to encodeBesFrame(0x12, isRead = false, byteArrayOf(0x01, 0x00))
        )
        scope.launch {
            for ((title, pkt) in polls) {
                appendLog("TX", title, pkt.toHex())
                sendRawBytes(pkt)
                delay(60)
            }
        }
    }

    /**
     * 2. Nirvana Luxe 7-Band Custom Hardware EQ (`Lh80.h1` / `Lh80.G1`, opcode `0x13` = `19`)
     * Bands: `60Hz, 120Hz, 250Hz, 750Hz, 2kHz, 5kHz, 15kHz`
     * Payload: `[0x04, g60, g120, g250, g750, g2k, g5k, g15k]`
     * Wire frame: `13 04 09 00 13 04 [g60] [g120] [g250] [g750] [g2k] [g5k] [g15k]`
     */
    fun sendLuxe7BandCustomEq(
        gainsDb: List<Float>,
        roomPlacement: RoomPlacement = RoomPlacement.FREE_STANDING,
        enabled: Boolean = true
    ) {
        // 1. Enable/Disable EQ state (`Lh80.F(enabled)` -> opcode 0x13, payload [0x02, 0x01/0x02])
        val switchFrame = encodeBesFrame(
            cmdType = 0x13,
            isRead = false,
            payload = byteArrayOf(0x02, if (enabled) 0x01 else 0x02)
        )
        appendLog("TX", "BES EQ Switch -> ${if (enabled) "ENABLED" else "BYPASS"}", switchFrame.toHex())
        sendRawBytes(switchFrame)

        if (!enabled) return

        // 2. Compute 7 hardware band bytes with Room Placement boundary compensation
        val payload = ByteArray(8)
        payload[0] = 0x04 // Custom 7-band write sub-command from Lh80.h1
        for (i in 0 until 7) {
            val base = gainsDb.getOrElse(i) { 0f }
            val roomOffset = when (i) {
                0, 1 -> roomPlacement.bassTiltDb
                2 -> roomPlacement.bassTiltDb * 0.4f
                5, 6 -> roomPlacement.trebleTiltDb
                else -> 0f
            }
            val finalDb = (base + roomOffset).roundToInt().coerceIn(-12, 12)
            payload[i + 1] = finalDb.toByte()
        }

        val eqFrame = encodeBesFrame(cmdType = 0x13, isRead = false, payload = payload)
        val summary = (0 until 7).joinToString("/") { "${payload[it + 1]}dB" }
        appendLog("TX", "Nirvana Luxe 7-Band EQ ($summary)", eqFrame.toHex())
        sendRawBytes(eqFrame)
    }

    /**
     * 3. Nirvana Luxe Sound Profile (`Lh80.K1`, opcode `0x1D` = `29`)
     * Wire frame: `13 04 03 00 1D [modeByte] [subByte]`
     */
    fun sendLuxeSoundMode(mode: LuxeSoundMode) {
        val frame = encodeBesFrame(
            cmdType = 0x1D,
            isRead = false,
            payload = byteArrayOf(mode.besModeByte, mode.besSubByte)
        )
        appendLog("TX", "Sound Profile -> ${mode.label}", frame.toHex())
        sendRawBytes(frame)
    }

    /**
     * 4. Nirvana Luxe LED Light Show Mode (`Lh80.A1` / `Lh80.B1`, opcode `0x1C` = `28`)
     * Wire frame: `13 04 03 00 1C 02 [ledOpcodeByte]`
     */
    fun sendLuxeLedMode(mode: LuxeLedMode) {
        val frame = encodeBesFrame(
            cmdType = 0x1C,
            isRead = false,
            payload = byteArrayOf(0x02, mode.besOpcodeByte)
        )
        appendLog("TX", "Luxe LED Show -> ${mode.label}", frame.toHex())
        sendRawBytes(frame)
    }

    /**
     * 5. Nirvana Luxe Broadcast / PartySync Multi-Speaker Mode (`Lh80.N1` / `Lh80$e1`, opcode `0x85` = `-123`)
     * Wire frame: `13 04 03 00 85 01 [01/00]`
     */
    fun sendLuxeBroadcastPartySync(enabled: Boolean) {
        val frame = encodeBesFrame(
            cmdType = 0x85,
            isRead = false,
            payload = byteArrayOf(0x01, if (enabled) 0x01 else 0x00)
        )
        appendLog("TX", "Broadcast PartySync -> ${if (enabled) "ACTIVE" else "OFF"}", frame.toHex())
        sendRawBytes(frame)
    }

    /**
     * 6. Nirvana Luxe Dual Multipoint Pairing (`Lh80.C1`, opcode `0x16` = `22`)
     * Wire frame: `13 04 03 00 16 02 [01/02]`
     */
    fun sendLuxeMultipoint(enabled: Boolean) {
        val frame = encodeBesFrame(
            cmdType = 0x16,
            isRead = false,
            payload = byteArrayOf(0x02, if (enabled) 0x01 else 0x02)
        )
        appendLog("TX", "Dual Multipoint -> ${if (enabled) "ENABLED" else "SINGLE"}", frame.toHex())
        sendRawBytes(frame)
    }

    fun sendCustomHexFrame(hexString: String): Boolean {
        val cleaned = hexString.replace(" ", "").replace(":", "").replace("-", "").trim()
        if (cleaned.isEmpty() || cleaned.length % 2 != 0) return false
        val bytes = runCatching {
            ByteArray(cleaned.length / 2) { i ->
                cleaned.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        }.getOrNull() ?: return false
        appendLog("TX", "Custom BES Hex Frame", bytes.toHex())
        sendRawBytes(bytes)
        return true
    }

    fun clearLogs() {
        _packetLogs.value = emptyList()
    }

    fun disconnectCurrent() {
        stopBleScan()
        runCatching { sppIn?.close() }
        runCatching { sppOut?.close() }
        runCatching { sppSocket?.close() }
        runCatching { activeGatt?.disconnect(); activeGatt?.close() }
        sppSocket = null
        sppOut = null
        sppIn = null
        activeGatt = null
        activeWriteChar = null
    }

    private fun sendRawBytes(bytes: ByteArray) {
        scope.launch {
            runCatching {
                val out = sppOut
                if (out != null && sppSocket?.isConnected == true) {
                    out.write(bytes)
                    out.flush()
                    return@runCatching
                }
                val gatt = activeGatt
                val ch = activeWriteChar
                if (gatt != null && ch != null) {
                    ch.value = bytes
                    gatt.writeCharacteristic(ch)
                }
            }
        }
    }

    private fun isDeviceConnected(device: BluetoothDevice): Boolean {
        return runCatching {
            val method = device.javaClass.getMethod("isConnected")
            (method.invoke(device) as? Boolean) ?: false
        }.getOrDefault(false)
    }

    private fun getDeviceBatteryLevel(device: BluetoothDevice): Int {
        return runCatching {
            val method = device.javaClass.getMethod("getBatteryLevel")
            (method.invoke(device) as? Int) ?: -1
        }.getOrDefault(-1)
    }

    private fun appendLog(direction: String, title: String, hex: String) {
        val fmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        val entry = ProtocolPacketLog(
            timestamp = fmt.format(Date()),
            direction = direction,
            commandTitle = title,
            hexPayload = hex
        )
        _packetLogs.value = (listOf(entry) + _packetLogs.value).take(80)
    }

    private fun ByteArray.toHex(): String = joinToString(" ") { "%02X".format(it) }
}
