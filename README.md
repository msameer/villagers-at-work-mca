# Villagers at Work: MCA Reborn extension

An optional extension that connects [Villagers at Work](https://github.com/msameer/villagers-at-work) to
MCA Reborn. When installed alongside both mods, it:

- lets MCA guards draw their gear from village armories instead of being equipped from nowhere, and
- lets blocked villagers explain in words what they need.

**Status:** early development. The extension currently loads and does nothing.

## Why a separate repository

MCA Reborn is licensed under the GPL-3.0, so this extension is too, and its source is published here.
Villagers at Work itself does not depend on MCA or on any GPL code; the extension depends on it.

## Building

Requires Java 25.

```bash
./gradlew build
```

The Villagers at Work api is resolved from the public Maven repository at
`https://msameer.github.io/maven/`; no credentials are needed.

## Licence

GPL-3.0-or-later. See [LICENSE](LICENSE).
