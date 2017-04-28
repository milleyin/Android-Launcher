package org.adw.launcher;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.adw.launcher.AllDrawer.OnPackageListener;
import org.adw.launcher.CellLayout.CellInfo;
import org.adw.launcher.DragLayer.DismissListener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout.LayoutParams;

import com.dongji.adapter.RecentOpenAdapter;
import com.dongji.adapter.RecentOpenAdapter2;
import com.dongji.enity.SortEntity;
import com.dongji.launcher.R;
import com.dongji.sqlite.DrawerDatabase;
import com.dongji.tool.AndroidUtils;
import com.dongji.ui.ScrollLayoutTouchable;

public class AppChannel implements OnClickListener, OnPackageListener,OnItemLongClickListener {
	private AllDrawer mSuperView;
	private Launcher mLauncher;
	DrawerWorkspace mWorkspace;
	private View mAppAppView;
	private LinearLayout mAppContentLayout;
	private DragLayer mDragLayer;
	private LayoutInflater mLayoutInflater;
	private Button mClearButton;
	private DesktopIndicator mDrawerAppAllIndicator;
	private ProgressBar mProgressBar;
	
	private LinearLayout.LayoutParams mParams;
	
	private DrawerWorkspace mRecentOpenWorkspace;
	private DrawerWorkspace mRecentInstallWorkspace;
	
	private ScrollLayoutTouchable mScrollLayout;
	private LinearLayout mRecentOpentClearLayout;
	private CheckBox mClearChexkBox;
	private View mRecentOpenContentView;
	
	private ScrollLayoutTouchable mNewFolderScrollLayoutTouchable;
	private EditText mTitleEditText;
	
	private List<ItemInfo> appList=new ArrayList<ItemInfo>();
	
	private int currentSortType;
	
	private ScrollLayoutTouchable mHideAppScrollLayout;
	private TextView mNumTextView;
	private PopupWindow mHideAppPopupWindow;
	PopupWindow app_popup;
	ApplicationInfo appInfo = null;
	Object tag = null;
	View tv = null;
	
	private static final int MAX_COLUMN = 6;
	private static final int MAX_ROW = 5;
	private static final int DIALOG_MAX_COLUMN = 7;
	private static final int DIALOG_MAX_ROW = 2;
	
	/**
	 * 按字母排序
	 */
	private static final int SORT_TYPE_LETTER = 0;
	/**
	 * 按时间排序
	 */
	private static final int SORT_TYPE_TIME = 1;
	/**
	 * 按使用次数排序
	 */
	private static final int SORT_TYPE_USENUM = 2;
	
	AppChannel(Launcher mLauncher, AllDrawer mSuperView) {
		this.mSuperView=mSuperView;
		this.mLauncher=mLauncher;
		initOther();
	}
	
	View initViews() {
		mAppAppView=mLayoutInflater.inflate(R.layout.layout_all_app, null);
		
        mAppContentLayout=(LinearLayout)mAppAppView.findViewById(R.id.app_content_layout);
        RadioButton mAllAppRadioButton=(RadioButton)mAppAppView.findViewById(R.id.allappbutton);
        RadioButton mRecentOpenRadioButton=(RadioButton)mAppAppView.findViewById(R.id.recentopenbutton);
        RadioButton mRecentInstallRadioButton=(RadioButton)mAppAppView.findViewById(R.id.recentinstallbutton);
        mClearButton=(Button)mAppAppView.findViewById(R.id.recent_open_clear_button);
        mAllAppRadioButton.setOnClickListener(this);
        mRecentOpenRadioButton.setOnClickListener(this);
        mRecentInstallRadioButton.setOnClickListener(this);
        mClearButton.setOnClickListener(this);
        
        initAllAppViews();
        
        initRecentOpenViews();
        
        initRecentInstallViews();
        
		return mAppAppView;
	}
	
	private void addNewView(View v) {
		if(mAppContentLayout.getChildCount()==1) {
			View mChildView=mAppContentLayout.getChildAt(0);
			if(mChildView.equals(v)) {
				return;
			}
		}
		mAppContentLayout.removeAllViews();
		mAppContentLayout.addView(v, mParams);
	}
	
	private int columnWidth=140;
    private int columnHeight=120;
    private int mDesktopColumns;
    private int mDesktopRows;
	private void initOther() {
		mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		mLayoutInflater=LayoutInflater.from(mLauncher);
		
		DisplayMetrics dm=new DisplayMetrics();
        mLauncher.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        int topHeight=AndroidUtils.dip2px(mLauncher, 125);
        
    	int num1=AndroidUtils.dip2px(mLauncher, columnWidth);
    	int num2=AndroidUtils.dip2px(mLauncher, columnHeight);
		mDesktopColumns = width / num1;
		mDesktopRows = (height - topHeight) / num2;
	}
	
