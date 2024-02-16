package com._5xstar.wschat;

import javax.annotation.Nonnull;

/**
 * 消息用户
 * 庞海文 2024-1-23
 */
public class MsgUser extends UserHashMap<String,String>{
    /**
     * 服务器唯一标识
     */
    public String serverName;
    /**
     * 在serverName中的唯一标识
     */
    public String roomName;
    /**
     * 在roomName中的唯一标识
     */
    public String userName;

    /**
     * 完整构造函数
     * @param serverName
     * @param roomName
     * @param userName
     */
    public MsgUser(@Nonnull String serverName, @Nonnull String roomName, @Nonnull String userName){
        super();
        this.serverName=serverName;
        this.roomName=roomName;
        this.userName=userName;
    }

    /**
     * 默认构造函数，构造后需要输入三属性才能作为聊天室用户
     */
    public MsgUser(){
        super();
    }

    @Override
    public String toString(){
        return   "serverName="+serverName
                 +"\nroomName="+roomName
                 +"\nuserName="+userName;
    }
}
