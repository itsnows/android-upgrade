package com.itsnows.upgrade.data;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/6 22:21
 * <p>
 * UpgradeDBHelper
 */
public class UpgradeDBHelper extends SQLiteOpenHelper {

    /**
     * 数据库名称
     */
    public static final String DB_NAME = "upgrade.db";

    /**
     * 数据库版本
     */
    private static final int DB_VERSION = 1;

    /**
     * 版本忽略表
     */
    private static final String SQL_CREATE_UPGRADE_VERSION = "CREATE TABLE IF NOT EXISTS " +
            UpgradePersistenceContrat.UpgradeVersionEntry.TABLE_NAME + " (" +
            UpgradePersistenceContrat.UpgradeVersionEntry.COLUMN_NAME_VERSION + " INTEGER NOT NULL," +
            UpgradePersistenceContrat.UpgradeVersionEntry.COLUMN_NAME_IS_IGNORED + " INTEGER,PRIMARY KEY(" +
            UpgradePersistenceContrat.UpgradeVersionEntry.COLUMN_NAME_VERSION + "))";

    /**
     * 版本缓存表
     */
    private static final String SQL_CREATE_UPGRADE_BUFFER = "CREATE TABLE IF NOT EXISTS " +
            UpgradePersistenceContrat.UpgradeBufferEntry.TABLE_NAME + " (" +
            UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL + " TEXT NOT NULL," +
            UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_FILE_MD5 + " TEXT," +
            UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_FILE_LENGTH + " INTEGER," +
            UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_BUFFER_LENGTH + " INTEGER," +
            UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_BUFFER_PART + " INTEGER," +
            UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_LAST_MODIFIED + " INTEGER,PRIMARY KEY(" +
            UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL + "))";

    public UpgradeDBHelper(Context context) {
        this(context, DB_NAME, null, DB_VERSION);
    }

    public UpgradeDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    public UpgradeDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_UPGRADE_VERSION);
        db.execSQL(SQL_CREATE_UPGRADE_BUFFER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
