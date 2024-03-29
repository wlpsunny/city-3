package com.sensoro.common.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：图片信息
 * 修订历史：
 * ================================================
 */
public class ImageItem implements Serializable, Parcelable {

    public String name;       //图片的名字
    public String path;       //图片或视频 路径
    public String thumbPath;  //缩略图的路径
    public long size;         //图片的大小
    public int width;         //图片的宽度
    public int height;        //图片的高度
    public String mimeType;   //图片的类型
    public long addTime;      //图片的创建时间
    public volatile boolean fromUrl = false;
    public volatile boolean isRecord = false;

    /**
     * 图片的路径和创建时间相同就认为是同一张图片
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ImageItem) {

            ImageItem item = (ImageItem) o;
            return this.path.equalsIgnoreCase(item.path) && this.addTime == item.addTime;
//            return this.path.equalsIgnoreCase(item.path);
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        try {
            if (TextUtils.isEmpty(path)) {
                return (int) addTime;
            } else {
                return path.hashCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeString(this.thumbPath);
        dest.writeLong(this.size);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeInt(this.fromUrl ? 0 : 1);
        dest.writeInt(this.isRecord ? 0 : 1);
        dest.writeString(this.mimeType);
        dest.writeLong(this.addTime);
    }

    public ImageItem() {
    }

    protected ImageItem(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
        this.thumbPath = in.readString();
        this.size = in.readLong();
        this.width = in.readInt();
        this.height = in.readInt();
        this.fromUrl = in.readInt() == 0;
        this.isRecord = in.readInt() == 0;
        this.mimeType = in.readString();
        this.addTime = in.readLong();
    }

    public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel source) {
            return new ImageItem(source);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };
}
