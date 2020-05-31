package com.itsnows.upgrade.model.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/8 21:09
 * <p>
 * UpgradeIgnored
 */
public class UpgradeVersion implements Parcelable {

    public static final Creator<UpgradeVersion> CREATOR = new Creator<UpgradeVersion>() {
        @Override
        public UpgradeVersion createFromParcel(Parcel in) {
            return new UpgradeVersion(in);
        }

        @Override
        public UpgradeVersion[] newArray(int size) {
            return new UpgradeVersion[size];
        }
    };
    /**
     * 版本
     */
    private int version;
    /**
     * 是否忽略
     */
    private boolean isIgnored;

    protected UpgradeVersion(Parcel in) {
        version = in.readInt();
        isIgnored = in.readByte() != 0;
    }

    public UpgradeVersion() {
    }

    public UpgradeVersion(int version, boolean isIgnored, boolean isInstall) {
        this.version = version;
        this.isIgnored = isIgnored;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isIgnored() {
        return isIgnored;
    }

    public void setIgnored(boolean ignored) {
        isIgnored = ignored;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(version);
        dest.writeByte((byte) (isIgnored ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
