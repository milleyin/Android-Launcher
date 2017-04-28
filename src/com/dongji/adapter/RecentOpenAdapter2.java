package com.dongji.adapter;

import java.util.ArrayList;
import java.util.List;

import org.adw.launcher.ApplicationInfo;
import org.adw.launcher.BubbleTextView;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.dongji.launcher.R;
import com.dongji.sqlite.DrawerDatabase;

public class RecentOpenAdapter2 extends BaseAdapter implements OnItemClickListener {
	private List<ApplicationInfo> list;
	private Context context;
	private boolean isEdit;
	private List<Boolean> selectList;
	
	public RecentOpenAdapter2(Context context, List<ApplicationInfo> list) {
		this.context=context;
		this.list=list;
		if(list!=null) {
			selectList=new ArrayList<Boolean>(list.size());
			for(int i=0;i<list.size();i++) {
				selectList.add(false);
			}
		}
	}
	
	public RecentOpenAdapter2(Context context, List<ApplicationInfo> list, boolean checkHide) {
		this.context=context;
		this.list=list;
		if(list!=null) {
			selectList=new ArrayList<Boolean>(list.size());
			for(int i=0;i<list.size();i++) {
				ApplicationInfo info=list.get(i);
				int visible=info.getSortEntity().visible;
				if(visible==0) {
					selectList.add(false);
				}else {
					selectList.add(true);
				}
			}
		}
	}
	
	public void setEditStatus(boolean isEdit) {
		this.isEdit=isEdit;
		notifyDataSetChanged();
	}
	
	public void setAllChecked(boolean checked) {
		if(selectList!=null) {
			for(int i=0;i<selectList.size();i++) {
				selectList.set(i, checked);
			}
			notifyDataSetChanged();
		}
	}
	
	public boolean clearRecentOpen() {
		if(selectList!=null) {
			DrawerDatabase db=new DrawerDatabase(context);
			for(int i=0;i<selectList.size();i++) {
				if(selectList.get(i)) {
					ApplicationInfo info=list.remove(i);
					selectList.remove(i--);
					db.clearDataByPackageName(info.getPackageName(), info.title.toString());
				}
			}
			return list.size()==0;
		}
		return false;
	}
	
	public void clearRecentOpenByPackageName(String packageName) {
		for(int i=0;i<list.size();i++) {
			ApplicationInfo info=list.get(i);
			if(info.getPackageName().equals(packageName)) {
				list.remove(i--);
				notifyDataSetChanged();
				break;
			}  
		}
	}
	
	public List<ApplicationInfo> getSelectList() {
		List<ApplicationInfo> appList=new ArrayList<ApplicationInfo>();
		for(int i=0;i<selectList.size();i++) {
			if(selectList.get(i)) {
				appList.add(list.get(i));
			}
		}
		return appList;
	}
	
	public List<ApplicationInfo> getAllList() {
		for(int i=0;i<selectList.size();i++) {
			if(selectList.get(i)) {
				list.get(i).getSortEntity().visible=1;
			}else {
				list.get(i).getSortEntity().visible=0;
			}
		}
		return list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list==null?0:list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(convertView==null) {
			convertView=LayoutInflater.from(context).inflate(R.layout.item_recent_open2, null);
			holder=new ViewHolder();
			holder.mCheckBox=(CheckBox)convertView.findViewById(R.id.checkbox);
			holder.mTextView=(BubbleTextView)convertView.findViewById(R.id.applicationinfotextview);
			convertView.setTag(holder);
		}else holder=(ViewHolder)convertView.getTag();
		ApplicationInfo info=list.get(position);
		holder.mTextView.setCompoundDrawablesWithIntrinsicBounds(null,
				info.icon, null, null);
		holder.mTextView.setText(info.title);
		holder.mTextView.setTag(info);
		if(isEdit) {
			holder.mCheckBox.setVisibility(View.VISIBLE);
			if(selectList.get(position)) {
				holder.mCheckBox.setChecked(true);
			}else {
				holder.mCheckBox.setChecked(false);
			}
			final int pos=position;
			holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					selectList.set(pos, !selectList.get(pos));
				}
			});
		}else {
			holder.mCheckBox.setVisibility(View.GONE);
		}
		return convertView;
	}

	private static class ViewHolder {
		CheckBox mCheckBox;
		BubbleTextView mTextView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(!isEdit) {
			ApplicationInfo info=list.get(position);
			final Intent intent = info.intent;
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        try {
	            context.startActivity(intent);
	            DrawerDatabase mDrawerDatabase=new DrawerDatabase(context);
				mDrawerDatabase.addOrUpdateOpenData(info.getId(), info.getItemType(), info.getPackageName(), info.title.toString());
	        } catch (ActivityNotFoundException e) {
	            Toast.makeText(context, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
	        } catch (SecurityException e) {
	            Toast.makeText(context, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
	        }
		}else {
			selectList.set(position, !selectList.get(position));
			notifyDataSetChanged();
		}
	}
}
