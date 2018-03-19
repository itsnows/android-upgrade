package com.upgradelibrary.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/10 23:56
 * <p>
 * UpgradeBuffer
 */

public class UpgradeBuffer implements Parcelable {
    /**
     * 升级缓存有效期
     */
    public static final int EXPIRY_DATE = 7 * 24 * 60 * 60 * 1000;
    public static final Creator<UpgradeBuffer> CREATOR = new Creator<UpgradeBuffer>() {
        @Override
        public UpgradeBuffer createFromParcel(Parcel in) {
            return new UpgradeBuffer(in);
        }

        @Override
        public UpgradeBuffer[] newArray(int size) {
            return new UpgradeBuffer[size];
        }
    };
    /**
     * 下载链接
     */
    private String downloadUrl;
    /**
     * MD5文件完整校验
     */
    private String fileMd5;
    /**
     * 文件总长度
     */
    private long fileLength;
    /**
     * 缓存长度
     */
    private long bufferLength;
    /**
     * 分流段部分
     */
    private List<ShuntPart> shuntParts;
    /**
     * 最后修改时间
     */
    private long lastModified;

    public UpgradeBuffer() {
    }

    public UpgradeBuffer(String downloadUrl, String fileMd5, long fileLength, long bufferLength, List<ShuntPart> shuntParts, long lastModified) {
        this.downloadUrl = downloadUrl;
        this.fileMd5 = fileMd5;
        this.fileLength = fileLength;
        this.bufferLength = bufferLength;
        this.shuntParts = shuntParts;
        this.lastModified = lastModified;
    }

    protected UpgradeBuffer(Parcel in) {
        downloadUrl = in.readString();
        fileMd5 = in.readString();
        fileLength = in.readLong();
        bufferLength = in.readLong();
        shuntParts = in.createTypedArrayList(ShuntPart.CREATOR);
        lastModified = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(downloadUrl);
        dest.writeString(fileMd5);
        dest.writeLong(fileLength);
        dest.writeLong(bufferLength);
        dest.writeTypedList(shuntParts);
        dest.writeLong(lastModified);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public long getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(long bufferLength) {
        this.bufferLength = bufferLength;
    }

    public List<ShuntPart> getShuntParts() {
        return shuntParts;
    }

    public void setShuntParts(List<ShuntPart> shuntParts) {
        this.shuntParts = shuntParts;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "UpgradeBuffer{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", fileMd5='" + fileMd5 + '\'' +
                ", fileLength=" + fileLength +
                ", bufferLength=" + bufferLength +
                ", shuntParts=" + shuntParts +
                ", lastModified=" + lastModified +
                '}';
    }

    public static class ShuntPart implements Parcelable {
        public static final Creator<ShuntPart> CREATOR = new Creator<ShuntPart>() {
            @Override
            public ShuntPart createFromParcel(Parcel in) {
                return new ShuntPart(in);
            }

            @Override
            public ShuntPart[] newArray(int size) {
                return new ShuntPart[size];
            }
        };
        private long startLength;
        private long endLength;

        public ShuntPart() {
        }

        public ShuntPart(long startLength, long endLength) {
            this.startLength = startLength;
            this.endLength = endLength;
        }

        protected ShuntPart(Parcel in) {
            startLength = in.readLong();
            endLength = in.readLong();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(startLength);
            dest.writeLong(endLength);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public long getStartLength() {
            return startLength;
        }

        public void setStartLength(long startLength) {
            this.startLength = startLength;
        }

        public long getEndLength() {
            return endLength;
        }

        public void setEndLength(long endLength) {
            this.endLength = endLength;
        }

        @Override
        public String toString() {
            return "ShuntPart{" +
                    "startLength=" + startLength +
                    ", endLength=" + endLength +
                    '}';
        }
    }

}
