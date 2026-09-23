# GhostSpear

A Paper 1.21.11 plugin inspired by ManePear's "I Found Minecraft's Fastest Speed".

- **Phantom Dash** – swing the Ghost Spear to launch forward. A ghost copy of you (with your skin and armor) is left where you started, plus a short trail of ghosts along the dash.
- **Soul Pierce** – right after a dash, hold right-click (spear charge). Anything you run the tip into gets one-tapped.
- No fall damage for a few seconds after dashing.

## Commands
| Command | Permission | Default |
|---|---|---|
| `/ghostspear give [player]` | `ghostspear.give` | OP |
| `/ghostspear reload` | `ghostspear.reload` | OP |
| (using the spear) | `ghostspear.use` | everyone |

Alias: `/gspear`

## Requirements
- **Paper** (or a Paper fork like Purpur) **1.21.11**. Plain Spigot won't work – the ghosts use Paper's Mannequin API.
- Java 21 (Paper 1.21.11 already needs this).

## Building
GitHub Actions builds it automatically on every push. The finished jar shows up in two places:
1. The **Releases** section on the right side of the repo page (easiest – just click the `.jar`).
2. The **Actions** tab → latest run → **Artifacts** → `GhostSpear-jar` (downloads as a zip with the jar inside).

To build locally instead: `mvn package` → `target/GhostSpear-1.0.0.jar`.

## Config
Everything is in `plugins/GhostSpear/config.yml` – dash strength, cooldown, ghost lifetime, pierce damage, whether a dash is required before a pierce, etc. Run `/ghostspear reload` after editing.
