package com.boat.hearables.pro.data

enum class ChipsetSdk(
    val displayName: String,
    val badge: String,
    val sppUuid: String,
    val bleServiceUuid: String,
    val bleWriteUuid: String,
    val bleNotifyUuid: String,
    val protocolSummary: String
) {
    BLUETRUM(
        displayName = "Bluetrum AB56xx / SmartCase",
        badge = "BLUETRUM SDK",
        sppUuid = "B6632277-0642-458B-A7A0-23FB1DC92C93",
        bleServiceUuid = "0000ab00-0000-1000-8000-00805f9b34fb",
        bleWriteUuid = "0000ab01-0000-1000-8000-00805f9b34fb",
        bleNotifyUuid = "0000ab01-0000-1000-8000-00805f9b34fb",
        protocolSummary = "TLV/Opcode framing: 0x20 10-Band Custom EQ, 0x22 Key Remap, 0x25 BEAST Mode, 0x26 In-Ear, 0x27 DeviceInfo TLV, 0x2A Find Buds, 0x2C/0x35 ANC & Gain, 0x32 Spatial 3D, 0x33 Multipoint, 0x36 Bass Engine"
    ),
    BES(
        displayName = "Bestechnic BES2500 / BES2600",
        badge = "BES SDK",
        sppUuid = "00001101-0000-1000-8000-00805F9B34FB",
        bleServiceUuid = "66666666-6666-6666-6666-666666666666",
        bleWriteUuid = "77777777-7777-7777-7777-777777777777",
        bleNotifyUuid = "77777777-7777-7777-7777-777777777777",
        protocolSummary = "BES/Excelpoint GATT & SPP: Multi-level ANC (High/Mid/Low/Ambient), Custom FreqAmp EQ, In-Ear (0x07), Multipoint (0x1C), Gaming Mode (0x1E), Dolby/Spatial Head-Tracking"
    ),
    WUQI(
        displayName = "Wuqi WQ7033 / WQ7034",
        badge = "WUQI SDK",
        sppUuid = "00007034-0000-1000-8000-00805F9B34FB",
        bleServiceUuid = "0000fff0-0000-1000-8000-00805f9b34fb",
        bleWriteUuid = "0000ffa1-0000-1000-8000-00805f9b34fb",
        bleNotifyUuid = "0000ffa2-0000-1000-8000-00805f9b34fb",
        protocolSummary = "08 EE framing: 08EE...811400 Custom EQ, 08EE...02060B ANC/Ambient, 08EE...02070B BEAST Mode, 08EE...02010D Touch Key Remap"
    ),
    JIELI(
        displayName = "JieLi AC693x / AC696x / AC697x",
        badge = "JIELI RCSP",
        sppUuid = "0000fe01-0000-1000-8000-00805f9b34fb",
        bleServiceUuid = "0000ae00-0000-1000-8000-00805f9b34fb",
        bleWriteUuid = "0000ae01-0000-1000-8000-00805f9b34fb",
        bleNotifyUuid = "0000ae02-0000-1000-8000-00805f9b34fb",
        protocolSummary = "JieLi RCSP (FE DC BA ... EF): Opcode 0x07 10-Band Dynamic EQ, ANC/Transparency, Touch Key JSON Schema, OTA"
    ),
    AIROHA(
        displayName = "Airoha (MediaTek) AB156x / AB158x",
        badge = "AIROHA RACE",
        sppUuid = "00000000-0000-0000-0099-AABBCCDDEEFF",
        bleServiceUuid = "5052494D-2DAB-0341-6972-6F6861424C45",
        bleWriteUuid = "43484152-2DAB-3241-6972-6F6861424C45",
        bleNotifyUuid = "43484152-2DAB-3141-6972-6F6861424C45",
        protocolSummary = "Airoha RACE (05 5A): 10-Band Parametric PEQ, Hybrid ANC Filter, Sidetone, Seal Check, Find My Buds, Multipoint"
    ),
    CEVA_ZIPLINE(
        displayName = "CEVA / Zipline / Stone Lumos",
        badge = "CEVA BLE",
        sppUuid = "00001101-0000-1000-8000-00805F9B34FB",
        bleServiceUuid = "0000FF20-0000-1000-8000-00805F9B34FB",
        bleWriteUuid = "0000FF21-0000-1000-8000-00805F9B34FB",
        bleNotifyUuid = "0000FF22-0000-1000-8000-00805F9B34FB",
        protocolSummary = "FF10/FF20 GATT control for ANC, Custom EQ, RGB LED patterns (Stone Lumos / PartyPal)"
    ),
    GAIA(
        displayName = "Qualcomm GAIA QCC30xx / QCC51xx",
        badge = "QUALCOMM GAIA",
        sppUuid = "00001107-D102-11E1-9B23-00025B00A5A5",
        bleServiceUuid = "00001100-D102-11E1-9B23-00025B00A5A5",
        bleWriteUuid = "00001101-D102-11E1-9B23-00025B00A5A5",
        bleNotifyUuid = "00001102-D102-11E1-9B23-00025B00A5A5",
        protocolSummary = "Qualcomm GAIA v3 User EQ bank, Adaptive ANC, aptX / LDAC toggle"
    ),
    UNIVERSAL_DSP(
        displayName = "Universal Android 10-Band DSP + HFP",
        badge = "SYSTEM DSP",
        sppUuid = "00001101-0000-1000-8000-00805F9B34FB",
        bleServiceUuid = "0000180f-0000-1000-8000-00805f9b34fb",
        bleWriteUuid = "00002a19-0000-1000-8000-00805f9b34fb",
        bleNotifyUuid = "00002a19-0000-1000-8000-00805f9b34fb",
        protocolSummary = "Hardware-agnostic Android DynamicsProcessing 10-Band Parametric EQ + Multi-Band Limiter + BassBoost + 360 Virtualizer + HFP Battery"
    )
}

