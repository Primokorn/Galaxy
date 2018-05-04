package com.dragons.aurora.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;

import com.dragons.aurora.R;
import com.dragons.aurora.activities.AuroraActivity;
import com.dragons.aurora.activities.DetailsActivity;
import com.dragons.aurora.fragment.details.ButtonDownload;
import com.dragons.aurora.notification.CancelDownloadService;

public class UpdatableAppBadge extends AppBadge {

    android.widget.Button updateSingle, cancelSingle;

    @Override
    public void draw() {
        updateSingle = view.findViewById(R.id.update_single);
        cancelSingle = view.findViewById(R.id.cancel_single);
        line2.clear();
        line3.clear();
        Context c = view.getContext();
        String updated = app.getUpdated();
        if (!TextUtils.isEmpty(updated)) {
            line2.add(Formatter.formatShortFileSize(c, app.getSize()));
            line3.add(c.getString(R.string.list_line_2_updatable, updated));
        }
        if (app.isSystem()) {
            line3.add(c.getString(R.string.list_app_system));
        }
        drawButtons();
        super.draw();
    }

    private void drawButtons() {
        final ButtonDownload buttonDownload = new ButtonDownload((AuroraActivity) view.getContext(), app);
        updateSingle.setVisibility(View.VISIBLE);
        updateSingle.setEnabled(true);
        updateSingle.setOnClickListener(v -> {
            DetailsActivity.app = app;
            buttonDownload.checkAndDownload();
            updateSingle.setEnabled(false);
            drawCancel();
        });
    }

    private void drawCancel() {
        updateSingle.setVisibility(View.GONE);
        cancelSingle.setVisibility(View.VISIBLE);
        cancelSingle.setOnClickListener(v -> {
            view.getContext().startService(
                    new Intent(view.getContext().getApplicationContext(),
                            CancelDownloadService.class)
                            .putExtra(CancelDownloadService.PACKAGE_NAME, app.getPackageName()));
            drawButtons();
        });
    }
}
