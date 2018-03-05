package com.upgradelibrary.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2017/10/13 9:13
 * <p>
 * 应用更新实体
 */

public class Upgrade implements Parcelable {
    /**
     * 更新模式 普通
     */
    public static final int UPGRADE_MODE_COMMON = 1;
    /**
     * 更新模式 强制
     */
    public static final int UPGRADE_MODE_FORCED = 2;
    /**
     * 更新模式 灰度
     */
    public static final int UPGRADE_MODE_PARTIAL = 3;
    public static final Creator<Upgrade> CREATOR = new Creator<Upgrade>() {
        @Override
        public Upgrade createFromParcel(Parcel in) {
            return new Upgrade(in);
        }

        @Override
        public Upgrade[] newArray(int size) {
            return new Upgrade[size];
        }
    };
    private static final int CONNECT_TIMEOUT = 2000;
    private static final int READ_TIMEOUT = 2000;
    /**
     * 灰度升级模式根据设备序列号更新
     */
    private List<String> device;
    /**
     * 更新日期
     */
    private String date;
    /**
     * 更新模式
     */
    private int mode;
    /**
     * 更新说明
     */
    private List<String> logs;
    /**
     * 新版App版本号
     */
    private int versionCode;
    /**
     * 新版App版本名称
     */
    private String versionName;
    /**
     * 新版App下载链接
     */
    private String dowanloadUrl;
    /**
     * 安装包MD5效验
     */
    private String md5;

    private String sha1;

    private String sha256;

    public Upgrade() {
    }

    public Upgrade(List<String> device, String date, Integer mode, List<String> logs, Integer versionCode, String versionName, String dowanloadUrl, String md5) {
        this.device = device;
        this.date = date;
        this.mode = mode;
        this.logs = logs;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.dowanloadUrl = dowanloadUrl;
        this.md5 = md5;
    }

    protected Upgrade(Parcel in) {
        device = in.createStringArrayList();
        date = in.readString();
        mode = in.readInt();
        logs = in.createStringArrayList();
        versionCode = in.readInt();
        versionName = in.readString();
        dowanloadUrl = in.readString();
        md5 = in.readString();
    }

    /**
     * 解析更新文档
     *
     * @param url 更新文档链接
     * @return
     * @throws Exception
     */
    public static Upgrade parser(String url) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new ConnectException();
            }
            return parser(connection.getInputStream());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 解析更新文档
     *
     * @param inputStream 更新文档数据流
     */
    public static Upgrade parser(InputStream inputStream) throws Exception {
        Upgrade appUpdate = null;
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(inputStream, "UTF-8");
            int event = xmlPullParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        appUpdate = new Upgrade();
                        break;
                    case XmlPullParser.START_TAG:
                        if ("device".equals(xmlPullParser.getName())) {
                            appUpdate.setDevice(new ArrayList<String>(0));
                        } else if ("sn".equals(xmlPullParser.getName())) {
                            appUpdate.getDevice().add(xmlPullParser.nextText().trim());
                        } else if ("date".equals(xmlPullParser.getName())) {
                            appUpdate.setDate(xmlPullParser.nextText().trim());
                        } else if ("mode".equals(xmlPullParser.getName())) {
                            appUpdate.setMode(Integer.parseInt(xmlPullParser.nextText().trim()));
                        } else if ("log".equals(xmlPullParser.getName())) {
                            appUpdate.setLogs(new ArrayList<String>(0));
                        } else if ("item".equals(xmlPullParser.getName())) {
                            appUpdate.getLogs().add(xmlPullParser.nextText().trim());
                        } else if ("versionCode".equals(xmlPullParser.getName())) {
                            appUpdate.setVersionCode(Integer.parseInt(xmlPullParser.nextText().trim()));
                        } else if ("versionName".equals(xmlPullParser.getName())) {
                            appUpdate.setVersionName(xmlPullParser.nextText().trim());
                        } else if ("dowanloadUrl".equals(xmlPullParser.getName())) {
                            appUpdate.setDowanloadUrl(xmlPullParser.nextText().trim());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                    default:
                        break;
                }
                event = xmlPullParser.next();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return appUpdate;
    }

    public List<String> getDevice() {
        return device;
    }

    public void setDevice(List<String> device) {
        this.device = device;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDowanloadUrl() {
        return dowanloadUrl;
    }

    public void setDowanloadUrl(String dowanloadUrl) {
        this.dowanloadUrl = dowanloadUrl;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String toString() {
        return "Upgrade{" +
                "device=" + device +
                ", date='" + date + '\'' +
                ", mode=" + mode +
                ", logs=" + logs +
                ", versionCode=" + versionCode +
                ", versionName='" + versionName + '\'' +
                ", dowanloadUrl='" + dowanloadUrl + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(device);
        dest.writeString(date);
        dest.writeInt(mode);
        dest.writeStringList(logs);
        dest.writeInt(versionCode);
        dest.writeString(versionName);
        dest.writeString(dowanloadUrl);
        dest.writeString(md5);
    }
}
