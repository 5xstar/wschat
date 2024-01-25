package com._5xstar.wschat._default;


import com._5xstar.wschat.WSChatServer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/** 
 * 登录，需要验证
 * 庞海文  2023-12-25
 */
public final class DefaultLoginServlet extends HttpServlet {

	public DefaultLoginServlet() {
		super();
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		this.doPost(request, response);
	}
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		   if(validate(request, response)){
			   response.setStatus(200);
			   //response.sendRedirect("lljk.jsp");
		   }else{
			   //非法登录终止
			   response.setStatus(403);
			   //response.sendRedirect("error.jsp");
		   }
	}
	private static boolean validate(final HttpServletRequest request, final HttpServletResponse response ){
		final String userName = request.getParameter("username");
		String roomName = request.getParameter("roomName");
		if(roomName==null)roomName=Const.roomName;
		String p;
		if(userName!=null && (p = getPassword(  request,   userName))!=null){
			final String password =  request.getParameter("password");
			if(password!=null && p.equals(password)){
				return WSChatServer.createWsId(  request, response, Const.serverName, roomName, userName);
			}
		}
		return false;
	}

	/**
	 * 获取用户密码
	 * @param request
	 * @param user
	 * @return
	 */
	private static String getPassword(final HttpServletRequest request, final String user){
		File f=null;
		final URL url = DefaultLoginServlet.class.getResource("com/_5xstar/wschat/_default/wschat_default_users.properties");
		if(url!=null)f = new File(url.getFile());
		if(f==null || !f.exists()) {
			final HttpSession session = request.getSession();
			final ServletContext servletContext = session.getServletContext();
			String path = servletContext.getRealPath("WEB-INF");
			System.out.println("path=" + path);
			f = new File(path, "com/_5xstar/wschat/_default/wschat_default_users.properties");
			if(!f.exists()) {
				path = servletContext.getRealPath("WEB-INF/classes");
				System.out.println("path=" + path);
				f = new File(path, "com/_5xstar/wschat/_default/wschat_default_users.properties");
			}
		}
		System.out.println(f.getAbsolutePath());
		try {
			if (f.exists()) {
				Properties p = new Properties();
				p.load(new FileInputStream(f));
				return (String) p.getProperty(user);
			}
		}catch (IOException ioe){
			ioe.printStackTrace();
		}
		return null;
	}


}