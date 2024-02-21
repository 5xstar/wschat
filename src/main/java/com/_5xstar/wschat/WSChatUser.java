package com._5xstar.wschat;

/**
 * 用户接口
 * 庞海文 2024-2-21
 */
public interface WSChatUser {
    /**
     * 服务器唯一标识
     */
    String getServerName();
    /**
     * 在serverName中的唯一标识
     */
    String getRoomName();
    /**
     * 在roomName中的唯一标识
     */
    String getUserName();

    /**
     * 是否可以被踢出
     * @return
     */
    default boolean canKick(){
        return true;
    }

    /**
     * 被踢出时执行的操作
     * @return
     */
    Runnable kickRun();
}
