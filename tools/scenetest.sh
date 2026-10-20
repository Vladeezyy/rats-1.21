#!/bin/sh
# Runs a client test scene (max 15 min): -Pscene=main (common/src/selftest/.../ClientScene).
# Screenshots in $LOADER/run/screenshots/scene_*.png; prints the checks and the errors of the run. Every run starts from the
# default configs and a new world. Extra gradle arguments are passed on (-Pscene_lang=ru_ru, -Pwith_compat,
# -Pneo_version=21.1.197 -Pno_jei).
cd "$(dirname "$0")/.."
# LOADER=neoforge (default) or LOADER=fabric
LOADER=${LOADER:-neoforge}
# processes of the dev runs: NeoForge passes the mod folders, Fabric's dev launcher the loom cache of the project
case $LOADER in
	neoforge) PROC='fml.modFolders=rats' ;;
	fabric) PROC="$(basename "$PWD")/(fabric/)?\.gradle/loom-cache" ;;
	*) echo "LOADER must be neoforge or fabric"; exit 2 ;;
esac
rm -rf $LOADER/run/saves/rats_scene $LOADER/run/screenshots/scene_*.png build-scene.log
rm -f $LOADER/run/config/rats-*.toml
perl -e 'alarm 900; exec @ARGV' ./gradlew :$LOADER:runScene --console=plain "$@" > build-scene.log 2>&1 &
gradle_pid=$!
while kill -0 $gradle_pid 2>/dev/null; do
	# a crash, or no display (macOS display asleep: run 'caffeinate -d -u -t 900 &' first)
	if grep -qE -- '---- Minecraft Crash Report ----|Failed to locate a primary monitor' build-scene.log 2>/dev/null; then
		sleep 3
		pkill -f "$PROC" 2>/dev/null
	fi
	sleep 2
done
wait $gradle_pid
echo "exit $?"
grep -E '\[scene\] (CHECK|DONE|PERF)' build-scene.log | sed -E 's/^.*\[scene\] //'
ls $LOADER/run/screenshots/ 2>/dev/null | grep scene_
echo "--- errors"
grep -E '/(ERROR|FATAL)\]|Caused by|Exception' build-scene.log | grep -v 'NoSuchFileException: server.properties' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
