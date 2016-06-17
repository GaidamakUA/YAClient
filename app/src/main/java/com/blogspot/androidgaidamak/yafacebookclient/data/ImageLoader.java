package com.blogspot.androidgaidamak.yafacebookclient.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageLoader {
	private static final int DISK_CACHE_INDEX = 0;
	private static final String TAG = "ImageLoader";

	//	private final WeakReference<ImageView> mViewReference;
	private final OkHttpClient mClient;

	private DiskLruCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "thumbnails";

	public ImageLoader(OkHttpClient client, Context context) {
		this.mClient = client;
		new InitDiskCacheTask().execute(getDiskCacheDir(context, DISK_CACHE_SUBDIR));
	}

	public void load(String url, ImageView imageView, Activity activity) {
		final WeakReference<ImageView> viewReference = new WeakReference<>(imageView);
		final WeakReference<Activity> activityReference = new WeakReference<>(activity);
		new BitmapWorkerTask(viewReference, activityReference).execute(url);
	}

	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
		@Override
		protected Void doInBackground(File... params) {
			synchronized (mDiskCacheLock) {
				File cacheDir = params[0];
				try {
					mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				mDiskCacheStarting = false; // Finished initialization
				mDiskCacheLock.notifyAll(); // Wake any waiting threads
			}
			Log.d(TAG, "initialized");
			return null;
		}
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> mViewReference;
		private final WeakReference<Activity> mActivityReference;

		public BitmapWorkerTask(WeakReference<ImageView> viewReference, WeakReference<Activity> activityReference) {
			mViewReference = viewReference;
			mActivityReference = activityReference;
		}

		@Override
		protected Bitmap doInBackground(String... urls) {
			final String imageKey = String.valueOf(urls[0]);

			// Check disk cache in background thread
			Bitmap bitmap = getBitmapFromDiskCache(imageKey);

			if (bitmap == null) { // Not found in disk cache
				// Process as normal
				Request request = new Request.Builder()
						.url(imageKey)
						.build();
				Response response = null;
				try {
					response = mClient.newCall(request).execute();
					InputStream inputStream = response.body().byteStream();
					bitmap = BitmapFactory.decodeStream(inputStream);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			// Add final bitmap to caches
			addBitmapToCache(imageKey, bitmap);

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			ImageView imageView = mViewReference.get();
			Activity activity = mActivityReference.get();
			if (imageView != null && activity != null) {
				imageView.setImageDrawable(new BitmapDrawable(activity.getResources(), bitmap));
			}
		}
	}

	public void addBitmapToCache(String data, Bitmap value) {
		if (data == null || value == null) {
			return;
		}

		synchronized (mDiskCacheLock) {
			// Add to disk cache
			if (mDiskLruCache != null) {
				final String key = hashKeyForDisk(data);
				OutputStream out = null;
				try {
					DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot == null) {
						final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
						if (editor != null) {
							out = editor.newOutputStream(DISK_CACHE_INDEX);
							value.compress(Bitmap.CompressFormat.PNG, 100, out);
							editor.commit();
							out.close();
						}
					} else {
						snapshot.getInputStream(DISK_CACHE_INDEX).close();
					}
				} catch (final IOException e) {
					Log.e(TAG, "addBitmapToCache - " + e);
				} catch (Exception e) {
					Log.e(TAG, "addBitmapToCache - " + e);
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {
					}
				}
			}
		}
	}

	public Bitmap getBitmapFromDiskCache(String data) {
		final String key = hashKeyForDisk(data);
		Bitmap bitmap = null;

		synchronized (mDiskCacheLock) {
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
					Log.w(TAG, "", e);
				}
			}
			if (mDiskLruCache != null) {
				InputStream inputStream = null;
				try {
					final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot != null) {
						inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
						if (inputStream != null) {
							FileDescriptor fd = ((FileInputStream) inputStream).getFD();

							bitmap = BitmapFactory.decodeStream(new FileInputStream(fd));
							Log.d(TAG, "cache hit");
						}
					}
				} catch (final IOException e) {
					Log.e(TAG, "getBitmapFromDiskCache - " + e);
				} finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					} catch (IOException e) {
						Log.w(TAG, "", e);
					}
				}
			}
			return bitmap;
		}
	}

	public static File getDiskCacheDir(Context context, String uniqueName) {
		final String cachePath =
				Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
						!isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
						context.getCacheDir().getPath();

		return new File(cachePath + File.separator + uniqueName);
	}

	public static File getExternalCacheDir(Context context) {
		return context.getExternalCacheDir();
	}

	public static boolean isExternalStorageRemovable() {
		return Environment.isExternalStorageRemovable();
	}

	public static String hashKeyForDisk(String key) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}
}
