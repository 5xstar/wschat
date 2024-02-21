package com._5xstar.wschat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * 消息
 * 庞海文 2024-1-24
 */
public abstract class Message {
    /**
     * 消息所有者
     * 如果==null，说明是系统消息
     */
    public WSChatUser user;

    /**
     * 获取消息
     * @return 字符串消息
     */
    public abstract String message();

    private String jsonMsg=null;
    public String getJSONMsg(){
            if (jsonMsg == null) {
                ArrayList<JSONMessage> lst = new ArrayList<>(1);
                lst.add(new JSONMessage(message()));
                jsonMsg = JSON.toJSONString(lst);
            }
            return jsonMsg;
    }

    /**
     * 客服程序调用打包源用户进msg
     * @param srcUser
     * @return
     */
    public String getJSONMsg(@Nonnull WSChatUser srcUser){
            // System.out.println("CustomerService user=" + user);
            //ArrayList<JSONUserMessage> lst = new ArrayList<>(1);
            //lst.add(new JSONUserMessage(srcUser,message()));
            //String json = JSON.toJSONString(lst);
            //System.out.println("srcUser="+srcUser+"  json="+json);
            return JSON.toJSONString(new JSONUserMessage(srcUser,message()));
    }

    /**
     * 构造函数
     * @param user 消息所有者
     */
    public Message(@Nonnull WSChatUser user){
        this.user=user;
    }
    public Message(@Nonnull JSONWSChatUser user){
        this(new WSChatUser() {
            @Override
            public String getServerName() {
                return user.serverName;
            }
            @Override
            public String getRoomName() {
                return user.roomName;
            }
            @Override
            public String getUserName() {
                return user.userName;
            }
            @Override
            public Runnable kickRun() {
                return null;
            }
        });
    }

    private static class JSONMessage {
        @JSONField
        public String msg;
        private JSONMessage(String msg){
            this.msg = msg;
        }
    }
    public static class JSONUserMessage extends JSONMessage{
        @JSONField
        public JSONWSChatUser user;
        private JSONUserMessage(@Nonnull WSChatUser user,@Nonnull String msg){
            super(msg);
            this.user=new JSONWSChatUser(user.getServerName(),user.getRoomName(),user.getUserName());
        }
    }
    private static class JSONWSChatUser{
        /**
         * 服务器唯一标识
         */
        @JSONField
        public String serverName;
        /**
         * 在serverName中的唯一标识
         */
        @JSONField
        public String roomName;
        /**
         * 在roomName中的唯一标识
         */
        @JSONField
        public String userName;

        /**
         * 完整构造函数
         * @param serverName
         * @param roomName
         * @param userName
         */
        private JSONWSChatUser(@Nonnull String serverName, @Nonnull String roomName, @Nonnull String userName){
            this.serverName=serverName;
            this.roomName=roomName;
            this.userName=userName;
        }

    }
}
