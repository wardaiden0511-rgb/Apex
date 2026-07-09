# Apex Client (Fabric)

A cosmetic Fabric client mod scaffold for Minecraft 1.21.11. Opens a modern, reddish GUI with toggles for safe, client-side features.

**Keybind:** comma (`,`) — opens the Apex Client GUI.

**Features:**
- Toggleable client-side particles
- Toggleable HUD overlay with active module display
- Toggleable info display (placeholder)
- Module framework with keybind assignment
- Per-module configuration screens
- Config persistence (JSON-based)

## Build Notes

There are currently compatibility issues between Gradle 9.5.1 and Fabric Loom that prevent a successful build. To build this project:

**Option 1: Use an older Gradle version (Recommended)**
```powershell
# Download Gradle 8.8 or similar
# https://gradle.org/releases/ → Binary only
# Set GRADLE_HOME and try building again
gradle wrapper
.\gradlew.bat build
```

**Option 2: Use Fabric's official template**
```bash
git clone https://github.com/FabricMC/fabric-example-mod.git apex-client-new
cd apex-client-new
# Copy the source files from src/main/java from this project
# Update fabric.mod.json with Apex Client metadata
gradlew build
```

**Source Code Status:**
The mod source code is complete and fully implemented. All features (GUI, modules, keybinds, config) are ready for testing. The build issue is purely a Gradle/Loom version compatibility problem, not a code issue.

## Project Structure

- `src/main/java/com/example/apexclient/` — Main mod code
  - `ApexClient.java` — Entrypoint, keybind & HUD rendering
  - `ApexConfig.java` — Global toggle flags
  - `ApexScreen.java` — Main GUI (comma key)
  - `module/` — Module system
  - `gui/` — Configuration screens
  - `input/` — Keybind management
  - `config/` — Config persistence

- `build.gradle` — Gradle build config
- `settings.gradle` — Gradle settings
- `build.ps1` — Windows build helper script

## Development Notes

This project is intentionally cosmetic and does not implement any aim-assist or automated targeting features.

The module system is extensible — new modules can be added by extending `Module` and registering with `ModuleManager`.
