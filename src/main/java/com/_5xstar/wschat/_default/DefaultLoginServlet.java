package com._5xstar.wschat._default;

import com._5xstar.wschat.LoginServlet;

/** 
 * 登录，需要验证
 * 庞海文  2023-12-25
 */
public final class DefaultLoginServlet extends LoginServlet {

	public DefaultLoginServlet() {
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