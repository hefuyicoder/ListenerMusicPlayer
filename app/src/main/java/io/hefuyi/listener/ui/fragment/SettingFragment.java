package io.hefuyi.listener.ui.fragment;


import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.ATEColorPreference;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

import io.hefuyi.listener.R;
import io.hefuyi.listener.ui.activity.SettingActivity;
import io.hefuyi.listener.util.PreferencesUtility;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private String mAteKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        PreferencesUtility.getInstance(getActivity()).setOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        invalidateSettings();
        ATE.apply(view, mAteKey);
    }

    public void invalidateSettings() {
        mAteKey = ((SettingActivity) getActivity()).getATEKey();

        ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
        primaryColorPref.setColor(Config.primaryColor(getActivity(), mAteKey), Color.BLACK);
        primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ColorChooserDialog.Builder((SettingActivity) getActivity(), R.string.primary_color)
                        .preselect(Config.primaryColor(getActivity(), mAteKey))
                        .show();
                return true;
            }
        });

        findPreference("dark_theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Marks both theme configs as changed so MainActivity restarts itself on return
                Config.markChanged(getActivity(), "light_theme");
                Config.markChanged(getActivity(), "dark_theme");
                // The dark_theme preference value gets saved by Android in the default PreferenceManager.
                // It's used in getATEKey() of both the Activities.
                getActivity().recreate();
                return true;
            }
        });

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
