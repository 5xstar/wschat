package com._5xstar.wschat.service;


import com._5xstar.wschat.LoginServlet;
/** 
 * 登录，需要验证
 * 庞海文  2023-12-25
 */
public final class ServiceLoginServlet extends LoginServlet {

	public ServiceLoginServlet() {
		super();
	}

	@Override
	protected String getUserPropsFile() {
		return Const.userPropsFile;
	}

	@Override
	protected String getServerName() {
		return Const.serverName;
	}

	@Override
	protected String getRoomName() {
		return Const.roomName;
	}


}