package com.xtjun.xpForwardSms.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.xtjun.xposed.forwardSms.R;
import com.xtjun.xpForwardSms.common.utils.PackageUtils;
import com.xtjun.xpForwardSms.ui.app.base.BaseActivity;


/**
 * 主界面
 */
public class HomeActivity extends BaseActivity {
    Toolbar mToolbar;

    private Fragment mCurrentFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        getExternalFilesDir("");

        handleIntent(getIntent());

        // setup toolbar
        mToolbar = findViewById(R.id.toolbar);
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        refreshActionBar(getString(R.string.app_name));
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        SettingsFragment settingsFragment = null;
        if (Intent.ACTION_VIEW.equals(action)) {
            String extraAction = intent.getStringExtra(SettingsFragment.EXTRA_ACTION);
            if (SettingsFragment.ACTION_GET_RED_PACKET.equals(extraAction)) {
                settingsFragment = SettingsFragment.newInstance(extraAction);
            }
        }

        if (settingsFragment == null) {
            settingsFragment = SettingsFragment.newInstance();
        }

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.home_content, settingsFragment)
                .commit();
        mCurrentFragment = settingsFragment;
    }

    private void refreshActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(!(mCurrentFragment instanceof SettingsFragment));
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            mFragmentManager.popBackStackImmediate();
            mCurrentFragment = mFragmentManager.findFragmentById(R.id.home_content);
            refreshActionBar(getString(R.string.app_name));
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_taichi_users_notice) {
            onTaichiUsersNoticeSelected();
            return true;
        } else if (id == R.id.action_edxposed_users_notice) {
            onEdxposedUsersNoticeSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }


    void onTaichiUsersNoticeSelected() {
        new MaterialDialog.Builder(this)
                .title(R.string.taichi_users_notice)
                .content(R.string.taichi_users_notice_content)
                .negativeText(R.string.add_apps_in_taichi)
                .onNegative((dialog, which) -> PackageUtils.startAddAppsInTaiChi(HomeActivity.this))
                .positiveText(R.string.check_module_in_taichi)
                .onPositive((dialog, which) -> PackageUtils.startCheckModuleInTaiChi(HomeActivity.this))
                .show();
    }

    void onEdxposedUsersNoticeSelected() {
        new MaterialDialog.Builder(this)
                .title(R.string.edxposed_users_notice)
                .content(R.string.edxposed_users_notice_content)
                .positiveText(R.string.i_know)
                .show();
    }
}
