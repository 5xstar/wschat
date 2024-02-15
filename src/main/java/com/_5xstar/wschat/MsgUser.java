package com._5xstar.wschat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 消息用户
 * 庞海文 2024-1-23
 */
public class MsgUser extends UserHashMap<String,String>{
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

    @Override
    public String toString(){
        return JSON.toJSONString(this);
    }
}
