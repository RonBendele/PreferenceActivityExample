package net.bendele.preferenceactivityexample;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

public class PrefsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {
    protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;

    /**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     *
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs() {
        if (Build.VERSION.SDK_INT >= 11 && mHasHeaders != null
                && mLoadHeaders != null) {
            try {
                return (Boolean) mHasHeaders.invoke(this);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle aSavedState) {
        if (Build.VERSION.SDK_INT >= 11) {
            // onBuildHeaders() will be called during super.onCreate()
            try {
                mLoadHeaders = getClass().getMethod("loadHeadersFromResource",
                        int.class, List.class);
                mHasHeaders = getClass().getMethod("hasHeaders");
            } catch (NoSuchMethodException e) {
            }
        }
        super.onCreate(aSavedState);
        // pre-v11 stuff goes here
        if (!isNewV11Prefs()) {
            addPreferencesFromResource(R.xml.app_prefs_tts_category);
            addPreferencesFromResource(R.xml.app_prefs_tts);
            addPreferencesFromResource(R.xml.app_prefs_intervals_category);
            addPreferencesFromResource(R.xml.app_prefs_intervals);
            addPreferencesFromResource(R.xml.app_prefs_vibrator_category);
            addPreferencesFromResource(R.xml.app_prefs_vibrator);
            addPreferencesFromResource(R.xml.app_prefs_additional_category);
            addPreferencesFromResource(R.xml.app_prefs_additional);
            CheckBoxPreference useTtsPreference = (CheckBoxPreference) findPreference(getString(R.string.keyUseTTS));
            setTtsSummary(useTtsPreference);
            CheckBoxPreference useIntervalPreference = (CheckBoxPreference) findPreference(getString(R.string.keyUseAnnounceInterval));
            useIntervalPreference.setDependency(getString(R.string.keyUseTTS));
            setIntervalsSummary(useIntervalPreference);
            // Initialize the summaries for each preference
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                initSummary(getPreferenceScreen().getPreference(i));
            }
        }
    }

    @Override
    public void onBuildHeaders(List<Header> aTarget) {
        try {
            mLoadHeaders.invoke(this, new Object[] { R.xml.pref_headers,
                    aTarget });
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    /*
     * If TTS is available on the device and the user previously selected to use
     * TTS, allow them to set the Announce Intervals Options. If they are not
     * available/selected then prevent the user from setting the Announce
     * Intervals Options and let them know why via the summary.
     */
    private static void setIntervalsSummary(CheckBoxPreference preference) {
        preference.setEnabled(MainApp.getTtsAvailable() && MainApp.getUseTTS());
        if (MainApp.getTtsAvailable() && MainApp.getUseTTS()) {
            preference.setSummaryOff(R.string.summaryOffUseAnnounceInterval);
            preference.setSummaryOn(R.string.summaryOnUseAnnounceInterval);
        } else if (!MainApp.getTtsAvailable()) {
            preference.setSummaryOff(R.string.summaryTTSNotAvailable);
            preference.setSummaryOn(R.string.summaryTTSNotAvailable);
        } else {
            preference.setSummaryOff(R.string.summaryOffUseTTS);
            preference.setSummaryOn(R.string.summaryOffUseTTS);
        }
    }

    /*
     * If TTS is available on the device, allow the user to choose what they
     * want to set in the Text-To-Speech Options. If TTS is not available
     * disable all the options & let the user know through the summary.
     */
    private static void setTtsSummary(CheckBoxPreference preference) {
        preference.setEnabled(MainApp.getTtsAvailable());
        if (preference.isEnabled()) {
            preference.setSummaryOff(R.string.summaryOffUseTTS);
            preference.setSummaryOn(R.string.summaryOnUseTTS);
        } else {
            preference.setSummaryOff(R.string.summaryTTSNotAvailable);
            preference.setSummaryOn(R.string.summaryTTSNotAvailable);
        }
    }

    /*
     * Recurse through each PreferenceCategory until we get to the leaf. At the
     * leaf, call updatePrefSummary().
     */
    private static void initSummary(Preference preference) {
        if (preference instanceof PreferenceCategory) {
            PreferenceCategory pCat = (PreferenceCategory) preference;
            for (int i = 0; i < pCat.getPreferenceCount(); i++) {
                initSummary(pCat.getPreference(i));
            }
        } else {
            updatePrefSummary(preference);
        }
    }

    private static void updatePrefSummary(Preference preference) {
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            preference.setSummary(listPref.getEntry());
        }
        if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) preference;
            preference.setSummary(editTextPref.getText());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        // any changes here must be duplicated in the Fragment as well.
        if (key.equals(getString(R.string.keyUseTTS))) {
            MainApp.setUseTTS(sharedPreferences.getBoolean(key, true));
        }
    }

    @TargetApi(11)
    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener for whenever a key changes
        if (Build.VERSION.SDK_INT < 11) {
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @TargetApi(11)
    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener for whenever a key changes
        if (Build.VERSION.SDK_INT < 11) {
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }
    }

    // v11 & greater stuff goes here
    @SuppressLint("NewApi")
    static public class PrefsFragment extends PreferenceFragment implements
            OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedState) {
            super.onCreate(savedState);
            Context context = getActivity().getApplicationContext();
            int thePrefRes = context.getResources().getIdentifier(
                    getArguments().getString("pref-resource"), "xml",
                    context.getPackageName());
            addPreferencesFromResource(thePrefRes);
            if (thePrefRes == R.xml.app_prefs_tts) {
                CheckBoxPreference useTtsPreference = (CheckBoxPreference) findPreference(getString(R.string.keyUseTTS));
                setTtsSummary(useTtsPreference);
            } else if (thePrefRes == R.xml.app_prefs_intervals) {
                CheckBoxPreference useIntervalPreference = (CheckBoxPreference) findPreference(getString(R.string.keyUseAnnounceInterval));
                setIntervalsSummary(useIntervalPreference);
            }
            // Initialize the summaries for each preference
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                initSummary(getPreferenceScreen().getPreference(i));
            }
        }

        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            // any changes here must be duplicated in the Activity as well.
            if (key.equals(getString(R.string.keyUseTTS))) {
                MainApp.setUseTTS(sharedPreferences.getBoolean(key, true));
            }
        }

        /*
         * Activity.onPause/onResume and Fragment.onPause/onResume are chained
         * together for v11 & greater.
         */

        @Override
        public void onPause() {
            super.onPause();
            // Unregister the listener for whenever a key changes
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            // Set up a listener for whenever a key changes
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }
    } // PrefsFragment

} // PrefsActivity