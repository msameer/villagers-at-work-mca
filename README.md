# Villagers at Work: MCA Reborn extension

An optional extension that connects [Villagers at Work](https://github.com/msameer/villagers-at-work) to
MCA Reborn. When installed alongside both mods, it:

- lets MCA guards draw their gear from village armories instead of being equipped from nowhere, and
- lets blocked villagers explain in words what they need.

**Status:** early development. The guard half works:

- MCA no longer equips guards from nowhere. A guard wears only what it carries, and fetches what its
  kit lacks from the armories of the village's armorer, weaponsmith, leatherworker and fletcher.
- Guards loot the rotten flesh of their kills and bring it to a cleric that needs it.
- MCA villagers keep seed but not food, since MCA breeding does not use it.
- A blocked MCA villager says why, in chat and in MCA's own voice, once per cause among its
  neighbours and only to players nearby. `villagers-at-work-mca.json` turns chat and voice on or off
  separately, shows the core's error icon on MCA villagers too, and sets the limits. For voice and
  for the lines in your language, install the extension on the client as well.

Handedness and children as errand-runners are still to come.

## Why a separate repository

MCA Reborn is licensed under the GPL-3.0, so this extension is too, and its source is published here.
Villagers at Work itself does not depend on MCA or on any GPL code; the extension depends on it.

## Building

Requires Java 25.

```bash
./gradlew build
```

The Villagers at Work api is resolved from the public Maven repository at
`https://msameer.github.io/maven/`, and MCA Reborn from Modrinth's; no credentials are needed.

### Game tests

Game tests run against the Villagers at Work core jar, which is not published, so they run only
when you point the build at one:

```bash
./gradlew runGameTest -PvawCoreJar=/path/to/villagers-at-work-<version>.jar
```

Without `-PvawCoreJar` the build compiles the extension and skips them, which is what CI does. Add
`-PvawApiRepository=mavenLocal` to build against an api published locally from the core repository.

## Licence

GPL-3.0-or-later. See [LICENSE](LICENSE).
