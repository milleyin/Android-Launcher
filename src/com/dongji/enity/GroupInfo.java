package com.dongji.enity;

public class GroupInfo {
	private long group_id;//组id
	private String group_name;//组名�?
	
	private String phone_name;
	private String phone_number;
	private boolean is_group;
	private boolean is_child;
	private String  person_id;//联系人id
	
	private String area; //归属�?
	
	public long getGroup_id() {
		return group_id;
	}
	public void setGroup_id(long group_id) {
		this.group_id = group_id;
	}
	public String getGroup_name() {
		return group_name;
	}
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	public String getPhone_name() {
		return phone_name;
	}
	public void setPhone_name(String phone_name) {
		this.phone_name = phone_name;
	}
	public String getPhone_number() {
		return phone_number;
	}
	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}
	public boolean isIs_group() {
		return is_group;
	}
	public void setIs_group(boolean is_group) {
		this.is_group = is_group;
	}
	public boolean isIs_child() {
		return is_child;
	}
	public void setIs_child(boolean is_child) {
		this.is_child = is_child;
	}
	public String getPerson_id() {
		return person_id;
	}
	public void setPerson_id(String person_id) {
		this.person_id = person_id;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
    
	
}
