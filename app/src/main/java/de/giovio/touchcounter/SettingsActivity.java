package de.giovio.touchcounter;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {

    public static final String KEY_PREF_CSV_DELIMITER = "pref_csvDelimiter";
    public static final String KEY_PREF_MIN_TIME_DIFF = "pref_minDataPointDiff";
    public static final String KEY_PREF_MAX_TIME_DIFF_PAUSE = "pref_maxDataPointDiffPause";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
