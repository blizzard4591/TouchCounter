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

    public void insert(DataPointSeries series, List<DataPoint> points) {
        new insertAsyncTask(mDataPointSeriesDao, points).execute(series);
    }

    public int getDataPointCount(int seriesId) {
        return mDataPointSeriesDao.getDataPointCount(seriesId);
    }

    private static class insertAsyncTask extends AsyncTask<DataPointSeries, Void, Void> {
        private DataPointSeriesDao mAsyncTaskDao;
        private List<DataPoint> mPoints;

        insertAsyncTask(DataPointSeriesDao dao, List<DataPoint> points) {
            mAsyncTaskDao = dao;
            mPoints = points;
        }

        @Override
        protected Void doInBackground(final DataPointSeries... params) {
            int id = (int)mAsyncTaskDao.insert(params[0]);
            for (DataPoint dp: mPoints) {
                dp.setSeriesIdFk(id);
                mAsyncTaskDao.insert(dp);
            }
            return null;
        }
    }
}
