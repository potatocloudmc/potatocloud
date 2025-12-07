# potatocloud

A simple "cloud" system for Minecraft servers that is performant, customizable and made to be simple and easy to use

## Features
- **All Core Cloud Features** – static/dynamic servers, templates, and more
- **Simple & Powerful API** - easy to integrate and use.
- **Screen System** - View service output live and execute commands
- **Property System** – add custom data to players, services, groups, or globally
- **No External Database Required** – runs out-of-the-box with minimal setup
- **Clean, Structured Console** - clear logs and output
- **Customizable** – many features can be configured or disabled if not needed
- **Easy to Use** - beginner-friendly and intuitive
- **Powerful Platform System** – auto-update platforms like Paper or add custom platforms

## Supported Platforms (Server Versions)
- Paper (1.20.4 - Latest)
- Velocity (3.3.0-SNAPSHOT - Latest)
- Purpur (1.20.4 - Latest)
- PandaSpigot (1.8.8 - 1.8.9)
- [Limbo](https://github.com/LOOHP/Limbo) (1.21.8, 1.21.10)
- Custom Platforms

## Installation
To install potatocloud, follow these steps:

1. **Install Java 21** if you don’t have it
2. **Download the latest release** from the [potatocloud releases](https://github.com/potatocloudmc/potatocloud/releases)
3. **Extract the ZIP** to any folder you want
4. **Run the cloud using the start script in the folder**:
    - Windows: `start.bat`
    - Linux/macOS: `start.sh`
5. **Create a lobby and a proxy group** with the command `group create` (don’t forget to say yes to the fallback question when creating a lobby)
6. That’s it! You should now be able to join your server

## Optional Plugins

These plugins are separate jars you can drop into your proxy or servers:

| Plugin Name          | Platform(s)    | Description                                                 |
|----------------------|----------------|-------------------------------------------------------------|
| Cloud Command Plugin | Velocity       | Allows you to manage many things ingame via commands        |
| Notify Plugin        | Velocity       | Sends notifications when servers start and stop             |
| Proxy Plugin         | Velocity       | Adds MOTD, Tablist (with LabyMod support), Maintenance Mode |
| Hub Command Plugin   | Velocity       | Command for returning to a fallback server                  |
| LabyMod Plugin       | Bukkit / Paper | Set LabyMod game mode on join to current server             |

All plugins are available in the [potatocloud](https://github.com/potatocloudmc/potatocloud/releases) zip file.

## API
Check out the [potatocloud API Wiki](https://github.com/potatocloudmc/potatocloud/wiki/01%E2%80%90Getting-Started-with-the-API). It shows you how to use the API and includes examples.

## Building from Source
Use Gradle to build the project:
```bash
./gradlew shadowJar
```

## Test Server
Used by [Surnex.net](https://surnex.net)

