package com.upgradelibrary.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.upgradelibrary.bean.UpgradeBuffer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/10 23:55
 * <p>
 * UpgradeHistorical
 */

public class UpgradeHistorical {
    private static final String TAG = UpgradeHistorical.class.getSimpleName();
    private static final String FILE_NAME = "upgrade";
    private static final String KEY_IGNORE_VERSION = "ignore_version";
    private static final String KEY_DOWNLOAD_HISTORICAL = "download_historical";

    /**
     * 是否忽略版本
     *
     * @param context     Context
     * @param versionCode 版本号
     * @return
     */
    public static synchronized boolean isIgnoreVersion(Context context, int versionCode) {
        try {
            Set<Integer> versions = new HashSet<>(0);
            String json = (String) get(context, KEY_IGNORE_VERSION, new JSONArray().toString());
            JSONArray jsonArray = new JSONArray(json);
            Log.d(TAG, "Ignored version " + jsonArray.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                versions.add(jsonObject.optInt("version_code"));
            }
            return versions.contains(versionCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置忽略版本
     *
     * @param context     Context
     * @param versionCode 版本号
     */
    public static synchronized void setIgnoreVersion(Context context, int versionCode) {
        try {
            String json = (String) get(context, KEY_IGNORE_VERSION, new JSONArray().toString());
            JSONArray jsonArray = new JSONArray(json);
            Log.d(TAG, "Ignored version " + jsonArray.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.optInt("version_code") == versionCode) {
                    return;
                }
            }
            jsonArray.put(new JSONObject().put("version_code", versionCode));
            put(context, KEY_IGNORE_VERSION, jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前下载链接缓存数据
     *
     * @param context     Context
     * @param downloadUrl 下载链接
     * @return
     */
    public static synchronized UpgradeBuffer getUpgradeBuffer(Context context, String downloadUrl) {
        try {
            String json = (String) get(context, KEY_DOWNLOAD_HISTORICAL, new JSONArray().toString());
            JSONArray jsonArray = new JSONArray(json);
            Log.d(TAG, jsonArray.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (TextUtils.equals(jsonObject.getString("download_url"), downloadUrl)) {
                    UpgradeBuffer buffer = new UpgradeBuffer();
                    buffer.setDownloadUrl(jsonObject.optString("download_url"));
                    buffer.setFileMd5(jsonObject.optString("file_md5"));
                    buffer.setFileLength(jsonObject.optLong("file_length"));
                    buffer.setBufferLength(jsonObject.optLong("buffer_length"));
                    JSONArray childJsonArray = jsonObject.optJSONArray("shunt_part");
                    List<UpgradeBuffer.ShuntPart> shuntParts = new ArrayList<>(0);
                    for (int j = 0; j < childJsonArray.length(); j++) {
                        JSONObject childJSONObject = childJsonArray.getJSONObject(j);
                        UpgradeBuffer.ShuntPart shuntPart = new UpgradeBuffer.ShuntPart();
                        shuntPart.setStartLength(childJSONObject.optLong("start_length"));
                        shuntPart.setEndLength(childJSONObject.optLong("end_length"));
                        shuntParts.add(shuntPart);
                    }
                    buffer.setShuntParts(shuntParts);
                    buffer.setLastModified(jsonObject.optLong("last_modified"));
                    return buffer;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置当前下载缓存数据
     *
     * @param context Context
     * @param buffer  缓存数据
     */
    public static synchronized void setUpgradeBuffer(Context context, UpgradeBuffer buffer) {
        try {
            String json = (String) get(context, KEY_DOWNLOAD_HISTORICAL, new JSONArray().toString());
            JSONArray jsonArray = new JSONArray(json);
            Log.d(TAG, jsonArray.toString());
            int index = jsonArray.length();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (TextUtils.equals(jsonObject.getString("download_url"), buffer.getDownloadUrl())) {
                    index = i;
                    break;
                }
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("download_url", buffer.getDownloadUrl());
            jsonObject.put("file_md5", buffer.getFileMd5());
            jsonObject.put("file_length", buffer.getFileLength());
            jsonObject.put("buffer_length", buffer.getBufferLength());
            JSONArray childJSONArray = new JSONArray();
            for (UpgradeBuffer.ShuntPart shuntPart : buffer.getShuntParts()) {
                JSONObject childJSONObject = new JSONObject();
                childJSONObject.put("start_length", shuntPart.getStartLength());
                childJSONObject.put("end_length", shuntPart.getEndLength());
                childJSONArray.put(childJSONObject);
            }
            jsonObject.put("shunt_part", childJSONArray);
            jsonObject.put("last_modified", buffer.getLastModified());
            jsonArray.put(index, jsonObject);
            put(context, KEY_DOWNLOAD_HISTORICAL, jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存参数
     *
     * @param context
     * @param key
     * @param value
     */
    private static void put(Context context, String key, Object value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else {
            editor.putString(key, value.toString());
        }
        editor.apply();
    }

    /**
     * 获取参数
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    private static Object get(Context context, String key, Object defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (defaultValue instanceof String) {
            return sharedPreferences.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return sharedPreferences.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return sharedPreferences.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            return sharedPreferences.getFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Long) {
            return sharedPreferences.getLong(key, (Long) defaultValue);
        }
        return null;
    }

}
