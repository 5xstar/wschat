package com._5xstar.wschat;

import com.alibaba.fastjson2.JSON;
import org.apache.tomcat.websocket.WsIOException;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.Closeable;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * tomcat websocket 服务
 * 庞海文 2024-1-24
 */
@ServerEndpoint(value = "/wscapi" )
public class WSChatServer implements Closeable {

	//命令库发送头
	public final static String comsLibHead = "comsLib:";

	// 记录当前有多少个用户加入到了聊天室，它是static全局变量。为了多线程安全使用原子变量AtomicInteger
	private static final AtomicInteger connectionIds = new AtomicInteger(0);

	//每个用户用一个CharAnnotation实例来维护，请你注意它是一个全局的static变量，所以用到了线程安全的CopyOnWriteArraySet
	//private static final Set<TbChatServer> connections = new CopyOnWriteArraySet<>();
	//聊天室
	private static final HashMap<String, Map<String, Set<WSChatServer>>>  servers = new HashMap<>();
	public static Set<String> getWsids(){
		return servers.keySet();
	}

	/**
	 * 每一个服务器的命令库
	 */
	private static final HashMap<String, Set<String>> comsList = new HashMap<>();

	/**
	 * 注册命令库
	 * {"serverName":"default","com":"test","data":"incoming json test","desc":"该字段可选，说明命令的功能"}
	 * @param serverName
	 * @param coms
	 */
	public static void  registerComs(@Nonnull String serverName, @Nonnull Set<String> coms){
		comsList.put(serverName, coms);
	}
	/**
	 * 添加一个命令到命令库
	 * @param serverName
	 * @param com
	 */
	public static void  addCom(@Nonnull String serverName, @Nonnull String com){
		Set<String> coms = comsList.get(serverName);
		if(coms==null){
			coms = new CopyOnWriteArraySet<>();
			comsList.put(serverName, coms);
		}
		coms.add(com);
	}

	/**
	 * 检查服务器是否存在
	 */
	public static boolean checkServer(String serverName){
		return servers.get(serverName)!=null;
	}

	//
	private static final Map<String, MsgUser> wsids = new HashMap<>();

	/**
	 * 存取wsid
	 * @param isPut 
	 * @param wsid
	 * @param user
	 * @return
	 */
	public static synchronized MsgUser doWsid(final boolean isPut, final String wsid, final MsgUser user){
		if(isPut) {
			wsids.put(wsid, user);
			return null;
		}else{
			return wsids.remove(wsid);
		}
	}
	
	private final int aid;  //测试
	private MsgUser user;
	private Session session;
	
