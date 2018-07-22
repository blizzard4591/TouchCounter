package de.giovio.touchcounter;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface DataPointDao {
    @Insert(onConflict = REPLACE)
    void insert(DataPoint dataPoint);

    @Query("DELETE FROM data_points")
    void deleteAll();

    @Query("DELETE FROM data_points WHERE series_fk = :seriesId")
    void deleteAllFromSeries(int seriesId);

    @Query("SELECT * from data_points ORDER BY time_in_milliseconds ASC")
    List<DataPoint> getAllDataPoints();

    @Query("SELECT * from data_points WHERE series_fk = :seriesId ORDER BY time_in_milliseconds ASC")
    List<DataPoint> getAllDataPointsForSeries(int seriesId);
}
