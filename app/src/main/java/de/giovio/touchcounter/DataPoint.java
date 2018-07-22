package de.giovio.touchcounter;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "data_points", foreignKeys = {
        @ForeignKey(
                entity = DataPointSeries.class,
                parentColumns = "id",
                childColumns = "series_fk"
        )}, indices = {@Index("series_fk")})
public class DataPoint {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "time_in_milliseconds")
    private long time;

    @NonNull
    @ColumnInfo(name = "series_fk")
    private int seriesIdFk;

    public DataPoint() {
        this.time = System.currentTimeMillis();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSeriesIdFk() {
        return seriesIdFk;
    }

    public void setSeriesIdFk(int seriesIdFk) {
        this.seriesIdFk = seriesIdFk;
    }
}