	public WSChatServer(){
		aid = connectionIds.getAndIncrement();
	}
	
	
	//新连接到达时，Tomcat会创建一个Session，并回调这个函数
	@OnOpen
	public void start(Session session)throws WsIOException {
		System.out.println("ws login session="+session);  //test
		final List<String> ids = session.getRequestParameterMap().get("wsid");
		MsgUser user;
		if(ids!=null && !ids.isEmpty()
				&& (user=doWsid(false, ids.get(0), null))!=null ){
			this.user=user;
			this.session=session;
			Map<String, Set<WSChatServer>> rooms = servers.get(user.serverName);
			if(rooms==null){
				rooms = new HashMap<>();
				servers.put(user.serverName, rooms);
			}
			Set<WSChatServer> room = rooms.get(user.roomName);
			if(room==null){  //创建聊天室
				room = new CopyOnWriteArraySet<>();
				rooms.put(user.roomName, room);
			}else{
				for(WSChatServer old : room){  //移除旧对象
					if(old.user.userName.equals(user.userName)){
						old.end();
						break;
					}
				}
			}
			room.add(this);
			final String message = String.format("*(%d) %s  %s", aid, user.userName, "进入。");
			broadcast(new Message(user) {
				@Override
				public String message() {
					return message;
				}
			});
			PubConst.es.submit(new Runnable() {  //启动异步线程进行处理
				@Override
				public void run() {
					//try{Thread.sleep(2000);}catch (Exception e){}
					test();  //测试
					sendComsLib();  //发送命令库
				}
			});

		}else{
			throw new WsIOException(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "id为空或不匹配！"));
		}
	}
	private void test(){
		System.out.println("incoming test");
		incoming("incoming test");  //imcomming
		incoming("{\"serverName\":\"default\",\"com\":\"test\",\"data\":\"incoming json test\"}");  //imcomming {"serverName":"default","com":"test","data":"incoming json test"}

			putMsg(new Message(this.user) {   //putMsg测试
				@Override
				public String message() {
					System.out.println("putMsg测试");
					return "putMsg测试";
				}
			});
	}

	/**
	 * 发送命令库
	 */
	private void sendComsLib(){
		System.out.println("send coms lib");
		Set<String> coms = comsList.get(this.user.serverName);
		if(coms==null || coms.isEmpty())return;
		try {
			sendMsg(new Message(this.user) {
				@Override
				public String message() {
					return comsLibHead+JSON.toJSONString(coms);
				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	//浏览器关闭连接时，Tomcat会回调这个函数
	@OnClose
	public void end() {
		Map<String, Set<WSChatServer>> rooms = servers.get(user.serverName);
		if(rooms==null)return;  //服务器不存在
		Set<WSChatServer> room = rooms.get(user.roomName);
		if(room==null)return;  //聊天室不存在
		room.remove(this);  //从聊天室中移除对象
		if(room.isEmpty()) {
			rooms.remove(user.roomName);  //如果聊天室已经没人，删除聊天室
			if(rooms.isEmpty())servers.remove(user.serverName);  //如果已没有聊天室，移除服务器
		}else {
			final String message = String.format("*(%d) %s (%d) %s", aid, user.userName, "已离开。");
			broadcast(new Message(user) {
				@Override
				public String message() {
					return message;
				}
			});
		}
	}

	//实现Closeable
	@Override
	public void close(){
		System.out.println("Closeable action");  //测试
		end();
	}

	//浏览器发送消息到服务器时，Tomcat会回调这个函数
	@OnMessage
	public void incoming(String msg) {
		System.out.println("incoming msg:" + msg);
		if(ComHandlers.filter(user, msg))return;  //已处理
		// Never trust the client
		final String filteredMessage = String.format("%s: %s",
				user.userName, msg.replaceAll("<[^>]*>", ""));
		broadcast(new Message(user) {
			@Override
			public String message() {
				return filteredMessage;
			}
		});
	}

	// WebSocket连接出错时，Tomcat会回调这个函数
	@OnError
	public void onError(Throwable t) throws Throwable {
		System.out.println("聊天室发生错误：" + t.toString());
		end();
	}

	// 向聊天室中的每个用户广播消息
	public static void broadcast(Message msg) {
		System.out.println(msg);  //测试
		Map<String, Set<WSChatServer>> rooms = servers.get(msg.user.serverName);
		if(rooms==null){
			System.out.println(String.format("%s服务器不存在！", msg.user.serverName));
			return;
		}
		Set<WSChatServer> room = rooms.get(msg.user.roomName);
		if(room==null){
			System.out.println(String.format("%s聊天室不存在！", msg.user.roomName));
			return;
		}
		for (WSChatServer client : room) {
			//try {
				synchronized (client) {
					//client.session.getBasicRemote().sendText(msg);
					client.session.getAsyncRemote().sendText(msg.getJSONMsg());
				}
			//} catch (IOException e) {
             //  e.printStackTrace();
			//}
		}
	}

	//主动离开
	public static void leave(final MsgUser user){
		if(user==null || user.serverName==null)return;
		Map<String, Set<WSChatServer>> rooms = servers.get(user.serverName);
		if(rooms==null){
			System.out.println(String.format("%s服务器不存在！", user.serverName));
			return;
		}
		Set<WSChatServer> room = rooms.get(user.roomName);
		if(room==null){
			System.out.println(String.format("%s聊天室不存在！", user.roomName));
			return;
		}
		for (WSChatServer client : room) {
			if(user.userName.equals(client.user.userName)) {
				synchronized (client) {
					client.end();
					break;
				}
			}
		}
	}

	/**
	 * 系统通知消息
	 * @param msg
	 */
	public static void putMsg(final Message msg) {
		if(msg.user!=null){
			broadcast( msg);
		}else{
			msg.user = new MsgUser();
			for(String serverName : servers.keySet()) {
				msg.user.serverName=serverName;
				Map<String, Set<WSChatServer>> rooms = servers.get(serverName);
				for (String roomName : rooms.keySet()) {
					msg.user.roomName = roomName;
					broadcast(msg);
				}
			}
		}
	}
	/**
	 * 向用户发送消息
	 * @param msgUser
	 * @param msg
	 */
	public static void putMsg(final MsgUser msgUser, final String msg){
		final Message message = new Message(msgUser) {
			@Override
			public String message() {
				return msg;
			}
		};
		putMsg(message);
	}

	/**
	 * 把消息发个用户
	 * @param msg
	 * @throws IOException
	 */
	public static void sendMsg(final Message msg)throws IOException {
		if(msg.user==null)return;
		Map<String, Set<WSChatServer>> rooms = servers.get(msg.user.serverName);
		if(rooms==null){
			System.out.println(String.format("%s服务器不存在！", msg.user.serverName));
			return;
		}
		Set<WSChatServer> room = rooms.get(msg.user.roomName);
		if(room==null){
			System.out.println(String.format("%s聊天室不存在！", msg.user.roomName));
			return;
		}
		for (WSChatServer client : room) {
			if(msg.user.userName.equals(client.user.userName)) {
				synchronized (client) {
					//client.session.getBasicRemote().sendText(msg);
					client.session.getAsyncRemote().sendText(msg.getJSONMsg());
					break;
				}
			}
		}
	}


	/**
	 * 用户通过验证后，创建wsId
	 * @param request
	 * @param response
	 * @param roomName
	 * @param userName
	 * @return
	 */
	public static boolean createWsId(final HttpServletRequest request,
									 final HttpServletResponse response,
									 final String serverName,
									 final String roomName,
									 final String userName) {
		final MsgUser user = new MsgUser();
		user.serverName=serverName;
		user.roomName=roomName;
		user.userName=userName;
		return createWsId(  request,  response,  user);

	}
	public static boolean createWsId(final HttpServletRequest request,
									 final HttpServletResponse response,
									 final MsgUser user){
		//生成登录id
		try {
			final String wsid = createWsid();
			//System.out.println("wsid="+wsid);
			doWsid(true, wsid, user);
			String setHeaderCookieStr = "wsid=" + wsid ;
			if (request.isSecure()) {
				setHeaderCookieStr += "; Secure" ;
			}
			response.setHeader("Set-Cookie", setHeaderCookieStr);
			//response.addCookie(new Cookie("wsid", wsid));
			//request.setAttribute("wsid", wsid);
			//response.addCookie(new Cookie("username", user));
			//request.setAttribute("username", user);
			System.out.println("user="+user+" wsid="+wsid);  //测试
					/*String wsid = request.getParameter("wsid");
					String username = request.getParameter("username");
					if(wsid==null || username == null){
						Cookie[] cks = request.getCookies();
						if(cks!=null && cks.length>0){
							String n;
							for(int i=0; i<cks.length; i++) {
								n = cks[i].getName();
								if(n.equals("username"))wsid=cks[i].getValue();
								else if(n.equals("username"))username=cks[i].getValue();
							}
						}
					}*/
			return true;
		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 创建wsid
	 * @return
	 * @throws Exception
	 */
	private static String createWsid()throws Exception{
		System.out.println("enter createwsid");
		final SecureRandom sr = new SecureRandom();
		final byte[] d = new byte[64];
		sr.nextBytes(d);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 64; i++) {
			sb.append(String.format("%02X", d[i]));
		}
		return sb.toString();
	}
}
