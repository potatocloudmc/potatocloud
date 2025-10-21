# potatocloud

A simple "cloud" system for Minecraft servers that is performant, customizable and made to be simple and easy to use

## Supported Platforms (Server Versions)
- Paper (1.20.4 - current)
- Velocity (3.3.0-SNAPSHOT - current)
- Purpur (1.20.4 - current)
- PandaSpigot (1.8.8 - 1.8.9)
- [Limbo](https://github.com/LOOHP/Limbo) (1.21.8, 1.21.10)
- Custom Platforms 

## Optional Plugins

| Plugin Name          | Platform(s)    | Description                                                |
|----------------------|----------------|------------------------------------------------------------|
| Cloud Command Plugin | Velocity       | Allows you to manage many things ingame via commands       |
| Notify Plugin        | Velocity       | Sends notifications when servers start and stop            |
| Proxy Plugin         | Velocity       | Adds MOTD, Tablist(With Labymod support), Maintenance Mode |
| Hub Command Plugin   | Velocity       | Command for returning to an fallback server                |
| LabyMod Plugin       | Bukkit / Paper | Set LabyMod game mode on join to current server            |

## API Dependency

### Maven

```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>com.github.potatocloudmc.potatocloud</groupId>
            <artifactId>api</artifactId>
            <version>1.3.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
```

### Gradle

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.potatocloudmc.potatocloud:api:1.3.0")
}
```

### You can access the API by using:

```java
CloudAPI api = CloudAPI.getInstance();

ServiceGroupManager groupManager = api.getServiceGroupManager();

ServiceManager serviceManager = api.getServiceManager();

CloudPlayerManager playerManager = api.getCloudPlayerManager();
```

## Property System Example

```java
// Example default property from DefaultProperties (more properties can be found in the class)
Property<String> gameState = DefaultProperties.GAME_STATE;

// Custom property
Property<String> custom = Property.ofString("server_owner", "me");

// Setting a property and optionally overwriting the default value
// Properties can be set for groups, services, or players
holder.setProperty(custom, "Player123");

// Getting the value of a property (use name or object)
Property<String> property = holder.getProperty("server_owner");
String serverOwner = property.getValue();
```
## ☁️ Test Server

This cloud system is used by [Surnex.net](https://surnex.net) as a test object.



