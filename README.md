# ClusterTracker

A free, open-source Android app for tracking cluster headaches. Built by a cluster headache sufferer, for cluster headache sufferers.

ClusterTracker helps you log attacks in real time, track oxygen therapy, record observations, and identify patterns across your cluster cycles — all from your phone, with your data staying entirely on your device.

## Features

### Live Attack Tracking
- One-tap attack timer with elapsed duration
- **KIP pain scale** (0–10) slider with automatic data logging
- **Oxygen therapy** tracking: adjustable flow rate (6–25 L/min), session timer, and completed session history
- Add therapy notes during an attack (medications, ice packs, energy drinks, etc.)

### Cycle Management
- Organize attacks into cluster cycles with date ranges
- **Cycle logs** for daily observations (vitamin regimen changes, triggers, sleep notes)
- Interleaved timeline of attacks and logs within each cycle
- Start live attacks or add past attacks manually

### Charts and Statistics
- **Pain and O2 chart** for every attack — pain curve overlaid with oxygen sessions
- **Time-of-day distribution** — bar chart showing when your attacks hit
- Per-cycle and overall stats: attack count, average duration, average peak pain, total O2 time

### Environmental Context
- Automatic capture of weather conditions at attack onset (temperature, barometric pressure, humidity)
- **Moon phase and illumination** logged per attack
- Helps identify potential environmental triggers over time

### Data You Own
- **JSON and CSV export** — share with your neurologist or back up your data
- **JSON import** — restore from a backup or move to a new device
- All data stored locally on-device using Room database
- No accounts, no cloud, no tracking

### Edit and Review
- Edit past attacks: adjust times, add or remove therapy notes
- Detailed attack review with full therapy timeline and environmental data
- Delete attacks or entire cycles with cascading cleanup

### Additional
- Dark theme support (Material3)
- Works offline — internet only needed for optional weather data
- Minimum Android 8.0 (API 26)

## Install

### From GitHub Releases (sideload)
1. Go to the [Releases](https://github.com/MyDigitalFreedom/ClusterTracker/releases) page
2. Download the latest `.apk` file
3. On your Android phone, open the downloaded file
4. If prompted, allow installation from unknown sources for your browser
5. Tap **Install**

### Build from Source
```bash
git clone https://github.com/MyDigitalFreedom/ClusterTracker.git
cd ClusterTracker
./gradlew assembleDebug
```
The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM with StateFlow |
| Database | Room (SQLite) |
| DI | Hilt (Dagger) |
| Charts | Vico 2.1.2 |
| Navigation | Compose Navigation (type-safe routes) |
| Weather API | Open-Meteo (free, no key required for geocoding) |
| Moon Phase | commons-suncalc |
| Serialization | kotlinx-serialization |

## About Cluster Headaches

Cluster headaches are one of the most painful conditions known to medicine, sometimes called "suicide headaches." They occur in cyclical patterns (clusters) and cause intense, piercing pain typically around one eye. Attacks last 15 minutes to 3 hours and can happen multiple times a day.

If you or someone you know suffers from cluster headaches, these resources may help:
- [ClusterBusters](https://clusterbusters.org/) — patient advocacy and research
- [OUCH (Organisation for the Understanding of Cluster Headache)](https://ouchuk.org/)

## License

This project is provided as-is for personal and community use. Feel free to fork, modify, and share.

## Contributing

Issues and pull requests are welcome. If you're a fellow cluster headache sufferer with feature ideas, open an issue — this app is built for our community.
