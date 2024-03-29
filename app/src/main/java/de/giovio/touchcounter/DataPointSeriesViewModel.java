package de.giovio.touchcounter;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import de.giovio.touchcounter.database.DataPointSeriesRepository;

public class DataPointSeriesViewModel extends AndroidViewModel {
    private DataPointSeriesRepository mRepository;
    private LiveData<List<DataPointSeries>> mAllSeries;
    public DataPointSeriesViewModel(Application application) {
        super(application);
        mRepository = new DataPointSeriesRepository(application);
        mAllSeries = mRepository.getAllSeries();
    }

    LiveData<List<DataPointSeries>> getAllSeries() {
        return mAllSeries;
    }

    public void insert(DataPointSeries series, List<DataPoint> points) {
        mRepository.insert(series, points);
    }

    public void delete(DataPointSeries series) {
        mRepository.delete(series);
    }

    public void deleteAll() {
        mRepository.deleteAll();
    }

    public List<DataPoint> getPointsFromSeries(int seriesId) {
        return mRepository.getPointsFromSeries(seriesId);
    }

    public int getDataPointCount(int seriesId) {
        return mRepository.getDataPointCount(seriesId);
    }
}
