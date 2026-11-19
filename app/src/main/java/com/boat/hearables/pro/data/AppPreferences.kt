package com.boat.hearables.pro.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class AccentTheme(
    val id: String,
    val label: String,
    val primaryHex: Long,
    val secondaryHex: Long
) {
    BOAT_CRIMSON("crimson", "boAt Crimson Red", 0xFFFF1E42, 0xFFFF6B81),
    ELECTRIC_CYAN("cyan", "Electric Cyan", 0xFF00E5FF, 0xFF64FFDA),
    NEON_VIOLET("violet", "Neon Violet", 0xFF9D4EDD, 0xFFE0AAFF),
    EMERALD_MATRIX("emerald", "Emerald Matrix", 0xFF00E676, 0xFF69F0AE),
    AMBER_GOLD("amber", "Amber Gold", 0xFFFFB300, 0xFFFFE57F)
}

data class DeviceAutoProfile(
    val deviceKey: String,
    val enabled: Boolean = false,
    val eqPresetId: String = "boat_signature",
    val ancMode: AncMode = AncMode.ANC_ON,
    val beastMode: Boolean = false,
    val targetVolumePercent: Int = 70
)

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("boat_hearables_pro_prefs", Context.MODE_PRIVATE)

    var selectedDeviceName: String
        get() = prefs.getString("selected_device_name", "Nirvana Ivy Pro") ?: "Nirvana Ivy Pro"
        set(value) = prefs.edit().putString("selected_device_name", value).apply()

    var chipsetOverride: ChipsetSdk?
        get() {
            val raw = prefs.getString("chipset_override", null) ?: return null
            return runCatching { ChipsetSdk.valueOf(raw) }.getOrNull()
        }
        set(value) = prefs.edit().putString("chipset_override", value?.name).apply()

    var activePresetId: String
        get() = prefs.getString("active_preset_id", "boat_signature") ?: "boat_signature"
        set(value) = prefs.edit().putString("active_preset_id", value).apply()

    var currentBandGains: List<Float>
        get() {
            val raw = prefs.getString("current_band_gains", null)
                ?: return BoatDeviceCatalog.BUILT_IN_PRESETS.first().gainsDb
            return runCatching {
                val arr = JSONArray(raw)
                List(10) { idx -> arr.optDouble(idx, 0.0).toFloat().coerceIn(-12f, 12f) }
            }.getOrElse { BoatDeviceCatalog.BUILT_IN_PRESETS.first().gainsDb }
        }
        set(value) {
            val arr = JSONArray()
            value.take(10).forEach { arr.put(it.toDouble()) }
            prefs.edit().putString("current_band_gains", arr.toString()).apply()
        }

    var preAmpDb: Float
        get() = prefs.getFloat("pre_amp_db", 0f).coerceIn(-12f, 12f)
        set(value) = prefs.edit().putFloat("pre_amp_db", value.coerceIn(-12f, 12f)).apply()

    var qFactor: Float
        get() = prefs.getFloat("q_factor", 1.41f).coerceIn(0.5f, 4.0f)
        set(value) = prefs.edit().putFloat("q_factor", value.coerceIn(0.5f, 4.0f)).apply()

    var limiterEnabled: Boolean
        get() = prefs.getBoolean("limiter_enabled", true)
        set(value) = prefs.edit().putBoolean("limiter_enabled", value).apply()

    var bassBoostPercent: Int
        get() = prefs.getInt("bass_boost_percent", 25).coerceIn(0, 100)
        set(value) = prefs.edit().putInt("bass_boost_percent", value.coerceIn(0, 100)).apply()

    var subBassCutoffHz: Int
        get() = prefs.getInt("sub_bass_cutoff_hz", 65).coerceIn(40, 120)
        set(value) = prefs.edit().putInt("sub_bass_cutoff_hz", value.coerceIn(40, 120)).apply()

    var virtualizerPercent: Int
        get() = prefs.getInt("virtualizer_percent", 20).coerceIn(0, 100)
        set(value) = prefs.edit().putInt("virtualizer_percent", value.coerceIn(0, 100)).apply()

    var loudnessPercent: Int
        get() = prefs.getInt("loudness_percent", 0).coerceIn(0, 100)
        set(value) = prefs.edit().putInt("loudness_percent", value.coerceIn(0, 100)).apply()

    var channelBalance: Int
        get() = prefs.getInt("channel_balance", 0).coerceIn(-100, 100)
        set(value) = prefs.edit().putInt("channel_balance", value.coerceIn(-100, 100)).apply()

    var roomAcoustics: RoomAcoustics
        get() = runCatching {
            RoomAcoustics.valueOf(prefs.getString("room_acoustics", RoomAcoustics.OFF.name)!!)
        }.getOrDefault(RoomAcoustics.OFF)
        set(value) = prefs.edit().putString("room_acoustics", value.name).apply()

    var ancMode: AncMode
        get() = runCatching {
            AncMode.valueOf(prefs.getString("anc_mode", AncMode.ANC_ON.name)!!)
        }.getOrDefault(AncMode.ANC_ON)
        set(value) = prefs.edit().putString("anc_mode", value.name).apply()

    var ancLevel: AncLevel
        get() = runCatching {
            AncLevel.valueOf(prefs.getString("anc_level", AncLevel.HIGH.name)!!)
        }.getOrDefault(AncLevel.HIGH)
        set(value) = prefs.edit().putString("anc_level", value.name).apply()

    var antiWindNoise: Boolean
        get() = prefs.getBoolean("anti_wind_noise", true)
        set(value) = prefs.edit().putBoolean("anti_wind_noise", value).apply()

    var beastMode: Boolean
        get() = prefs.getBoolean("beast_mode", false)
        set(value) = prefs.edit().putBoolean("beast_mode", value).apply()

    var spatialMode: SpatialMode
        get() = runCatching {
            SpatialMode.valueOf(prefs.getString("spatial_mode", SpatialMode.OFF.name)!!)
        }.getOrDefault(SpatialMode.OFF)
        set(value) = prefs.edit().putString("spatial_mode", value.name).apply()

    var inEarDetection: Boolean
        get() = prefs.getBoolean("in_ear_detection", true)
        set(value) = prefs.edit().putBoolean("in_ear_detection", value).apply()

    var multipointEnabled: Boolean
        get() = prefs.getBoolean("multipoint_enabled", true)
        set(value) = prefs.edit().putBoolean("multipoint_enabled", value).apply()

    var smartTalkEnabled: Boolean
        get() = prefs.getBoolean("smart_talk_enabled", false)
        set(value) = prefs.edit().putBoolean("smart_talk_enabled", value).apply()

    var bedTouchLock: Boolean
        get() = prefs.getBoolean("bed_touch_lock", false)
        set(value) = prefs.edit().putBoolean("bed_touch_lock", value).apply()

    var accentTheme: AccentTheme
        get() = runCatching {
            AccentTheme.valueOf(prefs.getString("accent_theme", AccentTheme.BOAT_CRIMSON.name)!!)
        }.getOrDefault(AccentTheme.BOAT_CRIMSON)
        set(value) = prefs.edit().putString("accent_theme", value.name).apply()

    var oledPitchBlack: Boolean
        get() = prefs.getBoolean("oled_pitch_black", true)
        set(value) = prefs.edit().putBoolean("oled_pitch_black", value).apply()

    var hapticSliders: Boolean
        get() = prefs.getBoolean("haptic_sliders", true)
        set(value) = prefs.edit().putBoolean("haptic_sliders", value).apply()

    var lowBatteryAlertPercent: Int
        get() = prefs.getInt("low_battery_alert_percent", 20).coerceIn(5, 50)
        set(value) = prefs.edit().putInt("low_battery_alert_percent", value.coerceIn(5, 50)).apply()

    fun getCustomPresets(): List<EqPreset> {
        val raw = prefs.getString("custom_eq_presets_json", "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val gainsArr = obj.getJSONArray("gains")
                    val gains = List(10) { idx -> gainsArr.optDouble(idx, 0.0).toFloat().coerceIn(-12f, 12f) }
                    add(
                        EqPreset(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            subtitle = obj.optString("subtitle", "User Custom 10-Band Profile"),
                            gainsDb = gains,
                            isCustom = true
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveCustomPresets(presets: List<EqPreset>) {
        val arr = JSONArray()
        presets.forEach { preset ->
            val obj = JSONObject()
            obj.put("id", preset.id)
            obj.put("name", preset.name)
            obj.put("subtitle", preset.subtitle)
            val gainsArr = JSONArray()
            preset.gainsDb.take(10).forEach { gainsArr.put(it.toDouble()) }
            obj.put("gains", gainsArr)
            arr.put(obj)
        }
        prefs.edit().putString("custom_eq_presets_json", arr.toString()).apply()
    }

    fun exportAllPresetsJson(): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("device", selectedDeviceName)
        root.put("preAmpDb", preAmpDb.toDouble())
        root.put("qFactor", qFactor.toDouble())
        root.put("bassBoostPercent", bassBoostPercent)
        root.put("virtualizerPercent", virtualizerPercent)
        val gainsArr = JSONArray()
        currentBandGains.forEach { gainsArr.put(it.toDouble()) }
        root.put("activeGains", gainsArr)
        root.put("customPresets", JSONArray(prefs.getString("custom_eq_presets_json", "[]") ?: "[]"))
        return root.toString(2)
    }

    fun importPresetsJson(jsonString: String): Boolean {
        return runCatching {
            val root = JSONObject(jsonString)
            if (root.has("activeGains")) {
                val g = root.getJSONArray("activeGains")
                if (g.length() == 10) {
                    currentBandGains = List(10) { idx -> g.optDouble(idx, 0.0).toFloat().coerceIn(-12f, 12f) }
                }
            }
            if (root.has("preAmpDb")) preAmpDb = root.optDouble("preAmpDb", 0.0).toFloat()
            if (root.has("qFactor")) qFactor = root.optDouble("qFactor", 1.41).toFloat()
            if (root.has("bassBoostPercent")) bassBoostPercent = root.optInt("bassBoostPercent", 25)
            if (root.has("virtualizerPercent")) virtualizerPercent = root.optInt("virtualizerPercent", 20)
            if (root.has("customPresets")) {
                val incoming = root.getJSONArray("customPresets")
                prefs.edit().putString("custom_eq_presets_json", incoming.toString()).apply()
            }
            true
        }.getOrDefault(false)
    }

    fun getTouchMappings(): Map<TouchGesture, TouchAction> {
        val defaults = mapOf(
            TouchGesture.LEFT_SINGLE to TouchAction.PLAY_PAUSE,
            TouchGesture.RIGHT_SINGLE to TouchAction.PLAY_PAUSE,
            TouchGesture.LEFT_DOUBLE to TouchAction.PREV_TRACK,
            TouchGesture.RIGHT_DOUBLE to TouchAction.NEXT_TRACK,
            TouchGesture.LEFT_TRIPLE to TouchAction.BEAST_MODE_TOGGLE,
            TouchGesture.RIGHT_TRIPLE to TouchAction.VOICE_ASSISTANT,
            TouchGesture.LEFT_LONG to TouchAction.ANC_AMBIENT_CYCLE,
            TouchGesture.RIGHT_LONG to TouchAction.ANC_AMBIENT_CYCLE
        )
        return TouchGesture.entries.associateWith { gesture ->
            val saved = prefs.getString("touch_${gesture.name}", null)
            saved?.let { runCatching { TouchAction.valueOf(it) }.getOrNull() } ?: defaults.getValue(gesture)
        }
    }

    fun setTouchMapping(gesture: TouchGesture, action: TouchAction) {
        prefs.edit().putString("touch_${gesture.name}", action.name).apply()
    }

    fun getAutoProfile(deviceKey: String): DeviceAutoProfile {
        val raw = prefs.getString("auto_profile_$deviceKey", null) ?: return DeviceAutoProfile(deviceKey)
        return runCatching {
            val obj = JSONObject(raw)
            DeviceAutoProfile(
                deviceKey = deviceKey,
                enabled = obj.optBoolean("enabled", false),
                eqPresetId = obj.optString("eqPresetId", "boat_signature"),
                ancMode = runCatching { AncMode.valueOf(obj.optString("ancMode", AncMode.ANC_ON.name)) }.getOrDefault(AncMode.ANC_ON),
                beastMode = obj.optBoolean("beastMode", false),
                targetVolumePercent = obj.optInt("targetVolumePercent", 70).coerceIn(10, 100)
            )
        }.getOrDefault(DeviceAutoProfile(deviceKey))
    }

    fun saveAutoProfile(profile: DeviceAutoProfile) {
        val obj = JSONObject().apply {
            put("enabled", profile.enabled)
            put("eqPresetId", profile.eqPresetId)
            put("ancMode", profile.ancMode.name)
            put("beastMode", profile.beastMode)
            put("targetVolumePercent", profile.targetVolumePercent)
        }
        prefs.edit().putString("auto_profile_${profile.deviceKey}", obj.toString()).apply()
    }
}
