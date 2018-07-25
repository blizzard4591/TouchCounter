package de.giovio.touchcounter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

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

    private class ReceivePointsForAllSeriesAsync extends AsyncTask<Void, Void, Map<Integer, List<DataPoint>>> {

        private final DataPointSeriesViewModel mViewModel;
        private final List<DataPointSeries> mSeries;

        ReceivePointsForAllSeriesAsync(DataPointSeriesViewModel viewModel, List<DataPointSeries> series) {
            mViewModel = viewModel;
            mSeries = series;
        }

        @Override
        protected Map<Integer, List<DataPoint>> doInBackground(final Void... params) {
            Map<Integer, List<DataPoint>> result = new HashMap<>();
            for (DataPointSeries dps: mSeries) {
                result.put(dps.getId(), mViewModel.getPointsFromSeries(dps.getId()));
            }

            return result;
        }

        @Override
        protected void onPostExecute(Map<Integer, List<DataPoint>> points) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_deleteAll:
                {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    // Yes button clicked
                                    mViewModel.deleteAll();
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(OverviewActivity.this);
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(getString(R.string.activity_overview_dialog_deleteAll_title))
                            .setMessage(getString(R.string.activity_overview_dialog_deleteAll_text))
                            .setPositiveButton(getString(R.string.activity_overview_dialog_deleteAll_yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.activity_overview_dialog_deleteAll_no), dialogClickListener).show();
                }
                return true;
            case R.id.action_exportAll:
                exportAllSeries();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(OverviewActivity.this, SettingsActivity.class);
                OverviewActivity.this.startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String seriesToString(List<DataPoint> points) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String csvDelimiterOption = sharedPref.getString(SettingsActivity.KEY_PREF_CSV_DELIMITER, "");
        String csvDelimiter = null;
        switch (csvDelimiterOption) {
            case "Comma":
                csvDelimiter = ",";
                break;
            case "Semicolon":
                csvDelimiter = ";";
                break;
            default:
                Log.e("OverviewActivity", "Unhandled and unsupported CSV Delimiter option chosen: " + csvDelimiterOption + " - Defaulting to Semicolon.");
                csvDelimiter = ";";
                break;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Time of Event in ms");
        sb.append(csvDelimiter);
        sb.append("Time since last Event in ms");
        sb.append(csvDelimiter);
        sb.append("Events per Minute (BPM)\n");
        DataPoint last = null;
        for (DataPoint dp: points) {
            sb.append(dp.getTime());
            sb.append(csvDelimiter);
            if (last != null) {
                final long diff = dp.getTime() - last.getTime();
                sb.append(diff);
                sb.append(csvDelimiter);
                sb.append(60000 / diff);
            } else {
                sb.append(csvDelimiter);
            }
            sb.append("\n");

            last = dp;
        }

        return sb.toString();
    }

    private String getFilenameForExport(DataPointSeries dps) {
        return "" + dps.getId() + "-" + dps.getName().replaceAll("[^a-zA-Z0-9]", "") + ".csv";
    }

    private void exportSeries(List<DataPointSeries> series, Map<Integer, List<DataPoint>> pointsMap) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (DataPointSeries dps: series) {
            List<DataPoint> points = pointsMap.get(dps.getId());
            Uri uri = saveFile(getFilenameForExport(dps), seriesToString(points));
            if (uri != null) {
                uris.add(uri);
            }
        }

        if (uris.size() > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            intent.setType("text/plain");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, getResources().getText(R.string.activity_overview_export_intent_title)));
        }
    }

    private Uri saveFile(String filename, String content) {
        try {
            // creates the directory if not present yet
            File exportsDirectory = new File(getFilesDir(), "exports");
            exportsDirectory.mkdir();

            File outputFile = new File(exportsDirectory, filename);
            FileOutputStream stream = new FileOutputStream(outputFile);
            try {
                stream.write(content.getBytes());
            } finally {
                stream.close();
            }

            return FileProvider.getUriForFile(OverviewActivity.this,"de.giovio.touchcounter.fileprovider", outputFile);
        } catch (Exception e) {
            Log.e("OverviewActivity", "Failed to export file: " + e.getMessage());
            Toast.makeText(getApplicationContext(), R.string.activity_overview_export_error, Toast.LENGTH_LONG).show();
        }
        return null;
    }

    private void exportSeries(DataPointSeries series, List<DataPoint> points) {
        Uri fileUri = saveFile(getFilenameForExport(series), seriesToString(points));
        if (fileUri != null) {
            //String mime = getContentResolver().getType(fileUri);
            // Grant temporary read permission to the content URI
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.setType("text/plain");
            //intent.setDataAndType(fileUri, mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, getResources().getText(R.string.activity_overview_export_intent_title)));
        }
    }

    private void exportSeries(DataPointSeries dps) {
        new ReceivePointsForSeriesAsync(mViewModel, dps).execute();
    }

    private void exportAllSeries() {
        new ReceivePointsForAllSeriesAsync(mViewModel, mViewModel.getAllSeries().getValue()).execute();
    }

}
