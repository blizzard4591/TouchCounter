package de.giovio.touchcounter;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = 1, entities = {DataPoint.class, DataPointSeries.class})
public abstract class SeriesDatabase extends RoomDatabase {
    public abstract DataPointDao dataPointDao();
    public abstract DataPointSeriesDao dataPointSeriesDao();

    private static SeriesDatabase INSTANCE;

    static SeriesDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SeriesDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SeriesDatabase.class, "series_database").build();
                }
            }
        }
        return INSTANCE;
    }
}
