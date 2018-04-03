package com.upgradelibrary.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2017/10/13 9:13
 * <p>
 * 应用更新实体
 */

public class Upgrade implements Parcelable {
    private static final int CONNECT_TIMEOUT = 6 * 1000;
    private static final int READ_TIMEOUT = 6 * 1000;
    /**
     * 更新模式 普通
     */
    public static final int UPGRADE_MODE_COMMON = 1;
    /**
     * 更新模式 强制
     */
    public static final int UPGRADE_MODE_FORCED = 2;

    /**
     * 稳定版
     */
    private Stable stable;
    /**
     * 测试版
     */
    private Bate bate;

    protected Upgrade(Parcel in) {
        stable = in.readParcelable(Stable.class.getClassLoader());
        bate = in.readParcelable(Bate.class.getClassLoader());
    }

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
                                } else if ("dowanloadUrl".equals(childStableNode.getNodeName())) {
                                    String text = childStableNode.getTextContent();
                                    upgrade.getStable().setDowanloadUrl(text == null ? text : text.trim());
                                } else if ("md5".equals(childStableNode.getNodeName())) {
                                    String text = childStableNode.getTextContent();
                                    upgrade.getStable().setMd5(text == null ? text : text.trim());
                                }
                            }
                        } else if ("beta".equals(stableNode.getNodeName())) {
                            upgrade.setBate(new Bate());
                            NodeList betaNodeList = stableNode.getChildNodes();
                            for (int k = 0; k < betaNodeList.getLength(); k++) {
                                Node childBetaNode = betaNodeList.item(k);
                                if (childBetaNode == null) {
                                    continue;
                                }
                                if ("device".equals(childBetaNode.getNodeName())) {
                                    NodeList deviceNodeList = childBetaNode.getChildNodes();
                                    upgrade.getBate().setDevice(new ArrayList<String>(0));
                                    for (int l = 0; deviceNodeList != null && l < deviceNodeList.getLength(); l++) {
                                        Node deviceChildNode = deviceNodeList.item(l);
                                        if (deviceChildNode == null) {
                                            continue;
                                        }
                                        if ("sn".equals(deviceChildNode.getNodeName())) {
                                            String text = deviceChildNode.getTextContent();
                                            upgrade.getBate().getDevice().add(text == null ? text : text.trim());
                                        }
                                    }
                                } else if ("date".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBate().setDate(text == null ? text : text.trim());
                                } else if ("mode".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBate().setMode(text == null ? 0 : Integer.parseInt(text.trim()));
                                } else if ("log".equals(childBetaNode.getNodeName())) {
                                    NodeList logNodeList = childBetaNode.getChildNodes();
                                    upgrade.getBate().setLogs(new ArrayList<String>(0));
                                    for (int l = 0; logNodeList != null && l < logNodeList.getLength(); l++) {
                                        Node logChildNode = logNodeList.item(l);
                                        if (logChildNode == null) {
                                            continue;
                                        }
                                        if ("item".equals(logChildNode.getNodeName())) {
                                            String text = logChildNode.getTextContent();
                                            upgrade.getBate().getLogs().add(text == null ? text : text.trim());
                                        }
                                    }
                                } else if ("versionCode".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBate().setVersionCode(text == null ? 0 : Integer.parseInt(text.trim()));
                                } else if ("versionName".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBate().setVersionName(text == null ? text : text.trim());
                                } else if ("dowanloadUrl".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBate().setDowanloadUrl(text == null ? text : text.trim());
                                } else if ("md5".equals(childBetaNode.getNodeName())) {
                                    String text = childBetaNode.getTextContent();
                                    upgrade.getBate().setMd5(text == null ? text : text.trim());
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

    private Upgrade() {
    }

    public Upgrade(Stable stable, Bate bate) {
        this.stable = stable;
        this.bate = bate;
    }

    public Stable getStable() {
        return stable;
    }

    public void setStable(Stable stable) {
        this.stable = stable;
    }

    public Bate getBate() {
        return bate;
    }

    public void setBate(Bate bate) {
        this.bate = bate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(stable, flags);
        dest.writeParcelable(bate, flags);
    }

    @Override
    public String toString() {
        return "Upgrade{" +
                "stable=" + stable +
                ", bate=" + bate +
                '}';
    }

    /**
     * 稳定版
     */
    public static class Stable implements Parcelable {

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

        public Stable() {
        }

        public Stable(String date, int mode, List<String> logs, int versionCode, String versionName, String dowanloadUrl, String md5) {
            this.date = date;
            this.mode = mode;
            this.logs = logs;
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.dowanloadUrl = dowanloadUrl;
            this.md5 = md5;
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

        protected Stable(Parcel in) {
            date = in.readString();
            mode = in.readInt();
            logs = in.createStringArrayList();
            versionCode = in.readInt();
            versionName = in.readString();
            dowanloadUrl = in.readString();
            md5 = in.readString();
        }

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
            dest.writeString(dowanloadUrl);
            dest.writeString(md5);
        }

        @Override
        public String toString() {
            return "Stable{" +
                    "date='" + date + '\'' +
                    ", mode=" + mode +
                    ", logs=" + logs +
                    ", versionCode=" + versionCode +
                    ", versionName='" + versionName + '\'' +
                    ", dowanloadUrl='" + dowanloadUrl + '\'' +
                    ", md5='" + md5 + '\'' +
                    '}';
        }
    }

    /**
     * 测试版
     */
    public static class Bate implements Parcelable {
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
        private String dowanloadUrl;
        /**
         * 安装包MD5效验
         */
        private String md5;

        public Bate() {
        }

        public Bate(List<String> device, String date, int mode, List<String> logs, int versionCode, String versionName, String dowanloadUrl, String md5) {
            this.device = device;
            this.date = date;
            this.mode = mode;
            this.logs = logs;
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.dowanloadUrl = dowanloadUrl;
            this.md5 = md5;
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

        protected Bate(Parcel in) {
            device = in.createStringArrayList();
            date = in.readString();
            mode = in.readInt();
            logs = in.createStringArrayList();
            versionCode = in.readInt();
            versionName = in.readString();
            dowanloadUrl = in.readString();
            md5 = in.readString();
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

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Bate> CREATOR = new Creator<Bate>() {
            @Override
            public Bate createFromParcel(Parcel in) {
                return new Bate(in);
            }

            @Override
            public Bate[] newArray(int size) {
                return new Bate[size];
            }
        };

        @Override
        public String toString() {
            return "Bate{" +
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
    }
}
