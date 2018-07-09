package com.upgradelibrary.data.bean;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/10 18:13
 * <p>
 * 升级选项
 */

public final class UpgradeOptions implements Parcelable {
    public static final Creator<UpgradeOptions> CREATOR = new Creator<UpgradeOptions>() {
        @Override
        public UpgradeOptions createFromParcel(Parcel in) {
            return new UpgradeOptions(in);
        }

        @Override
        public UpgradeOptions[] newArray(int size) {
            return new UpgradeOptions[size];
        }
    };
    /**
     * 通知栏图标
     */
    private final Bitmap icon;
    /**
     * 通知栏标题
     */
    private final CharSequence title;
    /**
     * 通知内容
     */
    private final CharSequence description;
    /**
     * 文件存储
     */
    private final File storage;
    /**
     * 下载链接或更新文档链接
     */
    private final String url;
    /**
     * MD5文件完整校验
     */
    private final String md5;
    /**
     * 是否支持多线程下载
     */
    private final boolean multithreadEnabled;
    /**
     * 多线程下载线程池最大数量
     */
    private final int multithreadPools;

    private UpgradeOptions(Builder builder) {
        this.icon = builder.icon;
        this.title = builder.title;
        this.description = builder.description;
        this.storage = builder.storage;
        this.url = builder.url;
        this.md5 = builder.md5;
        this.multithreadEnabled = builder.multithreadEnabled;
        this.multithreadPools = builder.multithreadPools;
    }

    protected UpgradeOptions(Parcel in) {
        icon = in.readParcelable(Bitmap.class.getClassLoader());
        title = (CharSequence) in.readValue(CharSequence.class.getClassLoader());
        description = (CharSequence) in.readValue(CharSequence.class.getClassLoader());
        storage = (File) in.readSerializable();
        url = in.readString();
        md5 = in.readString();
        multithreadEnabled = in.readByte() != 0;
        multithreadPools = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(icon, flags);
        dest.writeValue(title);
        dest.writeValue(description);
        dest.writeSerializable(storage);
        dest.writeString(url);
        dest.writeString(md5);
        dest.writeByte((byte) (multithreadEnabled ? 1 : 0));
        dest.writeInt(multithreadPools);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public CharSequence getTitle() {
        return title;
    }

    public CharSequence getDescription() {
        return description;
    }

    public File getStorage() {
        return storage;
    }

    public String getUrl() {
        return url;
    }

    public String getMd5() {
        return md5;
    }

    public boolean isMultithreadEnabled() {
        return multithreadEnabled;
    }

    public int getMultithreadPools() {
        return multithreadPools;
    }

    public static class Builder {
        private Bitmap icon;
        private CharSequence title;
        private CharSequence description;
        private File storage;
        private String url;
        private String md5;
        private boolean multithreadEnabled;
        private int multithreadPools;

        public Builder() {
        }

        public Builder setIcon(Bitmap icon) {
            this.icon = icon;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(CharSequence description) {
            this.description = description;
            return this;
        }

        public Builder setStorage(File storage) {
            this.storage = storage;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setMd5(String md5) {
            this.md5 = md5;
            return this;
        }

        public Builder setMultithreadEnabled(boolean enabled) {
            this.multithreadEnabled = enabled;
            return this;
        }

        public Builder setMultithreadPools(int pools) {
            this.multithreadPools = pools <= 0 ? 1 : pools;
            return this;
        }

        public UpgradeOptions build() {
            return new UpgradeOptions(this);
        }
    }

}
