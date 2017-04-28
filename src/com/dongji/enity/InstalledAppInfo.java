package com.dongji.enity;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

public class InstalledAppInfo {

	Drawable icon;
	String name;
	String versionName;
	int versionCode;
	String className;
	String pkgName;
	String size;
	int uid;
	String processName;
	long cacheVal;
	ApplicationInfo appInfo;
	int flag;//1代表系统应用，3代表第三方应用
	
	int usageMemory;

	public InstalledAppInfo() {}

	public InstalledAppInfo(Drawable icon, String name, String versionName, int versionCode, String className,
			String pkgName, String size, int uid, String processName, int cacheVal, ApplicationInfo appInfo) {
		super();
		this.icon = icon;
		this.name = name;
		this.versionName = versionName;
		this.versionCode = versionCode;
		this.className = className;
		this.pkgName = pkgName;
		this.size = size;
		this.uid = uid;
		this.processName = processName;
		this.cacheVal = cacheVal;
		this.appInfo = appInfo;
	}

	public Drawable getIcon() {
		return icon;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getPkgName() {
		return pkgName;
	}

	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public ApplicationInfo getAppInfo() {
		return appInfo;
	}

	public void setAppInfo(ApplicationInfo appInfo) {
		this.appInfo = appInfo;
	}

	public int getUsageMemory() {
		return usageMemory;
	}

	public void setUsageMemory(int usageMemory) {
		this.usageMemory = usageMemory;
	}

	public long getCacheVal() {
		return cacheVal;
	}

	public void setCacheVal(long cacheVal) {
		this.cacheVal = cacheVal;
	}

	/**
	 * 返回应用类型为系统或是第三方应用.
	 * @return 1代表系统应用，3代表第三方应用
	 */
	public int getFlag() {
		return flag;
	}

	/**
	 * 设置应用类型为系统还是第三方应用.
	 * @param flag 1代表系统应用，3代表第三方应用
	 */
	public void setFlag(int flag) {
		this.flag = flag;
	}

	@Override
	public String toString() {
		return "InstalledAppInfo [icon=" + icon + ", name=" + name
				+ ", versionName=" + versionName + ", versionCode="
				+ versionCode + ", className=" + className + ", pkgName="
				+ pkgName + ", size=" + size + ", uid=" + uid
				+ ", processName=" + processName + ", appInfo=" + appInfo + "]";
	}

}
