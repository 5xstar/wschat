package com._5xstar.wschat;

import com._5xstar.wschat.service.CustomerService;
import com.alibaba.fastjson2.JSON;
import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 命令处理器集
 * 庞海文  2024-1-23
 */
public final class ComHandlers {

    /**
     * 处理器集合
     * <serverName,<command, handler>>
     */
    final private static HashMap<String, Map<String, ComHandler>> handlerses = new HashMap<>();

    /**
     * 添加1个处理器
     * @param serverName 服务器名
     * @param com  命令字符串
     * @param handler   处理器
     */
    public static void addHandler(String serverName, String com, ComHandler handler ){
        Map<String, ComHandler> handlers = handlerses.get(serverName);
        if(handlers==null){
            handlers = new HashMap<>();
            handlerses.put(serverName, handlers);
        }
        handlers.put(com, handler);
    }
    /**
     * 使用反射机制：通过反射获取所有类，然后判断类是否实现了指定的接口。
     */
    static {
        Reflections reflections = new Reflections();  //默认反射器
        Set<Class<? extends ComHandler>> classes = reflections.getSubTypesOf(ComHandler.class);  //获取ComHandler的所有实现的类
        for (Class<? extends ComHandler> clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {  //非抽象类
                Constructor<?>[] cs = clazz.getConstructors();
                for(Constructor<?> c : cs){
                    if(c.getParameterCount()==0){  //无参数构造函数
                        try {
                            ComHandler handler = (ComHandler)c.newInstance();
                            addHandler(handler.getServerName(), handler.getCom(), handler);
                            System.out.println(handler.getCom() + " ComHandler init!");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 检查客户端上传的是不是命令，并做相应的处理
     * @param msg 上传的消息
     * @return  已处理，true
     */
    public static void doCom(@Nonnull WSChatUser user, @Nonnull String msg){
        //if(hasNotInit)init();
        final Command command = getCommand(msg);
        if(command==null){
            System.out.println("command is null!");
            return;
        }
        Map<String, ComHandler> handlers = handlerses.get(command.serverName);
        if(handlers==null){
            System.out.println("handlers is empty!");
            CustomerService.filter(user, command);  //检查是不是客服，客服服务无处理器。
            return;
        }
        ComHandler handler = handlers.get(command.com);
        if(handler==null){  //如果命令处理器没有加入处理器集，出现这种情况，应该返回true而非false
            System.out.println(String.format("命令处理器：%s不存在！数据：%s", command.com,command.data));
            return;
        }
        WSChatServer.es.submit(new Runnable() {  //启动异步线程进行处理
            @Override
            public void run() {
                handler.handle(user, command.data);
            }
        });
    }

    /**
     * 检查用户输入是不是命令
     * @param msg
     * @return 命令对象
     */
    private static Command getCommand(String msg){
        try{
            return JSON.parseObject(msg, Command.class);
        }catch (Exception e){
            //e.printStackTrace();  //测试
            return null;
        }
    }

}
