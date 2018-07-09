package com.upgradelibrary.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.upgradelibrary.data.bean.UpgradeBuffer;
import com.upgradelibrary.data.bean.UpgradeVersion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/6 20:55
 * <p>
 * UpgradeDataManager
 */
public class UpgradeRepository implements UpgradeDataSource {
    private UpgradeSQLiteOpenHelper helper;

    public UpgradeRepository(Context context) {
        helper = new UpgradeSQLiteOpenHelper(context);
    }

    @Override
    public UpgradeVersion getUpgradeVersion(int versionCode) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM " +
                UpgradePersistenceContrat.UpgradeVersionEntry.TABLE_NAME + " WHERE " +
                UpgradePersistenceContrat.UpgradeVersionEntry.COLUMN_NAME_VERSION + "=?";
        String[] selectionArgs = new String[]{String.valueOf(versionCode)};
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                UpgradeVersion upgradeVersion = new UpgradeVersion();
                upgradeVersion.setVersion(cursor.getColumnIndex(UpgradePersistenceContrat.UpgradeVersionEntry.COLUMN_NAME_VERSION));
                upgradeVersion.setIgnored(cursor.getInt(cursor.getColumnIndex(UpgradePersistenceContrat.UpgradeVersionEntry.COLUMN_NAME_VERSION)) == 1);
                return upgradeVersion;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return null;
    }

    @Override
    public void putUpgradeVersion(UpgradeVersion upgradeVersion) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO " +
                UpgradePersistenceContrat.UpgradeVersionEntry.TABLE_NAME + "(" +
                UpgradePersistenceContrat.UpgradeVersionEntry.COLUMN_NAME_VERSION + ")VALUES(?)";
        try {
            Object[] bindArgs = new Object[]{upgradeVersion.getVersion()};
            db.execSQL(sql, bindArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    public UpgradeBuffer getUpgradeBuffer(String url) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM " +
                UpgradePersistenceContrat.UpgradeBufferEntry.TABLE_NAME + " WHERE " +
                UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL + "=?";
        String[] selectionArgs = new String[]{url};
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                UpgradeBuffer upgradeBuffer = new UpgradeBuffer();
                upgradeBuffer.setDownloadUrl(cursor.getString(cursor.getColumnIndex(UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL)));
                upgradeBuffer.setFileMd5(cursor.getString(cursor.getColumnIndex(UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_FILE_MD5)));
                upgradeBuffer.setFileLength(cursor.getLong(cursor.getColumnIndex(UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_FILE_LENGTH)));
                upgradeBuffer.setBufferLength(cursor.getLong(cursor.getColumnIndex(UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_BUFFER_LENGTH)));
                String bufferPart = cursor.getString(cursor.getColumnIndex(UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_BUFFER_PART));
                List<UpgradeBuffer.BufferPart> bufferParts = new CopyOnWriteArrayList<>();
                JSONArray ja = new JSONArray(bufferPart);
                for (int j = 0; j < ja.length(); j++) {
                    JSONObject jo = ja.getJSONObject(j);
                    long startLength = jo.optLong("start_length");
                    long endLength = jo.optLong("end_length");
                    bufferParts.add(new UpgradeBuffer.BufferPart(startLength, endLength));
                }
                upgradeBuffer.setBufferParts(bufferParts);
                upgradeBuffer.setLastModified(cursor.getLong(cursor.getColumnIndex(UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_LAST_MODIFIED)));
                return upgradeBuffer;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return null;
    }

    @Override
    public void putUpgradeBuffer(UpgradeBuffer upgradeBuffer) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO " +
                UpgradePersistenceContrat.UpgradeBufferEntry.TABLE_NAME + "(" +
                UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL + "," +
                UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_FILE_MD5 + "," +
                UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_FILE_LENGTH + "," +
                UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_BUFFER_LENGTH + "," +
                UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_BUFFER_PART + "," +
                UpgradePersistenceContrat.UpgradeBufferEntry.COLUMN_NAME_LAST_MODIFIED + ")VALUES(?,?,?,?,?,?)";
        Object[] bindArgs = new Object[]{
                upgradeBuffer.getDownloadUrl(),
                upgradeBuffer.getFileMd5(),
                upgradeBuffer.getFileLength(),
                upgradeBuffer.getBufferLength(),
                upgradeBuffer.getLastModified(),
        };
        try {
            db.execSQL(sql, bindArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }

    }
}
