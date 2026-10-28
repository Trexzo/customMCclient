# UX S19 — Native ClickGUI module-row controls

**Scope:** Pure ClickGUI presentation/input in the existing Minecraft 1.8.9 UI lane. Stacked on S18 PR #845 at exact initial SHA `eabbd41bf04950b7020f7cff3ccb654537bebd4f`. No combat, World, controller, packet, module lifecycle, or auto-click scheduling modifications.

## Current visual references (October 2026)

- [Official Vape GUI usage](https://docs.vape.gg/intro/quick-start): compact module rows, module enable/disable by name, independent settings access via trailing dots; adapt the discoverability, not visual assets or copied code.
- [Official Slinky site](https://slinky.gg/): neutral dark surfaces, sparse decoration, tight grouping, and high contrast controls.
- [Official Rise site](https://riseclient.com/): inspiration for configurable visual presentation and HUD options, not mandatory purple coloring.

This design intentionally **does not resurrect Nebula**. It retains S17's default Graphite and S18's actual ArrayList motion.

## Functional changes

1. Module rows show a small trailing **...** detail action when a settings selection and detail-page route are configured.
2. Left-clicking that final 26-pixel region selects and navigates to module settings; the module is not toggled. Left-clicking anywhere else in the row still toggles the module. Right-click on the row continues to open details, as before.
3. The toggle shifts left only when a functioning detail action exists. Standalone module lists keep the old full-width toggle behavior and do not advertise an unavailable detail action.
4. Every module-name draw is enclosed in its own bounds-limited clip region, reserving horizontal room for actions, so long text never paints over the toggle/details control.
5. Status rail now differentiates ERROR and transitional states using the existing theme's danger/warning roles; a disabled module has no rail. No fabricated module state or new dependency.

## Native/CI acceptance checklist

- Native 1.8.9 GUI screenshot **required** for clipping/spacing/font verification. Unit tests cannot certify typography, real GL scissor behavior or perceived visual quality.
- New deterministic tests: row action routing, long-name clip boundary, fallback without configured detail page, right-click shortcut.
- Existing module filtering and scroll-hit parity tests must continue passing.
- Run exact-head Foundation on Ubuntu and Windows and original Mojang 1.8.9 UI acceptance. Preserve any red runs and do not claim production acceptance from static screenshots or Java doubles.
- Keep this branch **draft** until reviewed alongside S17/S18 in the real Minecraft 1.8.9 GUI. Do not merge in isolation into `main`.

## Further visual lane, pending acceptance

- S20: polish HUD typography/spacing with actual host font metrics and 1.8.9 screenshots; verify legibility against world backgrounds before changing colors.
- S21: configurable visual preferences and reduced-motion parity in all overlays where justified; keep defaults understated.
- S22: compare side-by-side native GUI screenshots at 720p/1080p, GUI scales, long localized labels and tiny viewports, then fix measured clipping/spacing defects.

**Parallel development contract:** UI PRs stack on prior UI PRs. Combat PRs stack independently on their combat heads. No cherry-pick or merge across lanes without a deliberate integration PR and strict native acceptance.
