# Language support

Thirst Was Upgraded includes **143 language files**, each containing all ten
current item-name, upgrade-title, and filter-tooltip keys.

The catalogue contains the **137 selectable languages from Minecraft 1.21.1's
asset index 17**, plus six additional locales matching Molten Vents Plus:
`cv_cu`, `fr_ch`, `go_fr`, `got_de`, `uz_uz`, and `vro`. Additional resource-pack
locales do not add entries to Minecraft's language menu by themselves.

## Translation status

These are initial translations, with machine-generated drafts, terminology
corrections, and manually authored entries. They have not received native-speaker
review. Complete key coverage does not imply a fluent translation in every locale.

| Status | Language files |
| --- | ---: |
| English and regional English | 5 |
| Initial translations | 89 |
| Regional variants of a translated language | 14 |
| Latin alphabet variants | 2 |
| Stylized English options | 5 |
| Explicit English fallback | 28 |

Regional variants share their base language's initial text, rather than claiming
distinct regional vocabulary. `sr_cs` and `be_latn` use Latin-script versions of
the Serbian and Belarusian entries. Pirate, Anglish, Shakespearean, LOLCAT, and
upside-down English have their own styled text. Classical Chinese, Asturian,
Nynorsk, and Toki Pona include manually authored initial entries.

These locales intentionally use English until a reliable translation is contributed:

| Language | File code |
| --- | --- |
| Boarisch | `bar` |
| Braobans | `brb` |
| Andalûh | `esan` |
| Fränggisch | `fra_de` |
| Gallo | `go_fr` |
| Gothic | `got_de` |
| Галицка | `hal_ua` |
| Høgnorsk | `hn_no` |
| Ido | `io_en` |
| Medžuslovjansky | `isv` |
| la .lojban. | `jbo_en` |
| Kölsch/Ripoarisch | `ksh` |
| Kernewek | `kw_gb` |
| Mēxikatlahtōlli | `nah` |
| Plattdüütsch | `nds_de` |
| Övdalska | `ovd` |
| Ngiiwa | `pls` |
| Cántabru/Montañés | `qcb_es` |
| Bhs. Indonesia edjaän lama | `qid` |
| Quenya | `qya_aa` |
| Русскій дореформенный | `rpr` |
| Руснацькый | `ry_ua` |
| Säggs’sch | `sxu` |
| tlhIngan Hol | `tlh_aa` |
| Bats'i k'op | `tzo_mx` |
| Viossa | `vp_vl` |
| Voro | `vro` |
| بهاس ملايو | `zlm_arab` |

The status table and fallback list above describe the current coverage. The
[language catalogue](gradle/minecraft-languages.json) retains native
names and right-to-left metadata. Minecraft handles right-to-left rendering.

## Contributing

1. Edit the appropriate UTF-8 JSON in
   [the language directory](src/main/resources/assets/thirst_was_upgraded/lang/).
   Use `en_us.json` as the key reference. Translate values and keep keys unchanged.
2. Preserve formatting arguments such as `%s` if future strings introduce them.
   Indexed arguments such as `%2$s` may reorder them.
3. Keep the two Drinking Upgrade names consistent. The item-tooltip key intentionally
   uses the `travelersbackpack` namespace because its upgrade API requests that key.
4. Update this guide when replacing an English fallback or base-language copy.
5. Run `./gradlew validateTranslations` (`gradlew.bat validateTranslations` on Windows)
   with JDK 21. `build`, `check`, and resource packaging run this automatically.

Validation checks the catalogue, exact key coverage, JSON parsing, nonempty string
values, replacement characters, and formatting arguments. It cannot assess fluency.

Normal builds and gameplay use the checked-in language files and never call a
translation service. A regular build needs only JDK 21.

Contributions are covered by the project's [MIT license](LICENSE).