enum class DeviceCategory(val label: String) {
    TWS_EARBUDS("TWS Earbuds"),
    NECKBAND("Neckband"),
    HEADPHONES("Over-Ear Headphones"),
    SPEAKER("Bluetooth Speaker")
}

enum class AncMode(val label: String, val subtitle: String, val bluetrumCode: Int, val wuqiCode: Int) {
    ANC_ON("ANC Active", "Maximum external noise cancellation", 1, 1),
    ADAPTIVE_ANC("Adaptive ANC", "Auto-scales cancellation to environment", 3, 1),
    TRANSPARENCY("Ambient Mode", "Let outside voice & traffic pass through", 2, 2),
    NORMAL("Standard (Off)", "Passive isolation only — saves ~20% battery", 0, 0)
}

enum class AncLevel(val label: String, val dbReduction: String, val levelByte: Int) {
    HIGH("Max Depth", "-46 to -50 dB", 3),
    MEDIUM("Balanced", "-32 dB", 2),
    LOW("Gentle / Indoor", "-20 dB", 1)
}

enum class SpatialMode(val label: String, val description: String, val code: Int) {
    OFF("Stereo (Off)", "Direct 2-channel audiophile signal path", 0),
    FIXED_360("boAt 360° Spatial", "Wide 3D soundstage expansion", 1),
    HEAD_TRACKED("Head-Tracked 3D", "IMU dynamic anchor stage", 2),
    DOLBY_MOVIE("Cinema Surround", "Enhanced dialog center & sub-bass LFE", 3),
    DOLBY_MUSIC("Concert Stage", "Natural acoustic reflections & depth", 4)
}

enum class RoomAcoustics(val label: String) {
    OFF("Bypass"),
    STUDIO("Mastering Studio"),
    CLUB("Underground Club"),
    CONCERT_HALL("Symphony Hall"),
    ARENA("Open Arena")
}

