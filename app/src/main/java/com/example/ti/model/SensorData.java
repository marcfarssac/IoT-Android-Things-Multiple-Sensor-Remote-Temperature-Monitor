package com.example.ti.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorData implements Parcelable {

    private String mMagData;    // UUID_MAG_DATA_REF
    private String mBarData;    // UUID_BAR_DATA_REF
    private String mAccData;    // UUID_BAR_DATA_REF
    private String mOptData;    // UUID_OPT_DATA_REF
    private String mGyrData;    // UUID_GYR_DATA_REF
    private String mIrtDataRef; // UUID_IRT_DATA_AMB_REF
    private String mIrtDataObj; // UUID_HUM_DATA_REF2
    private String mHumData;    // UUID_HUM_DATA_REF

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SensorData createFromParcel(Parcel in) {
            return new SensorData(in);
        }

        @Override
        public SensorData[] newArray(int size) {
            return new SensorData[0];
        }
    };

    public SensorData(Parcel in) {
        this.mAccData = in.readString();
        this.mBarData = in.readString();
        this.mGyrData = in.readString();
        this.mHumData = in.readString();
        this.mIrtDataObj = in.readString();
        this.mIrtDataRef = in.readString();
        this.mMagData = in.readString();
        this.mOptData = in.readString(); // Luxometer Sensor Tag 2
    }

    public SensorData() {
    }

    public boolean allSensorsRead() {
        return mIrtDataRef!=null && mIrtDataObj!=null ;
    }

    public String getmMagData() {
        return mMagData;
    }

    public void setmMagData(String mMagData) {
        this.mMagData = mMagData;
    }

    public String getmBarData() {
        return mBarData;
    }

    public void setmBarData(String mBarData) {
        this.mBarData = mBarData;
    }

    public String getmAccData() {
        return mAccData;
    }

    public void setmAccData(String mAccData) {
        this.mAccData = mAccData;
    }

    public String getmOptData() {
        return mOptData;
    }

    public void setmOptData(String mOptData) {
        this.mOptData = mOptData;
    }

    public String getmGyrData() {
        return mGyrData;
    }

    public void setmGyrData(String mGyrData) {
        this.mGyrData = mGyrData;
    }

    public String getmIrtDataRef() {
        return mIrtDataRef;
    }

    public void setmIrtDataRef(String mIrtDataRef) {
        this.mIrtDataRef = mIrtDataRef;
    }

    public String getmIrtDataObj() {
        return mIrtDataObj;
    }

    public void setmIrtDataObj(String mIrtDataObj) {
        this.mIrtDataObj = mIrtDataObj;
    }

    public String getmHumData() {
        return mHumData;
    }

    public void setmHumData(String mHumData) {
        this.mHumData = mHumData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mAccData);
        dest.writeString(mBarData);
        dest.writeString(mGyrData);
        dest.writeString(mHumData);
        dest.writeString(mIrtDataObj);
        dest.writeString(mIrtDataRef);
        dest.writeString(mMagData);
        dest.writeString(mOptData);

    }

    @Override
    public String toString() {

        return "SensorData{" +
                "mAccData=" + mAccData +'\'' +
                "mBarData=" + mBarData +'\'' +
                "mGyrData=" + mGyrData +'\'' +
                "mHumData=" + mHumData +'\'' +
                "mIrtDataObj=" + mIrtDataObj +'\'' +
                "mIrtDataRef=" + mIrtDataRef +'\'' +
                "mMagData=" + mMagData +'\'' +
                "mOptData=" + mOptData +'\'' +
                '}';
    }


}

