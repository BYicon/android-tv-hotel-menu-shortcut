# TV Hotel Menu — Android TV 音量上限菜单

[English](README.md) · 简体中文

在兼容的 Android TV 上，从 **全部应用** 选择 **酒店菜单**，就能用遥控器打开原厂 **Hotel Menu**。如果手机投屏时按音量加会连带调高电视声音，可以在这个菜单里找到音量上限。

开启 **Hotel Enable**，再用 **Max Volume** 设置最高音量。**Preset Volume** 管开机音量。快捷 App 负责打开页面，设置仍由电视处理。

## 安装

1. 从 [GitHub Releases](https://github.com/BYicon/tv-hotel-menu/releases/latest) 下载并解压 APK 安装包。
2. 安装 `Hotel-Menu-Shortcut.apk`。支持从移动存储安装 APK 的固件，可以通过 U 盘和电视的媒体中心安装。本项目实测使用 ADB 安装。 [TCL 安装说明](https://detail.youzan.com/show/shopnote/detail?alias=fXf4vHMVxL)
3. 从电视应用列表打开 **酒店菜单**。装好后日常只用遥控器即可。

### 使用 ADB 安装

在实测的国行 TCL Android TV 固件上，进入 **设置 → 关于 → 本机信息**。停在这个页面，用遥控器快速依次按 **上、下、左、右**，相邻两键间隔不到一秒。然后打开新增的 **更多 → ADB** 开关。菜单位置和连接方式可能随固件变化。 [同平台固件笔记](https://github.com/pengchujin/aerial-tv/blob/main/docs/devices/tcl-mt5879-cn.md#打开-adb)

让电脑和电视连接同一个局域网。在电脑上安装 Android SDK Platform-Tools，让 APK 和对应的 `.apk.idsig` 留在同一个目录，然后从解压目录执行下列命令。将示例地址换成电视实际的 ADB 地址。如果电视弹出授权窗口，在电视上确认连接。 [Android ADB 说明](https://developer.android.com/tools/adb) · [APK v4 签名说明](https://source.android.com/docs/security/features/apksigning/v4)

```bash
tv_address='192.0.2.10:5555'
adb connect "$tv_address"
adb -s "$tv_address" install --user 0 -r Hotel-Menu-Shortcut.apk
```

## 兼容性

已在国行 TCL（系统型号 `tcl_mt5879_cn`、Android 11、固件增量版本 `2026012200`）实测 1.1.0 版。直接打开和设置页的 **试用** 都能进入带 **Max Volume** 的酒店菜单。允许侧载的 Android 6.0 及以上系统可以安装 APK。能否打开目标菜单，取决于具体固件开放的入口和参数。

在已验证的型号和固件上，内置预设可以直达酒店菜单。其他固件可在 **酒店菜单设置** 中测试已确认的固件入口，再保存为一键入口。需要修改已有配置时，也可以直接从应用列表打开 **酒店菜单设置**。

内置 TCL 入口如下。

| 字段 | 值 |
| --- | --- |
| 组件 | `com.tcl.factory.view/.MainMenu` |
| Action | `com.tcl.factory.intent.action.FACTORYMENU` |
| 整数参数 | `select=5` |

点 **加载 TCL 预设** 会填好上表中的字段。配置其他固件时，将该固件已知的酒店菜单组件按 `包名/Activity` 格式填写。按需填写 Action 和整数参数的名称和值。先选 **试用**，看到 **Hotel Menu** 和 **Max Volume** 后，返回设置页选择 **保存**。兼容的固件需要允许普通应用打开目标 Activity，并通过所填参数进入酒店菜单。

## 实现原理

快捷 App 注册了电视应用入口，并通过 Android `startActivity()` 发送显式 Intent。TCL 预设使用上表中的组件、Action 和整数参数。菜单由电视的原厂应用绘制，调整的选项由电视保存。App 本身无需授予 Android 权限。用电脑安装时，ADB 授权用于调试连接。 [Android TV 启动入口说明](https://developer.android.com/training/tv/get-started/create) · [Android Intent 参考](https://developer.android.com/reference/android/content/Intent)

## 从源码构建

需要 JDK 17、`zip`、Android SDK Platform 34 和 Android SDK Build-Tools。构建脚本已用 Build-Tools 30.0.3 验证。首次构建时创建本地签名密钥，然后从仓库根目录执行构建。

```bash
keytool -genkeypair -storetype PKCS12 -keystore hotel-local.p12 \
  -alias hotel-menu -keyalg RSA -keysize 2048 -validity 3650 \
  -dname 'CN=Android TV Hotel Menu Shortcut'

bash build.sh /path/to/Android/sdk 30.0.3 hotel-local.p12 hotel-menu
```

输出为 `build/Hotel-Menu-Shortcut.apk` 和对应的 `.idsig` 文件。覆盖升级已有安装时，Android 要求使用相同的签名密钥。
