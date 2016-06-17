package com.blogspot.androidgaidamak.yafacebookclient.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.androidgaidamak.yafacebookclient.R;
import com.blogspot.androidgaidamak.yafacebookclient.data.Friend;

import java.util.ArrayList;
import java.util.List;

public class MyFriendRecyclerViewAdapter extends RecyclerView.Adapter<MyFriendRecyclerViewAdapter.ViewHolder> {

	private final List<Friend> mValues = new ArrayList<>();
	private final MainActivity mActivity;

	public MyFriendRecyclerViewAdapter(@NonNull MainActivity activity) {
		mActivity = activity;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.friend_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		holder.bind(mValues.get(position));
	}

	@Override
	public int getItemCount() {
		return mValues.size();
	}

	public void setValues(List<Friend> values) {
		mValues.clear();
		mValues.addAll(values);
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private final View mView;
		private final ImageView mProfileImageView;
		private final TextView mNameTextView;

		public ViewHolder(View view) {
			super(view);
			mView = view;
			mProfileImageView = (ImageView) view.findViewById(R.id.profile_image_view);
			mNameTextView = (TextView) view.findViewById(R.id.name_text_view);
		}

		public void bind(final Friend item) {
			mActivity.getImageLoader().load(item.getPhotoUrl(), mProfileImageView, mActivity);
			mNameTextView.setText(item.getName());
			mView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mActivity.showUserDetails(item);
				}
			});
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mNameTextView.getText() + "'";
		}
	}
}
