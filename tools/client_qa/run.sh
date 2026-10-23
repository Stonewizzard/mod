#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
mkdir -p build/client-qa run/visual-qa
glxinfo -B >build/client-qa/graphics.txt 2>&1 || true
for qa_lvp_icd in /usr/share/vulkan/icd.d/lvp_*.json; do
    if [ -f "$qa_lvp_icd" ]; then export VK_DRIVER_FILES="$qa_lvp_icd"; break; fi
done
vulkaninfo --summary >build/client-qa/vulkan.txt 2>&1 || true
rm -f build/client-qa/state.json build/client-qa/command.json
cat > run/visual-qa/options.txt <<'EOF'
onboardAccessibility:false
startedCleanly:true
narrator:0
guiScale:3
maxFps:30
enableVsync:false
fullscreen:false
renderDistance:2
simulationDistance:5
EOF
./gradlew runClientVisualQa >build/client-qa/client.log 2>&1 &
client_pid=$!
export CLIENT_QA_PID="$client_pid"
video_pid=""
cleanup() {
    kill "$client_pid" 2>/dev/null || true
    if [ -n "$video_pid" ]; then kill -INT "$video_pid" 2>/dev/null || true; fi
}
trap cleanup EXIT
# The recording contains only this disposable development game window.
ffmpeg -nostdin -loglevel error -f x11grab -video_size 1280x720 -framerate 10 -i "$DISPLAY" -c:v libx264 -preset ultrafast -crf 30 -pix_fmt yuv420p -y build/client-qa/session.mp4 >build/client-qa/video.log 2>&1 &
video_pid=$!
set +e
python3 tools/client_qa/drive.py
qa_result=$?
kill -INT "$video_pid" 2>/dev/null
wait "$video_pid" 2>/dev/null
# The bridge requests a graceful native client shutdown, allowing Gradle to exit.
for attempt in $(seq 1 30); do
    if ! kill -0 "$client_pid" 2>/dev/null; then break; fi
    sleep 1
done
if kill -0 "$client_pid" 2>/dev/null; then kill "$client_pid" 2>/dev/null; fi
wait "$client_pid"
client_result=$?
set -e
if [ "$qa_result" -ne 0 ]; then tail -100 build/client-qa/client.log; exit "$qa_result"; fi
exit "$client_result"
