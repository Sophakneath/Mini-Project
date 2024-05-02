package com.example.myapplication.utils;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

// Recognition class for holding label, confidence, and id
public class Recognition implements Parcelable {
    private String id;
    private String englishName;
    private String koreanName = "null";
    private float confidence;
    private boolean isSafe = false;
    private List<String> allergenInfo = new ArrayList<>();

    public Recognition(String id, String englishName, String koreanName, float confidence, boolean isSafe, List<String> allergenInfo) {
        this.id = id;
        this.englishName = englishName;
        this.koreanName = koreanName;
        this.confidence = confidence;
        this.isSafe = isSafe;
        this.allergenInfo = allergenInfo;
    }

    public Recognition(String id, String englishName, float confidence) {
        this.id = id;
        this.englishName = englishName;
        this.confidence = confidence;
    }

    protected Recognition(Parcel in) {
        id = in.readString();
        englishName = in.readString();
        koreanName = in.readString();
        confidence = in.readFloat();
        isSafe = in.readByte() != 0;
        allergenInfo = in.createStringArrayList();
    }

    public static final Creator<Recognition> CREATOR = new Creator<Recognition>() {
        @Override
        public Recognition createFromParcel(Parcel in) {
            return new Recognition(in);
        }

        @Override
        public Recognition[] newArray(int size) {
            return new Recognition[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public float getConfidence() {
        return confidence;
    }

    public boolean getSafe() {
        return isSafe;
    }

    public void setConfidence(float confidence) { this.confidence = confidence; }

    public List<String> getAllergenInfo() { return allergenInfo; }

    @NonNull
    @Override
    public String toString() {
        return "Recognition{" +
                "id='" + id + '\'' +
                ", label='" + englishName + '\'' +
                ", confidence=" + confidence +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(englishName);
        parcel.writeString(koreanName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(isSafe);
        }
        parcel.writeFloat(confidence);
        parcel.writeStringList(allergenInfo);
    }
}