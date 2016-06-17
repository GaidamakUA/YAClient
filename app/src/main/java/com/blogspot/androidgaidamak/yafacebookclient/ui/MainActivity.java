package com.blogspot.androidgaidamak.yafacebookclient.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.blogspot.androidgaidamak.yafacebookclient.R;
import com.blogspot.androidgaidamak.yafacebookclient.data.Friend;
import com.blogspot.androidgaidamak.yafacebookclient.data.ImageLoader;
import com.facebook.AccessToken;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		RetainFragment.findOrCreateRetainFragment(getSupportFragmentManager());
		if (AccessToken.getCurrentAccessToken() == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, new FacebookLoginFragment())
					.commit();
		} else {
			showFriendsListFragment();
		}
	}

	public void showFriendsListFragment() {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, new FriendsFragment())
				.commit();
	}

	public ImageLoader getImageLoader() {
		return RetainFragment.findOrCreateRetainFragment(getSupportFragmentManager()).mImageLoader;
	}

	public void showUserDetails(Friend item) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, UserDetailesFragment.newInstance(item))
				.addToBackStack(null)
				.commit();
	}
}
