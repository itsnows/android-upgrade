package com.itsnows.upgrade.model;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.itsnows.upgrade.model.bean.UpgradeBuffer;
import com.itsnows.upgrade.model.bean.UpgradeVersion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/6 20:55
 * <p>
 * UpgradeRepository
 */
public class UpgradeRepository implements UpgradeDataSource {
    private static UpgradeRepository instance;
    private UpgradeDBHelper helper;

    public static UpgradeRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (UpgradeRepository.class) {
                if (instance == null) {
                    instance = new UpgradeRepository(context);
                }
            }
        }
        return instance;
    }

    private UpgradeRepository(Context context) {
        helper = new UpgradeDBHelper(context);
    }

    @Override
    public UpgradeVersion getUpgradeVersion(int versionCode) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM " +
                UpgradePersistenceContract.UpgradeVersionEntry.TABLE_NAME + " WHERE " +
                UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_VERSION + "=?";
        String[] selectionArgs = new String[]{String.valueOf(versionCode)};
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                UpgradeVersion upgradeVersion = new UpgradeVersion();
                upgradeVersion.setVersion(cursor.getInt(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_VERSION)));
                upgradeVersion.setIgnored(cursor.getInt(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_IS_IGNORED)) == 1);
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
    public void putUpgradeVersion(UpgradeVersion version) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO "
                + UpgradePersistenceContract.UpgradeVersionEntry.TABLE_NAME + "("
                + UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_VERSION + ","
                + UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_IS_IGNORED + ")VALUES(?,?)";
        try {
            Object[] bindArgs = new Object[]{
                    version.getVersion(),
                    version.isIgnored()};
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
                UpgradePersistenceContract.UpgradeBufferEntry.TABLE_NAME + " WHERE " +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL + "=?";
        String[] selectionArgs = new String[]{url};
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                UpgradeBuffer upgradeBuffer = new UpgradeBuffer();
                upgradeBuffer.setDownloadUrl(cursor.getString(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL)));
                upgradeBuffer.setFileMd5(cursor.getString(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_MD5)));
                upgradeBuffer.setFileLength(cursor.getLong(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_LENGTH)));
                upgradeBuffer.setBufferLength(cursor.getLong(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_BUFFER_LENGTH)));
                String bufferPart = cursor.getString(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_BUFFER_PART));
                List<UpgradeBuffer.BufferPart> bufferParts = new CopyOnWriteArrayList<>();
                JSONArray ja = new JSONArray(bufferPart);
                for (int index = 0; index < ja.length(); index++) {
                    JSONObject jo = ja.getJSONObject(index);
                    long startLength = jo.optLong("start_length");
                    long endLength = jo.optLong("end_length");
                    bufferParts.add(new UpgradeBuffer.BufferPart(startLength, endLength));
                }
                upgradeBuffer.setBufferParts(bufferParts);
                upgradeBuffer.setLastModified(cursor.getLong(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_LAST_MODIFIED)));
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
    public void putUpgradeBuffer(UpgradeBuffer buffer) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO " +
                UpgradePersistenceContract.UpgradeBufferEntry.TABLE_NAME + "(" +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL + "," +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_MD5 + "," +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_LENGTH + "," +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_BUFFER_LENGTH + "," +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_BUFFER_PART + "," +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_LAST_MODIFIED + ")VALUES(?,?,?,?,?,?)";
        try {
            JSONArray ja = new JSONArray();
            List<UpgradeBuffer.BufferPart> bufferParts = buffer.getBufferParts();
            for (int index = 0; index < bufferParts.size(); index++) {
                JSONObject jo = new JSONObject();
                jo.put("start_length", bufferParts.get(index).getStartLength());
                jo.put("end_length", bufferParts.get(index).getEndLength());
                ja.put(index, jo);
            }
            Object[] bindArgs = new Object[]{
                    buffer.getDownloadUrl(),
                    buffer.getFileMd5(),
                    buffer.getFileLength(),
                    buffer.getBufferLength(),
                    ja.toString(),
                    buffer.getLastModified()};
            db.execSQL(sql, bindArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }

    }
}