	/***
	 * 初始化全部应用 View
	 */
	private void initAllAppViews() {
		mDragLayer = (DragLayer) mLayoutInflater.inflate(R.layout.layout_all_app_child, null);
		mDragLayer.addDismissListener(new DismissListener() {
			
			@Override
			public void dismiss() {
				mSuperView.dismiss();
			}
		});
		
		mWorkspace=(DrawerWorkspace)mDragLayer.findViewById(R.id.all_app_workspace);
		mWorkspace.setOnLongClickListener(mLauncher);
		mWorkspace.setDragger(mDragLayer);
        mWorkspace.setLauncher(mLauncher);
        mDragLayer.setDragScoller(mWorkspace);
		
		mDrawerAppAllIndicator=(DesktopIndicator)mDragLayer.findViewById(R.id.drawer_all_app_indicator);
		mProgressBar=(ProgressBar)mDragLayer.findViewById(R.id.progressbar);
		
		mDrawerAppAllIndicator.setAutoHide(false);
		mDrawerAppAllIndicator.setItems(0);
		mWorkspace.setDesktopIndicator(mDrawerAppAllIndicator);
		
		
		addNewView(mDragLayer);
	}
	
	/**
	 * 初始化最近打开 View 
	 */
	private void initRecentOpenViews() {  
		/*mRecentOpenWorkspace=(DrawerWorkspace)mLayoutInflater.inflate(R.layout.layout_recent_install, null);
		mRecentOpenWorkspace.setOnLongClickListener(mLauncher);
		mRecentOpenWorkspace.setLauncher(mLauncher);*/
		mRecentOpenContentView=mLayoutInflater.inflate(R.layout.layout_recent_open, null);
		mScrollLayout=(ScrollLayoutTouchable)mRecentOpenContentView.findViewById(R.id.rencent_open_content);
		mRecentOpentClearLayout=(LinearLayout)mRecentOpenContentView.findViewById(R.id.bottom_clear_layout);
		mClearChexkBox=(CheckBox)mRecentOpenContentView.findViewById(R.id.bottom_clear_checkbox);
		Button mClearCancelButton=(Button)mRecentOpenContentView.findViewById(R.id.bottom_clear_cancel_button);
		Button mConfirmCancelButton=(Button)mRecentOpenContentView.findViewById(R.id.bottom_clear_confirm_button);
		mClearChexkBox.setOnClickListener(this);
		mClearCancelButton.setOnClickListener(this);
		mConfirmCancelButton.setOnClickListener(this);
	}
	
	private void initRecentOpenData() {
		mClearButton.setVisibility(View.VISIBLE);
		mScrollLayout.removeAllViews();
		DrawerDatabase db=new DrawerDatabase(mLauncher);
		List<SortEntity> openList=db.getSortList(false);
		if(openList!=null && openList.size()>0) {
    		List<ItemInfo> appList2=new ArrayList<ItemInfo>();
    		for(int i=0;i<openList.size();i++) {
    			SortEntity entity=openList.get(i);
    			for(int j=0;j<appList.size();j++) {
    				if(appList.get(j) instanceof ApplicationInfo) {
	    				ApplicationInfo info=(ApplicationInfo)appList.get(j);
	    				if(info.getPackageName().equals(entity.packageName) && info.title.toString().equals(entity.appName)) {
	    					if(info.mSortEntity.visible==0) {
	    						ApplicationInfo aInfo=new ApplicationInfo(info);
								appList2.add(aInfo);
							}
	    					break;
	    				}
    				}
    			}
    		}
    		int count=appList2.size();
    		
			final int screenChildMaxNum = mDesktopColumns * mDesktopRows;
    		if(count>0) {
    			List<ApplicationInfo> recentList=new ArrayList<ApplicationInfo>();
	    		for(int i=0;i<count;i++) {
	    			if(i>0 && i%(screenChildMaxNum)==0) {
	    				GridView mGridView=(GridView)mLayoutInflater.inflate(R.layout.layout_rencent_gridview2, null);
	    				mGridView.setNumColumns(mDesktopColumns);
	    				RecentOpenAdapter2 mAdapter=new RecentOpenAdapter2(mLauncher, recentList);
		    			mGridView.setAdapter(mAdapter);
		    			mGridView.setOnItemClickListener(mAdapter);
		    			mGridView.setOnItemLongClickListener(this);
		    			mScrollLayout.addView(mGridView);
	    				recentList=new ArrayList<ApplicationInfo>();
	    			}
	    			recentList.add((ApplicationInfo)appList2.get(i));
	    		}
//	    		if(recentList.size()<screenChildMaxNum) {
		    		GridView mGridView=(GridView)mLayoutInflater.inflate(R.layout.layout_rencent_gridview2, null);
		    		mGridView.setNumColumns(mDesktopColumns);
		    		RecentOpenAdapter2 mAdapter=new RecentOpenAdapter2(mLauncher, recentList);
	    			mGridView.setAdapter(mAdapter);
	    			mGridView.setOnItemClickListener(mAdapter);
	    			mGridView.setOnItemLongClickListener(this);
	    			mScrollLayout.addView(mGridView);  
//	    		}
    		}
		}
	}
	
	/**
	 * 初始化最近安装 View 
	 */
	private void initRecentInstallViews() {
		mRecentInstallWorkspace=(DrawerWorkspace)mLayoutInflater.inflate(R.layout.layout_recent_install, null);
		mRecentInstallWorkspace.setOnLongClickListener(mLauncher);
		mRecentInstallWorkspace.setLauncher(mLauncher);
		
//		mRecentInstallWorkspace.setDragger(mDragLayer);
	}
	
