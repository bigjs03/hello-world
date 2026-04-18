# Immersed With Shading

Fabric 1.21.x client mod:

- Small held items (first-person scale, configurable)
- Singleplayer/LAN-only bridge trainer (hotkey `B`)
- Cloud-synced config (fetch from any HTTPS URL, e.g. a GitHub Gist raw link)
- Iris/Sodium friendly (no rendering hacks beyond a first-person matrix scale)

## Build the JAR

Requires JDK 21.

```
./gradlew build
```

Output: `build/libs/immersed-with-shading-0.1.0.jar` — drop it into your `mods/` folder alongside Fabric API.

If Gradle complains about versions not resolving, open `gradle.properties` and update
`minecraft_version`, `yarn_mappings`, `loader_version`, and `fabric_version` to
the current matching set from <https://fabricmc.net/develop/>.

## Config

Edit `.minecraft/config/immersed-with-shading.json`:

```json
{
  "smallHeldItemsScale": 0.6,
  "bridgeIntervalMs": 220,
  "cloudSyncUrl": "https://gist.githubusercontent.com/YOU/ID/raw/config.json",
  "autoSyncOnStart": true
}
```

Press the `Sync Config From Cloud` keybind (unbound by default) to re-pull on demand.

## Bridge trainer

- Only works when `client.getServer() != null` (singleplayer or you hosting a LAN world).
- Press `B` to toggle.
- While enabled: sneak and walk backward holding any block — it auto-places behind/below you at `bridgeIntervalMs` cadence.
- The check is evaluated every tick on multiplayer servers and silently refuses; there is no way to force it on remote servers.
