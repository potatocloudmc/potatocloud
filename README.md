# potatocloud

A cloudsystem for Minecraft servers that is performant, customizable and made to be simple and easy to use

## Features

- **Core cloud functionality** - static/dynamic servers, templates, and more  
- **Simple API** - easy to integrate and use, with all the features you need
- **Screen System** - view service output live and execute commands  
- **Property System** - add custom data for players, services, groups, or globally  
- **Beginner friendly** - works out of the box with zero setup
- **Clean, Structured Console** - clear logs and output  
- **Customizable** - customize, enable or disable features as needed  
- **Powerful Platform System** - auto-update platforms and add custom platforms

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

## Supported Platforms (Server Versions)
- Paper (1.21.4 - Latest)
- Velocity (3.3.0-SNAPSHOT - Latest)
- Purpur (1.21.4 - Latest)
- LeafMC (1.21.4 - Latest)
- PandaSpigot (1.8.8 - 1.8.9)
- [Limbo](https://github.com/LOOHP/Limbo) (1.21.8, 1.21.10)
- Custom Platforms
  
## Optional Plugins

These plugins are separate jars you can drop into your proxy or servers:

- **Cloud Command** (Velocity) - Adds a `/cloud` command that allows you to manage many things ingame
- **Notify** (Velocity) - Sends notifications when servers start and stop
- **Proxy** (Velocity) - Adds MOTD, Tablist (with LabyMod support), Maintenance Mode
- **Hub Command** (Velocity) - Command for returning to a fallback server 
- **LabyMod** (Bukkit / Paper) - Set LabyMod game mode on join to current server

> All plugins are available in the [potatocloud](https://github.com/potatocloudmc/potatocloud/releases) zip file.

## API
Check out the [potatocloud API Wiki](https://github.com/potatocloudmc/potatocloud/wiki/01%E2%80%90Getting-Started-with-the-API). Alternatively, you can take a look at [DeepWiki](https://deepwiki.com/potatocloudmc/potatocloud)

## Building from Source
Use Gradle to build the project:
```bash
./gradlew shadowJar
```

## Test Server
Used by [Surnex.net](https://surnex.net)