	private ApplicationInfo getApplicationInfo(PackageManager manager,
			Intent intent, Context context) {
		// ADW: Changed the check to avoid bypassing SDcard apps in froyo
		ComponentName componentName = intent.getComponent();
		if (componentName == null) {
			return null;
		}

		final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);

		final ApplicationInfo info = new ApplicationInfo();
		info.intent = intent;
		if (resolveInfo != null) {
			final ActivityInfo activityInfo = resolveInfo.activityInfo;

			info.icon = LauncherModel.getIcon(manager, context, activityInfo);

			if (info.title == null || info.title.length() == 0) {
				info.title = activityInfo.loadLabel(manager);
			}
			if (info.title == null) {
				info.title = "";
			}
		} else {
			// ADW: add default icon for apps on SD
			info.icon = manager.getDefaultActivityIcon();
		}
		info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
		return info;
	}
	
	private ApplicationInfo addToDatabase(ResolveInfo resolveInfo) {
		PackageManager packageManager = mLauncher.getPackageManager();
		Intent allApplicationMainIntent = new Intent(Intent.ACTION_MAIN, null);
		allApplicationMainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		String packageName=resolveInfo.activityInfo.packageName;
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		ComponentName cn = new ComponentName(packageName,
				resolveInfo.activityInfo.name);
		String title = null;
		try {
			ActivityInfo aInfo = packageManager.getActivityInfo(cn, 0);
			intent.setComponent(cn);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			title = aInfo.loadLabel(packageManager).toString();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ApplicationInfo applicationinfo = getApplicationInfo(packageManager,
				intent, mLauncher);
		applicationinfo.title = title;

		LauncherModel.addItemToDatabase(mLauncher, applicationinfo,
				LauncherSettings.Favorites.CONTAINER_DESKTOP,
				applicationinfo.screen, applicationinfo.cellX,
				applicationinfo.cellY, false, 3);
		return applicationinfo;
	}
	
	private  ArrayList<ResolveInfo>  returnAllResolveInfo() {
		PackageManager packageManager=mLauncher.getPackageManager();
 		Intent allApplicationMainIntent = new Intent(Intent.ACTION_MAIN, null);
 		allApplicationMainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 		List<ResolveInfo> all_apps = packageManager.queryIntentActivities(
 				allApplicationMainIntent, 0);
 		ArrayList<ResolveInfo> appInfoss=new ArrayList<ResolveInfo>();
 		for(int i=0;i<all_apps.size();i++)
 		{
 			ResolveInfo resolveInfo=all_apps.get(i);
 			appInfoss.add(resolveInfo);
 		}
 		
 		return appInfoss;
 	}
	
	private Comparator<ItemInfo> mComparator=new Comparator<ItemInfo>() {
		
		@Override
		public int compare(ItemInfo object1, ItemInfo object2) {
			Collator mCollator = Collator.getInstance(java.util.Locale.CHINA);
			String str1=getTitle(object1);
			String str2=getTitle(object2);
//			System.out.println(object1+", "+object2+", "+str1+", "+str2);
			if(str1==null || str2==null) {
				return 0;
			}
			if (mCollator.compare(str1, str2) > 0) {
				return 1;
			} else if (mCollator.compare(str1, str2) < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	};
	
	private String getTitle(ItemInfo info) {
    	if(info instanceof ApplicationInfo) {
    		return ((ApplicationInfo)info).title.toString();
    	}else if(info instanceof FolderInfo) {
    		return ((FolderInfo)info).title.toString();
    	}
    	return null;
    }
	
	void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {
		DrawerDatabase db=new DrawerDatabase(mLauncher);
		PackageManager pm=mLauncher.getPackageManager();
		if(end>start && shortcuts.size()>=end) {
			List<ItemInfo> items=shortcuts.subList(start, end);
			
			ArrayList<ResolveInfo> resolveInfos=returnAllResolveInfo();
			
			for(int i=0;i<items.size();i++) {
				if(items.get(i) instanceof ApplicationInfo) {
					ApplicationInfo aInfo=(ApplicationInfo)items.get(i);
					String packageName=aInfo.getPackageName();
					try{
						if(pm.getPackageInfo(packageName, 0)==null) {
							db.deleteInstallPackageName(packageName);
							db.delete(packageName);
							LauncherModel.removePackageByDrawer(mLauncher, packageName);
							items.remove(i--);
							continue;
						}
					}catch(NameNotFoundException e) {
						db.deleteInstallPackageName(packageName);
						db.delete(packageName);
						LauncherModel.removePackageByDrawer(mLauncher, packageName);
						items.remove(i--);
						continue;
					}
					for(int j=0;j<resolveInfos.size();j++) {
						ResolveInfo resolveInfo=resolveInfos.get(j);
						if(packageName.equals(resolveInfo.activityInfo.packageName)) {
							resolveInfos.remove(j--);
						}
					}
				}
			}
			
			for(int j=0;j<resolveInfos.size();j++) {
				ResolveInfo resolveInfo=resolveInfos.get(j);
				String packageName=resolveInfo.activityInfo.packageName;
				ComponentName cn = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
	    		ActivityInfo info;
				try {
					info = pm.getActivityInfo(cn, 0);
		            String name=info.loadLabel(pm).toString();
		            db.addInstallPackageName(packageName, name);
					db.addOrUpdateOpenData(0, LauncherSettings.Favorites.ITEM_TYPE_APPLICATION, packageName, name);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				ApplicationInfo aInfo=addToDatabase(resolveInfo);
				if(aInfo!=null) {
					items.add(aInfo);
				}
			}
			
			Collections.sort(items, mComparator);
			
			final int maxColumn=mWorkspace.getColumns()-1;
			final int maxRow=mWorkspace.getRows();
			int[] num=new int[3];
			for (int i=0; i < items.size(); i++) {
				final ItemInfo item = items.get(i);
				
				item.screen=num[0];
				item.cellX=num[1];
				item.cellY=num[2];
				
				switch (item.itemType) {
				case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
				case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
					ApplicationInfo aInfo=(ApplicationInfo) item;
					
					SortEntity entity=db.getSortEntityByPackageName(aInfo.getPackageName(), aInfo.title.toString());
					aInfo.mSortEntity=entity;
					appList.add(aInfo);
					
					if(entity.visible==0) {
						final View shortcut = mLauncher.createShortcut(aInfo);
						
						mWorkspace.addInScreen(shortcut, item.screen, item.cellX,
								item.cellY, 1, 1, true);

						if(num[1]<maxColumn) {
		    				num[1]+=1; 
		    			}else {
		    				num[1]=0;
		    				num[2]+=1;
		    				if(num[2]%maxRow==0) {
		    					num[0]++;
		    					num[2]=0;
		    				}
		    			}
					}
					break;
				case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
					UserFolderInfo userInfo=(UserFolderInfo) item;
					final FolderIcon newFolder = FolderIcon.fromXml(
							R.layout.folder_icon, mLauncher,
							(ViewGroup) mWorkspace.getChildAt(mWorkspace
									.getCurrentScreen()), userInfo);

					for (int j = 0; j < userInfo.contents.size(); j++) {
						SortEntity sortEntity=db.getSortEntityByPackageName(userInfo.contents.get(j).getPackageName(), userInfo.title.toString());
						userInfo.contents.get(j).mSortEntity=sortEntity;
					}
					
					appList.add(item);
					// if (themeFont != null)
					// ((TextView) newFolder).setTypeface(themeFont);
					mWorkspace.addInScreen(newFolder, item.screen, item.cellX,
							item.cellY, 1, 1, true);

					if(num[1]<maxColumn) {
	    				num[1]+=1; 
	    			}else {
	    				num[1]=0;
	    				num[2]+=1;
	    				if(num[2]%maxRow==0) {
	    					num[0]++;
	    					num[2]=0;
	    				}
	    			}
					break;
				case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
					final FolderIcon newLiveFolder = LiveFolderIcon.fromXml(
							R.layout.live_folder_icon, mLauncher,
							(ViewGroup) mWorkspace.getChildAt(mWorkspace
									.getCurrentScreen()), (LiveFolderInfo) item);
					// if (themeFont != null)
					// ((TextView) newLiveFolder).setTypeface(themeFont);
					mWorkspace.addInScreen(newLiveFolder, item.screen, item.cellX,
							item.cellY, 1, 1, true);
					break;
	
				}
			}
			mWorkspace.bindItemsByLetterSort(appList);
			mRecentInstallWorkspace.setDataList(appList);
			mWorkspace.requestLayout();
			mWorkspace.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
			
			if (mDrawerAppAllIndicator != null) {
				mDrawerAppAllIndicator.setType(AlmostNexusSettingsHelper
						.getDesktopIndicatorType(mLauncher));
				if (mWorkspace != null) {
					mDrawerAppAllIndicator.setItems(mWorkspace.getChildCount());
				}
//				mDrawerAppAllIndicator.indicate(0.1f);
				/*if (isAllAppsVisible()) {
					if (mDesktopIndicator != null)
						mDesktopIndicator.hide();
				}*/
			}
		}
	}
	
	void startDrag(CellInfo cellInfo) {
		
		if (mWorkspace.allowLongPress()) {
            if (cellInfo.cell == null) {
                if (cellInfo.valid) {
                    // User long pressed on empty space
                    mWorkspace.setAllowLongPress(false);
//                    showAddDialog(cellInfo);
                }
            } else {
                if (!(cellInfo.cell instanceof Folder)) {
                    // User long pressed on an item
                	
            		mWorkspace.startDrag(cellInfo);
                }
            }
        }
	}
	
	void sortByType(int sortType) {
		currentSortType=sortType;
		switch(sortType) {
			case SORT_TYPE_LETTER:
				mWorkspace.sortByLetter();
				break;
			case SORT_TYPE_TIME:
				mWorkspace.sortByTime();
				break;
			case SORT_TYPE_USENUM:
				mWorkspace.sortByUseNum();
				break;
		}
	}

	@Override
	public void onClick(View v) {  
		switch(v.getId()) {
			case R.id.allappbutton:
				mClearButton.setVisibility(View.GONE);
				mSuperView.setTopButtonVisibleByType(1);
				addNewView(mDragLayer);
				break;
			case R.id.recentopenbutton:
				initRecentOpenData();
				mSuperView.setTopButtonVisibleByType(2);
				addNewView(mRecentOpenContentView);
//				mRecentOpenWorkspace.addRecentOpenViews();
//				addNewView(mRecentOpenWorkspace);
				break;
			case R.id.recentinstallbutton:
				mSuperView.setTopButtonVisibleByType(2);
				mClearButton.setVisibility(View.GONE);
				mRecentInstallWorkspace.setRecentInstallViews();
				addNewView(mRecentInstallWorkspace);
				break;
			case R.id.recent_open_clear_button:
				clearRecentOpenData(true);
				break;
			case R.id.bottom_clear_checkbox:
				selectAllRecentOpen(mClearChexkBox.isChecked());
				break;
			case R.id.bottom_clear_cancel_button:
				clearRecentOpenData(false);
				break;
			case R.id.bottom_clear_confirm_button:
				confirmClearRecentOpen();
				break;
			case R.id.cancelbutton:
				dismissPopupWindow(mNewFolderPopupWindow);
				break;
			case R.id.confirmbutton:
				newFolder();
				dismissPopupWindow(mNewFolderPopupWindow);
				break;
			case R.id.hide_app_cancelbutton:
				dismissPopupWindow(mHideAppPopupWindow);
				break;
			case R.id.hide_app_confirmbutton:
				hideApp();
				dismissPopupWindow(mHideAppPopupWindow);
				break;
			case R.id.add_to:
				mSuperView.showAddDialog(tag, tv);
				app_popup.dismiss();
				break;
			case R.id.uninstall:
				String UninstallPkg = null;
				try {
					if (appInfo.iconResource != null)
						UninstallPkg = appInfo.iconResource.packageName;
					else {
						PackageManager mgr = mLauncher.getPackageManager();
						ResolveInfo res = mgr.resolveActivity(appInfo.intent, 0);
						UninstallPkg = res.activityInfo.packageName;
					}

					Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
							Uri.parse("package:" + UninstallPkg));
					mLauncher.startActivity(uninstallIntent);

					mLauncher.overridePendingTransition(R.anim.enteralpha,R.anim.exitalpha);

				} catch (Exception e) {
					e.printStackTrace();
				}
				app_popup.dismiss();
				break;
				
			case R.id.app_info:
				try {
					PackageManager mgr = mLauncher.getPackageManager();
					ResolveInfo res = mgr.resolveActivity(appInfo.intent, 0);

					mLauncher.showInstalledAppDetails(mLauncher,res.activityInfo.packageName);
					mLauncher.overridePendingTransition(R.anim.enteralpha,R.anim.exitalpha);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				app_popup.dismiss();
				break;	
		}
	}
	
	private void clearRecentOpenData(boolean flag) {
		int count=mScrollLayout.getChildCount();
		for(int i=0;i<count;i++) {
			GridView mGridView=(GridView)mScrollLayout.getChildAt(i);
			RecentOpenAdapter2 mAdapter=(RecentOpenAdapter2)mGridView.getAdapter();
			mAdapter.setEditStatus(flag);
			if(!flag) {
				mAdapter.setAllChecked(flag);
			}
		}
		if(flag) {
			mRecentOpentClearLayout.setVisibility(View.VISIBLE);
			mClearButton.setVisibility(View.GONE);
			mClearChexkBox.setChecked(false);
		}else {
			mRecentOpentClearLayout.setVisibility(View.GONE);
			mClearButton.setVisibility(View.VISIBLE);
		}
	}
	
	private void selectAllRecentOpen(boolean checked) {
		int count=mScrollLayout.getChildCount();
		for(int i=0;i<count;i++) {
			GridView mGridView=(GridView)mScrollLayout.getChildAt(i);
			RecentOpenAdapter2 mAdapter=(RecentOpenAdapter2)mGridView.getAdapter();
			mAdapter.setAllChecked(checked);
		}
	}
	
	private void confirmClearRecentOpen() {
		for(int i=0;i<mScrollLayout.getChildCount();i++) {
			GridView mGridView=(GridView)mScrollLayout.getChildAt(i);
			RecentOpenAdapter2 mAdapter=(RecentOpenAdapter2)mGridView.getAdapter();
			boolean flag=mAdapter.clearRecentOpen();
			if(flag) {
				mScrollLayout.removeViewAt(i--);
			}else {
				mAdapter.setEditStatus(flag);
				if(!flag) {
					mAdapter.setAllChecked(flag);
				}
			}
		}
		mRecentOpentClearLayout.setVisibility(View.GONE);
		mClearButton.setVisibility(View.VISIBLE);
	}
	
	private void clearRecentOpenByPackageName(String packageName) {
		int count=mScrollLayout.getChildCount();
		for(int i=0;i<count;i++) {
			GridView mGridView=(GridView)mScrollLayout.getChildAt(i);
			RecentOpenAdapter2 mAdapter=(RecentOpenAdapter2)mGridView.getAdapter();
			mAdapter.clearRecentOpenByPackageName(packageName);
		}
	}
	
	
	private PopupWindow mNewFolderPopupWindow;
	private View initNewFolderViews() {
		View mContentView=mLayoutInflater.inflate(R.layout.layout_add_newfolder, null);
		mTitleEditText=(EditText)mContentView.findViewById(R.id.foldername_edittext);
		mNewFolderScrollLayoutTouchable=(ScrollLayoutTouchable)mContentView.findViewById(R.id.newfolder_content);
		Button mCancelButton=(Button)mContentView.findViewById(R.id.cancelbutton);
		Button mConfirmButton=(Button)mContentView.findViewById(R.id.confirmbutton);
		mCancelButton.setOnClickListener(this);
		mConfirmButton.setOnClickListener(this);
		return mContentView;
	}
	
	
	/**
	 * 新建文件夹
	 */
	void showNewFolder(View v) {
		if(mNewFolderPopupWindow==null) {
			mNewFolderPopupWindow=new PopupWindow(initNewFolderViews(), LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
			mNewFolderPopupWindow.setBackgroundDrawable(new BitmapDrawable()); 
			mNewFolderPopupWindow.setAnimationStyle(R.style.PopAnimation);
		}
		initNewFolderData();
		mNewFolderPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
	}
	
	private void initNewFolderData() {
		mNewFolderScrollLayoutTouchable.removeAllViews();
		mTitleEditText.setText("新建文件夹");
		List<ItemInfo> allList=mWorkspace.getAllImtem();
		List<ApplicationInfo> appList2=new ArrayList<ApplicationInfo>();
		for(int i=0;i<allList.size();i++) {
			ItemInfo info=allList.get(i);
			System.out.println(info+" "+info.id+", "+info.container);
			if(info instanceof ApplicationInfo) {
//				try {
//					appList2.add((ApplicationInfo)info.clone());
					appList2.add((ApplicationInfo)info);
				/*} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
		}
		int count=appList2.size();
		List<ApplicationInfo> recentList=new ArrayList<ApplicationInfo>();
		final int maxNum = DIALOG_MAX_COLUMN * DIALOG_MAX_ROW;
		for(int i=0;i<count;i++) {
			if(i>0 && i%maxNum==0) {
				GridView mGridView=(GridView)mLayoutInflater.inflate(R.layout.layout_rencent_gridview, null);
				RecentOpenAdapter mAdapter=new RecentOpenAdapter(mLauncher, recentList);
    			mGridView.setAdapter(mAdapter);
    			mAdapter.setEditStatus(true);
    			mGridView.setOnItemClickListener(mAdapter);
    			mNewFolderScrollLayoutTouchable.addView(mGridView);
				recentList=new ArrayList<ApplicationInfo>();
			}
			recentList.add(appList2.get(i));
		}
//		if(recentList.size()<maxNum) {
			GridView mGridView=(GridView)mLayoutInflater.inflate(R.layout.layout_rencent_gridview, null);
			RecentOpenAdapter mAdapter=new RecentOpenAdapter(mLauncher, recentList);
			mGridView.setAdapter(mAdapter);
			mAdapter.setEditStatus(true);
			mGridView.setOnItemClickListener(mAdapter);
			mNewFolderScrollLayoutTouchable.addView(mGridView);
//			}
		mNewFolderScrollLayoutTouchable.setToScreen(0);
	}
	
	private void newFolder() {
		int count=mNewFolderScrollLayoutTouchable.getChildCount();
		List<ApplicationInfo> containers=new ArrayList<ApplicationInfo>();
		for(int i=0;i<count;i++) {
			GridView mGridView=(GridView)mNewFolderScrollLayoutTouchable.getChildAt(i);
			RecentOpenAdapter mAdapter=(RecentOpenAdapter)mGridView.getAdapter();
			containers.addAll(mAdapter.getSelectList());
		}
		if(containers.size()>0) {
			String title=mTitleEditText.getText().toString();
			mWorkspace.addNewFolder(title, containers);
			sortByType(currentSortType);
		}else {
			Toast.makeText(mLauncher, "创建文件夹至少需要选择一个应用!", Toast.LENGTH_SHORT).show();
		}
	}
	
	private View initHideAppViews() {
		View mContentView=mLayoutInflater.inflate(R.layout.layout_hide_app, null);
		mNumTextView=(TextView)mContentView.findViewById(R.id.hide_app_textview);
		mHideAppScrollLayout=(ScrollLayoutTouchable)mContentView.findViewById(R.id.hide_app_content);
		Button mCancelButton=(Button)mContentView.findViewById(R.id.hide_app_cancelbutton);
		Button mConfirmButton=(Button)mContentView.findViewById(R.id.hide_app_confirmbutton);
		mCancelButton.setOnClickListener(this);
		mConfirmButton.setOnClickListener(this);
		return mContentView;
	}
	
	void showHideAppPopupWindow(View v) {
		if(mHideAppPopupWindow==null) {
			mHideAppPopupWindow=new PopupWindow(initHideAppViews(), LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
			mHideAppPopupWindow.setBackgroundDrawable(new BitmapDrawable()); 
			mHideAppPopupWindow.setAnimationStyle(R.style.PopAnimation);
		}
		initHideAppData();
		mHideAppPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
	}
	
	private void initHideAppData() {
		mHideAppScrollLayout.removeAllViews();
		final List<ItemInfo> allList=appList;
		List<ApplicationInfo> appList2=new ArrayList<ApplicationInfo>();
		for(int i=0;i<allList.size();i++) {
			ItemInfo info=allList.get(i);
			if(info instanceof ApplicationInfo) {
				ApplicationInfo aInfo=new ApplicationInfo((ApplicationInfo)info);
				appList2.add(aInfo);
			}else if(info instanceof UserFolderInfo) {
				UserFolderInfo userInfo=new UserFolderInfo((UserFolderInfo)info);
				for(int j=0;j<userInfo.contents.size();j++) {
					ApplicationInfo aInfo=new ApplicationInfo(userInfo.contents.get(j));
					appList2.add(aInfo);
				}
			}
		}
		int count=appList2.size();
		List<ApplicationInfo> recentList=new ArrayList<ApplicationInfo>();
		for(int i=0;i<count;i++) {
			if(i>0 && i%14==0) {
				GridView mGridView=(GridView)mLayoutInflater.inflate(R.layout.layout_rencent_gridview, null);
				RecentOpenAdapter mAdapter=new RecentOpenAdapter(mLauncher, recentList, true);
    			mGridView.setAdapter(mAdapter);
    			mAdapter.setEditStatus(true);
    			mGridView.setOnItemClickListener(mAdapter);
    			mHideAppScrollLayout.addView(mGridView);
				recentList=new ArrayList<ApplicationInfo>();
			}
			recentList.add(appList2.get(i));
		}
		GridView mGridView=(GridView)mLayoutInflater.inflate(R.layout.layout_rencent_gridview, null);
		RecentOpenAdapter mAdapter=new RecentOpenAdapter(mLauncher, recentList, true);
		mGridView.setAdapter(mAdapter);
		mAdapter.setEditStatus(true);
		mGridView.setOnItemClickListener(mAdapter);
		mHideAppScrollLayout.addView(mGridView);
		mHideAppScrollLayout.setToScreen(0);
	}
	
	/**
	 * 隐藏程序
	 */
	private void hideApp() {
		int count=mHideAppScrollLayout.getChildCount();
		List<ApplicationInfo> containers=new ArrayList<ApplicationInfo>();
		for(int i=0;i<count;i++) {
			GridView mGridView=(GridView)mHideAppScrollLayout.getChildAt(i);
			RecentOpenAdapter mAdapter=(RecentOpenAdapter)mGridView.getAdapter();
			containers.addAll(mAdapter.getAllList());
		}
		List<ApplicationInfo> packageList=new ArrayList<ApplicationInfo>();
		for(int i=0;i<containers.size();i++) {
			ApplicationInfo info=new ApplicationInfo(containers.get(i));
			packageList.add(info);
		}
		if(containers.size()>0) {
			mWorkspace.hideApp(containers);
			sortByType(currentSortType);
			for(int i=0;i<appList.size();i++) {
				for(int j=0;j<packageList.size();j++) {
					ApplicationInfo aInfo=packageList.get(j);
					if(appList.get(i) instanceof ApplicationInfo) {
						ApplicationInfo info=(ApplicationInfo)appList.get(i);
						if(info.getPackageName().equals(aInfo.getPackageName()) && info.title.equals(aInfo.title)) {
							if(info.mSortEntity.visible!=aInfo.mSortEntity.visible) {
								info.mSortEntity.visible=aInfo.mSortEntity.visible;
//								System.out.println("visible:"+info+", "+info.mSortEntity.visible);
							}
							break;
						}
					}else if(appList.get(i) instanceof UserFolderInfo) {
						UserFolderInfo userInfo=(UserFolderInfo)appList.get(i);
						for(int k=0;k<userInfo.contents.size();k++) {
							if(userInfo.contents.get(k).getPackageName().equals(aInfo.getPackageName())) {
								if(userInfo.contents.get(k).mSortEntity.visible!=aInfo.mSortEntity.visible)
									userInfo.contents.get(k).mSortEntity.visible=aInfo.mSortEntity.visible;
								break;
							}
						}
					}
				}
			}
			
		}
	}
	
	private void dismissPopupWindow(PopupWindow pop) {
		if(pop!=null && pop.isShowing()) {
			pop.dismiss();
		}
	}
	
	void handleFolderClick(FolderInfo folderInfo) {
		if (!folderInfo.opened) {
			// Close any open folder
			closeFolder();
			// Open the requested folder
			openFolder(folderInfo);
		} else {
			// Find the open folder...
			Folder openFolder = mWorkspace.getFolderForTag(folderInfo);
			int folderScreen;
			if (openFolder != null) {
				folderScreen = mWorkspace.getScreenForView(openFolder);
				// .. and close it
				closeFolder(openFolder);
				if (folderScreen != mWorkspace.getCurrentScreen()) {
					// Close any folder open on the current screen
					closeFolder();
					// Pull the folder onto this screen
					openFolder(folderInfo);
				}
			}
		}
	}
	
	/*private void closeFolder() {
		Folder folder = mWorkspace.getOpenFolder();
		if (folder != null) {
			closeFolder(folder);
		}
	}*/
	
	boolean closeFolder() {
		Folder folder = mWorkspace.getOpenFolder();
		if (folder != null) {
			closeFolder(folder);
			return true;
		}
		return false;
	}
	
	private void closeFolder(Folder folder) {
		folder.getInfo().opened = false;
		ViewGroup parent = (ViewGroup) folder.getParent();
		if (parent != null) {
			parent.removeView(folder);
		}
		folder.onClose();
	}
	
	private void openFolder(FolderInfo folderInfo) {
		Folder openFolder;

		if (folderInfo instanceof UserFolderInfo) {
			openFolder = UserFolder.fromXml(mLauncher);
		} else if (folderInfo instanceof LiveFolderInfo) {
			openFolder = org.adw.launcher.LiveFolder.fromXml(mLauncher, folderInfo);
		} else {
			return;
		}

		openFolder.setDragger(mDragLayer);
		openFolder.setLauncher(mLauncher);

		openFolder.bind(folderInfo);
		folderInfo.opened = true;

		if (folderInfo.container == LauncherSettings.Favorites.CONTAINER_DOCKBAR
				|| folderInfo.container == LauncherSettings.Favorites.CONTAINER_LAB
				|| folderInfo.container == LauncherSettings.Favorites.CONTAINER_RAB
				|| folderInfo.container == LauncherSettings.Favorites.CONTAINER_LAB2
				|| folderInfo.container == LauncherSettings.Favorites.CONTAINER_RAB2) {
			mWorkspace.addInScreen(openFolder, mWorkspace.getCurrentScreen(),
					0, 0, mWorkspace.currentDesktopColumns(),
					mWorkspace.currentDesktopRows());
		} else {
			mWorkspace.addInScreen(openFolder, folderInfo.screen, 0, 0,
					mWorkspace.currentDesktopColumns(),
					mWorkspace.currentDesktopRows());
		}
		openFolder.onOpen();
		// ADW: closing drawer, removed from onpause
//		closeDrawer(false);
	}
	
	
	@Override
	public void onPackageAdded(final String packageName, boolean replacing) {
		if(!replacing) {
			mLauncher.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
			
			List<ItemInfo> items=mWorkspace.onPackageChanged(packageName, true);
			sortByType(currentSortType);
			if(mRecentInstallWorkspace!=null) {
				for(int i=0;i<items.size();i++) {
					ApplicationInfo mItemInfo=new ApplicationInfo((ApplicationInfo)items.get(i));
					mRecentInstallWorkspace.addRecentInstallViews(packageName, mItemInfo);
				}
			}
				}
			});
		}
	}

	@Override
	public void onPackageRemoved(final String packageName, boolean replacing) {
		if(!replacing) {
			mLauncher.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					List<ItemInfo> removeList = mWorkspace.onPackageChanged(
							packageName, false);
					for (int i = 0; i < appList.size(); i++) {
						ApplicationInfo aInfo = (ApplicationInfo) appList
								.get(i);
						for (int j = 0; j < removeList.size(); j++) {
							ApplicationInfo info = (ApplicationInfo) removeList
									.get(j);
							if (aInfo.getPackageName().equals(
									info.getPackageName())) {
								appList.remove(i--);
								break;
							}
						}
					}
					sortByType(currentSortType);

					if (mScrollLayout != null) {
						clearRecentOpenByPackageName(packageName);
					}

					if (mRecentInstallWorkspace != null) {
						mRecentInstallWorkspace
								.removeRecentInstallApplicationInfo(packageName);
					}
				}
			});
		}
	}
	
	DrawerWorkspace getWorkspace(){
		return mWorkspace;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,long arg3) {
		
		BubbleTextView mTextView = (BubbleTextView) view.findViewById(R.id.applicationinfotextview);
		
		tag = mTextView.getTag();
		this.tv = mTextView;
		
		appInfo = (ApplicationInfo) mTextView.getTag();
		
		View p_v = LayoutInflater.from(mLauncher).inflate(R.layout.alldrawerpopup, null);
		app_popup = new PopupWindow(p_v, LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT, true);
		
		ImageView appto = (ImageView) p_v.findViewById(R.id.add_to);
		ImageView uninstall = (ImageView) p_v.findViewById(R.id.uninstall);
		ImageView app_info = (ImageView) p_v.findViewById(R.id.app_info);
		
		appto.setOnClickListener(this);
		uninstall.setOnClickListener(this);
		app_info.setOnClickListener(this);

		app_popup.setBackgroundDrawable(new BitmapDrawable());
		app_popup.setOutsideTouchable(true);
		app_popup.setAnimationStyle(R.style.popupAnimation_down);
		app_popup.showAsDropDown(view);
		return true;
	}
}
