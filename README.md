# boAt Hearables Pro (`v2.0.0-pro`)

**A clean, zero-ad, zero-login, redesigned Android companion & 10-Band Custom Equalizer studio for all 95+ boAt Hearables (Airdopes, Nirvana, Rockerz, Stone & PartyPal).**

Built by reverse-engineering the official `boAt Hearables 1.9.0.apk` (`138.8 MB`) to extract its multi-chipset Bluetooth Classic (RFCOMM SPP) and BLE GATT protocols while stripping out all ads, promotional WebViews, mandatory OTP login walls, analytics trackers, and artificial device locks.

---

## Reverse-Engineering Findings from `boAt Hearables 1.9.0.apk`

| Metric / Aspect | Official `boAt Hearables 1.9.0.apk` | `boAt Hearables Pro v2.0.0` |
|---|---|---|
| **APK Size** | **138.8 MB** (>80 MB uncompressed Lottie JSONs + 3.1 MB JioSaavn ad GIFs + 3D `.obj` files) | **~4 MB** (-97% size reduction) |
| **Custom Equalizer** | Artificially locked in `Constants.j2` (`CUSTOM_EQ_PREF_FLAG`) to ~10 hardcoded models; `NON_SDK` devices locked to 6 static presets with `EQView.setEnableSeekbar(false)` | **Unlocked 10-Band Custom Spline EQ (`31Hz–16kHz`, `-12` to `+12 dB`) + Unlimited User Profiles for ALL 95+ boAt models** |
| **DSP Engine** | Basic 5-band legacy `Equalizer` on `NON_SDK` devices | **Dual-Engine Sync**: Hardware Chipset EQ packets + Android 10-Band `DynamicsProcessing`, Pre-Amp, Q-Factor, Brickwall Limiter, Sub-Bass Punch, 360° Virtualizer & LoudnessEnhancer |
| **Ads & Monetization** | `UniversalAdSlot`, `Monatisation`, `SpotifyBannerAdResponse`, `JioSaavnEmbedWidget` (`boat_script_new.html`), `KukuFmMediaSession`, `CartActivity` store, Instagram promo stories | **0 Ads, 0 WebViews, 0 Banners, 0 Store Upsells** |
| **Onboarding & Privacy** | Mandatory phone OTP (`VerifyMobileActivity`) / email login + CleverTap, Mixpanel & Firebase In-App Messaging | **100% Offline, Zero Login, Zero Trackers** |

---

## Multi-Chipset Bluetooth Protocols Supported

Extracted directly from the official DEX bytecode (`com.boAt.hearables.*`, `com.bluetrum.devicemanager.*`, `com.jieli.bluetooth.*`, `com.airoha.sdk.*`, `org.wuqi.*`):

1. **Bluetrum (`BLUETRUM_SDK`)** — *Nirvana Crown, Nirvana Ion V2, Airdopes Prime 700/702/513 ANC, Airdopes Plus 421/422/Spark ANC, Airdopes 341/393/423 ANC, Rockerz Plus 450/351 ANC, Nirvana Eutopia 2 ANC*
   - **SPP UUIDs**: `B6632277-0642-458B-A7A0-23FB1DC92C93`, `00001101-0000-1000-8000-00805F9B34FB`
   - **BLE Service**: `0000ab00-0000-1000-8000-00805f9b34fb` (Smart Case: `0000fae0-...`)
   - **Opcodes**: `0x20` (`EqRequest` 10-band Custom EQ), `0x22` (`KeyRequest` Touch Remap), `0x25` (`WorkModeRequest` BEAST™ Low-Latency), `0x26` (`InEarDetectRequest`), `0x27` (`DeviceInfoRequest` L/R/Case Battery TLV), `0x2A` (`FindDeviceRequest`), `0x2C`/`0x35` (`AncRequest` TLV), `0x32` (`SpatialAudioModeRequest`), `0x33` (`MultipointRequest`), `0x36` (`BassEngineRequest`), `0xF0` (`AntiWindNoiseRequest`).
2. **Bestechnic (`BES_SDK`)** — *Nirvana Ivy / Ivy Pro, Nirvana Zenith Pro, Nirvana Space 2, Nirvana X TWS, Nirvana Ion ANC 2, Airdopes 800 / 800 HiDef, Airdopes Prime Supreme / 300 / 412 / 413 / 511 / 512 / 701 ANC / 800D / Raga, Rockerz 650 Pro / 210 ANC*
   - **BLE Service**: `66666666-6666-6666-6666-666666666666` (Write/Notify: `77777777-...`) & Excelpoint `65786365-6c70-6f69-6e74-2e636f820000`
   - **Commands**: `0x01 0x13` Custom FreqAmp EQ, `0x1D` Multi-Level ANC (`High / Mid / Low / Ambient / Off`), `0x07` In-Ear Detection, `0x1C` Multipoint, `0x1E` Gaming Mode, `0x20` Spatial/Dolby Mode.
