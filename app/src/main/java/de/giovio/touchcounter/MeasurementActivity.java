package de.giovio.touchcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MeasurementActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY_NAME = "REPLY_NEW_MEASUREMENT_NAME";
    public static final String EXTRA_REPLY_STARTTIME = "REPLY_NEW_MEASUREMENT_STARTTIME";
    public static final String EXTRA_REPLY_POINTS = "REPLY_NEW_MEASUREMENT_POINTS";

    private ArrayList<DataPoint> dataPoints;
    private static final String BUNDLE_NAME_DATAPOINTS = "DataPoints";
    private long startTime;
    private static final String BUNDLE_NAME_STARTTIME = "StartTime";

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putParcelableArrayList(BUNDLE_NAME_DATAPOINTS, dataPoints);
        savedInstanceState.putLong(BUNDLE_NAME_STARTTIME, startTime);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);

        dataPoints = new ArrayList<>();
        startTime = -1;

        if (savedInstanceState != null) {
            ArrayList<DataPoint> oldDataPoints = savedInstanceState.getParcelableArrayList(BUNDLE_NAME_DATAPOINTS);
            if (oldDataPoints != null && oldDataPoints.size() > 0) {
                dataPoints = oldDataPoints;
                startTime = savedInstanceState.getLong(BUNDLE_NAME_STARTTIME);
                Log.i("MeasurementActivity", "Restored " + dataPoints.size() + " points from saved instance.");
            }
        }

        updateNumDataPointsCollected();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Store values between instances here
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();  // Put the values from the UI

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<DataPoint>>() {}.getType();
        String json = gson.toJson(dataPoints, listType);

        editor.putString(BUNDLE_NAME_DATAPOINTS, json);
        editor.putLong(BUNDLE_NAME_STARTTIME, startTime);
        editor.apply();

        Log.i("MeasurementActivity", "Stored " + dataPoints.size() + " points to shared prefs.");
    }

    @Override
    public void onResume(){
        super.onResume();

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String jsonPoints = preferences.getString(BUNDLE_NAME_DATAPOINTS, null);
        long savedStartTime = preferences.getLong(BUNDLE_NAME_STARTTIME, -1);
        if (jsonPoints != null && savedStartTime != -1) {
            Type listType = new TypeToken<ArrayList<DataPoint>>() {}.getType();
            Gson gson = new Gson();
            List<DataPoint> savedDataPoints = gson.fromJson(jsonPoints, listType);
            dataPoints = (ArrayList<DataPoint>) savedDataPoints;
            startTime = savedStartTime;
            Log.i("MeasurementActivity", "Resumed " + dataPoints.size() + " points from shared prefs.");
        }

        updateNumDataPointsCollected();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        if (savedInstanceState != null) {
            ArrayList<DataPoint> oldDataPoints = savedInstanceState.getParcelableArrayList(BUNDLE_NAME_DATAPOINTS);
            if (oldDataPoints != null && oldDataPoints.size() > 0) {
                dataPoints = oldDataPoints;
                startTime = savedInstanceState.getLong(BUNDLE_NAME_STARTTIME);
                Log.i("MeasurementActivity", "Restored " + dataPoints.size() + " points from saved instance.");
            }
        }

        updateNumDataPointsCollected();
    }

    @Override
    public void onBackPressed() {
        if (dataPoints.size() == 0) {
            Intent replyIntent = new Intent();
            setResult(RESULT_CANCELED, replyIntent);
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.activity_measurement_dialog_cancel_title))
                    .setMessage(getString(R.string.activity_measurement_dialog_cancel_text))
                    .setPositiveButton(getString(R.string.activity_measurement_dialog_cancel_yes), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton(getString(R.string.activity_measurement_dialog_cancel_no), null)
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    private void updateNumDataPointsCollected() {
        final TextView textView = findViewById(R.id.textNumDataPointsValue);
        textView.setText(String.valueOf(dataPoints.size()));

        // Avg BPM
        long avgBpm = 0;
        if (dataPoints.size() >= 2) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            final String minDiffOptionString = sharedPref.getString(SettingsActivity.KEY_PREF_MIN_TIME_DIFF, "50");
            final long minDiff = Long.parseLong(minDiffOptionString);

            final String maxDiffOptionString = sharedPref.getString(SettingsActivity.KEY_PREF_MAX_TIME_DIFF_PAUSE, "1500");
            final long maxDiff = Long.parseLong(maxDiffOptionString);

            long duration = 0;
            int count = 0;
            long lastTimestamp = dataPoints.get(dataPoints.size() - 1).getTime();
            for (int i = dataPoints.size() - 2; i >= 0; --i) {
                final long currentTimestamp = dataPoints.get(i).getTime();
                final long diff = lastTimestamp - currentTimestamp;
                if (diff <= minDiff) {
                    continue;
                } else if (diff >= maxDiff) {
                    break;
                } else {
                    duration += diff;
                    ++count;
                    lastTimestamp = currentTimestamp;

                    if (count >= 5) {
                        break;
                    }
                }
            }

            if ((count > 0) && (duration > 0)) {
                avgBpm = 60000 / (duration / count);
            }
        }
        final TextView textViewAvgBpm = findViewById(R.id.textAvgBpmValue);
        textViewAvgBpm.setText(String.valueOf(avgBpm));
    }

    public void btnMeasureOnClick(View v) {
        if (dataPoints.size() == 0) {
            startTime = System.currentTimeMillis();
        }

        dataPoints.add(new DataPoint());
        updateNumDataPointsCollected();
    }

    private String serializeDataPoints() {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (DataPoint dp: dataPoints) {
            if (!isFirst) {
                sb.append("#");
            }
            sb.append(dp.getTime());

            isFirst = false;
        }
        return sb.toString();
    }

    public void btnSaveOnClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.activity_measurement_dialog_save_title));
        builder.setMessage(R.string.activity_measurement_dialog_save_text);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.activity_measurement_dialog_save_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getApplicationContext(), R.string.activity_measurement_dialog_save_nameNotEmpty, Toast.LENGTH_LONG).show();
                } else if (dataPoints.size() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.activity_measurement_dialog_save_pointsNotEmpty, Toast.LENGTH_LONG).show();
                } else {
                    final String startTimeString = String.valueOf(startTime);
                    final String serializedPoints = serializeDataPoints();

                    Intent replyIntent = new Intent();
                    replyIntent.putExtra(EXTRA_REPLY_NAME, name);
                    replyIntent.putExtra(EXTRA_REPLY_STARTTIME, startTimeString);
                    replyIntent.putExtra(EXTRA_REPLY_POINTS, serializedPoints);
                    setResult(RESULT_OK, replyIntent);

                    // Reset points
                    dataPoints = new ArrayList<>();

                    finish();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.activity_measurement_dialog_save_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void btnCancelOnClick(View v) {
        if (dataPoints.size() == 0) {
            Intent replyIntent = new Intent();
            setResult(RESULT_CANCELED, replyIntent);
            finish();
        } else {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            Intent replyIntent = new Intent();
                            setResult(RESULT_CANCELED, replyIntent);

                            // Reset points
                            dataPoints = new ArrayList<>();

                            finish();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.activity_measurement_dialog_cancel_title))
                    .setMessage(getString(R.string.activity_measurement_dialog_cancel_text))
                    .setPositiveButton(getString(R.string.activity_measurement_dialog_cancel_yes), dialogClickListener)
                    .setNegativeButton(getString(R.string.activity_measurement_dialog_cancel_no), dialogClickListener).show();
        }
    }
}
