# TizenTube Bridge for Android TV and Fire TV

TizenTube Bridge is a small compatibility app that takes the place of the
official YouTube TV app and forwards YouTube launch requests to
[TizenTube Cobalt](https://github.com/reisxd/TizenTubeCobalt). It contains no
video player, YouTube client, account login, tracking, analytics, advertising,
or network client — it only forwards Android intents.

It's built as two flavors from the same codebase:

- **`atv`** — for Android TV / Google TV. Uses the package ID
  `com.google.android.youtube.tv`.
- **`firetv`** — for Amazon Fire TV / Fire Stick. Uses the package ID
  `com.amazon.firetv.youtube`.

## Features

- Opens TizenTube Cobalt when the YouTube tile, a remote's YouTube button, a
  YouTube link, or a voice command (including Alexa on Fire TV) is used.
- Forwards YouTube website links and `youtube:`/`vnd.youtube:` URI schemes.
- Converts voice/text search queries into a YouTube search URL, since Cobalt
  only understands URIs, not raw search extras.
- Delivers intents directly to Cobalt's resolved launcher component, which
  doesn't depend on Cobalt's own manifest declaring matching intent-filters.
- Shows a Toast naming TizenTube Cobalt if it's missing or can't be opened.
- Supports Android 5.0 (API 21) and later.

## Fire TV / Fire Stick support

The original bridge, built by [TobiPeterG](https://github.com/TobiPeterG), only
hijacks the Google TV YouTube package. Fire OS ships a separate YouTube app
under a different package (`com.amazon.firetv.youtube`), so Alexa's "open
YouTube" resolved to that missing app instead of the bridge.

Fire TV support adds a second `firetv` product flavor that hijacks that
package instead, plus the `com.amazon.permission.media.session.voicecommandcontrol`
permission the real Amazon YouTube app uses for Alexa media/voice control.
Both flavors share the same `ShellActivity` forwarding logic unchanged.

**Voice search initially didn't work** because Alexa's (and Android TV's)
voice-search intents carry the spoken query as an Intent extra
(`SearchManager.QUERY`), not a URI. Cobalt's Android glue code
(`CobaltActivity`, from the [Cobalt](https://cobalt.dev) runtime it's built
on) only ever reads an intent's data URI and ignores extras entirely, so the
query was silently dropped and Cobalt just opened its home screen. This is
fixed by converting the query into a `youtube.com/results?search_query=`
URL before forwarding, and by targeting Cobalt's launcher component directly
(bypassing intent-filter matching) to work around a
[known Cobalt manifest bug](https://github.com/reisxd/TizenTubeCobalt/issues/129).

## What it cannot do

- Cannot coexist with the official YouTube TV app (same package ID).
- Does not modify, patch, bundle, or install TizenTube Cobalt.
- Cannot make Cobalt support a deep link it doesn't understand — it can only
  open Cobalt's home screen in that case.
- Does not fetch videos, channels, titles, or thumbnails, or provide search
  results itself; it only forwards intents it's given.
- Cannot read Cobalt's signed-in session, history, or recommendations —
  Android isolates the two apps.
- Does not provide casting, DIAL, notifications, or background playback.

## How it works

1. Android sees this app as the YouTube package for its platform
   (`com.google.android.youtube.tv` or `com.amazon.firetv.youtube`).
2. A launcher, remote button, voice action, or another app sends an intent to
   that package.
3. `ShellActivity` resolves Cobalt's launcher activity component and forwards
   the intent to it directly.
4. If the intent already carries a URI, that URI is forwarded as-is. If it
   instead carries a search query extra, the bridge builds a YouTube search
   URL from it.
5. Extras, clip data, MIME type, and relevant flags are copied alongside the
   URI for compatibility with anything Cobalt may read.

## Requirements

- An Android TV, Google TV, or Fire TV device. Install the `atv` flavor on
  Android/Google TV and the `firetv` flavor on Fire TV.
- TizenTube Cobalt installed with package ID `io.gh.reisxd.tizentube.cobalt`.
- Permission to uninstall/disable the official YouTube TV app and sideload
  APKs.

## Installation

Install TizenTube Cobalt first and verify that it opens normally.

The official YouTube TV app and this bridge cannot be installed together. On
devices where YouTube is uninstallable, uninstall it. If YouTube cannot be
uninstalled, this app is not compatible with your device.

### Install with Downloader by AFTVnews

1. Install **Downloader by AFTVnews** from the Google Play Store.
2. Allow Downloader to install unknown apps (**Settings > Apps > Special app
   access > Install unknown apps > Downloader**).
3. Open Downloader and enter this code:

   ```text
   8792210
   ```

4. Select **Go**, wait for the APK to download, then select **Install**.

To update, enter the same code again and install over the existing app. Don't
uninstall the bridge before an update.

If Android reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, a differently signed
package with the same package ID is still registered on the device. Some
system images don't allow replacing their built-in YouTube package without
root access or firmware changes.

## Privacy and security

The bridge does not request internet access and makes no network requests. It
only receives Android intents and starts TizenTube Cobalt; handling that data
is then Cobalt's responsibility.

Only install APKs from releases you trust.

## License

Copyright (C) 2026 TizenTube Bridge contributors.

This project is licensed under the GNU General Public License version 3.0 only
(`GPL-3.0-only`). See [LICENSE](LICENSE) for the complete license text.
