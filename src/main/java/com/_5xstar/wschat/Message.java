package com._5xstar.wschat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

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
    public MsgUser user;

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
    public String getJSONMsg(MsgUser srcUser){
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
    public Message(MsgUser user){
        this.user=user;
    }

    public static class JSONMessage {
        @JSONField
        public String msg;
        public JSONMessage(String msg){
            this.msg = msg;
        }
    }
    public static class JSONUserMessage extends JSONMessage{
        @JSONField
        public MsgUser user;
        public JSONUserMessage(MsgUser user, String msg){
            super(msg);
            this.user=user;
        }
    }
}
