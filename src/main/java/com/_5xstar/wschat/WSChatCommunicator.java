package com._5xstar.wschat;

import com.alibaba.fastjson2.JSON;
import org.apache.tomcat.websocket.WsIOException;

import javax.annotation.Nonnull;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 聊天约定通信
 * 庞海文 2024-2-20
 */
public interface WSChatCommunicator {
    /**
     * 初始化聊天室，进入聊天室时使用
     */
    default String INIT_ROOM_HEAD() {return "INIT_ROOM:";}
    /**
     * 离开聊天室，通知聊天室其他人
     */
    default String LEAVE_ROOM_HEAD(){return "LEAVE_ROOM:";}
    /**
     * 进入聊天室，通知聊天室所有人
     */
    default String ENTER_ROOM_HEAD(){return  "ENTER_ROOM:";}

    /**
     * 命令库发送头
     */
    default String COMMAND_LIB_HEAD(){return "COMMAND_LIB:";}

    /**
     * 消息中的定向符
     * @return
     */
    default String TARGET_STRING(){return "#&40";}

    /**
     * 定向符
     * @return
     */
    default String TARGET_HEAD(){return "@";}

    /**
     * 发送命令库
     */
    default void sendComsLib(MsgUser user){
        System.out.println("send coms lib");
        Set<String> coms = WSChatServer.getComsList().get(user.serverName);
        if(coms==null || coms.isEmpty())return;
        try {
            WSChatServer.sendMsg(new Message(user) {
                @Override
                public String message() {
                    return COMMAND_LIB_HEAD()+ JSON.toJSONString(coms);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *新连接
     * @param session
     * @throws WsIOException
     */
    default void enter(WSChatServer client, Session session)throws WsIOException {
        System.out.println("ws login session="+session);  //test
        final List<String> ids = session.getRequestParameterMap().get("wsid");
        MsgUser user;
        if(ids!=null && !ids.isEmpty()
                && (user=WSChatServer.doWsid(false, ids.get(0), null))!=null ){
            client.setUser(user);
            client.setSession(session);
            Map<String, Set<WSChatServer>> rooms = WSChatServer.getServers().get(user.serverName);
            if(rooms==null){
                rooms = new HashMap<>();
                WSChatServer.getServers().put(user.serverName, rooms);
            }
            Set<WSChatServer> room = rooms.get(user.roomName);
            if(room==null){  //创建聊天室
                room = new CopyOnWriteArraySet<>();
                rooms.put(user.roomName, room);
            }else{
                for(WSChatServer old : room){  //移除旧对象
                    if(old.getUser().userName.equals(user.userName)){
                        old.leave();
                        break;
                    }
                }
            }
            room.add(client);
            //初始化聊天室
            final ArrayList<String> temp = new ArrayList<>();
            for(WSChatServer s : room)temp.add(s.getUser().userName);
            try {
                sendMsg(new Message(user) {
                    @Override
                    public String message() {
                        return INIT_ROOM_HEAD() + JSON.toJSONString(temp);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
            if(room.size()>1){
                final ArrayList<String> temp2 = new ArrayList<>();
                temp2.add(user.userName);
                final String message1 = ENTER_ROOM_HEAD()+JSON.toJSONString(temp2);
                broadcast(new Message(user) {
                    @Override
                    public String message() {
                        return message1;
                    }
                }, true);
            }
            final String message = String.format("%s进入。", user.userName);
            broadcast(new Message(user) {
                @Override
                public String message() {
                    return message;
                }
            });
            WSChatServer.es.submit(new Runnable() {  //启动异步线程进行处理
                @Override
                public void run() {
                    //try{Thread.sleep(2000);}catch (Exception e){}
                    //test();  //测试
                    sendComsLib(user);  //发送命令库
                }
            });

        }else{
            throw new WsIOException(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "id为空或不匹配！"));
        }
    }

    /**
     *离开聊天室
     * @param client
     */
    default void leave(@Nonnull WSChatServer client) {
        final MsgUser user = client.getUser();
        Map<String, Set<WSChatServer>> rooms = WSChatServer.getServers().get(user.serverName);
        if(rooms==null)return;  //服务器不存在
        Set<WSChatServer> room = rooms.get(user.roomName);
        if(room==null)return;  //聊天室不存在
        if(!room.contains(client))return;  //如果移除，不再操作
        room.remove(client);  //从聊天室中移除对象
        if(room.isEmpty()) {
            rooms.remove(user.roomName);  //如果聊天室已经没人，删除聊天室
            if(rooms.isEmpty())WSChatServer.getServers().remove(user.serverName);  //如果已没有聊天室，移除服务器
        }else {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(user.userName);
            final String message1 = LEAVE_ROOM_HEAD()+JSON.toJSONString(temp);
            broadcast(new Message(user) {
                @Override
                public String message() {
                    return message1;
                }
            });
            final String message2 = String.format("%s已离开。", user.userName);
            broadcast(new Message(user) {
                @Override
                public String message() {
                    return message2;
                }
            });
        }
    }

    /**
     * 接收客户端来消息
     * @param client
     * @param msg
     */
    default void msgIncoming(@Nonnull WSChatServer client, @Nonnull String msg) {
        System.out.println("incoming msg:" + msg);
        final MsgUser user = client.getUser();
        if(ComHandlers.filter(user, msg))return;  //是命令，已处理
        //处理定向发送
        final ArrayList<String> targets = new ArrayList<>();
        msg = direction(user, msg, targets);
        if(msg==null)return;
        // Never trust the client
        final String filteredMessage = String.format("%s: %s",
                user.userName, msg.replaceAll("<[^>]*>", "")).replaceAll(TARGET_STRING(), "@");
        final Message message = new Message(user) {
            @Override
            public String message() {
                return filteredMessage;
            }
        };
        if(targets.isEmpty()) {
            broadcast(message);
        }else{
            broadcast(message, targets);
        }
    }

    /**
     * 提取定向的用户
     * @param user
     * @param msg
     * @param targets
     * @return
     */
    default String direction(@Nonnull final MsgUser user, @Nonnull String msg, @Nonnull final ArrayList<String> targets){
        Map<String, Set<WSChatServer>> rooms = WSChatServer.getServers().get(user.serverName);
        if(rooms==null){
            System.out.println(String.format("%s服务器不存在！", user.serverName));
            return null;
        }
        Set<WSChatServer> room = rooms.get(user.roomName);
        if(room==null){
            System.out.println(String.format("%s聊天室不存在！", user.roomName));
            return null;
        }
        String userName;
        String temp;
        for (WSChatServer client : room) {
            userName = client.getUser().userName;
            temp = TARGET_HEAD() + userName;
            if(msg.contains(temp)) {
                msg=msg.replaceAll(temp, "");
                targets.add(userName);
            }
        }
        if("".equals(msg))return null;   //不发空消息
        return msg;
    }
    /**
     * 向聊天室中的每个用户广播消息
     * @param msg
     */
    default void broadcast(@Nonnull Message msg) {
        broadcast(  msg, false);
    }
    /**
     * 向聊天室中的每个用户广播消息
     * @param msg
     * @param targets 指定用户名
     */
    default void broadcast(@Nonnull Message msg, @Nonnull final List<String> targets) {
        if(targets.isEmpty())return;
        System.out.println(msg);  //测试
        Map<String, Set<WSChatServer>> rooms = WSChatServer.getServers().get(msg.user.serverName);
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
            if(targets.contains(client.getUser().userName)) {
                //try {
                synchronized (client) {
                    //client.session.getBasicRemote().sendText(msg);
                    client.getSession().getAsyncRemote().sendText(msg.getJSONMsg());
                }
                //} catch (IOException e) {
                //  e.printStackTrace();
                //}
            }
        }
    }
    /**
     * 向聊天室中的指定用户广播消息
     * @param msg
     * @param forOthers 只发给别人
     */
    default void broadcast(@Nonnull Message msg, boolean forOthers) {
        System.out.println(msg);  //测试
        Map<String, Set<WSChatServer>> rooms = WSChatServer.getServers().get(msg.user.serverName);
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
            if(forOthers && client.getUser().userName.equals(msg.user.userName))continue;
            //try {
            synchronized (client) {
                //client.session.getBasicRemote().sendText(msg);
                client.getSession().getAsyncRemote().sendText(msg.getJSONMsg());
            }
            //} catch (IOException e) {
            //  e.printStackTrace();
            //}
        }
    }

    /**
     * 主动离开
     * @param user
     */
    default void leave(final MsgUser user){
        if(user==null || user.serverName==null)return;
        Map<String, Set<WSChatServer>> rooms = WSChatServer.getServers().get(user.serverName);
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
            if(user.userName.equals(client.getUser().userName)) {
                synchronized (client) {
                    client.leave();
                    break;
                }
            }
        }
    }

    /**
     * 通知消息  约定msg.user==null为系统消息
     * @param msg
     */
    default void putMsg(@Nonnull final Message msg) {
        if(msg.user!=null){
            broadcast( msg);
        }else{
            msg.user = new MsgUser();
            for(String serverName : WSChatServer.getServers().keySet()) {
                msg.user.serverName=serverName;
                Map<String, Set<WSChatServer>> rooms = WSChatServer.getServers().get(serverName);
                for (String roomName : rooms.keySet()) {
                    msg.user.roomName = roomName;
                    broadcast(msg);
                }
            }
        }
    }

    /**
     * 为用户发送消息
     * @param msgUser
     * @param msg
     */
    default void putMsg(@Nonnull final MsgUser msgUser, @Nonnull final String msg){
        final Message message = new Message(msgUser) {
            @Override
            public String message() {
                return msg;
            }
        };
        putMsg(message);
    }

    /**
     * 把消息发给一个用户
     * @param msg
     */
    default void sendMsg(@Nonnull final Message msg) {
        if(msg.user==null)return;
        Map<String, Set<WSChatServer>> rooms = WSChatServer.getServers().get(msg.user.serverName);
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
            if(msg.user.userName.equals(client.getUser().userName)) {
                synchronized (client) {
                    //client.session.getBasicRemote().sendText(msg);
                    client.getSession().getAsyncRemote().sendText(msg.getJSONMsg());
                    break;
                }
            }
        }
    }

    /*default void test(){
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
	}*/


}
