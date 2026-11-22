#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 4 ]]; then
  printf 'Usage: bash build.sh <Android SDK directory> <build-tools version> <PKCS12 keystore> <key alias>\n' >&2
  exit 2
fi

sdk_dir=$(cd "$1" && pwd -P)
tools_dir="$sdk_dir/build-tools/$2"
platform_jar="$sdk_dir/platforms/android-34/android.jar"
keystore=$3
key_alias=$4
project_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
build_dir="$project_dir/build"
if [[ ! -r "$platform_jar" || ! -r "$keystore" ]]; then
  printf 'The Android 34 platform or the signing keystore could not be read.\n' >&2
  exit 1
fi

mkdir -p "$build_dir"
stage_dir=$(mktemp -d "$build_dir/staging.XXXXXX")
trap 'rm -rf -- "$stage_dir"' EXIT
mkdir "$stage_dir/generated" "$stage_dir/classes" "$stage_dir/dex"

"$tools_dir/aapt2" compile --dir "$project_dir/res" -o "$stage_dir/resources.zip"
"$tools_dir/aapt2" link -I "$platform_jar" --manifest "$project_dir/AndroidManifest.xml" \
  --java "$stage_dir/generated" -o "$stage_dir/unsigned.apk" "$stage_dir/resources.zip"
javac --release 8 -encoding UTF-8 -classpath "$platform_jar" -d "$stage_dir/classes" \
  "$stage_dir/generated/net/localtools/tclhotelmenu/R.java" \
  "$project_dir"/src/net/localtools/tclhotelmenu/*.java
jar --create --file "$stage_dir/classes.jar" -C "$stage_dir/classes" .
"$tools_dir/d8" --min-api 23 --lib "$platform_jar" --output "$stage_dir/dex" "$stage_dir/classes.jar"
zip -qj "$stage_dir/unsigned.apk" "$stage_dir/dex/classes.dex"
"$tools_dir/zipalign" -f 4 "$stage_dir/unsigned.apk" "$stage_dir/aligned.apk"

sign_options=(--ks "$keystore" --ks-key-alias "$key_alias" --v4-signing-enabled true)
if [[ -n ${TCL_HOTEL_SIGNING_PASSWORD:-} ]]; then
  sign_options+=(--ks-pass env:TCL_HOTEL_SIGNING_PASSWORD)
fi
"$tools_dir/apksigner" sign "${sign_options[@]}" \
  --out "$stage_dir/Hotel-Menu-Shortcut.apk" "$stage_dir/aligned.apk"
"$tools_dir/apksigner" verify "$stage_dir/Hotel-Menu-Shortcut.apk"
mv "$stage_dir/Hotel-Menu-Shortcut.apk" "$stage_dir/Hotel-Menu-Shortcut.apk.idsig" "$build_dir/"
printf '%s\n' "$build_dir/Hotel-Menu-Shortcut.apk"
