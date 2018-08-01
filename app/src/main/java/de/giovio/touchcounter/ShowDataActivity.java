package de.giovio.touchcounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowDataActivity extends AppCompatActivity {

    public static final String EXTRA_SERIES_ID = "EXTRA_SERIES_ID";

    private DataPointSeriesViewModel mViewModel;
    private int mSeriesId;
    private List<DataPoint> mDataPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);

        mViewModel = ViewModelProviders.of(this).get(DataPointSeriesViewModel.class);
        mSeriesId = getIntent().getIntExtra(EXTRA_SERIES_ID, -1);
        mDataPoints = new ArrayList<>();

        new ReceiveDataPointsAsync(mViewModel, mSeriesId).execute();
    }

    private class ReceiveDataPointsAsync extends AsyncTask<Void, Void, List<DataPoint>> {

        private final DataPointSeriesViewModel mLocalViewModel;
        private final int mLocalSeriesId;

        ReceiveDataPointsAsync(DataPointSeriesViewModel viewModel, int localSeriesId) {
            mLocalViewModel = viewModel;
            mLocalSeriesId = localSeriesId;
        }

        @Override
        protected List<DataPoint> doInBackground(final Void... params) {
            return mLocalViewModel.getPointsFromSeries(mLocalSeriesId);
        }

        @Override
        protected void onPostExecute(List<DataPoint> points) {
            mDataPoints = points;
            showDataPoints(points);
        }
    }

    private void showDataPoints(List<DataPoint> points) {
        if (points.size() < 2) {
            return;
        }
        GraphView graph = (GraphView) findViewById(R.id.graph);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String minDiffOptionString = sharedPref.getString(SettingsActivity.KEY_PREF_MIN_TIME_DIFF, "50");
        final long minDiff = Long.parseLong(minDiffOptionString);


        ArrayList<com.jjoe64.graphview.series.DataPoint> prelimList = new ArrayList<>();
        final long smallestTime = points.get(0).getTime();
        double largestX = 0;
        double largestY = 0;
        double smallestY = 60000 + 1;
        int i = 0;
        DataPoint last = null;
        for (DataPoint dp: points) {
            if (last != null) {
                final long diff = dp.getTime() - last.getTime();
                if (diff <= minDiff) {
                    continue;
                }
            }

            if (last != null) {
                long diff = dp.getTime() - last.getTime();
                if (diff == 0) {
                    diff = 1;
                }

                final double x = dp.getTime() - smallestTime;
                final double y = (60000.0 / diff);
                prelimList.add(new com.jjoe64.graphview.series.DataPoint(x, y));
                if (x > largestX) {
                    largestX = x;
                }
                if (y > largestY) {
                    largestY = y;
                }
                if (y < smallestY) {
                    smallestY = y;
                }
            }
            last = dp;
        }
        Log.i("ShowDataActivity", "Displaying " + prelimList.size() + " points from " + points.size() + " inputs.");
        com.jjoe64.graphview.series.DataPoint graphPoints[] = prelimList.toArray(new com.jjoe64.graphview.series.DataPoint[prelimList.size()]);


        LineGraphSeries<com.jjoe64.graphview.series.DataPoint> series = new LineGraphSeries<>(graphPoints);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(smallestY * 0.9);
        graph.getViewport().setMaxY(largestY * 1.1);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(largestX);

        // enable scaling and scrolling
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScrollableY(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                // return as Integer
                return String.valueOf((int) value);
            }
        });

        series.setTitle("Beats per Minute");
        series.setColor(Color.RED);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(8);

        graph.addSeries(series);
    }
}
