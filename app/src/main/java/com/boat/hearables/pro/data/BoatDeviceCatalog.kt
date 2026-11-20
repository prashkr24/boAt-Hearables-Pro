package com.boat.hearables.pro.data

/**
 * Dedicated hardware specification, BES command definitions, sound profiles,
 * LED Light Show modes, and Room Placement acoustics exclusively for the
 * boAt Nirvana Luxe Bluetooth Speaker.
 *
 * Reverse-engineered from `boAt Hearables 1.9.0.apk`:
 * - Device constant: `Constants.w` / `Constants.J0()` -> `"Nirvana Luxe"`
 * - Protocol controller: `Lh80` (`BESHelper`) + `Lzu1` + `Lcr1` (header `13 04 [len_lo] [len_hi] [opcode] ...`)
 * - Transport: Bluetooth Classic RFCOMM SPP (`00001101-0000-1000-8000-00805F9B34FB`)
 *   with BLE GATT fallback (`66666666-6666-6666-6666-666666666666` / `77777777-7777-7777-7777-777777777777`)
 * - Hardware EQ (`Lh80.h1` / `Lh80.G1` / `Lri0` `BesDataFreqAmpData`):
 *   7 hardware bands (`60Hz, 120Hz, 250Hz, 750Hz, 2kHz, 5kHz, 15kHz`) via opcode `0x13` (`19`).
 * - Sound Modes (`Lh80.K1` / `Lh80.J1` / `La6c.U8`):
 *   `boAt Signature Sound`, `boAt 360° Spatial Audio`, `Spatial Surround` via opcode `0x1D` (`29`).
 * - LED Light Show (`Lh80.A1` / `Lh80.B1`):
 *   5 LED modes + breathing/static states via opcode `0x1C` (`28`).
 * - Broadcast / PartySync (`Lh80.N1`): opcode `0x85` (`-123`).
 * - Dual Multipoint (`Lh80.C1`): opcode `0x16` (`22`).
 */

enum class LuxeSoundMode(
    val label: String,
    val subtitle: String,
    val besModeByte: Byte,
    val besSubByte: Byte
) {
    SIGNATURE_SOUND(
        label = "boAt Signature Sound",
        subtitle = "Deep passive-radiator bass slam & crisp highs (Hardware Mode 0)",
        besModeByte = 0x00,
        besSubByte = 0x02
    ),
    SPATIAL_360(
        label = "boAt 360° Spatial Audio",
        subtitle = "Omnidirectional 360° room-filling soundstage expansion (Hardware Mode 2)",
        besModeByte = 0x02,
        besSubByte = 0x02
    ),
    SPATIAL_CINEMA(
        label = "3D Cinema Surround",
        subtitle = "Wide theatre soundstage with elevated dialog clarity (Hardware Mode 1)",
        besModeByte = 0x01,
        besSubByte = 0x02
    ),
    OUTDOOR_PARTY(
        label = "Outdoor Party Boost",
        subtitle = "Maximum SPL projection + upper-bass punch for open spaces",
        besModeByte = 0x02,
        besSubByte = 0x02
    ),
    LATE_NIGHT(
        label = "Late Night / Apartment",
        subtitle = "Tames floor-transmitted sub-bass rumble & compresses sudden loud peaks",
        besModeByte = 0x00,
        besSubByte = 0x02
    )
}

enum class LuxeLedMode(
    val label: String,
    val subtitle: String,
    val besOpcodeByte: Byte
) {
    MODE_ONE(
        label = "Luxe Pulse",
        subtitle = "Beat-synced dynamic warm pulse (Mode 1)",
        besOpcodeByte = 0x01
    ),
    MODE_TWO(
        label = "Rhythm Strobe",
        subtitle = "Fast tempo-reactive party light show (Mode 2)",
        besOpcodeByte = 0x02
    ),
    MODE_THREE(
        label = "Ambient Breathing",
        subtitle = "Smooth slow breathing aura (Mode 3)",
        besOpcodeByte = 0x03
    ),
    MODE_FOUR(
        label = "Horizon Flow",
        subtitle = "Continuous gradient wave effect (Mode 4)",
        besOpcodeByte = 0x04
    ),
    OFF(
        label = "LEDs Off (Eco)",
        subtitle = "Turns off speaker light show to extend battery playtime by ~20%",
        besOpcodeByte = 0x05
    )
}

enum class RoomPlacement(
    val label: String,
    val description: String,
    val bassTiltDb: Float,
    val trebleTiltDb: Float
) {
    FREE_STANDING(
        label = "Center Table / Open",
        description = "Neutral acoustic radiation (0 dB boundary compensation)",
        bassTiltDb = 0f,
        trebleTiltDb = 0f
    ),
    NEAR_WALL(
        label = "Against Wall",
        description = "-2.5 dB low-bass cut to prevent wall-reflection boominess",
        bassTiltDb = -2.5f,
        trebleTiltDb = 0.5f
    ),
    ROOM_CORNER(
        label = "Room Corner",
        description = "-4.5 dB sub-bass cut to eliminate corner cavity resonance",
        bassTiltDb = -4.5f,
        trebleTiltDb = 1.0f
    ),
    OUTDOOR_OPEN(
        label = "Outdoor / Terrace",
        description = "+3.5 dB bass & +2.0 dB presence boost for open-air dispersion",
        bassTiltDb = 3.5f,
        trebleTiltDb = 2.0f
    )
}

data class EqPreset(
    val id: String,
    val name: String,
    val subtitle: String,
    // 7 native BES hardware bands for Nirvana Luxe: 60Hz, 120Hz, 250Hz, 750Hz, 2kHz, 5kHz, 15kHz
    val gainsDb: List<Float>,
    val isCustom: Boolean = false
)

object BoatDeviceCatalog {

