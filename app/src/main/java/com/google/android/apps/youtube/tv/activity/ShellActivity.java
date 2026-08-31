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
     * Returns the URI to forward to Cobalt: the incoming intent's own data if
     * present, otherwise a YouTube search results URL built from a voice/text
     * search query extra (e.g. from MEDIA_PLAY_FROM_SEARCH or SEARCH intents).
     */
    private Uri resolveData(Intent incoming) {
        if (incoming == null) {
            return null;
        }

        if (incoming.getData() != null) {
            return incoming.getData();
        }

        String query = incoming.getStringExtra(SearchManager.QUERY);
        if (query != null && !query.trim().isEmpty()) {
            return Uri.parse(YOUTUBE_SEARCH_URL + Uri.encode(query));
        }

        return null;
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
