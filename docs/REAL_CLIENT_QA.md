# Real Minecraft visual QA

This is a separate graphical test run. It starts **Minecraft 26.3 with NeoForge**, an actual X11 window and software OpenGL. `xdotool` sends mouse and keyboard input through X11 and SDL to Minecraft. ImageMagick captures the presented window, and FFmpeg records the session. The output is not reconstructed from drawing commands, fonts, mock widgets or model meshes.

[The first actual-client review](BOOK_VISUAL_BASELINE.md) records observed book defects and includes actual game captures.

The earlier layout/datagen checks remain useful for bounds and content validation. They cannot approve visual quality or prove that an interface feels usable.

## Running it

On pull requests that change the guide or this tooling, the **Real Minecraft visual QA** workflow runs in GitHub Actions. You can also run it manually from Actions once the workflow is on the default branch.

Each run uploads `real-minecraft-visual-qa`, containing:

- `captures/*.png`: full-resolution, actual game-window screenshots.
- Matching JSON files: the live screen state at each capture, including the window size and GUI scale.
- `session.mp4`: a recording of the virtual desktop, including client startup and interactions.
- `timeline.json`: actions and their resulting live states.
- `result.json`, `client.log` and `video.log`: functional results and diagnostics.

`graphics.txt` records the actual OpenGL renderer. To decode screenshots from a downloaded job log, run `python3 tools/client_qa/import_evidence.py job.log output-directory`.

A small selection of screenshots is also encoded in the job log with `MC_QA_IMAGE` markers. This lets the GitHub connector retrieve the actual pixels for visual inspection without copying Minecraft runtime assets or requiring an account login. These are disposable CI captures, not screenshots of a player's PC.

On a Linux machine with Java 25, X11, Xvfb, Mesa OpenGL/Vulkan software drivers, xdotool, ImageMagick and FFmpeg installed:

```bash
LIBGL_ALWAYS_SOFTWARE=1 ALSOFT_DRIVERS=null SDL_VIDEODRIVER=x11 SDL_VIDEO_FORCE_EGL=1 \
  xvfb-run -a -s '-screen 0 1280x720x24 +extension GLX +render -noreset' \
  bash tools/client_qa/run.sh
```

For manual inspection on a machine with a desktop, `./gradlew runClientVisualQa` opens the same actual client and book. Normal `runClient` and released gameplay never enable the QA bridge. The graphical QA client uses the isolated `run/visual-qa` profile and does not overwrite normal development-client settings.

## Current scope

The script uses OS input to create a disposable Creative/Superflat world through Minecraft's native UI. The development bridge puts the five real book items in the hotbar. The script selects each slot and right-clicks it through the normal item-opening path, then exercises that edition's starting point, categories, semantic sections, related links, preserved search/history, no-match recovery, natural-language lookup, modifier topics, Escape, keyboard Tab/Return activation and session bookmarks. It captures all five books at GUI scales 2 and 3 and at 1280×720 and 854×480. A hotbar capture verifies the actual item icons.

Minecraft 26.3 binds item data when a world loads, so a title-screen-only book preview is invalid. The bridge bootstraps one screen only after world entry, provides test inventory fixtures, changes GUI scale, quits, and reports read-only live state. It does not call the guide's navigation actions. All comparison navigation and subsequent item opening is performed by OS input. Normal clients never register this bridge. The fixture books do not add a gameplay grant to released worlds.

GitHub Actions provides a repeatable screenshot-and-input loop, not a live remote desktop connection to the user's computer. A new scripted run takes a few minutes. The test harness can be run interactively in an attached Linux desktop when that capability is available. Software-rendered CI frame rates are not representative of a player’s GPU performance.

## Persona-based review

The five simulated experience levels, acceptance criteria and design tradeoffs are documented in [the design plan](FIVE_BOOK_DESIGN_PLAN.md). [The completed review](FIVE_BOOK_REVIEW.md) records the critical findings, revisions, selection and native captures.

These are simulated task perspectives, not evidence from human participants. A passing script establishes that the expected actions completed. Screenshot review is required to judge presentation; small-window comfort, localization, screen-reader output and real player comprehension still benefit from human feedback. Do not infer that the book looks good merely because tests pass.
