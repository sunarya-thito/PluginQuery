![alt text](https://i.ibb.co/FHQnbTg/shaded-header.png "Plugin Query")
![alt text](https://i.ibb.co/WpnXh8R/body.png "Features")

# PluginQuery
Allows spigot/bungeecord/velocity plugins to send plugin message without having to require the player to be online on the server

## Installation
1. Download the jar from [SpigotMC](https://www.spigotmc.org/resources/80064/)
2. Drop the jar inside plugins folder
3. Start your server
4. Join your proxy
5. Synchronize your secret key

## Dependencies Setup
### Maven
[![Release](https://jitpack.io/v/sunarya-thito/PluginQuery.svg)](https://jitpack.io/#sunarya-thito/PluginQuery)
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
```xml
<dependency>
    <groupId>com.github.sunarya-thito</groupId>
    <artifactId>PluginQuery</artifactId>
    <version>VERSION</version>
</dependency>
```

## Synchronization
### In-Game
1. Join your proxy
2. Do /pq sync
### Manually
1. Open PluginQuery data folder inside your proxy plugins folder
2. Copy secret.key
3. Paste it inside PluginQuery data folder inside your server plugins folder
4. Do /spq reload

## Useful Links
* [Wiki](https://sunaryayalasatriathito.gitbook.io/pluginquery/)
* [Java Doc](https://sunarya-thito.github.io/PluginQuery/)
