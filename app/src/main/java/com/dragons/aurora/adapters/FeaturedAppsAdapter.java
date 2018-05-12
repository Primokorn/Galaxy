package com.dragons.aurora.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dragons.aurora.R;
import com.dragons.aurora.activities.AuroraActivity;
import com.dragons.aurora.activities.DetailsActivity;
import com.dragons.aurora.fragment.details.ButtonDownload;
import com.dragons.aurora.fragment.details.ButtonUninstall;
import com.dragons.aurora.fragment.details.DownloadOptions;
import com.dragons.aurora.model.App;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FeaturedAppsAdapter extends RecyclerView.Adapter<FeaturedAppsAdapter.ViewHolder> {

    private List<App> appsToAdd;
    private Context context;

    public FeaturedAppsAdapter(Context context, List<App> appsToAdd) {
        this.context = context;
        this.appsToAdd = appsToAdd;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.features_apps_adapter, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final App app = appsToAdd.get(position);

        holder.appName.setText(app.getDisplayName());
        holder.appRatingBar.setRating(app.getRating().getStars(1));
        setText(holder.view, holder.appRating, R.string.details_rating, app.getRating().getAverage());

        holder.appCard.setOnClickListener(v -> context
                .startActivity(DetailsActivity.getDetailsIntent(context, app.getPackageName())));

        if (app.getPageBackgroundImage() != null)
            drawBackground(app, holder);

        drawIcon(app, holder);

        holder.appMenu3Dot.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.menu_download);
            new DownloadOptions((AuroraActivity) context, app).inflate(popup.getMenu());
            popup.getMenu().findItem(R.id.action_download).setVisible(new ButtonDownload((AuroraActivity) context, app).shouldBeVisible());
            popup.getMenu().findItem(R.id.action_uninstall).setVisible(app.isInstalled());
            popup.getMenu().findItem(R.id.action_manual).setVisible(app.isInstalled());
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_ignore:
                    case R.id.action_whitelist:
                    case R.id.action_unignore:
                    case R.id.action_unwhitelist:
                    case R.id.action_download:
                        new ButtonDownload((AuroraActivity) context, app).checkAndDownload();
                        break;
                    case R.id.action_uninstall:
                        new ButtonUninstall((AuroraActivity) context, app).uninstall();
                        break;
                    default:
                        return new DownloadOptions((AuroraActivity) context, app).onContextItemSelected(item);
                }
                return false;
            });
            popup.show();
        });
    }

    private void drawBackground(App app, ViewHolder holder) {
        Picasso.with(context)
                .load(app.getPageBackgroundImage().getUrl())
                .placeholder(R.color.transparent)
                .into(holder.appBackground, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) holder.appBackground.getDrawable()).getBitmap();
                        if (bitmap != null)
                            getPalette(bitmap, holder);
                    }

                    @Override
                    public void onError() {
                        holder.appName.setTextColor(Color.WHITE);
                        holder.appData.setBackgroundColor(Color.DKGRAY);
                    }
                });
    }

    private void drawIcon(App app, ViewHolder holder) {
        Picasso.with(context)
                .load(app.getIconInfo().getUrl())
                .placeholder(R.color.transparent)
                .into(holder.appIcon);
    }

    private void getPalette(Bitmap bitmap, ViewHolder holder) {
        Palette.from(bitmap)
                .generate(palette -> {
                    Palette.Swatch mySwatch = palette.getDarkVibrantSwatch();
                    if (mySwatch != null) {
                        paintEmAll(palette.getDarkVibrantColor(Color.DKGRAY), holder);
                    }
                });
    }

    private void paintEmAll(int color, ViewHolder holder) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{ColorUtils.setAlphaComponent(color, 150), 0x05000000});
        gradientDrawable.setCornerRadius(0f);
        holder.appData.setBackground(gradientDrawable);
    }

    protected void setText(TextView textView, String text) {
        if (null != textView)
            textView.setText(text);
    }

    protected void setText(View v, TextView textView, int stringId, Object... text) {
        setText(textView, v.getResources().getString(stringId, text));
    }

    @Override
    public int getItemCount() {
        return appsToAdd.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private RelativeLayout appCard;
        private RelativeLayout appData;
        private TextView appName;
        private TextView appRating;
        private RatingBar appRatingBar;
        private ImageView appIcon;
        private ImageView appBackground;
        private ImageView appMenu3Dot;


        ViewHolder(View view) {
            super(view);
            this.view = view;
            appCard = view.findViewById(R.id.app_container);
            appData = view.findViewById(R.id.app_data);
            appName = view.findViewById(R.id.app_name);
            appRating = view.findViewById(R.id.app_rating);
            appRatingBar = view.findViewById(R.id.app_ratingbar);
            appIcon = view.findViewById(R.id.app_icon);
            appBackground = view.findViewById(R.id.app_background);
            appMenu3Dot = view.findViewById(R.id.app_menu3dot);
        }
    }
}