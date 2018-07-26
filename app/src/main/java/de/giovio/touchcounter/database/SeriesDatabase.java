package de.giovio.touchcounter.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import de.giovio.touchcounter.DataPoint;
import de.giovio.touchcounter.DataPointSeries;

@Database(version = 1, entities = {DataPoint.class, DataPointSeries.class})
public abstract class SeriesDatabase extends RoomDatabase {
    public abstract DataPointSeriesDao dataPointSeriesDao();

    private static SeriesDatabase INSTANCE;

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final DataPointSeriesDao mDao;

        PopulateDbAsync(SeriesDatabase db) {
            mDao = db.dataPointSeriesDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDao.deleteAllPoints();
            mDao.deleteAll();
            DataPointSeries dpsA = new DataPointSeries("Hello World", 123456);
            long dpsAId = mDao.insert(dpsA);
            DataPointSeries dpsB = new DataPointSeries("Testi Test", 1234567);
            long dpsBId = mDao.insert(dpsB);

            DataPoint dpA = new DataPoint();
            dpA.setSeriesIdFk((int)dpsAId);
            mDao.insert(dpA);

            DataPoint dpB = new DataPoint();
            dpB.setSeriesIdFk((int)dpsBId);
            mDao.insert(dpB);

            DataPoint dpC = new DataPoint();
            dpC.setSeriesIdFk((int)dpsBId);
            mDao.insert(dpC);

            return null;
        }
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onOpen (@NonNull SupportSQLiteDatabase db){
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    static SeriesDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SeriesDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SeriesDatabase.class, "series_database")
                            //.addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
