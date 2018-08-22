package de.giovio.touchcounter;

import android.os.Parcel;
import android.os.Parcelable;

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
public class DataPoint implements Parcelable {
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

    // Parcel stuff
    public DataPoint(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<DataPoint> CREATOR = new Parcelable.Creator<DataPoint>() {
        public DataPoint createFromParcel(Parcel in) {
            return new DataPoint(in);
        }

        public DataPoint[] newArray(int size) {
            return new DataPoint[size];
        }
    };

    public void readFromParcel(Parcel in) {
        id = in.readInt();
        time = in.readLong();
        seriesIdFk = in.readInt();

    }
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeLong(time);
        dest.writeInt(seriesIdFk);
    }
}
