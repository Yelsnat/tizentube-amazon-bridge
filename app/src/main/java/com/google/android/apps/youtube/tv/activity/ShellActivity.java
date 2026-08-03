package com.google.android.apps.youtube.tv.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.tv.R;

public final class ShellActivity extends Activity {
    private static final String COBALT_PACKAGE = "io.gh.reisxd.tizentube.cobalt";

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
        Intent outgoing = createForwardIntent(incoming);

        try {
            startActivity(outgoing);
        } catch (ActivityNotFoundException firstFailure) {
            Intent launcher = getPackageManager().getLeanbackLaunchIntentForPackage(COBALT_PACKAGE);
            if (launcher == null) {
                launcher = getPackageManager().getLaunchIntentForPackage(COBALT_PACKAGE);
            }

            if (launcher == null) {
                Toast.makeText(this, R.string.error_not_installed, Toast.LENGTH_LONG).show();
            } else {
                copyPayload(incoming, launcher);
                try {
                    startActivity(launcher);
                } catch (ActivityNotFoundException ignored) {
                    Toast.makeText(this, R.string.error_cannot_open, Toast.LENGTH_LONG).show();
                }
            }
        } finally {
            finish();
        }
    }

    private Intent createForwardIntent(Intent incoming) {
        String action = incoming != null ? incoming.getAction() : null;
        Uri data = incoming != null ? incoming.getData() : null;

        Intent outgoing;
        if (data != null) {
            outgoing = new Intent(action != null ? action : Intent.ACTION_VIEW, data);
        } else {
            outgoing = new Intent(action != null ? action : Intent.ACTION_MAIN);
        }

        outgoing.setPackage(COBALT_PACKAGE);
        copyPayload(incoming, outgoing);
        return outgoing;
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
