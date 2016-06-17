package com.blogspot.androidgaidamak.yafacebookclient.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.androidgaidamak.yafacebookclient.R;
import com.blogspot.androidgaidamak.yafacebookclient.data.Friend;
import com.blogspot.androidgaidamak.yafacebookclient.data.ImageLoader;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class FriendsFragment extends Fragment {
	private static final String TAG = "FriendsFragment";
	public static final String DATA = "data";
	public static final String NAME = "name";
	public static final String PICTURE = "picture";
	public static final String URL = "url";
	public static final String ID = "id";
	private Friend[] mFriends;

	private RecyclerView mRecyclerView;
	private MyFriendRecyclerViewAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new GraphRequest(
				AccessToken.getCurrentAccessToken(),
				"/me/taggable_friends",
				null,
				HttpMethod.GET,
				new GraphRequest.Callback() {
					public void onCompleted(GraphResponse response) {
						try {
							JSONArray data = response.getJSONObject().getJSONArray(DATA);
							Log.d(TAG, data.toString());
							mFriends = new Friend[data.length()];
							for (int i = 0; i < data.length(); i++) {
								JSONObject rawFriend = data.getJSONObject(i);
								String id = rawFriend.getString(ID);
								String name = rawFriend.getString(NAME);
								String photoUrl = rawFriend.getJSONObject(PICTURE)
										.getJSONObject(DATA).getString(URL);
								mFriends[i] = new Friend(id, name, photoUrl);
								tryShowData();
							}
						} catch (JSONException e) {
							throw new RuntimeException(e);
						}
					}
				}
		).executeAsync();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_friend_list, container, false);
		mRecyclerView = (RecyclerView) view;
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mAdapter = new MyFriendRecyclerViewAdapter((MainActivity) getActivity());
		mRecyclerView.setAdapter(mAdapter);
		tryShowData();
		return view;
	}

	private void tryShowData() {
		if (mAdapter != null && mFriends != null) {
			mAdapter.setValues(Arrays.asList(mFriends));
			mAdapter.notifyDataSetChanged();
		}
	}
}