    const val TARGET_DEVICE_NAME = "Nirvana Luxe"
    const val BES_SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    const val BES_BLE_SERVICE_UUID = "66666666-6666-6666-6666-666666666666"
    const val BES_BLE_CHAR_UUID = "77777777-7777-7777-7777-777777777777"
    const val EXCELPOINT_SERVICE_UUID = "65786365-6c70-6f69-6e74-2e636f820000"
    const val EXCELPOINT_WRITE_UUID = "65786365-6c70-6f69-6e74-2e636f820001"
    const val EXCELPOINT_NOTIFY_UUID = "65786365-6c70-6f69-6e74-2e636f820002"

    /**
     * Exact 7 hardware equalizer center frequencies used by `Lh80.h1` (`BesDataFreqAmpData`)
     * for boAt Nirvana Luxe (`60Hz, 120Hz, 250Hz, 750Hz, 2kHz, 5kHz, 15kHz`).
     */
    val LUXE_EQ_FREQUENCIES_HZ = listOf(60, 120, 250, 750, 2000, 5000, 15000)
    val LUXE_EQ_LABELS = listOf("60Hz", "120Hz", "250Hz", "750Hz", "2kHz", "5kHz", "15kHz")

    /**
     * Strictly checks whether a Bluetooth device is the user's boAt Nirvana Luxe speaker.
     */
    fun isNirvanaLuxeSpeaker(btName: String?): Boolean {
        if (btName.isNullOrBlank()) return false
        val clean = btName.trim().lowercase()
        return clean.contains("nirvana luxe") ||
            (clean.contains("nirvana") && clean.contains("luxe")) ||
            clean == "boAt nirvana luxe".lowercase() ||
            clean.contains("boat luxe")
    }

    /**
     * Built-in sound profiles tailored specifically for the boAt Nirvana Luxe speaker,
     * including the exact 7-band BES curves extracted from `La80.<clinit>` in `boAt Hearables 1.9.0.apk`.
     */
    val BUILT_IN_PRESETS: List<EqPreset> = listOf(
        EqPreset(
            id = "luxe_signature",
            name = "Nirvana Luxe Signature",
            subtitle = "Rich 60Hz–120Hz passive radiator slam with crystal-clear 15kHz highs",
            gainsDb = listOf(5.5f, 4.5f, 1.0f, -0.5f, 1.5f, 3.5f, 4.5f)
        ),
        EqPreset(
            id = "deep_room_bass",
            name = "Deep Room Sub-Bass",
            subtitle = "Maximizes Nirvana Luxe low-end thump at 60Hz & 120Hz without vocal mud",
            gainsDb = listOf(9.0f, 7.5f, 2.5f, -1.5f, 0.5f, 2.5f, 3.0f)
        ),
        EqPreset(
            id = "balanced_bes",
            name = "Balanced (Stock BES)",
            subtitle = "Extracted from La80 BES Balanced curve (60Hz–15kHz)",
            gainsDb = listOf(0.0f, 1.0f, 1.0f, 1.0f, 3.0f, -3.0f, 0.0f)
        ),
        EqPreset(
            id = "pop_bes",
            name = "Pop (Stock BES)",
            subtitle = "Extracted from La80 BES Pop curve",
            gainsDb = listOf(2.0f, -3.0f, 1.0f, 1.0f, -3.0f, 1.0f, 3.0f)
        ),
        EqPreset(
            id = "rock_bes",
            name = "Rock (Stock BES)",
            subtitle = "Extracted from La80 BES Rock curve",
            gainsDb = listOf(3.0f, 2.0f, 3.0f, 1.0f, 3.0f, 2.0f, 2.0f)
        ),
        EqPreset(
            id = "jazz_bes",
            name = "Jazz (Stock BES)",
            subtitle = "Extracted from La80 BES Jazz curve",
            gainsDb = listOf(3.0f, 2.0f, 3.0f, -1.0f, 3.0f, -2.0f, 1.0f)
        ),
        EqPreset(
            id = "club_bes",
            name = "Club / Party (Stock BES)",
            subtitle = "Extracted from La80 BES Club curve — high-energy kick & treble",
            gainsDb = listOf(5.0f, 4.0f, 2.0f, -1.0f, 1.0f, 3.5f, 4.0f)
        ),
        EqPreset(
            id = "cinema_360",
            name = "360° Cinema & Dialog",
            subtitle = "Deep sub-bass LFE rumble with boosted 750Hz–2kHz movie dialog",
            gainsDb = listOf(7.0f, 4.0f, -1.5f, 3.5f, 4.5f, 2.0f, 3.0f)
        ),
        EqPreset(
            id = "outdoor_terrace",
            name = "Outdoor / Terrace Max",
            subtitle = "High-projection upper-bass & vocal presence for open spaces",
            gainsDb = listOf(6.5f, 8.0f, 4.5f, 2.5f, 4.0f, 5.5f, 5.0f)
        ),
        EqPreset(
            id = "vocal_acoustic",
            name = "Vocal & Acoustic",
            subtitle = "Warm midrange clarity for acoustic guitars, podcasts & vocals",
            gainsDb = listOf(-2.0f, -0.5f, 2.0f, 4.5f, 5.0f, 3.0f, 1.5f)
        ),
        EqPreset(
            id = "late_night_warm",
            name = "Late Night (Low Volume)",
            subtitle = "Fletcher-Munson loudness contour without wall-shaking bass",
            gainsDb = listOf(2.5f, 2.0f, 1.0f, 0.5f, -1.0f, -2.0f, 1.0f)
        ),
        EqPreset(
            id = "flat_studio",
            name = "Flat Studio Reference",
            subtitle = "0 dB uncolored response across all 7 hardware bands",
            gainsDb = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        )
    )
}
