# Real Minecraft visual QA

This is a separate graphical test run. It starts **Minecraft 26.3 with NeoForge**, an actual X11 window and software OpenGL. `xdotool` sends mouse and keyboard input through X11 and SDL to Minecraft. ImageMagick captures the presented window, and FFmpeg records the session. The output is not reconstructed from drawing commands, fonts, mock widgets or model meshes.

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

The pilot uses OS input to create a disposable Creative/Superflat world through Minecraft’s native UI, then opens the actual book in that world. Minecraft 26.3 binds item data when a world loads, so a title-screen-only book preview is invalid. The pilot does not yet test obtaining the book or claim to cover unrelated gameplay. It verifies the book's actual textures, text, native widget rendering and OS input at GUI scales 2 and 3 and at 1280×720 and 854×480 window sizes. It exercises search, topics, article pages, related links, history, Escape, Close and reopening.

The bridge only opens the guide after world entry, changes the GUI scale, quits and reports live state. It does not call the guide's navigation actions. All pilot navigation is performed by OS input. Its observers report the same control bounds that the real screen creates; screenshot inspection is still needed to detect misleading or badly drawn controls.

GitHub Actions provides a repeatable screenshot-and-input loop, not a live remote desktop connection to the user's computer. A new scripted run takes a few minutes. The test harness can be run interactively in an attached Linux desktop when that capability is available.

## Persona-based review

The script supplies reproducible evidence for three initial task paths:

| Perspective | Task | Visual/usability questions to assess from the actual client |
|---|---|---|
| New archaeologist | Open the introduction, follow it to Sniffers, then return | Is the starting point obvious? Are the headings readable? Does the page explain a concrete next step? |
| Returning player | Search for Magnetized and read its details | Is search visible? Are the results clear? Do page changes and Back preserve context? |
| Equipment builder | Browse modifier topics and inspect an area modifier | Do the categories make sense? Is important information easy to compare? Can the player recover from taking the wrong path? |

These are simulated task perspectives, not evidence from human participants. A passing script only establishes that the expected actions completed. After viewing the real screenshots and video, record issues separately by severity: blockers, readability/navigation problems, clutter and visual polish. Do not infer that the book looks good merely because tests pass.

Future book revisions should run these paths again and review the images before being marked ready. Expand the paths to include ordinary in-world book use and inventory presentation when world-level UI testing is added.
