package com.milk.milkrun.localrate;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {LocalMilkRate.class}, version = 2) // ⬅ updated version number
public abstract class MilkRateDatabase extends RoomDatabase {

    private static MilkRateDatabase instance;

    public abstract MilkRateDao milkRateDao();

    public static synchronized MilkRateDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            MilkRateDatabase.class, "milk_rate_db")
                    .addMigrations(MIGRATION_1_2) // ⬅ apply migration logic
                    .build();
        }
        return instance;
    }

    // Customize this migration logic based on your schema change
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Example: if you added a new column `snf` to the table
            // Replace this with your actual changes
            database.execSQL("ALTER TABLE LocalMilkRate ADD COLUMN snf TEXT");
        }
    };
}
