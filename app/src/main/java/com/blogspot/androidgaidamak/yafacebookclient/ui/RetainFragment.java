package com.blogspot.androidgaidamak.yafacebookclient.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.blogspot.androidgaidamak.yafacebookclient.data.ImageLoader;

import okhttp3.OkHttpClient;

public class RetainFragment extends Fragment {
	private static final String TAG = "RetainFragment";
	@Nullable
	public ImageLoader mImageLoader;

	public RetainFragment() {}

	public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
		RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG);
		if (fragment == null) {
			fragment = new RetainFragment();
			fm.beginTransaction().add(fragment, TAG).commit();
		}
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(new OkHttpClient(), getActivity());
		}
	}
}
