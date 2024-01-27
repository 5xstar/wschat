package com._5xstar.wschat;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 
 * 登出
 * 庞海文  2023-12-25
 */
public abstract class LogoutServlet extends HttpServlet{

	protected LogoutServlet() {
		super();
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		this.doPost(request, response);
	}
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws  IOException{
		final MsgUser user = new MsgUser();
		user.serverName= getServerName();
		final String roomName = request.getParameter("roomName");
		if(roomName==null)user.roomName= getRoomName();
		else user.roomName=roomName;
		WSChatServer.leave(user);  //移除聊天室
		response.sendRedirect(getLoginPage());
	}

	protected abstract String getServerName();
	protected abstract String getRoomName();
	protected abstract String getLoginPage();

}