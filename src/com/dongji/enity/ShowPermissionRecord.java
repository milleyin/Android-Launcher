package com.dongji.enity;

public class ShowPermissionRecord {

	private int _id;
	private String pkgName;
	private int state;
	public ShowPermissionRecord(int _id, String pkgName, int state) {
		this._id = _id;
		this.pkgName = pkgName;
		this.state = state;
	}
	
	public ShowPermissionRecord(String pkgName, int state) {
		this.pkgName = pkgName;
		this.state = state;
	}

	public ShowPermissionRecord() {}
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String getPkgName() {
		return pkgName;
	}
	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
}
