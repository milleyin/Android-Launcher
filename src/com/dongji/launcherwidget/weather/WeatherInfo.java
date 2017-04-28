package com.dongji.launcherwidget.weather;

import java.io.Serializable;

public class WeatherInfo implements Serializable {

	String city;
	String date_y;
	String temp1;
	String weather;
	String wind1;
	String img_title;
	int img;
	
	String code;
	
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getImg_title() {
		return img_title;
	}
	public void setImg_title(String img_title) {
		this.img_title = img_title;
	}
	public int getImg() {
		return img;
	}
	public void setImg(int img) {
		this.img = img;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getDate_y() {
		return date_y;
	}
	public void setDate_y(String date_y) {
		this.date_y = date_y;
	}
	public String getTemp1() {
		return temp1;
	}
	public void setTemp1(String temp1) {
		this.temp1 = temp1;
	}
	public String getWeather() {
		return weather;
	}
	public void setWeather(String weather) {
		this.weather = weather;
	}
	public String getWind1() {
		return wind1;
	}
	public void setWind1(String wind1) {
		this.wind1 = wind1;
	}

}
