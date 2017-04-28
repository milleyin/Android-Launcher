package com.dongji.enity;

public class NetrafficRecord {

	private int _id;
	private String pkg_name;
	private long flow_value;
	
	public NetrafficRecord() {}

	public NetrafficRecord(int _id, String pkg_name, long flow_value) {
		this._id = _id;
		this.pkg_name = pkg_name;
		this.flow_value = flow_value;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getPkg_name() {
		return pkg_name;
	}

	public void setPkg_name(String pkg_name) {
		this.pkg_name = pkg_name;
	}

	public long getFlow_value() {
		return flow_value;
	}

	public void setFlow_value(long flow_value) {
		this.flow_value = flow_value;
	}
	
}
