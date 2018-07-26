package de.giovio.touchcounter.database;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import de.giovio.touchcounter.DataPoint;
import de.giovio.touchcounter.DataPointSeries;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface DataPointSeriesDao {
    //-----------------------------

    @Insert(onConflict = REPLACE)
    long insert(DataPoint dataPoint);

    @Query("DELETE FROM data_points")
    void deleteAllPoints();

    @Query("DELETE FROM data_points WHERE series_fk = :seriesId")
    void deleteAllFromSeries(int seriesId);

    @Query("SELECT * from data_points ORDER BY time_in_milliseconds ASC")
    List<DataPoint> getAllDataPoints();

    @Query("SELECT * from data_points WHERE series_fk = :seriesId ORDER BY time_in_milliseconds ASC")
    List<DataPoint> getAllDataPointsForSeries(int seriesId);

    //-----------------------------

    @Transaction
    @Insert(onConflict = REPLACE)
    long insert(DataPointSeries dataPointSeries);

    @Query("DELETE FROM series")
    void deleteAll();

    @Query("DELETE FROM series WHERE id = :seriesId")
    void deleteById(int seriesId);

    @Transaction
    @Query("SELECT * from series ORDER BY start_time ASC")
    LiveData<List<DataPointSeries>> getAllSeries();

    @Query("SELECT * FROM series WHERE id = :id")
    LiveData<DataPointSeries> getSeries(int id);

    @Query("SELECT COUNT(*) FROM data_points WHERE series_fk = :seriesId")
    int getDataPointCount(int seriesId);
}
