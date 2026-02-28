# PadConnect

> **Low latency virtual gamepad** — built with **Kotlin** and **Jetpack Compose**

PadConnect lets your phone act as a **real controller (Xbox 360/DualShock4)** for a PC. It streams gamepad input with very low latency and exposes it on Windows using **ViGEm**.

This project is split into two parts:

* **PadConnect** → Android / client app (virtual controller UI)
* **[PadConnectReceiver](https://github.com/Ishan09811/PadConnectReceiver)** → Windows / receiver app (creates virtual controller)

---

## Features

* **Xbox 360(working)/DualShock4(soon) compatible** virtual controller
* **Low latency input streaming** (UDP based)
* Built with **Kotlin**
* UI powered by **Jetpack Compose**
* Windows receiver using **ViGEm**
* Supports buttons, triggers, sticks, and D-Pad
* Works over **local Wi-Fi** (no internet required)

---

## How it works

```
[ Android Phone ] ── UDP ──▶ [ PadConnectReceiver (Windows) ] ──▶ [ ViGEm ] ──▶ Game
```

1. **PadConnect (Android)**

    * Renders a virtual controller using Compose
    * Captures button / axis input
    * Serializes input events
    * Sends them over UDP

2. **[PadConnectReceiver](https://github.com/Ishan09811/PadConnectReceiver) (Windows)**

    * Listens for incoming UDP packets
    * Deserializes controller events
    * Feeds them into **ViGEm**
    * Exposes a virtual **Xbox 360(working)/DualShock4(soon) controller** to the OS

Games see it as a *real* controller.

---

## Tech Stack
### PadConnect (Client)

* Kotlin + Jetpack Compose
* UDP networking
* Touch → Gamepad mapping

### [PadConnectReceiver](https://github.com/Ishan09811/PadConnectReceiver) (Server)

* Kotlin Multiplatform (KMP)/JVM
* ViGEm (Virtual Gamepad Emulation Framework)
* Xbox 360(working)/DualShock4(soon) device emulation

---

## Requirements

### Android

* Android 8.0+
* Wi-Fi connection

### Windows

* Windows 10 / 11
* **ViGEmBus Driver** installed
* .NET / JVM compatible environment (depending on receiver build)

---

## Getting Started

### 1. Install ViGEm

Download and install **ViGEmBus**:

* Official ViGEm GitHub release

Reboot after installation.

---

### 2. Run PadConnectReceiver (Windows)

```bash
PadConnectReceiver.exe
```

* Starts listening on UDP
* Creates a virtual controller

---

### 3. Run PadConnect (Android)

* Install the app on your phone
* Create a layout
* Start playing

---

## Supported Inputs

* Buttons: A / B / X / Y
* Shoulder buttons
* Triggers (analog)
* D-Pad

---

## Project Structure

```
PadConnect/
 ├─ app/

PadConnectReceiver/
 ├─ data/          
 ├─ input/
 ├─ main/ui/
 ├─ native/
 ├─ utils/
 ├─ viewmodel/
```

---

## Roadmap

* [ ] DualShock4 input executor 
* [ ] Multiple controller support
* [ ] Custom controller layouts
* [ ] Latency & packet loss stats
* [ ] BLE HID (does not require receiver)
* [ ] Linux receiver

---

## Notes

* Works best on **local Wi-Fi** (TODO: Implement BLE HID)
* Firewall may need UDP port allowance
* ViGEm is Windows only

---

## Credits

* **ViGEm** — Virtual Gamepad Emulation Framework
* Kotlin & Jetpack Compose teams

---

## License

[GPL3](https://github.com/Ishan09811/PadConnect/blob/master/LICENSE) License

---

> Built for low latency, simplicity, and fun.
