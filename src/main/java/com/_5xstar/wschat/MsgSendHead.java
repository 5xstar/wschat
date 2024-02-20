package com._5xstar.wschat;

/**
 * 向客户端发定制消息
 * 庞海文 2024-2-20
 */
public interface MsgSendHead {
    /**
     * 初始化聊天室，进入聊天室时使用
     */
    String INIT_ROOM_HEAD = "INIT_ROOM:";
    /**
     * 离开聊天室，通知聊天室其他人
     */
    String LEAVE_ROOM_HEAD = "LEAVE_ROOM:";
    /**
     * 进入聊天室，通知聊天室所有人
     */
    String ENTER_ROOM_HEAD = "ENTER_ROOM:";
}
