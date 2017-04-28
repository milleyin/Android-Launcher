package com.dongji.adapter;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.dongji.enity.ContactBean;
import com.dongji.launcher.R;


/**
 * 多选
 * @author Administrator
 *
 */
public class RemindContactsMutilAdapter extends BaseAdapter  implements SectionIndexer {

	Context context;
	private String[] mNicks;
	
	List<ContactBean> contacts = new ArrayList<ContactBean>();
	
//	public CheckBox cb;
//	int position = -1;
	
	boolean[] itemStatus ;
	
	public RemindContactsMutilAdapter(Context context,List<ContactBean> contacts)
	{
		this.context = context;
		this.contacts = contacts;
		getCallerName();
		
		itemStatus = new boolean[contacts.size()];
	}
	
	private void getCallerName() {
		Cursor c = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
				new String[] { ContactsContract.Contacts.DISPLAY_NAME }, null,null, null);
		String[] nicks = new String[c.getCount()];
		if (c.moveToFirst()) {
			do {
				String CACHED_NAME = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				nicks[c.getPosition()] = CACHED_NAME;
			} while (c.moveToNext());
		}
		
		mNicks = nicks;
		c.close();
	}
	
	@Override
	public int getCount() {
		return contacts.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		convertView = LayoutInflater.from(context).inflate(R.layout.remind_contact_items, null);

		final ViewHolder holder = new ViewHolder();

		holder.tvCatalog = (TextView) convertView.findViewById(R.id.contactitem_catalog);
		holder.ivAvatar = (ImageView) convertView.findViewById(R.id.contactitem_avatar_iv);
		holder.tvNick = (TextView) convertView.findViewById(R.id.contactitem_nick);
		holder.number = (TextView) convertView.findViewById(R.id.number);
		holder.area = (TextView) convertView.findViewById(R.id.area);
		
		holder.cb = (CheckBox) convertView.findViewById(R.id.cb);
		holder.cb.setOnCheckedChangeListener(new MyCheckBoxChangedListener(position));
		
		if (itemStatus[position] == true) {
			holder.cb.setChecked(true);
		} else {
			holder.cb.setChecked(false);
		}
		
		ContactBean contactBean = contacts.get(position);
		
		holder.cb.setTag(contactBean.getContact_id()+":"+contactBean.getNick());
		
		Long contact_id = contactBean.getContact_id();

		String photo_id = contactBean.getPhoto_id();
		if (photo_id != null) {
			Cursor photo = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,new String[] { ContactsContract.Contacts.Data.DATA15 },
					"ContactsContract.Data._ID = " + photo_id, null, null);
			if (photo.moveToNext()) {
				byte[] photoicon = photo.getBlob(photo.getColumnIndex(ContactsContract.Contacts.Data.DATA15));
				ByteArrayInputStream inputStream = new ByteArrayInputStream(photoicon);
				Bitmap contactPhoto = BitmapFactory.decodeStream(inputStream);
				if (contactPhoto == null)
					holder.ivAvatar.setImageResource(R.drawable.default_contact);
				else
					holder.ivAvatar.setImageBitmap(contactPhoto);
			}
			photo.close();
		} else {
			holder.ivAvatar.setImageResource(R.drawable.default_contact);
		}
		
		Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				        new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ " = " + contact_id, null, null);
		if (phones.moveToNext()) {
			String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			contactBean.setNumber(phone);
			holder.number.setText(phone);
		}
		phones.close();
		
//		holder.btn_select.setTag(String.valueOf(contactBean.getContact_id()) + ":" + contactBean.getNick()+":" + contactBean.getNumber());
		
		String nick = contactBean.getNick();
		
//		String nickName = mNicks[position];
//		String catalog = converterToFirstSpell(nickName).substring(0, 1);
//		if (position == 0) {
//			holder.tvCatalog.setText(catalog);
//		} else {
//			String lastCatalog = converterToFirstSpell(mNicks[position - 1]).substring(0, 1);
//			if (catalog.equals(lastCatalog)) {
//			} else {
//				holder.tvCatalog.setText(catalog);
//			}
//		}
		holder.tvNick.setText(nick);
		
//		holder.area.setText(MainActivity.CheckNumberArea(holder.number.getText().toString()));
		
		return convertView;
	}

	@Override
	public int getPositionForSection(int section) {
//		for (int i = 0; i < mNicks.length; i++) {
//			String l = converterToFirstSpell(mNicks[i]).substring(0, 1);
//			char firstChar = l.toUpperCase().charAt(0);
//			if (firstChar == section) {
//				return i;
//			}
//		}
		return -1;
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		return null;
	}

	
	class ViewHolder {
		TextView tvCatalog;// 目录
		ImageView ivAvatar;// 头像
		TextView tvNick;// 昵称
		TextView number;
		TextView area;
		
//		Button btn_select;
		CheckBox cb;
	}
	
	public int[] getSelectedItemIndexes() {

		if (itemStatus == null || itemStatus.length == 0) {
			return new int[0];
		} else {
			int size = itemStatus.length;
			int counter = 0;
			for (int i = 0; i < size; i++) {
				if (itemStatus[i] == true)
					++counter;
			}
			int[] selectedIndexes = new int[counter];
			int index = 0;
			for (int i = 0; i < size; i++) {
				if (itemStatus[i] == true)
					selectedIndexes[index++] = i;
			}
			return selectedIndexes;
		}
	};
	
	class MyCheckBoxChangedListener implements OnCheckedChangeListener {
		int position;

		MyCheckBoxChangedListener(int position) {
			this.position = position;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			System.out.println("" + position + "Checked?:" + isChecked);
			if (isChecked)
				itemStatus[position] = true;
			else
				itemStatus[position] = false;
		}
	}
	
//	/**
//	 * 汉字转换位汉语拼音首字母，英文字符不变
//	 * 
//	 * @param chines
//	 *            汉字
//	 * @return 拼音
//	 */
//	public static String converterToFirstSpell(String chines) {
//		String pinyinName = "";
//		char[] nameChar = chines.toCharArray();
//		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
//		defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
//		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//		for (int i = 0; i < nameChar.length; i++) {
//			if (nameChar[i] > 128) {
//				try {
//					pinyinName += PinyinHelper.toHanyuPinyinStringArray(
//							nameChar[i], defaultFormat)[0].charAt(0);
//				}
////				catch (BadHanyuPinyinOutputFormatCombination e) {
////					e.printStackTrace();
////				}
//				catch(Exception ex){
//					ex.printStackTrace();
//				}
//			} else {
//				pinyinName += nameChar[i];
//			}
//		}
//		return pinyinName;
//	}
}
