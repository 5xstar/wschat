package com._5xstar.wschat._default.handlers;

import com._5xstar.wschat.ComHandlerAbstractImpl;
import com._5xstar.wschat.WSChatUser;
import com._5xstar.wschat._default.Const;

/**
 * 测试命令处理器
 * 庞海文 2024-1-24
 */
public class TestComHandler extends ComHandlerAbstractImpl {

    //json命令值
    final public static String com = "test";

    /**
     * 获取命令字符串
     * @return
     */
    public String getCom(){
        return com;
    }

    /**
     * 处理数据
     * @param data
     */
    @Override
    public void handle(WSChatUser user, String data) {
        System.out.println("user="+user+" data="+data);
    }

    public TestComHandler(){
        super(Const.serverName);
    }

}
