# Contributing to PadConnect

Thanks for your interest in contributing to PadConnect the Android client that turns your phone into a low latency PC game controller. This document covers how to get set up, the project's conventions, and how to submit changes.

## Project Overview

PadConnect is the **Android / client** half of the PadConnect project. It renders a virtual controller UI, captures touch input, and streams it over UDP to [PadConnectReceiver](https://github.com/Ishan09811/PadConnectReceiver) running on Windows/Linux, which exposes it to games.

```
[ Android Phone ] -- UDP --> [ PadConnectReceiver ] --> [ ViGEm ] --> Game
```

If your change involves the receiver side (desktop app, ViGEm integration, virtual controller emulation), please open your PR against [PadConnectReceiver](https://github.com/Ishan09811/PadConnectReceiver) instead.

## Tech Stack

- Kotlin
- Jetpack Compose (UI)
- UDP networking (input streaming)

## Getting Started

### Prerequisites

- Android Studio (recent stable version)
- JDK 17
- Android SDK with API level matching `compileSdk` in `app/build.gradle.kts`
- A physical Android device or emulator running **Android 10+**

### Setup

1. Fork the repository:
   ```
   git clone https://github.com/Ishan09811/PadConnect.git
   cd PadConnect
   ```
2. Open the project in Android Studio and let Gradle sync.
3. Build and run:
   ```
   ./gradlew assembleDebug or ./gradlew assembleRelease
   ```
   or use the Run configuration in Android Studio.

### Testing against a receiver

To test end to end input streaming, you will need a running instance of [PadConnectReceiver](https://github.com/Ishan09811/PadConnectReceiver) on the same local network. Check the repo's README for setup and current version compatibility, PadConnect and PadConnectReceiver versions are paired (newer receiver builds may require a minimum PadConnect version), so check the receiver's release notes if you hit connection issues.

## Project Structure

```
PadConnect/
 ├─ app/                 # main Android application
 ├─ baselineprofile/     # baseline profile for optimizations
 ├─ fastlane/            # fatlane metadata for app stores eg. F-Droid
 ├─ gradle/              # gradle files
```

## How to Contribute

### Reporting Bugs

Open an issue and include:
- Android version and device model
- PadConnect version
- PadConnectReceiver version (if relevant to the bug)
- Steps to reproduce
- Logs, if available (`adb logcat` output is helpful) otherwise you can get a log from `Android/data/io.github.padconnect/files/`

### Suggesting Features

Open an issue describing the use case before submitting a large PR so this avoids wasted effort if the feature doesn't fit the project's direction. Check the README's Roadmap section first to see if it's already planned.

### Submitting Code Changes

1. Create a branch off `master`:
   ```
   git checkout -b feature/short-description
   ```
2. Make your changes, following the code style below.
3. Test your changes on a real device where possible, touch input and controller mapping behavior can differ from emulator behavior.
4. Commit with a clear, descriptive message (see below).
5. Push to your fork and open a pull request against `master`.
6. Fill in the PR description: what the change does, why, and how you tested it.

### Commit Messages

Keep commits focused and messages descriptive, e.g.:
```
input: fix analog trigger deadzone calculation
ui: adjust D-Pad hitbox for landscape layout
```

### Code Style

- Follow standard Kotlin conventions (as enforced by Android Studio's default formatter / `ktlint` if configured).
- Prefer Compose idioms (state hoisting, unidirectional data flow) over imperative View manipulation.
- Keep UDP/networking logic separate from UI code where possible, the project structure already separates these concerns so keep new code following that pattern.

### Pull Request Checklist

- [ ] Code builds without errors or harmful warnings (`./gradlew assembleDebug` or `./gradlew assembleRelease`)
- [ ] Tested on a physical device or emulator running Android 10+
- [ ] No unrelated changes bundled into the PR
- [ ] PR description explains the change and testing performed

## License

By contributing, you agree that your contributions will be licensed under the project's [GPL-3.0-only license](LICENSE).

## Questions?

Join the [Discord server](https://discord.gg/BrMAZbEyXs) or open a GitHub issue.
