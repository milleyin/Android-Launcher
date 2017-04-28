package com.dongji.enity;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证已安装应用是否存在高危权限
 * @author RanQing
 *
 */

public class HighRiskPermissionExpl {

	private String[] highRiskArray = {
			"android.permission.DELETE_PACKAGES",
			"android.permission.INSTALL_PACKAGES",
			"android.permission.INTERNET",
			"android.permission.READ_SMS",
			"android.permission.RECEIVE_SMS",
			"android.permission.RESTART_PACKAGES",
			"android.permission.SEND_SMS",
			"android.permission.SET_PREFERRED_APPLICATIONS",
			"android.permission.WRITE_SETTINGS",
			"android.permission.WRITE_SMS"
	};
	private String[] highRiskExpArray = {
			"允许一个程序删除包;",
			"允许一个程序安装packages;",
			"允许程序打开网络套接字;",
			"允许程序读取短信息;",
			"允许程序监控一个将收到短信息，记录或处理;",
			"允许程序重新启动其他程序;",
			"允许程序发送SMS短信;",
			"允许一个程序修改列表参数;",
			"允许程序读取或写入系统设置(Allows an application to read or write the system settings. );",
			"允许程序写短信(Allows an application to write SMS messages);"
	};
	
	private static HighRiskPermissionExpl permissionExplInstance;
	
	private HighRiskPermissionExpl() {}
	
	public static HighRiskPermissionExpl getInstance() {
		if (permissionExplInstance == null) {
			permissionExplInstance = new HighRiskPermissionExpl();
		}
		return permissionExplInstance;
	}
	
	/**
	 * 查检是否存在高危权限
	 * @param permissionList
	 * @return
	 */
	public boolean hasHighRiskPermission(String[] permissionList) {
		if (permissionList != null && permissionList.length > 0) {
			for (String highRisk : highRiskArray) {
				for (String permission : permissionList) {
					if (highRisk.equals(permission)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 根据权限名称获取相关权限说明
	 * @param permissionList
	 * @return
	 */
	public List<PermissionCls> getPermissionExplain(String[] permissionList) {
		List<PermissionCls> list = new ArrayList<PermissionCls>();
		for (int i = 0 ; i < highRiskArray.length ; i++) {
			for (String permission : permissionList) {
				if (highRiskArray[i].equals(permission)) {
					list.add(new PermissionCls(permission, highRiskExpArray[i]));
				}
			}
		}
		return list;
	}
	
	public class PermissionCls {
		String permissionName, permissionExp;

		public PermissionCls(String permissionName, String permissionExp) {
			super();
			this.permissionName = permissionName;
			this.permissionExp = permissionExp;
		}

		public String getPermissionName() {
			return permissionName;
		}

		public void setPermissionName(String permissionName) {
			this.permissionName = permissionName;
		}

		public String getPermissionExp() {
			return permissionExp;
		}

		public void setPermissionExp(String permissionExp) {
			this.permissionExp = permissionExp;
		}
		
	}
}
