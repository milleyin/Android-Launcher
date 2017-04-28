package org.adw.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dongji.adapter.WallpaperMngrAdapter;
import com.dongji.enity.WallpaperInfo2;
import com.dongji.launcher.R;
import com.dongji.tool.AndroidUtils;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WallpaperActivity extends Activity {
	
	private static final int LOADING_STATIC_WALLPAPER = 0;
	private static final int LOADING_LIVE_WALLPAPER = 1;
	private static final int LOCAL_PHOTO_REQUEST_ID = 2;

	private MyHandler mHandler;
	private List<WallpaperInfo2> wallpaperList;
	
	private GridView mGridView;
	private LinearLayout mCtrl_Btn_layout;
	
	private WallpaperManager mWallpaperManager;
	private WallpaperMngrAdapter mAdapter;
	private int screen_count;
	Drawable mCurWallpaper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_wallpaper_mngr);
		initBackground();
		
		initViews();
		initStaticWallpapers();
		initHandler();
		
//		System.out.println("file path======>" + Environment.getDataDirectory().getAbsolutePath() + "/data/com.hiapk.marketpho/cache/");
//		System.out.println("file size======>" + AndroidUtils.getFileSize2(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Document/")));
		screen_count = getIntent().getIntExtra("screen_count", 1);
	}

	private void initBackground() {
		if (mWallpaperManager == null) {
//			mWallpaperManager = WallpaperManager.getInstance(WallpaperActivity.this);
			mWallpaperManager = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
		}
		mCurWallpaper = mWallpaperManager.getDrawable();
		View layout = findViewById(R.id.wallpaper_mngr_layout);
		layout.setBackgroundDrawable(mCurWallpaper);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		mCurWallpaper.setCallback(null);
		mCurWallpaper = null;
	}
	
	private void initStaticWallpapers() {
		mGridView = (GridView) findViewById(R.id.wallpaper_gridview);
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mAdapter.refreshChooseItem(position);
				mCtrl_Btn_layout.setVisibility(View.VISIBLE);
			}
		});
	}
	
	private void initViews() {
		ImageView mBackImg = (ImageView) findViewById(R.id.back_img);
		Button staticWallpaperBtn = (Button) findViewById(R.id.static_wallpaper_btn);
		Button liveWallpaperBtn = (Button) findViewById(R.id.live_wallpaper_btn);
		TextView chooseFromMapdepotTV = (TextView) findViewById(R.id.choose_from_mapdepot);
		mCtrl_Btn_layout = (LinearLayout) findViewById(R.id.ctrl_layout);
		Button confirmBtn = (Button) findViewById(R.id.confirm_btn);
		Button cancleBtn = (Button) findViewById(R.id.cancle_btn);
		
		mBackImg.setOnClickListener(listener);
		staticWallpaperBtn.setOnClickListener(listener);
		liveWallpaperBtn.setOnClickListener(listener);
		chooseFromMapdepotTV.setOnClickListener(listener);
		confirmBtn.setOnClickListener(listener);
		cancleBtn.setOnClickListener(listener);
	}
	
	OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back_img:
				finish();
				break;
			case R.id.static_wallpaper_btn:
				mHandler.sendEmptyMessage(LOADING_STATIC_WALLPAPER);
				break;
			case R.id.live_wallpaper_btn:
//				mHandler.sendEmptyMessage(LOADING_LIVE_WALLPAPER);
				callLiveWallpaper();
				break;
			case R.id.choose_from_mapdepot:
