package com._5xstar.wschat.service;

import com._5xstar.wschat.*;
import com.alibaba.fastjson2.JSON;

/**
 * 客服程序
 * 庞海文 2024-1-26
 */
public class CustomerService {
    /**
     * 客户提交咨询
     * @param user  客户
     * @param data  客户数据
     */
    private static void ask(WSChatUser user, String data) {
        System.out.println("CustomerService user=" + user + " data=" + data);
        WSChatServer.es.submit(new Runnable() {  //测试
            @Override
            public void run() {
                try {
                    //生成客服的虚拟客户
                    WSChatUser cs = new WSChatUser(){
                        @Override
                        public String getServerName() {
                            return Const.serverName;
                        }

                        @Override
                        public String getRoomName() {
                            return Const.roomName;
                        }

                        @Override
                        public String getUserName() {
                            return user.getUserName();
                        }

                        @Override
                        public Runnable kickRun() {
                            return null;
                        }
                    };
                    final Message temp = new Message(user) {
                        @Override
                        public String message() {
                            return data;
                        }
                    };
                    WSChatServer.sendMsg(temp);  //消息返回客户
                    //System.out.println("CustomerService user=" + user);
                    final WSChatUser _user = user;
                    WSChatServer.broadcast(new Message(cs) {  //发给客服组所以成员
                        @Override
                        public String message() {
                            return temp.getJSONMsg(_user);  //把客户打包进msg字段, user==cs，一定要重命名客户
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }
        });
    }

    /**
     * 客服回答客户
     * @param user  客服
     * @param data   回答数据，含user的Message对象
     */
    private static void answer(WSChatUser user, String data) {
        System.out.println("answer user=" + user + " data=" + data);
        WSChatServer.es.submit(new Runnable() {  //测试
            @Override
            public void run() {
                try {
                    final Message.JSONUserMessage umsg = JSON.parseObject(data, Message.JSONUserMessage.class);
                    WSChatServer.broadcast(new Message(user) { //广播客户
                        @Override
                        public String message() {
                            return umsg.msg;
                        }
                    });
                    WSChatServer.sendMsg(new Message(umsg.user) {  //提问客服
                        @Override
                        public String message() {
                            return umsg.msg;  //返回回答的内容
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }
        });
    }

    public static void filter(WSChatUser user, Command command){
        if(command.serverName.equals(Const.serviceName)){
            if(WSChatServer.checkServer(Const.serverName)) {
                if(command.com!=null) {
                    if(command.com.equals(Const.askCom)) ask(user, command.data);  //user是客户
                    else if(command.com.equals(Const.answerCom))answer(user,command.data);  //user是客服
                    else System.out.println("非法命令："+command);
                }
            }else try{ WSChatServer.sendMsg(new Message(user) {
                @Override
                public String message() {
                    return "客服不在线！";
                }
            });}catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

