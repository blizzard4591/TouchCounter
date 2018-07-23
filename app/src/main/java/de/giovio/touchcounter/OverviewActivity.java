package de.giovio.touchcounter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
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
        registerForContextMenu(recyclerView);

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

    private class ReceivePointsForSeriesAsync extends AsyncTask<Void, Void, List<DataPoint>> {

        private final DataPointSeriesViewModel mViewModel;
        private final DataPointSeries mSeries;

        ReceivePointsForSeriesAsync(DataPointSeriesViewModel viewModel, DataPointSeries series) {
            mViewModel = viewModel;
            mSeries = series;
        }

        @Override
        protected List<DataPoint> doInBackground(final Void... params) {
            return mViewModel.getPointsFromSeries(mSeries.getId());
        }

        @Override
        protected void onPostExecute(List<DataPoint> points) {
            exportSeries(mSeries, points);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_MEASUREMENT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            long startTime = Long.parseLong(data.getStringExtra(MeasurementActivity.EXTRA_REPLY_STARTTIME));

            DataPointSeries dps = new DataPointSeries(data.getStringExtra(MeasurementActivity.EXTRA_REPLY_NAME), startTime);

            String[] pointStrings = data.getStringExtra(MeasurementActivity.EXTRA_REPLY_POINTS).split("#");
            List<DataPoint> points = new ArrayList<>();
            for (String s: pointStrings) {
                DataPoint dp = new DataPoint();
                try {
                    dp.setTime(Long.parseLong(s));
                    points.add(dp);
                } catch (NumberFormatException e) {
                    Log.e("OverviewActivity", "Could not parse DataPoint \"" + s + "\" to long!");
                }
            }
            mViewModel.insert(dps, points);
            Toast.makeText(getApplicationContext(), "Saving...", Toast.LENGTH_LONG).show();
        } else {
            //Toast.makeText(getApplicationContext(), R.string.empty_not_saved, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.recyclerView) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_longpress, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuRecyclerView.RecyclerViewContextMenuInfo info = (ContextMenuRecyclerView.RecyclerViewContextMenuInfo) item.getMenuInfo();
        // handle menu here - get item index or ID from info
        int itemIndex = info.position;
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        DataPointSeriesListAdapter adapter = (DataPointSeriesListAdapter)recyclerView.getAdapter();
        final DataPointSeries dps = adapter.getItem(itemIndex);

        switch(item.getItemId()) {
            case R.id.menu_longpress_export:
                // export stuff here
                exportSeries(dps);
                return true;
            case R.id.menu_longpress_delete:
                // remove stuff here
                {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    // Yes button clicked
                                    mViewModel.delete(dps);
                                    Toast.makeText(getApplicationContext(), "Deleting \"" + dps.getName() + "\"...", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(OverviewActivity.this);
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(getString(R.string.activity_overview_dialog_delete_title))
                            .setMessage(getString(R.string.activity_overview_dialog_delete_text))
                            .setPositiveButton(getString(R.string.activity_overview_dialog_delete_yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.activity_overview_dialog_delete_no), dialogClickListener).show();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private String seriesToString(List<DataPoint> points) {
        StringBuilder sb = new StringBuilder();
        sb.append("Time of Event in ms, Time since last Event in ms, Events per Minute (BPM)\n");
        DataPoint last = null;
        for (DataPoint dp: points) {
            sb.append(dp.getTime());
            sb.append(",");
            if (last != null) {
                final long diff = dp.getTime() - last.getTime();
                sb.append(diff);
                sb.append(",");
                sb.append(60000 / diff);
            } else {
                sb.append(",");
            }
            sb.append("\n");

            last = dp;
        }

        return sb.toString();
    }

    private void exportSeries(DataPointSeries series, List<DataPoint> points) {
        FileOutputStream outputStream;
        final String filename = "" + series.getId() + "-" + series.getName().replaceAll("[^a-zA-Z0-9]", "") + ".csv";

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(seriesToString(points).getBytes());
            outputStream.close();

            File exportFile = new File(filename);
            try {
                Uri fileUri = FileProvider.getUriForFile(OverviewActivity.this,"de.giovio.touchcounter.fileprovider", exportFile);
                if (fileUri != null) {
                    String mime = getContentResolver().getType(fileUri);
                    // Grant temporary read permission to the content URI
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(fileUri, mime);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                }
            } catch (IllegalArgumentException e) {
                Log.e("OverviewActivity", "The selected file can't be shared: " + filename);
            }

            Toast.makeText(getApplicationContext(), getString(R.string.activity_overview_export_success) + filename + " containing " + points.size() + " events.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.activity_overview_export_error, Toast.LENGTH_LONG).show();
        }
    }

    private void exportSeries(DataPointSeries dps) {
        new ReceivePointsForSeriesAsync(mViewModel, dps).execute();
    }

}
