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
    LUXE_GOLD("luxe_gold", "Luxe Champagne Gold", 0xFFFFB300, 0xFFFFE57F),
    BOAT_CRIMSON("crimson", "boAt Crimson Red", 0xFFFF1E42, 0xFFFF6B81),
    ELECTRIC_CYAN("cyan", "Electric Cyan", 0xFF00E5FF, 0xFF64FFDA),
    NEON_VIOLET("violet", "Neon Violet", 0xFF9D4EDD, 0xFFE0AAFF),
    EMERALD_MATRIX("emerald", "Emerald Matrix", 0xFF00E676, 0xFF69F0AE)
}

data class LuxeAutoProfile(
    val enabled: Boolean = true,
    val eqPresetId: String = "luxe_signature",
    val soundMode: LuxeSoundMode = LuxeSoundMode.SIGNATURE_SOUND,
    val ledMode: LuxeLedMode = LuxeLedMode.MODE_ONE,
    val targetVolumePercent: Int = 65
)

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("boat_nirvana_luxe_prefs", Context.MODE_PRIVATE)

    var lastKnownLuxeMac: String?
        get() = prefs.getString("last_known_luxe_mac", null)
        set(value) = prefs.edit().putString("last_known_luxe_mac", value).apply()

    var activePresetId: String
        get() = prefs.getString("active_preset_id", "luxe_signature") ?: "luxe_signature"
        set(value) = prefs.edit().putString("active_preset_id", value).apply()

    var currentBandGains: List<Float>
        get() {
            val raw = prefs.getString("luxe_7band_gains", null)
                ?: return BoatDeviceCatalog.BUILT_IN_PRESETS.first().gainsDb
            return runCatching {
                val arr = JSONArray(raw)
                List(7) { idx -> arr.optDouble(idx, 0.0).toFloat().coerceIn(-12f, 12f) }
            }.getOrElse { BoatDeviceCatalog.BUILT_IN_PRESETS.first().gainsDb }
        }
        set(value) {
            val arr = JSONArray()
            value.take(7).forEach { arr.put(it.toDouble()) }
            prefs.edit().putString("luxe_7band_gains", arr.toString()).apply()
        }

    var hardwareEqEnabled: Boolean
        get() = prefs.getBoolean("hardware_eq_enabled", true)
        set(value) = prefs.edit().putBoolean("hardware_eq_enabled", value).apply()

    var soundMode: LuxeSoundMode
        get() = runCatching {
            LuxeSoundMode.valueOf(prefs.getString("luxe_sound_mode", LuxeSoundMode.SIGNATURE_SOUND.name)!!)
        }.getOrDefault(LuxeSoundMode.SIGNATURE_SOUND)
        set(value) = prefs.edit().putString("luxe_sound_mode", value.name).apply()

    var ledMode: LuxeLedMode
        get() = runCatching {
            LuxeLedMode.valueOf(prefs.getString("luxe_led_mode", LuxeLedMode.MODE_ONE.name)!!)
        }.getOrDefault(LuxeLedMode.MODE_ONE)
        set(value) = prefs.edit().putString("luxe_led_mode", value.name).apply()

    var roomPlacement: RoomPlacement
        get() = runCatching {
            RoomPlacement.valueOf(prefs.getString("room_placement", RoomPlacement.FREE_STANDING.name)!!)
        }.getOrDefault(RoomPlacement.FREE_STANDING)
        set(value) = prefs.edit().putString("room_placement", value.name).apply()

    var broadcastPartySync: Boolean
        get() = prefs.getBoolean("broadcast_party_sync", false)
        set(value) = prefs.edit().putBoolean("broadcast_party_sync", value).apply()

    var multipointEnabled: Boolean
        get() = prefs.getBoolean("multipoint_enabled", true)
        set(value) = prefs.edit().putBoolean("multipoint_enabled", value).apply()

    var batteryBoosterEnabled: Boolean
        get() = prefs.getBoolean("battery_booster_enabled", false)
        set(value) = prefs.edit().putBoolean("battery_booster_enabled", value).apply()

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
        get() = prefs.getInt("bass_boost_percent", 30).coerceIn(0, 100)
        set(value) = prefs.edit().putInt("bass_boost_percent", value.coerceIn(0, 100)).apply()

    var subBassCutoffHz: Int
        get() = prefs.getInt("sub_bass_cutoff_hz", 70).coerceIn(40, 120)
        set(value) = prefs.edit().putInt("sub_bass_cutoff_hz", value.coerceIn(40, 120)).apply()

    var virtualizerPercent: Int
        get() = prefs.getInt("virtualizer_percent", 25).coerceIn(0, 100)
        set(value) = prefs.edit().putInt("virtualizer_percent", value.coerceIn(0, 100)).apply()

    var loudnessPercent: Int
        get() = prefs.getInt("loudness_percent", 15).coerceIn(0, 100)
        set(value) = prefs.edit().putInt("loudness_percent", value.coerceIn(0, 100)).apply()

    var accentTheme: AccentTheme
        get() = runCatching {
            AccentTheme.valueOf(prefs.getString("accent_theme", AccentTheme.LUXE_GOLD.name)!!)
        }.getOrDefault(AccentTheme.LUXE_GOLD)
        set(value) = prefs.edit().putString("accent_theme", value.name).apply()

    var oledPitchBlack: Boolean
        get() = prefs.getBoolean("oled_pitch_black", true)
        set(value) = prefs.edit().putBoolean("oled_pitch_black", value).apply()

    var hapticSliders: Boolean
        get() = prefs.getBoolean("haptic_sliders", true)
        set(value) = prefs.edit().putBoolean("haptic_sliders", value).apply()

    fun getCustomPresets(): List<EqPreset> {
        val raw = prefs.getString("luxe_custom_eq_presets_json", "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val gainsArr = obj.getJSONArray("gains")
                    val gains = List(7) { idx -> gainsArr.optDouble(idx, 0.0).toFloat().coerceIn(-12f, 12f) }
                    add(
                        EqPreset(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            subtitle = obj.optString("subtitle", "Custom Nirvana Luxe Profile"),
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
            preset.gainsDb.take(7).forEach { gainsArr.put(it.toDouble()) }
            obj.put("gains", gainsArr)
            arr.put(obj)
        }
        prefs.edit().putString("luxe_custom_eq_presets_json", arr.toString()).apply()
    }

    fun getAutoProfile(): LuxeAutoProfile {
        val raw = prefs.getString("luxe_auto_profile", null) ?: return LuxeAutoProfile()
        return runCatching {
            val obj = JSONObject(raw)
            LuxeAutoProfile(
                enabled = obj.optBoolean("enabled", true),
                eqPresetId = obj.optString("eqPresetId", "luxe_signature"),
                soundMode = runCatching {
                    LuxeSoundMode.valueOf(obj.optString("soundMode", LuxeSoundMode.SIGNATURE_SOUND.name))
                }.getOrDefault(LuxeSoundMode.SIGNATURE_SOUND),
                ledMode = runCatching {
                    LuxeLedMode.valueOf(obj.optString("ledMode", LuxeLedMode.MODE_ONE.name))
                }.getOrDefault(LuxeLedMode.MODE_ONE),
                targetVolumePercent = obj.optInt("targetVolumePercent", 65).coerceIn(10, 100)
            )
        }.getOrDefault(LuxeAutoProfile())
    }

    fun saveAutoProfile(profile: LuxeAutoProfile) {
        val obj = JSONObject().apply {
            put("enabled", profile.enabled)
            put("eqPresetId", profile.eqPresetId)
            put("soundMode", profile.soundMode.name)
            put("ledMode", profile.ledMode.name)
            put("targetVolumePercent", profile.targetVolumePercent)
        }
        prefs.edit().putString("luxe_auto_profile", obj.toString()).apply()
    }

    fun exportAllPresetsJson(): String {
        val root = JSONObject()
        root.put("speaker", "boAt Nirvana Luxe")
        root.put("version", 3)
        root.put("soundMode", soundMode.name)
        root.put("ledMode", ledMode.name)
        root.put("roomPlacement", roomPlacement.name)
        root.put("preAmpDb", preAmpDb.toDouble())
        root.put("bassBoostPercent", bassBoostPercent)
        root.put("virtualizerPercent", virtualizerPercent)
        root.put("loudnessPercent", loudnessPercent)
        val gainsArr = JSONArray()
        currentBandGains.forEach { gainsArr.put(it.toDouble()) }
        root.put("active7BandGains", gainsArr)
        root.put("customPresets", JSONArray(prefs.getString("luxe_custom_eq_presets_json", "[]") ?: "[]"))
        return root.toString(2)
    }

    fun importPresetsJson(jsonString: String): Boolean {
        return runCatching {
            val root = JSONObject(jsonString)
            if (root.has("active7BandGains")) {
                val g = root.getJSONArray("active7BandGains")
                if (g.length() >= 7) {
                    currentBandGains = List(7) { idx -> g.optDouble(idx, 0.0).toFloat().coerceIn(-12f, 12f) }
                }
            }
            if (root.has("preAmpDb")) preAmpDb = root.optDouble("preAmpDb", 0.0).toFloat()
            if (root.has("bassBoostPercent")) bassBoostPercent = root.optInt("bassBoostPercent", 30)
            if (root.has("virtualizerPercent")) virtualizerPercent = root.optInt("virtualizerPercent", 25)
            if (root.has("loudnessPercent")) loudnessPercent = root.optInt("loudnessPercent", 15)
            if (root.has("customPresets")) {
                val incoming = root.getJSONArray("customPresets")
                prefs.edit().putString("luxe_custom_eq_presets_json", incoming.toString()).apply()
            }
            true
        }.getOrDefault(false)
    }
}
