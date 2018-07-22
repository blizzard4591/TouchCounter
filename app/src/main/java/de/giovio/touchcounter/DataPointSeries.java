package de.giovio.touchcounter;

import java.util.List;
import java.util.Vector;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

@Entity(tableName = "series", indices = {@Index("start_time")})
public class DataPointSeries {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "name")
    private String mName;

    @NonNull
    @ColumnInfo(name = "start_time")
    private long mStartTime;

    @Ignore
    public List<DataPoint> mDataPoints = null;

    public DataPointSeries(@NonNull int id, @NonNull String name, @NonNull long startTime) {
        this.id = id;
        this.mName = name;
        this.mStartTime = startTime;
    }

    public String getName() {
        return mName;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<DataPoint> getDataPoints() {
        return mDataPoints;
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        mDataPoints = dataPoints;
    }
}
