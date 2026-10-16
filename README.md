# 📡 Cyber Operations Command Center

A real-time, hybrid cybersecurity hacking simulation and tactical network defense game built side-by-side using **Java (Swing)** and **Python 3.14**.

This project bridges software engineering and security operations by separating a multi-threaded Python simulation core from a high-performance Java tactical HUD interface, communicating via local asynchronous TCP network sockets.

---

## 🎮 Gameplay Features

*   **🔴 Dynamic Background Attack Generator:** An active background process mimics malicious threat groups launching unpredictable DDoS attacks against network nodes.
*   **📊 Live Network Telemetry Matrix:** A responsive Java graphical HUD that tracks server health, visual port workload capacities (fading from Green to Amber to flashing Red), and overall Mainframe Integrity.
*   **🐍 Integrated Player Scripting Engine:** A functional sandboxed API layout allowing players to write custom `.py` automation scripts, load them dynamically during runtime, and build algorithmic firewalls.
*   **📜 Tactical Campaign System:** Real-time event parsing that pushes script notifications and narrative directives directly from an NPC Command Handler into the player console log.

---

## 🛠️ Architecture & Core Components

```text
cyber-defense-game/
├── python_backend/
│   ├── simulation.py        # Core game engine loop, socket server & campaign generator
│   └── scripts/             # Player automation sandbox storage folder
│       └── smart_defense.py # Custom programmatic countermeasure automation script
│
└── java_frontend/
    └── src/
        └── GameApp.java     # Graphical HUD, Regex JSON parser, and async TCP client
```

*   **Java UI Client:** Implements non-blocking IO streams, regex scanning arrays to parse raw composite matrices, and focus-locking interface panels.
*   **Python Simulation Core:** Governs network topology physics, handles background multi-threading intervals, and processes programmatic code injections using dynamic module loading utilities (`importlib`).

---

## ⚡ Core Operational Commands

Type these directly into the green command terminal panel inside the game:
*   `scan` — Executes an infrastructure audit sweep revealing host operating systems and system vulnerabilities.
*   `mitigate [IP]` — Manually drops a high-priority packet firewall rule signature over a specific target box.
*   `run [script_name.py]` — Spawns the sandbox runtime to interpret automated defense scripts from the `scripts/` directory.

---

## 🚀 Installation & Running Guide

### 1. Prerequisites
Ensure you have the following installed on your machine:
*   Java Development Kit (JDK 17 or higher)
*   Python (Version 3.10+)

### 2. Booting the Python Backend
Open your terminal environment, navigate to the `python_backend` folder, and execute the core module:
```bash
python simulation.py
```
*Expected Terminal Output:* `[*] Tactical Defense Engine server active on port 9999...`

### 3. Launching the Java UI Front-End
Keep the Python window open. Open a secondary terminal, navigate to your Java source folder, compile and run `GameApp.java`:
```bash
javac GameApp.java
java GameApp
```
The **Operations HUD** frame will pop open on your screen and instantly connect across local network loops to stream real-time visual metrics!
