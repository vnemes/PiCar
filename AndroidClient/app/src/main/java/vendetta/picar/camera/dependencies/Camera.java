// Copyright © 2016 Shawn Baker using the MIT License.
package vendetta.picar.camera.dependencies;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Camera implements Comparable, Parcelable {

    // instance variables
    private String network;
    public String name;
    public Source source;

    //******************************************************************************
    // Camera
    //******************************************************************************
    public Camera(Source.ConnectionType connectionType, String network, String address, int port) {
        this.network = network;
        this.name = "";
        this.source = new Source(connectionType, address, port);
        //Log.d(TAG, "address/source: " + toString());
    }

    //******************************************************************************
    // Camera
    //******************************************************************************
    private Camera(Parcel in) {
        readFromParcel(in);
        //Log.d(TAG, "parcel: " + toString());
    }

    //******************************************************************************
    // writeToParcel
    //******************************************************************************
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(network);
        dest.writeString(name);
        dest.writeParcelable(source, flags);
    }

    //******************************************************************************
    // readFromParcel
    //******************************************************************************
    private void readFromParcel(Parcel in) {
        network = in.readString();
        name = in.readString();
        source = in.readParcelable(Source.class.getClassLoader());
    }

    //******************************************************************************
    // describeContents
    //******************************************************************************
    public int describeContents() {
        return 0;
    }

    //******************************************************************************
    // Parcelable.Creator
    //******************************************************************************
    public static final Creator CREATOR = new Creator() {
        public Camera createFromParcel(Parcel in) {
            return new Camera(in);
        }

        public Camera[] newArray(int size) {
            return new Camera[size];
        }
    };

    //******************************************************************************
    // equals
    //******************************************************************************
    @Override
    public boolean equals(Object otherCamera) {
        return otherCamera instanceof Camera && compareTo(otherCamera) == 0;
    }

    //******************************************************************************
    // compareTo
    //******************************************************************************
    @Override
    public int compareTo(@NonNull Object otherCamera) {
        int result = 1;
        if (otherCamera instanceof Camera) {
            Camera camera = (Camera) otherCamera;
            result = name.compareTo(camera.name);
            if (result == 0) {
                result = source.compareTo(camera.source);
                if (result == 0) {
                    result = network.compareTo(camera.network);
                }
            }
        }
        return result;
    }

    //******************************************************************************
    // toString
    //******************************************************************************
    @Override
    public String toString() {
        return name + "," + network + "," + source.toString();
    }

}
