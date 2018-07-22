package de.giovio.touchcounter;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface DataPointSeriesDao {
    //-----------------------------

    @Insert(onConflict = REPLACE)
    void insert(DataPoint dataPoint);

    @Query("DELETE FROM data_points WHERE series_fk = :seriesId")
    void deleteAllFromSeries(int seriesId);

    @Query("SELECT * from data_points ORDER BY time_in_milliseconds ASC")
    List<DataPoint> getAllDataPoints();

    @Query("SELECT * from data_points WHERE series_fk = :seriesId ORDER BY time_in_milliseconds ASC")
    List<DataPoint> getAllDataPointsForSeries(int seriesId);

    //-----------------------------

    @Transaction
    @Insert(onConflict = REPLACE)
    void insert(DataPointSeries dataPointSeries);

    @Query("DELETE FROM series")
    void deleteAll();

    @Query("DELETE FROM series WHERE id = :seriesId")
    void deleteById(int seriesId);

    @Transaction
    @Query("SELECT * from series ORDER BY start_time ASC")
    LiveData<List<DataPointSeries>> getAllSeries();

    @Query("SELECT * FROM series WHERE id = :id")
    abstract LiveData<DataPointSeries> getSeries(int id);
}
