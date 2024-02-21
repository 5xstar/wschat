package com._5xstar.wschat;

import org.apache.tomcat.websocket.WsIOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.Closeable;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;

/**
 * tomcat websocket 服务
 * 庞海文 2024-1-24
 */
@ServerEndpoint(value = "/wscapi" )
public class WSChatServer implements Closeable {

	//本应用用的执行池,在定时执行器中关闭
	public final static ExecutorService es = Executors.newCachedThreadPool();

	//本应用使用的文本通信协议
	private static WSChatCommunicator communicator = new WSChatCommunicator() {};
	public static void setWSChatCommunicator(@Nonnull WSChatCommunicator wsctr){
		communicator=wsctr;
	}

	// 记录当前有多少个用户加入到了聊天室，它是static全局变量。为了多线程安全使用原子变量AtomicInteger
	//private static final AtomicInteger connectionIds = new AtomicInteger(0);

	//聊天室
	private static final HashMap<String, Map<String, Set<WSChatServer>>>  servers = new HashMap<>();
	public static HashMap<String, Map<String, Set<WSChatServer>>> getServers(){
		return servers;
	}

	/**
	 * 每一个服务器的命令库
	 */
	private static final HashMap<String, Set<String>> comsList = new HashMap<>();
	public static HashMap<String, Set<String>> getComsList(){
		return comsList;
	}
	/**
	 * 注册命令库
	 * {"serverName":"default","com":"test","data":"incoming json test","desc":"该字段可选，说明命令的功能"}
	 * @param serverName
	 * @param coms
	 */
	public static void  registerComs(@Nonnull String serverName, @Nonnull Set<String> coms){
		addComs(serverName, coms);
	}
	public static void  addComs(@Nonnull String serverName, @Nonnull Set<String> coms){
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
	 * 删除一个命令
	 * @param serverName
	 * @param com
	 */
	public static void  removeCom(@Nonnull String serverName, @Nonnull String com){
		Set<String> coms = comsList.get(serverName);
		if(coms==null)return;
		coms.remove(com);
		if(coms.isEmpty())comsList.remove(serverName);
	}

	/**
	 * 检查服务器是否存在
	 */
	public static boolean checkServer(@Nonnull String serverName){
		return servers.get(serverName)!=null;
	}

	//
	private static final Map<String, MsgUser> wsids = new HashMap<>();
	public static Set<String> getWsids(){
		return wsids.keySet();
	}

	/**
	 * 存取wsid
	 * @param isPut 
	 * @param wsid
	 * @param user
	 * @return
	 */
	public static synchronized MsgUser doWsid(final boolean isPut, final String wsid, @Nullable final MsgUser user){
		if(isPut) {
			wsids.put(wsid, user);
			return null;
		}else{
			return wsids.remove(wsid);
		}
	}
	
	//private final int aid;  //测试
	private MsgUser user;
	public void setUser(@Nonnull MsgUser user){
		this.user=user;
	}
	public MsgUser getUser(){
		return this.user;
	}
	private Session session;
	public void setSession(@Nonnull Session session){
		this.session=session;
	}
	public Session getSession(){
		return this.session;
	}
	
	public WSChatServer(){
		//aid = connectionIds.getAndIncrement();
	}
	
	
	//新连接到达时，Tomcat会创建一个Session，并回调这个函数
	@OnOpen
	public void enter(Session session)throws WsIOException {
		communicator.enter(this, session);
	}

	//浏览器关闭连接时，Tomcat会回调这个函数
	@OnClose
	public void leave() {
		communicator.leave(this);
	}

	//实现Closeable，这个方法只有在对象失联后，被系统清理时才执行
	@Override
	public void close(){
		System.out.println("Closeable action");  //测试
		leave();
	}

	//浏览器发送消息到服务器时，Tomcat会回调这个函数
	@OnMessage
	public void msgIncoming(String msg) {
		if(msg==null)return;
		communicator.msgIncoming(this, msg);
	}

	// WebSocket连接出错时，Tomcat会回调这个函数
	@OnError
	public void onError(Throwable t) throws Throwable {
		System.out.println("聊天室发生错误：" + t.toString());
		leave();
	}

	// 向聊天室中的每个用户广播消息
	public static void broadcast(Message msg) {
		communicator.broadcast(msg) ;
	}

	//主动离开
	public static void leave(final MsgUser user){
		communicator.leave(user);
	}

	/**
	 * 系统通知消息
	 * @param msg
	 */
	public static void putMsg(@Nonnull final Message msg) {
		communicator.putMsg(msg);
	}

	/**
	 * 为用户发送消息
	 * @param msgUser
	 * @param msg
	 */
	public static void putMsg(@Nonnull final MsgUser msgUser, @Nonnull final String msg){
		communicator.putMsg(msgUser, msg);
	}

	/**
	 * 把消息发给一个用户
	 * @param msg
	 */
	public static void sendMsg(@Nonnull final Message msg) {
		communicator.sendMsg(msg);
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
			doWsid(true, wsid, user);
			String setHeaderCookieStr = "wsid=" + wsid ;
			if (request.isSecure()) {
				setHeaderCookieStr += "; Secure" ;
			}
			response.setHeader("Set-Cookie", setHeaderCookieStr);
			System.out.println("user="+user+"\nwsid="+wsid);  //测试
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
