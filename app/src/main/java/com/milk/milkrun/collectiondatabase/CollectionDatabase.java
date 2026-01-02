package com.milk.milkrun.collectiondatabase;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.milk.milkrun.PendingRequest;
import com.milk.milkrun.PendingRequestDao;

@Database(entities = {MilkCollection.class, PendingRequest.class}, version = 6)
public abstract class CollectionDatabase extends RoomDatabase {

    private static volatile CollectionDatabase INSTANCE;

    public abstract MilkCollectionDao milkCollectionDao();
    public abstract PendingRequestDao pendingRequestDao();

    // Migration from version 4 to 5: Added aTrNo, trNo, mlkTrType
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE milk_collection ADD COLUMN aTrNo TEXT");
            database.execSQL("ALTER TABLE milk_collection ADD COLUMN trNo TEXT");
            database.execSQL("ALTER TABLE milk_collection ADD COLUMN mlkTrType TEXT");
        }
    };

    // ✅ Migration from version 5 to 6: Added meCode column
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE milk_collection ADD COLUMN meCode INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static CollectionDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CollectionDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    CollectionDatabase.class,
                                    "milk_collection_db")
                            .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