enum class TouchGesture(
    val side: String,
    val gestureName: String,
    val bluetrumKeyType: Int,
    val wuqiActionCode: Int
) {
    LEFT_SINGLE("Left Bud", "Single Tap", 1, 1),
    LEFT_DOUBLE("Left Bud", "Double Tap", 3, 2),
    LEFT_TRIPLE("Left Bud", "Triple Tap", 5, 3),
    LEFT_LONG("Left Bud", "Long Press (1.5s)", 7, 4),
    RIGHT_SINGLE("Right Bud", "Single Tap", 2, 1),
    RIGHT_DOUBLE("Right Bud", "Double Tap", 4, 2),
    RIGHT_TRIPLE("Right Bud", "Triple Tap", 6, 3),
    RIGHT_LONG("Right Bud", "Long Press (1.5s)", 8, 4)
}

enum class TouchAction(
    val label: String,
    val bluetrumFuncCode: Int,
    val jieliFuncCode: Int,
    val wuqiFuncCode: Int
) {
    PLAY_PAUSE("Play / Pause", 5, 5, 1),
    NEXT_TRACK("Next Track", 4, 4, 2),
    PREV_TRACK("Previous Track", 3, 3, 3),
    VOLUME_UP("Volume Up", 8, 8, 4),
    VOLUME_DOWN("Volume Down", 9, 9, 5),
    ANC_AMBIENT_CYCLE("Cycle ANC / Ambient / Normal", 10, 10, 9),
    BEAST_MODE_TOGGLE("Toggle BEAST™ Low-Latency Mode", 11, 11, 10),
    VOICE_ASSISTANT("Voice Assistant (Gemini / Assistant)", 12, 6, 11),
    DISABLED("Disabled (Prevent Accidental Touch)", 0, 0, 0)
}

data class EqPreset(
    val id: String,
    val name: String,
    val subtitle: String,
    val gainsDb: List<Float>, // 10 bands: 31, 63, 125, 250, 500, 1k, 2k, 4k, 8k, 16k Hz
    val isCustom: Boolean = false
)

data class BoatDeviceSpec(
    val name: String,
    val category: DeviceCategory,
    val sdk: ChipsetSdk,
    val supportsAnc: Boolean = true,
    val supportsMultiLevelAnc: Boolean = true,
    val supportsSpatialAudio: Boolean = true,
    val supportsBeastMode: Boolean = true,
    val supportsInEarDetection: Boolean = true,
    val supportsMultipoint: Boolean = true,
    val supportsTouchRemap: Boolean = true,
    val supportsSmartCase: Boolean = false,
    val supportsBassEngine: Boolean = true,
    val supportsAntiWindNoise: Boolean = true,
    val hardwareEqBands: Int = 10
)

object BoatDeviceCatalog {

