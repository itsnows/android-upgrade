package com.itsnows.android.upgrade.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.itsnows.android.upgrade.UpgradeLogger;
import com.itsnows.android.upgrade.model.bean.UpgradeBuffer;
import com.itsnows.android.upgrade.model.bean.UpgradeVersion;
import com.itsnows.android.upgrade.model.db.UpgradeDBHelper;
import com.itsnows.android.upgrade.model.db.UpgradePersistenceContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * UpgradeRepository
 *
 * @author itsnows, xue.com.fei@gmail.com
 * @since 2018/2/6 20:55
 */
public class UpgradeRepository implements UpgradeDataSource {
    private static final String TAG = UpgradeRepository.class.getSimpleName();
    private static UpgradeRepository instance;
    private final UpgradeDBHelper helper;

    private UpgradeRepository(Context context) {
        helper = new UpgradeDBHelper(context);
    }

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

    @SuppressLint("Range")
    @Override
    public UpgradeVersion getUpgradeVersion(int versionCode) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM " +
                UpgradePersistenceContract.UpgradeVersionEntry.TABLE_NAME + " WHERE " +
                UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_VERSION + "=?";
        String[] selectionArgs = new String[]{String.valueOf(versionCode)};
        Cursor cursor = null;
        UpgradeVersion upgradeVersion = null;
        try {
            cursor = db.rawQuery(sql, selectionArgs);
            if (cursor.moveToNext()) {
                upgradeVersion = new UpgradeVersion();
                upgradeVersion.setVersion(cursor.getInt(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_VERSION)));
                upgradeVersion.setIgnored(cursor.getInt(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_IS_IGNORED)) == 1);
            }
        } finally {
            helper.close(cursor, db);
        }
        return upgradeVersion;
    }

    @Override
    public void putUpgradeVersion(UpgradeVersion version) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO " +
                UpgradePersistenceContract.UpgradeVersionEntry.TABLE_NAME + "(" +
                UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_VERSION + "," +
                UpgradePersistenceContract.UpgradeVersionEntry.COLUMN_NAME_IS_IGNORED + ")VALUES(?,?)";
        try {
            Object[] bindArgs = new Object[]{
                    version.getVersion(),
                    version.isIgnored()};
            db.execSQL(sql, bindArgs);
        } finally {
            helper.close(db);
        }
    }

    @SuppressLint("Range")
    @Override
    public UpgradeBuffer getUpgradeBuffer(String url) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM " +
                UpgradePersistenceContract.UpgradeBufferEntry.TABLE_NAME + " WHERE " +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL + "=?";
        String[] selectionArgs = new String[]{url};
        Cursor cursor = null;
        UpgradeBuffer upgradeBuffer = null;
        try {
            cursor = db.rawQuery(sql, selectionArgs);
            if (cursor.moveToNext()) {
                upgradeBuffer = new UpgradeBuffer();
                upgradeBuffer.setDownloadUrl(cursor.getString(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL)));
                upgradeBuffer.setFileMd5(cursor.getString(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_MD5)));
                upgradeBuffer.setFilePath(cursor.getString(cursor.getColumnIndex(
                        UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_PATH)));
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
            }
        } catch (Exception e) {
            UpgradeLogger.e(TAG, "Get UpgradeBuffer failure", e);
        } finally {
            helper.close(cursor, db);
        }
        return upgradeBuffer;
    }

    @Override
    public void putUpgradeBuffer(UpgradeBuffer buffer) {
        long startTime = System.currentTimeMillis();
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO " +
                UpgradePersistenceContract.UpgradeBufferEntry.TABLE_NAME + "(" +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_DOWNLOAD_URL + "," +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_MD5 + "," +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_PATH + "," +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_FILE_LENGTH + "," +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_BUFFER_LENGTH + "," +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_BUFFER_PART + "," +
                UpgradePersistenceContract.UpgradeBufferEntry.COLUMN_NAME_LAST_MODIFIED + ")VALUES(?,?,?,?,?,?,?)";
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
                    buffer.getFilePath(),
                    buffer.getFileLength(),
                    buffer.getBufferLength(),
                    ja.toString(),
                    buffer.getLastModified()};
            db.execSQL(sql, bindArgs);
            long endTime = System.currentTimeMillis();
            UpgradeLogger.d(TAG, "Elapsed time: " + (endTime - startTime));
        } catch (Exception e) {
            UpgradeLogger.e(TAG, "Put UpgradeBuffer failure", e);
        } finally {
            helper.close(db);
        }
    }

}
