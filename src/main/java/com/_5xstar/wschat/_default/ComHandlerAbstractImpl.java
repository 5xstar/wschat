package com._5xstar.wschat._default;

import com._5xstar.wschat.ComHandler;

public abstract class ComHandlerAbstractImpl implements ComHandler {
    /**
     * 获取服务器名称
     * @return
     */
    @Override
    public String getServerName(){
        return Const.serverName;
    }
}
