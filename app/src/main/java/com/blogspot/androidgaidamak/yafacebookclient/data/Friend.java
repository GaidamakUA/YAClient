package com.blogspot.androidgaidamak.yafacebookclient.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Friend implements Parcelable {
	private final String id;
	private final String name;
	private final String photoUrl;

	public Friend(String id, String name, String photoUrl) {
		this.id = id;
		this.name = name;
		this.photoUrl = photoUrl;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.id);
		dest.writeString(this.name);
		dest.writeString(this.photoUrl);
	}

	protected Friend(Parcel in) {
		this.id = in.readString();
		this.name = in.readString();
		this.photoUrl = in.readString();
	}

	public static final Parcelable.Creator<Friend> CREATOR = new Parcelable.Creator<Friend>() {
		@Override
		public Friend createFromParcel(Parcel source) {
			return new Friend(source);
		}

		@Override
		public Friend[] newArray(int size) {
			return new Friend[size];
		}
	};
}
