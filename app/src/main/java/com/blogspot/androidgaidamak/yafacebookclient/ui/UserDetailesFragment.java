package com.blogspot.androidgaidamak.yafacebookclient.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.androidgaidamak.yafacebookclient.R;
import com.blogspot.androidgaidamak.yafacebookclient.data.Friend;
import com.facebook.FacebookActivity;

public class UserDetailesFragment extends Fragment {
	private static final String FRIEND = "friend";
	private Friend mFriend;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mFriend = getArguments().getParcelable(FRIEND);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_user_detailes, container, false);
		ImageView profileImageView = (ImageView) view.findViewById(R.id.profile_image_view);
		TextView descriptionTextView = (TextView) view.findViewById(R.id.description_text_view);
		((MainActivity) getActivity()).getImageLoader()
				.load(mFriend.getPhotoUrl(), profileImageView, getActivity());
		descriptionTextView.setText(mFriend.getName());
		return view;
	}

	public static UserDetailesFragment newInstance(Friend friend) {
		UserDetailesFragment fragment = new UserDetailesFragment();
		Bundle args = new Bundle();
		args.putParcelable(FRIEND, friend);
		fragment.setArguments(args);
		return fragment;
	}
}