    val EQ_FREQUENCIES_HZ = listOf(31, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
    val EQ_LABELS = listOf("31", "63", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

    /**
     * All built-in presets unlocked for EVERY device (combining the hardware curves extracted
     * from `com.boAt.hearables.Bluetrum.PresetDataEq` & `EqCacheUtil` with audiophile curves).
     */
    val BUILT_IN_PRESETS: List<EqPreset> = listOf(
        EqPreset(
            id = "boat_signature",
            name = "boAt Signature",
            subtitle = "Punchy sub-bass & crisp highs (Stock V-Curve)",
            gainsDb = listOf(5.0f, 6.5f, 4.0f, 0.5f, -1.5f, -1.0f, 1.5f, 3.5f, 4.5f, 4.0f)
        ),
        EqPreset(
            id = "flat",
            name = "Flat / Studio Reference",
            subtitle = "0 dB neutral response across all 10 bands",
            gainsDb = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        ),
        EqPreset(
            id = "bass_boost",
            name = "Basshead Extreme",
            subtitle = "Deep 31Hz–125Hz sub-bass slam without muddying vocals",
            gainsDb = listOf(8.5f, 8.0f, 5.5f, 2.0f, -1.0f, -0.5f, 1.0f, 2.5f, 3.0f, 2.5f)
        ),
        EqPreset(
            id = "balanced",
            name = "Balanced / Natural",
            subtitle = "Smooth Harman-inspired curve for all-day listening",
            gainsDb = listOf(2.5f, 2.0f, 0.5f, 0.0f, 0.0f, 0.5f, 1.5f, 2.0f, 1.5f, 1.0f)
        ),
        EqPreset(
            id = "pop",
            name = "Pop",
            subtitle = "Extracted from boAt PresetDataEq.EQ_GAINS_POP",
            gainsDb = listOf(-2.0f, 0.0f, 2.0f, 4.0f, -2.0f, -2.0f, 0.0f, 0.0f, 4.0f, 4.0f)
        ),
        EqPreset(
            id = "rock",
            name = "Rock",
            subtitle = "Extracted from boAt PresetDataEq.EQ_GAINS_ROCK",
            gainsDb = listOf(0.0f, 8.0f, 8.0f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 2.0f)
        ),
        EqPreset(
            id = "jazz",
            name = "Jazz",
            subtitle = "Extracted from boAt PresetDataEq.EQ_GAINS_JAZZ",
            gainsDb = listOf(0.0f, 0.0f, 0.0f, 4.0f, 4.0f, 4.0f, 0.0f, 2.0f, 3.0f, 4.0f)
        ),
        EqPreset(
            id = "classic",
            name = "Classical",
            subtitle = "Extracted from boAt PresetDataEq.EQ_GAINS_CLASSIC",
            gainsDb = listOf(3.0f, 1.0f, 0.0f, -2.0f, -4.0f, -4.0f, -2.0f, 0.0f, 1.0f, 2.0f)
        ),
        EqPreset(
            id = "club",
            name = "Club / EDM",
            subtitle = "High-energy kick drum & sparkling hi-hats",
            gainsDb = listOf(6.0f, 7.5f, 4.5f, 0.0f, -2.0f, -1.0f, 2.0f, 4.5f, 5.5f, 5.0f)
        ),
        EqPreset(
            id = "podcast_vocal",
            name = "Vocal / Podcast Clarity",
            subtitle = "Cuts low rumble & elevates 1kHz–4kHz speech intelligibility",
            gainsDb = listOf(-6.0f, -4.5f, -2.0f, 1.5f, 4.0f, 5.5f, 5.0f, 3.0f, 0.5f, -1.0f)
        ),
        EqPreset(
            id = "treble_sparkle",
            name = "Treble & Detail Boost",
            subtitle = "Opens up dark earbuds with airy 8kHz–16kHz presence",
            gainsDb = listOf(0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 2.0f, 3.5f, 5.5f, 7.0f, 6.5f)
        ),
        EqPreset(
            id = "fps_footsteps",
            name = "FPS Gaming (Footsteps)",
            subtitle = "Suppresses explosion rumble; highlights 2kHz–6kHz footsteps & reloads",
            gainsDb = listOf(-5.0f, -3.5f, -1.0f, 1.0f, 2.5f, 4.5f, 6.5f, 6.0f, 3.0f, 1.0f)
        ),
        EqPreset(
            id = "hip_hop",
            name = "Hip-Hop & 808s",
            subtitle = "Deep 31Hz–63Hz 808 sub-bass extension with crisp vocals",
            gainsDb = listOf(7.5f, 6.5f, 3.0f, -1.0f, -1.5f, 1.0f, 2.5f, 3.5f, 4.0f, 3.5f)
        ),
        EqPreset(
            id = "late_night",
            name = "Late Night / Warm",
            subtitle = "Fletcher-Munson low-volume compensation & fatigue-free highs",
            gainsDb = listOf(4.5f, 3.5f, 2.0f, 0.5f, 0.0f, -0.5f, -1.5f, -2.5f, -1.0f, 1.0f)
        )
    )

    /**
     * Complete catalog of 95+ boAt Hearables models reverse-engineered from `boAt Hearables 1.9.0.apk`.
     */
    val DEVICES: List<BoatDeviceSpec> = listOf(
        // --- NIRVANA SERIES ---
        BoatDeviceSpec("Nirvana Ivy Pro", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES, supportsSmartCase = false),
        BoatDeviceSpec("Nirvana Ivy", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Nirvana Zenith Pro", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Nirvana Crown", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM, supportsSmartCase = true),
        BoatDeviceSpec("Nirvana Ion ANC Pro", DeviceCategory.TWS_EARBUDS, ChipsetSdk.WUQI),
        BoatDeviceSpec("Nirvana Ion ANC 2", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Nirvana Ion ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.WUQI),
        BoatDeviceSpec("Nirvana Ion V2", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Nirvana Ion", DeviceCategory.TWS_EARBUDS, ChipsetSdk.WUQI, supportsAnc = false),
        BoatDeviceSpec("Nirvana Space 2", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Nirvana Space", DeviceCategory.TWS_EARBUDS, ChipsetSdk.AIROHA),
        BoatDeviceSpec("Nirvana X TWS", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Nirvana Crystl", DeviceCategory.TWS_EARBUDS, ChipsetSdk.WUQI),
        BoatDeviceSpec("Nirvana Iris", DeviceCategory.TWS_EARBUDS, ChipsetSdk.WUQI),
        BoatDeviceSpec("Nirvana Lucid", DeviceCategory.TWS_EARBUDS, ChipsetSdk.AIROHA),
        BoatDeviceSpec("Nirvana Luxe", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Nirvana 525ANC", DeviceCategory.NECKBAND, ChipsetSdk.BES),
        BoatDeviceSpec("Nirvana Eutopia 2 ANC", DeviceCategory.HEADPHONES, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Nirvana Eutopia", DeviceCategory.HEADPHONES, ChipsetSdk.AIROHA),

        // --- AIRDOPES PRIME & PLUS SERIES ---
        BoatDeviceSpec("Airdopes Prime Supreme", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Airdopes Prime 800D", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Airdopes Prime 702 ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Airdopes Prime 701 ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Airdopes Prime 700 ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Airdopes Prime 513 ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Airdopes Prime 512", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES, supportsAnc = false),
        BoatDeviceSpec("Airdopes Prime 511", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES, supportsAnc = false),
        BoatDeviceSpec("Airdopes Prime 413", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES, supportsAnc = false),
        BoatDeviceSpec("Airdopes Prime 412", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES, supportsAnc = false),
        BoatDeviceSpec("Airdopes Prime 300", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES, supportsAnc = false),
        BoatDeviceSpec("Airdopes Prime Raga", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Airdopes Plus 422 ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Airdopes Plus 421 ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Airdopes Plus Spark ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Airdopes Plus Aura", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM, supportsAnc = false),
        BoatDeviceSpec("Airdopes Plus Neo", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM, supportsAnc = false),
        BoatDeviceSpec("Airdopes Plus Wave", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM, supportsAnc = false),
        BoatDeviceSpec("Airdopes Plus 224", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM, supportsAnc = false),
        BoatDeviceSpec("Airdopes Plus 223", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM, supportsAnc = false),
        BoatDeviceSpec("Airdopes Plus 222", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM, supportsAnc = false),
        BoatDeviceSpec("Airdopes Plus 221", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM, supportsAnc = false),

        // --- AIRDOPES CORE SERIES ---
        BoatDeviceSpec("Airdopes 800 HiDef", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Airdopes 800", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Airdopes Supreme", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Airdopes Ultra Pro", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Airdopes Flex 454 ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.AIROHA),
        BoatDeviceSpec("Airdopes 423 ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Airdopes 393ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Airdopes 341ANC", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Airdopes 300", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BES),
        BoatDeviceSpec("Airdopes ProClip", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM, supportsAnc = false),
        BoatDeviceSpec("Airdopes Clip", DeviceCategory.TWS_EARBUDS, ChipsetSdk.BLUETRUM, supportsAnc = false),
        BoatDeviceSpec("Airdopes 441", DeviceCategory.TWS_EARBUDS, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Airdopes 281 Pro", DeviceCategory.TWS_EARBUDS, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Airdopes 161", DeviceCategory.TWS_EARBUDS, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Airdopes 141", DeviceCategory.TWS_EARBUDS, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Airdopes 131", DeviceCategory.TWS_EARBUDS, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Airdopes Alpha", DeviceCategory.TWS_EARBUDS, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),

        // --- ROCKERZ NECKBANDS & HEADPHONES ---
        BoatDeviceSpec("Rockerz 650 Pro", DeviceCategory.HEADPHONES, ChipsetSdk.BES),
        BoatDeviceSpec("Rockerz 551 ANC Pro", DeviceCategory.HEADPHONES, ChipsetSdk.JIELI),
        BoatDeviceSpec("Rockerz Plus 550 ANC", DeviceCategory.HEADPHONES, ChipsetSdk.CEVA_ZIPLINE),
        BoatDeviceSpec("Rockerz 512 ANC", DeviceCategory.HEADPHONES, ChipsetSdk.CEVA_ZIPLINE),
        BoatDeviceSpec("Rockerz Plus 450 ANC", DeviceCategory.HEADPHONES, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Rockerz 450 Pro", DeviceCategory.HEADPHONES, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Rockerz 450R", DeviceCategory.HEADPHONES, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Rockerz 450", DeviceCategory.HEADPHONES, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Rockerz Trinity Grande", DeviceCategory.NECKBAND, ChipsetSdk.WUQI),
        BoatDeviceSpec("Rockerz Trinity Gen 2", DeviceCategory.NECKBAND, ChipsetSdk.WUQI),
        BoatDeviceSpec("Rockerz Prime 415", DeviceCategory.NECKBAND, ChipsetSdk.BES),
        BoatDeviceSpec("Rockerz 351 ANC", DeviceCategory.NECKBAND, ChipsetSdk.BLUETRUM),
        BoatDeviceSpec("Rockerz 301 ANC", DeviceCategory.NECKBAND, ChipsetSdk.CEVA_ZIPLINE),
        BoatDeviceSpec("Rockerz Zen ANC", DeviceCategory.NECKBAND, ChipsetSdk.CEVA_ZIPLINE),
        BoatDeviceSpec("Rockerz 255 ANC", DeviceCategory.NECKBAND, ChipsetSdk.WUQI),
        BoatDeviceSpec("Rockerz 255 Z Plus", DeviceCategory.NECKBAND, ChipsetSdk.BES),
        BoatDeviceSpec("Rockerz 255 Max", DeviceCategory.NECKBAND, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Rockerz 255 Pro+", DeviceCategory.NECKBAND, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Rockerz Prime 255Z", DeviceCategory.NECKBAND, ChipsetSdk.BES, supportsAnc = false),
        BoatDeviceSpec("Rockerz Prime 205", DeviceCategory.NECKBAND, ChipsetSdk.BES, supportsAnc = false),
        BoatDeviceSpec("Rockerz 210 ANC", DeviceCategory.NECKBAND, ChipsetSdk.BES),
        BoatDeviceSpec("Rockerz 330", DeviceCategory.NECKBAND, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Rockerz 150 Pro", DeviceCategory.NECKBAND, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),
        BoatDeviceSpec("Rockerz 103 Pro", DeviceCategory.NECKBAND, ChipsetSdk.UNIVERSAL_DSP, supportsAnc = false),

        // --- STONE & PARTYPAL SPEAKERS ---
        BoatDeviceSpec("Stone 1100", DeviceCategory.SPEAKER, ChipsetSdk.CEVA_ZIPLINE, supportsAnc = false, supportsInEarDetection = false),
        BoatDeviceSpec("Stone Lumos Pro Hogwarts", DeviceCategory.SPEAKER, ChipsetSdk.CEVA_ZIPLINE, supportsAnc = false, supportsInEarDetection = false),
        BoatDeviceSpec("Stone Lumos Pro The Deathly Hallows", DeviceCategory.SPEAKER, ChipsetSdk.CEVA_ZIPLINE, supportsAnc = false, supportsInEarDetection = false),
        BoatDeviceSpec("Stone Lumos", DeviceCategory.SPEAKER, ChipsetSdk.CEVA_ZIPLINE, supportsAnc = false, supportsInEarDetection = false),
        BoatDeviceSpec("Stone Arc Pro Plus", DeviceCategory.SPEAKER, ChipsetSdk.JIELI, supportsAnc = false, supportsInEarDetection = false),
        BoatDeviceSpec("Stone Arc Pro", DeviceCategory.SPEAKER, ChipsetSdk.JIELI, supportsAnc = false, supportsInEarDetection = false),
        BoatDeviceSpec("Stone 358 Pro", DeviceCategory.SPEAKER, ChipsetSdk.JIELI, supportsAnc = false, supportsInEarDetection = false),
        BoatDeviceSpec("Stone 352 Pro", DeviceCategory.SPEAKER, ChipsetSdk.JIELI, supportsAnc = false, supportsInEarDetection = false),
        BoatDeviceSpec("Stone 350 Pro", DeviceCategory.SPEAKER, ChipsetSdk.JIELI, supportsAnc = false, supportsInEarDetection = false),
        BoatDeviceSpec("PartyPal 600", DeviceCategory.SPEAKER, ChipsetSdk.CEVA_ZIPLINE, supportsAnc = false, supportsInEarDetection = false)
    )

    fun resolveDeviceSpec(btName: String?): BoatDeviceSpec {
        if (btName.isNullOrBlank()) return DEVICES.first()
        val normalized = btName.trim()
        DEVICES.firstOrNull { it.name.equals(normalized, ignoreCase = true) }?.let { return it }
        DEVICES.firstOrNull {
            normalized.contains(it.name, ignoreCase = true) ||
                it.name.contains(normalized.removePrefix("boAt ").trim(), ignoreCase = true)
        }?.let { return it }

        val guessedCategory = when {
            normalized.contains("Rockerz", ignoreCase = true) &&
                (normalized.contains("450") || normalized.contains("550") || normalized.contains("551") || normalized.contains("650")) ->
                DeviceCategory.HEADPHONES
            normalized.contains("Rockerz", ignoreCase = true) -> DeviceCategory.NECKBAND
            normalized.contains("Stone", ignoreCase = true) || normalized.contains("PartyPal", ignoreCase = true) ->
                DeviceCategory.SPEAKER
            else -> DeviceCategory.TWS_EARBUDS
        }
        val guessedSdk = when {
            normalized.contains("Crown", ignoreCase = true) || normalized.contains("Plus", ignoreCase = true) -> ChipsetSdk.BLUETRUM
            normalized.contains("Ion", ignoreCase = true) || normalized.contains("Trinity", ignoreCase = true) -> ChipsetSdk.WUQI
            normalized.contains("Ivy", ignoreCase = true) || normalized.contains("Prime", ignoreCase = true) || normalized.contains("Zenith", ignoreCase = true) -> ChipsetSdk.BES
            else -> ChipsetSdk.BLUETRUM
        }
        return BoatDeviceSpec(
            name = normalized,
            category = guessedCategory,
            sdk = guessedSdk
        )
    }
}
