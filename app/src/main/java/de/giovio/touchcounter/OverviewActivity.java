package de.giovio.touchcounter;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverviewActivity extends AppCompatActivity {

    public static final int NEW_MEASUREMENT_ACTIVITY_REQUEST_CODE = 1;

    private DataPointSeriesViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                Intent intent = new Intent(OverviewActivity.this, MeasurementActivity.class);
                //OverviewActivity.this.startActivity(intent);
                startActivityForResult(intent, NEW_MEASUREMENT_ACTIVITY_REQUEST_CODE);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final DataPointSeriesListAdapter adapter = new DataPointSeriesListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mViewModel = ViewModelProviders.of(this).get(DataPointSeriesViewModel.class);
        mViewModel.getAllSeries().observe(this, new Observer<List<DataPointSeries>>() {
            @Override
            public void onChanged(@Nullable final List<DataPointSeries> series) {
                new QueryDbAsync(mViewModel, series, adapter).execute();
            }
        });
    }

    private static class QueryDbAsync extends AsyncTask<Void, Void, Map<Integer, Integer>> {

        private final DataPointSeriesViewModel mViewModel;
        private final List<DataPointSeries> mSeries;
        private final DataPointSeriesListAdapter mAdapter;

        QueryDbAsync(DataPointSeriesViewModel viewModel, List<DataPointSeries> series, DataPointSeriesListAdapter adapter) {
            mViewModel = viewModel;
            mSeries = series;
            mAdapter = adapter;
        }

        @Override
        protected Map<Integer, Integer> doInBackground(final Void... params) {
            Map<Integer, Integer> seriesToCountMap = new HashMap<>();
            for (DataPointSeries dps: mSeries) {
                seriesToCountMap.put(dps.getId(), mViewModel.getDataPointCount(dps.getId()));
            }

            return seriesToCountMap;
            // Update the cached copy of the series in the adapter.
            //mAdapter.setSeries(series, seriesToCountMap);
        }

        @Override
        protected void onPostExecute(Map<Integer, Integer> map) {
            mAdapter.setSeries(mSeries, map);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_MEASUREMENT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            long startTime = Long.parseLong(data.getStringExtra(MeasurementActivity.EXTRA_REPLY_STARTTIME));

            DataPointSeries dps = new DataPointSeries(data.getStringExtra(MeasurementActivity.EXTRA_REPLY_NAME), startTime);

            String[] pointStrings = data.getStringExtra(MeasurementActivity.EXTRA_REPLY_POINTS).split("|");
            List<DataPoint> points = new ArrayList<>();
            for (String s: pointStrings) {
                DataPoint dp = new DataPoint();
                dp.setTime(Long.parseLong(s));
                points.add(dp);
            }
            mViewModel.insert(dps, points);
            Toast.makeText(getApplicationContext(), "Saving...", Toast.LENGTH_LONG).show();
        } else {
            //Toast.makeText(getApplicationContext(), R.string.empty_not_saved, Toast.LENGTH_LONG).show();
        }
    }

}
