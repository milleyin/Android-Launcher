/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.adw.launcher;

import static android.util.Log.d;
import static android.util.Log.e;
import static android.util.Log.w;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import mobi.intuitit.android.content.LauncherIntent;
import mobi.intuitit.android.content.LauncherMetadata;

import org.adw.launcher.ActionButton.SwipeListener;
import org.adw.launcher.CellLayout.CellInfo;
import org.adw.launcher.DockBar.DockBarListener;
import org.adw.launcher.DragLayer.DismissListener;
import org.adw.launcher.catalogue.AppCatalogueFilter;
import org.adw.launcher.catalogue.AppCatalogueFilters;
import org.adw.launcher.catalogue.AppGroupAdapter;
import org.adw.launcher.catalogue.AppInfoMList;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcelable;
import android.provider.LiveFolders;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dongji.adapter.AllAppAdapter;
import com.dongji.adapter.MyGridViewAdapter;
import com.dongji.adapter.MyWidgetAdapter;
import com.dongji.desktopswitch.ADownloadService;
import com.dongji.desktopswitch.PopupWindow_ChooseWifi;
import com.dongji.desktopswitch.PopupWindow_Setting;
import com.dongji.desktopswitch.SettingTools;
import com.dongji.launcher.R;
import com.dongji.launcher.RemindCalendar;
import com.dongji.receiver.AlarmHelper;
import com.dongji.service.UpdateVersionService;
import com.dongji.sqlite.DButil;
import com.dongji.sqlite.DrawerDatabase;
import com.dongji.sqlite.MyDatabaseUtil;
import com.dongji.tool.AndroidUtils;
import com.dongji.ui.ScrollLayoutTouchable;
import com.dongji.ui.ScrollLayoutTouchable.OnCurrentViewChangedListener;
import com.dongji.ui.VerScrollLayout;
import com.umeng.analytics.MobclickAgent;

/**
 * Default launcher application.
 */
