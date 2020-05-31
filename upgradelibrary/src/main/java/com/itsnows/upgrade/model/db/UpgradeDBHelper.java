package com.itsnows.upgrade.model.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/6 22:21
 * <p>
 * UpgradeDBHelper
 */
public class UpgradeDBHelper {
    private static final String DB_NAME = "upgrade.db";
    private static final int DB_VERSION = 2;
    private static final String SQL_CREATE_UPGRADE_VERSION = "CREATE TABLE IF NOT EXISTS " +
            UpgradePersistenceContract.UpgradeVersionEntry.TABLE_NAME + " (" +
            UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_VERSION + " INTEGER NOT NULL," +
            UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_IS_IGNORED + " INTEGER,PRIMARY KEY(" +
            UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_VERSION + "))";
    private static final String SQL_CREATE_UPGRADE_BUFFER = "CREATE TABLE IF NOT EXISTS " +
            UpgradePersistenceContract.UpgradeBufferEntry.TABLE_NAME + " (" +
            UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL + " TEXT NOT NULL," +
            UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_MD5 + " TEXT," +
            UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_PATH + " TEXT," +
            UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_LENGTH + " INTEGER," +
            UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_BUFFER_LENGTH + " INTEGER," +
            UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_BUFFER_PART + " INTEGER," +
            UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_LAST_MODIFIED + " INTEGER,PRIMARY KEY(" +
            UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL + "))";
    private SQLiteOpenHelper helper;
    private SQLiteDatabase db;
    private AtomicInteger lock;

    public UpgradeDBHelper(Context context) {
        this(context, DB_NAME, null, DB_VERSION);
    }

    public UpgradeDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    public UpgradeDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        helper = new UpgradeDBHelperWrapper(context, name, factory, version, errorHandler);
        lock = new AtomicInteger();
    }

    public synchronized SQLiteDatabase getWritableDatabase() {
        if (lock.getAndIncrement() == 0) {
            db = helper.getWritableDatabase();
        }
        return db;
    }

    public synchronized SQLiteDatabase getReadableDatabase() {
        if (lock.getAndIncrement() == 0) {
            db = helper.getReadableDatabase();
        }
        return db;
    }

    public synchronized void close(SQLiteDatabase db) {
        if (lock.decrementAndGet() == 0) {
            db.close();
        }
    }

    private static class UpgradeDBHelperWrapper extends SQLiteOpenHelper {

        private UpgradeDBHelperWrapper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
            super(context, name, factory, version, errorHandler);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_UPGRADE_VERSION);
            db.execSQL(SQL_CREATE_UPGRADE_BUFFER);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            for (int version = oldVersion; version <= newVersion; version++) {
                switch (version) {
                    case 2:
                        db.execSQL("DROP TABLE " + UpgradePersistenceContract.UpgradeVersionEntry.TABLE_NAME);
                        db.execSQL("DROP TABLE " + UpgradePersistenceContract.UpgradeBufferEntry.TABLE_NAME);
                        onCreate(db);
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            super.onDowngrade(db, oldVersion, newVersion);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }

    }

}
