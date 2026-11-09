# Thirst Was Upgraded

A thirst-aware **Drinking Upgrade** for Traveler's Backpack, by **Izcool**.
For **Minecraft 1.21.1 / NeoForge**. Install on both the client and server.

**Source:** [IWasCool/thirst-was-upgraded](https://github.com/IWasCool/thirst-was-upgraded)
| **Version:** 1.0.0 | **License:** [MIT](LICENSE)

## Features

- Automatically drink thirst-restoring drinks from backpack storage or water from either backpack tank.
- Nine filter slots with allow-list and block-list modes. A water bottle or bucket represents tank water; purity is respected.
- Choose when to drink: whenever thirst is missing, when at least half the hydration would be used, or only when none would be wasted.
- Choose purified water only or all water purities. Normal thirst-mod purity effects still apply.
- Keep feeding and drinking behavior coordinated with Thirst Was Reclaimed.
- Fill both backpack tanks correctly with external fluid handlers, including Create Spouts.
- Resource-pack-aware drinking controls: backgrounds use Traveler's Backpack's GUI atlas, and droplets use Thirst Was Reclaimed's thirst-bar atlas.

Hoses are excluded from automatic drinking to avoid spills and accidental fluid use.

The water controls borrow the allow/block button's frame and background color from
`assets/travelersbackpack/textures/gui/icons.png`. Their water symbols use
`assets/thirst/textures/gui/thirst_icons.png`. Resource packs can replace those
normal upstream textures; no separate Thirst Was Upgraded background override is
needed. The old combined `thirst_was_upgraded:textures/gui/icons.png` is no longer
used, including overrides bundled with older Create New World Core builds.

## Installation

Use Minecraft **1.21.1**, **NeoForge 21.1.248+**, and these required mods:

| Mod | Minimum version |
| --- | --- |
| [Traveler's Backpack](https://modrinth.com/mod/travelersbackpack) | 10.1.38 |
| [Thirst Was Reclaimed](https://modrinth.com/mod/thirst-was-reclaimed) | 1.21.1-3.0.4 |

Download the mod JAR from [GitHub Releases](https://github.com/IWasCool/thirst-was-upgraded/releases/latest),
or download the `thirst-was-upgraded` artifact from a successful
[GitHub Actions build](https://github.com/IWasCool/thirst-was-upgraded/actions/workflows/build.yml)
and extract the ZIP, or build from source. GitHub requires sign-in to download build artifacts.
Place `thirst_was_upgraded-neoforge-1.21.1-1.0.0.jar` in the `mods` folder on the client and server,
replacing older Thirst Was Upgraded JARs so only one version is installed.

**Optional integrations:** Create 6.0.10+ adds Builder's Tea support; Tough As Nails
10.1.0.13+ adds canteen support. Install each optional mod's own dependencies as usual.
Neither optional mod is needed to build or run this addon.

## Using the upgrade

Craft one blank Traveler's Backpack upgrade with two water bottles and two redstone dust:

```text
    Water bottle
Redstone  Blank upgrade  Redstone
    Water bottle
```

Install the Drinking Upgrade in your backpack and enable it. Store drinks in
the backpack or install a Tanks Upgrade and fill a tank with water. Open the
drinking tab to configure the filter, thirst threshold, and water purity.

## Languages

Includes complete key coverage for all **137 Minecraft 1.21.1 language options**,
plus six additional community locales matching the other Our Creation mods.
Initial translations, regional variants, and explicitly listed English fallbacks
are described in [TRANSLATIONS.md](TRANSLATIONS.md). Contributions and corrections are welcome.

## Building

Install **JDK 21** and run:

```powershell
./gradlew.bat build
```

On Linux or macOS, run `./gradlew build`. Gradle downloads pinned dependencies
automatically; no local modpack JARs are required. The first build needs internet access.
The mod is written to `build/libs/thirst_was_upgraded-neoforge-1.21.1-1.0.0.jar`.
Builds validate every language file before packaging the mod.
Use `./gradlew validateTranslations` to check translations separately.

For development, use `./gradlew runClient` or `./gradlew runServer`.
Optional compatibility mods can be placed in the development `run/mods` directory.
Local tests, authoring tools, dependency copies, worlds, logs, IDE files, and caches
are excluded from Git. The public repository contains the mod source and assets,
build files, licenses, and release documentation.

See [CHANGELOG.md](CHANGELOG.md) for changes and the
[issue tracker](https://github.com/IWasCool/thirst-was-upgraded/issues) to report problems.
The [NeoForge template license](TEMPLATE_LICENSE.txt) is retained; dependency mods
remain separate projects under their own licenses and are not bundled in this addon.
