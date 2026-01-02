package com.milk.milkrun.customerdatabase;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Customer.class}, version = 2, exportSchema = false)
public abstract class CustomerDatabase extends RoomDatabase {

    private static volatile CustomerDatabase INSTANCE;

    public abstract CustomerDao customerDao();

    public static CustomerDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CustomerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    CustomerDatabase.class, "customer_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
