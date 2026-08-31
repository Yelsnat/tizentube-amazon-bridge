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
- Converts voice/text search queries and `youtube://search?query=...`-style
  deep links into a real `youtube.com` search URL, since Cobalt can only
  navigate to actual `http(s)` web addresses.
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

**Voice search initially didn't work.** Cobalt's Android glue code
(`CobaltActivity`, from the [Cobalt](https://cobalt.dev) runtime it's built
on) navigates directly to the forwarded intent's data as if it were a literal
web page address — it never reads Intent extras. Alexa's Fire TV voice search
sends a custom, non-`http(s)` deep link for this
(`youtube://search?query=cat+videos&isVoice=true`), which isn't a navigable
web address, so Cobalt's browser silently failed to load it and fell back to
its default page — confirmed by device logs showing the deep link correctly
reaching Cobalt's native layer but never resulting in an actual page
navigation, on both cold and warm app starts.

The fix: `ShellActivity` now recognizes `youtube:`/`vnd.youtube:` search deep
links (and Android TV's standard `SearchManager.QUERY` search-intent extra)
and translates the query into a real `https://www.youtube.com/results?search_query=`
URL before forwarding, since that's the only kind of address Cobalt can
actually load. It also targets Cobalt's launcher component directly
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
4. If the intent's URI is a search deep link (`youtube://search?query=...`)
   or the intent carries a search query extra instead of a URI, the bridge
   builds a real YouTube search URL from the query. Any other URI is
   forwarded as-is.
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

1. Install **Downloader by AFTVnews** from the Google Play Store on your
   Android TV, Google TV, or Fire TV device.
2. Allow Downloader to install unknown apps (**Settings > Apps > Special app
   access > Install unknown apps > Downloader**).
3. Open Downloader and enter the direct APK URL for your platform from the
   [latest release](https://github.com/Yelsnat/tizentube-amazon-bridge/releases/latest)
   (Downloader accepts a full URL, not just a numeric code):

   - Android TV / Google TV: `https://github.com/Yelsnat/tizentube-amazon-bridge/releases/latest/download/app-atv-debug.apk`
   - Fire TV / Fire Stick: `https://github.com/Yelsnat/tizentube-amazon-bridge/releases/latest/download/app-firetv-debug.apk`

4. Select **Go**, wait for the APK to download, then select **Install**.

To update, enter the same URL again and install over the existing app. Don't
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
