package com.xtjun.xpForwardSms.ui.home;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.xtjun.xposed.forwardSms.BuildConfig;
import com.xtjun.xpForwardSms.common.constant.Const;
import com.xtjun.xpForwardSms.common.utils.ModuleUtils;
import com.xtjun.xpForwardSms.common.utils.PackageUtils;
import com.xtjun.xpForwardSms.common.utils.StorageUtils;
import com.xtjun.xpForwardSms.common.utils.Utils;
import com.xtjun.xpForwardSms.data.http.entity.ApkVersion;
import com.xtjun.xpForwardSms.data.repository.DataRepository;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.xtjun.xpForwardSms.ui.home.SettingsFragment.ACTION_GET_RED_PACKET;
import static com.xtjun.xpForwardSms.ui.home.SettingsFragment.EXTRA_ACTION;

public class SettingsPresenter implements SettingsContract.Presenter {

    private SettingsContract.View mView;
    private Context mContext;

    private CompositeDisposable mCompositeDisposable;

    @Inject
    SettingsPresenter() {
        mCompositeDisposable = new CompositeDisposable();
    }

    @Inject
    @Override
    public void onAttach(Context context, SettingsContract.View view) {
        mContext = context;
        mView = view;
    }

    @Override
    public void onDetach() {
        mView = null;
        if (mCompositeDisposable.size() > 0) {
            mCompositeDisposable.clear();
        }
    }

    @Override
    public void handleArguments(Bundle args) {
        if (args == null) {
            return;
        }
        String extraAction = args.getString(EXTRA_ACTION);
        if (ACTION_GET_RED_PACKET.equals(extraAction)) {
            args.remove(EXTRA_ACTION);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                mView.updateUIByModuleStatus(ModuleUtils.isModuleEnabled());
            }, 50L);
        }
    }

    @Override
    public void setPreferenceWorldWritable(String preferencesName) {
        // dataDir: /data/data/<package_name>/
        // spDir: /data/data/<package_name>/shared_prefs/
        // spFile: /data/data/<package_name>/shared_prefs/<preferences_name>.xml
        File prefsFile = StorageUtils.getSharedPreferencesFile(mContext, preferencesName);
        StorageUtils.setFileWorldWritable(prefsFile, 2);
    }

    @Override
    public void hideOrShowLauncherIcon(boolean hide) {
        PackageManager pm = mContext.getPackageManager();
        ComponentName launcherCN = new ComponentName(mContext, Const.HOME_ACTIVITY_ALIAS);
        int state = hide ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        if (pm.getComponentEnabledSetting(launcherCN) != state) {
            pm.setComponentEnabledSetting(launcherCN, state, PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    public void showSourceProject() {
        Utils.showWebPage(mContext, Const.PROJECT_SOURCE_CODE_URL);
    }

    @Override
    public void setInternalFilesWritable() {
        // dataDir or external dataDir
        // filesDir or external filesDir
        StorageUtils.setFileWorldWritable(StorageUtils.getFilesDir(), 1);
    }

    @Override
    public void checkUpdate() {
        try {
            Disposable disposable = DataRepository.getLatestVersion()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(latestVersion -> {
                                ApkVersion currentVersion = new ApkVersion(BuildConfig.VERSION_NAME, "");
                                if (currentVersion.getVersionValue() < latestVersion.getVersionValue()) {
                                    mView.showUpdateDialog(latestVersion);
                                } else {
                                    mView.showAppAlreadyNewest();
                                }
                            },
                            throwable -> mView.showCheckError(throwable)
                    );
            mCompositeDisposable.add(disposable);
        } catch (Throwable e) {
            Log.e("checkUpdate", "Fail and throw Err: ", e);
        }
    }

    @Override
    public void updateFromGithub() {
        Utils.showWebPage(mContext, Const.PROJECT_GITHUB_LATEST_RELEASE_URL);
    }

    @Override
    public void updateFromCoolApk() {
        PackageUtils.showAppDetailsInCoolApk(mContext);
    }
}
