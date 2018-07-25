package de.giovio.touchcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MeasurementActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY_NAME = "REPLY_NEW_MEASUREMENT_NAME";
    public static final String EXTRA_REPLY_STARTTIME = "REPLY_NEW_MEASUREMENT_STARTTIME";
    public static final String EXTRA_REPLY_POINTS = "REPLY_NEW_MEASUREMENT_POINTS";

    private ArrayList<DataPoint> dataPoints;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);

        dataPoints = new ArrayList<>();
        updateNumDataPointsCollected();

        startTime = -1;
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
