package com._5xstar.wschat._default;

import com._5xstar.wschat.MsgUser;
import com._5xstar.wschat.WSChatServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 
 * 登出
 * 庞海文  2023-12-25
 */
public final class DefaultLogoutServlet extends HttpServlet{

	public DefaultLogoutServlet() {
		super();
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		this.doPost(request, response);
	}
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws  IOException{
		final MsgUser user = new MsgUser();
		user.serverName=Const.serverName;
		final String roomName = request.getParameter("roomName");
		if(roomName==null)user.roomName=Const.roomName;
		else user.roomName=roomName;
		WSChatServer.leave(user);  //移除聊天室
		response.sendRedirect(Const.loginPage);
	}



}