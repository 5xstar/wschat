package com._5xstar.wschat._default.handlers;

import com._5xstar.wschat._default.ComHandlerAbstractImpl;

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
    public void handle(String data) {
        System.out.println("data="+data);
    }

}
