package com.dongji.desktopswitch;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.dongji.launcher.R;

public class Adapter_lv_item_scrollSetting extends BaseAdapter {
	public static final String SCROLLSETTING_IMAGE = "scrollsetting_image";
	public static final String SCROLLSETTING_SEEK = "scrollsetting_seek";

	public static int progress_bright = 0;
	public static int progress_music = 0;
	public static int progress_ring = 0;
	public static int int_sleep = 0;

	private Context context;
	private ArrayList<HashMap<String, Object>> data;

	public Adapter_lv_item_scrollSetting(Context context,
			ArrayList<HashMap<String, Object>> data) {
		this.context = context;
		this.data = data;
		progress_bright = Integer.valueOf(data.get(0).get(SCROLLSETTING_SEEK)
				.toString());
		progress_music = Integer.valueOf(data.get(1).get(SCROLLSETTING_SEEK)
				.toString());
		progress_ring = Integer.valueOf(data.get(2).get(SCROLLSETTING_SEEK)
				.toString());
		int_sleep = Integer.valueOf(data.get(3).get(SCROLLSETTING_SEEK)
				.toString());
	}

	@Override
	public int getCount() {
		return null == data ? 0 : data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(
				R.layout.listview_item_scrollsetting, null);
		ImageView iv_scrollSetting = (ImageView) convertView
				.findViewById(R.id.iv_scrollSetting);
		SeekBar seekBar_scrollSetting = (SeekBar) convertView
				.findViewById(R.id.seekBar_scrollSetting);
		TextView tv_scrollSetting_seek = (TextView) convertView
				.findViewById(R.id.tv_scrollSetting_seek);

		iv_scrollSetting.setImageResource(Integer.valueOf(data.get(position)
				.get(SCROLLSETTING_IMAGE).toString()));
		seekBar_scrollSetting.setMax(100);
		seekBar_scrollSetting.setProgress(Integer.valueOf(data.get(position)
				.get(SCROLLSETTING_SEEK).toString()));

		tv_scrollSetting_seek
				.setText(seekBar_scrollSetting.getProgress() + "%");
		seekBar_scrollSetting.setMax(100);
		switch (position) {
		case 3:
			seekBar_scrollSetting.setMax(300);
			seekBar_scrollSetting.setProgress(Integer.valueOf(data
					.get(position).get(SCROLLSETTING_SEEK).toString()));
			iv_scrollSetting.setImageResource(R.drawable.setting_power_sleep);
			tv_scrollSetting_seek.setText(seekBar_scrollSetting.getProgress()
					+ "s");
			break;
		}
		seekBar_scrollSetting
				.setOnSeekBarChangeListener(new MyScrollSeekBarChangeListener(
						position, tv_scrollSetting_seek));
		return convertView;
	}

	private class MyScrollSeekBarChangeListener implements
			OnSeekBarChangeListener {
		private int position;
		private TextView textView;

		public MyScrollSeekBarChangeListener(int position, TextView textView) {
			this.position = position;
			this.textView = textView;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			switch (position) {
			case 0:
				progress_bright = progress;
				textView.setText(progress + "%");
				break;
			case 1:
				progress_music = progress;
				textView.setText(progress + "%");
				break;
			case 2:
				progress_ring = progress;
				textView.setText(progress + "%");
				break;
			case 3:
				int_sleep = progress;
				textView.setText(progress + "s");
				break;
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	}
}
