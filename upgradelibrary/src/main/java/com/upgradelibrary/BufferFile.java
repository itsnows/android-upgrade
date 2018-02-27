package com.upgradelibrary;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/10 23:56
 * <p>
 * BufferFile
 */

public class BufferFile implements Parcelable {
    public static final int EXPIRY_DATE = 7 * 24 * 60 * 60 * 1000;
    public static final Creator<BufferFile> CREATOR = new Creator<BufferFile>() {
        @Override
        public BufferFile createFromParcel(Parcel in) {
            return new BufferFile(in);
        }

        @Override
        public BufferFile[] newArray(int size) {
            return new BufferFile[size];
        }
    };
    /**
     * 下载链接
     */
    private String url;
    /**
     * MD5文件完整校验
     */
    private String md5;
    /**
     * 文件总长度
     */
    private long length;
    /**
     * 缓存长度
     */
    private long bufferLength;
    /**
     * 分流缓存部分
     */
    private List<Part> parts;
    /**
     * 最后修改时间
     */
    private long lastModified;

    public BufferFile() {
    }

    public BufferFile(String url, String md5, long length, long bufferLength, List<Part> parts, long lastModified) {
        this.url = url;
        this.md5 = md5;
        this.length = length;
        this.bufferLength = bufferLength;
        this.parts = parts;
        this.lastModified = lastModified;
    }

    protected BufferFile(Parcel in) {
        url = in.readString();
        md5 = in.readString();
        length = in.readLong();
        bufferLength = in.readLong();
        parts = in.createTypedArrayList(Part.CREATOR);
        lastModified = in.readLong();
    }

    public static int getExpiryDate() {
        return EXPIRY_DATE;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(md5);
        dest.writeLong(length);
        dest.writeLong(bufferLength);
        dest.writeTypedList(parts);
        dest.writeLong(lastModified);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(long bufferLength) {
        this.bufferLength = bufferLength;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "BufferFile{" +
                "url='" + url + '\'' +
                ", md5='" + md5 + '\'' +
                ", length=" + length +
                ", bufferLength=" + bufferLength +
                ", parts=" + parts +
                ", lastModified=" + lastModified +
                '}';
    }

    public static class Part implements Parcelable {
        public static final Creator<Part> CREATOR = new Creator<Part>() {
            @Override
            public Part createFromParcel(Parcel in) {
                return new Part(in);
            }

            @Override
            public Part[] newArray(int size) {
                return new Part[size];
            }
        };
        private long startLength;
        private long endLength;

        public Part() {
        }

        public Part(long startLength, long endLength) {
            this.startLength = startLength;
            this.endLength = endLength;
        }

        protected Part(Parcel in) {
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
            return "Part{" +
                    "startLength=" + startLength +
                    ", endLength=" + endLength +
                    '}';
        }
    }

}
