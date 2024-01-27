package com._5xstar.wschat;

import com._5xstar.wschat.ComHandler;
import com._5xstar.wschat._default.Const;

public abstract class ComHandlerAbstractImpl implements ComHandler {
    final public String serverName;

    /**
     * 获取服务器名称
     * @return
     */
    @Override
    public String getServerName(){
        return Const.serverName;
    }
    protected ComHandlerAbstractImpl(String serverName){
        this.serverName = serverName;
    }
}
