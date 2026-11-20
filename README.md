# boAt Nirvana Luxe Speaker Studio (`v2.1.0-luxe`)

**A dedicated, zero-ad, zero-login Android companion and 7-Band BES Hardware Custom Equalizer studio built exclusively for the boAt Nirvana Luxe Bluetooth Speaker.**

Reverse-engineered directly from `boAt Hearables 1.9.0.apk` (`Constants.w` / `Constants.J0()` -> `"Nirvana Luxe"`, `Lh80` `BESHelper`, `Lzu1`, `Lcr1`, `La6c.U8`/`V8`/`W8`/`X8`, and `La80` `BesDataFreqAmpData`).

---

## Exclusive Nirvana Luxe Features

1. **Strict Nirvana Luxe Bluetooth Detection & Auto-Connect**
   - Strictly filters paired and nearby Bluetooth devices for **`Nirvana Luxe`** (`BoatDeviceCatalog.isNirvanaLuxeSpeaker`), ignoring all other Bluetooth devices (watches, earbuds, cars, TVs).
   - Connects directly via Classic Bluetooth RFCOMM SPP (`00001101-0000-1000-8000-00805F9B34FB`) and BES BLE GATT (`66666666-6666-6666-6666-666666666666` / `77777777-7777-7777-7777-777777777777`).

2. **Unlocked 7-Band BES Hardware Custom Equalizer (`Opcode 0x13`)**
   - In the stock `v1.9.0` APK, `Constants.j2` hid the Custom EQ UI for `Nirvana Luxe` even though `Lh80.h1` (`BesDataFreqAmpData`) supports writing custom signed dB gains across its 7 native hardware DSP bands:
     **`60 Hz`, `120 Hz`, `250 Hz`, `750 Hz`, `2 kHz`, `5 kHz`, `15 kHz`**
   - Sends the authentic BES `0x0413` frame (`13 04 09 00 13 04 [g60] [g120] [g250] [g750] [g2k] [g5k] [g15k]`) + `Lh80.F` EQ switch (`13 04 03 00 13 02 01`), synchronized in real time with Android's 7-band `DynamicsProcessing` engine.
   - Includes **12 Nirvana Luxe Sound Profiles** (including the exact stock BES curves from `La80.<clinit>`) and **unlimited user-saved Custom EQ Profiles** with JSON export/import.

3. **Nirvana Luxe Hardware Sound Modes (`Opcode 0x1D`)**
   - **boAt Signature Sound** (`13 04 03 00 1D 00 02`)
   - **boAt 360° Spatial Audio** (`13 04 03 00 1D 02 02`)
   - **3D Cinema Surround** (`13 04 03 00 1D 01 02`)
   - **Outdoor Party Boost** & **Late Night / Apartment Mode**

4. **Nirvana Luxe LED Light Show Controller (`Opcode 0x1C`)**
   - **Luxe Pulse (Mode 1)** (`13 04 03 00 1C 02 01`)
   - **Rhythm Strobe (Mode 2)** (`13 04 03 00 1C 02 02`)
   - **Ambient Breathing (Mode 3)** (`13 04 03 00 1C 02 03`)
   - **Horizon Flow (Mode 4)** (`13 04 03 00 1C 02 04`)
   - **LEDs Off / Eco (+20% Playtime)** (`13 04 03 00 1C 02 05`)

5. **Extra Speaker Customizations**
   - **Room Placement Acoustic Calibration**: `Center Table / Open`, `Against Wall (-2.5 dB Bass)`, `Room Corner (-4.5 dB Anti-Boom)`, `Outdoor / Terrace (+3.5 dB Projection)`.
   - **Passive Radiator Sub-Bass Punch (`0–100%`)**, **360° Soundstage Widener**, **Party Loudness Boost**, **Pre-Amp Gain**, and **Woofer Excursion Limiter**.
   - **Broadcast / PartySync Multi-Speaker Link (`Opcode 0x85`)** & **Dual Multipoint (`Opcode 0x16`)**.
   - **Auto-Apply on Nirvana Luxe Connect**, **Speaker Sleep Timer (`15–90 min`)**, and **Woofer / Passive Radiator Frequency Test (`35 Hz – 15 kHz`)**.
