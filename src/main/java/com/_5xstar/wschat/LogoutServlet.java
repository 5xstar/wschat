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
		final LogoutServlet srv = this;
		final WSChatUser user = new WSChatUser(){
			@Override
			public String getServerName() {
				return srv.getServerName();
			}
			@Override
			public String getRoomName() {
				final String roomName = request.getParameter("roomName");
				if(roomName==null)return srv.getRoomName();
				else return roomName;
			}
			@Override
			public String getUserName() {
				return null;
			}
			@Override
			public Runnable kickRun() {
				return null;
			}
		};
		WSChatServer.leave(user);  //移除聊天室
		response.sendRedirect(getLoginPage());
	}

	protected abstract String getServerName();
	protected abstract String getRoomName();
	protected abstract String getLoginPage();

}