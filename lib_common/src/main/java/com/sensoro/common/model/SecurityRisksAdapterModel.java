package com.sensoro.common.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.sensoro.common.R;

import java.io.Serializable;
import java.util.ArrayList;

public class SecurityRisksAdapterModel implements Serializable, Parcelable {
    @Expose(serialize = false, deserialize = false)
    public int locationColor = R.color.c_a6a6a6;
    @Expose(serialize = false, deserialize = false)
    public int behaviorColor = R.color.c_a6a6a6;
    @Expose
    public boolean locationIsBold = false;
    @Expose
    public boolean behaviorIsBold  = false;
    //

    public String place;
    public ArrayList<String> action = new ArrayList<>();


    public SecurityRisksAdapterModel() {

    }

    protected SecurityRisksAdapterModel(Parcel in) {
        locationColor = in.readInt();
        behaviorColor = in.readInt();
        place = in.readString();
        action = in.createStringArrayList();
        locationIsBold = in.readByte() != 0;
        behaviorIsBold = in.readByte() != 0;
    }

    public static final Creator<SecurityRisksAdapterModel> CREATOR = new Creator<SecurityRisksAdapterModel>() {
        @Override
        public SecurityRisksAdapterModel createFromParcel(Parcel in) {
            return new SecurityRisksAdapterModel(in);
        }

        @Override
        public SecurityRisksAdapterModel[] newArray(int size) {
            return new SecurityRisksAdapterModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(locationColor);
        dest.writeInt(behaviorColor);
        dest.writeString(place);
        dest.writeStringList(action);
        dest.writeByte((byte) (locationIsBold ? 1 : 0));
        dest.writeByte((byte) (behaviorIsBold ? 1 : 0));
    }


}