//				getStaticWallpaperFromMapdepot();
				startWallpaperChooser();
				break;
			case R.id.confirm_btn:
				if (mAdapter.getChosenIndex() == -1) {
					Toast.makeText(WallpaperActivity.this, "未选中壁纸", Toast.LENGTH_SHORT).show();
					break;
				}
				if (mWallpaperManager == null) {
//					mWallpaperManager = WallpaperManager.getInstance(WallpaperActivity.this);
					mWallpaperManager = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
				}
				try {
					int wallpaperId = wallpaperList.get(mAdapter.getChosenIndex()).getWallpaperId();
//					Drawable wallpaper = getResources().getDrawable(wallpaperId);
//					Bitmap bm = AndroidUtils.drawable2Bitmap(wallpaper);
//					DisplayMetrics metrics = AndroidUtils.getScreenSize(WallpaperActivity.this);
//					mWallpaperManager.setWallpaperOffsetSteps(0.5f, 0);
//					mWallpaperManager.setWallpaperOffsetSteps(0.0f, 1.0f/(screen_count));
//					mWallpaperManager.suggestDesiredDimensions(metrics.widthPixels, metrics.heightPixels);
//					mWallpaperManager.setBitmap(bm);
//					mWallpaperManager.clear();
					mWallpaperManager.clearWallpaperOffsets(mGridView.getWindowToken());
					mWallpaperManager.setResource(wallpaperId);
					finish();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case R.id.cancle_btn:
				if (mAdapter.getChosenIndex() == -1) {
					Toast.makeText(WallpaperActivity.this, "未选中壁纸", Toast.LENGTH_SHORT).show();
					break;
				}
				mCtrl_Btn_layout.setVisibility(View.GONE);
				mAdapter.resetChosen();
				mAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		}
	};
	
	/**
	 * 调用壁纸选择器
	 */
	private void startWallpaperChooser() {
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper,
                getText(R.string.chooser_wallpaper));
        WallpaperManager wm = (WallpaperManager)
                getSystemService(Context.WALLPAPER_SERVICE);
        WallpaperInfo wi = wm.getWallpaperInfo();
        if (wi != null && wi.getSettingsActivity() != null) {
            LabeledIntent li = new LabeledIntent(getPackageName(),
                    R.string.configure_wallpaper, 0);
            li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
        }
        startActivity(chooser);
    }
	
	/**
	 * 调用动态壁纸选择器
	 */
	private void callLiveWallpaper() {
		Intent intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
		startActivity(intent);
	}
	
	/**
	 * 获取系统自带启动器launcher2的壁纸切换功能
	 */
	private void getSystemWallpaper() {
		
		Intent chooseIntent = new Intent(Intent.ACTION_SET_WALLPAPER); 
		// 启动系统选择应用 
		Intent intent = new Intent(Intent.ACTION_CHOOSER); 
		intent.putExtra(Intent.EXTRA_INTENT, chooseIntent); 
		intent.putExtra(Intent.EXTRA_TITLE, "选择壁纸"); 
		startActivity(intent);
	}
	
	/**
	 * 获取图库中的图片资源
	 */
	private void getStaticWallpaperFromMapdepot() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, LOCAL_PHOTO_REQUEST_ID);
	}
	
	/**
	 * 获取launcher中的壁纸及其缩略图
	 * @return
	 */
	private List<WallpaperInfo2> getStaticWallpapers() {
		List<WallpaperInfo2> list = new ArrayList<WallpaperInfo2>();
		List<Integer> ids = new ArrayList<Integer>();
		String[] names= getResources().getStringArray(R.array.extra_wallpapers);
		Resources resources = getResources();
		String pkgName = getApplication().getPackageName();
		for (String name : names) {
            int res = resources.getIdentifier(name, "drawable", pkgName);
            if (res != 0) {
            	final int thumbRes = resources.getIdentifier(name + "_small",
                        "drawable", pkgName);

                if (thumbRes != 0) {
                    list.add(new WallpaperInfo2(res, thumbRes, name));
                }
			}
		}
		return list;
	}
	
	private void initHandler() {
		HandlerThread handlerThread = new HandlerThread("handler");
		handlerThread.start();
		mHandler = new MyHandler(handlerThread.getLooper());
		mHandler.sendEmptyMessage(LOADING_STATIC_WALLPAPER);
	}
	
	class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOADING_STATIC_WALLPAPER:
				wallpaperList = getStaticWallpapers();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mAdapter == null) {
							mAdapter = new WallpaperMngrAdapter(
									WallpaperActivity.this, wallpaperList);
						}
						mGridView.setAdapter(mAdapter);
					}
				});
				break;
			case LOADING_LIVE_WALLPAPER:
				
				break;
			default:
				break;
			}
		}
		
	}
	
	private void setCustomWallpaper(Bitmap bm) {
		if (mWallpaperManager == null) {
			mWallpaperManager = WallpaperManager.getInstance(this);
		}
		try {
			mWallpaperManager.setBitmap(bm);
			finish();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