public final class Launcher extends Activity implements View.OnClickListener,
		OnLongClickListener, OnSharedPreferenceChangeListener, SwipeListener {
	static final String LOG_TAG = "Launcher";
	static final boolean LOGD = false;

	private static final boolean PROFILE_STARTUP = false;
	private static final boolean PROFILE_ROTATE = false;
	private static final boolean DEBUG_USER_INTERFACE = false;

	private static final int MENU_GROUP_ADD = 1;
	private static final int MENU_GROUP_CATALOGUE = 2;
	private static final int MENU_GROUP_NORMAL = 3;

	private static final int MENU_ADD = Menu.FIRST + 1;
	private static final int MENU_WALLPAPER_SETTINGS = MENU_ADD + 1;
	private static final int MENU_SEARCH = MENU_WALLPAPER_SETTINGS + 1;
	private static final int MENU_NOTIFICATIONS = MENU_SEARCH + 1;
	private static final int MENU_SETTINGS = MENU_NOTIFICATIONS + 1;
	private static final int MENU_ALMOSTNEXUS = MENU_SETTINGS + 1;
	private static final int MENU_APP_GRP_CONFIG = MENU_SETTINGS + 2;
	private static final int MENU_APP_GRP_RENAME = MENU_SETTINGS + 3;
	private static final int MENU_APP_SWITCH_GRP = MENU_SETTINGS + 4;
	private static final int MENU_APP_DELETE_GRP = MENU_SETTINGS + 5;

	private static final int REQUEST_CREATE_SHORTCUT = 1;
	private static final int REQUEST_CREATE_LIVE_FOLDER = 4;
	private static final int REQUEST_CREATE_APPWIDGET = 5;
	private static final int REQUEST_PICK_APPLICATION = 6;
	private static final int REQUEST_PICK_SHORTCUT = 7;
	private static final int REQUEST_PICK_LIVE_FOLDER = 8;
	private static final int REQUEST_PICK_APPWIDGET = 9;
	private static final int REQUEST_PICK_ANYCUT = 10;
	private static final int REQUEST_SHOW_APP_LIST = 11;
	private static final int REQUEST_EDIT_SHIRTCUT = 12;
	private static final int REQUEST_MINI_CREATE_APPWIDGET = 13;
	private static final int REQUEST_MINI_CREATE_APPWIDGET_2 = 14;
	public static final int PHOTO_ICON = 15;

	// zy:mark
	private static final int REQUEST_PICK_APPWIDGET_2 = 16;
	public static final int REQUEST_PICK_APPWIDGET_3 = 17;
	private static final int REQUEST_CREATE_APPWIDGET_2 = 18;
	private static final int REQUEST_CREATE_APPWIDGET_3 = 19;

	static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

	static final String EXTRA_CUSTOM_WIDGET = "custom_widget";
	static final String SEARCH_WIDGET = "search_widget";

	static final String REMIND_CALENDAR_WIDGET = "remind_calendar_widget";
	
	static final String SHORTCUT_TASK_MANAGER_WIDGET = "shortcut_task_manager_widget";
	static final String SHORTCUT_CLOUD_APP_WIDGET = "shortcut_cloud_app_widget";
	static final String SHORTCUT_MY_APP_WIDGET = "shortcut_my_app_widget";

	static final int WALLPAPER_SCREENS_SPAN = 2;
	static final int SCREEN_COUNT = 5;
	static final int DEFAULT_SCREN = 2;
	static final int NUMBER_CELLS_X = 4;
	static final int NUMBER_CELLS_Y = 4;

	private static final int DIALOG_CREATE_SHORTCUT = 1;
	static final int DIALOG_RENAME_FOLDER = 2;
	static final int DIALOG_CHOOSE_GROUP = 3;
	static final int DIALOG_NEW_GROUP = 4;
	static final int DIALOG_DELETE_GROUP_CONFIRM = 5;

	// zy:mark;
	private static final int DIALOG_MY_LONG_PRESS = 6;

	private static final String PREFERENCES = "launcher.preferences";

	// Type: int
	private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
	// Type: boolean
	private static final String RUNTIME_STATE_ALL_APPS_FOLDER = "launcher.all_apps_folder";
	// Type: long
	private static final String RUNTIME_STATE_USER_FOLDERS = "launcher.user_folder";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cellX";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cellY";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_spanX";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_spanY";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_COUNT_X = "launcher.add_countX";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_COUNT_Y = "launcher.add_countY";
	// Type: int[]
	private static final String RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS = "launcher.add_occupied_cells";
	// Type: boolean
	private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
	// Type: long
	private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
	// Type: boolean
	private static final String RUNTIME_STATE_DOCKBAR = "launcher.dockbar";

	static final LauncherModel sModel = new LauncherModel();

	private static final Object sLock = new Object();
	private static int sScreen = DEFAULT_SCREN;

	private final BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();
	private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();
	private final ContentObserver mObserver = new FavoritesChangeObserver();
	private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

	private LayoutInflater mInflater;

	private DragLayer mDragLayer;
	public Workspace mWorkspace;

	public static Launcher mLauncher;
	AppWidgetManager mAppWidgetManager;
	LauncherAppWidgetHost mAppWidgetHost;

	static final int APPWIDGET_HOST_ID = 1024;

	private static CellLayout.CellInfo mAddItemCellInfo;
	private CellLayout.CellInfo mMenuAddInfo;
	private final int[] mCellCoordinates = new int[2];
	private FolderInfo mFolderInfo;
	

	/**
	 * ADW: now i use an ActionButton instead of a fixed app-drawer button
	 */
	// private ActionButton mHandleView;bob
	/**
	 * mAllAppsGrid will be "AllAppsGridView" or "AllAppsSlidingView" depending
	 * on user settings, so I cast it later.
	 */
	private Drawer mAllAppsGrid;

	private boolean mDesktopLocked = true;
	private Bundle mSavedState;

	private SpannableStringBuilder mDefaultKeySsb = null;

	private boolean mDestroyed;

	private boolean mIsNewIntent;

	private boolean mRestoring;
	private boolean mWaitingForResult;
	private boolean mLocaleChanged;

	private Bundle mSavedInstanceState;

	private DesktopBinder mBinder;
	/**
	 * ADW: New views/elements for dots, dockbar, lab/rab, etc
	 */
	// private ImageView mPreviousView;
	// private ImageView mNextView;
	MiniLauncher mMiniLauncher;
	private DockBar mDockBar;
	// private ActionButton mLAB;
	// private ActionButton mRAB;
	// private ActionButton mLAB2;
	// private ActionButton mRAB2; bob
	// private View mDrawerToolbar; bob
	/**
	 * ADW: variables to store actual status of elements
	 */
	private boolean allAppsOpen = false;
	private boolean customerAppsOpen = false;
	private final boolean allAppsAnimating = false;
	private boolean showingPreviews = false;
	private boolean mShouldHideStatusbaronFocus = false;
	/**
	 * ADW: A lot of properties to store the custom settings
	 */
	private boolean allowDrawerAnimations = true;
	private boolean newPreviews = true;
	private boolean hideStatusBar = false;
	private boolean showDots = true;
	private boolean showDockBar = true;
	private boolean autoCloseDockbar;
	protected boolean autoCloseFolder;
	private boolean hideABBg = false;
	private float uiScaleAB = 0.5f;
	private boolean uiHideLabels = false;
	private boolean wallpaperHack = true;
	private boolean scrollableSupport = false;
	private DesktopIndicator mDesktopIndicator;
	private int savedOrientation;
	private boolean useDrawerCatalogNavigation = true;
	/**
	 * ADW: Home/Swype down binding constants
	 */
	protected static final int BIND_NONE = 0;
	protected static final int BIND_DEFAULT = 1;
	protected static final int BIND_HOME_PREVIEWS = 2;
	protected static final int BIND_PREVIEWS = 3;
	protected static final int BIND_APPS = 4;
	protected static final int BIND_STATUSBAR = 5;
	protected static final int BIND_NOTIFICATIONS = 6;
	protected static final int BIND_HOME_NOTIFICATIONS = 7;
	protected static final int BIND_DOCKBAR = 8;
	protected static final int BIND_APP_LAUNCHER = 9;

	private int mHomeBinding = BIND_PREVIEWS;

	/**
	 * wjax: Swipe Down binding enum
	 */
	private int mSwipedownAction = BIND_NOTIFICATIONS;
	/**
	 * wjax: Swipe UP binding enum
	 */
	private int mSwipeupAction = BIND_NOTIFICATIONS;
	/**
	 * ADW:Wallpaper intent receiver
	 */
	private static WallpaperIntentReceiver sWallpaperReceiver;
	private boolean mShouldRestart = false;
	private boolean mMessWithPersistence = false;
	// ADW Theme constants
	public static final int THEME_ITEM_BACKGROUND = 0;
	public static final int THEME_ITEM_FOREGROUND = 1;
	public static final String THEME_DEFAULT = "ADW.Default theme";
	private Typeface themeFont = null;
	private boolean mIsEditMode = false;
	private View mScreensEditor = null;
	private boolean mIsWidgetEditMode = false;
	private LauncherAppWidgetInfo mlauncherAppWidgetInfo = null;
	// /TODO:ADW. Current code fully ready for upto 9
	// but need to add more drawables for the desktop dots...
	// or completely redo the desktop dots implementation
	private final static int MAX_SCREENS = 7;
	// ADW: NAVIGATION VALUES FOR THE NEXT/PREV CATALOG ACTIONS
	private final static int ACTION_CATALOG_PREV = 1;
	private final static int ACTION_CATALOG_NEXT = 2;
	// ADW: Custom counter receiver
	private CounterReceiver mCounterReceiver;
	/**
	 * ADW: Different main dock styles/configurations
	 */
	protected static final int DOCK_STYLE_NONE = 0;
	protected static final int DOCK_STYLE_3 = 1;
	protected static final int DOCK_STYLE_5 = 2;
	protected static final int DOCK_STYLE_1 = 3;
	private int mDockStyle = DOCK_STYLE_3;
	// DRAWER STYLES
	private final int[] mDrawerStyles = { R.layout.old_drawer,
			R.layout.new_drawer };

	FolderIcon opening_floder_icon;
	LinearLayout settingButton;// 设置按钮
	// LinearLayout modButton;// 模式按钮
	LinearLayout addAppButton;// 添加应用按钮
	LinearLayout drawerButton;// 抽屉按钮
	LinearLayout switchButton;// 开关按钮
	LinearLayout searchButton;// 搜索按钮

	ImageView generalButton;// 普通模式
	// Button conciseButton;//简洁模式 后续版本增加
	ImageView bustlingButton;// 繁华模式
	ImageView customButton;// 自定义模式
	// EditText searchText;//搜索框内容
	PopupWindow popupWindow;
	private PopupWindow mSettingPop; // 设置下拉选项
	private View mTopView;
	AllDrawer mAllDrawer;
	private LinearLayout mDesktopLayout;
	private boolean isFirstInDrawer = true;

	public static final String EXTRA_APPLICATIONINFO = "EXTRA_APPLICATIONINFO";
	public PopupWindow mPopupWindow;
	PopupWindow widgetPopupWindow;
	String UninstallPkg = null;
	static ItemInfo itemInfo = null;
	PopupWindow desktopPopupWindow = null;
	private int mIconSize;
	private Bitmap mBitmap;
	MyDatabaseUtil myDatabaseUtil = null;
	CellLayout.CellInfo cell;
	String id = null;
	int decipheringTag = 0;
	public ApplicationInfo folder_app_info = null;
	public View contentView = null;
	// HashMap<Integer, ApplicationInfo> hashMap = new HashMap<Integer,
	// ApplicationInfo>();
	int orgX = 0;
	int orgY = 0;
	Drawable mCloseIcon;
	Context context;
	LinearLayout appdelete;
	LinearLayout uninstall;
	LinearLayout appinfo;
	LinearLayout changeicon;
	LinearLayout rename;
	LinearLayout folderdelete;
	LinearLayout deciphering;
	LinearLayout encryption;
	LinearLayout addapp;
	LinearLayout folderrename;
	LinearLayout appwidgetdelete;
	Button submit;
	Button cancel;
	EditText rename_et;
	EditText pwd;
	EditText repwd;
	TextView title;
	CheckBox checkDelete;
	GridView appGv;
	ImageButton appImage;
	TextView appName;
	View iniView;
	TextView name;

	public static int stateMod = 0;
	private int saveMob;

	// 添加应用 模式
	PopupWindow add_app_popup;
	// Dialog dialog_add_app;
	Button btn_swap_app;
	Button btn_swap_widget;
	ScrollLayoutTouchable scroller_content;
	WorkspaceMini workspaceMini;
	public List<Bitmap> temp_bitmaps = new ArrayList<Bitmap>();
	// MyAnimationFrameLayout myAnimationFrameLayout;
	int workspaceMiniMode = 0; // 0:为添加app ， 1:为添加widget
	DesktopIndicator add_mode_indicator;

	private MyHandler myHandler = new MyHandler();
	String arrVersionUpdate[];
	String update_url;
	String version_info = "1.0";
	String version_date = "2012-12-28";
	float currentAppVersion = -1;
	public static boolean isDownLoadding = false;
	public static int width = 0;
	public static int height = 0;

	WorkspaceMini add_workspaceMini; // 抽屉模式中，添加应用到桌面的 workspace对象
	public TextView mPublishDateTV, mVersionInfoTV;
	public Button mConfirmBtn, mCheckUpdateBtn;
	public Button mFeedbackBtn;
	public static String feedURL = "http://bbs.91dongji.com/forum-57-1.html";
	PopupWindow pWindow;

	private VerScrollLayout mVerScrollLayout;

	TextView mWallpapaerSettingView;
	TextView mAboutView;
	AlarmHelper alarmHelper;
	Calendar mCalendar = Calendar.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		addStatistics();
		this.context = Launcher.this;
		myDatabaseUtil = DButil.getInstance(context);
		alarmHelper = new AlarmHelper(Launcher.this);

		mCalendar.setTimeInMillis(System.currentTimeMillis());
		mCalendar.set(Calendar.HOUR_OF_DAY, 19);
		mCalendar.set(Calendar.MINUTE, 55);
		mCalendar.set(Calendar.SECOND, 0);
		mCalendar.set(Calendar.MILLISECOND, 0);

		alarmHelper.openAlarm(30, mCalendar.getTimeInMillis());

		mMessWithPersistence = AlmostNexusSettingsHelper
				.getSystemPersistent(this);
		if (mMessWithPersistence) {
			changeOrientation(
					AlmostNexusSettingsHelper.getDesktopOrientation(this), true);
			setPersistent(true);
		} else {
			setPersistent(false);
			changeOrientation(
					AlmostNexusSettingsHelper.getDesktopOrientation(this),
					false);
		}
		super.onCreate(savedInstanceState);
		mInflater = getLayoutInflater();

		mLauncher = this;
		checkUpdate(false);
		AppCatalogueFilters.getInstance().init(this);
		LauncherActions.getInstance().init(this);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.widthPixels;
		// 应用太多，创建默认屏幕个数
		if (!((dm.widthPixels > 800 && dm.heightPixels > 600) || (dm.widthPixels > 600 && dm.heightPixels > 800))) {

			AlmostNexusSettingsHelper.setDesktopColumns(this, 4);
			AlmostNexusSettingsHelper.setDesktopRows(this, 4);
		}
		mAppWidgetManager = AppWidgetManager.getInstance(this);

		mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
		mAppWidgetHost.startListening();

		if (PROFILE_STARTUP) {
			android.os.Debug.startMethodTracing("/sdcard/launcher");
		}
		updateAlmostNexusVars();
		checkForLocaleChange();
		// setWallpaperDimension();
		setContentView(R.layout.launcher);
		setupViews();

		registerIntentReceivers();
		registerContentObservers();

		mSavedState = savedInstanceState;
		restoreState(mSavedState);

		if (PROFILE_STARTUP) {
			android.os.Debug.stopMethodTracing();
		}
		// int mode = getSharedPreferences("state_mode", 0).getInt("mode", 0);


		if (!mRestoring) {
			startLoaders(stateMod);
		}

		// For handling default keys
		mDefaultKeySsb = new SpannableStringBuilder();
		Selection.setSelection(mDefaultKeySsb, 0);

		// ADW: register a sharedpref listener
		getSharedPreferences("launcher.preferences.almostnexus",
				Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
				this);

	}

	private void addStatistics() {
		MobclickAgent.setDebugMode(true);
		MobclickAgent.onError(this);
	}

	private void checkForLocaleChange() {
		final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
		readConfiguration(this, localeConfiguration);

		final Configuration configuration = getResources().getConfiguration();

		final String previousLocale = localeConfiguration.locale;
		final String locale = configuration.locale.toString();

		final int previousMcc = localeConfiguration.mcc;
		final int mcc = configuration.mcc;

		final int previousMnc = localeConfiguration.mnc;
		final int mnc = configuration.mnc;

		mLocaleChanged = !locale.equals(previousLocale) || mcc != previousMcc
				|| mnc != previousMnc;

		if (mLocaleChanged) {
			localeConfiguration.locale = locale;
			localeConfiguration.mcc = mcc;
			localeConfiguration.mnc = mnc;

			writeConfiguration(this, localeConfiguration);
		}
	}

	private static class LocaleConfiguration {
		public String locale;
		public int mcc = -1;
		public int mnc = -1;
	}

	private static void readConfiguration(Context context,
			LocaleConfiguration configuration) {
		DataInputStream in = null;
		try {
			in = new DataInputStream(context.openFileInput(PREFERENCES));
			configuration.locale = in.readUTF();
			configuration.mcc = in.readInt();
			configuration.mnc = in.readInt();
		} catch (FileNotFoundException e) {
			// Ignore
		} catch (IOException e) {
			// Ignore
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

	private static void writeConfiguration(Context context,
			LocaleConfiguration configuration) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(context.openFileOutput(PREFERENCES,
					MODE_PRIVATE));
			out.writeUTF(configuration.locale);
			out.writeInt(configuration.mcc);
			out.writeInt(configuration.mnc);
			out.flush();
		} catch (FileNotFoundException e) {
			// Ignore
		} catch (IOException e) {
			// noinspection ResultOfMethodCallIgnored
			context.getFileStreamPath(PREFERENCES).delete();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

	static int getScreen() {
		synchronized (sLock) {
			return sScreen;
		}
	}

	static void setScreen(int screen) {
		synchronized (sLock) {
			sScreen = screen;
		}
	}

	void startLoaders(int mode) {
		boolean loadApplications = sModel.loadApplications(true, this,
				mLocaleChanged);

		sModel.loadUserItems(!mLocaleChanged, this, mLocaleChanged,
				loadApplications, mode);
		mRestoring = false;

	}

	private void setWallpaperDimension() {
		WallpaperManager wpm = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);

		Display display = getWindowManager().getDefaultDisplay();
		boolean isPortrait = display.getWidth() < display.getHeight();

		final int width = isPortrait ? display.getWidth() : display.getHeight();
		final int height = isPortrait ? display.getHeight() : display
				.getWidth();

		wpm.suggestDesiredDimensions(width * WALLPAPER_SCREENS_SPAN, height);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mWaitingForResult = false;
		// The pattern used here is that a user PICKs a specific application,
		// which, depending on the target, might need to CREATE the actual
		// target.

		// For example, the user would PICK_SHORTCUT for "Music playlist", and
		// we
		// launch over to the Music app to actually CREATE_SHORTCUT.
		if (resultCode == RESULT_OK && mAddItemCellInfo != null) {// 
			switch (requestCode) {
			case REQUEST_PICK_APPLICATION:
				completeAddApplication(this, data, mAddItemCellInfo,
						!mDesktopLocked);
				break;
			case REQUEST_PICK_SHORTCUT:
				processShortcut(data, REQUEST_PICK_APPLICATION,
						REQUEST_CREATE_SHORTCUT);
				break;
			case REQUEST_CREATE_SHORTCUT:
				completeAddShortcut(data, mAddItemCellInfo, !mDesktopLocked);
				break;
			case REQUEST_PICK_LIVE_FOLDER:
				addLiveFolder(data);
				break;
			case REQUEST_CREATE_LIVE_FOLDER:
				completeAddLiveFolder(data, mAddItemCellInfo, !mDesktopLocked);
				break;
			case REQUEST_PICK_APPWIDGET:
				addAppWidget(data);
				break;
			case REQUEST_CREATE_APPWIDGET:
				completeAddAppWidget(data, mAddItemCellInfo, !mDesktopLocked);
				break;
			case REQUEST_PICK_ANYCUT:
				completeAddShortcut(data, mAddItemCellInfo, !mDesktopLocked);
				break;
			case REQUEST_EDIT_SHIRTCUT:
				// completeEditShirtcut(data); bob
				break;
			case PHOTO_ICON:
				updateIcon(data);
				break;

			case REQUEST_MINI_CREATE_APPWIDGET_2:
				add_workspaceMini.addWidgetAfterSetConfig2(data);
				break;

			// zy:mark
			case REQUEST_PICK_APPWIDGET_2:
				workspaceMini.addWidgetIn(data);
				break;

			// zy:mark
			case REQUEST_PICK_APPWIDGET_3:

				if (!configureOrAddAppWidget3(data)) {
					mAllDrawer.showAddDialog(data);
				}
				break;

			// zy:mark
			case REQUEST_CREATE_APPWIDGET_2:
				workspaceMini.addWidgetAfterSetConfig2(data);
				break;

			case REQUEST_CREATE_APPWIDGET_3:
				mAllDrawer.showAddDialog(data);
				break;
			}

		} else if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_SHOW_APP_LIST:
				mAllAppsGrid.updateAppGrp();
				showAllApps(true, null);
				break;
			case REQUEST_EDIT_SHIRTCUT:
				// completeEditShirtcut(data); bob
				break;

			case PHOTO_ICON:
				updateIcon(data);
				break;

			case REQUEST_MINI_CREATE_APPWIDGET:
				workspaceMini.addWidgetAfterSetConfig(data);
				break;

			// zy:mark
			case REQUEST_PICK_APPWIDGET_2:
				workspaceMini.addWidgetIn(data);
				break;

			// zy:mark
			case REQUEST_PICK_APPWIDGET_3:

				if (!configureOrAddAppWidget3(data)) {
					mAllDrawer.showAddDialog(data);
				}
				break;

			// zy:mark
			case REQUEST_CREATE_APPWIDGET_2:
				workspaceMini.addWidgetAfterSetConfig2(data);
				break;

			case REQUEST_CREATE_APPWIDGET_3:
				mAllDrawer.showAddDialog(data);
				break;

			}
		} else if ((requestCode == REQUEST_PICK_APPWIDGET || requestCode == REQUEST_CREATE_APPWIDGET)
				&& resultCode == RESULT_CANCELED && data != null) {
			// Clean up the appWidgetId if we canceled
			int appWidgetId = data.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if (appWidgetId != -1) {
				mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		MobclickAgent.onResume(this);

		if (shouldRestart())
			return;
		// ADW: Use custom settings to set the rotation
		/*
		 * this.setRequestedOrientation(
		 * AlmostNexusSettingsHelper.getDesktopRotation(this)?
		 * ActivityInfo.SCREEN_ORIENTATION_USER
		 * :ActivityInfo.SCREEN_ORIENTATION_NOSENSOR );
		 */
		// ADW: Use custom settings to change number of columns (and rows for
		// SlidingGrid) depending on phone rotation
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			mAllAppsGrid.setNumColumns(AlmostNexusSettingsHelper
					.getColumnsPortrait(Launcher.this));
			mAllAppsGrid.setNumRows(AlmostNexusSettingsHelper
					.getRowsPortrait(Launcher.this));
			mAllAppsGrid.setPageHorizontalMargin(AlmostNexusSettingsHelper
					.getPageHorizontalMargin(Launcher.this));
		} else {
			mAllAppsGrid.setNumColumns(AlmostNexusSettingsHelper
					.getColumnsLandscape(Launcher.this));
			mAllAppsGrid.setNumRows(AlmostNexusSettingsHelper
					.getRowsLandscape(Launcher.this));
		}
		mWorkspace.setWallpaper(false);
		if (mRestoring) {
			startLoaders(stateMod);
		}

		// If this was a new intent (i.e., the mIsNewIntent flag got set to true
		// by
		// onNewIntent), then close the search dialog if needed, because it
		// probably
		// came from the user pressing 'home' (rather than, for example,
		// pressing 'back').
		if (mIsNewIntent) {
			// Post to a handler so that this happens after the search dialog
			// tries to open
			// itself again.
			mWorkspace.post(new Runnable() {
				public void run() {
					// ADW: changed from using ISearchManager to use
					// SearchManager (thanks to Launcher+ source code)
					SearchManager searchManagerService = (SearchManager) Launcher.this
							.getSystemService(Context.SEARCH_SERVICE);
					try {
						searchManagerService.stopSearch();
					} catch (Exception e) {
						e(LOG_TAG, "error stopping search", e);
					}
				}
			});
		}

		mIsNewIntent = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		// ADW: removed cause it was closing app-drawer every time Home button
		// is triggered
		// ADW: it should be done only on certain circumstances
		// closeDrawer(false);
		MobclickAgent.onPause(this);

		savedOrientation = getResources().getConfiguration().orientation;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Flag any binder to stop early before switching
		if (mBinder != null) {
			mBinder.mTerminate = true;
		}
		// if(mMessWithPersistence)setPersistent(false);
		if (PROFILE_ROTATE) {
			android.os.Debug.startMethodTracing("/sdcard/launcher-rotate");
		}
		return null;
	}

	private boolean acceptFilter() {
		final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		return !inputManager.isFullscreenMode();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean handled = super.onKeyDown(keyCode, event);
		if (!handled && acceptFilter() && keyCode != KeyEvent.KEYCODE_ENTER) {
			boolean gotKey = TextKeyListener.getInstance().onKeyDown(
					mWorkspace, mDefaultKeySsb, keyCode, event);
			if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
				// something usable has been typed - start a search
				// the typed text will be retrieved and cleared by
				// showSearchDialog()
				// If there are multiple keystrokes before the search dialog
				// takes focus,
				// onSearchRequested() will be called for every keystroke,
				// but it is idempotent, so it's fine.
				return onSearchRequested();
			}
		}

		return handled;
	}

	private String getTypedText() {
		return mDefaultKeySsb.toString();
	}

	private void clearTypedText() {
		mDefaultKeySsb.clear();
		mDefaultKeySsb.clearSpans();
		Selection.setSelection(mDefaultKeySsb, 0);
	}

	/**
	 * Restores the previous state, if it exists.
	 * 
	 * @param savedState
	 *            The previous state.
	 */
	private void restoreState(Bundle savedState) {
		if (savedState == null) {
			return;
		}

		final int currentScreen = savedState.getInt(
				RUNTIME_STATE_CURRENT_SCREEN, -1);
		if (currentScreen > -1) {
			mWorkspace.setCurrentScreen(currentScreen);
		}

		final int addScreen = savedState.getInt(
				RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
		if (addScreen > -1) {
			mAddItemCellInfo = new CellLayout.CellInfo();
			final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
			addItemCellInfo.valid = true;
			addItemCellInfo.screen = addScreen;
			addItemCellInfo.cellX = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
			addItemCellInfo.cellY = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
			addItemCellInfo.spanX = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
			addItemCellInfo.spanY = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
			addItemCellInfo.findVacantCellsFromOccupied(savedState
					.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS),
					savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_X),
					savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y));
			mRestoring = true;
		}

		boolean renameFolder = savedState.getBoolean(
				RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
		if (renameFolder) {
			long id = savedState
					.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
			mFolderInfo = sModel.getFolderById(this, id);
			mRestoring = true;
		}
	}

	/**
	 * Finds all the views we need and configure them properly.
	 */
	private void setupViews() {

		mVerScrollLayout = (VerScrollLayout) findViewById(R.id.verscrolllayout);
		mAllDrawer = (AllDrawer) findViewById(R.id.alldrawer);
		mDesktopLayout = (LinearLayout) findViewById(R.id.desktoplayout);
		mAllDrawer.setLauncher(this);

		View v_add_app_popup = LayoutInflater.from(this).inflate(
				R.layout.activity_add_app, null);

		add_app_popup = new PopupWindow(v_add_app_popup,
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
		add_app_popup.setBackgroundDrawable(new BitmapDrawable());
		add_app_popup.setAnimationStyle(R.style.PopAnimation);
		add_app_popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override
			public void onDismiss() {
				clearTempBitmap();
			}
		});
		((LinearLayout) v_add_app_popup.findViewById(R.id.base_layout))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						add_app_popup.dismiss();
					}
				});

		btn_swap_app = (Button) v_add_app_popup.findViewById(R.id.btn_swap_app);
		btn_swap_app.setOnClickListener(this);
		btn_swap_widget = (Button) v_add_app_popup
				.findViewById(R.id.btn_swap_widget);
		btn_swap_widget.setOnClickListener(this);
		scroller_content = (ScrollLayoutTouchable) v_add_app_popup
				.findViewById(R.id.ln_content);

		scroller_content
				.setmOnCurrentViewChangedListener(new OnCurrentViewChangedListener() {

					@Override
					public void onCurrentViewChanged(View view, int currentview) {
						if (add_mode_indicator != null) {
							add_mode_indicator.fullIndicate(currentview);
						}
					}
				});
		workspaceMini = (WorkspaceMini) v_add_app_popup
				.findViewById(R.id.workspaceMini);
		workspaceMini.setLauncher(this);
		ImageView imageView = (ImageView) v_add_app_popup
				.findViewById(R.id.animation_icon);
		workspaceMini.setAnimation_icon(imageView);

		add_mode_indicator = (DesktopIndicator) v_add_app_popup
				.findViewById(R.id.add_mode_indicator);
		add_mode_indicator.setAutoHide(false);

		// zy:mark
		((Button) v_add_app_popup.findViewById(R.id.btn_add_widget))
				.setOnClickListener(this);

		((ImageView) v_add_app_popup.findViewById(R.id.img_open_box))
				.setOnClickListener(this);

		// myAnimationFrameLayout
		// =(MyAnimationFrameLayout)v_add_app_popup.findViewById(R.id.base_layout);
		// myAnimationFrameLayout.setAnima_layout((AbsoluteLayout)v_add_app_popup.findViewById(R.id.anima_layout));
		mTopView = findViewById(R.id.toplayout);
		mTopView.setOnClickListener(this);

		// modButton = (LinearLayout) findViewById(R.id.btn_mode);
		// ((Button) findViewById(R.id.btn_m)).setOnClickListener(this);

		settingButton = (LinearLayout) findViewById(R.id.btn_setting);
		((Button) findViewById(R.id.btn_s)).setOnClickListener(this);

		addAppButton = (LinearLayout) findViewById(R.id.btn_add_app);
		((Button) findViewById(R.id.btn_a)).setOnClickListener(this);

		drawerButton = (LinearLayout) findViewById(R.id.btn_drawer);
		((Button) findViewById(R.id.btn_d)).setOnClickListener(this);

		switchButton = (LinearLayout) findViewById(R.id.btn_switch);
		((Button) findViewById(R.id.btn_sw)).setOnClickListener(this);

		searchButton = (LinearLayout) findViewById(R.id.btn_search);
		// searchText=(EditText)findViewById(R.id.search_text);
		settingButton.setOnClickListener(this);
		addAppButton.setOnClickListener(this);
		// modButton.setOnClickListener(this);
		drawerButton.setOnClickListener(this);
		switchButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
		final DragLayer dragLayer = mDragLayer;
		mDragLayer.addDismissListener(new DismissListener() {

			@Override
			public void dismiss() {
				if (mPopupWindow != null && mPopupWindow.isShowing()) {
					mPopupWindow.dismiss();
				}
			}
		});

		mWorkspace = (Workspace) dragLayer.findViewById(R.id.workspace);
		final Workspace workspace = mWorkspace;
		// ADW: The app drawer is now a ViewStub and we load the resource
		// depending on custom settings
		ViewStub tmp = (ViewStub) dragLayer.findViewById(R.id.stub_drawer);
		int drawerStyle = AlmostNexusSettingsHelper.getDrawerStyle(this);
		tmp.setLayoutResource(mDrawerStyles[drawerStyle]);
		mAllAppsGrid = (Drawer) tmp.inflate();

		// final DeleteZone deleteZone = (DeleteZone)
		// dragLayer.findViewById(R.id.delete_zone); bob 屏蔽删除

		/*
		 * mHandleView = (ActionButton) dragLayer.findViewById(R.id.btn_mab);
		 * mHandleView.setFocusable(true); mHandleView.setLauncher(this); bob
		 * mHandleView.setOnClickListener(this);
		 * dragLayer.addDragListener(mHandleView);
		 */
		/*
		 * mHandleView.setOnTriggerListener(new OnTriggerListener() { public
		 * void onTrigger(View v, int whichHandle) { mDockBar.open(); } public
		 * void onGrabbedStateChange(View v, boolean grabbedState) { } public
		 * void onClick(View v) { if (allAppsOpen) { closeAllApps(true); } else
		 * { showAllApps(true, null); } } });
		 */
		mAllAppsGrid.setTextFilterEnabled(false);
		mAllAppsGrid.setDragger(dragLayer);
		mAllAppsGrid.setLauncher(this);

		workspace.setOnLongClickListener(this);
		workspace.setDragger(dragLayer);
		workspace.setLauncher(this);

		// deleteZone.setLauncher(this); bob 屏蔽删除
		// deleteZone.setDragController(dragLayer);

		dragLayer.setIgnoredDropTarget((View) mAllAppsGrid);
		dragLayer.setDragScoller(workspace);
		// dragLayer.addDragListener(deleteZone); bob 屏蔽删除
		// ADW: Dockbar inner icon viewgroup (MiniLauncher.java)
		mMiniLauncher = (MiniLauncher) dragLayer
				.findViewById(R.id.mini_content);
		mMiniLauncher.setLauncher(this);
		mMiniLauncher.setOnLongClickListener(this);
		mMiniLauncher.setDragger(dragLayer);
		mMiniLauncher.setOnClickListener(this);
		dragLayer.addDragListener(mMiniLauncher);

		// ADW: Action Buttons (LAB/RAB)
		/*
		 * mLAB = (ActionButton) dragLayer.findViewById(R.id.btn_lab);
		 * mLAB.setLauncher(this); mLAB.setSpecialIcon(R.drawable.arrow_left);
		 * mLAB.setSpecialAction(ACTION_CATALOG_PREV);
		 * dragLayer.addDragListener(mLAB); mRAB = (ActionButton)
		 * dragLayer.findViewById(R.id.btn_rab); mRAB.setLauncher(this);
		 * mRAB.setSpecialIcon(R.drawable.arrow_right);
		 * mRAB.setSpecialAction(ACTION_CATALOG_NEXT);
		 * dragLayer.addDragListener(mRAB); mLAB.setOnClickListener(this);
		 * mRAB.setOnClickListener(this); //ADW: secondary aActionButtons mLAB2
		 * = (ActionButton) dragLayer.findViewById(R.id.btn_lab2);
		 * mLAB2.setLauncher(this); dragLayer.addDragListener(mLAB2); mRAB2 =
		 * (ActionButton) dragLayer.findViewById(R.id.btn_rab2);
		 * mRAB2.setLauncher(this); dragLayer.addDragListener(mRAB2);
		 * mLAB2.setOnClickListener(this); mRAB2.setOnClickListener(this);
		 * //ADW: Dots ImageViews // mPreviousView =
		 * (ImageView)findViewById(R.id.btn_scroll_left); // mNextView =
		 * (ImageView)findViewById(R.id.btn_scroll_right); //
		 * mPreviousView.setOnLongClickListener(this); //
		 * mNextView.setOnLongClickListener(this);bob //ADW: ActionButtons swipe
		 * gestures mHandleView.setSwipeListener(this);
		 * mLAB.setSwipeListener(this); mLAB2.setSwipeListener(this);
		 * mRAB.setSwipeListener(this); mRAB2.setSwipeListener(this);
		 * 
		 * //ADW linearlayout with apptray, lab and rab //
		 * mDrawerToolbar=findViewById(R.id.drawer_toolbar); bob
		 * mHandleView.setNextFocusUpId(R.id.drag_layer);
		 * mHandleView.setNextFocusLeftId(R.id.drag_layer);
		 * mLAB.setNextFocusUpId(R.id.drag_layer);
		 * mLAB.setNextFocusLeftId(R.id.drag_layer);
		 * mRAB.setNextFocusUpId(R.id.drag_layer);
		 * mRAB.setNextFocusLeftId(R.id.drag_layer);
		 * mLAB2.setNextFocusUpId(R.id.drag_layer);
		 * mLAB2.setNextFocusLeftId(R.id.drag_layer);
		 * mRAB2.setNextFocusUpId(R.id.drag_layer);
		 * mRAB2.setNextFocusLeftId(R.id.drag_layer);
		 */
		// ADW add a listener to the dockbar to show/hide the app-drawer-button
		// and the dots
		mDockBar = (DockBar) findViewById(R.id.dockbar);
		mDockBar.setDockBarListener(new DockBarListener() {
			public void onOpen() {
				// mDrawerToolbar.setVisibility(View.GONE);
				// if(mNextView.getVisibility()==View.VISIBLE){
				// mNextView.setVisibility(View.INVISIBLE);
				// mPreviousView.setVisibility(View.INVISIBLE);bob
				// }
			}

			public void onClose() {
				// if(mDockStyle!=DOCK_STYLE_NONE)mDrawerToolbar.setVisibility(View.VISIBLE);
				if (showDots && !isAllAppsVisible()) {
					// mNextView.setVisibility(View.VISIBLE);
					// mPreviousView.setVisibility(View.VISIBLE);bob
				}

			}
		});
		if (AlmostNexusSettingsHelper.getDesktopIndicator(this)) {
			mDesktopIndicator = (DesktopIndicator) (findViewById(R.id.desktop_indicator));
		}
		// ADW: Add focusability to screen items
		// mLAB.setFocusable(true);
		// mRAB.setFocusable(true);
		// mLAB2.setFocusable(true); bob
		// mRAB2.setFocusable(true);
		// mPreviousView.setFocusable(true);
		// mNextView.setFocusable(true);

		// ADW: Load the specified theme
		String themePackage = AlmostNexusSettingsHelper.getThemePackageName(
				this, THEME_DEFAULT);
		PackageManager pm = getPackageManager();
		Resources themeResources = null;
		if (!themePackage.equals(THEME_DEFAULT)) {
			try {
				themeResources = pm.getResourcesForApplication(themePackage);
			} catch (NameNotFoundException e) {
				// ADW The saved theme was uninstalled so we save the default
				// one
				AlmostNexusSettingsHelper.setThemePackageName(this,
						Launcher.THEME_DEFAULT);
			}
		}
		if (themeResources != null) {
			// Action Buttons
			/*
			 * loadThemeResource(themeResources,themePackage,"lab_bg",mLAB,
			 * THEME_ITEM_BACKGROUND);
			 * loadThemeResource(themeResources,themePackage
			 * ,"rab_bg",mRAB,THEME_ITEM_BACKGROUND);
			 * loadThemeResource(themeResources
			 * ,themePackage,"lab2_bg",mLAB2,THEME_ITEM_BACKGROUND);
			 * loadThemeResource
			 * (themeResources,themePackage,"rab2_bg",mRAB2,THEME_ITEM_BACKGROUND
			 * );
			 * loadThemeResource(themeResources,themePackage,"mab_bg",mHandleView
			 * ,THEME_ITEM_BACKGROUND);
			 */
			// App drawer button
			// loadThemeResource(themeResources,themePackage,"handle_icon",mHandleView,THEME_ITEM_FOREGROUND);
			// View appsBg=findViewById(R.id.appsBg);
			// loadThemeResource(themeResources,themePackage,"handle",appsBg,THEME_ITEM_BACKGROUND);
			// Deletezone
			// loadThemeResource(themeResources,themePackage,"ic_delete",deleteZone,THEME_ITEM_FOREGROUND);
			// bob 屏蔽删除
			// loadThemeResource(themeResources,themePackage,"delete_zone_selector",deleteZone,THEME_ITEM_BACKGROUND);
			// //Desktop dots
			// loadThemeResource(themeResources,themePackage,"home_arrows_left",mPreviousView,THEME_ITEM_FOREGROUND);
			// loadThemeResource(themeResources,themePackage,"home_arrows_right",mNextView,THEME_ITEM_FOREGROUND);
			// Dockbar
			loadThemeResource(themeResources, themePackage, "dockbar_bg",
					mMiniLauncher, THEME_ITEM_BACKGROUND);
			try {
				themeFont = Typeface.createFromAsset(
						themeResources.getAssets(), "themefont.ttf");
			} catch (RuntimeException e) {
				// TODO: handle exception
			}
		}
		/*
		 * Drawable previous = mPreviousView.getDrawable(); Drawable next =
		 * mNextView.getDrawable(); bob mWorkspace.setIndicators(previous,
		 * next);
		 */
		// ADW: EOF Themes
		updateAlmostNexusUI();

	}

	/**
	 * Creates a view representing a shortcut.
	 * 
	 * @param info
	 *            The data structure describing the shortcut.
	 * 
	 * @return A View inflated from R.layout.application.
	 */
	View createShortcut(ApplicationInfo info) {
		return createShortcut(
				R.layout.application,
				(ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()),
				info);
	}

	View createShortcut(ApplicationInfo info, boolean miniWorkspace) {
		return createShortcut(R.layout.application, null, info);
	}

	/**
	 * Creates a view representing a shortcut inflated from the specified
	 * resource.
	 * 
	 * @param layoutResId
	 *            The id of the XML layout used to create the shortcut.
	 * @param parent
	 *            The group the shortcut belongs to.
	 * @param info
	 *            The data structure describing the shortcut.
	 * 
	 * @return A View inflated from layoutResId.
	 */
	View createShortcut(int layoutResId, ViewGroup parent, ApplicationInfo info) {
		CounterTextView favorite = (CounterTextView) mInflater.inflate(
				layoutResId, parent, false);

		if (!info.filtered) {
			info.icon = Utilities.createIconThumbnail(info.icon, this);
			info.filtered = true;
		}

		favorite.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null,
				null);
		if (!uiHideLabels)
			favorite.setText(info.title);
		favorite.setTag(info);
		favorite.setOnClickListener(this);
		// ADW: Custom font
		if (themeFont != null)
			favorite.setTypeface(themeFont);
		// ADW: Counters stuff
		favorite.setCounter(info.counter, info.counterColor);
		return favorite;
	}

	View createShortcut(int layoutResId, ApplicationInfo info) {
		CounterTextView favorite = (CounterTextView) mInflater.inflate(
				layoutResId, null, false);

		if (!info.filtered) {
			info.icon = Utilities.createIconThumbnail(info.icon, this);
			info.filtered = true;
		}

		favorite.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null,
				null);
		if (!uiHideLabels)
			favorite.setText(info.title);
		favorite.setTag(info);
		favorite.setOnClickListener(this);
		// ADW: Custom font
		if (themeFont != null)
			favorite.setTypeface(themeFont);
		// ADW: Counters stuff
		favorite.setCounter(info.counter, info.counterColor);
		return favorite;
	}

	/**
	 * Add an application shortcut to the workspace.
	 * 
	 * @param data
	 *            The intent describing the application.
	 * @param cellInfo
	 *            The position on screen where to create the shortcut.
	 */
	void completeAddApplication(Context context, Intent data,
			CellLayout.CellInfo cellInfo, boolean insertAtFirst) {
		cellInfo.screen = mWorkspace.getCurrentScreen();
		if (!findSingleSlot(cellInfo))
			return;

		final ApplicationInfo info = infoFromApplicationIntent(context, data);
		if (info != null) {
			mWorkspace.addApplicationShortcut(info, cellInfo, insertAtFirst);
		}
	}

	private static ApplicationInfo infoFromApplicationIntent(Context context,
			Intent data) {
		ComponentName component = data.getComponent();
		PackageManager packageManager = context.getPackageManager();
		ActivityInfo activityInfo = null;
		try {
			activityInfo = packageManager.getActivityInfo(component, 0 /*
																		 * no
																		 * flags
																		 */);
		} catch (NameNotFoundException e) {
			e(LOG_TAG, "Couldn't find ActivityInfo for selected application", e);
		}

		if (activityInfo != null) {
			ApplicationInfo itemInfo = new ApplicationInfo();

			itemInfo.title = activityInfo.loadLabel(packageManager);
			if (itemInfo.title == null) {
				itemInfo.title = activityInfo.name;
			}

			itemInfo.setActivity(component, Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			// itemInfo.icon = activityInfo.loadIcon(packageManager);
			itemInfo.container = ItemInfo.NO_ID;

			itemInfo.icon = LauncherModel.getIcon(packageManager, context,
					activityInfo);

			return itemInfo;
		}

		return null;
	}

	/**
	 * Add a shortcut to the workspace.
	 * 
	 * @param data
	 *            The intent describing the shortcut.
	 * @param cellInfo
	 *            The position on screen where to create the shortcut.
	 * @param insertAtFirst
	 */
	private void completeAddShortcut(Intent data, CellLayout.CellInfo cellInfo,
			boolean insertAtFirst) {
		cellInfo.screen = mWorkspace.getCurrentScreen();
		if (!findSingleSlot(cellInfo))
			return;

		final ApplicationInfo info = addShortcut(this, data, cellInfo, false);

		if (!mRestoring) {
			sModel.addDesktopItem(info);

			final View view = createShortcut(info);
			mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY,
					1, 1, insertAtFirst);
		} else if (sModel.isDesktopLoaded()) {
			sModel.addDesktopItem(info);
		}
	}

	public View completeAddShortcut2(ApplicationInfo info, int screen,
			int cellX, int cellY) {

		sModel.addDesktopItem(info);

		LauncherModel.addOrMoveItemInDatabase(mLauncher, info,
				LauncherSettings.Favorites.CONTAINER_DESKTOP, screen, cellX,
				cellY);

		final View view = createShortcut(R.layout.application,
				(ViewGroup) mWorkspace.getChildAt(screen), info);
		mWorkspace.addInScreen(view, screen, cellX, cellY, 1, 1, false);

		return view;
	}

	// 批量添加使用的添加应用方法
	public View batchAddShortcut(ApplicationInfo info, int screen, int cellX,
			int cellY) {

		sModel.addDesktopItem(info);

		LauncherModel.addOrMoveItemInDatabase(mLauncher, info,
				LauncherSettings.Favorites.CONTAINER_DESKTOP, screen, cellX,
				cellY, saveMob);

		final View view = createShortcut(R.layout.application,
				(ViewGroup) mWorkspace.getChildAt(screen), info);
		mWorkspace.addInScreen(view, screen, cellX, cellY, 1, 1, false);

		return view;
	}

	/**
	 * Add a widget to the workspace.
	 * 
	 * @param data
	 *            The intent describing the appWidgetId.
	 * @param cellInfo
	 *            The position on screen where to create the widget.
	 */
	private void completeAddAppWidget(Intent data,
			CellLayout.CellInfo cellInfo, final boolean insertAtFirst) {

		Bundle extras = data.getExtras();
		final int appWidgetId = extras.getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

		if (LOGD)
			d(LOG_TAG, "dumping extras content=" + extras.toString());

		final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
				.getAppWidgetInfo(appWidgetId);

		// Calculate the grid spans needed to fit this widget
		CellLayout layout = (CellLayout) mWorkspace.getChildAt(cellInfo.screen);
		final int[] spans = layout.rectToCell(appWidgetInfo.minWidth,
				appWidgetInfo.minHeight);
		final CellLayout.CellInfo cInfo = cellInfo;
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		// zy:mark
		// final View dlg_layout = View.inflate(Launcher.this,
		// R.layout.widget_span_setup, null);
		// final NumberPicker ncols = (NumberPicker) dlg_layout
		// .findViewById(R.id.widget_columns_span);
		// ncols.setRange(1, mWorkspace.currentDesktopColumns());
		// ncols.setCurrent(spans[0]);
		// final NumberPicker nrows = (NumberPicker) dlg_layout
		// .findViewById(R.id.widget_rows_span);
		// nrows.setRange(1, mWorkspace.currentDesktopRows());
		// nrows.setCurrent(spans[1]);
		// builder = new AlertDialog.Builder(Launcher.this);
		// builder.setView(dlg_layout);
		// alertDialog = builder.create();
		// alertDialog.setTitle(getResources().getString(
		// R.string.widget_config_dialog_title));
		// alertDialog.setMessage(getResources().getString(
		// R.string.widget_config_dialog_summary));
		// alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources()
		// .getString(android.R.string.ok),
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// spans[0] = ncols.getCurrent();
		// spans[1] = nrows.getCurrent();
		// realAddWidget(appWidgetInfo, cInfo, spans, appWidgetId,
		// insertAtFirst);
		// }
		// });
		// alertDialog.show();

		// new
		realAddWidget(appWidgetInfo, cInfo, spans, appWidgetId, insertAtFirst);
	}

	public LauncherAppWidgetHost getAppWidgetHost() {
		return mAppWidgetHost;
	}

	static ApplicationInfo addShortcut(Context context, Intent data,
			CellLayout.CellInfo cellInfo, boolean notify) {

		final ApplicationInfo info = infoFromShortcutIntent(context, data);
		LauncherModel.addItemToDatabase(context, info,
				LauncherSettings.Favorites.CONTAINER_DESKTOP, cellInfo.screen,
				cellInfo.cellX, cellInfo.cellY, notify);

		return info;
	}

	static ApplicationInfo addShortcutInstall(Context context, Intent data,
			CellLayout.CellInfo cellInfo, boolean notify) {

		final ApplicationInfo info = infoFromShortcutIntent(context, data);
		LauncherModel.addItemToDatabaseInstall(context, info,
				LauncherSettings.Favorites.CONTAINER_DESKTOP, cellInfo.screen,
				cellInfo.cellX, cellInfo.cellY, notify);

		return info;
	}

	private static ApplicationInfo infoFromShortcutIntent(Context context,
			Intent data) {
		Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		Bitmap bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

		Drawable icon = null;
		boolean filtered = false;
		boolean customIcon = false;
		ShortcutIconResource iconResource = null;

		if (bitmap != null) {
			icon = new FastBitmapDrawable(Utilities.createBitmapThumbnail(
					bitmap, context));
			filtered = true;
			customIcon = true;
		} else {
			Parcelable extra = data
					.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
			if (extra != null && extra instanceof ShortcutIconResource) {
				try {
					iconResource = (ShortcutIconResource) extra;
					final PackageManager packageManager = context
							.getPackageManager();
					Resources resources = packageManager
							.getResourcesForApplication(iconResource.packageName);
					final int id = resources.getIdentifier(
							iconResource.resourceName, null, null);
					icon = resources.getDrawable(id);
				} catch (Exception e) {
					w(LOG_TAG, "Could not load shortcut icon: " + extra);
				}
			}
		}

		if (icon == null) {
			icon = context.getPackageManager().getDefaultActivityIcon();
		}

		final ApplicationInfo info = new ApplicationInfo();
		info.icon = icon;
		info.filtered = filtered;
		info.title = name;
		info.intent = intent;
		info.customIcon = customIcon;
		info.iconResource = iconResource;

		return info;
	}

	void closeSystemDialogs() {
		getWindow().closeAllPanels();

		try {
			dismissDialog(DIALOG_CREATE_SHORTCUT);
			// Unlock the workspace if the dialog was showing
			mWorkspace.unlock();
		} catch (Exception e) {
			// An exception is thrown if the dialog is not visible, which is
			// fine
		}

		try {
			dismissDialog(DIALOG_RENAME_FOLDER);
			// Unlock the workspace if the dialog was showing
			mWorkspace.unlock();
		} catch (Exception e) {
			// An exception is thrown if the dialog is not visible, which is
			// fine
		}
		try {
			dismissDialog(DIALOG_CHOOSE_GROUP);
			// Unlock the workspace if the dialog was showing
		} catch (Exception e) {
			// An exception is thrown if the dialog is not visible, which is
			// fine
		}
		try {
			dismissDialog(DIALOG_NEW_GROUP);
			// Unlock the workspace if the dialog was showing
		} catch (Exception e) {
			// An exception is thrown if the dialog is not visible, which is
			// fine
		}
		try {
			dismissDialog(DIALOG_DELETE_GROUP_CONFIRM);
			// Unlock the workspace if the dialog was showing
		} catch (Exception e) {
			// An exception is thrown if the dialog is not visible, which is
			// fine
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// Close the menu
		if (Intent.ACTION_MAIN.equals(intent.getAction())) {

			closeAllDrawer();

			if (allAppsOpen) {
				closeDrawer();
			} else {
				closeFolder();
			}
			if (isPreviewing()) {
				dismissPreviews();
			}
			if (mIsEditMode) {
				stopDesktopEdit();
			}
			if (mIsWidgetEditMode) {
				stopWidgetEdit();
			}

			closeSystemDialogs();

			// Set this flag so that onResume knows to close the search dialog
			// if it's open,
			// because this was a new intent (thus a press of 'home' or some
			// such) rather than
			// for example onResume being called when the user pressed the
			// 'back' button.
			mIsNewIntent = true;

			if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
				if (!isAllAppsVisible() || mHomeBinding == BIND_APPS)
					fireHomeBinding(mHomeBinding, 1);
				if (mHomeBinding != BIND_APPS) {
					closeDrawer(true);
				}
				final View v = getWindow().peekDecorView();
				if (v != null && v.getWindowToken() != null) {
					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			} else {
				closeDrawer(false);
			}
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// NOTE: Do NOT do this. Ever. This is a terrible and horrifying hack.
		//
		// Home loads the content of the workspace on a background thread. This
		// means that
		// a previously focused view will be, after orientation change, added to
		// the view
		// hierarchy at an undeterminate time in the future. If we were to
		// invoke
		// super.onRestoreInstanceState() here, the focus restoration would fail
		// because the
		// view to focus does not exist yet.
		//
		// However, not invoking super.onRestoreInstanceState() is equally bad.
		// In such a case,
		// panels would not be restored properly. For instance, if the menu is
		// open then the
		// user changes the orientation, the menu would not be opened in the new
		// orientation.
		//
		// To solve both issues Home messes up with the internal state of the
		// bundle to remove
		// the properties it does not want to see restored at this moment. After
		// invoking
		// super.onRestoreInstanceState(), it removes the panels state.
		//
		// Later, when the workspace is done loading, Home calls
		// super.onRestoreInstanceState()
		// again to restore focus and other view properties. It will not,
		// however, restore
		// the panels since at this point the panels' state has been removed
		// from the bundle.
		//
		// This is a bad example, do not do this.
		//
		// If you are curious on how this code was put together, take a look at
		// the following
		// in Android's source code:
		// - Activity.onRestoreInstanceState()
		// - PhoneWindow.restoreHierarchyState()
		// - PhoneWindow.DecorView.onAttachedToWindow()
		//
		// The source code of these various methods shows what states should be
		// kept to
		// achieve what we want here.

		Bundle windowState = savedInstanceState
				.getBundle("android:viewHierarchyState");
		SparseArray<Parcelable> savedStates = null;
		int focusedViewId = View.NO_ID;

		if (windowState != null) {
			savedStates = windowState.getSparseParcelableArray("android:views");
			windowState.remove("android:views");
			focusedViewId = windowState.getInt("android:focusedViewId",
					View.NO_ID);
			windowState.remove("android:focusedViewId");
		}

		super.onRestoreInstanceState(savedInstanceState);

		if (windowState != null) {
			windowState.putSparseParcelableArray("android:views", savedStates);
			windowState.putInt("android:focusedViewId", focusedViewId);
			windowState.remove("android:Panels");
		}

		mSavedInstanceState = savedInstanceState;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// ADW: If we leave the menu open, on restoration it will try to auto
		// find
		// the ocupied cells. But this could happed before the workspace is
		// fully loaded,
		// so it can cause a NPE cause of the way we load the desktop
		// columns/rows count.
		// I prefer to just close it than diggin the code to make it load
		// later...
		// Accepting patches :-)
		closeOptionsMenu();
		super.onSaveInstanceState(outState);

		outState.putInt(RUNTIME_STATE_CURRENT_SCREEN,
				mWorkspace.getCurrentScreen());

		final ArrayList<Folder> folders = mWorkspace.getOpenFolders();
		if (folders.size() > 0) {
			final int count = folders.size();
			long[] ids = new long[count];
			for (int i = 0; i < count; i++) {
				final FolderInfo info = folders.get(i).getInfo();
				ids[i] = info.id;
			}
			outState.putLongArray(RUNTIME_STATE_USER_FOLDERS, ids);
		}

		final boolean isConfigurationChange = getChangingConfigurations() != 0;

		// When the drawer is opened and we are saving the state because of a
		// configuration change
		if (allAppsOpen && isConfigurationChange) {
			outState.putBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, true);
		}
		if (mDockBar.isOpen()) {
			outState.putBoolean(RUNTIME_STATE_DOCKBAR, true);
		}
		if (mAddItemCellInfo != null && mAddItemCellInfo.valid
				&& mWaitingForResult) {
			final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
			final CellLayout layout = (CellLayout) mWorkspace
					.getChildAt(addItemCellInfo.screen);

			outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN,
					addItemCellInfo.screen);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X,
					addItemCellInfo.cellX);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y,
					addItemCellInfo.cellY);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X,
					addItemCellInfo.spanX);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y,
					addItemCellInfo.spanY);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_X,
					layout.getCountX());
			outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y,
					layout.getCountY());
			outState.putBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS,
					layout.getOccupiedCells());
		}

		if (mFolderInfo != null && mWaitingForResult) {
			outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
			outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID,
					mFolderInfo.id);
		}
	}

	@Override
	public void onDestroy() {

		if (stateMod == 3) {
			stateMod = 0;
		}

		SharedPreferences preferencesMode = getSharedPreferences("state_mode",
				0);
		Editor editor = preferencesMode.edit();
		editor.putInt("mode", stateMod);
		editor.commit();

		mDestroyed = true;
		// setPersistent(false);
		// ADW: unregister the sharedpref listener
		getSharedPreferences("launcher.preferences.almostnexus",
				Context.MODE_PRIVATE)
				.unregisterOnSharedPreferenceChangeListener(this);

		super.onDestroy();

		ActivityManager manager = (ActivityManager) mLauncher
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.dongji.desktopswitch.ADownloadService"
					.equals(service.service.getClassName())) {
				Intent tempIntent = new Intent();
				tempIntent.setClass(mLauncher, ADownloadService.class);
				mLauncher.stopService(tempIntent);
			}
		}

		try {
			mAppWidgetHost.stopListening();
		} catch (NullPointerException ex) {
			w(LOG_TAG,
					"problem while stopping AppWidgetHost during Launcher destruction",
					ex);
		}

		TextKeyListener.getInstance().release();

		mAllAppsGrid.clearTextFilter();
		mAllAppsGrid.setAdapter(null);

		sModel.unbind();
		sModel.abortLoaders();
		mWorkspace.unbindWidgetScrollableViews();
		getContentResolver().unregisterContentObserver(mObserver);
		getContentResolver().unregisterContentObserver(mWidgetObserver);
		unregisterReceiver(mApplicationsReceiver);
		unregisterReceiver(mCloseSystemDialogsReceiver);
		PopupWindow_Setting.unregisterSettingBroadcastReceiver(mLauncher);
		PopupWindow_ChooseWifi.unregisterChooseWifiBroadcastReceiver(mLauncher);
		if (mCounterReceiver != null)
			unregisterReceiver(mCounterReceiver);
		if (scrollableSupport)
			mWorkspace.unregisterProvider();

		mWorkspace.recyleWallPaper();
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		if (intent == null)
			return;
		// ADW: closing drawer, removed from onpause
		if (requestCode != REQUEST_SHOW_APP_LIST && // do not close drawer if it
													// is for switching
													// catalogue.
				!CustomShirtcutActivity.ACTION_LAUNCHERACTION.equals(intent
						.getAction()))

			closeDrawer(false);
		if (requestCode >= 0)
			mWaitingForResult = true;
		try {
			super.startActivityForResult(intent, requestCode);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "此应用未安装", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void startSearch(String initialQuery, boolean selectInitialQuery,
			Bundle appSearchData, boolean globalSearch) {

		closeDrawer(false);

		// Slide the search widget to the top, if it's on the current screen,
		// otherwise show the search dialog immediately.
		Search searchWidget = mWorkspace.findSearchWidgetOnCurrentScreen();
		if (searchWidget == null) {
			showSearchDialog(initialQuery, selectInitialQuery, appSearchData,
					globalSearch);
		} else {
			searchWidget.startSearch(initialQuery, selectInitialQuery,
					appSearchData, globalSearch);
			// show the currently typed text in the search widget while sliding
			searchWidget.setQuery(getTypedText());
		}
	}

	/**
	 * Show the search dialog immediately, without changing the search widget.
	 * 
	 * @see Activity#startSearch(String, boolean, android.os.Bundle, boolean)
	 */
	void showSearchDialog(String initialQuery, boolean selectInitialQuery,
			Bundle appSearchData, boolean globalSearch) {

		if (initialQuery == null) {
			// Use any text typed in the launcher as the initial query
			initialQuery = getTypedText();
			clearTypedText();
		}
		if (appSearchData == null) {
			appSearchData = new Bundle();
			appSearchData.putString("source", "launcher-search");
		}

		final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

		final Search searchWidget = mWorkspace
				.findSearchWidgetOnCurrentScreen();
		if (searchWidget != null) {
			// This gets called when the user leaves the search dialog to go
			// back to
			// the Launcher.
			searchManager
					.setOnCancelListener(new SearchManager.OnCancelListener() {
						public void onCancel() {
							searchManager.setOnCancelListener(null);
							stopSearch();
						}
					});
		}

		searchManager.startSearch(initialQuery, selectInitialQuery,
				getComponentName(), appSearchData, globalSearch);
	}

	/**
	 * Cancel search dialog if it is open.
	 */
	void stopSearch() {
		// Close search dialog
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchManager.stopSearch();
		// Restore search widget to its normal position
		Search searchWidget = mWorkspace.findSearchWidgetOnCurrentScreen();
		if (searchWidget != null) {
			searchWidget.stopSearch(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mDesktopLocked && mSavedInstanceState == null)
			return false;

		super.onCreateOptionsMenu(menu);
		// menu.add(MENU_GROUP_ADD, MENU_ADD, 0, R.string.menu_add)
		// .setIcon(android.R.drawable.ic_menu_add)
		// .setAlphabeticShortcut('A');
		// menu.add(MENU_GROUP_NORMAL, MENU_WALLPAPER_SETTINGS, 0,
		// R.string.menu_wallpaper)
		// .setIcon(android.R.drawable.ic_menu_gallery)
		// .setAlphabeticShortcut('W');
		// menu.add(MENU_GROUP_NORMAL, MENU_SEARCH, 0, R.string.menu_search)
		// .setIcon(android.R.drawable.ic_search_category_default)
		// .setAlphabeticShortcut(SearchManager.MENU_KEY);
		// menu.add(MENU_GROUP_NORMAL, MENU_NOTIFICATIONS, 0,
		// R.string.menu_edit)
		// .setIcon(android.R.drawable.ic_menu_edit)
		// .setAlphabeticShortcut('E');
		//
		// final Intent settings = new Intent(
		// android.provider.Settings.ACTION_SETTINGS);
		// settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		// | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		//
		// menu.add(MENU_GROUP_NORMAL, MENU_SETTINGS, 0, R.string.menu_settings)
		// .setIcon(android.R.drawable.ic_menu_preferences)
		// .setAlphabeticShortcut('P').setIntent(settings);
		// // ADW: add custom settings
		// menu.add(MENU_GROUP_NORMAL, MENU_ALMOSTNEXUS, 0,
		// R.string.menu_adw_settings)
		// .setIcon(android.R.drawable.ic_menu_preferences)
		// .setAlphabeticShortcut('X');
		//
		// menu.add(MENU_GROUP_CATALOGUE, MENU_APP_GRP_CONFIG, 0,
		// R.string.AppGroupConfig).setIcon(
		// android.R.drawable.ic_menu_agenda);
		// // menu.add(MENU_GROUP_CATALOGUE, MENU_APP_GRP_RENAME, 0,
		// // R.string.AppGroupRename)
		// // .setIcon(R.drawable.ic_menu_notifications);
		// menu.add(MENU_GROUP_CATALOGUE, MENU_APP_SWITCH_GRP, 0,
		// R.string.AppGroupChoose).setIcon(
		// android.R.drawable.ic_menu_manage);
		// menu.add(MENU_GROUP_CATALOGUE, MENU_APP_DELETE_GRP, 0,
		// R.string.AppGroupDel)
		// .setIcon(android.R.drawable.ic_menu_delete);
		// return true;

		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (mIsEditMode || mIsWidgetEditMode)
			return false;
		// We can't trust the view state here since views we may not be done
		// binding.
		// Get the vacancy state from the model instead.
		mMenuAddInfo = mWorkspace.findAllVacantCellsFromModel();
		menu.setGroupVisible(MENU_GROUP_ADD, mMenuAddInfo != null
				&& mMenuAddInfo.valid && (!allAppsOpen));
		menu.setGroupVisible(MENU_GROUP_NORMAL, !allAppsOpen);
		menu.setGroupVisible(MENU_GROUP_CATALOGUE, allAppsOpen);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD:
			addItems();
			return true;
		case MENU_WALLPAPER_SETTINGS:
			startWallpaper();
			return true;
		case MENU_SEARCH:
			onSearchRequested();
			return true;
		case MENU_NOTIFICATIONS:
			// showNotifications();
			// ADW: temp usage for desktop eiting

			if (allAppsOpen)
				closeAllApps(false);
			startDesktopEdit();
			return true;
		case MENU_ALMOSTNEXUS:
			showCustomConfig();
			return true;
		case MENU_APP_GRP_CONFIG:
			showAppList();
			return true;
		case MENU_APP_GRP_RENAME:
			showNewGrpDialog();
			return true;
		case MENU_APP_SWITCH_GRP:
			showSwitchGrp();
			return true;
		case MENU_APP_DELETE_GRP:
			showDeleteGrpDialog();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void showAppList() {

		final AppCatalogueFilter flt = sModel.getApplicationsAdapter()
				.getCatalogueFilter();
		if (!flt.isUserGroup()) {
			Toast.makeText(this, getString(R.string.AppGroupConfigError),
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent i = new Intent(this, AppInfoMList.class);
		i.putExtra(AppInfoMList.EXTRA_CATALOGUE_INDEX,
				flt.getCurrentFilterIndex());
		startActivityForResult(i, REQUEST_SHOW_APP_LIST);
	}

	void showDeleteGrpDialog() {
		if (!sModel.getApplicationsAdapter().getCatalogueFilter().isUserGroup()) {
			Toast.makeText(this, getString(R.string.AppGroupConfigError),
					Toast.LENGTH_SHORT).show();
			return;
		}
		showDialog(DIALOG_DELETE_GROUP_CONFIRM);
	}

	void showNewGrpDialog() {
		mWaitingForResult = true;
		showDialog(DIALOG_NEW_GROUP);
	}

	/**
	 * Indicates that we want global search for this activity by setting the
	 * globalSearch argument for {@link #startSearch} to true.
	 */

	@Override
	public boolean onSearchRequested() {
		startSearch(null, false, null, true);
		return true;
	}

	private void addItems() {
		showAddDialog(mMenuAddInfo);
	}

	private void removeShortcutsForPackage(String packageName) {
		if (packageName != null && packageName.length() > 0) {
			mWorkspace.removeShortcutsForPackage(packageName);
		}
	}

	private void updateShortcutsForPackage(String packageName) {
		if (packageName != null && packageName.length() > 0) {
			mWorkspace.updateShortcutsForPackage(packageName);
			// ADW: Update ActionButtons icons
			/*
			 * mLAB.reloadIcon(); mLAB2.reloadIcon(); mRAB.reloadIcon(); bob
			 * mRAB2.reloadIcon(); mHandleView.reloadIcon();
			 */
			mMiniLauncher.reloadIcons();
		}
	}

	void addAppWidget(final Intent data) {
		// TODO: catch bad widget exception when sent
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				-1);

		String customWidget = data.getStringExtra(EXTRA_CUSTOM_WIDGET);
		if (SEARCH_WIDGET.equals(customWidget)) {
			// We don't need this any more, since this isn't a real app widget.
			mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			// add the search widget
			addSearch();

		} else if (REMIND_CALENDAR_WIDGET.equals(customWidget)) {
			mAppWidgetHost.deleteAppWidgetId(appWidgetId);

			addRemindCalendar();
		} else if(SHORTCUT_CLOUD_APP_WIDGET.equals(customWidget)) {
			mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			addCustomShortcutWidget(customWidget);
		} else if(SHORTCUT_MY_APP_WIDGET.equals(customWidget)) {
			mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			addCustomShortcutWidget(customWidget);
		} else if(SHORTCUT_TASK_MANAGER_WIDGET.equals(customWidget)) {
			mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			addCustomShortcutWidget(customWidget);
		} else {
			AppWidgetProviderInfo appWidget = mAppWidgetManager
					.getAppWidgetInfo(appWidgetId);

			try {
				Bundle metadata = getPackageManager().getReceiverInfo(
						appWidget.provider, PackageManager.GET_META_DATA).metaData;
				if (metadata != null) {
					if (metadata
							.containsKey(LauncherMetadata.Requirements.APIVersion)) {
						int requiredApiVersion = metadata
								.getInt(LauncherMetadata.Requirements.APIVersion);
						if (requiredApiVersion > LauncherMetadata.CurrentAPIVersion) {
							onActivityResult(REQUEST_CREATE_APPWIDGET,
									Activity.RESULT_CANCELED, data);
							// Show a nice toast here to tell the user why the
							// widget is rejected.
							new AlertDialog.Builder(this)
									.setTitle(R.string.adw_version)
									.setCancelable(true)
									.setIcon(R.drawable.dongji_ico)
									.setPositiveButton(
											getString(android.R.string.ok),
											null)
									.setMessage(
											getString(R.string.scrollable_api_required))
									.create().show();
							return;
						}
					}
					// If there are Settings for scrollable or animations test
					// them here too!
					if (metadata
							.containsKey(LauncherMetadata.Requirements.Scrollable)) {
						boolean requiresScrolling = metadata
								.getBoolean(LauncherMetadata.Requirements.Scrollable);
						if (!isScrollableAllowed() && requiresScrolling) {
							// ask the user what to do
							AlertDialog.Builder dlg = new AlertDialog.Builder(
									this);
							dlg.setPositiveButton(
									getString(android.R.string.yes),
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											AlmostNexusSettingsHelper
													.setUIScrollableWidgets(
															Launcher.this, true);
											configureOrAddAppWidget(data);
										}
									});
							dlg.setNegativeButton(
									getString(android.R.string.no),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											onActivityResult(
													REQUEST_CREATE_APPWIDGET,
													Activity.RESULT_CANCELED,
													data);
										}
									});
							dlg.setMessage(getString(R.string.need_scrollable));
							dlg.create().show();
							return;
						}
					}
				}
			} catch (PackageManager.NameNotFoundException expt) {
				// No Metadata available... then it is all OK...
			}
			configureOrAddAppWidget(data);
		}
	}

	private void configureOrAddAppWidget(Intent data) {
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				-1);
		AppWidgetProviderInfo appWidget = mAppWidgetManager
				.getAppWidgetInfo(appWidgetId);
		if (appWidget.configure != null) {
			// Launch over to configure widget, if needed
			Intent intent = new Intent(
					AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(appWidget.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

			startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
		} else {
			// Otherwise just add it
			onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
		}
	}

	public boolean configureOrAddAppWidget2(Intent data) {
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				-1);
		AppWidgetProviderInfo appWidget = mAppWidgetManager
				.getAppWidgetInfo(appWidgetId);
		if (appWidget.configure != null) {
			// Launch over to configure widget, if needed
			Intent intent = new Intent(
					AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(appWidget.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

			startActivityForResult(intent, REQUEST_CREATE_APPWIDGET_2);

			return true;
		}
		return false;
	}

	public boolean configureOrAddAppWidget3(Intent data) {
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				-1);
		AppWidgetProviderInfo appWidget = mAppWidgetManager
				.getAppWidgetInfo(appWidgetId);

		if (appWidget == null) {
			return false;
		}

		if (appWidget.configure != null) {
			// Launch over to configure widget, if needed
			Intent intent = new Intent(
					AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(appWidget.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

			startActivityForResult(intent, REQUEST_CREATE_APPWIDGET_3);

			return true;
		}
		return false;
	}

	void addSearch() {
		final Widget info = Widget.makeSearch();
		final CellLayout.CellInfo cellInfo = mAddItemCellInfo;

		final int[] xy = mCellCoordinates;
		final int spanX = info.spanX;
		final int spanY = info.spanY;

		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		final View dlg_layout = View.inflate(Launcher.this,
				R.layout.widget_span_setup, null);
		final NumberPicker ncols = (NumberPicker) dlg_layout
				.findViewById(R.id.widget_columns_span);
		ncols.setRange(1, mWorkspace.currentDesktopColumns());
		ncols.setCurrent(spanX);
		final NumberPicker nrows = (NumberPicker) dlg_layout
				.findViewById(R.id.widget_rows_span);
		nrows.setRange(1, mWorkspace.currentDesktopRows());
		nrows.setCurrent(spanY);
		builder = new AlertDialog.Builder(Launcher.this);
		builder.setView(dlg_layout);
		alertDialog = builder.create();
		alertDialog.setTitle(getResources().getString(
				R.string.widget_config_dialog_title));
		alertDialog.setMessage(getResources().getString(
				R.string.widget_config_dialog_summary));
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources()
				.getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						int spanX = ncols.getCurrent();
						int spanY = nrows.getCurrent();
						realAddSearch(info, cellInfo, xy, spanX, spanY);
					}
				});
		alertDialog.show();
	}

	void addRemindCalendar() {
		final Widget info = Widget.makeRemindCalendar();
		final CellLayout.CellInfo cellInfo = mAddItemCellInfo;

		final int[] xy = mCellCoordinates;
		final int spanX = info.spanX;
		final int spanY = info.spanY;

		if (!findSlot(cellInfo, xy, spanX, spanY))
			return;

		info.spanX = spanX;
		info.spanY = spanY;
		sModel.addDesktopItem(info);
		LauncherModel.addItemToDatabase(this, info,
				LauncherSettings.Favorites.CONTAINER_DESKTOP,
				mWorkspace.getCurrentScreen(), xy[0], xy[1], false);

		// final View view = mInflater.inflate(info.layoutResource, null);
		// view.setTag(info);
		// Search search = (Search) view.findViewById(R.id.widget_search);
		// search.setLauncher(this);

		RemindCalendar remindCalendar = new RemindCalendar(this);
		remindCalendar.v.setTag(info);

		mWorkspace.addInCurrentScreen(remindCalendar.v, xy[0], xy[1], spanX,
				spanY);
		// rcs.add(remindCalendar);
	}
	
	void addCustomShortcutWidget(String customWidget) {
		
		Widget w = new Widget();
        w.spanX = 1;
        w.spanY = 1;
        View mContentView = LayoutInflater.from(
        		context).inflate(R.layout.layout_custom_widget, null, false);
        ImageView mImageView=(ImageView)mContentView.findViewById(R.id.custom_shortcut_imageview);
        TextView mTextView=(TextView)mContentView.findViewById(R.id.custom_shortcut_textview);
		if(SHORTCUT_CLOUD_APP_WIDGET.equals(customWidget)) {
			w.layoutResource = R.layout.layout_custom_widget;
			w.name = "动机云应用";
			w.itemType = LauncherSettings.Favorites.ITEM_TYPE_WIDGET_CLOUD_APP;
			mContentView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mVerScrollLayout.snapToScreen(1);

					Drawable mBackgroundDrawable = WallpaperManager.getInstance(
							Launcher.mLauncher).getDrawable();
					mAllDrawer.setBackgroundDrawable(mBackgroundDrawable);

					saveMob = stateMod;
					stateMod = 3;
					
					mAllDrawer.performByShortcut(1);
				}
			});
			mImageView.setBackgroundResource(R.drawable.cloud_app_icon);
		} else if(SHORTCUT_MY_APP_WIDGET.equals(customWidget)) {
			w.layoutResource = R.layout.layout_custom_widget;
			w.name = "我的应用";
			w.itemType = LauncherSettings.Favorites.ITEM_TYPE_WIDGET_MY_APP;
			mContentView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mVerScrollLayout.snapToScreen(1);

					Drawable mBackgroundDrawable = WallpaperManager.getInstance(
							Launcher.mLauncher).getDrawable();
					mAllDrawer.setBackgroundDrawable(mBackgroundDrawable);

					saveMob = stateMod;
					stateMod = 3;

					mAllDrawer.performFirst();
				}
			});
			mImageView.setBackgroundResource(R.drawable.my_app_icon);
		} else if(SHORTCUT_TASK_MANAGER_WIDGET.equals(customWidget)) {
			w.layoutResource = R.layout.layout_custom_widget;
			w.name = "任务管理";
			w.itemType = LauncherSettings.Favorites.ITEM_TYPE_WIDGET_TASK_MANAGER;
			mContentView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mVerScrollLayout.snapToScreen(1);

					Drawable mBackgroundDrawable = WallpaperManager.getInstance(
							Launcher.mLauncher).getDrawable();
					mAllDrawer.setBackgroundDrawable(mBackgroundDrawable);

					saveMob = stateMod;
					stateMod = 3;
					
					mAllDrawer.performByShortcut(2);
				}
			});
			mImageView.setBackgroundResource(R.drawable.task_manager_icon);
		}
		mTextView.setText(w.name);
		
		final CellLayout.CellInfo cellInfo = mAddItemCellInfo;

		final int[] xy = mCellCoordinates;
		final int spanX = w.spanX;
		final int spanY = w.spanY;

		if (!findSlot(cellInfo, xy, spanX, spanY))
			return;

		w.spanX = spanX;
		w.spanY = spanY;
		sModel.addDesktopItem(w);
		LauncherModel.addItemToDatabase(this, w,
				LauncherSettings.Favorites.CONTAINER_DESKTOP,
				mWorkspace.getCurrentScreen(), xy[0], xy[1], false);

		mContentView.setTag(w);
		
		mWorkspace.addInCurrentScreen(mContentView, xy[0], xy[1], spanX,
				spanY);
	}
	
	View addCustomShortcutWidgetByMini(String customWidget, int[] xy, int screen) {
		Widget w = new Widget();
        w.spanX = 1;
        w.spanY = 1;
        View mContentView = LayoutInflater.from(
        		context).inflate(R.layout.layout_custom_widget, null, false);
        ImageView mImageView=(ImageView)mContentView.findViewById(R.id.custom_shortcut_imageview);
        TextView mTextView=(TextView)mContentView.findViewById(R.id.custom_shortcut_textview);
		if(SHORTCUT_CLOUD_APP_WIDGET.equals(customWidget)) {
			w.layoutResource = R.layout.layout_custom_widget;
			w.name = "动机云应用";
			w.itemType = LauncherSettings.Favorites.ITEM_TYPE_WIDGET_CLOUD_APP;
			mContentView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mVerScrollLayout.snapToScreen(1);

					Drawable mBackgroundDrawable = WallpaperManager.getInstance(
							Launcher.mLauncher).getDrawable();
					mAllDrawer.setBackgroundDrawable(mBackgroundDrawable);

					saveMob = stateMod;
					stateMod = 3;
					
					mAllDrawer.performByShortcut(1);
				}
			});
			mImageView.setBackgroundResource(R.drawable.cloud_app_icon);
		} else if(SHORTCUT_MY_APP_WIDGET.equals(customWidget)) {
			w.layoutResource = R.layout.layout_custom_widget;
			w.name = "我的应用";
			w.itemType = LauncherSettings.Favorites.ITEM_TYPE_WIDGET_MY_APP;
			mContentView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mVerScrollLayout.snapToScreen(1);

					Drawable mBackgroundDrawable = WallpaperManager.getInstance(
							Launcher.mLauncher).getDrawable();
					mAllDrawer.setBackgroundDrawable(mBackgroundDrawable);

					saveMob = stateMod;
					stateMod = 3;
					
					mAllDrawer.performFirst();
				}
			});
			mImageView.setBackgroundResource(R.drawable.my_app_icon);
		} else if(SHORTCUT_TASK_MANAGER_WIDGET.equals(customWidget)) {
			w.layoutResource = R.layout.layout_custom_widget;
			w.name = "任务管理";
			w.itemType = LauncherSettings.Favorites.ITEM_TYPE_WIDGET_TASK_MANAGER;
			mContentView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mVerScrollLayout.snapToScreen(1);

					Drawable mBackgroundDrawable = WallpaperManager.getInstance(
							Launcher.mLauncher).getDrawable();
					mAllDrawer.setBackgroundDrawable(mBackgroundDrawable);

					saveMob = stateMod;
					stateMod = 3;
					
					mAllDrawer.performByShortcut(2);
				}
			});
			mImageView.setBackgroundResource(R.drawable.task_manager_icon);
		}
		mTextView.setText(w.name);
		
		final int spanX = w.spanX;
		final int spanY = w.spanY;

		w.cellX=xy[0];
		w.cellY=xy[1];
		w.screen= screen;
		w.spanX = spanX;
		w.spanY = spanY;
		sModel.addDesktopItem(w);
		LauncherModel.addItemToDatabase(this, w,
				LauncherSettings.Favorites.CONTAINER_DESKTOP,
				screen, xy[0], xy[1], false, 0);

		mContentView.setTag(w);
		
		mWorkspace.addInScreen(mContentView, screen, xy[0], xy[1], spanX,
				spanY);
		return mContentView;
	}

	View addRemindCalendar2(int screen, int cellX, int cellY) {
		final Widget info = Widget.makeRemindCalendar();

		final int spanX = info.spanX;
		final int spanY = info.spanY;

		info.spanX = spanX;
		info.spanY = spanY;
		sModel.addDesktopItem(info);
		LauncherModel.addItemToDatabase(this, info,
				LauncherSettings.Favorites.CONTAINER_DESKTOP, screen, cellX,
				cellY, false);

		RemindCalendar remindCalendar = new RemindCalendar(this);
		remindCalendar.v.setTag(info);
		mWorkspace.addInScreen(remindCalendar.v, screen, cellX, cellY, spanX,
				spanY);

		// rcs.add(remindCalendar);

		return remindCalendar.v;
	}

	View addRemindCalendar3(int screen, int cellX, int cellY) {
		final Widget info = Widget.makeRemindCalendar();

		final int spanX = info.spanX;
		final int spanY = info.spanY;

		info.spanX = spanX;
		info.spanY = spanY;
		sModel.addDesktopItem(info);
		LauncherModel.addItemToDatabase(this, info,
				LauncherSettings.Favorites.CONTAINER_DESKTOP, screen, cellX,
				cellY, false, saveMob);

		RemindCalendar remindCalendar = new RemindCalendar(this);
		remindCalendar.v.setTag(info);
		mWorkspace.addInScreen(remindCalendar.v, screen, cellX, cellY, spanX,
				spanY);

		// rcs.add(remindCalendar);
		return remindCalendar.v;
	}

	void processShortcut(Intent intent, int requestCodeApplication,
			int requestCodeShortcut) {
		// Handle case where user selected "Applications"
		String applicationName = getResources().getString(
				R.string.group_applications);
		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

		if (applicationName != null && applicationName.equals(shortcutName)) {
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
			pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
			startActivityForResult(pickIntent, requestCodeApplication);
		} else {
			startActivityForResult(intent, requestCodeShortcut);
		}
	}

	void addLiveFolder(Intent intent) {
		// Handle case where user selected "Folder"
		String folderName = getResources().getString(R.string.group_folder);
		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

		if (folderName != null && folderName.equals(shortcutName)) {
			addFolder(!mDesktopLocked);
		} else {
			startActivityForResult(intent, REQUEST_CREATE_LIVE_FOLDER);
		}
	}

	void addFolder(boolean insertAtFirst) {
		UserFolderInfo folderInfo = new UserFolderInfo();
		folderInfo.title = getText(R.string.folder_name);

		CellLayout.CellInfo cellInfo = mAddItemCellInfo;
		cellInfo.screen = mWorkspace.getCurrentScreen();
		if (!findSingleSlot(cellInfo))
			return;

		// Update the model
		LauncherModel.addItemToDatabase(this, folderInfo,
				LauncherSettings.Favorites.CONTAINER_DESKTOP,
				mWorkspace.getCurrentScreen(), cellInfo.cellX, cellInfo.cellY,
				false);
		sModel.addDesktopItem(folderInfo);
		sModel.addFolder(folderInfo);

		// Create the view
		FolderIcon newFolder = FolderIcon
				.fromXml(R.layout.folder_icon, this, (ViewGroup) mWorkspace
						.getChildAt(mWorkspace.getCurrentScreen()), folderInfo);
		if (themeFont != null)
			((TextView) newFolder).setTypeface(themeFont);
		mWorkspace.addInCurrentScreen(newFolder, cellInfo.cellX,
				cellInfo.cellY, 1, 1, insertAtFirst);
	}

	private void completeAddLiveFolder(Intent data,
			CellLayout.CellInfo cellInfo, boolean insertAtFirst) {
		cellInfo.screen = mWorkspace.getCurrentScreen();
		if (!findSingleSlot(cellInfo))
			return;

		final LiveFolderInfo info = addLiveFolder(this, data, cellInfo, false);

		if (!mRestoring) {
			sModel.addDesktopItem(info);

			final View view = LiveFolderIcon.fromXml(R.layout.live_folder_icon,
					this, (ViewGroup) mWorkspace.getChildAt(mWorkspace
							.getCurrentScreen()), info);
			if (themeFont != null)
				((TextView) view).setTypeface(themeFont);
			mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY,
					1, 1, insertAtFirst);
		} else if (sModel.isDesktopLoaded()) {
			sModel.addDesktopItem(info);
		}
	}

	static LiveFolderInfo addLiveFolder(Context context, Intent data,
			CellLayout.CellInfo cellInfo, boolean notify) {

		Intent baseIntent = data
				.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT);
		String name = data.getStringExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME);

		Drawable icon = null;
		boolean filtered = false;
		Intent.ShortcutIconResource iconResource = null;

		Parcelable extra = data
				.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON);
		if (extra != null && extra instanceof Intent.ShortcutIconResource) {
			try {
				iconResource = (Intent.ShortcutIconResource) extra;
				final PackageManager packageManager = context
						.getPackageManager();
				Resources resources = packageManager
						.getResourcesForApplication(iconResource.packageName);
				final int id = resources.getIdentifier(
						iconResource.resourceName, null, null);
				icon = resources.getDrawable(id);
			} catch (Exception e) {
				w(LOG_TAG, "Could not load live folder icon: " + extra);
			}
		}

		if (icon == null) {
			icon = context.getResources().getDrawable(
					R.drawable.ic_launcher_folder);
		}

		final LiveFolderInfo info = new LiveFolderInfo();
		info.icon = icon;
		info.filtered = filtered;
		info.title = name;
		info.iconResource = iconResource;
		info.uri = data.getData();
		info.baseIntent = baseIntent;
		info.displayMode = data.getIntExtra(
				LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
				LiveFolders.DISPLAY_MODE_GRID);

		LauncherModel.addItemToDatabase(context, info,
				LauncherSettings.Favorites.CONTAINER_DESKTOP, cellInfo.screen,
				cellInfo.cellX, cellInfo.cellY, notify);
		sModel.addFolder(info);

		return info;
	}

	private boolean findSingleSlot(CellLayout.CellInfo cellInfo) {
		final int[] xy = new int[2];
		if (findSlot(cellInfo, xy, 1, 1)) {
			cellInfo.cellX = xy[0];
			cellInfo.cellY = xy[1];
			return true;
		}
		return false;
	}

	private boolean findSlot(CellLayout.CellInfo cellInfo, int[] xy, int spanX,
			int spanY) {
		if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
			boolean[] occupied = mSavedState != null ? mSavedState
					.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS)
					: null;
			cellInfo = mWorkspace.findAllVacantCells(occupied);
			if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
				Toast.makeText(this, getString(R.string.out_of_space),
						Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return true;
	}

	private void showNotifications() {
		if (hideStatusBar) {
			fullScreen(false);
			mShouldHideStatusbaronFocus = true;
		}
		try {
			Object service = getSystemService("statusbar");
			if (service != null) {
				Method expand = service.getClass().getMethod("expand");
				expand.invoke(service);
			}
		} catch (Exception e) {
		}
	}

	private void startWallpaper() {
		final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
		Intent chooser = Intent.createChooser(pickWallpaper,
				getText(R.string.chooser_wallpaper));
		WallpaperManager wm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
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
	 * Registers various intent receivers. The current implementation registers
	 * only a wallpaper intent receiver to let other applications change the
	 * wallpaper.
	 */
	private void registerIntentReceivers() {
		boolean useNotifReceiver = AlmostNexusSettingsHelper
				.getNotifReceiver(this);
		if (useNotifReceiver && mCounterReceiver == null) {
			mCounterReceiver = new CounterReceiver(this);
			mCounterReceiver
					.setCounterListener(new CounterReceiver.OnCounterChangedListener() {
						public void onTrigger(String pname, int counter,
								int color) {
							updateCountersForPackage(pname, counter, color);
						}
					});
			registerReceiver(mCounterReceiver, mCounterReceiver.getFilter());
		}
		if (sWallpaperReceiver == null) {
			final Application application = getApplication();

			sWallpaperReceiver = new WallpaperIntentReceiver(application, this);

			IntentFilter filter = new IntentFilter(
					Intent.ACTION_WALLPAPER_CHANGED);
			application.registerReceiver(sWallpaperReceiver, filter);
		} else {
			sWallpaperReceiver.setLauncher(this);
		}
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, filter);
		filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		registerReceiver(mCloseSystemDialogsReceiver, filter);
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
		registerReceiver(mApplicationsReceiver, filter);

	}

	/**
	 * Registers various content observers. The current implementation registers
	 * only a favorites observer to keep track of the favorites applications.
	 */
	private void registerContentObservers() {
		ContentResolver resolver = getContentResolver();
		resolver.registerContentObserver(
				LauncherSettings.Favorites.CONTENT_URI, true, mObserver);
		resolver.registerContentObserver(
				LauncherProvider.CONTENT_APPWIDGET_RESET_URI, true,
				mWidgetObserver);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				return true;
			case KeyEvent.KEYCODE_HOME:
				return true;
			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				if (!event.isCanceled()) {
					mWorkspace.dispatchKeyEvent(event);

					closeAllDrawer();

					if (allAppsOpen) {
						closeDrawer();
					} else {
						closeFolder();
					}
					if (isPreviewing()) {
						dismissPreviews();
					}
					if (mIsEditMode) {
						stopDesktopEdit();
					}
					if (mIsWidgetEditMode) {
						stopWidgetEdit();
					}
				}
				return true;
			case KeyEvent.KEYCODE_HOME:
				return true;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	private void closeAllDrawer() {
		// if (mAllDrawer.getVisibility() == View.VISIBLE) {
		// mDesktopLayout.setVisibility(View.VISIBLE);
		// mAllDrawer.close();
		if (mVerScrollLayout.getCurScreen() == 1) {
			mVerScrollLayout.snapToScreen(0);
			stateMod = saveMob;
		}
	}

	private void closeDrawer() {
		closeDrawer(true);
	}

	private void closeDrawer(boolean animated) {
		if (allAppsOpen) {
			if (animated) {
				closeAllApps(true);
			} else {
				closeAllApps(false);
			}
			if (mAllAppsGrid.hasFocus()) {
				mWorkspace.getChildAt(mWorkspace.getCurrentScreen())
						.requestFocus();
			}
		}
	}

	private void closeFolder() {
		Folder folder = mWorkspace.getOpenFolder();
		if (folder != null) {
			closeFolder(folder);
		}
	}

	void closeFolder(Folder folder) {
		folder.getInfo().opened = false;
		ViewGroup parent = (ViewGroup) folder.getParent();
		if (parent != null) {
			parent.removeView(folder);
		}
		folder.onClose();
	}

	void closeFolder(Folder folder, ApplicationInfo app) {
		folder.getInfo().opened = false;
		ViewGroup parent = (ViewGroup) folder.getParent();
		if (parent != null) {
			parent.removeView(folder);
		}
		folder.onClose();

		opening_floder_icon.refreshAfterDropOut(app);
	}

	/**
	 * When the notification that favorites have changed is received, requests a
	 * favorites list refresh.
	 */
	private void onFavoritesChanged() {
//		mDesktopLocked = true;
		sModel.loadUserItems(false, this, false, false, 0);
	}

	/**
	 * Re-listen when widgets are reset.
	 */
	private void onAppWidgetReset() {
		mAppWidgetHost.startListening();
	}

	void onDesktopItemsLoaded(ArrayList<ItemInfo> shortcuts,
			ArrayList<LauncherAppWidgetInfo> appWidgets) {
		if (mDestroyed) {
			if (LauncherModel.DEBUG_LOADERS) {
				d(LauncherModel.LOG_TAG,
						"  ------> destroyed, ignoring desktop items");
			}
			return;
		}
		bindDesktopItems(shortcuts, appWidgets);
	}

	/**
	 * Refreshes the shortcuts shown on the workspace.
	 */
	private void bindDesktopItems(ArrayList<ItemInfo> shortcuts,
			ArrayList<LauncherAppWidgetInfo> appWidgets) {

		final ApplicationsAdapter drawerAdapter = sModel
				.getApplicationsAdapter();
		if (shortcuts == null || appWidgets == null || drawerAdapter == null) {
			if (LauncherModel.DEBUG_LOADERS)
				d(LauncherModel.LOG_TAG, "  ------> a source is null");
			return;
		}

		if (mVerScrollLayout.getCurScreen() == 0) {

			final Workspace workspace = mWorkspace;
			int count = workspace.getChildCount();
			for (int i = 0; i < count; i++) {
				((ViewGroup) workspace.getChildAt(i)).removeAllViewsInLayout();
			}

			final MiniLauncher miniLauncher = (MiniLauncher) mDragLayer
					.findViewById(R.id.mini_content);
			miniLauncher.removeAllViewsInLayout();

			if (DEBUG_USER_INTERFACE) {
				android.widget.Button finishButton = new android.widget.Button(
						this);
				finishButton.setText("Finish");
				workspace.addInScreen(finishButton, 1, 0, 0, 1, 1);

				finishButton
						.setOnClickListener(new android.widget.Button.OnClickListener() {
							public void onClick(View v) {
								finish();
							}
						});
			}
		}

		// Flag any old binder to terminate early
		if (mBinder != null) {
			mBinder.mTerminate = true;
		}

		mBinder = new DesktopBinder(this, shortcuts, appWidgets, drawerAdapter);
		mBinder.startBindingItems();
	}

	private void bindItems(Launcher.DesktopBinder binder,
			ArrayList<ItemInfo> shortcuts, int start, int count) {

		// final int end = Math.max(start + DesktopBinder.ITEMS_COUNT, count);
		// int i = start;
		int i = 0;

		if (mVerScrollLayout.getCurScreen() == 1
				&& mAllDrawer.getVisibility() == View.VISIBLE) {
			mAllDrawer.bindItems(shortcuts, start, count);
			/*
			 * if (end >= count) { finishBindDesktopItems();
			 * binder.startBindingDrawer(); } else {
			 * binder.obtainMessage(DesktopBinder.MESSAGE_BIND_ITEMS, i,
			 * count).sendToTarget(); }
			 */
			return;
		}

		final Workspace workspace = mWorkspace;
		final boolean desktopLocked = mDesktopLocked;
		final MiniLauncher miniLauncher = (MiniLauncher) mDragLayer
				.findViewById(R.id.mini_content);

		for (; i < shortcuts.size(); i++) {
			final ItemInfo item = shortcuts.get(i);
			switch ((int) item.container) {
			/*
			 * case LauncherSettings.Favorites.CONTAINER_LAB:
			 * mLAB.UpdateLaunchInfo(item); break; case
			 * LauncherSettings.Favorites.CONTAINER_RAB:
			 * mRAB.UpdateLaunchInfo(item); break; case
			 * LauncherSettings.Favorites.CONTAINER_LAB2:
			 * mLAB2.UpdateLaunchInfo(item); break; case
			 * LauncherSettings.Favorites.CONTAINER_RAB2:
			 * mRAB2.UpdateLaunchInfo(item); bob break; case
			 * LauncherSettings.Favorites.CONTAINER_MAB:
			 * mHandleView.UpdateLaunchInfo(item); break;
			 */
			case LauncherSettings.Favorites.CONTAINER_DOCKBAR:
				miniLauncher.addItemInDockBar(item);
				break;
			default:
				switch (item.itemType) {
				case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
				case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
					final View shortcut = createShortcut((ApplicationInfo) item);
					workspace.addInScreen(shortcut, item.screen, item.cellX,
							item.cellY, 1, 1, !desktopLocked);
					break;
				case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
					final FolderIcon newFolder = FolderIcon.fromXml(
							R.layout.folder_icon, this, (ViewGroup) workspace
									.getChildAt(workspace.getCurrentScreen()),
							(UserFolderInfo) item);
					if (themeFont != null)
						((TextView) newFolder).setTypeface(themeFont);
					workspace.addInScreen(newFolder, item.screen, item.cellX,
							item.cellY, 1, 1, !desktopLocked);
					break;
				case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
					final FolderIcon newLiveFolder = LiveFolderIcon
							.fromXml(R.layout.live_folder_icon, this,
									(ViewGroup) workspace.getChildAt(workspace
											.getCurrentScreen()),
									(LiveFolderInfo) item);
					if (themeFont != null)
						((TextView) newLiveFolder).setTypeface(themeFont);
					workspace.addInScreen(newLiveFolder, item.screen,
							item.cellX, item.cellY, 1, 1, !desktopLocked);
					break;

				case LauncherSettings.Favorites.ITEM_TYPE_WIDGET_SEARCH:
					final int screen = workspace.getCurrentScreen();
					final View view = mInflater.inflate(R.layout.widget_search,
							(ViewGroup) workspace.getChildAt(screen), false);

					Search search = (Search) view
							.findViewById(R.id.widget_search);
					search.setLauncher(this);

					final Widget widget = (Widget) item;
					view.setTag(widget);

					workspace.addWidget(view, widget, !desktopLocked);
					break;

				case LauncherSettings.Favorites.ITEM_TYPE_WIDGET_REMIND_CALENDAR:
					// final int screen1 = workspace.getCurrentScreen();
					// final View view1 =
					// mInflater.inflate(R.layout.widget_remind_calendar,
					// (ViewGroup) workspace.getChildAt(screen1), false);

					// Search search = (Search)
					// view.findViewById(R.id.widget_search);
					// search.setLauncher(this);

					final Widget widget1 = (Widget) item;

					RemindCalendar remindCalendar = new RemindCalendar(this);
					remindCalendar.v.setTag(widget1);
					// rcs.add(remindCalendar);
					workspace.addWidget(remindCalendar.v, widget1,
							!desktopLocked);
					break;
				case LauncherSettings.Favorites.ITEM_TYPE_WIDGET_CLOUD_APP:
					final Widget widget2 = (Widget) item;
					View mContentView = LayoutInflater.from(
			        		context).inflate(R.layout.layout_custom_widget, null, false);
			        ImageView mImageView=(ImageView)mContentView.findViewById(R.id.custom_shortcut_imageview);
			        TextView mTextView=(TextView)mContentView.findViewById(R.id.custom_shortcut_textview);
			        mTextView.setText("动机云应用");
			        widget2.name="动机云应用";
			        mImageView.setBackgroundResource(R.drawable.cloud_app_icon);
//			        LinearLayout.LayoutParams mParams1=(LinearLayout.LayoutParams)mImageView.getLayoutParams();
//			        mParams1.width=AndroidUtils.dip2px(this, 55);
//			        mParams1.height=AndroidUtils.dip2px(this, 40);
//			        mImageView.setLayoutParams(mParams1);
			        mContentView.setTag(widget2);
			        mContentView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mVerScrollLayout.snapToScreen(1);

							Drawable mBackgroundDrawable = WallpaperManager.getInstance(
									Launcher.mLauncher).getDrawable();
							mAllDrawer.setBackgroundDrawable(mBackgroundDrawable);

							saveMob = stateMod;
							stateMod = 3;
							
							mAllDrawer.performByShortcut(1);
						}
					});
					mWorkspace.addInScreen(mContentView, widget2.screen,
							widget2.cellX, widget2.cellY, 1, 1, false);
					break;
				case LauncherSettings.Favorites.ITEM_TYPE_WIDGET_MY_APP:
					final Widget widget3 = (Widget) item;
					View mContentView2 = LayoutInflater.from(
			        		context).inflate(R.layout.layout_custom_widget, null, false);
			        ImageView mImageView2=(ImageView)mContentView2.findViewById(R.id.custom_shortcut_imageview);
			        TextView mTextView2=(TextView)mContentView2.findViewById(R.id.custom_shortcut_textview);
			        mTextView2.setText("我的应用");
			        widget3.name="我的应用";
			        mImageView2.setBackgroundResource(R.drawable.my_app_icon);
//			        LinearLayout.LayoutParams mParams2=(LinearLayout.LayoutParams)mImageView2.getLayoutParams();
//			        mParams2.width=AndroidUtils.dip2px(this, 45);
//			        mParams2.height=AndroidUtils.dip2px(this, 50);
//			        mImageView2.setLayoutParams(mParams2);
			        mContentView2.setTag(widget3);
			        mContentView2.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mVerScrollLayout.snapToScreen(1);

							Drawable mBackgroundDrawable = WallpaperManager.getInstance(
									Launcher.mLauncher).getDrawable();
							mAllDrawer.setBackgroundDrawable(mBackgroundDrawable);

							saveMob = stateMod;
							stateMod = 3;
							mAllDrawer.performFirst();
						}
					});
			        mWorkspace.addInScreen(mContentView2, widget3.screen,
			        		widget3.cellX, widget3.cellY, 1, 1, false);
					break;
				case LauncherSettings.Favorites.ITEM_TYPE_WIDGET_TASK_MANAGER:
					final Widget widget4 = (Widget) item;
					View mContentView3 = LayoutInflater.from(
			        		context).inflate(R.layout.layout_custom_widget, null, false);
			        ImageView mImageView3=(ImageView)mContentView3.findViewById(R.id.custom_shortcut_imageview);
			        TextView mTextView3=(TextView)mContentView3.findViewById(R.id.custom_shortcut_textview);
			        mTextView3.setText("任务管理");
			        widget4.name="任务管理";
			        mImageView3.setBackgroundResource(R.drawable.task_manager_icon);
