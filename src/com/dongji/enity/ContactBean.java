package com.dongji.enity;

import java.util.ArrayList;
import java.util.List;

public class ContactBean {

	Long contact_id;
	
	String photo_id;
	
	String nick;
	
	String number;
	
	String sork_key;
	
	String name_pinyin; //名字的全拼音
	
	String name_pinyin_cap;//名字的拼音首写字�?
	
	String name_letter;
	
	byte[] photo;
	
	List<String> numberlist = new ArrayList<String>();
	
	public List<String> getNumberlist() {
		return numberlist;
	}

	public void setNumberlist(List<String> numberlist) {
		this.numberlist = numberlist;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public String getName_letter() {
		return name_letter;
	}

	public void setName_letter(String name_letter) {
		this.name_letter = name_letter;
	}

	public String getName_pinyin() {
		return name_pinyin;
	}

	public void setName_pinyin(String name_pinyin) {
		this.name_pinyin = name_pinyin;
	}

	String lookup_key;
	
	public String getSork_key() {
		return sork_key;
	}

	public void setSork_key(String sork_key) {
		this.sork_key = sork_key;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Long getContact_id() {
		return contact_id;
	}

	public void setContact_id(Long contact_id) {
		this.contact_id = contact_id;
	}

	public String getPhoto_id() {
		return photo_id;
	}

	public void setPhoto_id(String photo_id) {
		this.photo_id = photo_id;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getLookup_key() {
		return lookup_key;
	}

	public void setLookup_key(String lookup_key) {
		this.lookup_key = lookup_key;
	}

	public String getName_pinyin_cap() {
		return name_pinyin_cap;
	}

	public void setName_pinyin_cap(String name_pinyin_cap) {
		this.name_pinyin_cap = name_pinyin_cap;
	}
}
