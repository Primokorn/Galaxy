package com.dragons.aurora.task;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.animation.AnimationUtils;

import com.dragons.aurora.AppListIteratorHelper;
import com.dragons.aurora.PlayStoreApiAuthenticator;
import com.dragons.aurora.R;
import com.dragons.aurora.adapters.RecyclerAppsAdapter;
import com.dragons.aurora.fragment.PreferenceFragment;
import com.dragons.aurora.model.App;
import com.dragons.aurora.playstoreapiv2.CategoryAppsIterator;
import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;
import com.dragons.aurora.playstoreapiv2.GooglePlayException;
import com.dragons.aurora.playstoreapiv2.IteratorGooglePlayException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.dragons.aurora.task.playstore.PlayStoreTask.noNetwork;

public class CategoryTaskHelper {

    private Context context;
    private RecyclerView recyclerView;
    private Disposable disposable;

    public CategoryTaskHelper(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
    }

    public void getCategoryApps(String categoryId, GooglePlayAPI.SUBCATEGORY subCategory) {
        disposable = Observable.fromCallable(() -> getApps(categoryId, subCategory))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((appList) -> {
                    if (!appList.isEmpty())
                        setupListView(recyclerView, appList);
                });
    }

    private List<App> getApps(String categoryId, GooglePlayAPI.SUBCATEGORY subCategory) throws IOException {
        List<App> apps = new ArrayList<>();
        AppListIteratorHelper iterator = new AppListIteratorHelper(new CategoryAppsIterator(
                new PlayStoreApiAuthenticator(context).getApi(), categoryId, subCategory));

        try {
            iterator.setGooglePlayApi(new PlayStoreApiAuthenticator(context).getApi());
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Building an api object from preferences failed");
        }

        if (!iterator.hasNext()) {
            return new ArrayList<>();
        }

        while (iterator.hasNext() && apps.isEmpty()) {
            try {
                apps.addAll(iterator.next());
            } catch (IteratorGooglePlayException e) {
                if (null == e.getCause()) {
                    continue;
                }
                if (noNetwork(e.getCause())) {
                    throw (IOException) e.getCause();
                } else if (e.getCause() instanceof GooglePlayException
                        && ((GooglePlayException) e.getCause()).getCode() == 401
                        && PreferenceFragment.getBoolean(context, PlayStoreApiAuthenticator.PREFERENCE_APP_PROVIDED_EMAIL)
                        ) {
                    PlayStoreApiAuthenticator authenticator = new PlayStoreApiAuthenticator(context);
                    authenticator.refreshToken();
                    iterator.setGooglePlayApi(authenticator.getApi());
                    apps.addAll(iterator.next());
                }
            }
        }
        return apps;
    }

    public void setupListView(RecyclerView recyclerView, List<App> appsToAdd) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(context, R.anim.layout_anim));
        recyclerView.setAdapter(new RecyclerAppsAdapter(context, appsToAdd));
    }
}
