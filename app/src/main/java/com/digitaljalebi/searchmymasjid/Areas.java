package com.digitaljalebi.searchmymasjid;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

/**
 * Created by dipesh on 25/10/16.
 */
@JsonObject
public class Areas {
    @JsonField
    public List<Area> areas;

    @JsonObject
    public static class Area implements Parcelable {
        @JsonField
        public long id;
        @JsonField
        public String name;
        @JsonField
        public String pincode;
        @JsonField
        public String zone;

        protected Area() {

        }

        protected Area(Parcel in) {
            id = in.readLong();
            name = in.readString();
            pincode = in.readString();
            zone = in.readString();
        }

        public static final Creator<Area> CREATOR = new Creator<Area>() {
            @Override
            public Area createFromParcel(Parcel in) {
                return new Area(in);
            }

            @Override
            public Area[] newArray(int size) {
                return new Area[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeString(name);
            dest.writeString(pincode);
            dest.writeString(zone);
        }
    }
}