3. **Wuqi (`WUQI_SDK`)** — *Nirvana Ion, Nirvana Ion ANC, Nirvana Ion ANC Pro, Nirvana Iris, Nirvana Crystl, Rockerz 255 ANC, Rockerz Trinity Gen 2 / Grande*
   - **SPP UUID**: `00007034-0000-1000-8000-00805F9B34FB` | **BLE Service**: `0000fff0-...` (`ffa1` / `ffa2`)
   - **Framing (`08 EE ...`)**:
     - Custom EQ: `08 EE 00 00 00 03 81 14 00 [mode] [10 bands]`
     - ANC On: `08 EE 00 00 00 02 06 0B 00 01 0A` | Normal: `08 EE 00 00 00 02 06 0B 00 00 09` | Ambient: `08 EE 00 00 00 02 06 0B 00 02 0B`
     - BEAST™ Mode: `08 EE 00 00 00 02 07 0B 00 01 0B`
     - Touch Key Remap: `08 EE 00 00 00 02 01 0D 00 [side] [gesture] [action]`
4. **JieLi (`JL_SDK`)** — *Rockerz 551 ANC Pro, Stone Arc Pro / Plus, Stone 350/352/358 Pro*
   - **BLE Service**: `0000ae00-...` (`ae01` / `ae02`) | **SPP**: `0000fe01-...`
   - **RCSP Frames**: `FE DC BA 00 07 ... EF` (10-Band Dynamic EQ), `0x08` ANC/Transparency, `0x09` Key Remap.
5. **Airoha (`AIROHA_SDK`)** — *Nirvana Space, Nirvana Lucid, Nirvana Eutopia, Airdopes Flex 454 ANC*
   - **BLE Service**: `5052494D-2DAB-0341-6972-6F6861424C45` | **SPP**: `00000000-0000-0000-0099-AABBCCDDEEFF`
   - **RACE Frames**: `05 5A ...` for 10-band PEQ, Hybrid ANC, Sidetone, and Find My Buds.
6. **CEVA / Zipline / Qualcomm GAIA / Universal System DSP (`UNIVERSAL_DSP`)**
   - Full 10-band Android `DynamicsProcessing` + `Equalizer` + `BassBoost` + `Virtualizer` + `LoudnessEnhancer` for any boAt model (`Airdopes 131/141/161/281 Pro/441/Alpha`, `Rockerz 255 Pro+/330/450`) or any Bluetooth headphones.

---

## Key Features & Customizations

- **Cockpit Dashboard**: Triple radial battery rings (**Left Bud %**, **Right Bud %**, **Smart Case %**), live Bluetooth RSSI & transport badge, 4-mode Noise Control Deck (**ANC Active**, **Adaptive ANC**, **Ambient Mode**, **Standard Off**) with **ANC Depth** (`-50dB High`, `-32dB Medium`, `-20dB Low`) and **Anti-Wind Noise**, plus quick toggles for **BEAST™ Mode (<45ms)**, **360° Spatial Audio**, **Dual Multipoint**, **In-Ear Auto Pause**, and **Smart Talk**.
- **Studio 10-Band Custom Equalizer**: Interactive touch-draggable **Cubic Spline Frequency Response Graph** (`31Hz`, `63Hz`, `125Hz`, `250Hz`, `500Hz`, `1k`, `2k`, `4k`, `8k`, `16k`), 14 unlocked studio & hardware curves, unlimited user-saved custom profiles, JSON import/export, **Pre-Amp Gain**, **Parametric Q-Factor**, **Brickwall Anti-Clip Limiter**, **Dynamic Sub-Bass Punch + Crossover**, **360° Soundstage Widener**, **Room Acoustics**, **Loudness Enhancer**, and **L/R Driver Balance**.
- **Touch Remapper & Bed Mode**: Reassign Single, Double, Triple, and Long Press independently for Left and Right earbuds, or toggle **Bed Mode** to disable accidental pillow taps.
- **Per-Device Auto-Connect Automation**: Automatically apply your preferred EQ preset, ANC mode, BEAST™ Mode, and startup volume whenever your boAt device connects.
- **Acoustic Lab & Find My Buds**: Independent **Ring Left**, **Ring Right**, or **Ring Both** high-frequency chirp locator + Bluetooth RSSI proximity radar, **4-Second Ear-Tip Acoustic Seal Test** (`55Hz` vs `500Hz`), **20 Hz – 20 kHz Sine Sweep & Polarity Phase Generator**, and a **Live Hex Protocol Console**.

---

## Cloud Auto-Build (GitHub Actions)

Just like `NEWS-dash`, every push to `main` (or manual trigger in **Actions → Build APK → Run workflow**) compiles a signed release APK on GitHub's cloud runner and uploads `boAt-Hearables-Pro-apk` under the workflow run's **Artifacts**.
