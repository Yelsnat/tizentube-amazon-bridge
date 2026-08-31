# TizenTube Bridge for Android TV and Fire TV

TizenTube Bridge is a small compatibility app that takes the place of an
official YouTube TV package and forwards YouTube launch requests to
[TizenTube Cobalt](https://github.com/reisxd/TizenTubeCobalt).

It is built as two product flavors from the same source:

- **`atv`** — for Android TV and Google TV devices such as Chromecast with
  Google TV. Uses the Google TV YouTube package ID,
  `com.google.android.youtube.tv`.
- **`firetv`** — for Amazon Fire TV devices. Uses the Fire OS YouTube package
  ID, `com.amazon.firetv.youtube`, so that Alexa voice commands (e.g. "open
  YouTube") resolve to this bridge instead of the uninstalled Amazon-catalog
  YouTube app. It also declares the
  `com.amazon.permission.media.session.voicecommandcontrol` permission that
  the real Amazon YouTube app uses to hook into Alexa media/voice control.

Both flavors share the same `ShellActivity` forwarding logic, which is
package-agnostic. The bridge contains no video player, YouTube client, account
login, tracking, analytics, advertising, or network client.

## Features

- Uses the target platform's YouTube package ID (`com.google.android.youtube.tv`
  for Android/Google TV, `com.amazon.firetv.youtube` for Fire TV).
- Opens TizenTube Cobalt when the YouTube app tile or a dedicated YouTube remote
  button is selected.
- Forwards YouTube website links and supported YouTube URI schemes.
- Forwards video requests originating from Google TV home-screen cards when the
  launcher targets the YouTube package.
- Accepts common Android TV media, search, voice-command, and Google search
  intents and forwards their data and extras to Cobalt.
- Preserves URI permission flags, MIME type, clip data, and extras where
  possible.
- Falls back to Cobalt's main TV activity if Cobalt does not expose an activity
  for the incoming deep link.
- Shows an English error message if Cobalt is missing or cannot be opened.
- Keeps regular user-facing bridge labels transparent by referring to YouTube.
  Error messages name TizenTube Cobalt so missing dependencies can be diagnosed
  and installed.
- Supports Android 5.0 (API 21) and later. Current TizenTube Cobalt releases may
  require a newer Android version.

## What it cannot do

- It cannot coexist with the official YouTube TV app because both use the same
  package ID.
- It does not modify, patch, bundle, or install TizenTube Cobalt.
- It cannot make Cobalt support a deep link that Cobalt itself does not
  understand. In that case it can only open Cobalt's home screen.
- It does not provide a local search-result database and does not fetch videos,
  channels, titles, or thumbnails. Google TV may independently show server-side
  YouTube results and send their playback intents to the YouTube package. The
  bridge can forward those intents but cannot control which results Google
  displays.
- It does not create or refresh Android TV home-screen recommendation channels,
  personalized feeds, or **Watch Next** entries.
- It cannot read Cobalt's signed-in session, history, subscriptions,
  recommendations, or playback state. Android isolates the two apps.
- It cannot restore recommendations removed together with the official YouTube
  app. It only forwards cards that already target the YouTube package.
- It does not provide casting, DIAL, notifications, background playback, or an
  update service.
- It cannot bypass device policies, package-signature checks, or restrictions
  imposed by Google TV, the launcher, or the firmware.

## Comparison with SmartTube's ATV Bridge

This comparison is based on static inspection of the official
`ATV_SYTV_Bridge.apk` version 3.4 (version code 44), downloaded from SmartTube's
official GitHub release URL.

| Capability | TizenTube Bridge | SmartTube ATV Bridge 3.4 |
| --- | --- | --- |
| Uses `com.google.android.youtube.tv` | Yes | Yes |
| Launcher and Leanback launcher entry | Yes | Yes |
| Opens from a YouTube remote button | Yes | Yes |
| Forwards HTTP(S) YouTube links | Yes | Yes |
| Forwards `youtube://` requests | Yes | Yes, for `search` and `play` hosts |
| Forwards `vnd.youtube:` requests | Yes | Not declared in its manifest |
| Handles `MEDIA_PLAY_FROM_SEARCH` | Yes, including requests without a URI | Declared together with its URI-constrained filter |
| Handles additional generic search/voice actions | Yes | Not declared in its manifest |
| Local global-search content provider | No | No provider declared |
| Android TV recommendation-channel provider | No | No provider declared |
| Creates new personalized home recommendations | No | No |
| Forwards existing home-card intents | Yes | Yes, when addressed to YouTube |
| Copies incoming intent data and extras | Yes | Yes |
| Fallback to target app's launcher activity | Yes | No; it tries fixed SmartTube activities |
| Target applications | TizenTube Cobalt | SmartTube stable, beta, F-Droid, and two legacy packages |
| Missing-target feedback | Toast | Full-screen error view |
| Target-specific bridge extras | No | Yes: `hide_tips` and `bridge_package_name` |
| Minimum Android version | Android 5.0 / API 21 | Android 4.2 / API 17 |


## How it works

1. Android sees this app as `com.google.android.youtube.tv`.
2. A launcher, remote button, voice action, recommendation card, or another app
   sends an intent to the YouTube package.
3. `ShellActivity` receives the intent and creates an equivalent intent limited
   to `io.gh.reisxd.tizentube.cobalt`.
4. The original URI, extras, clip data, MIME type, and relevant flags are copied.
5. If Cobalt accepts that intent, Android opens the requested page or video.
6. If no Cobalt component accepts it, the bridge opens Cobalt's Leanback/main
   launcher activity instead.

On supported Google TV launchers, Google may supply concrete YouTube search
results from its own servers because the YouTube package ID is installed. Those
results are not generated by this bridge. When a selected result targets
`com.google.android.youtube.tv`, the normal forwarding flow opens it in Cobalt.
This behavior depends on Google's launcher and backend and is not guaranteed.

## Requirements

- An Android TV, Google TV, or Fire TV device. Install the `atv` flavor APK on
  Android/Google TV and the `firetv` flavor APK on Fire TV devices.
- TizenTube Cobalt installed with package ID
  `io.gh.reisxd.tizentube.cobalt`.
- Permission to uninstall or disable the official YouTube TV package and install
  APKs from outside Google Play.
- ADB, Downloader, or another trusted sideloading method.

## Installation

Install TizenTube Cobalt first and verify that it opens normally.

The official YouTube TV app and this bridge cannot be installed together. On
devices where YouTube is uninstallable, uninstall it. If YouTube cannot be uninstalled then this app is not compatible with your device.

### Install with Downloader by AFTVnews

1. Install **Downloader by AFTVnews** from the Google Play Store on your Android
   TV or Google TV device.
2. Allow Downloader to install unknown apps when Android asks. This setting is
   usually located under **Settings > Apps > Special app access > Install
   unknown apps > Downloader**.
3. Open Downloader and enter this numeric code:

   ```text
   8792210
   ```

4. Select **Go**, wait for `tizentube-bridge.apk` to download, and select
   **Install**.
5. After installation, you may delete the downloaded APK from Downloader.

To update the bridge, enter the same Downloader code again and install the new
APK over the existing app. Do not uninstall the bridge before a normal update.

If Android reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, a differently signed
package with the YouTube package ID is still registered on the device. Some
system images do not allow replacing their built-in YouTube package without
root access or firmware changes.


## Privacy and security

The bridge does not request internet access and does not make network requests.
It only receives Android intents and starts TizenTube Cobalt. Incoming intent
data and search extras are passed to Cobalt; handling them is then Cobalt's
responsibility.

Only install APKs from releases you trust.

## License

Copyright (C) 2026 TizenTube Bridge contributors.

This project is licensed under the GNU General Public License version 3.0 only
(`GPL-3.0-only`). See [LICENSE](LICENSE) for the complete license text.