//			        LinearLayout.LayoutParams mParams3=(LinearLayout.LayoutParams)mImageView3.getLayoutParams();
//			        mParams3.width=AndroidUtils.dip2px(this, 45);
//			        mParams3.height=AndroidUtils.dip2px(this, 40);
//			        mImageView3.setLayoutParams(mParams3);
			        mContentView3.setTag(widget4);
			        mContentView3.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mVerScrollLayout.snapToScreen(1);

							Drawable mBackgroundDrawable = WallpaperManager.getInstance(
									Launcher.mLauncher).getDrawable();
							mAllDrawer.setBackgroundDrawable(mBackgroundDrawable);

							saveMob = stateMod;
							stateMod = 3;
							
							mAllDrawer.performByShortcut(2);
						}
					});
			        mWorkspace.addInScreen(mContentView3, widget4.screen,
			        		widget4.cellX, widget4.cellY, 1, 1, false);
					break;
				}
			}
		}

		workspace.requestLayout();

		// if (end >= count) {
		finishBindDesktopItems();
		binder.startBindingDrawer();
		// } else {
		// binder.obtainMessage(DesktopBinder.MESSAGE_BIND_ITEMS, i, count)
		// .sendToTarget();
		// }

		// mDrawerToolbar.setVisibility(View.INVISIBLE);
		// mPreviousView.setVisibility(View.INVISIBLE);
		// mNextView.setVisibility(View.INVISIBLE); bob

		mDockBar.open();
	}

	private void finishBindDesktopItems() {
		if (mSavedState != null) {
			if (!mWorkspace.hasFocus()) {
				View v = mWorkspace.getChildAt(mWorkspace.getCurrentScreen());
				if (v != null) {
					v.requestFocus();
				}
			}

			final long[] userFolders = mSavedState
					.getLongArray(RUNTIME_STATE_USER_FOLDERS);
			if (userFolders != null) {
				for (long folderId : userFolders) {
					final FolderInfo info = sModel.findFolderById(folderId);
					if (info != null) {
						openFolder(info);
					}
				}
				final Folder openFolder = mWorkspace.getOpenFolder();
				if (openFolder != null) {
					openFolder.requestFocus();
				}
			}

			final boolean allApps = mSavedState.getBoolean(
					RUNTIME_STATE_ALL_APPS_FOLDER, false);
			if (allApps) {
				showAllApps(false, null);
			}
			final boolean dockOpen = mSavedState.getBoolean(
					RUNTIME_STATE_DOCKBAR, false);
			if (dockOpen) {
				mDockBar.open();
			}
			mSavedState = null;
		}

		if (mSavedInstanceState != null) {
			// ADW: sometimes on rotating the phone, some widgets fail to
			// restore its states.... so... damn.
			try {
				super.onRestoreInstanceState(mSavedInstanceState);
			} catch (Exception e) {
			}
			mSavedInstanceState = null;
		}

		if (allAppsOpen && !mAllAppsGrid.hasFocus()) {
			mAllAppsGrid.requestFocus();
		}

		mDesktopLocked = false;
		// ADW: Show the changelog screen if needed bob
		/*
		 * if(AlmostNexusSettingsHelper.shouldShowChangelog(this)){ try {
		 * AlertDialog builder =
		 * AlmostNexusSettingsHelper.ChangelogDialogBuilder.create(this);
		 * builder.show(); } catch (Exception e) { e.printStackTrace(); } }
		 */
	}

	private void bindDrawer(Launcher.DesktopBinder binder,
			ApplicationsAdapter drawerAdapter) {
		int currCatalog = AlmostNexusSettingsHelper.getCurrentAppCatalog(this);
		AppCatalogueFilters.getInstance().getDrawerFilter()
				.setCurrentGroupIndex(currCatalog);
		drawerAdapter.buildViewCache((ViewGroup) mAllAppsGrid);
		mAllAppsGrid.setAdapter(drawerAdapter);
		mAllAppsGrid.updateAppGrp();
		binder.startBindingAppWidgetsWhenIdle();
	}

	private void bindAppWidgets(Launcher.DesktopBinder binder,
			LinkedList<LauncherAppWidgetInfo> appWidgets) {

		final Workspace workspace = mWorkspace;
		final boolean desktopLocked = mDesktopLocked;

		if (!appWidgets.isEmpty()) {
			final LauncherAppWidgetInfo item = appWidgets.removeFirst();

			final int appWidgetId = item.appWidgetId;
			final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
					.getAppWidgetInfo(appWidgetId);
			item.hostView = mAppWidgetHost.createView(this, appWidgetId,
					appWidgetInfo);

			if (LOGD) {
				d(LOG_TAG, String.format(
						"about to setAppWidget for id=%d, info=%s",
						appWidgetId, appWidgetInfo));
			}

			item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
			item.hostView.setTag(item);
			workspace.addInScreen(item.hostView, item.screen, item.cellX,
					item.cellY, item.spanX, item.spanY, !desktopLocked);

			workspace.requestLayout();
			// finish load a widget, send it an intent
			if (appWidgetInfo != null)
				appwidgetReadyBroadcast(appWidgetId, appWidgetInfo.provider,
						new int[] { item.spanX, item.spanY });
		}

		if (appWidgets.isEmpty()) {
			if (PROFILE_ROTATE) {
				android.os.Debug.stopMethodTracing();
			}
		} else {
			binder.obtainMessage(DesktopBinder.MESSAGE_BIND_APPWIDGETS)
					.sendToTarget();
		}
	}

	private void showSettingPopupWindow() {
		if (mSettingPop == null) {
			initSettingPopupWindow();
		}
		if (!isFinishing()) {
			mSettingPop.showAsDropDown(mTopView,
					SettingTools.getPopupwindow_x(context) + 210, -10);
		}
	}

	private void initSettingPopupWindow() {
		mSettingPop = new PopupWindow(initSettingView(),
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mSettingPop.setOutsideTouchable(true);
		mSettingPop.setAnimationStyle(R.style.popupAnimation_slow);
		mSettingPop.setBackgroundDrawable(new BitmapDrawable());
	}

	private void dismissSettingPopupWindow() {
		if (mSettingPop != null && !isFinishing()) {
			mSettingPop.dismiss();
		}
	}

	private View initSettingView() {
		View mSettingView = LayoutInflater.from(this).inflate(
				R.layout.popupwindow_setting, null);
		mWallpapaerSettingView = (TextView) mSettingView
				.findViewById(R.id.wallpaper_setting_textview);
		mAboutView = (TextView) mSettingView.findViewById(R.id.about_textview);
		mAboutView.setOnClickListener(mSettingOnClickListener);
		mWallpapaerSettingView.setOnClickListener(mSettingOnClickListener);
		return mSettingView;
	}

	private View.OnClickListener mSettingOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				switch (v.getId()) {
				case R.id.wallpaper_setting_textview:
					Intent i = new Intent(Launcher.this,
							WallpaperActivity.class);
					i.putExtra("screen_count", mWorkspace.getChildCount());
					startActivity(i);
					break;
				/*
				 * case R.id.screen_manager_textview: Intent intent = new
				 * Intent(Launcher.this, ScreenManagerActivity.class);
				 * startActivity(intent); break;
				 */
				case R.id.check_update:
					checkUpdate(true);
					break;
				case R.id.feedback:
					Intent it = new Intent(Intent.ACTION_VIEW,
							Uri.parse(feedURL));
					it.setClassName("com.android.browser",
							"com.android.browser.BrowserActivity");
					Launcher.this.startActivity(it);
					break;
				case R.id.about_confirm:
					if (pWindow != null && pWindow.isShowing()) {
						pWindow.dismiss();
					}
					break;
				case R.id.about_textview:
					View aboutView = LayoutInflater.from(Launcher.this)
							.inflate(R.layout.layout_about_dialog, null);
					((LinearLayout) aboutView.findViewById(R.id.ln_layout))
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									if (pWindow != null) {
										pWindow.dismiss();
									}
								}
							});
					pWindow = new PopupWindow(aboutView,
							LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,
							true);
					pWindow.setOutsideTouchable(true);
					pWindow.setBackgroundDrawable(new BitmapDrawable());
					pWindow.showAtLocation(settingButton, Gravity.CENTER, 0, 0);
					mPublishDateTV = (TextView) aboutView
							.findViewById(R.id.publish_date);
					mVersionInfoTV = (TextView) aboutView
							.findViewById(R.id.version_info);
					mConfirmBtn = (Button) aboutView
							.findViewById(R.id.about_confirm);
					mCheckUpdateBtn = (Button) aboutView
							.findViewById(R.id.check_update);
					mFeedbackBtn = (Button) aboutView
							.findViewById(R.id.feedback);
					mConfirmBtn.setOnClickListener(this);
					mCheckUpdateBtn.setOnClickListener(this);
					mFeedbackBtn.setOnClickListener(this);

					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dismissSettingPopupWindow();
		}
	};

	void clickInsertDB(ItemInfo info) {
		if (mDrawerDatabase == null) {
			mDrawerDatabase = new DrawerDatabase(this);
		}
		if (info instanceof ApplicationInfo) {
			ApplicationInfo aInfo = (ApplicationInfo) info;
			mDrawerDatabase.addOrUpdateOpenData(info.id, info.itemType,
					aInfo.getPackageName(),
					((ApplicationInfo) info).title.toString());
		} else if ((mAllDrawer.getVisibility() == View.VISIBLE && info instanceof FolderInfo)) {
			mDrawerDatabase.addOrUpdateOpenData(info.id, info.itemType, null,
					null);
		}
	}

	public void updateWorkspace() {

		int mHomeScreens = AlmostNexusSettingsHelper.getDesktopScreens(context);
		LayoutInflater layoutInflter = LayoutInflater.from(context);
		for (int i = 0; i < mHomeScreens - 1; i++) {
			mWorkspace.removeScreen(i);
		}

		for (int i = mHomeScreens - 1; i > 0; i--) {
			CellLayout screen = (CellLayout) layoutInflter.inflate(
					R.layout.workspace_screen, mWorkspace, false);
			mWorkspace.addView(screen);
		}
		mWorkspace.initWorkspace();
		// mWorkspace.mScroller=new CustomScroller(context, new
		// ElasticInterpolator(5f));

		// mWorkspace.initWorkspace();
		// CellLayout
		// screen=(CellLayout)layoutInflter.inflate(R.layout.workspace_screen,
		// mWorkspace, false);
		// mWorkspace.addView(screen);
	}

	/**
	 * Launches the intent referred by the clicked shortcut.
	 * 
	 * @param v
	 *            The view representing the clicked shortcut.
	 */
	public void onClick(View v) {
		Object tag = v.getTag();
		// ADW: Check if the tag is a special action (the app drawer category
		// navigation)
		if (tag instanceof Integer) {
			navigateCatalogs(Integer.parseInt(tag.toString()));
			return;
		}
		// TODO:ADW Check whether to display a toast if clicked mLAB or mRAB
		// withount binding
		if (tag == null && v instanceof ActionButton) {
			Toast t = Toast.makeText(this, R.string.toast_no_application_def,
					Toast.LENGTH_SHORT);
			t.show();
			return;
		}
		if (tag instanceof ApplicationInfo) {
			// Open shortcut
			final ApplicationInfo info = (ApplicationInfo) tag;
			final Intent intent = info.intent;
			int[] pos = new int[2];
			v.getLocationOnScreen(pos);
			try {
				intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0]
						+ v.getWidth(), pos[1] + v.getHeight()));
			} catch (NoSuchMethodError e) {
			}
			;
			startActivitySafely(intent);

			clickInsertDB(info);

			// Close dockbar if setting says so
			if (info.container == LauncherSettings.Favorites.CONTAINER_DOCKBAR
					&& isDockBarOpen() && autoCloseDockbar) {
				mDockBar.close();
			}
		} else if (tag instanceof FolderInfo) {

			itemInfo = (ItemInfo) v.getTag();
			iniView = v;

			UserFolderInfo user = (UserFolderInfo) itemInfo;
			String id = String.valueOf(user.id);

			if (myDatabaseUtil.isDeciphering(id)) { // 解密

				// decipheringDialog();
				openFolderPwdValidateDialog(tag, "openFolder", v);

				// if (decipheringTag > 0) {
				// FolderInfo folderInfo = (FolderInfo) tag;
				//
				// if(mAllDrawer.getVisibility()==View.VISIBLE) {
				// mAllDrawer.handleFolderClick(folderInfo);
				// }else {
				// handleFolderClick(folderInfo);
				// }
				//
				// clickInsertDB(folderInfo);
				//
				// opening_floder_icon = (FolderIcon) v;
				// }

			} else {

				FolderInfo folderInfo = (FolderInfo) tag;
				// if(mAllDrawer.getVisibility()==View.VISIBLE) {
				// mAllDrawer.handleFolderClick(folderInfo);
				// }else {
				handleFolderClick(folderInfo);
				// }

				clickInsertDB(folderInfo);

				opening_floder_icon = (FolderIcon) v;
			}
		}

		switch (v.getId()) {
		// case R.id.btn_m:
		/*
		 * case R.id.btn_mode: View modView =
		 * mInflater.inflate(R.layout.modle_sorts, null); generalButton =
		 * (ImageView) modView.findViewById(R.id.general); bustlingButton =
		 * (ImageView) modView.findViewById(R.id.bustling); customButton =
		 * (ImageView) modView.findViewById(R.id.custom);
		 * generalButton.setOnClickListener(Launcher.this);
		 * bustlingButton.setOnClickListener(Launcher.this);
		 * customButton.setOnClickListener(Launcher.this); popupWindow = new
		 * PopupWindow(modView, LayoutParams.WRAP_CONTENT,
		 * LayoutParams.WRAP_CONTENT, true);
		 * popupWindow.setContentView(modView);
		 * popupWindow.setBackgroundDrawable(new BitmapDrawable());
		 * popupWindow.showAsDropDown(modButton, -75, -9);
		 * 
		 * break;
		 */
		case R.id.general:
			Toast.makeText(Launcher.this, "切回普通模式", Toast.LENGTH_SHORT).show();
			updateWorkspace();
			stateMod = 0;
			startLoaders(0);
			if (popupWindow != null && popupWindow.isShowing()) {
				popupWindow.dismiss();
			}
			break;
		// case R.id.concise: 后续版本增加
		// Toast.makeText(Launcher.this, "concise", Toast.LENGTH_SHORT).show();
		// if(popupWindow!=null && popupWindow.isShowing())
		// {
		// popupWindow.dismiss();
		// }
		// break;
		case R.id.bustling:

			Toast.makeText(Launcher.this, "切回繁华模式", Toast.LENGTH_SHORT).show();
			updateWorkspace();
			// hideDesktop(true);
			startLoaders(2);
			// hideDesktop(false);
			if (popupWindow != null && popupWindow.isShowing()) {
				popupWindow.dismiss();
			}
			break;

		case R.id.custom:
			Toast.makeText(Launcher.this, "切回自定义模式", Toast.LENGTH_SHORT).show();
			updateWorkspace();
			stateMod = 1;
			startLoaders(1);
			if (popupWindow != null && popupWindow.isShowing()) {
				popupWindow.dismiss();
			}
			break;

		case R.id.btn_s:
		case R.id.btn_setting:
			showSettingPopupWindow();
			break;

		case R.id.btn_a:
		case R.id.btn_add_app:
			bindWorkspaceMini();
			workspaceMiniMode = 0;
			add_app_popup.showAtLocation(addAppButton, Gravity.CENTER, 0, 0);
			// add_app_popup.setAnimationStyle(R.style.popupAnimation_slow);
			// dialog_add_app.show();
			gotoAddAppMode();
			break;

		case R.id.btn_d:
		case R.id.btn_drawer:
			/*
			 * String intentDescription =
			 * "#Intent;action=org.adw.launcher.action.launcheraction;launchFlags=0x10000000;component=org.adw.launcher/.CustomShirtcutActivity;i.DefaultLauncherAction.EXTRA_BINDINGVALUE=4;end"
			 * ; Intent intent; bob try { intent =
			 * Intent.parseUri(intentDescription, 0);
			 * intent.setClass(Launcher.this, CustomShirtcutActivity.class);
			 * startActivitySafely(intent); } catch (URISyntaxException e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); }
			 */

			// mDesktopLayout.setVisibility(View.GONE);
			/*
			 * mAllDrawer.setVisibility(View.VISIBLE);
			 * mDesktopLayout.setVisibility(View.GONE);
			 */
			mVerScrollLayout.snapToScreen(1);

			Drawable mBackgroundDrawable = WallpaperManager.getInstance(
					Launcher.mLauncher).getDrawable();
			mAllDrawer.setBackgroundDrawable(mBackgroundDrawable);

			saveMob = stateMod;
			stateMod = 3;

			mAllDrawer.performFirst();
			break;

		case R.id.btn_sw:
		case R.id.btn_switch:
			if (!isFinishing()) {
				PopupWindow pw_Setting = PopupWindow_Setting.getInstance(
						Launcher.this, mTopView);
				pw_Setting.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.popupwindow_background));
				pw_Setting.setFocusable(true);
				pw_Setting.setOutsideTouchable(true);
				pw_Setting.setBackgroundDrawable(new BitmapDrawable());
				pw_Setting.setAnimationStyle(R.style.popupAnimation_slow);
				pw_Setting.showAsDropDown(mTopView,
						SettingTools.getPopupwindow_x(context), -10);
				pw_Setting.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss() {
						PopupWindow_Setting
								.unregisterSettingBroadcastReceiver(mLauncher);
					}
				});
			}

			break;
		case R.id.btn_search:

			final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

			Bundle appSearchData = new Bundle();
			appSearchData.putString("source", "launcher-search");

			searchManager.startSearch("", false, getComponentName(),
					appSearchData, true);

			// searchText.setText("");
			break;

		case R.id.btn_swap_app:
			if (workspaceMiniMode != 0) {
				gotoAddAppMode();
				workspaceMiniMode = 0;

				btn_swap_app.setBackgroundResource(R.drawable.left_selected);
				btn_swap_app.setTextColor(getResources().getColor(
						R.color.add_item_text_selected));
				btn_swap_widget.setBackgroundResource(R.drawable.right_normal);
				btn_swap_widget.setTextColor(getResources().getColor(
						R.color.add_item_text_normal));
			}

			break;

		case R.id.btn_swap_widget:
			if (workspaceMiniMode != 1) {
				gotoAddWidgetMode();
				workspaceMiniMode = 1;

				btn_swap_app.setBackgroundResource(R.drawable.left_normal);
				btn_swap_app.setTextColor(getResources().getColor(
						R.color.add_item_text_normal));
				btn_swap_widget
						.setBackgroundResource(R.drawable.right_selected);
				btn_swap_widget.setTextColor(getResources().getColor(
						R.color.add_item_text_selected));
			}
			break;

		// zy:mark
		case R.id.btn_add_widget:
			pickAppWidget();
			break;

		case R.id.toplayout:
			// closeFolder();
			break;

		case R.id.mini_content:
			// closeFolder();
			break;

		case R.id.img_open_box:
			add_app_popup.dismiss();

			mVerScrollLayout.snapToScreen(1);

			Drawable mB = WallpaperManager.getInstance(Launcher.mLauncher)
					.getDrawable();
			mAllDrawer.setBackgroundDrawable(mB);

			saveMob = stateMod;
			stateMod = 3;

			mAllDrawer.performFirst();
			break;
		default:
			break;
		}
	}

	private void showDrawerPopupWindow() {
		if (!isFinishing()) {

		}
	}

	private void initDrawerViews() {
		View mContentView = LayoutInflater.from(this).inflate(
				R.layout.layout_alldrawer, null);

	}

	private DrawerDatabase mDrawerDatabase;

	void startActivitySafely(Intent intent) {
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			startActivity(intent);

			// System.out.println(intent.getAction() + ", " +
			// intent.getPackage()
			// + ", " + intent.getComponent().getPackageName());

		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			e(LOG_TAG,
					"Launcher does not have the permission to launch "
							+ intent
							+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
							+ "or use the exported attribute for this activity.",
					e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleFolderClick(FolderInfo folderInfo) {
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

	/**
	 * Opens the user fodler described by the specified tag. The opening of the
	 * folder is animated relative to the specified View. If the View is null,
	 * no animation is played.
	 * 
	 * @param folderInfo
	 *            The FolderInfo describing the folder to open.
	 */
	private void openFolder(FolderInfo folderInfo) {
		Folder openFolder;

		if (folderInfo instanceof UserFolderInfo) {
			openFolder = UserFolder.fromXml(this);
		} else if (folderInfo instanceof LiveFolderInfo) {
			openFolder = org.adw.launcher.LiveFolder.fromXml(this, folderInfo);
		} else {
			return;
		}

		openFolder.setDragger(mDragLayer);
		openFolder.setLauncher(this);

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
		closeDrawer(false);
	}

	/**
	 * Returns true if the workspace is being loaded. When the workspace is
	 * loading, no user interaction should be allowed to avoid any conflict.
	 * 
	 * @return True if the workspace is locked, false otherwise.
	 */
	boolean isWorkspaceLocked() {
		return mDesktopLocked;
	}

	public boolean onLongClick(View v) {

		if (mDesktopLocked) {
			return false;
		}
		// ADW: Show previews on longpressing the dots
		/*
		 * switch (v.getId()) { case R.id.btn_scroll_left:
		 * mWorkspace.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS,
		 * HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
		 * showPreviousPreview(v); return true; case R.id.btn_scroll_right:
		 * mWorkspace.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS,
		 * HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
		 * showNextPreview(v); return true; }
		 */
		if (!(v instanceof CellLayout)) {
			v = (View) v.getParent();
		}
		
		CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();

		// This happens when long clicking an item with the dpad/trackball
		if (cellInfo == null) {
			return true;
		}

		if (cellInfo != null) {

			View child = cellInfo.cell;

			if (child != null && mVerScrollLayout.getCurScreen() == 0) {

				itemInfo = (ItemInfo) child.getTag();
				int type = itemInfo.itemType;

				LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = mLayoutInflater.inflate(R.layout.operationpopup,
						null);
				if (itemInfo instanceof ApplicationInfo) {
					view.findViewById(R.id.apply).setVisibility(View.VISIBLE);
					view.findViewById(R.id.folderly).setVisibility(View.GONE);
					view.findViewById(R.id.appwidgetly)
							.setVisibility(View.GONE);

					ApplicationInfo appInfo = (ApplicationInfo) itemInfo;
					PackageManager mgr = context.getPackageManager();
					ResolveInfo res = mgr.resolveActivity(appInfo.intent, 0);
					if (res == null) {
						return false;
					}

					if (filterApp(res)) {
						view.findViewById(R.id.appdelete).setVisibility(
								View.GONE);
					} else {
						view.findViewById(R.id.appdelete).setVisibility(
								View.VISIBLE);
					}
				} else if (itemInfo instanceof UserFolderInfo) {

					cell = cellInfo;

					view.findViewById(R.id.apply).setVisibility(View.GONE);
					view.findViewById(R.id.appwidgetly)
							.setVisibility(View.GONE);
					view.findViewById(R.id.folderly)
							.setVisibility(View.VISIBLE);

					UserFolderInfo userFolderInfo = (UserFolderInfo) itemInfo;

					id = String.valueOf(userFolderInfo.id);

					if (myDatabaseUtil.isDeciphering(id)) {

						view.findViewById(R.id.deciphering).setVisibility(
								View.VISIBLE);
						view.findViewById(R.id.encryption).setVisibility(
								View.GONE);
					} else {

						view.findViewById(R.id.deciphering).setVisibility(
								View.GONE);
						view.findViewById(R.id.encryption).setVisibility(
								View.VISIBLE);
					}

				} else if (itemInfo instanceof LauncherAppWidgetInfo
						|| type == 1003
						|| type == LauncherSettings.Favorites.ITEM_TYPE_WIDGET_CLOUD_APP
						|| type == LauncherSettings.Favorites.ITEM_TYPE_WIDGET_MY_APP
						|| type == LauncherSettings.Favorites.ITEM_TYPE_WIDGET_TASK_MANAGER) {
					view.findViewById(R.id.apply).setVisibility(View.GONE);
					view.findViewById(R.id.appwidgetly).setVisibility(
							View.VISIBLE);
					view.findViewById(R.id.folderly).setVisibility(View.GONE);
				} else {
					view.findViewById(R.id.apply).setVisibility(View.GONE);
					view.findViewById(R.id.appwidgetly)
							.setVisibility(View.GONE);
					view.findViewById(R.id.folderly).setVisibility(View.GONE);
				}

				if (mPopupWindow == null || !mPopupWindow.isShowing()) {
					mPopupWindow = new PopupWindow(view,
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
					mPopupWindow.setOutsideTouchable(true);

					MenuClickListener menuClickListener = new MenuClickListener();

					appdelete = (LinearLayout) view
							.findViewById(R.id.appdelete);
					uninstall = (LinearLayout) view
							.findViewById(R.id.uninstall);
					appinfo = (LinearLayout) view
							.findViewById(R.id.applictioninfo);
					changeicon = (LinearLayout) view
							.findViewById(R.id.changeicon);
					rename = (LinearLayout) view.findViewById(R.id.rename);
					folderdelete = (LinearLayout) view
							.findViewById(R.id.folderdelete);
					deciphering = (LinearLayout) view
							.findViewById(R.id.deciphering);
					encryption = (LinearLayout) view
							.findViewById(R.id.encryption);
					addapp = (LinearLayout) view.findViewById(R.id.addapp);
					folderrename = (LinearLayout) view
							.findViewById(R.id.folderrename);
					appwidgetdelete = (LinearLayout) view
							.findViewById(R.id.appwidgetdelete);

					appdelete.setOnClickListener(menuClickListener);
					uninstall.setOnClickListener(menuClickListener);
					appinfo.setOnClickListener(menuClickListener);
					changeicon.setOnClickListener(menuClickListener);
					rename.setOnClickListener(menuClickListener);
					folderdelete.setOnClickListener(menuClickListener);
					deciphering.setOnClickListener(menuClickListener);
					encryption.setOnClickListener(menuClickListener);
					addapp.setOnClickListener(menuClickListener);
					folderrename.setOnClickListener(menuClickListener);
					appwidgetdelete.setOnClickListener(menuClickListener);

					// mPopupWindow.setAnimationStyle(R.anim.up_in);
					mPopupWindow.setAnimationStyle(R.style.popupAnimation_down);
					// mPopupWindow.showAsDropDown(child, -80, 0);

					CellLayout cellLayout = (CellLayout) mWorkspace
							.getChildAt(0);

					if (itemInfo instanceof LauncherAppWidgetInfo
							|| type == 1003) {
						mPopupWindow.showAsDropDown(child, 240, -10);
					} else if (type == LauncherSettings.Favorites.ITEM_TYPE_WIDGET_CLOUD_APP
							|| type == LauncherSettings.Favorites.ITEM_TYPE_WIDGET_MY_APP
							|| type == LauncherSettings.Favorites.ITEM_TYPE_WIDGET_TASK_MANAGER) {
						int n=AndroidUtils.dip2px(this, 95.0f)/2;
						mPopupWindow.showAsDropDown(child, n, -10);
					} else {
						mPopupWindow.showAsDropDown(child, 0,
								85 - cellLayout.mCellHeight);
					}
				}
			}
		}

		if (mVerScrollLayout.getCurScreen() == 1) {
			mAllDrawer.startDrag(cellInfo);

			return true;
		}

		if (mWorkspace.allowLongPress()) {
			if (cellInfo.cell == null) {
				if (cellInfo.valid) {
					// mWorkspace.setAllowLongPress(false);

					// zy:mark
					// showAddDialog(cellInfo);
					showLongPressDialog(cellInfo);
				}
			} else {
				if (!(cellInfo.cell instanceof Folder)) {
					// User long pressed on an item
					mWorkspace.startDrag(cellInfo);
				}
			}
		}
		return true;
	}

	public boolean filterApp(ResolveInfo res) {
		if ((res.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
			return true;
		}
		return false;
	}

	private void updateIcon(Intent data) {

		mIconSize = (int) getResources().getDimension(
				android.R.dimen.app_icon_size);

		Uri photoUri = data.getData();

		try {
			InputStream is = getContentResolver().openInputStream(photoUri);
			BitmapFactory.Options opts = new BitmapFactory.Options();
			Bitmap bitmap;
			opts.inJustDecodeBounds = true;
			bitmap = BitmapFactory.decodeStream(is, null, opts);

			BitmapFactory.Options ops2 = new BitmapFactory.Options();
			int width = mIconSize;
			float w = opts.outWidth;
			int scale = (int) (w / width);
			ops2.inSampleSize = scale;
			is = getContentResolver().openInputStream(photoUri);
			mBitmap = BitmapFactory.decodeStream(is, null, ops2);
			ApplicationInfo appInfo = null;
			View view = null;
			if (folder_app_info != null) {
				appInfo = (ApplicationInfo) folder_app_info;
				view = contentView;
			} else {
				appInfo = (ApplicationInfo) itemInfo;
				view = mWorkspace.getViewForTag(appInfo);
			}

			if (mBitmap != null) {
				if (mBitmap.getWidth() > mIconSize)
					mBitmap = Utilities.createBitmapThumbnail(mBitmap, this);

				Drawable drawable = new BitmapDrawable(mBitmap);
				appInfo.icon = drawable;
				if (folder_app_info != null) {
					((CounterTextView) view).setBackgroundDrawable(drawable);
				} else {
					((BubbleTextView) view).setBackgroundDrawable(drawable);
				}

				getModel().updateItemInDatabase(context, appInfo);

				mWorkspace.updateShortcutFromApplicationInfo(appInfo);
			}

			if (folder_app_info != null) {
				Folder folder = mWorkspace.getOpenFolder();
				if (folder != null) {
					folder.notifyDataSetChanged();
				}

				UserFolderInfo userFolderInfo = (UserFolderInfo) itemInfo;

				View v = mWorkspace.getViewForTag(userFolderInfo);

				FolderIcon icon = (FolderIcon) v;
				mCloseIcon = new BitmapDrawable(
						FolderIcon.creaetCloseIcon(userFolderInfo));
				((BubbleTextView) v).setCompoundDrawablesWithIntrinsicBounds(
						null, mCloseIcon, null, null);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MenuClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.uninstall:

				// ApplicationInfo info=(ApplicationInfo) itemInfo;
				// PackageManager p = context.getPackageManager();
				// ResolveInfo r = p.resolveActivity(info.intent, 0);
				// removeShortcutsForPackage(r.activityInfo.applicationInfo.packageName);

				ApplicationInfo info = (ApplicationInfo) itemInfo;
				getModel().deleteItemFromDatabase(context, info);

				View view = mWorkspace.getViewForTag(info);
				CellLayout app_layout = (CellLayout) mWorkspace
						.getChildAt(mWorkspace.getCurrentScreen());
				app_layout.removeViewInLayout(view);
				app_layout.requestLayout();
				app_layout.invalidate();

				break;

			case R.id.appdelete:
				ApplicationInfo appInfo = (ApplicationInfo) itemInfo;
				try {
					if (appInfo.iconResource != null)
						UninstallPkg = appInfo.iconResource.packageName;
					else {
						PackageManager mgr = context.getPackageManager();
						ResolveInfo res = mgr
								.resolveActivity(appInfo.intent, 0);
						UninstallPkg = res.activityInfo.packageName;
					}

				} catch (Exception e) {
					Log.w(LOG_TAG, "Could not load shortcut icon: " + itemInfo);
					UninstallPkg = null;
				}

				Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
						Uri.parse("package:" + UninstallPkg));
				context.startActivity(uninstallIntent);

				app_layout = (CellLayout) mWorkspace.getChildAt(mWorkspace
						.getCurrentScreen());
				app_layout.requestLayout();
				app_layout.invalidate();

				overridePendingTransition(R.anim.enteralpha, R.anim.exitalpha);
				break;

			case R.id.applictioninfo:
				try {
					ApplicationInfo applicationInfo = (ApplicationInfo) itemInfo;
					PackageManager mgr = context.getPackageManager();
					ResolveInfo res = mgr.resolveActivity(
							applicationInfo.intent, 0);

					showInstalledAppDetails(context,
							res.activityInfo.packageName);
					overridePendingTransition(R.anim.enteralpha,
							R.anim.exitalpha);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				break;

			case R.id.changeicon:

				Intent pickerIntent = new Intent(Intent.ACTION_PICK);
				pickerIntent.setType("image/*");
				pickerIntent.putExtra("type", "desktop_app");
				startActivityForResult(
						Intent.createChooser(pickerIntent, "Select icon"),
						PHOTO_ICON);
				overridePendingTransition(R.anim.enteralpha, R.anim.exitalpha);

				break;

			case R.id.rename:
				renameDialog(v);
				break;

			case R.id.folderdelete:

				if (myDatabaseUtil.isDeciphering(id)) { // 解密

					openFolderPwdValidateDialog(null, "deletefolder",
							folderdelete);

				} else {

					deleteFolder(v);

				}
				break;

			case R.id.deciphering:

				decipheringDialog(v);

				break;

			case R.id.encryption:

				encryption(v);

				break;

			case R.id.addapp:

				UserFolderInfo userFolderInfo = (UserFolderInfo) itemInfo;

				CellLayout layout = (CellLayout) mWorkspace
						.getChildAt(mWorkspace.getCurrentScreen());

				if (myDatabaseUtil.isDeciphering(id)) { // 解密

					openFolderPwdValidateDialog(null, "addApp", addapp);

				} else {

					addAppToFolder(userFolderInfo, v);

				}

				layout.requestLayout();
				layout.invalidate();

				break;

			case R.id.folderrename:

				if (myDatabaseUtil.isDeciphering(id)) {

					openFolderPwdValidateDialog(null, "rename", rename);

				} else {

					renameDialog(v);
				}

				break;

			case R.id.appwidgetdelete:

				View widget = mWorkspace.getViewForTag(itemInfo);
				CellLayout widgetlayout = (CellLayout) mWorkspace
						.getChildAt(mWorkspace.getCurrentScreen());

				getModel().deleteItemFromDatabase(context, itemInfo);
				widgetlayout.removeViewInLayout(widget);
				widgetlayout.requestLayout();
				widgetlayout.invalidate();

				break;
			}

			closePop();
		}

	}

	void closePop() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		}
	}

	private void dismissPopupwindow(PopupWindow pop) {
		if (pop != null && pop.isShowing()) {
			pop.dismiss();
		}
	}

	void deleteFolder(View v) {

		closePop();

		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mLayoutInflater.inflate(R.layout.deletefolderdialog, null);
		desktopPopupWindow = new PopupWindow(view, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		desktopPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		desktopPopupWindow.setOutsideTouchable(true);

		desktopPopupWindow.setAnimationStyle(R.style.desktopPopupAnimation);
		desktopPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
		((LinearLayout) view.findViewById(R.id.deletefolderdialogly))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						desktopPopupWindow.dismiss();
					}
				});

		checkDelete = (CheckBox) view.findViewById(R.id.check_delete);
		submit = (Button) view.findViewById(R.id.submit);
		cancel = (Button) view.findViewById(R.id.cancel);

		submit.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("static-access")
			@Override
			public void onClick(View v) {

				UserFolderInfo userFolderInfo = (UserFolderInfo) itemInfo;
				View view = mWorkspace.getViewForTag(userFolderInfo);

				CellLayout layout = (CellLayout) mWorkspace
						.getChildAt(mWorkspace.getCurrentScreen());

				if (!checkDelete.isChecked()) {

					ArrayList<ApplicationInfo> contents = userFolderInfo.contents;
					int contentsCount = contents.size();
					int screen = mWorkspace.getCurrentScreen();
					int user = layout.countSpace();

					if (contentsCount > 0) {

						for (int k = 0; k < contentsCount; k++) {

							ApplicationInfo info = contents.get(k);

							Point p = null;

							if ((layout.getTotal() - user) < contentsCount) {
								if (mWorkspace.getCurrentScreen() == mWorkspace
										.getChildCount()) {
									final ScreensAdapter screens = new ScreensAdapter(
											context, mWorkspace.getChildAt(0)
													.getWidth(), mWorkspace
													.getChildAt(0).getHeight());
									if (mWorkspace.getChildCount() < MAX_SCREENS) {
										CellLayout newScreen = mWorkspace
												.addScreen(mWorkspace
														.getChildCount() + 1);
										mWorkspace.addScreen(mWorkspace
												.getChildCount() + 1);
										layout = (CellLayout) mWorkspace
												.getChildAt(mWorkspace
														.getChildCount() + 1);
									} else {
										Toast t = Toast
												.makeText(
														Launcher.this,
														R.string.message_cannot_add_desktop_screen,
														Toast.LENGTH_LONG);
										t.show();
									}
								} else {
									layout = (CellLayout) mWorkspace
											.getChildAt(mWorkspace
													.getCurrentScreen() + 1);
								}

								p = layout.vaild_points.get(k);
							} else {
								p = layout.vaild_points.get(k);
							}

							int[] mCellCoordinates = new int[2];
							layout.pointToCellExact(p.x, p.y, mCellCoordinates);

							if (info.icon instanceof FastBitmapDrawable) {
								info.icon = new BitmapDrawable(
										((FastBitmapDrawable) info.icon)
												.getBitmap());
							} else {
								info.icon = new BitmapDrawable(
										((BitmapDrawable) info.icon)
												.getBitmap());
							}

							info.filtered = true;
							info.title = new String(info.title.toString());
							info.intent = new Intent(info.intent);
							info.customIcon = true;
							info.iconResource = null;
							info.screen = screen;
							info.cellX = mCellCoordinates[0];
							info.cellY = mCellCoordinates[1];
							info.spanX = 1;
							info.spanY = 1;

							completeAddShortcut2(info, screen,
									mCellCoordinates[0], mCellCoordinates[1]);
						}
					}

				}

				getModel().deleteUserFolderContentsFromDatabase(context,
						userFolderInfo);

				layout.removeViewInLayout(view);
				layout.requestLayout();
				layout.invalidate();

				dismissPopupwindow(desktopPopupWindow);

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dismissPopupwindow(desktopPopupWindow);
			}
		});

	}

	void addAppToFolder(final UserFolderInfo userFolderInfo, View v) {
		closePop();
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mLayoutInflater.inflate(R.layout.allappgrid, null);
		desktopPopupWindow = new PopupWindow(view, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		desktopPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		desktopPopupWindow.setOutsideTouchable(true);

		desktopPopupWindow.setAnimationStyle(R.style.desktopPopupAnimation);
		desktopPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

		((LinearLayout) view.findViewById(R.id.allappgridly))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						desktopPopupWindow.dismiss();
					}
				});

		name = (TextView) view.findViewById(R.id.name);
		submit = (Button) view.findViewById(R.id.submit);
		cancel = (Button) view.findViewById(R.id.cancel);

		name.setText(userFolderInfo.title);

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dismissPopupwindow(desktopPopupWindow);

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dismissPopupwindow(desktopPopupWindow);

			}
		});

		PackageManager pm = context.getPackageManager();

		List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();
		for (int i = 0; i < sModel.mApplications.size(); i++) {
			apps.add(sModel.mApplications.get(i));
		}

		final ArrayList<ApplicationInfo> contents = userFolderInfo.contents;

		AllAppAdapter allAppAdapter = new AllAppAdapter(context, apps, pm,
				contents);
		appGv = (GridView) view.findViewById(R.id.gridview);
		appGv.setAdapter(allAppAdapter);
		appGv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				CheckBox cb = (CheckBox) view.findViewById(R.id.cb);
				ImageView appImage = (ImageView) view.findViewById(R.id.appimg);
				ApplicationInfo appInfo = (ApplicationInfo) appImage.getTag();

				if (cb.isChecked()) {
					cb.setChecked(false);
				} else {
					cb.setChecked(true);
				}

				if (cb.isChecked()) {
					userFolderInfo.add(appInfo);
					getModel().addOrMoveItemInDatabase(context, appInfo,
							userFolderInfo.id, mWorkspace.getCurrentScreen(),
							userFolderInfo.cellX, userFolderInfo.cellY);
				} else {
					getModel().removeUserFolderItem(userFolderInfo, appInfo);
					getModel().deleteItemFromDatabase(mLauncher, appInfo);
				}

				View v = mWorkspace.getViewForTag(userFolderInfo);

				FolderIcon icon = (FolderIcon) v;
				mCloseIcon = new BitmapDrawable(icon
						.creaetCloseIcon(userFolderInfo));
				((BubbleTextView) v).setCompoundDrawablesWithIntrinsicBounds(
						null, mCloseIcon, null, null);
			}
		});

	}

	public void encryption(View v) {
		closePop();

		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mLayoutInflater
				.inflate(R.layout.folderopertiondialog, null);
		desktopPopupWindow = new PopupWindow(view, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		desktopPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		desktopPopupWindow.setOutsideTouchable(true);

		desktopPopupWindow.setAnimationStyle(R.style.desktopPopupAnimation);
		desktopPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
		((LinearLayout) view.findViewById(R.id.folderopertiondialogly))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						desktopPopupWindow.dismiss();
					}
				});

		title = (TextView) view.findViewById(R.id.title);
		title.setText("加密文件夹");

		pwd = (EditText) view.findViewById(R.id.pwd);
		repwd = (EditText) view.findViewById(R.id.repwd);

		submit = (Button) view.findViewById(R.id.submit);
		cancel = (Button) view.findViewById(R.id.cancel);

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!pwd.getText().toString().equals("")) {

					String password = pwd.getText().toString();
					String repassword = repwd.getText().toString();

					if (repassword.equals(password)) {

						long isSuccess = myDatabaseUtil.insertEntryption(
								String.valueOf(itemInfo.id), password);
						if (isSuccess > 0) {
							// dialog.cancel();
							dismissPopupwindow(desktopPopupWindow);
						} else {
							Toast.makeText(context, "加密失败！", Toast.LENGTH_SHORT)
									.show();
						}

					} else {

						Toast.makeText(context, "两次密码不一致，请重新确认密码！",
								Toast.LENGTH_SHORT).show();

					}

				} else {
					Toast.makeText(context, "密码不能为空！", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dismissPopupwindow(desktopPopupWindow);

			}
		});
	}

	public void openFolderPwdValidateDialog(final Object tag,
			final String type, final View v) {
		closePop();
		final String id = String.valueOf(itemInfo.id);

		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mLayoutInflater.inflate(R.layout.openfolderpwd, null);
		desktopPopupWindow = new PopupWindow(view, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		desktopPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		desktopPopupWindow.setOutsideTouchable(true);

		desktopPopupWindow.setAnimationStyle(R.style.desktopPopupAnimation);
		desktopPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
		((LinearLayout) view.findViewById(R.id.openfolderpwdly))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						desktopPopupWindow.dismiss();
					}
				});

		pwd = (EditText) view.findViewById(R.id.pwd);
		submit = (Button) view.findViewById(R.id.submit);
		cancel = (Button) view.findViewById(R.id.cancel);

		pwd.setOnClickListener(this);

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				if (!pwd.getText().toString().equals("")) {

					int count = myDatabaseUtil.queryEntryption(id, pwd
							.getText().toString());

					if (count > 0) {

						dismissPopupwindow(desktopPopupWindow);

						if (type.equals("openFolder")) {

							// FolderInfo folderInfo = (FolderInfo) tag;
							// handleFolderClick(folderInfo);
							//
							// clickInsertDB(folderInfo);
							//
							// opening_floder_icon = (FolderIcon) iniView;

							FolderInfo folderInfo = (FolderInfo) tag;
							// if(mAllDrawer.getVisibility()==View.VISIBLE) {
							// mAllDrawer.handleFolderClick(folderInfo);
							// }else {

							handleFolderClick(folderInfo);
							// }

							clickInsertDB(folderInfo);

							opening_floder_icon = (FolderIcon) iniView;

						} else if (type.equals("addApp")) {

							UserFolderInfo userFolderInfo = (UserFolderInfo) itemInfo;
							addAppToFolder(userFolderInfo, v);

						} else if (type.equals("rename")) {

							renameDialog(v);

						} else if (type.equals("deletefolder")) {

							deleteFolder(v);
						}

					} else {

						Toast.makeText(context, "密码错误！", Toast.LENGTH_SHORT)
								.show();
					}

				} else {
					Toast.makeText(context, "密码不能为空！", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dismissPopupwindow(desktopPopupWindow);

			}
		});

	}

	public void decipheringDialog(View v) {
		closePop();
		final String id = String.valueOf(itemInfo.id);

		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mLayoutInflater.inflate(R.layout.decipheringdialog, null);
		desktopPopupWindow = new PopupWindow(view, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		desktopPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		desktopPopupWindow.setOutsideTouchable(true);

		desktopPopupWindow.setAnimationStyle(R.style.desktopPopupAnimation);
		desktopPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

		((LinearLayout) view.findViewById(R.id.decipheringdialogly))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						desktopPopupWindow.dismiss();
					}
				});

		pwd = (EditText) view.findViewById(R.id.pwd);
		submit = (Button) view.findViewById(R.id.submit);
		cancel = (Button) view.findViewById(R.id.cancel);

		pwd.setOnClickListener(this);

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!pwd.getText().toString().equals("")) {

					int count = myDatabaseUtil.queryEntryption(id, pwd
							.getText().toString());

					if (count > 0) {

						long isSuccess = myDatabaseUtil.updateEntryption(id);

						if (isSuccess > 0) {
							dismissPopupwindow(desktopPopupWindow);
						}

						decipheringTag = count;

					} else {

						Toast.makeText(context, "密码错误！", Toast.LENGTH_SHORT)
								.show();
					}

				} else {
					Toast.makeText(context, "密码不能为空！", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dismissPopupwindow(desktopPopupWindow);
			}
		});

	}

	void renameDialog(View v) {
		closePop();
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mLayoutInflater.inflate(R.layout.renamedialog, null);
		desktopPopupWindow = new PopupWindow(view, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		desktopPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		desktopPopupWindow.setOutsideTouchable(true);

		desktopPopupWindow.setAnimationStyle(R.style.desktopPopupAnimation);
		desktopPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
		((LinearLayout) view.findViewById(R.id.renamely))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						desktopPopupWindow.dismiss();
					}
				});

		name = (TextView) view.findViewById(R.id.name);
		title = (TextView) view.findViewById(R.id.title);
		rename_et = (EditText) view.findViewById(R.id.rename);
		submit = (Button) view.findViewById(R.id.submit);
		cancel = (Button) view.findViewById(R.id.cancel);

		if (itemInfo instanceof ApplicationInfo) {
			ApplicationInfo appInfo = (ApplicationInfo) itemInfo;
			title.setText("编辑应用名称");
			name.setText(appInfo.title);
		} else {
			UserFolderInfo userFolderInfo = (UserFolderInfo) itemInfo;
			title.setText("编辑文件夹名称");
			name.setText(userFolderInfo.title);
		}

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!rename_et.getText().toString().equals("")) {

					Intent mReturnData = new Intent();
					mReturnData.putExtra(Intent.EXTRA_SHORTCUT_NAME, rename_et
							.getText().toString());
					mReturnData.putExtra(EXTRA_APPLICATIONINFO, itemInfo.id);
					setResult(RESULT_OK, mReturnData);

					if (itemInfo.itemType == 0 || itemInfo.itemType == 1
							|| itemInfo instanceof ApplicationInfo) {

						ApplicationInfo appInfo = (ApplicationInfo) itemInfo;
						View view = mWorkspace.getViewForTag(appInfo);
						appInfo.title = rename_et.getText().toString();
						((BubbleTextView) view).setText(appInfo.title);
						getModel().updateItemInDatabase(context, appInfo);

					} else {

						UserFolderInfo userFolderInfo = (UserFolderInfo) itemInfo;

						View view = null;

						// if(mLauncher.mAllDrawer.getVisibility()==View.VISIBLE)
						// {
						// view =
						// mAllDrawer.drawerWorkspace.getViewForTag(userFolderInfo);
						// } else {
						view = mWorkspace.getViewForTag(userFolderInfo);
						// }

						userFolderInfo.title = rename_et.getText().toString();
						((BubbleTextView) view).setText(userFolderInfo.title);
						getModel()
								.updateItemInDatabase(context, userFolderInfo);

					}

					dismissPopupwindow(desktopPopupWindow);
				} else {
					Toast.makeText(context, "名称不能为空！", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dismissPopupwindow(desktopPopupWindow);

			}
		});

	}

	/**
	 * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息 对于Android 2.3（Api Level
	 * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码
	 * 
	 * @param context
	 * @param packageName
	 */
	public static void showInstalledAppDetails(Context context,
			String packageName) {
		String scheme = "package";
		String app_pkg_name_21 = "com.android.settings.ApplicationPkgName"; // 调用系统InstalledAppDetails界面所需的Extra名称(用于Android
																			// 2.1及之前版本)
		String app_pkg_name_22 = "pkg"; // 调用系统InstalledAppDetails界面所需的Extra名称(用于Android
										// 2.2)
		String app_detail_pkg_name = "com.android.settings"; // InstalledAppDetails所在包名
		String app_detail_class_name = "com.android.settings.InstalledAppDetails"; // InstalledAppDetails类名

		Intent intent = new Intent();
		int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) { // 2.3以上版本，直接调用接口
			intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			Uri uri = Uri.fromParts(scheme, packageName, null);
			intent.setData(uri);
		} else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）,2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
			String appPkgName = apiLevel == 8 ? app_pkg_name_22
					: app_pkg_name_21;
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(app_detail_pkg_name, app_detail_class_name);
			intent.putExtra(appPkgName, packageName);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	static LauncherModel getModel() {
		return sModel;
	}

	void closeAllApplications() {
		closeAllApps(false);
	}

	/*
	 * View getDrawerHandle() { return mHandleView; bob }
	 */

	/*
	 * boolean isDrawerDown() { return !mDrawer.isMoving() &&
	 * !mDrawer.isOpened(); }
	 * 
	 * boolean isDrawerUp() { return mDrawer.isOpened() && !mDrawer.isMoving();
	 * }
	 * 
	 * boolean isDrawerMoving() { return mDrawer.isMoving(); }
	 */

	Workspace getWorkspace() {
		return mWorkspace;
	}

	// ADW: we return a View, so classes using this should cast
	// to AllAppsGridView or AllAppsSlidingView if they need to access proper
	// members
	View getApplicationsGrid() {
		return (View) mAllAppsGrid;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_CREATE_SHORTCUT:
			return new CreateShortcut().createDialog();
		case DIALOG_RENAME_FOLDER:
			return new RenameFolder().createDialog();
		case DIALOG_CHOOSE_GROUP:
			return new CreateGrpDialog().createDialog();
		case DIALOG_NEW_GROUP:
			return new NewGrpTitle().createDialog();
		case DIALOG_DELETE_GROUP_CONFIRM:
			return new AlertDialog.Builder(this)
					.setTitle(R.string.AppGroupDelLong)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									delCurrentGrp();
									/* User clicked OK so do some stuff */
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked OK so do some stuff */
								}
							}).create();

		case DIALOG_MY_LONG_PRESS:
			mWaitingForResult = true;
			final Dialog d = new Dialog(this, R.style.theme_myDialog);
			d.setCanceledOnTouchOutside(true);
			View d_v = LayoutInflater.from(this).inflate(
					R.layout.longclick_dialog, null);
			((Button) d_v.findViewById(R.id.btn_add_widget_to_destop))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							d.dismiss();

							addCustomWidgetData(REQUEST_PICK_APPWIDGET);
						}
					});
			((Button) d_v.findViewById(R.id.btn_set_wallpaper))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							d.dismiss();
							startWallpaper();
						}
					});
			d.setOnShowListener(new DialogInterface.OnShowListener() {

				@Override
				public void onShow(DialogInterface dialog) {
					mWorkspace.lock();
				}
			});
			d.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					mWorkspace.unlock();
					mWaitingForResult = false;
				}
			});
			d.setContentView(d_v);

			return d;
		}

		return super.onCreateDialog(id);
	}
	
	void addCustomWidgetData(int requestCode) {
		int appWidgetId = Launcher.this.mAppWidgetHost.allocateAppWidgetId();

		Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
		ArrayList<Bundle> customExtras = new ArrayList<Bundle>();

		// add the search widget
		// AppWidgetProviderInfo info = new
		// AppWidgetProviderInfo();
		// info.provider = new
		// ComponentName(getPackageName(), "XXX.YYY");
		// info.label = getString(R.string.group_search);
		// info.icon = R.drawable.ic_search_widget;
		// customInfo.add(info);
		//
		// Bundle b = new Bundle();
		// b.putString(EXTRA_CUSTOM_WIDGET, SEARCH_WIDGET);
		// customExtras.add(b);

		// add Remind calendar widget
		AppWidgetProviderInfo info1 = new AppWidgetProviderInfo();
		info1.provider = new ComponentName(getPackageName(), "XXX.YYY");
		info1.label = "动机提醒日历";
		info1.icon = R.drawable.remind_icon;
		customInfo.add(info1);

		Bundle b1 = new Bundle();
		b1.putString(EXTRA_CUSTOM_WIDGET, REMIND_CALENDAR_WIDGET);
		customExtras.add(b1);

		AppWidgetProviderInfo info2 = new AppWidgetProviderInfo();
		info2.provider = new ComponentName(getPackageName(), "XXX.YYY");
		info2.label = "任务管理";
		info2.icon = R.drawable.task_manager_widget_icon;
		customInfo.add(info2);
		Bundle b2 = new Bundle();
		b2.putString(EXTRA_CUSTOM_WIDGET, SHORTCUT_TASK_MANAGER_WIDGET);
		customExtras.add(b2);

		AppWidgetProviderInfo info3 = new AppWidgetProviderInfo();
		info3.provider = new ComponentName(getPackageName(), "XXX.YYY");
		info3.label = "动机云应用";
		info3.icon = R.drawable.cloud_app_widget_icon;
		customInfo.add(info3);
		Bundle b3 = new Bundle();
		b3.putString(EXTRA_CUSTOM_WIDGET, SHORTCUT_CLOUD_APP_WIDGET);
		customExtras.add(b3);

		AppWidgetProviderInfo info4 = new AppWidgetProviderInfo();
		info4.provider = new ComponentName(getPackageName(), "XXX.YYY");
		info4.label = "我的应用";
		info4.icon = R.drawable.my_app_widget_icon;
		customInfo.add(info4);
		Bundle b4 = new Bundle();
		b4.putString(EXTRA_CUSTOM_WIDGET, SHORTCUT_MY_APP_WIDGET);
		customExtras.add(b4);

		pickIntent.putParcelableArrayListExtra(
				AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
		pickIntent.putParcelableArrayListExtra(
				AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);

		// start the pick activity
		startActivityForResult(pickIntent, requestCode);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_CREATE_SHORTCUT:
			break;
		case DIALOG_RENAME_FOLDER:
			if (mFolderInfo != null) {
				EditText input = (EditText) dialog
						.findViewById(R.id.folder_name);
				final CharSequence text = mFolderInfo.title;
				input.setText(text);
				input.setSelection(0, text.length());
			}
			break;
		}
	}

	public void delCurrentGrp() {
		int index = sModel.getApplicationsAdapter().getCatalogueFilter()
				.getCurrentFilterIndex();
		AppCatalogueFilters.getInstance().dropGroup(index);
		checkActionButtonsSpecialMode();
		showSwitchGrp();
	}

	public void showSwitchGrp() {
		removeDialog(DIALOG_CHOOSE_GROUP);
		showDialog(DIALOG_CHOOSE_GROUP);
	}

	void showRenameDialog(FolderInfo info) {
		mFolderInfo = info;
		mWaitingForResult = true;
		showDialog(DIALOG_RENAME_FOLDER);
	}

	private void showAddDialog(CellLayout.CellInfo cellInfo) {
		mAddItemCellInfo = cellInfo;
		mWaitingForResult = true;
		showDialog(DIALOG_CREATE_SHORTCUT);
	}

	// zy:mark
	private void showLongPressDialog(CellLayout.CellInfo cellInfo) {
		mAddItemCellInfo = cellInfo;
		mWaitingForResult = true;
		showDialog(DIALOG_MY_LONG_PRESS);
	}

	private void pickShortcut(int requestCode, int title) {
		Bundle bundle = new Bundle();

		ArrayList<String> shortcutNames = new ArrayList<String>();
		shortcutNames.add(getString(R.string.group_applications));
		bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

		ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
		shortcutIcons.add(ShortcutIconResource.fromContext(Launcher.this,
				R.drawable.ic_launcher_application));
		bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				shortcutIcons);

		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(
				Intent.ACTION_CREATE_SHORTCUT));
		pickIntent.putExtra(Intent.EXTRA_TITLE, getText(title));
		pickIntent.putExtras(bundle);

		startActivityForResult(pickIntent, requestCode);
	}

	private class RenameFolder {
		private EditText mInput;

		Dialog createDialog() {
			mWaitingForResult = true;
			final View layout = View.inflate(Launcher.this,
					R.layout.rename_folder, null);
			mInput = (EditText) layout.findViewById(R.id.folder_name);

			AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
			builder.setIcon(0);
			builder.setTitle(getString(R.string.rename_folder_title));
			builder.setCancelable(true);
			builder.setOnCancelListener(new Dialog.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					cleanup();
				}
			});
			builder.setNegativeButton(getString(R.string.cancel_action),
					new Dialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							cleanup();
						}
					});
			builder.setPositiveButton(getString(R.string.rename_action),
					new Dialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							changeFolderName();
						}
					});
			builder.setView(layout);

			final AlertDialog dialog = builder.create();
			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
				public void onShow(DialogInterface dialog) {
					mWorkspace.lock();
				}
			});

			return dialog;
		}

		private void changeFolderName() {
			final String name = mInput.getText().toString();
			if (!TextUtils.isEmpty(name)) {
				// Make sure we have the right folder info
				mFolderInfo = sModel.findFolderById(mFolderInfo.id);
				mFolderInfo.title = name;
				LauncherModel.updateItemInDatabase(Launcher.this, mFolderInfo);

				if (mDesktopLocked) {
					sModel.loadUserItems(false, Launcher.this, false, false, 0);
				} else {
					final FolderIcon folderIcon = (FolderIcon) mWorkspace
							.getViewForTag(mFolderInfo);
					if (folderIcon != null) {
						folderIcon.setText(name);
						getWorkspace().requestLayout();
					} else {
						mDesktopLocked = true;
						sModel.loadUserItems(false, Launcher.this, false,
								false, 0);
					}
				}
			}
			cleanup();
		}

		private void cleanup() {
			mWorkspace.unlock();
			dismissDialog(DIALOG_RENAME_FOLDER);
			mWaitingForResult = false;
			mFolderInfo = null;
		}
	}

	protected class CreateGrpDialog implements DialogInterface.OnClickListener,
			DialogInterface.OnCancelListener,
			DialogInterface.OnDismissListener, DialogInterface.OnShowListener {

		private AppGroupAdapter mAdapter;

		Dialog createDialog() {
			mWaitingForResult = true;

			mAdapter = new AppGroupAdapter(Launcher.this);

			final AlertDialog.Builder builder = new AlertDialog.Builder(
					Launcher.this);
			builder.setTitle(getString(R.string.AppGroupChoose));
			builder.setAdapter(mAdapter, this);

			builder.setInverseBackgroundForced(true);

			AlertDialog dialog = builder.create();
			dialog.setOnCancelListener(this);
			dialog.setOnDismissListener(this);
			dialog.setOnShowListener(this);
			return dialog;
		}

		public void onCancel(DialogInterface dialog) {
			mWaitingForResult = false;
			cleanup();
		}

		public void onDismiss(DialogInterface dialog) {
			mWorkspace.unlock();
		}

		private void cleanup() {
			mWorkspace.unlock();
			dismissDialog(DIALOG_CHOOSE_GROUP);
		}

		public void onClick(DialogInterface dialog, int which) {
			cleanup();
			AppGroupAdapter.ListItem itm = (AppGroupAdapter.ListItem) mAdapter
					.getItem(which);
			int action = itm.actionTag;

			// 1st is add,
			// 2nd is All, mapping to -1, check AppGrpUtils For detail
			// int dbGrp = AppGrpUtils.getGrpNumber(which-2);
			if (action == AppGroupAdapter.APP_GROUP_ADD) {
				showNewGrpDialog();
			} else {
				sModel.getApplicationsAdapter().getCatalogueFilter()
						.setCurrentGroupIndex(action);
				AlmostNexusSettingsHelper.setCurrentAppCatalog(Launcher.this,
						action);
				mAllAppsGrid.updateAppGrp();
				checkActionButtonsSpecialMode();
			}
			// mDrawer.open();
		}

		public void onShow(DialogInterface dialog) {
			mWorkspace.lock();
		}
	}

	private class NewGrpTitle {
		private EditText mInput;

		Dialog createDialog() {
			mWaitingForResult = true;
			final View layout = View.inflate(Launcher.this,
					R.layout.rename_grp, null);
			mInput = (EditText) layout.findViewById(R.id.group_name);

			AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
			builder.setIcon(0);
			builder.setTitle(getString(R.string.rename_group_title));
			builder.setCancelable(true);
			builder.setOnCancelListener(new Dialog.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					cleanup();
				}
			});
			builder.setNegativeButton(getString(R.string.cancel_action),
					new Dialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							cleanup();
						}
					});
			builder.setPositiveButton(getString(R.string.rename_action),
					new Dialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							newGrpName();
						}
					});
			builder.setView(layout);

			final AlertDialog dialog = builder.create();

			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
				public void onShow(DialogInterface dialog) {
					mWorkspace.lock();
					mInput.requestFocus();
					InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.showSoftInput(mInput, 0);
				}
			});

			return dialog;
		}

		private void newGrpName() {
			final String name = mInput.getText().toString();
			mInput.setText("");
			if (!TextUtils.isEmpty(name)) {
				// Make sure we have the right folder info
				int which = AppCatalogueFilters.getInstance().createNewGroup(
						name);
				AlmostNexusSettingsHelper.setCurrentAppCatalog(Launcher.this,
						which);
				sModel.getApplicationsAdapter().getCatalogueFilter()
						.setCurrentGroupIndex(which);
				checkActionButtonsSpecialMode();
				LauncherModel.mApplicationsAdapter.updateDataSet();
			}
			cleanup();
		}

		private void cleanup() {
			mWorkspace.unlock();
			dismissDialog(DIALOG_NEW_GROUP);
			mWaitingForResult = false;
			mFolderInfo = null;
		}
	}

	/**
	 * Displays the shortcut creation dialog and launches, if necessary, the
	 * appropriate activity. bob
	 */
	private class CreateShortcut implements DialogInterface.OnClickListener,
			DialogInterface.OnCancelListener,
			DialogInterface.OnDismissListener, DialogInterface.OnShowListener {

		private AddAdapter mAdapter;

		Dialog createDialog() {
			mWaitingForResult = true;

			mAdapter = new AddAdapter(Launcher.this);

			final AlertDialog.Builder builder = new AlertDialog.Builder(
					Launcher.this);
			builder.setTitle(getString(R.string.menu_item_add_item));
			builder.setAdapter(mAdapter, this);

			builder.setInverseBackgroundForced(true);

			AlertDialog dialog = builder.create();
			dialog.setOnCancelListener(this);
			dialog.setOnDismissListener(this);
			dialog.setOnShowListener(this);

			return dialog;
		}

		public void onCancel(DialogInterface dialog) {
			mWaitingForResult = false;
			cleanup();
		}

		public void onDismiss(DialogInterface dialog) {
			mWorkspace.unlock();
		}

		private void cleanup() {
			mWorkspace.unlock();
			dismissDialog(DIALOG_CREATE_SHORTCUT);
		}

		/**
		 * Handle the action clicked in the "Add to home" dialog.
		 */
		public void onClick(DialogInterface dialog, int which) {
			Resources res = getResources();
			cleanup();

			switch (which) {
			case AddAdapter.ITEM_SHORTCUT: {
				// Insert extra item to handle picking application
				pickShortcut(REQUEST_PICK_SHORTCUT,
						R.string.title_select_shortcut);
				break;
			}

			case AddAdapter.ITEM_APPWIDGET: {
				int appWidgetId = Launcher.this.mAppWidgetHost
						.allocateAppWidgetId();

				Intent pickIntent = new Intent(
						AppWidgetManager.ACTION_APPWIDGET_PICK);
				pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetId);

				ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
				ArrayList<Bundle> customExtras = new ArrayList<Bundle>();

				// add the search widget
				AppWidgetProviderInfo info = new AppWidgetProviderInfo();
				info.provider = new ComponentName(getPackageName(), "XXX.YYY");
				info.label = getString(R.string.group_search);
				info.icon = R.drawable.ic_search_widget;
				customInfo.add(info);

				Bundle b = new Bundle();
				b.putString(EXTRA_CUSTOM_WIDGET, SEARCH_WIDGET);
				customExtras.add(b);

				// add Remind calendar widget
				AppWidgetProviderInfo info1 = new AppWidgetProviderInfo();
				info1.provider = new ComponentName(getPackageName(), "XXX.YYY");
				info1.label = "提醒日历";
				info1.icon = android.R.drawable.ic_input_add;
				customInfo.add(info1);

				Bundle b1 = new Bundle();
				b1.putString(EXTRA_CUSTOM_WIDGET, REMIND_CALENDAR_WIDGET);
				customExtras.add(b1);

				pickIntent.putParcelableArrayListExtra(
						AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
				pickIntent.putParcelableArrayListExtra(
						AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);

				// start the pick activity
				startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
				break;
			}

			case AddAdapter.ITEM_LIVE_FOLDER: {
				// Insert extra item to handle inserting folder
				Bundle bundle = new Bundle();

				ArrayList<String> shortcutNames = new ArrayList<String>();
				shortcutNames.add(res.getString(R.string.group_folder));
				bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME,
						shortcutNames);

				ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
				shortcutIcons.add(ShortcutIconResource.fromContext(
						Launcher.this, R.drawable.ic_launcher_folder));
				bundle.putParcelableArrayList(
						Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

				Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
				pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(
						LiveFolders.ACTION_CREATE_LIVE_FOLDER));
				pickIntent.putExtra(Intent.EXTRA_TITLE,
						getText(R.string.title_select_live_folder));
				pickIntent.putExtras(bundle);

				startActivityForResult(pickIntent, REQUEST_PICK_LIVE_FOLDER);
				break;
			}

			case AddAdapter.ITEM_WALLPAPER: {
				startWallpaper();
				break;
			}

			case AddAdapter.ITEM_ANYCUT: {
				Intent anycutIntent = new Intent();
				anycutIntent.setClass(Launcher.this,
						CustomShirtcutActivity.class);
				startActivityForResult(anycutIntent, REQUEST_PICK_ANYCUT);
				break;
			}
			}
		}

		public void onShow(DialogInterface dialog) {
			mWorkspace.lock();
		}
	}

	/**
	 * Receives notifications when applications are added/removed.
	 */
	private class ApplicationsIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
					|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
					|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {

				final String packageName = intent.getData()
						.getSchemeSpecificPart();
				final boolean replacing = intent.getBooleanExtra(
						Intent.EXTRA_REPLACING, false);

				if (LauncherModel.DEBUG_LOADERS) {
					d(LauncherModel.LOG_TAG, "application intent received: "
							+ action + ", replacing=" + replacing);
					d(LauncherModel.LOG_TAG, "  --> " + intent.getData());
				}

				if (!Intent.ACTION_PACKAGE_CHANGED.equals(action)) {

					if (mAllDrawer != null) {

						System.out.println("replacing:" + replacing);

						mAllDrawer.onPackageChanged(intent, replacing);
					}

					if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
						if (!replacing) {
							removeShortcutsForPackage(packageName);
							if (LauncherModel.DEBUG_LOADERS) {
								d(LauncherModel.LOG_TAG, "  --> remove package");
							}
							sModel.removePackage(Launcher.this, packageName);
						}
						// else, we are replacing the package, so a
						// PACKAGE_ADDED will be sent
						// later, we will update the package at this time
					} else {
						if (!replacing) {
							if (LauncherModel.DEBUG_LOADERS) {
								d(LauncherModel.LOG_TAG, "  --> add package");
							}
							sModel.addPackage(Launcher.this, packageName);
						} else {
							if (LauncherModel.DEBUG_LOADERS) {
								d(LauncherModel.LOG_TAG,
										"  --> update package " + packageName);
							}
							sModel.updatePackage(Launcher.this, packageName);
							updateShortcutsForPackage(packageName);
						}
					}
					removeDialog(DIALOG_CREATE_SHORTCUT);
				} else {
					if (LauncherModel.DEBUG_LOADERS) {
						d(LauncherModel.LOG_TAG, "  --> sync package "
								+ packageName);
					}
					sModel.syncPackage(Launcher.this, packageName);
				}
			} else {
				if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE
						.equals(action)) {
					String packages[] = intent
							.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
					if (packages == null || packages.length == 0) {
						return;
					} else {
						for (int i = 0; i < packages.length; i++) {
							sModel.addPackage(Launcher.this, packages[i]);
							updateShortcutsForPackage(packages[i]);
						}
					}
				} else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE
						.equals(action)) {
					String packages[] = intent
							.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
					if (packages == null || packages.length == 0) {
						return;
					} else {
						for (int i = 0; i < packages.length; i++) {
							sModel.removePackage(Launcher.this, packages[i]);
							// ADW: We tell desktop to update packages
							// (probably will load the standard android icon)
							// to show the user the app is no more available.
							// We may add the froyo code to just load a
							// grayscale version of the icon, but...
							updateShortcutsForPackage(packages[i]);
						}
					}
				}
			}
		}
	}

	/**
	 * Receives notifications when applications are added/removed.
	 */
	private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			closeSystemDialogs();
		}
	}

	/**
	 * Receives notifications whenever the user favorites have changed.
	 */
	private class FavoritesChangeObserver extends ContentObserver {
		public FavoritesChangeObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			onFavoritesChanged();
		}
	}

	/**
	 * Receives notifications whenever the appwidgets are reset.
	 */
	private class AppWidgetResetObserver extends ContentObserver {
		public AppWidgetResetObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			onAppWidgetReset();
		}
	}

	private static class DesktopBinder extends Handler implements
			MessageQueue.IdleHandler {
		static final int MESSAGE_BIND_ITEMS = 0x1;
		static final int MESSAGE_BIND_APPWIDGETS = 0x2;
		static final int MESSAGE_BIND_DRAWER = 0x3;

		// Number of items to bind in every pass
		static final int ITEMS_COUNT = 6;

		private final ArrayList<ItemInfo> mShortcuts;
		private final LinkedList<LauncherAppWidgetInfo> mAppWidgets;
		private final ApplicationsAdapter mDrawerAdapter;
		private final WeakReference<Launcher> mLauncher;

		public boolean mTerminate = false;

		DesktopBinder(Launcher launcher, ArrayList<ItemInfo> shortcuts,
				ArrayList<LauncherAppWidgetInfo> appWidgets,
				ApplicationsAdapter drawerAdapter) {

			mLauncher = new WeakReference<Launcher>(launcher);
			mShortcuts = shortcuts;
			mDrawerAdapter = drawerAdapter;

			// Sort widgets so active workspace is bound first
			final int currentScreen = launcher.mWorkspace.getCurrentScreen();
			final int size = appWidgets.size();
			mAppWidgets = new LinkedList<LauncherAppWidgetInfo>();

			for (int i = 0; i < size; i++) {
				LauncherAppWidgetInfo appWidgetInfo = appWidgets.get(i);
				if (appWidgetInfo.screen == currentScreen) {
					mAppWidgets.addFirst(appWidgetInfo);
				} else {
					mAppWidgets.addLast(appWidgetInfo);
				}
			}

			if (LauncherModel.DEBUG_LOADERS) {
				d(Launcher.LOG_TAG, "------> binding " + shortcuts.size()
						+ " items");
				d(Launcher.LOG_TAG, "------> binding " + appWidgets.size()
						+ " widgets");
			}
		}

		public void startBindingItems() {
			if (LauncherModel.DEBUG_LOADERS)
				d(Launcher.LOG_TAG, "------> start binding items");
			obtainMessage(MESSAGE_BIND_ITEMS, 0, mShortcuts.size())
					.sendToTarget();
		}

		public void startBindingDrawer() {
			obtainMessage(MESSAGE_BIND_DRAWER).sendToTarget();
		}

		public void startBindingAppWidgetsWhenIdle() {
			// Ask for notification when message queue becomes idle
			final MessageQueue messageQueue = Looper.myQueue();
			messageQueue.addIdleHandler(this);
		}

		public boolean queueIdle() {
			// Queue is idle, so start binding items
			startBindingAppWidgets();
			return false;
		}

		public void startBindingAppWidgets() {
			obtainMessage(MESSAGE_BIND_APPWIDGETS).sendToTarget();
		}

		@Override
		public void handleMessage(Message msg) {
			Launcher launcher = mLauncher.get();
			if (launcher == null || mTerminate) {
				return;
			}

			switch (msg.what) {
			case MESSAGE_BIND_ITEMS: {
				launcher.bindItems(this, mShortcuts, msg.arg1, msg.arg2);
				break;
			}
			case MESSAGE_BIND_DRAWER: {
				launcher.bindDrawer(this, mDrawerAdapter);
				break;
			}
			case MESSAGE_BIND_APPWIDGETS: {
				launcher.bindAppWidgets(this, mAppWidgets);
				break;
			}
			}
		}
	}

	/****************************************************************
	 * ADW: Start custom functions/modifications
	 ***************************************************************/

	/**
	 * ADW: Show the custom settings activity
	 */
	private void showCustomConfig() {
		Intent launchPreferencesIntent = new Intent().setClass(this,
				MyLauncherSettings.class);
		startActivity(launchPreferencesIntent);
	}

	private void updateAlmostNexusVars() {
		allowDrawerAnimations = AlmostNexusSettingsHelper
				.getDrawerAnimated(Launcher.this);
		newPreviews = AlmostNexusSettingsHelper.getNewPreviews(this);
		mHomeBinding = AlmostNexusSettingsHelper.getHomeBinding(this);
		mSwipedownAction = AlmostNexusSettingsHelper.getSwipeDownActions(this);
		mSwipeupAction = AlmostNexusSettingsHelper.getSwipeUpActions(this);
		hideStatusBar = AlmostNexusSettingsHelper.getHideStatusbar(this);
		showDots = AlmostNexusSettingsHelper.getUIDots(this);
		mDockStyle = AlmostNexusSettingsHelper.getmainDockStyle(this);
		showDockBar = AlmostNexusSettingsHelper.getUIDockbar(this);
		autoCloseDockbar = AlmostNexusSettingsHelper.getUICloseDockbar(this);
		autoCloseFolder = AlmostNexusSettingsHelper.getUICloseFolder(this);
		hideABBg = AlmostNexusSettingsHelper.getUIABBg(this);
		uiHideLabels = AlmostNexusSettingsHelper.getUIHideLabels(this);
		if (mWorkspace != null) {
			mWorkspace
					.setSpeed(AlmostNexusSettingsHelper.getDesktopSpeed(this));
			mWorkspace.setBounceAmount(AlmostNexusSettingsHelper
					.getDesktopBounce(this));
			mWorkspace.setDefaultScreen(AlmostNexusSettingsHelper
					.getDefaultScreen(this));
			mWorkspace.setWallpaperScroll(AlmostNexusSettingsHelper
					.getWallpaperScrolling(this));
		}
		int animationSpeed = AlmostNexusSettingsHelper.getZoomSpeed(this);
		if (mAllAppsGrid != null) {
			mAllAppsGrid.setAnimationSpeed(animationSpeed);
		}
		// wallpaperHack = AlmostNexusSettingsHelper.getWallpaperHack(this);
		wallpaperHack = false;
		scrollableSupport = AlmostNexusSettingsHelper
				.getUIScrollableWidgets(this);
		useDrawerCatalogNavigation = AlmostNexusSettingsHelper
				.getDrawerCatalogsNavigation(this);
	}

	/**
	 * ADW: Refresh UI status variables and elements after changing settings.
	 */
	private void updateAlmostNexusUI() {
		if (mIsEditMode || mIsWidgetEditMode)
			return;
		updateAlmostNexusVars();
		float scale = AlmostNexusSettingsHelper.getuiScaleAB(this);
		if (scale != uiScaleAB) {
			uiScaleAB = scale;
			/*
			 * mRAB.updateIcon(); mLAB.updateIcon(); mRAB2.updateIcon(); bob
			 * mLAB2.updateIcon(); mHandleView.updateIcon();
			 */
		}
		// if(!showDockBar){
		// mDockBar.close();
		// }
		fullScreen(hideStatusBar);
		/*
		 * if(!mDockBar.isOpen() && !showingPreviews){ if(!isAllAppsVisible()){
		 * mNextView.setVisibility(showDots?View.VISIBLE:View.GONE);
		 * mPreviousView.setVisibility(showDots?View.VISIBLE:View.GONE); bob }
		 * switch (mDockStyle) { case DOCK_STYLE_1:
		 * mRAB.setVisibility(View.GONE); mLAB.setVisibility(View.GONE);
		 * mRAB2.setVisibility(View.GONE); mLAB2.setVisibility(View.GONE); //
		 * mDrawerToolbar.setVisibility(View.VISIBLE); break; case DOCK_STYLE_3:
		 * mRAB.setVisibility(View.VISIBLE); mLAB.setVisibility(View.VISIBLE);
		 * mRAB2.setVisibility(View.GONE); mLAB2.setVisibility(View.GONE);
		 * mDrawerToolbar.setVisibility(View.VISIBLE); break; case DOCK_STYLE_5:
		 * mRAB.setVisibility(View.VISIBLE); mLAB.setVisibility(View.VISIBLE);
		 * mRAB2.setVisibility(View.VISIBLE); mLAB2.setVisibility(View.VISIBLE);
		 * mDrawerToolbar.setVisibility(View.VISIBLE); break; case
		 * DOCK_STYLE_NONE: mDrawerToolbar.setVisibility(View.GONE); default:
		 * break; } //View appsBg=findViewById(R.id.appsBg);
		 * //appsBg.setVisibility(hideAppsBg?View.INVISIBLE:View.VISIBLE);
		 * mHandleView.hideBg(hideABBg); mRAB.hideBg(hideABBg);
		 * mLAB.hideBg(hideABBg); mRAB2.hideBg(hideABBg);
		 * mLAB2.hideBg(hideABBg); }
		 */
		if (mWorkspace != null) {
			mWorkspace.setWallpaperHack(wallpaperHack);
		}
		if (mDesktopIndicator != null) {
			mDesktopIndicator.setType(AlmostNexusSettingsHelper
					.getDesktopIndicatorType(this));
			mDesktopIndicator.setAutoHide(AlmostNexusSettingsHelper
					.getDesktopIndicatorAutohide(this));
			if (mWorkspace != null) {
				mDesktopIndicator.setItems(mWorkspace.getChildCount());
			}
			if (isAllAppsVisible()) {
				if (mDesktopIndicator != null)
					mDesktopIndicator.hide();
			}
		}

	}

	/**
	 * ADW: Create a copy of an application icon/shortcut with a reflection
	 * 
	 * @param layoutResId
	 * @param parent
	 * @param info
	 * @return
	 */
	View createSmallShortcut(int layoutResId, ViewGroup parent,
			ApplicationInfo info) {
		CounterImageView favorite = (CounterImageView) mInflater.inflate(
				layoutResId, parent, false);

		if (!info.filtered) {
			info.icon = Utilities.createIconThumbnail(info.icon, this);
			info.filtered = true;
		}
		favorite.setImageDrawable(Utilities.drawReflection(info.icon, this));
		favorite.setTag(info);
		favorite.setOnClickListener(this);
		// ADW: Counters stuff
		favorite.setCounter(info.counter, info.counterColor);
		return favorite;
	}

	/**
	 * ADW: Create a copy of an folder icon with a reflection
	 * 
	 * @param layoutResId
	 * @param parent
	 * @param info
	 * @return
	 */
	View createSmallFolder(int layoutResId, ViewGroup parent,
			UserFolderInfo info) {
		ImageView favorite = (ImageView) mInflater.inflate(layoutResId, parent,
				false);

		final Resources resources = getResources();
		// Drawable d = resources.getDrawable(R.drawable.ic_launcher_folder);
		Drawable d = null;
		if (AlmostNexusSettingsHelper.getThemeIcons(this)) {
			String packageName = AlmostNexusSettingsHelper.getThemePackageName(
					this, THEME_DEFAULT);
			if (packageName.equals(THEME_DEFAULT)) {
				d = resources.getDrawable(R.drawable.ic_launcher_folder);
			} else {
				d = FolderIcon.loadFolderFromTheme(this, getPackageManager(),
						packageName, "ic_launcher_folder");
				if (d == null) {
					d = resources.getDrawable(R.drawable.ic_launcher_folder);
				}
			}
		} else {
			d = resources.getDrawable(R.drawable.ic_launcher_folder);
		}
		d = Utilities.drawReflection(d, this);
		favorite.setImageDrawable(d);
		favorite.setTag(info);
		favorite.setOnClickListener(this);
		return favorite;
	}

	/**
	 * ADW: Create a copy of an LiveFolder icon with a reflection
	 * 
	 * @param layoutResId
	 * @param parent
	 * @param info
	 * @return
	 */
	View createSmallLiveFolder(int layoutResId, ViewGroup parent,
			LiveFolderInfo info) {
		ImageView favorite = (ImageView) mInflater.inflate(layoutResId, parent,
				false);

		final Resources resources = getResources();
		Drawable d = info.icon;
		if (d == null) {
			if (AlmostNexusSettingsHelper.getThemeIcons(this)) {
				// Drawable d =
				// resources.getDrawable(R.drawable.ic_launcher_folder);
				String packageName = AlmostNexusSettingsHelper
						.getThemePackageName(this, THEME_DEFAULT);
				if (packageName.equals(THEME_DEFAULT)) {
					d = resources.getDrawable(R.drawable.ic_launcher_folder);
				} else {
					d = FolderIcon.loadFolderFromTheme(this,
							getPackageManager(), packageName,
							"ic_launcher_folder");
					if (d == null) {
						d = resources
								.getDrawable(R.drawable.ic_launcher_folder);
					}
				}
			} else {
				d = resources.getDrawable(R.drawable.ic_launcher_folder);
			}
			info.filtered = true;
		}
		d = Utilities.drawReflection(d, this);
		favorite.setImageDrawable(d);
		favorite.setTag(info);
		favorite.setOnClickListener(this);
		return favorite;
	}

	/**
	 * ADW:Create a smaller copy of an icon for use inside Action Buttons
	 * 
	 * @param info
	 * @return
	 */
	Drawable createSmallActionButtonIcon(ItemInfo info) {
		Drawable d = null;
		final Resources resources = getResources();
		if (info != null) {
			if (info instanceof ApplicationInfo) {
				if (!((ApplicationInfo) info).filtered) {
					((ApplicationInfo) info).icon = Utilities
							.createIconThumbnail(((ApplicationInfo) info).icon,
									this);
					((ApplicationInfo) info).filtered = true;
				}
				d = ((ApplicationInfo) info).icon;
			} else if (info instanceof LiveFolderInfo) {
				d = ((LiveFolderInfo) info).icon;
				if (d == null) {
					if (AlmostNexusSettingsHelper.getThemeIcons(this)) {
						// d =
						// Utilities.createIconThumbnail(resources.getDrawable(R.drawable.ic_launcher_folder),
						// this);
						String packageName = AlmostNexusSettingsHelper
								.getThemePackageName(this, THEME_DEFAULT);
						if (!packageName.equals(THEME_DEFAULT)) {
							d = FolderIcon.loadFolderFromTheme(this,
									getPackageManager(), packageName,
									"ic_launcher_folder");
						}
					} else {
						d = Utilities.createIconThumbnail(resources
								.getDrawable(R.drawable.ic_launcher_folder),
								this);
					}
					((LiveFolderInfo) info).filtered = true;
				}
			} else if (info instanceof UserFolderInfo) {
				if (AlmostNexusSettingsHelper.getThemeIcons(this)) {
					// d = resources.getDrawable(R.drawable.ic_launcher_folder);
					String packageName = AlmostNexusSettingsHelper
							.getThemePackageName(this, THEME_DEFAULT);
					if (!packageName.equals(THEME_DEFAULT)) {
						d = FolderIcon.loadFolderFromTheme(this,
								getPackageManager(), packageName,
								"ic_launcher_folder");
					}
				} else {
					d = resources.getDrawable(R.drawable.ic_launcher_folder);
				}
			}
		}
		if (d == null) {
			d = Utilities.createIconThumbnail(
					resources.getDrawable(R.drawable.ab_empty), this);
		}
		d = Utilities.scaledDrawable(d, this, false, uiScaleAB);

		return d;
	}

	Drawable createSmallActionButtonDrawable(Drawable d) {
		d = Utilities.scaledDrawable(d, this, false, uiScaleAB);
		return d;
	}

	// ADW: Previews Functions
	public void previousScreen(View v) {
		mWorkspace.scrollLeft();
	}

	public void nextScreen(View v) {
		mWorkspace.scrollRight();
	}

	protected boolean isPreviewing() {
		return showingPreviews;
	}

	private void fullScreen(boolean enable) {
		if (enable) {
			// go full screen
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(attrs);
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			hideStatusBar = true;
		} else {
			// go non-full screen
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setAttributes(attrs);
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			hideStatusBar = false;
		}
	}

	private void hideDesktop(boolean enable) {
		if (enable) {
			if (mDesktopIndicator != null)
				mDesktopIndicator.hide();
			// mNextView.setVisibility(View.INVISIBLE);
			// mPreviousView.setVisibility(View.INVISIBLE); bob
			// mDrawerToolbar.setVisibility(View.GONE);
			// if(mDockBar.isOpen()){
			// mDockBar.setVisibility(View.INVISIBLE);
			// }
		} else {
			if (mDesktopIndicator != null)
				mDesktopIndicator.show();
			if (mDockBar.isOpen()) {
				mDockBar.setVisibility(View.VISIBLE);
			} else {
				// if(mDockStyle!=DOCK_STYLE_NONE)mDrawerToolbar.setVisibility(View.VISIBLE);
				/*
				 * if(showDots){ mNextView.setVisibility(View.VISIBLE);
				 * mPreviousView.setVisibility(View.VISIBLE); bob }
				 */
			}
		}
	}

	public void dismissPreviews() {
		if (showingPreviews) {
			if (newPreviews) {
				hideDesktop(false);
				showingPreviews = false;
				// mDesktopLocked=false;
				mWorkspace.openSense(false);
			} else {
				// dismissPreview(mNextView);
				// dismissPreview(mPreviousView); bob
				// dismissPreview(mHandleView);
				for (int i = 0; i < mWorkspace.getChildCount(); i++) {
					View cell = mWorkspace.getChildAt(i);
					cell.setDrawingCacheEnabled(false);
				}
			}
		}
	}

	private void dismissPreview(final View v) {
		final PopupWindow window = (PopupWindow) v.getTag(R.id.TAG_PREVIEW);
		if (window != null) {
			hideDesktop(false);
			window.setOnDismissListener(new PopupWindow.OnDismissListener() {
				public void onDismiss() {
					ViewGroup group = (ViewGroup) v.getTag(R.id.workspace);
					int count = group.getChildCount();
					for (int i = 0; i < count; i++) {
						((ImageView) group.getChildAt(i))
								.setImageDrawable(null);
					}
					ArrayList<Bitmap> bitmaps = (ArrayList<Bitmap>) v
							.getTag(R.id.icon);
					for (Bitmap bitmap : bitmaps)
						bitmap.recycle();

					v.setTag(R.id.workspace, null);
					v.setTag(R.id.icon, null);
					window.setOnDismissListener(null);
				}
			});
			window.dismiss();
			showingPreviews = false;
			mWorkspace.unlock();
			mWorkspace.invalidate();
			mDesktopLocked = false;
		}
		v.setTag(R.id.TAG_PREVIEW, null);
	}

	private void showPreviousPreview(View anchor) {
		int current = mWorkspace.getCurrentScreen();
		if (newPreviews) {
			if (current <= 0)
				return;
			showPreviews(anchor, 0, mWorkspace.getCurrentScreen());
		} else {
			showPreviews(anchor, 0, mWorkspace.getChildCount());
		}
	}

	private void showNextPreview(View anchor) {
		int current = mWorkspace.getCurrentScreen();
		if (newPreviews) {
			if (current >= mWorkspace.getChildCount() - 1)
				return;
			showPreviews(anchor, mWorkspace.getCurrentScreen() + 1,
					mWorkspace.getChildCount());
		} else {
			showPreviews(anchor, 0, mWorkspace.getChildCount());
		}
	}

	public void showPreviews(final View anchor, int start, int end) {
		if (newPreviews) {
			showingPreviews = true;
			hideDesktop(true);
			mWorkspace.lock();
			mWorkspace.openSense(true);
		} else {
			// check first if it's already open
			final PopupWindow window = (PopupWindow) anchor
					.getTag(R.id.TAG_PREVIEW);
			if (window != null)
				return;
			Resources resources = getResources();

			Workspace workspace = mWorkspace;
			CellLayout cell = ((CellLayout) workspace.getChildAt(start));
			float max;
			ViewGroup preview;
			max = workspace.getChildCount();
			preview = new LinearLayout(this);

			Rect r = new Rect();
			// ADW: seems sometimes this throws an out of memory error.... so...
			try {
				resources.getDrawable(R.drawable.preview_background)
						.getPadding(r);
			} catch (OutOfMemoryError e) {
			}
			int extraW = (int) ((r.left + r.right) * max);
			int extraH = r.top + r.bottom;

			int aW = cell.getWidth() - extraW;
			float w = aW / max;

			int width = cell.getWidth();
			int height = cell.getHeight();
			// width -= (x + cell.getRightPadding());
			// height -= (y + cell.getBottomPadding());
			if (width != 0 && height != 0) {
				showingPreviews = true;
				float scale = w / width;

				int count = end - start;

				final float sWidth = width * scale;
				float sHeight = height * scale;

				PreviewTouchHandler handler = new PreviewTouchHandler(anchor);
				ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(count);

				for (int i = start; i < end; i++) {
					ImageView image = new ImageView(this);
					cell = (CellLayout) workspace.getChildAt(i);
					Bitmap bitmap = Bitmap.createBitmap((int) sWidth,
							(int) sHeight, Bitmap.Config.ARGB_8888);
					cell.setDrawingCacheEnabled(false);
					Canvas c = new Canvas(bitmap);
					c.scale(scale, scale);
					c.translate(-cell.getLeftPadding(), -cell.getTopPadding());
					cell.dispatchDraw(c);

					image.setBackgroundDrawable(resources
							.getDrawable(R.drawable.preview_background));
					image.setImageBitmap(bitmap);
					image.setTag(i);
					image.setOnClickListener(handler);
					image.setOnFocusChangeListener(handler);
					image.setFocusable(true);
					if (i == mWorkspace.getCurrentScreen())
						image.requestFocus();

					preview.addView(image,
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);

					bitmaps.add(bitmap);
				}

				PopupWindow p = new PopupWindow(this);
				p.setContentView(preview);
				p.setWidth((int) (sWidth * count + extraW));
				p.setHeight((int) (sHeight + extraH));
				p.setAnimationStyle(R.style.AnimationPreview);
				p.setOutsideTouchable(true);
				p.setFocusable(true);
				p.setBackgroundDrawable(new ColorDrawable(0));
				p.showAsDropDown(anchor, 0, 0);
				p.setOnDismissListener(new PopupWindow.OnDismissListener() {
					public void onDismiss() {
						dismissPreview(anchor);
					}
				});
				anchor.setTag(R.id.TAG_PREVIEW, p);
				anchor.setTag(R.id.workspace, preview);
				anchor.setTag(R.id.icon, bitmaps);
			}
		}
	}

	class PreviewTouchHandler implements View.OnClickListener, Runnable,
			View.OnFocusChangeListener {
		private final View mAnchor;

		public PreviewTouchHandler(View anchor) {
			mAnchor = anchor;
		}

		public void onClick(View v) {
			mWorkspace.snapToScreen((Integer) v.getTag());
			v.post(this);
		}

		public void run() {
			dismissPreview(mAnchor);
		}

		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				mWorkspace.snapToScreen((Integer) v.getTag());
			}
		}
	}

	/**
	 * ADW: Override this to hide statusbar when necessary
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (mShouldHideStatusbaronFocus && hasFocus) {
			fullScreen(true);
			mShouldHideStatusbaronFocus = false;
		}
	}

	/************************************************
	 * ADW: Functions to handle Apps Grid
	 */
	public void showAllApps(boolean animated, AppCatalogueFilter filter) {
		if (!allAppsOpen) {
			if (getWindow().getDecorView().getWidth() > getWindow()
					.getDecorView().getHeight()) {
				/*
				 * mHandleView.setNextFocusUpId(R.id.drag_layer);
				 * mHandleView.setNextFocusLeftId(R.id.all_apps_view);
				 * mLAB.setNextFocusUpId(R.id.drag_layer);
				 * mLAB.setNextFocusLeftId(R.id.all_apps_view);
				 * mRAB.setNextFocusUpId(R.id.drag_layer);
				 * mRAB.setNextFocusLeftId(R.id.all_apps_view);
				 * mLAB2.setNextFocusUpId(R.id.drag_layer);
				 * mLAB2.setNextFocusLeftId(R.id.all_apps_view); bob
				 * mRAB2.setNextFocusUpId(R.id.drag_layer);
				 * mRAB2.setNextFocusLeftId(R.id.all_apps_view);
				 */
			} else {
				/*
				 * mHandleView.setNextFocusUpId(R.id.all_apps_view);
				 * mHandleView.setNextFocusLeftId(R.id.drag_layer);
				 * mLAB.setNextFocusUpId(R.id.all_apps_view);
				 * mLAB.setNextFocusLeftId(R.id.drag_layer);
				 * mRAB.setNextFocusUpId(R.id.all_apps_view);
				 * mRAB.setNextFocusLeftId(R.id.drag_layer);
				 * mLAB2.setNextFocusUpId(R.id.all_apps_view);
				 * mLAB2.setNextFocusLeftId(R.id.drag_layer); bob
				 * mRAB2.setNextFocusUpId(R.id.all_apps_view);
				 * mRAB2.setNextFocusLeftId(R.id.drag_layer);
				 */
			}
			mWorkspace.hideWallpaper(true);
			allAppsOpen = true;
			mWorkspace.enableChildrenCache();
			mWorkspace.lock();
			if (filter != null)
				sModel.getApplicationsAdapter().setCatalogueFilter(filter);
			else
				sModel.getApplicationsAdapter().setCatalogueFilter(
						AppCatalogueFilters.getInstance().getDrawerFilter());
			// mDesktopLocked=true;
			mWorkspace.invalidate();
			checkActionButtonsSpecialMode();
			mAllAppsGrid.open(animated && allowDrawerAnimations);
			// mPreviousView.setVisibility(View.GONE);
			// mNextView.setVisibility(View.GONE);bob
			if (mDesktopIndicator != null)
				mDesktopIndicator.hide();
			mAllAppsGrid.setAdapter(sModel.getApplicationsAdapter());

			// mDesktopLayout.setVisibility(View.GONE);
			// mAllDrawer.setVisibility(View.VISIBLE);
			// // mAllDrawer.invalidate();
			// findViewById(R.id.launcherlayout).setVisibility(View.GONE);
		} else if (filter != null)
			sModel.getApplicationsAdapter().setCatalogueFilter(filter);
		else {
			sModel.getApplicationsAdapter().setCatalogueFilter(
					AppCatalogueFilters.getInstance().getDrawerFilter());
			mAllAppsGrid.setAdapter(sModel.getApplicationsAdapter());
		}
		customerAppsOpen = false;
	}

	/*
	 * public void setCustmer(boolean animated, AppCatalogueFilter filter) {
	 * allAppsOpen=false; if(!customerAppsOpen) {
	 * if(getWindow().getDecorView().getWidth
	 * ()>getWindow().getDecorView().getHeight()){
	 * mHandleView.setNextFocusUpId(R.id.drag_layer);
	 * mHandleView.setNextFocusLeftId(R.id.all_apps_view);
	 * mLAB.setNextFocusUpId(R.id.drag_layer);
	 * mLAB.setNextFocusLeftId(R.id.all_apps_view);
	 * mRAB.setNextFocusUpId(R.id.drag_layer);
	 * mRAB.setNextFocusLeftId(R.id.all_apps_view);
	 * mLAB2.setNextFocusUpId(R.id.drag_layer);
	 * mLAB2.setNextFocusLeftId(R.id.all_apps_view);
	 * mRAB2.setNextFocusUpId(R.id.drag_layer);
	 * mRAB2.setNextFocusLeftId(R.id.all_apps_view); }else{
	 * mHandleView.setNextFocusUpId(R.id.all_apps_view);
	 * mHandleView.setNextFocusLeftId(R.id.drag_layer);
	 * mLAB.setNextFocusUpId(R.id.all_apps_view);
	 * mLAB.setNextFocusLeftId(R.id.drag_layer);
	 * mRAB.setNextFocusUpId(R.id.all_apps_view);
	 * mRAB.setNextFocusLeftId(R.id.drag_layer);
	 * mLAB2.setNextFocusUpId(R.id.all_apps_view);
	 * mLAB2.setNextFocusLeftId(R.id.drag_layer);
	 * mRAB2.setNextFocusUpId(R.id.all_apps_view);
	 * mRAB2.setNextFocusLeftId(R.id.drag_layer); }
	 * mWorkspace.hideWallpaper(true); customerAppsOpen=true;
	 * mWorkspace.enableChildrenCache(); mWorkspace.lock(); if (filter != null)
	 * sModel.getApplicationsAdapters(this).setCatalogueFilter(filter); else
	 * sModel
	 * .getApplicationsAdapters(this).setCatalogueFilter(AppCatalogueFilters
	 * .getInstance().getDrawerFilter()); //mDesktopLocked=true;
	 * mWorkspace.invalidate(); checkActionButtonsSpecialMode();
	 * mAllAppsGrid.open(animated && allowDrawerAnimations); //
	 * mPreviousView.setVisibility(View.GONE); //
	 * mNextView.setVisibility(View.GONE); bob
	 * if(mDesktopIndicator!=null)mDesktopIndicator.hide(); }
	 * mAllAppsGrid.setAdapter(sModel.getApplicationsAdapters(this)); bob }
	 */

	private void checkActionButtonsSpecialMode() {
		boolean showSpecialMode = useDrawerCatalogNavigation
				&& allAppsOpen
				&& AppCatalogueFilters.getInstance().getUserCatalogueCount() > 0;
		// mLAB.setSpecialMode(showSpecialMode);
		// mRAB.setSpecialMode(showSpecialMode);bob
	}

	private void closeAllApps(boolean animated) {
		if (allAppsOpen) {
			/*
			 * mHandleView.setNextFocusUpId(R.id.drag_layer);
			 * mHandleView.setNextFocusLeftId(R.id.drag_layer);
			 * mLAB.setNextFocusUpId(R.id.drag_layer);
			 * mLAB.setNextFocusLeftId(R.id.drag_layer);
			 * mRAB.setNextFocusUpId(R.id.drag_layer);
			 * mRAB.setNextFocusLeftId(R.id.drag_layer);
			 * mLAB2.setNextFocusUpId(R.id.drag_layer);
			 * mLAB2.setNextFocusLeftId(R.id.drag_layer);
			 * mRAB2.setNextFocusUpId(R.id.drag_layer); bob
			 * mRAB2.setNextFocusLeftId(R.id.drag_layer);
			 */
			mWorkspace.hideWallpaper(false);
			allAppsOpen = false;
			mWorkspace.unlock();
			mDesktopLocked=false;
			mWorkspace.invalidate();
			/*
			 * mLAB.setSpecialMode(false); mRAB.setSpecialMode(false);
			 */

			/*
			 * if(!isDockBarOpen() && showDots){
			 * mPreviousView.setVisibility(View.VISIBLE);
			 * mNextView.setVisibility(View.VISIBLE); }else{
			 * mPreviousView.setVisibility(View.GONE);
			 * mNextView.setVisibility(View.GONE); bob }
			 */
			if (mDesktopIndicator != null)
				mDesktopIndicator.show();

			mAllAppsGrid.close(animated && allowDrawerAnimations);
			mAllAppsGrid.clearTextFilter();
		}
	}

	boolean isAllAppsVisible() {
		// return allAppsOpen;
		if (mAllAppsGrid != null)
			return mAllAppsGrid.getVisibility() == View.VISIBLE;
		else
			return false;
	}

	boolean isAllAppsOpaque() {
		return mAllAppsGrid.isOpaque() && !allAppsAnimating;
	}

	protected boolean isDockBarOpen() {
		return mDockBar.isOpen();
	}

	/**
	 * ADW: wallpaper intent receiver for proper trackicng of wallpaper changes
	 */
	private static class WallpaperIntentReceiver extends BroadcastReceiver {
		private WeakReference<Launcher> mLauncher;

		WallpaperIntentReceiver(Application application, Launcher launcher) {
			setLauncher(launcher);
		}

		void setLauncher(Launcher launcher) {
			mLauncher = new WeakReference<Launcher>(launcher);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mLauncher != null) {
				final Launcher launcher = mLauncher.get();
				if (launcher != null) {
					final Workspace workspace = launcher.getWorkspace();
					if (workspace != null) {
						workspace.setWallpaper(true);
					}
				}
			}
		}
	}

	public void setWindowBackground(boolean lwp) {
		wallpaperHack = lwp;
		if (!lwp) {
			getWindow().setBackgroundDrawable(null);
			getWindow().setFormat(PixelFormat.OPAQUE);
			// getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		} else {
			getWindow().setBackgroundDrawable(new ColorDrawable(0));
			getWindow().setFormat(PixelFormat.TRANSPARENT);
			// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		}
	}

	private boolean shouldRestart() {
		try {
			if (mShouldRestart) {
				android.os.Process.killProcess(android.os.Process.myPid());
				finish();
				startActivity(getIntent());
				return true;
			} else {
				/*
				 * if(mMessWithPersistence){ int
				 * currentOrientation=getResources(
				 * ).getConfiguration().orientation;
				 * if(currentOrientation!=savedOrientation){
				 * mShouldRestart=true; finish(); startActivity(getIntent()); }
				 * }
				 */
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		// ADW: Try to add the restart flag here instead on preferences activity
		if (AlmostNexusSettingsHelper.needsRestart(key)) {
			setPersistent(false);
			mShouldRestart = true;
		} else {
			// TODO: ADW Move here all the updates instead on
			// updateAlmostNexusUI()
			if (key.equals("homeOrientation")) {
				if (!mMessWithPersistence) {
					changeOrientation(
							AlmostNexusSettingsHelper
									.getDesktopOrientation(this),
							false);
				} else {
					// ADW: If a user changes between different orientation
					// modes
					// we temporarily disable persistence to change the app
					// orientation
					// it will be re-enabled on the next onCreate
					setPersistent(false);
					changeOrientation(
							AlmostNexusSettingsHelper
									.getDesktopOrientation(this),
							true);
				}
			} else if (key.equals("systemPersistent")) {
				mMessWithPersistence = AlmostNexusSettingsHelper
						.getSystemPersistent(this);
				if (mMessWithPersistence) {
					changeOrientation(
							AlmostNexusSettingsHelper
									.getDesktopOrientation(this),
							true);
					// ADW: If previously in portrait, set persistent
					// else, it will call the setPersistent on the next onCreate
					// caused by the orientation change
					if (savedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
						setPersistent(true);
				} else {
					setPersistent(false);
					changeOrientation(
							AlmostNexusSettingsHelper
									.getDesktopOrientation(this),
							false);
				}
			} else if (key.equals("uiScrollableWidgets")) {
				boolean scroll = AlmostNexusSettingsHelper
						.getUIScrollableWidgets(this);
				scrollableSupport = scroll;
				if (scroll) {
					mWorkspace.registerProvider();
				} else {
					mWorkspace.unregisterProvider();
				}
				sModel.loadUserItems(false, Launcher.this, false, false, 0);
			} else if (key.equals("notif_receiver")) {
				boolean useNotifReceiver = AlmostNexusSettingsHelper
						.getNotifReceiver(this);
				if (!useNotifReceiver) {
					if (mCounterReceiver != null)
						unregisterReceiver(mCounterReceiver);
					mCounterReceiver = null;
				} else {
					if (mCounterReceiver == null) {
						mCounterReceiver = new CounterReceiver(this);
						mCounterReceiver
								.setCounterListener(new CounterReceiver.OnCounterChangedListener() {
									public void onTrigger(String pname,
											int counter, int color) {
										updateCountersForPackage(pname,
												counter, color);
									}
								});
					}
					registerReceiver(mCounterReceiver,
							mCounterReceiver.getFilter());
				}
			} else if (key.equals("main_dock_style")) {
				int dockstyle = AlmostNexusSettingsHelper
						.getmainDockStyle(this);
				if (dockstyle == DOCK_STYLE_NONE) {
					mShouldRestart = true;
				} else if (mDockStyle == DOCK_STYLE_NONE) {
					mShouldRestart = true;
				}
			}
			updateAlmostNexusUI();
		}
	}

	private void appwidgetReadyBroadcast(int appWidgetId, ComponentName cname,
			int[] widgetSpan) {
		Intent motosize = new Intent(
				"com.motorola.blur.home.ACTION_SET_WIDGET_SIZE");

		motosize.setComponent(cname);
		motosize.putExtra("appWidgetId", appWidgetId);
		motosize.putExtra("spanX", widgetSpan[0]);
		motosize.putExtra("spanY", widgetSpan[1]);
		motosize.putExtra("com.motorola.blur.home.EXTRA_NEW_WIDGET", true);
		sendBroadcast(motosize);

		if (isScrollableAllowed()) {
			Intent ready = new Intent(LauncherIntent.Action.ACTION_READY)
					.putExtra(LauncherIntent.Extra.EXTRA_APPWIDGET_ID,
							appWidgetId)
					.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
					.putExtra(LauncherIntent.Extra.EXTRA_API_VERSION,
							LauncherMetadata.CurrentAPIVersion)
					.setComponent(cname);
			sendBroadcast(ready);
		}
	}

	/**
	 * ADW: Home binding actions
	 */
	public void fireHomeBinding(int bindingValue, int type) {
		// ADW: switch home button binding user selection
		if (mIsEditMode || mIsWidgetEditMode)
			return;
		switch (bindingValue) {
		case BIND_DEFAULT:
			dismissPreviews();
			if (!mWorkspace.isDefaultScreenShowing()) {
				mWorkspace.moveToDefaultScreen();
			}
			break;
		case BIND_HOME_PREVIEWS:
			if (!mWorkspace.isDefaultScreenShowing()) {
				dismissPreviews();
				mWorkspace.moveToDefaultScreen();
			} else {
				if (!showingPreviews) {
					// showPreviews(mHandleView, 0, mWorkspace.mHomeScreens);
					// bob
				} else {
					dismissPreviews();
				}
			}
			break;
		case BIND_PREVIEWS:
			if (!showingPreviews) {
				// showPreviews(mHandleView, 0, mWorkspace.mHomeScreens); bob
			} else {
				dismissPreviews();
			}
			break;
		case BIND_APPS:
			dismissPreviews();
			if (isAllAppsVisible()) {
				closeDrawer();
			} else {
				showAllApps(true, null);
			}
			break;
		case BIND_STATUSBAR:
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			if ((attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
				// go non-full screen
				fullScreen(false);
			} else {
				// go full screen
				fullScreen(true);
			}
			break;
		case BIND_NOTIFICATIONS:
			dismissPreviews();
			showNotifications();
			break;
		case BIND_HOME_NOTIFICATIONS:
			if (!mWorkspace.isDefaultScreenShowing()) {
				dismissPreviews();
				mWorkspace.moveToDefaultScreen();
			} else {
				dismissPreviews();
				showNotifications();
			}
			break;
		case BIND_DOCKBAR:
			dismissPreviews();
			if (showDockBar) {
				// if(mDockBar.isOpen()){
				// mDockBar.close();
				// }else{
				mDockBar.open();
				// }
			}
			break;
		case BIND_APP_LAUNCHER:
			// Launch or bring to front selected app
			// Get PackageName and ClassName of selected App
			String package_name = "";
			String name = "";
			switch (type) {
			case 1:
				package_name = AlmostNexusSettingsHelper
						.getHomeBindingAppToLaunchPackageName(this);
				name = AlmostNexusSettingsHelper
						.getHomeBindingAppToLaunchName(this);
				break;
			case 2:
				package_name = AlmostNexusSettingsHelper
						.getSwipeUpAppToLaunchPackageName(this);
				name = AlmostNexusSettingsHelper
						.getSwipeUpAppToLaunchName(this);
				break;
			case 3:
				package_name = AlmostNexusSettingsHelper
						.getSwipeDownAppToLaunchPackageName(this);
				name = AlmostNexusSettingsHelper
						.getSwipeDownAppToLaunchName(this);
				break;
			default:
				break;
			}
			// Create Intent to Launch App
			if (package_name != "" && name != "") {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_MAIN);
				i.addCategory(Intent.CATEGORY_LAUNCHER);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				i.setComponent(new ComponentName(package_name, name));
				try {
					startActivity(i);
				} catch (Exception e) {
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * wjax: Swipe down binding action
	 */
	public void fireSwipeDownAction() {
		// wjax: switch SwipeDownAction button binding user selection
		fireHomeBinding(mSwipedownAction, 3);
	}

	/**
	 * wjax: Swipe up binding action
	 */
	public void fireSwipeUpAction() {
		// wjax: switch SwipeUpAction button binding user selection
		fireHomeBinding(mSwipeupAction, 2);
	}

	public boolean isScrollableAllowed() {
		return scrollableSupport;
	}

	private void realAddWidget(AppWidgetProviderInfo appWidgetInfo,
			CellLayout.CellInfo cellInfo, int[] spans, int appWidgetId,
			boolean insertAtFirst) {
		// Try finding open space on Launcher screen
		final int[] xy = new int[2];
		if (!findSlot(cellInfo, xy, spans[0], spans[1])) {
			if (appWidgetId != -1)
				mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			return;
		}

		// Build Launcher-specific widget info and save to database
		LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(
				appWidgetId);
		launcherInfo.spanX = spans[0];
		launcherInfo.spanY = spans[1];

		LauncherModel.addItemToDatabase(this, launcherInfo,
				LauncherSettings.Favorites.CONTAINER_DESKTOP,
				mWorkspace.getCurrentScreen(), xy[0], xy[1], false);

		if (!mRestoring) {
			sModel.addDesktopAppWidget(launcherInfo);

			// Perform actual inflation because we're live
			launcherInfo.hostView = mAppWidgetHost.createView(this,
					appWidgetId, appWidgetInfo);

			launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
			launcherInfo.hostView.setTag(launcherInfo);

			mWorkspace.addInCurrentScreen(launcherInfo.hostView, xy[0], xy[1],
					launcherInfo.spanX, launcherInfo.spanY, insertAtFirst);
		} else if (sModel.isDesktopLoaded()) {
			sModel.addDesktopAppWidget(launcherInfo);
		}
		// finish load a widget, send it an intent
		if (appWidgetInfo != null)
			appwidgetReadyBroadcast(appWidgetId, appWidgetInfo.provider, spans);
	}

	public View realAddWidget2(AppWidgetProviderInfo appWidgetInfo, int screen,
			int[] XY, int[] spans, int appWidgetId) {
		// Try finding open space on Launcher screen

		// Build Launcher-specific widget info and save to database
		LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(
				appWidgetId);
		launcherInfo.spanX = spans[0];
		launcherInfo.spanY = spans[1];

		LauncherModel.addItemToDatabase(this, launcherInfo,
				LauncherSettings.Favorites.CONTAINER_DESKTOP,
				mWorkspace.getCurrentScreen(), XY[0], XY[1], false);

		if (!mRestoring) {
			sModel.addDesktopAppWidget(launcherInfo);

			// Perform actual inflation because we're live
			launcherInfo.hostView = mAppWidgetHost.createView(this,
					appWidgetId, appWidgetInfo);

			launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
			launcherInfo.hostView.setTag(launcherInfo);

			mWorkspace.addInScreen(launcherInfo.hostView, screen, XY[0], XY[1],
					launcherInfo.spanX, launcherInfo.spanY);
		} else if (sModel.isDesktopLoaded()) {
			sModel.addDesktopAppWidget(launcherInfo);
		}

		if (appWidgetInfo != null)
			appwidgetReadyBroadcast(appWidgetId, appWidgetInfo.provider, spans);

		return launcherInfo.hostView;
	}

	public View realAddWidget3(AppWidgetProviderInfo appWidgetInfo, int screen,
			int[] XY, int[] spans, int appWidgetId) {
		// Try finding open space on Launcher screen

		// Build Launcher-specific widget info and save to database
		LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(
				appWidgetId);
		launcherInfo.spanX = spans[0];
		launcherInfo.spanY = spans[1];

		LauncherModel.addItemToDatabase(this, launcherInfo,
				LauncherSettings.Favorites.CONTAINER_DESKTOP, screen, XY[0],
				XY[1], false, saveMob);

		if (!mRestoring) {
			sModel.addDesktopAppWidget(launcherInfo);

			// Perform actual inflation because we're live
			launcherInfo.hostView = mAppWidgetHost.createView(this,
					appWidgetId, appWidgetInfo);

			launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
			launcherInfo.hostView.setTag(launcherInfo);

			mWorkspace.addInScreen(launcherInfo.hostView, screen, XY[0], XY[1],
					launcherInfo.spanX, launcherInfo.spanY);
		} else if (sModel.isDesktopLoaded()) {
			sModel.addDesktopAppWidget(launcherInfo);
		}

		if (appWidgetInfo != null)
			appwidgetReadyBroadcast(appWidgetId, appWidgetInfo.provider, spans);

		return launcherInfo.hostView;
	}

	private void realAddSearch(Widget info, final CellLayout.CellInfo cellInfo,
			final int[] xy, int spanX, int spanY) {
		if (!findSlot(cellInfo, xy, spanX, spanY))
			return;
		info.spanX = spanX;
		info.spanY = spanY;
		sModel.addDesktopItem(info);
		LauncherModel.addItemToDatabase(this, info,
				LauncherSettings.Favorites.CONTAINER_DESKTOP,
				mWorkspace.getCurrentScreen(), xy[0], xy[1], false);

		final View view = mInflater.inflate(info.layoutResource, null);
		view.setTag(info);
		Search search = (Search) view.findViewById(R.id.widget_search);
		search.setLauncher(this);

		mWorkspace.addInCurrentScreen(view, xy[0], xy[1], spanX, spanY);

	}

	public View addWidget(AppWidgetProviderInfo appWidgetInfo, int screen,
			int[] xys, int[] spans) {

		int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

		ComponentName cn = appWidgetInfo.provider;

		final AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(this);
		appWidgetManager.bindAppWidgetId(appWidgetId, cn);

		final int[] xy = xys;
		LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(
				appWidgetId);
		launcherInfo.spanX = spans[0];
		launcherInfo.spanY = spans[1];

		LauncherModel.addItemToDatabase(this, launcherInfo,
				LauncherSettings.Favorites.CONTAINER_DESKTOP, screen, xy[0],
				xy[1], false);

		if (!mRestoring) {
			sModel.addDesktopAppWidget(launcherInfo);

			launcherInfo.hostView = mAppWidgetHost.createView(this,
					appWidgetId, appWidgetInfo);
			launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
			launcherInfo.hostView.setTag(launcherInfo);

			mAppWidgetHost.startListening();

			mWorkspace.addInScreen(launcherInfo.hostView, screen, xy[0], xy[1],
					launcherInfo.spanX, launcherInfo.spanY, false);
		} else if (sModel.isDesktopLoaded()) {
			sModel.addDesktopAppWidget(launcherInfo);
		}

		if (appWidgetInfo != null)
			appwidgetReadyBroadcast(appWidgetId, appWidgetInfo.provider, spans);

		return launcherInfo.hostView;
	}

	/*
	 * void editShirtcut(ApplicationInfo info) { bob Intent edit = new
	 * Intent(Intent.ACTION_EDIT); edit.setClass(this,
	 * CustomShirtcutActivity.class);
	 * edit.putExtra(CustomShirtcutActivity.EXTRA_APPLICATIONINFO, info.id);
	 * startActivityForResult(edit, REQUEST_EDIT_SHIRTCUT); }
	 */

	/*
	 * private void completeEditShirtcut(Intent data) { if
	 * (!data.hasExtra(CustomShirtcutActivity.EXTRA_APPLICATIONINFO)) return;
	 * long appInfoId =
	 * data.getLongExtra(CustomShirtcutActivity.EXTRA_APPLICATIONINFO, 0);
	 * ApplicationInfo info = LauncherModel.loadApplicationInfoById(this,
	 * appInfoId); if (info != null) { Bitmap bitmap =
	 * data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
	 * 
	 * launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId,
	 * appWidgetInfo); launcherInfo.hostView.setAppWidget(appWidgetId,
	 * appWidgetInfo); launcherInfo.hostView.setTag(launcherInfo);
	 * 
	 * mAppWidgetHost.startListening();
	 * 
	 * mWorkspace.addInScreen(launcherInfo.hostView,screen, xy[0],
	 * xy[1],launcherInfo.spanX, launcherInfo.spanY, false); } else if
	 * (sModel.isDesktopLoaded()) { sModel.addDesktopAppWidget(launcherInfo); }
	 * 
	 * if(appWidgetInfo!=null) appwidgetReadyBroadcast(appWidgetId,
	 * appWidgetInfo.provider, spans);
	 * 
	 * return launcherInfo.hostView; }
	 */

	public boolean configureOrAddAppWidget(AppWidgetProviderInfo appWidget) {
		if (appWidget.configure != null) {
			// Launch over to configure widget, if needed
			int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

			ComponentName cn = appWidget.provider;

			final AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(this);
			appWidgetManager.bindAppWidgetId(appWidgetId, cn);

			Intent intent = new Intent(
					AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(appWidget.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

			startActivityForResult(intent, REQUEST_MINI_CREATE_APPWIDGET);

			return true;
		}

		return false;
	}

	public boolean configureOrAddAppWidget2(AppWidgetProviderInfo appWidget,
			WorkspaceMini workspaceMini) {

		this.add_workspaceMini = workspaceMini;

		if (appWidget.configure != null) {
			int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

			ComponentName cn = appWidget.provider;

			final AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(this);
			appWidgetManager.bindAppWidgetId(appWidgetId, cn);

			Intent intent = new Intent(
					AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(appWidget.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

			startActivityForResult(intent, REQUEST_MINI_CREATE_APPWIDGET_2);

			return true;
		}

		return false;
	}

	public static int getScreenCount(Context context) {
		return AlmostNexusSettingsHelper.getDesktopScreens(context);
	}

	public DesktopIndicator getDesktopIndicator() {
		return mDesktopIndicator;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		// if(mMessWithPersistence)setPersistent(false);
		super.onStart();
		// int currentOrientation=getResources().getConfiguration().orientation;
		// if(currentOrientation!=savedOrientation){
		// mShouldRestart=true;
		// }
	}

	@Override
	protected void onStop() {
		// if(!mShouldRestart){
		// savedOrientation=getResources().getConfiguration().orientation;
		// if(mMessWithPersistence)setPersistent(true);
		// }
		// TODO Auto-generated method stub
		super.onStop();
	}

	/**
	 * ADW: Load the specified theme resource
	 * 
	 * @param themeResources
	 *            Resources from the theme package
	 * @param themePackage
	 *            the theme's package name
	 * @param item_name
	 *            the theme item name to load
	 * @param item
	 *            the View Item to apply the theme into
	 * @param themeType
	 *            Specify if the themed element will be a background or a
	 *            foreground item
	 */
	public static void loadThemeResource(Resources themeResources,
			String themePackage, String item_name, View item, int themeType) {
		Drawable d = null;
		if (themeResources != null) {
			int resource_id = themeResources.getIdentifier(item_name,
					"drawable", themePackage);
			if (resource_id != 0) {
				d = themeResources.getDrawable(resource_id);
				if (themeType == THEME_ITEM_FOREGROUND
						&& item instanceof ImageView) {
					// ADW remove the old drawable
					Drawable tmp = ((ImageView) item).getDrawable();
					if (tmp != null) {
						tmp.setCallback(null);
						tmp = null;
					}
					((ImageView) item).setImageDrawable(d);
				} else {
					// ADW remove the old drawable
					Drawable tmp = item.getBackground();
					if (tmp != null) {
						tmp.setCallback(null);
						tmp = null;
					}
					item.setBackgroundDrawable(d);
				}
			}
		}
	}

	public Typeface getThemeFont() {
		return themeFont;
	}

	private void changeOrientation(int type, boolean persistence) {
		if (!persistence) {
			switch (type) {
			case AlmostNexusSettingsHelper.ORIENTATION_SENSOR:
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
				break;
			case AlmostNexusSettingsHelper.ORIENTATION_PORTRAIT:
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
				break;
			case AlmostNexusSettingsHelper.ORIENTATION_LANDSCAPE:
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			default:
				break;
			}
		} else {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	void editShirtcut(ApplicationInfo info) {
		Intent edit = new Intent(Intent.ACTION_EDIT);
		edit.setClass(this, CustomShirtcutActivity.class);
		edit.putExtra(CustomShirtcutActivity.EXTRA_APPLICATIONINFO, info.id);
		startActivityForResult(edit, REQUEST_EDIT_SHIRTCUT);
	}

	private void completeEditShirtcut(Intent data) {
		if (!data.hasExtra(CustomShirtcutActivity.EXTRA_APPLICATIONINFO))
			return;
		long appInfoId = data.getLongExtra(
				CustomShirtcutActivity.EXTRA_APPLICATIONINFO, 0);
		ApplicationInfo info = LauncherModel.loadApplicationInfoById(this,
				appInfoId);
		if (info != null) {
			Bitmap bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

			Drawable icon = null;
			boolean customIcon = false;
			ShortcutIconResource iconResource = null;

			if (bitmap != null) {
				icon = new FastBitmapDrawable(Utilities.createBitmapThumbnail(
						bitmap, this));
				customIcon = true;
			} else {
				Parcelable extra = data
						.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
				if (extra != null && extra instanceof ShortcutIconResource) {
					try {
						iconResource = (ShortcutIconResource) extra;
						final PackageManager packageManager = getPackageManager();
						Resources resources = packageManager
								.getResourcesForApplication(iconResource.packageName);
						final int id = resources.getIdentifier(
								iconResource.resourceName, null, null);
						icon = resources.getDrawable(id);
					} catch (Exception e) {
						w(LOG_TAG, "Could not load shortcut icon: " + extra);
					}
				}
			}

			if (icon != null) {
				info.icon = icon;
				info.customIcon = customIcon;
				info.iconResource = iconResource;
				info.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
			}
			info.title = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
			info.intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
			LauncherModel.updateItemInDatabase(this, info);

			mWorkspace.updateShortcutFromApplicationInfo(info);
		}
	}

	/**
	 * ADW: Put the launcher in desktop edit mode We could be able to add,
	 * remove and reorder screens
	 */
	private void startDesktopEdit() {
		if (!mIsEditMode) {
			mIsEditMode = true;
			final Workspace workspace = mWorkspace;
			if (workspace == null)
				return;
			workspace.enableChildrenCache();
			hideDesktop(true);
			workspace.lock();
			// Load a gallery view
			final ScreensAdapter screens = new ScreensAdapter(this, workspace
					.getChildAt(0).getWidth(), workspace.getChildAt(0)
					.getHeight());
			for (int i = 0; i < workspace.getChildCount(); i++) {
				screens.addScreen((CellLayout) workspace.getChildAt(i));
			}
			mScreensEditor = mInflater.inflate(R.layout.screens_editor, null);
			final Gallery gal = (Gallery) mScreensEditor
					.findViewById(R.id.gallery_screens);
			gal.setCallbackDuringFling(false);
			gal.setClickable(false);
			gal.setAdapter(screens);
			// Setup delete button event
			View deleteButton = mScreensEditor.findViewById(R.id.delete_screen);
			deleteButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							final int screenToDelete = gal
									.getSelectedItemPosition();
							if (workspace.getChildCount() > 1) {
								AlertDialog alertDialog = new AlertDialog.Builder(
										Launcher.this).create();
								alertDialog.setTitle(getResources().getString(
										R.string.title_dialog_xml));
								alertDialog
										.setMessage(getResources()
												.getString(
														R.string.message_delete_desktop_screen));
								alertDialog.setButton(
										DialogInterface.BUTTON_POSITIVE,
										getResources().getString(
												android.R.string.ok),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												workspace
														.removeScreen(screenToDelete);
												screens.removeScreen(screenToDelete);
											}
										});
								alertDialog.setButton(
										DialogInterface.BUTTON_NEGATIVE,
										getResources().getString(
												android.R.string.cancel),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
											}
										});
								alertDialog.show();
							} else {
								Toast t = Toast
										.makeText(
												Launcher.this,
												R.string.message_cannot_delete_desktop_screen,
												Toast.LENGTH_LONG);
								t.show();
							}

						}
					});
			// Setup add buttons events
			View addLeftButton = mScreensEditor.findViewById(R.id.add_left);
			addLeftButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							if (screens.getCount() < MAX_SCREENS) {
								final int screenToAddLeft = gal
										.getSelectedItemPosition();
								CellLayout newScreen = workspace
										.addScreen(screenToAddLeft);
								screens.addScreen(newScreen, screenToAddLeft);
							} else {
								Toast t = Toast
										.makeText(
												Launcher.this,
												R.string.message_cannot_add_desktop_screen,
												Toast.LENGTH_LONG);
								t.show();
							}
						}
					});
			View addRightButton = mScreensEditor.findViewById(R.id.add_right);
			addRightButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							if (screens.getCount() < MAX_SCREENS) {
								final int screenToAddRight = gal
										.getSelectedItemPosition();
								CellLayout newScreen = workspace
										.addScreen(screenToAddRight + 1);
								screens.addScreen(newScreen,
										screenToAddRight + 1);
							} else {
								Toast t = Toast
										.makeText(
												Launcher.this,
												R.string.message_cannot_add_desktop_screen,
												Toast.LENGTH_LONG);
								t.show();
							}
						}
					});

			final View swapLeftButton = mScreensEditor
					.findViewById(R.id.swap_left);
			swapLeftButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							int currentScreen = gal.getSelectedItemPosition();
							if (currentScreen > 0) {
								workspace.swapScreens(currentScreen - 1,
										currentScreen);
								screens.swapScreens(currentScreen - 1,
										currentScreen);
							} else {
								Toast t = Toast
										.makeText(
												Launcher.this,
												R.string.message_cannot_swap_desktop_screen,
												Toast.LENGTH_LONG);
								t.show();
							}
						}
					});
			final View swapRightButton = mScreensEditor
					.findViewById(R.id.swap_right);
			swapRightButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							int currentScreen = gal.getSelectedItemPosition();
							if (currentScreen < gal.getCount() - 1) {
								workspace.swapScreens(currentScreen,
										currentScreen + 1);
								screens.swapScreens(currentScreen,
										currentScreen + 1);
							} else {
								Toast t = Toast
										.makeText(
												Launcher.this,
												R.string.message_cannot_swap_desktop_screen,
												Toast.LENGTH_LONG);
								t.show();
							}
						}
					});
			final View setDefaultButton = mScreensEditor
					.findViewById(R.id.set_default);
			setDefaultButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							int currentScreen = gal.getSelectedItemPosition();
							if (currentScreen < mWorkspace.getChildCount()) {
								mWorkspace.setDefaultScreen(currentScreen);
								AlmostNexusSettingsHelper.setDefaultScreen(
										Launcher.this, currentScreen);
								Toast t = Toast.makeText(Launcher.this,
										R.string.pref_title_default_screen,
										Toast.LENGTH_LONG);
								t.show();
							}
						}
					});
			gal.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					if (position <= 0) {
						swapLeftButton.setVisibility(View.GONE);
					} else {
						swapLeftButton.setVisibility(View.VISIBLE);
					}
					if (position < parent.getCount() - 1) {
						swapRightButton.setVisibility(View.VISIBLE);
					} else {
						swapRightButton.setVisibility(View.GONE);
					}
				}

				public void onNothingSelected(AdapterView<?> arg0) {
				}

			});
			mDragLayer.addView(mScreensEditor);
		}
	}

	private void stopDesktopEdit() {
		mIsEditMode = false;
		hideDesktop(false);
		for (int i = 0; i < mWorkspace.getChildCount(); i++) {
			mWorkspace.getChildAt(i).setDrawingCacheEnabled(false);
		}
		mWorkspace.clearChildrenCache();
		mWorkspace.unlock();
		if (mScreensEditor != null) {
			mDragLayer.removeView(mScreensEditor);
			mScreensEditor = null;
		}
	}

	protected boolean isEditMode() {
		return mIsEditMode;
	}

	protected void editWidget(final View widget) {
		if (mWorkspace != null) {
			mIsWidgetEditMode = true;
			final CellLayout screen = (CellLayout) mWorkspace
					.getChildAt(mWorkspace.getCurrentScreen());
			if (screen != null) {
				mlauncherAppWidgetInfo = (LauncherAppWidgetInfo) widget
						.getTag();

				final Intent motosize = new Intent(
						"com.motorola.blur.home.ACTION_SET_WIDGET_SIZE");
				final int appWidgetId = ((AppWidgetHostView) widget)
						.getAppWidgetId();
				final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
						.getAppWidgetInfo(appWidgetId);
				if (appWidgetInfo != null) {
					motosize.setComponent(appWidgetInfo.provider);
				}
				motosize.putExtra("appWidgetId", appWidgetId);
				motosize.putExtra("com.motorola.blur.home.EXTRA_NEW_WIDGET",
						true);
				final int minw = (mWorkspace.getWidth()
						- screen.getLeftPadding() - screen.getRightPadding())
						/ screen.getCountX();
				final int minh = (mWorkspace.getHeight()
						- screen.getBottomPadding() - screen.getTopPadding())
						/ screen.getCountY();
				mScreensEditor = new ResizeViewHandler(this);
				// Create a default HightlightView if we found no face in the
				// picture.
				int width = (mlauncherAppWidgetInfo.spanX * minw);
				int height = (mlauncherAppWidgetInfo.spanY * minh);

				final Rect screenRect = new Rect(screen.getLeftPadding(),
						screen.getTopPadding(), screen.getWidth()
								- screen.getLeftPadding()
								- screen.getRightPadding(), screen.getHeight()
								- screen.getTopPadding()
								- screen.getBottomPadding());
				final int x = mlauncherAppWidgetInfo.cellX * minw;
				final int y = mlauncherAppWidgetInfo.cellY * minh;
				final int[] spans = new int[] { 1, 1 };
				final int[] position = new int[] { 1, 1 };
				final CellLayout.LayoutParams lp = (CellLayout.LayoutParams) widget
						.getLayoutParams();
				RectF widgetRect = new RectF(x, y, x + width, y + height);
				((ResizeViewHandler) mScreensEditor).setup(null, screenRect,
						widgetRect, false, false, minw - 10, minh - 10);
				mDragLayer.addView(mScreensEditor);
				((ResizeViewHandler) mScreensEditor)
						.setOnValidateSizingRect(new ResizeViewHandler.OnSizeChangedListener() {

							@Override
							public void onTrigger(RectF r) {
								final float left = Math.round(r.left / minw)
										* minw;
								final float top = Math.round(r.top / minh)
										* minh;
								final float right = left
										+ (Math.max(
												Math.round(r.width() / (minw)),
												1) * minw);
								final float bottom = top
										+ (Math.max(
												Math.round(r.height() / (minh)),
												1) * minh);

								r.set(left, top, right, bottom);
							}
						});
				final Rect checkRect = new Rect();
				((ResizeViewHandler) mScreensEditor)
						.setOnSizeChangedListener(new ResizeViewHandler.OnSizeChangedListener() {
							@Override
							public void onTrigger(RectF r) {
								int[] tmpspans = {
										Math.max(
												Math.round(r.width() / (minw)),
												1),
										Math.max(
												Math.round(r.height() / (minh)),
												1) };
								int[] tmpposition = {
										Math.round(r.left / minw),
										Math.round(r.top / minh) };
								checkRect.set(tmpposition[0], tmpposition[1],
										tmpposition[0] + tmpspans[0],
										tmpposition[1] + tmpspans[1]);
								boolean ocupada = getModel().ocuppiedArea(
										screen.getScreen(), appWidgetId,
										checkRect);
								if (!ocupada) {
									((ResizeViewHandler) mScreensEditor)
											.setColliding(false);
								} else {
									((ResizeViewHandler) mScreensEditor)
											.setColliding(true);
								}
								if (tmpposition[0] != position[0]
										|| tmpposition[1] != position[1]
										|| tmpspans[0] != spans[0]
										|| tmpspans[1] != spans[1]) {
									if (!ocupada) {
										position[0] = tmpposition[0];
										position[1] = tmpposition[1];
										spans[0] = tmpspans[0];
										spans[1] = tmpspans[1];
										lp.cellX = position[0];
										lp.cellY = position[1];
										lp.cellHSpan = spans[0];
										lp.cellVSpan = spans[1];
										widget.setLayoutParams(lp);
										mlauncherAppWidgetInfo.cellX = lp.cellX;
										mlauncherAppWidgetInfo.cellY = lp.cellY;
										mlauncherAppWidgetInfo.spanX = lp.cellHSpan;
										mlauncherAppWidgetInfo.spanY = lp.cellVSpan;
										widget.setTag(mlauncherAppWidgetInfo);
										// send the broadcast
										motosize.putExtra("spanX", spans[0]);
										motosize.putExtra("spanY", spans[1]);
										Launcher.this.sendBroadcast(motosize);
										Log.d("RESIZEHANDLER",
												"sent resize broadcast");
									}
								}
							}
						});
			}
		}
	}

	private void stopWidgetEdit() {
		mIsWidgetEditMode = false;
		if (mlauncherAppWidgetInfo != null) {
			LauncherModel.resizeItemInDatabase(this, mlauncherAppWidgetInfo,
					LauncherSettings.Favorites.CONTAINER_DESKTOP,
					mlauncherAppWidgetInfo.screen,
					mlauncherAppWidgetInfo.cellX, mlauncherAppWidgetInfo.cellY,
					mlauncherAppWidgetInfo.spanX, mlauncherAppWidgetInfo.spanY);
			mlauncherAppWidgetInfo = null;
		}
		// Remove the resizehandler view
		if (mScreensEditor != null) {
			mDragLayer.removeView(mScreensEditor);
			mScreensEditor = null;
		}
	}

	private void navigateCatalogs(int direction) {
		final ApplicationsAdapter drawerAdapter = sModel
				.getApplicationsAdapter();
		if (drawerAdapter == null)
			return;

		List<Integer> filterIndexes = AppCatalogueFilters.getInstance()
				.getGroupsAndSpecialGroupIndexes();
		final AppCatalogueFilter filter = drawerAdapter.getCatalogueFilter();
		int currentFIndex = filter.getCurrentFilterIndex();
		// Translate to index of the list
		currentFIndex = filterIndexes.contains(currentFIndex) ? filterIndexes
				.indexOf(currentFIndex) : filterIndexes
				.indexOf(AppGroupAdapter.APP_GROUP_ALL);
		switch (direction) {
		case ACTION_CATALOG_PREV:
			currentFIndex--;
			break;
		case ACTION_CATALOG_NEXT:
			currentFIndex++;
			break;
		default:
			break;
		}

		if (currentFIndex < 0)
			currentFIndex = filterIndexes.size() - 1;
		else if (currentFIndex >= filterIndexes.size())
			currentFIndex = 0;
		// Translate to "filter index"
		currentFIndex = filterIndexes.get(currentFIndex);
		filter.setCurrentGroupIndex(currentFIndex);

		if (filter == AppCatalogueFilters.getInstance().getDrawerFilter())
			AlmostNexusSettingsHelper.setCurrentAppCatalog(Launcher.this,
					currentFIndex);
		mAllAppsGrid.updateAppGrp();
		// Uncomment this to show a toast with the name of the new group...
		/*
		 * String name = currentFIndex == AppGroupAdapter.APP_GROUP_ALL ?
		 * getString(R.string.AppGroupAll) :
		 * AppCatalogueFilters.getInstance().getGroupTitle(currentFIndex); if
		 * (name != null) { Toast t=Toast.makeText(this, name,
		 * Toast.LENGTH_SHORT); t.show(); }
		 */
	}

	private void updateCounters(View view, String packageName, int counter,
			int color) {
		Object tag = view.getTag();
		if (tag instanceof ApplicationInfo) {
			ApplicationInfo info = (ApplicationInfo) tag;
			// We need to check for ACTION_MAIN otherwise getComponent() might
			// return null for some shortcuts (for instance, for shortcuts to
			// web pages.)
			final Intent intent = info.intent;
			final ComponentName name = intent.getComponent();
			if ((info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)
					&& Intent.ACTION_MAIN.equals(intent.getAction())
					&& name != null
					&& packageName.equals(name.getPackageName())) {
				if (view instanceof CounterImageView)
					((CounterImageView) view).setCounter(counter, color);
				// else if
				view.invalidate();
				sModel.updateCounterDesktopItem(info, counter, color);
			}
		}
	}

	private void updateCountersForPackage(String packageName, int counter,
			int color) {
		if (packageName != null && packageName.length() > 0) {
			mWorkspace.updateCountersForPackage(packageName, counter, color);
			// ADW: Update ActionButtons icons
			/*
			 * updateCounters(mHandleView, packageName, counter, color);
			 * updateCounters(mLAB, packageName, counter, color);
			 * updateCounters(mRAB, packageName, counter,color);
			 * updateCounters(mLAB2, packageName, counter,color); bob
			 * updateCounters(mRAB2, packageName, counter,color);
			 */
			mMiniLauncher.updateCounters(packageName, counter, color);
			sModel.updateCounterForPackage(this, packageName, counter, color);
		}
	}

	@Override
	public void startActivity(Intent intent) {
		// TODO Auto-generated method stub
		final ComponentName name = intent.getComponent();
		if (name != null)
			updateCountersForPackage(name.getPackageName(), 0, 0);
		super.startActivity(intent);
	}

	@Override
	public void onSwipe() {
		// TODO: specify different action for each ActionButton?
		if (showDockBar)
			mDockBar.open();
	}

	private void gotoAddAppMode() {
		scroller_content.removeAllViews();

		ArrayList<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();

		int index = 0;

		int child_size;

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		if (dm.widthPixels > dm.heightPixels) // 横屏
		{
			child_size = 14;
		} else {
			child_size = 12;

			if (dm.widthPixels > 480 && dm.widthPixels < 620) {
				child_size = 10;
			}
		}

		for (int i = 0; i < sModel.mApplications.size(); i++) {
			apps.add(sModel.mApplications.get(i));
			index++;

			if (index == child_size) {
				GridView grid = (GridView) LayoutInflater.from(this).inflate(
						R.layout.app_app_all, null);
				MyGridViewAdapter myGridViewAdapter = new MyGridViewAdapter(
						this, apps);
				grid.setAdapter(myGridViewAdapter);
				grid.setOnItemClickListener(new AddAppGirdItemClickListener(
						myGridViewAdapter));
				scroller_content.addView(grid);

				apps = new ArrayList<ApplicationInfo>();

				index = 0;
			}

			if (index == child_size && i == sModel.mApplications.size() - 1) {
				break;
			}

			if (i == sModel.mApplications.size() - 1) {
				GridView grid = (GridView) LayoutInflater.from(this).inflate(
						R.layout.app_app_all, null);
				MyGridViewAdapter myGridViewAdapter = new MyGridViewAdapter(
						this, apps);
				grid.setAdapter(myGridViewAdapter);
				grid.setOnItemClickListener(new AddAppGirdItemClickListener(
						myGridViewAdapter));
				scroller_content.addView(grid);
			}
		}

		add_mode_indicator.setItems(scroller_content.getChildCount());
		add_mode_indicator.fullIndicate(0);
		scroller_content.setToScreen(0);
	}

	// 所有插件
	private void gotoAddWidgetMode() {

		scroller_content.removeAllViews();

		List<AppWidgetProviderInfo> allAppWidgetInfo = mAppWidgetManager
				.getInstalledProviders();
		AppWidgetProviderInfo calendar = new AppWidgetProviderInfo();
		calendar.label = "动机提醒日历";
		allAppWidgetInfo.add(0, calendar);
		
		AppWidgetProviderInfo widgetInfo1 = new AppWidgetProviderInfo();
		widgetInfo1.label = "我的应用";
		allAppWidgetInfo.add(0, widgetInfo1);
		
		AppWidgetProviderInfo widgetInfo2 = new AppWidgetProviderInfo();
		widgetInfo2.label = "动机云应用";
		allAppWidgetInfo.add(0, widgetInfo2);
		
		AppWidgetProviderInfo widgetInfo3 = new AppWidgetProviderInfo();
		widgetInfo3.label = "任务管理";
		allAppWidgetInfo.add(0, widgetInfo3);
		

		ArrayList<AppWidgetProviderInfo> widgets = new ArrayList<AppWidgetProviderInfo>();

		int index = 0;

		for (int i = 0; i < allAppWidgetInfo.size(); i++) {
			widgets.add(allAppWidgetInfo.get(i));
			index++;

			if (index == 6) {
				GridView grid = (GridView) LayoutInflater.from(this).inflate(
						R.layout.app_widget_all, null);
				grid.setAdapter(new MyWidgetAdapter(this, widgets));
				grid.setOnItemClickListener(new WidgetItemClickListener(widgets));
				scroller_content.addView(grid);

				widgets = new ArrayList<AppWidgetProviderInfo>();

				index = 0;
			}

			if (index == 6 && i == allAppWidgetInfo.size() - 1) {
				break;
			}

			if (i == allAppWidgetInfo.size() - 1) {
				GridView grid = (GridView) LayoutInflater.from(this).inflate(
						R.layout.app_widget_all, null);
				grid.setAdapter(new MyWidgetAdapter(this, widgets));
				grid.setOnItemClickListener(new WidgetItemClickListener(widgets));
				scroller_content.addView(grid);
			}
		}

		// 重置
		workspaceMiniMode = 0;

		btn_swap_app.setBackgroundResource(R.drawable.left_selected);
		btn_swap_app.setTextColor(getResources().getColor(
				R.color.add_item_text_selected));
		btn_swap_widget.setBackgroundResource(R.drawable.right_normal);
		btn_swap_widget.setTextColor(getResources().getColor(
				R.color.add_item_text_normal));
		scroller_content.setToScreen(0);

		add_mode_indicator.setItems(scroller_content.getChildCount());
		add_mode_indicator.fullIndicate(0);

	}

	void pickAppWidget() {
		// new
		int appWidgetId = Launcher.this.mAppWidgetHost.allocateAppWidgetId();

		Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
		ArrayList<Bundle> customExtras = new ArrayList<Bundle>();

		// add the search widget
		// AppWidgetProviderInfo info = new AppWidgetProviderInfo();
		// info.provider = new ComponentName(getPackageName(), "XXX.YYY");
		// info.label = getString(R.string.group_search);
		// info.icon = R.drawable.ic_search_widget;
		// customInfo.add(info);

		// Bundle b = new Bundle();
		// b.putString(EXTRA_CUSTOM_WIDGET, SEARCH_WIDGET);
		// customExtras.add(b);

		// add Remind calendar widget
		AppWidgetProviderInfo info1 = new AppWidgetProviderInfo();
		info1.provider = new ComponentName(getPackageName(), "XXX.YYY");
		info1.label = "动机提醒日历";
		info1.icon = R.drawable.remind_icon;
		customInfo.add(info1);
		
		Bundle b1 = new Bundle();
		b1.putString(EXTRA_CUSTOM_WIDGET, REMIND_CALENDAR_WIDGET);
		customExtras.add(b1);
		
		AppWidgetProviderInfo info2 = new AppWidgetProviderInfo();
		info2.provider = new ComponentName(getPackageName(), "XXX.YYY");
		info2.label = "任务管理";
		info2.icon = R.drawable.task_manager_widget_icon;
		customInfo.add(info2);
		Bundle b2 = new Bundle();
		b2.putString(EXTRA_CUSTOM_WIDGET, SHORTCUT_TASK_MANAGER_WIDGET);
		customExtras.add(b2);

		AppWidgetProviderInfo info3 = new AppWidgetProviderInfo();
		info3.provider = new ComponentName(getPackageName(), "XXX.YYY");
		info3.label = "动机云应用";
		info3.icon = R.drawable.cloud_app_widget_icon;
		customInfo.add(info3);
		Bundle b3 = new Bundle();
		b3.putString(EXTRA_CUSTOM_WIDGET, SHORTCUT_CLOUD_APP_WIDGET);
		customExtras.add(b3);

		AppWidgetProviderInfo info4 = new AppWidgetProviderInfo();
		info4.provider = new ComponentName(getPackageName(), "XXX.YYY");
		info4.label = "我的应用";
		info4.icon = R.drawable.my_app_widget_icon;
		customInfo.add(info4);
		Bundle b4 = new Bundle();
		b4.putString(EXTRA_CUSTOM_WIDGET, SHORTCUT_MY_APP_WIDGET);
		customExtras.add(b4);

		pickIntent.putParcelableArrayListExtra(
				AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
		pickIntent.putParcelableArrayListExtra(
				AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);

		// start the pick activity
		startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET_2);
	}

	class WidgetItemClickListener implements OnItemClickListener {

		List<AppWidgetProviderInfo> allAppWidgetInfo;

		public WidgetItemClickListener(
				List<AppWidgetProviderInfo> allAppWidgetInfo) {
			super();
			this.allAppWidgetInfo = allAppWidgetInfo;
		}

		public void onItemClick(AdapterView<?> arg0, View view, int location,
				long arg3) {
			workspaceMini.addWidgetIn(view, allAppWidgetInfo.get(location));
		}
	}

	public void bindWorkspaceMini() {

		clearTempBitmap();
		int size = mWorkspace.getChildCount();

		mMiniLauncher.setDrawingCacheEnabled(true);
		mMiniLauncher.destroyDrawingCache();
		mMiniLauncher.buildDrawingCache();
		Bitmap mini_bitmap = mMiniLauncher.getDrawingCache();

		workspaceMini.init(size, mWorkspace.getCurrentScreen(), mini_bitmap);

		final WorkspaceMini workspace = workspaceMini;
		final boolean desktopLocked = false;

		int s = mWorkspace.getChildCount();

		int unit_width = getResources().getDimensionPixelSize(
				R.dimen.mini_workspace_cell_item_width);
		int unit_height = getResources().getDimensionPixelSize(
				R.dimen.mini_workspace_cell_item_height);

		for (int j = 0; j < s; j++) {
			CellLayout c = (CellLayout) mWorkspace.getChildAt(j);

			// System.out.println(" 第 " + j + " 屏   共有  : --->" +
			// c.getChildCount() +" 个元素");
			for (int k = 0; k < c.getChildCount(); k++) {
				View v = c.getChildAt(k);
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.no_bg));
				v.setDrawingCacheEnabled(true);
				v.buildDrawingCache();

				Bitmap b = Bitmap.createBitmap(v.getDrawingCache());

				temp_bitmaps.add(b);
				Object o = v.getTag();
				ItemInfo itemInfo = (ItemInfo) o;

				int src_width = b.getWidth();
				int src_height = b.getHeight();

				// System.out.println(" src_width ---> " + src_width);
				// System.out.println(" src_height ---> " +src_height);
				//
				// System.out.println("itemInfo.screen " + itemInfo.screen);

				// System.out.println("  itemInfo.spanX --->   " +
				// itemInfo.spanX);
				// System.out.println("  itemInfo.spanY --->   " +
				// itemInfo.spanY);
				//
				// System.out.println(" unit_width --->   " + unit_width);
				// System.out.println(" unit_height --->   " + unit_height);
				//
				// System.out.println("  src_width  -->" + src_width);
				// System.out.println("  src_height  -->" + src_height);
				//
				// System.out.println("  target_width  -->" + target_width);
				// System.out.println("  target_height  -->" + target_height);

				// Bitmap bitmap = b.createScaledBitmap(b,target_width,
				// target_height, false);
				// //
				// ImageView img = new ImageView(this);
				// img.setPadding(5, 5, 5, 5);
				// img.setScaleType(ScaleType.CENTER_INSIDE);
				// // img.setImageBitmap(b);
				// img.setBackgroundDrawable(new BitmapDrawable(b));
				// img.setTag(v);
				// img.setClickable(true);
				// img.setLongClickable(true);
				// img.setFocusable(true);
				// img.setOnLongClickListener(new OnLongClickListener() {
				//
				// @Override
				// public boolean onLongClick(View v) {
				// System.out.println("  img  touch ---> ");
				// return true;
				// }
				// });

				ImageView img = new ImageView(this);
				// img.setPadding(5, 5, 5, 5);
				// img.setScaleType(ScaleType.CENTER_INSIDE);
				// img.setImageBitmap(b);
				img.setBackgroundDrawable(new BitmapDrawable(b));
				img.setTag(v);
				img.setLongClickable(true);
				img.setOnLongClickListener(workspace); // 长按监听

				// img.setOnClickListener(new OnClickListener() {
				//
				// @Override
				// public void onClick(View v) {
				// System.out.println("  img  touch ---> ");
				// }
				// });

				// img.setOnTouchListener(new OnTouchListener() {
				//
				// @Override
				// public boolean onTouch(View v, MotionEvent event) {
				// // TODO Auto-generated method stub
				// System.out.println(" event.action " + event.getAction());
				// return false;
				// }
				// });

				workspace.addInScreen(img, itemInfo.screen, itemInfo.cellX,
						itemInfo.cellY, itemInfo.spanX, itemInfo.spanY,
						!desktopLocked);
			}
		}

		add_mode_indicator.setItems(workspace.getChildCount());
	}

	class AddAppGirdItemClickListener implements OnItemClickListener {

		MyGridViewAdapter myGridViewAdapter;

		public AddAppGirdItemClickListener(MyGridViewAdapter myGridViewAdapter) {
			super();
			this.myGridViewAdapter = myGridViewAdapter;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			workspaceMini.addAppIn(view,
					(ApplicationInfo) myGridViewAdapter.getItem(position));
		}
	}

	public void checkUpdate(final boolean isOnclick) {

		if (!isOnclick) {

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {

						if (AndroidUtils.isNetworkAvailable(Launcher.this)) {
							ArrayList<String> strings = AndroidUtils
									.checkAppUpdate(Launcher.this);
							if (!(strings.size() == 0)) {
								String download_url = strings.get(0);
								if (download_url != null
										&& !"".equals(download_url)) {
									update_url = download_url;
									myHandler.sendEmptyMessage(7);
								}

							}

						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} else {
			// 手动点击更新

			if (AndroidUtils.isNetworkAvailable(Launcher.this)) {
				new Thread(new Runnable() {
					public void run() {
						try {
							ArrayList<String> strings = AndroidUtils
									.checkAppUpdate(Launcher.this);
							if (!(strings.size() == 0)) {
								String download_url = strings.get(0);
								if (download_url != null
										&& !"".equals(download_url)) {
									update_url = download_url;
									version_info = strings.get(1);
									version_date = strings.get(2);
									myHandler.sendEmptyMessage(6);
								}
							} else {
								myHandler.sendEmptyMessage(9);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();

			} else {

				myHandler.sendEmptyMessage(10);
			}
		}
	}

	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			try {

				switch (msg.what) {
				case 6: //
					if (!UpdateVersionService.IS_DOWNLOAD) {
						if (pWindow != null && pWindow.isShowing()) {
							pWindow.dismiss();
						}
						Dialog dialog = new AlertDialog.Builder(Launcher.this)
								.setIcon(android.R.drawable.ic_dialog_info)
								.setTitle("发现新版本")
								.setPositiveButton("马上更新",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {

												downloadUpdatedApp();
											}
										}).create();

						dialog.show();
					} else {
						Toast.makeText(Launcher.this, "动机桌面正在后台下载，请稍后",
								Toast.LENGTH_SHORT).show();
					}
					break;
				case 7: // 如果有版本更新
					if (!UpdateVersionService.IS_DOWNLOAD) {
						downloadUpdatedApp();
					} else {
						Toast.makeText(Launcher.this, "动机桌面正在后台下载，请稍后",
								Toast.LENGTH_SHORT).show();
					}
					break;

				case 9: // 没有版本更新
					Toast.makeText(Launcher.this, "已是最新版本", Toast.LENGTH_LONG)
							.show();
					break;
				case 10: // 没有版本更新
					Toast.makeText(Launcher.this, "网络异常，请检查网络",
							Toast.LENGTH_LONG).show();
					break;
				case 11: // 版本更新出现问题
					/*
					 * if (updateDialog.isShowing()) { updateDialog.dismiss(); }
					 * Toast.makeText(Launcher.this, "下载失败，请重试",
					 * Toast.LENGTH_LONG).show(); break;
					 */
				default:
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void downloadUpdatedApp() {
		if (AndroidUtils.isSdcardExists()) // sd卡是否可用
		{
			Intent updateIntent = new Intent(Launcher.this,
					UpdateVersionService.class);
			updateIntent.putExtra("update_url", update_url);

			startService(updateIntent);
		} else {
			Toast.makeText(this, "sd card faild", Toast.LENGTH_SHORT).show();
		}
	}

	public void clearTempBitmap() {
		for (Bitmap b : temp_bitmaps) {
			if (b != null && !b.isRecycled()) {
				b.recycle();
				b = null;
			}
		}
		temp_bitmaps.clear();
		System.gc();
	}

	public int getMode() {
		if (stateMod == 3) {
			return saveMob;
		}

		return stateMod;
	}
}
