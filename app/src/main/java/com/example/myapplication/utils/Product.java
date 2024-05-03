package com.example.myapplication.utils;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Product implements Parcelable {
    private int id;
    private String englishName;
    private String koreanName = "null";
    private float confidence;
    private boolean isSafe = true;
    @Exclude
    private List<String> allergenList = new ArrayList<>();
    public Product(){}
    public Product(int id, String englishName, String koreanName, float confidence, boolean isSafe, List<String> allergenList) {
        this.id = id;
        this.englishName = englishName;
        this.koreanName = koreanName;
        this.confidence = confidence;
        this.isSafe = isSafe;
        this.allergenList = allergenList;
    }

    public Product(int id, String englishName, float confidence) {
        this.id = id;
        this.englishName = englishName;
        this.confidence = confidence;
    }

    protected Product(Parcel in) {
        id = in.readInt();
        englishName = in.readString();
        koreanName = in.readString();
        confidence = in.readFloat();
        isSafe = in.readByte() != 0;
        allergenList = in.createStringArrayList();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public Integer getId() {
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

    public List<String> getAllergenList() { return allergenList; }

    public void setAllergenList(ArrayList<String> info) {
        this.allergenList = info;
    }

    public void setIsSafe(boolean isSafe) { this.isSafe = isSafe; }

    @NonNull
    @Override
    public String toString() {
        return "Recognition{" +
                "id='" + id + '\'' +
                ", englishName='" + englishName + '\'' +
                ", koreanName='" + koreanName + '\'' +
                ", isSafe='" + isSafe + '\'' +
                ", allergenInfo='" + allergenList.size() + '\'' +
                ", confidence=" + confidence +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(englishName);
        parcel.writeString(koreanName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(isSafe);
        }
        parcel.writeFloat(confidence);
        parcel.writeStringList(allergenList);
    }
}