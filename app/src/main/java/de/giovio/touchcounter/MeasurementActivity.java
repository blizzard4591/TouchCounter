package de.giovio.touchcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Vector;

public class MeasurementActivity extends AppCompatActivity {

    private Vector<DataPoint> dataPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);

        dataPoints = new Vector<>();
        updateNumDataPointsCollected();
    }

    @Override
    public void onBackPressed() {
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

    private void updateNumDataPointsCollected() {
        final TextView textView = (TextView)findViewById(R.id.textNumDataPointsValue);
        textView.setText(String.valueOf(dataPoints.size()));
    }

    public void btnMeasureOnClick(View v) {
        dataPoints.add(new DataPoint());
        updateNumDataPointsCollected();
    }

    public void btnSaveOnClick(View v) {

    }

    public void btnCancelOnClick(View v) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        Intent intent = new Intent(MeasurementActivity.this, OverviewActivity.class);
                        MeasurementActivity.this.startActivity(intent);
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
