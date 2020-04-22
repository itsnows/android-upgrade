package com.itsnows.upgrade.model.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/1/13 9:13
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
    private static final int CONNECT_TIMEOUT = 20 * 1000;
    private static final int READ_TIMEOUT = 20 * 1000;
    /**
     * 稳定版
     */
    private Stable stable;
    /**
     * 测试版
     */
    private Beta beta;

    protected Upgrade(Parcel in) {
        stable = in.readParcelable(Stable.class.getClassLoader());
        beta = in.readParcelable(Beta.class.getClassLoader());
    }

    private Upgrade() {
    }

    public Upgrade(Stable stable, Beta beta) {
        this.stable = stable;
        this.beta = beta;
    }

    /**
     * 解析更新文档
     *
     * @param url 更新文档链接
     * @return
     * @throws Exception
     */
    public static Upgrade parser(String url) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setDoOutput(false);
            conn.setUseCaches(false);
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new ConnectException();
            }
            InputStream readerStream = conn.getInputStream();
            ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
            int len = -1;
            int tmp = -1;
            byte[] buf = new byte[8096];
            while ((tmp = readerStream.read(buf)) != -1) {
                len += tmp;
                if (len > 1024 * 1024) {
                    throw new IOException("Update document content length cannot greater than 1024KB.");
                }
                bufferStream.write(buf, 0, tmp);
            }
            readerStream.close();
            byte[] buffer = bufferStream.toByteArray();
            String str = new String(buffer, StandardCharsets.UTF_8).trim();
            str = str.substring(0, Math.min(str.length(), 2));
            if (str.contains("[") || str.contains("{")) {
                return parserJson(new ByteArrayInputStream(buffer));
            } else if (str.contains("<")) {
                return parserXml(new ByteArrayInputStream(buffer));
            }
            throw new IOException("Upgrade document format not supported.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 解析更新文档
     *
     * @param inputStream 更新文档数据流
     */
    public static Upgrade parserXml(InputStream inputStream) throws Exception {
        Upgrade upgrade = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            if (document != null) {
                upgrade = new Upgrade();
                NodeList nodeList = document.getChildNodes();
                for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node == null) {
                        continue;
                    }
                    NodeList androidNodeList = node.getChildNodes();
                    for (int j = 0; androidNodeList != null && j < androidNodeList.getLength(); j++) {
                        Node stableNode = androidNodeList.item(j);
                        if ("stable".equals(stableNode.getNodeName())) {
                            upgrade.setStable(new Stable());
                            NodeList stableNodeList = stableNode.getChildNodes();
                            for (int k = 0; k < stableNodeList.getLength(); k++) {
                                Node childStableNode = stableNodeList.item(k);
                                if (childStableNode == null) {
                                    continue;
                                }
                                if ("date".equals(childStableNode.getNodeName())) {
                                    String text = childStableNode.getTextContent();
                                    upgrade.getStable().setDate(text == null ? text : text.trim());
                                } else if ("mode".equals(childStableNode.getNodeName())) {
                                    String text = childStableNode.getTextContent();
                                    upgrade.getStable().setMode(text == null ? 0 : Integer.parseInt(text.trim()));
                                } else if ("log".equals(childStableNode.getNodeName())) {
                                    NodeList logNodeList = childStableNode.getChildNodes();
                                    upgrade.getStable().setLogs(new ArrayList<String>(0));
                                    for (int l = 0; logNodeList != null && l < logNodeList.getLength(); l++) {
                                        Node logChildNode = logNodeList.item(l);
                                        if (logChildNode == null) {
                                            continue;
                                        }
                                        if ("item".equals(logChildNode.getNodeName())) {
                                            String text = logChildNode.getTextContent();
                                            upgrade.getStable().getLogs().add(text == null ? text : text.trim());
                                        }
                                    }
                                } else if ("versionCode".equals(childStableNode.getNodeName())) {
                                    String text = childStableNode.getTextContent();
                                    upgrade.getStable().setVersionCode(text == null ? 0 : Integer.parseInt(text.trim()));
                                } else if ("versionName".equals(childStableNode.getNodeName())) {
                                    String text = childStableNode.getTextContent();
                                    upgrade.getStable().setVersionName(text == null ? text : text.trim());
                                } else if ("downloadUrl".equals(childStableNode.getNodeName())) {
                                    String text = childStableNode.getTextContent();
                                    upgrade.getStable().setDownloadUrl(text == null ? text : text.trim());
                                } else if ("md5".equals(childStableNode.getNodeName())) {
                                    String text = childStableNode.getTextContent();
                                    upgrade.getStable().setMd5((text == null || text.isEmpty() ? null : text.trim()));
                                }
                            }
                        } else if ("beta".equals(stableNode.getNodeName())) {
                            upgrade.setBeta(new Beta());
                            NodeList betaNodeList = stableNode.getChildNodes();
                            for (int k = 0; k < betaNodeList.getLength(); k++) {
                                Node childBetaNode = betaNodeList.item(k);
                                if (childBetaNode == null) {
                                    continue;
                                }
                                if ("device".equals(childBetaNode.getNodeName())) {
                                    NodeList deviceNodeList = childBetaNode.getChildNodes();
                                    upgrade.getBeta().setDevice(new ArrayList<String>(0));
                                    for (int l = 0; deviceNodeList != null && l < deviceNodeList.getLength(); l++) {
                                        Node deviceChildNode = deviceNodeList.item(l);
                                        if (deviceChildNode == null) {
                                            continue;
                                        }
                                        if ("sn".equals(deviceChildNode.getNodeName())) {
                                            String text = deviceChildNode.getTextContent();
                                            upgrade.getBeta().getDevice().add(text == null ? text : text.trim());
                                        }
                                    }
                                } else if ("date".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBeta().setDate(text == null ? text : text.trim());
                                } else if ("mode".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBeta().setMode(text == null ? 0 : Integer.parseInt(text.trim()));
                                } else if ("log".equals(childBetaNode.getNodeName())) {
                                    NodeList logNodeList = childBetaNode.getChildNodes();
                                    upgrade.getBeta().setLogs(new ArrayList<String>(0));
                                    for (int l = 0; logNodeList != null && l < logNodeList.getLength(); l++) {
                                        Node logChildNode = logNodeList.item(l);
                                        if (logChildNode == null) {
                                            continue;
                                        }
                                        if ("item".equals(logChildNode.getNodeName())) {
                                            String text = logChildNode.getTextContent();
                                            upgrade.getBeta().getLogs().add(text == null ? text : text.trim());
                                        }
                                    }
                                } else if ("versionCode".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBeta().setVersionCode(text == null ? 0 : Integer.parseInt(text.trim()));
                                } else if ("versionName".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBeta().setVersionName(text == null ? text : text.trim());
                                } else if ("downloadUrl".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBeta().setDownloadUrl(text == null ? text : text.trim());
                                } else if ("md5".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBeta().setMd5((text == null || text.isEmpty() ? null : text.trim()));
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return upgrade;
    }

    /**
     * 解析更新文档
     *
     * @param inputStream
     * @return
     */
    public static Upgrade parserJson(InputStream inputStream) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = null;
            StringBuilder json = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                json.append(line);
            }
            JSONObject android = new JSONObject(json.toString()).getJSONObject("android");
            JSONObject androidStable = android.getJSONObject("stable");
            JSONObject androidBeta = android.getJSONObject("beta");
            Upgrade upgrade = new Upgrade();
            if (androidStable != null) {
                Stable stable = new Stable();
                stable.setDate(androidStable.getString("date").trim());
                stable.setMode(androidStable.getInt("mode"));
                stable.setLogs(new ArrayList<String>());
                JSONArray log = androidStable.getJSONArray("log");
                for (int i = 0; log != null && i < log.length(); i++) {
                    stable.getLogs().add(log.getString(i).trim());
                }
                stable.setVersionCode(androidStable.getInt("versionCode"));
                stable.setVersionName(androidStable.getString("versionName").trim());
                stable.setDownloadUrl(androidStable.getString("downloadUrl").trim());
                stable.setMd5(androidStable.getString("md5"));
                stable.setMd5("".equals(stable.getMd5())
                        || "null".equals(stable.getMd5()) ? null : stable.getMd5());
                upgrade.setStable(stable);
            }
            if (androidBeta != null) {
                Beta beta = new Beta();
                beta.setDevice(new ArrayList<String>());
                JSONArray device = androidBeta.getJSONArray("device");
                for (int i = 0; device != null && i < device.length(); i++) {
                    beta.getDevice().add(device.getString(i));
                }
                beta.setDate(androidBeta.getString("date").trim());
                beta.setMode(androidBeta.getInt("mode"));
                beta.setLogs(new ArrayList<String>());
                JSONArray log = androidBeta.getJSONArray("log");
                for (int i = 0; log != null && i < log.length(); i++) {
                    beta.getLogs().add(log.getString(i).trim());
                }
                beta.setVersionCode(androidBeta.getInt("versionCode"));
                beta.setVersionName(androidBeta.getString("versionName").trim());
                beta.setDownloadUrl(androidBeta.getString("downloadUrl").trim());
                beta.setMd5(androidBeta.getString("md5"));
                beta.setMd5("".equals(beta.getMd5())
                        || "null".equals(beta.getMd5()) ? null : beta.getMd5());
                upgrade.setBeta(beta);
            }
            return upgrade;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Stable getStable() {
        return stable;
    }

    public void setStable(Stable stable) {
        this.stable = stable;
    }

    public Beta getBeta() {
        return beta;
    }

    public void setBeta(Beta beta) {
        this.beta = beta;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(stable, flags);
        dest.writeParcelable(beta, flags);
    }

    public static class Stable implements Parcelable {

        public static final Creator<Stable> CREATOR = new Creator<Stable>() {
            @Override
            public Stable createFromParcel(Parcel in) {
                return new Stable(in);
            }

            @Override
            public Stable[] newArray(int size) {
                return new Stable[size];
            }
        };
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
        private String downloadUrl;
        /**
         * 安装包MD5效验
         */
        private String md5;

        public Stable() {
        }

        public Stable(String date, int mode, List<String> logs, int versionCode, String versionName, String dowanloadUrl, String md5) {
            this.date = date;
            this.mode = mode;
            this.logs = logs;
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.downloadUrl = dowanloadUrl;
            this.md5 = md5;
        }

        protected Stable(Parcel in) {
            date = in.readString();
            mode = in.readInt();
            logs = in.createStringArrayList();
            versionCode = in.readInt();
            versionName = in.readString();
            downloadUrl = in.readString();
            md5 = in.readString();
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public List<String> getLogs() {
            return logs;
        }

        public void setLogs(List<String> logs) {
            this.logs = logs;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(date);
            dest.writeInt(mode);
            dest.writeStringList(logs);
            dest.writeInt(versionCode);
            dest.writeString(versionName);
            dest.writeString(downloadUrl);
            dest.writeString(md5);
        }

    }

    public static class Beta implements Parcelable {
        public static final Creator<Beta> CREATOR = new Creator<Beta>() {
            @Override
            public Beta createFromParcel(Parcel in) {
                return new Beta(in);
            }

            @Override
            public Beta[] newArray(int size) {
                return new Beta[size];
            }
        };
        /**
         * 测试版设备序列号
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
        private String downloadUrl;
        /**
         * 安装包MD5效验
         */
        private String md5;

        public Beta() {
        }

        public Beta(List<String> device, String date, int mode, List<String> logs, int versionCode, String versionName, String dowanloadUrl, String md5) {
            this.device = device;
            this.date = date;
            this.mode = mode;
            this.logs = logs;
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.downloadUrl = dowanloadUrl;
            this.md5 = md5;
        }

        protected Beta(Parcel in) {
            device = in.createStringArrayList();
            date = in.readString();
            mode = in.readInt();
            logs = in.createStringArrayList();
            versionCode = in.readInt();
            versionName = in.readString();
            downloadUrl = in.readString();
            md5 = in.readString();
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

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public List<String> getLogs() {
            return logs;
        }

        public void setLogs(List<String> logs) {
            this.logs = logs;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringList(device);
            dest.writeString(date);
            dest.writeInt(mode);
            dest.writeStringList(logs);
            dest.writeInt(versionCode);
            dest.writeString(versionName);
            dest.writeString(downloadUrl);
            dest.writeString(md5);
        }

        @Override
        public int describeContents() {
            return 0;
        }

    }
}
