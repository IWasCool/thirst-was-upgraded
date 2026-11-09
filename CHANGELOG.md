# Changelog

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
