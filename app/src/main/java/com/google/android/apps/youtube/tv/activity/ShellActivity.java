package com.google.android.apps.youtube.tv.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.tv.R;

public final class ShellActivity extends Activity {
    private static final String COBALT_PACKAGE = "io.gh.reisxd.tizentube.cobalt";
    private static final String YOUTUBE_SEARCH_URL = "https://www.youtube.com/results?search_query=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forward(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        forward(intent);
    }

    private void forward(Intent incoming) {
        // Cobalt (the target app) only ever reads an incoming intent's data
        // URI; it does not look at extras such as SearchManager.QUERY. Target
        // its resolved component directly rather than just its package so
        // the intent is delivered regardless of Cobalt's own manifest
        // intent-filter declarations, which are not reliably matched.
        ComponentName target = resolveCobaltComponent();
        if (target == null) {
            Toast.makeText(this, R.string.error_not_installed, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Intent outgoing = createForwardIntent(incoming, target);
        try {
            startActivity(outgoing);
        } catch (ActivityNotFoundException ignored) {
            Toast.makeText(this, R.string.error_cannot_open, Toast.LENGTH_LONG).show();
        } finally {
            finish();
        }
    }

    private ComponentName resolveCobaltComponent() {
        Intent launcher = getPackageManager().getLeanbackLaunchIntentForPackage(COBALT_PACKAGE);
        if (launcher == null) {
            launcher = getPackageManager().getLaunchIntentForPackage(COBALT_PACKAGE);
        }
        return launcher != null ? launcher.getComponent() : null;
    }

    private Intent createForwardIntent(Intent incoming, ComponentName target) {
        String action = incoming != null ? incoming.getAction() : null;
        Uri data = resolveData(incoming);

        Intent outgoing = new Intent(action != null ? action : Intent.ACTION_VIEW);
        if (data != null) {
            outgoing.setData(data);
        }

        outgoing.setComponent(target);
        copyPayload(incoming, outgoing);
        return outgoing;
    }

    /**
     * Returns the URI to forward to Cobalt: a translated YouTube search URL
     * if the incoming intent's data or extras carry a search query, or the
     * incoming intent's own data otherwise.
     */
    private Uri resolveData(Intent incoming) {
        if (incoming == null) {
            return null;
        }

        Uri data = incoming.getData();
        if (data != null) {
            Uri searchUrl = searchUrlFromCustomSchemeUri(data);
            return searchUrl != null ? searchUrl : data;
        }

        return buildSearchUrl(incoming.getStringExtra(SearchManager.QUERY));
    }

    /**
     * Cobalt navigates directly to the forwarded intent's data as if it were
     * a literal web page address; it cannot load custom, non-http(s) schemes.
     * Alexa's Fire TV voice search sends exactly such a URI, e.g.
     * "youtube://search?query=cat+videos&isVoice=true", which Cobalt's
     * browser silently fails to load and falls back to its default page. If
     * the data URI carries a search query this way, translate it into a real
     * https://www.youtube.com search URL instead.
     */
    private Uri searchUrlFromCustomSchemeUri(Uri data) {
        String scheme = data.getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
            return null;
        }
        if (!"youtube".equals(scheme) && !"vnd.youtube".equals(scheme)
                && !"vnd.youtube.launch".equals(scheme)) {
            return null;
        }
        return buildSearchUrl(data.getQueryParameter("query"));
    }

    /**
     * Builds a YouTube search results URL from a raw query string. Android's
     * Uri does not translate '+' to a space when decoding query parameters
     * (that's an application/x-www-form-urlencoded convention, not part of
     * the URI spec), so a literal '+' from Alexa's query extra/URI is treated
     * as a word separator here before re-encoding.
     */
    private Uri buildSearchUrl(String query) {
        if (query == null) {
            return null;
        }
        query = query.replace('+', ' ').trim();
        if (query.isEmpty()) {
            return null;
        }
        return Uri.parse(YOUTUBE_SEARCH_URL + Uri.encode(query));
    }

    private void copyPayload(Intent source, Intent target) {
        if (source == null) {
            return;
        }

        if (source.getExtras() != null) {
            target.putExtras(source.getExtras());
        }
        if (source.getClipData() != null) {
            target.setClipData(source.getClipData());
        }
        if (source.getType() != null) {
            target.setDataAndType(target.getData(), source.getType());
        }

        target.addFlags(source.getFlags() & (
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}
