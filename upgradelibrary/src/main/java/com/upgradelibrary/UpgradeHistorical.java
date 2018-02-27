package com.upgradelibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

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

    public static boolean isIgnoreVersion(Context context, int version) {
        try {
            Set<Integer> versions = new HashSet<>(0);
            String json = (String) get(context, KEY_IGNORE_VERSION, new JSONArray().toString());
            JSONArray jsonArray = new JSONArray(json);
            Log.d(TAG, jsonArray.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                versions.add(jsonObject.optInt("version"));
            }
            return versions.contains(version);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setIgnoreVersion(Context context, int version) {
        try {
            String json = (String) get(context, KEY_IGNORE_VERSION, new JSONArray().toString());
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Log.d(TAG, jsonArray.toString());
                if (jsonObject.getInt("version") == version) {
                    return;
                }
            }
            jsonArray.put(new JSONObject().put("version", version));
            put(context, KEY_IGNORE_VERSION, jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static BufferFile getBufferFile(Context context, String url) {
        try {
            String json = (String) get(context, KEY_DOWNLOAD_HISTORICAL, new JSONArray().toString());
            JSONArray jsonArray = new JSONArray(json);
            Log.d(TAG, jsonArray.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (TextUtils.equals(jsonObject.getString("url"), url)) {
                    BufferFile bufferFile = new BufferFile();
                    bufferFile.setUrl(jsonObject.optString("url"));
                    bufferFile.setMd5(jsonObject.optString("md5"));
                    bufferFile.setLength(jsonObject.optLong("length"));
                    bufferFile.setBufferLength(jsonObject.optLong("bufferLength"));
                    JSONArray childJsonArray = jsonObject.optJSONArray("part");
                    List<BufferFile.Part> parts = new ArrayList<>(0);
                    for (int j = 0; j < childJsonArray.length(); j++) {
                        JSONObject childJSONObject = childJsonArray.getJSONObject(j);
                        BufferFile.Part part = new BufferFile.Part();
                        part.setStartLength(childJSONObject.optLong("startLength"));
                        part.setEndLength(childJSONObject.optLong("endLength"));
                        parts.add(part);
                    }
                    bufferFile.setParts(parts);
                    bufferFile.setLastModified(jsonObject.optLong("lastModified"));
                    return bufferFile;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setBufferFile(Context context, BufferFile bufferFile) {
        try {
            String json = (String) get(context, KEY_DOWNLOAD_HISTORICAL, new JSONArray().toString());
            JSONArray jsonArray = new JSONArray(json);
            Log.d(TAG, jsonArray.toString());
            int index = jsonArray.length();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (TextUtils.equals(jsonObject.getString("url"), bufferFile.getUrl())) {
                    index = i;
                    break;
                }
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("url", bufferFile.getUrl());
            jsonObject.put("md5", bufferFile.getMd5());
            jsonObject.put("length", bufferFile.getLength());
            jsonObject.put("bufferLength", bufferFile.getBufferLength());
            JSONArray childJSONArray = new JSONArray();
            for (BufferFile.Part part : bufferFile.getParts()) {
                JSONObject childJSONObject = new JSONObject();
                childJSONObject.put("startLength", part.getStartLength());
                childJSONObject.put("endLength", part.getEndLength());
                childJSONArray.put(childJSONObject);
            }
            jsonObject.put("part", childJSONArray);
            jsonObject.put("lastModified", bufferFile.getLastModified());
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
    public static void put(Context context, String key, Object value) {
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
