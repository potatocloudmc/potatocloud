# potatocloud

A simple "cloud" system for Minecraft servers that is performant, customizable and made to be simple and easy to use

## Supported Platforms (Server Versions)
- Paper (1.20.4 - current)
- Velocity (3.3.0-SNAPSHOT - current)
- Purpur (1.20.4 - current)
- PandaSpigot (1.8.8 - 1.8.9)

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
            <version>1.3.0-BETA</version>
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
    compileOnly("com.github.potatocloudmc.potatocloud:api:v1.3.0-BETA")
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
// default property which can be found in the propety class
Property gameState = Property.GAME_STATE;

// custom property
Property custom = Property.ofString("server_owner", "me");

// setting a property and overwriting the default value (optional)
// properties can be set for groups, services and players
holder.setProperty(custom, "Player123");

// getting the value of a property
Object value = holder.getProperty("server_owner").getValue();
```
## ☁️ Test Server

This cloud system is used by [Surnex.net](https://surnex.net) as a test object.



