package com.dongji.enity;

public class PowerbootRecord {

	private int _id;
	private String pkgName;
	private int state;
	private int uid;
	
	public PowerbootRecord() {}
	
	public PowerbootRecord(int _id, String pkgName, int state, int uid) {
		this._id = _id;
		this.pkgName = pkgName;
		this.state = state;
		this.uid = uid;
	}

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

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}
	
}
