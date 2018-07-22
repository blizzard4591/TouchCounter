package de.giovio.touchcounter;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

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

    public void insert(DataPointSeries series) {
        mRepository.insert(series);
    }
}
