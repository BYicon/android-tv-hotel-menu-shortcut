# Android TV Hotel Menu Shortcut

English · [简体中文](README.zh-CN.md)

Open a compatible Android TV's built-in **Hotel Menu** from **All Apps**, using the remote. This provides a quick way to reach **Max Volume** when casting from a phone can also raise the TV's volume.

Turn on **Hotel Enable**, then set the ceiling under **Max Volume**. **Preset Volume** controls the volume at startup. The shortcut opens the menu and leaves its controls to the TV.

## Install

1. Download and extract the APK installation ZIP from [GitHub Releases](https://github.com/BYicon/android-tv-hotel-menu-shortcut/releases/latest).
2. Install `Hotel-Menu-Shortcut.apk`. You can use a USB drive and the TV's media center on firmware that permits APK installation from USB. The verified installation used ADB. [TCL installation guide](https://detail.youzan.com/show/shopnote/detail?alias=fXf4vHMVxL)
3. Open **Hotel Menu** from the TV's app list. The shortcut runs locally with a remote after installation.

### Install with ADB

On the tested mainland-China TCL Android TV firmware, go to **Settings → About → Device Info**. While on that screen, press **Up, Down, Left, Right** in quick succession, with less than a second between presses. Open the newly visible **More → ADB** switch. The menu and connection procedure can vary by firmware. [Firmware notes](https://github.com/pengchujin/aerial-tv/blob/main/docs/devices/tcl-mt5879-cn.md#打开-adb)

Connect the TV and computer to the same network. Install Android SDK Platform-Tools, keep the APK and its matching `.apk.idsig` next to one another, and run these commands from the extracted folder. Replace the example address with the TV's actual ADB address. Approve the connection on the TV if prompted. [Android ADB guide](https://developer.android.com/tools/adb) · [APK signature scheme v4](https://source.android.com/docs/security/features/apksigning/v4)

```bash
tv_address='192.0.2.10:5555'
adb connect "$tv_address"
adb -s "$tv_address" install --user 0 -r Hotel-Menu-Shortcut.apk
```

## Compatibility

Version 1.1.0 was tested on a mainland-China TCL TV reporting model `tcl_mt5879_cn`, Android 11, and firmware build `2026012200`. Both the direct shortcut and Setup's **Try** button opened **Hotel Menu** with **Max Volume**. The APK can be installed on Android 6.0 and later where sideloading is permitted. Compatibility depends on the menu activity and parameters exposed by the firmware.

On the known model and build, the built-in profile opens the verified target directly. On other builds, use **Hotel Menu Setup** to test a verified firmware entry and save it for one-click access. Setup is also available from the app list when you need to edit a saved entry.

The built-in TCL entry uses:

| Field | Value |
| --- | --- |
| Component | `com.tcl.factory.view/.MainMenu` |
| Action | `com.tcl.factory.intent.action.FACTORYMENU` |
| Integer extra | `select=5` |

**Load TCL preset** fills in the fields above. For a different firmware, enter the component as `package/activity` using the documented Activity for its hotel menu. Fill in the action and the integer extra name and value if the firmware expects them. Choose **Try**, confirm that the TV displays **Hotel Menu** with **Max Volume**, then return to Setup and choose **Save**. For a match, Android must let an ordinary app open the target Activity and the supplied parameters must select the hotel menu.

## How it works

The shortcut registers a TV launcher entry and calls Android's `startActivity()` with an explicit Intent. The built-in TCL profile supplies the factory menu component, action and integer extra above. The TV renders its own menu and applies any settings you choose. The app requests no Android permissions. ADB authorization is only needed if you use a computer to install it. [Android TV launcher documentation](https://developer.android.com/training/tv/get-started/create) · [Android Intent reference](https://developer.android.com/reference/android/content/Intent)

## Build from source

Use JDK 17, `zip`, Android SDK Platform 34 and Android SDK Build-Tools. The build script has been tested with Build-Tools 30.0.3. Create a local signing key once, then run the following from the repository root.

```bash
keytool -genkeypair -storetype PKCS12 -keystore hotel-local.p12 \
  -alias hotel-menu -keyalg RSA -keysize 2048 -validity 3650 \
  -dname 'CN=Android TV Hotel Menu Shortcut'

bash build.sh /path/to/Android/sdk 30.0.3 hotel-local.p12 hotel-menu
```

The script writes `build/Hotel-Menu-Shortcut.apk` and its `.idsig` companion. Android requires the same signing key to install an update over an existing installation.
