package com._5xstar.wschat;

/**
 * 命令处理器
 * 庞海文 2024-1-23
 */
public interface ComHandler {
    /**
     * 获取服务器名称
     * @return
     */
    String getServerName();
    /**
     * 获取命令字符串
     * @return
     */
    String getCom();
    /**
     * 处理客户端上传的字符串
     * @param data
     */
    void handle(MsgUser user, String data);


}
