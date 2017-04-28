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


import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dongji.launcher.R;

/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends BubbleTextView implements DropTarget {
    UserFolderInfo mInfo;
    private static Launcher mLauncher;
    private Drawable mCloseIcon;
    private Drawable mOpenIcon;

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FolderIcon(Context context) {
        super(context);
    }

    static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            UserFolderInfo folderInfo) {

        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);
        //TODO:ADW Load icon from theme/iconpack
        icon.mLauncher = launcher;
        
        Drawable dclosed;
        Drawable dopen;
        final Resources resources = launcher.getResources();
        String themePackage=AlmostNexusSettingsHelper.getThemePackageName(launcher, Launcher.THEME_DEFAULT);
        if(themePackage.equals(Launcher.THEME_DEFAULT)){
        	dclosed = resources.getDrawable(R.drawable.ic_launcher_folder);
        	dopen = resources.getDrawable(R.drawable.ic_launcher_folder_open);
        }else{
        	Drawable tmpIcon1 = loadFolderFromTheme(launcher, launcher.getPackageManager(), themePackage,"ic_launcher_folder");
        	if(tmpIcon1==null){
        		dclosed = resources.getDrawable(R.drawable.ic_launcher_folder);
        	}else{
        		dclosed = tmpIcon1;
        	}
        	Drawable tmpIcon2 = loadFolderFromTheme(launcher, launcher.getPackageManager(), themePackage,"ic_launcher_folder_open");
        	if(tmpIcon2==null){
        		dopen = resources.getDrawable(R.drawable.ic_launcher_folder_open);
        	}else{
        		dopen = tmpIcon2;
        	}
        }
//        icon.mCloseIcon=Utilities.createIconThumbnail(dclosed, launcher);
        
        icon.mCloseIcon= new BitmapDrawable(creaetCloseIcon(folderInfo)) ;
        icon.mOpenIcon=dopen;
        
        /*final Resources resources = launcher.getResources();
        Drawable d = resources.getDrawable(R.drawable.ic_launcher_folder);
        d = Utilities.createIconThumbnail(d, launcher);
        icon.mCloseIcon = d;
        icon.mOpenIcon = resources.getDrawable(R.drawable.ic_launcher_folder_open);*/
        icon.setCompoundDrawablesWithIntrinsicBounds(null, icon.mCloseIcon, null, null);
        if(!AlmostNexusSettingsHelper.getUIHideLabels(launcher))icon.setText(folderInfo.title);
        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        
        return icon;
    }

    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;
        final int itemType = item.itemType;
        return (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)
                && item.container != mInfo.id;
    }

    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo, Rect recycle) {
        return null;
    }

    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
        final ApplicationInfo item = (ApplicationInfo) dragInfo;
        // TODO: update open folder that is looking at this data
        mInfo.add(item);
        LauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, 0, 0, 0);
        
        mCloseIcon = new BitmapDrawable(creaetCloseIcon(mInfo));
        setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null, null);
    }

    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
        setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null, null);
    }

    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
    }

    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
        setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null, null);
    }
    /**
     * ADW: Load the floder icon drawables from the theme
     * @param context
     * @param manager
     * @param themePackage
     * @param resourceName
     * @return
     */
    static Drawable loadFolderFromTheme(Context context,
			PackageManager manager, String themePackage, String resourceName) {
		Drawable icon=null;
    	Resources themeResources=null;
    	try {
			themeResources=manager.getResourcesForApplication(themePackage);
		} catch (NameNotFoundException e) {
			//e.printStackTrace();
		}
		if(themeResources!=null){
			int resource_id=themeResources.getIdentifier (resourceName, "drawable", themePackage);
			if(resource_id!=0){
				icon=themeResources.getDrawable(resource_id);
			}
		}
		return icon;
	}
    
    
    static Bitmap creaetCloseIcon(UserFolderInfo folderInfo)
    {
    	Resources resources = mLauncher.getResources();
    	 int sIconWidth=60;
//         sIconWidth = (int) resources.getDimension(android.R.dimen.app_icon_size);
         
         int itemWidth = sIconWidth/2-2;
         
         Bitmap gb= BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.floder_bg);
         Bitmap bitmap = Bitmap.createScaledBitmap(gb, sIconWidth, sIconWidth, false);
         
//         Bitmap bitmap = Bitmap.createBitmap(sIconWidth, sIconWidth, Bitmap.Config.RGB_565);
         
         Canvas canvas = new Canvas(bitmap);
         
         gb.recycle();
         gb = null;
         
         
         for(int i =0;i<folderInfo.contents.size();i++)
         {
        	 Bitmap b = null;
        	if(folderInfo.contents.get(i).icon instanceof FastBitmapDrawable)
        	{
        		b = ((FastBitmapDrawable)folderInfo.contents.get(i).icon).getBitmap();
        	}else{
        		b = ((BitmapDrawable)folderInfo.contents.get(i).icon).getBitmap();
        	}
         	
         	Bitmap bb = b.createScaledBitmap(b, itemWidth, itemWidth, true);
         	if(i==0)
         	{
             	canvas.drawBitmap(bb, 2, 2, null);
         	}
         	if(i==1)
         	{
             	canvas.drawBitmap(bb, sIconWidth/2, 2, null);
         	}
         	if(i==2)
         	{
             	canvas.drawBitmap(bb, 2, sIconWidth/2, null);
         	}
         	if(i==3)
         	{
             	canvas.drawBitmap(bb, sIconWidth/2, sIconWidth/2, null);
         	}
         	
         	if(i>3)
         	{
         		break;
         	}
         }
         
         return bitmap;
    }
    
    public void refreshAfterDropOut(ApplicationInfo app)
    {
    	 mInfo.contents.remove(app);
    	 mCloseIcon = new BitmapDrawable(creaetCloseIcon(mInfo));
         setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null, null);
    }
    
    public void refresh()
    {
    	 mCloseIcon = new BitmapDrawable(creaetCloseIcon(mInfo));
         setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null, null);
    }
}
