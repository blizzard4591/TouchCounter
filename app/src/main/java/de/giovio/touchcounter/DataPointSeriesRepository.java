package de.giovio.touchcounter;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;

public class DataPointSeriesRepository {
    private DataPointSeriesDao mDataPointSeriesDao;
    private LiveData<List<DataPointSeries>> mAllSeries;

    DataPointSeriesRepository(Application application) {
        SeriesDatabase db = SeriesDatabase.getDatabase(application);
        mDataPointSeriesDao = db.dataPointSeriesDao();
        mAllSeries = mDataPointSeriesDao.getAllSeries();
    }

    LiveData<List<DataPointSeries>> getAllSeries() {
        return mAllSeries;
    }

    public void insert(DataPointSeries series) {
        new insertAsyncTask(mDataPointSeriesDao).execute(series);
    }

    private static class insertAsyncTask extends AsyncTask<DataPointSeries, Void, Void> {
        private DataPointSeriesDao mAsyncTaskDao;

        insertAsyncTask(DataPointSeriesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final DataPointSeries... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
