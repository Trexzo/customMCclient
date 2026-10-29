# UX S27 — source-rendered Graphite module information hierarchy

## Actual references and evidence

This visual lane used the earlier user Library research notes describing three reference directions:
- Vape V4: clearly navigable module categories, compact switches and a dedicated three-dot settings action. The **official** [Vape Quick Start](https://docs.vape.gg/intro/quick-start) specifically documents category navigation and the three-dot module settings affordance; its screenshot shows compact module information without large decorative cards.
- Slinky: minimal/understated HUD and readable compact controls (research notes), not copied branding, binaries, assets, or code.
- Rise: secondary descriptions, restrained separation and information hierarchy (research notes), not copied interface geometry or visuals.

The Library content search returned retrospective reference notes and one Slinky configuration JSON, **not verified Vape/Slinky/Rise UI binaries**. This PR claims design inspiration rather than an asset-level donor comparison. Public Vape documentation independently corroborates navigation behavior.

The actual original Minecraft 1.8.9 screenshots from [S25 official-game run #40697489881](https://github.com/Trexzo/customMCclient/actions/runs/40697489881), artifact `12819354015`, reveal a large unused horizontal region inside each single-line module row, tiny title labels, and category headings with weak separation. S25's three-item sidebar and safely masked 18px bottom footer remain untouched.

## Implemented source changes (graphite, no Nebula)

- New `UiModuleCardText` renders one **source-native two-line module entry** when a registered `ModuleDescriptor.description` exists and the real row is wide enough. The title is higher-contrast and the description quieter, both using the actual Minecraft UiTextCommand, not a synthetic screenshot or custom font.
- Both lines share the same existing GL text-scissor width, terminating **before** the switch and three-dot detail action. A missing description or constrained viewport automatically uses the existing centered single-line title.
- Module row geometry is unchanged: 29px height + 4px gap, identical switch and details pointer hitboxes, same source state transitions, same input controller. No settings/module lifecycle/persistence changes. Existing search, category order and original scrolling still operate on authoritative registries.
- Category headers now have stronger typographic hierarchy, a thin low-contrast separator and a live count of visible modules in that group (including search-filtered results). Counts are derived from the same filtered list, not a cached/guessed total.
- No purple, glowing outlines, giant tiles, custom fonts, copied graphics or background blur; this retains a quiet graphite identity.

## Verification gates

`UiModuleCardTextTest` checks two distinct clipped lines, actual native text y positions, narrow viewport fallback, empty description fallback, and preserved control reservation. `ClickGuiModuleInformationHierarchyTest` checks descriptions and per-category count originate from registered module metadata and confirms the exact same physical first-row click toggles the same module. The existing Foundation and original 1.8.9 suite also remain mandatory.

This S27 change stacks **only on S25 #862** at `7600acf453c7e0a398ab34d3c1366baa56e5c1a6`, not on the concurrent S26 #863 native mouse-focus diagnostic branch or any combat branch. Keep draft pending exact-head Windows/Ubuntu Foundation and original Mojang screenshot review. A native visual capture of the **new two-line rows** must be inspected before calling the new appearance verified. No main merge.
