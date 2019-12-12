package com.itsnows.upgrade.model.bean;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;

import java.io.File;

/**
 * Author: itsnows
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
     * 对话框主题
     */
    private final int theme;
    /**
     * 通知栏图标
     */
    private final Bitmap icon;
    /**
     * 通知栏标题
     */
    private final CharSequence title;
    /**
     * 通知栏描述
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
     * 多线程下载线程池大小
     */
    private final int multithreadPools;

    /**
     * 是否支持多线程下载
     */
    private final boolean multithreadEnabled;

    /**
     * 是否自动安装安装包
     */
    private final boolean automountEnabled;

    /**
     * 是否自动清除安装包
     */
    private final boolean autocleanEnabled;

    private UpgradeOptions(Params params) {
        theme = params.theme;
        icon = params.icon;
        title = params.title;
        description = params.description;
        storage = params.storage;
        url = params.url;
        md5 = params.md5;
        multithreadPools = params.multithreadPools;
        multithreadEnabled = params.multithreadEnabled;
        automountEnabled = params.automountEnabled;
        autocleanEnabled = params.autocleanEnabled;
    }

    protected UpgradeOptions(Parcel in) {
        theme = in.readInt();
        icon = in.readParcelable(Bitmap.class.getClassLoader());
        title = (CharSequence) in.readValue(CharSequence.class.getClassLoader());
        description = (CharSequence) in.readValue(CharSequence.class.getClassLoader());
        storage = (File) in.readSerializable();
        url = in.readString();
        md5 = in.readString();
        multithreadPools = in.readInt();
        multithreadEnabled = in.readByte() != 0;
        automountEnabled = in.readByte() != 0;
        autocleanEnabled = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(theme);
        dest.writeParcelable(icon, flags);
        dest.writeValue(title);
        dest.writeValue(description);
        dest.writeSerializable(storage);
        dest.writeString(url);
        dest.writeString(md5);
        dest.writeInt(multithreadPools);
        dest.writeByte((byte) (multithreadEnabled ? 1 : 0));
        dest.writeInt((byte) (automountEnabled ? 1 : 0));
        dest.writeInt((byte) (autocleanEnabled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getTheme() {
        return theme;
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

    public int getMultithreadPools() {
        return multithreadPools;
    }

    public boolean isMultithreadEnabled() {
        return multithreadEnabled;
    }

    public boolean isAutomountEnabled() {
        return automountEnabled;
    }

    public boolean isAutocleanEnabled() {
        return autocleanEnabled;
    }

    public static class Builder {
        private Params params;

        public Builder() {
            params = new Params();
        }

        public Builder setTheme(@ColorInt int theme) {
            params.theme = theme;
            return this;
        }

        public Builder setIcon(Bitmap icon) {
            params.icon = icon;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            params.title = title;
            return this;
        }

        public Builder setDescription(CharSequence description) {
            params.description = description;
            return this;
        }

        public Builder setStorage(File storage) {
            params.storage = storage;
            return this;
        }

        public Builder setUrl(String url) {
            params.url = url;
            return this;
        }

        public Builder setMd5(String md5) {
            params.md5 = md5;
            return this;
        }

        public Builder setMultithreadPools(int pools) {
            params.multithreadPools = Math.max(pools, 0);
            return this;
        }

        public Builder setMultithreadEnabled(boolean enabled) {
            params.multithreadEnabled = enabled;
            return this;
        }

        public Builder setAutomountEnabled(boolean enabled) {
            params.automountEnabled = enabled;
            return this;
        }

        public Builder setAutocleanEnabled(boolean enabled) {
            params.autocleanEnabled = enabled;
            return this;
        }

        public UpgradeOptions build() {
            return new UpgradeOptions(params);
        }
    }

    private static class Params {
        private int theme;
        private Bitmap icon;
        private CharSequence title;
        private CharSequence description;
        private File storage;
        private String url;
        private String md5;
        private int multithreadPools;
        private boolean multithreadEnabled;
        private boolean automountEnabled;
        private boolean autocleanEnabled;
    }

}
