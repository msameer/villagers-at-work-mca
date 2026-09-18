# Villagers at Work: MCA Reborn extension

An optional extension that connects [Villagers at Work](https://github.com/msameer/villagers-at-work) to
MCA Reborn. When installed alongside both mods, it:

- lets MCA guards draw their gear from village armories instead of being equipped from nowhere, and
- lets blocked villagers explain in words what they need.

**Status:** early development. The extension registers itself as the guard provider, which switches
on village armories, and gives MCA villagers their own reserve profile: they keep seed, not food.
Guards drawing their gear from armories is in progress.

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
