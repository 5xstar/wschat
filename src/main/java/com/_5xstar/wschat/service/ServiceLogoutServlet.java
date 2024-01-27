package com._5xstar.wschat.service;

import com._5xstar.wschat.LogoutServlet;

/** 
 * 登出
 * 庞海文  2023-12-25
 */
public final class ServiceLogoutServlet extends LogoutServlet {

	public ServiceLogoutServlet() {
		super();
	}

	@Override
	protected String getServerName() {
		return Const.serverName;
	}

	@Override
	protected String getRoomName() {
		return Const.roomName;
	}

	@Override
	protected String getLoginPage() {
		return Const.loginPage;
	}


}