# Changelog

## 1.0.0 | Stable Release

- Promote the resource-pack-aware drinking-button update to the first stable release.
- Includes automatic drinking, nine-slot allow/block filters, purity controls, and backpack tank-filling fixes.
- Includes all 143 language files, with translation coverage and English fallbacks documented in TRANSLATIONS.md.
- Preserve the existing mod ID, item IDs, saved upgrades, filter settings, and drinking behavior from 0.0.7.

## 0.0.7 | Resource-Pack-Aware Drinking Buttons

- Draw the two water-button backgrounds and hover highlights from Traveler's Backpack's native GUI atlas, so backpack GUI resource packs can style them.
- Draw water symbols from Thirst Was Reclaimed's HUD atlas, so thirst-bar resource packs also apply. Keep full, half-full, and empty thirst states and a green tint for allowing impure water.
- Stop rendering the old combined water-icon/background texture, including the baked-background override bundled with Create New World Core.
- Preserve compatibility with Create New World Core's existing water-icon mixin without requiring a core update.

## 0.0.6 | Project Cleanup & Languages

- Updated author credit to Izcool and added public source and issue links.
- Replaced template documentation and added the MIT license, retaining the NeoForge template notice.
- Download pinned dependencies automatically; local modpack JARs are no longer needed to build.
- Added language files for all 137 Minecraft 1.21.1 language options and six additional community locales. Translation status and fallbacks are documented in TRANSLATIONS.md.
- Validate language coverage, keys, values, and formatting arguments during builds.
- Simplified core registration and tank-drinking code without changing saved IDs, filter settings, or drinking behavior.
- Added reproducible mod/source JARs and GitHub Actions builds; excluded local worlds, logs, caches, and dependency copies from Git.

## 0.0.5 | Drink Filters & Backpack Tank Fixes

- Added nine whitelist/blacklist slots for drinks and tank water, respecting purity.
- Aligned the drinking filter buttons with each other and the filter slots.
- Removed automatic hose actions to prevent spills and lava consumption.
- Fixed Create spouts filling only one backpack tank.
- Removed the Minecraft version suffix from the mod version and jar filename.
