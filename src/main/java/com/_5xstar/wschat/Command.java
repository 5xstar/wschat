package com._5xstar.wschat;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 命令消息
 * 庞海文 2024-1-24
 */
public class Command {
    /**
     * 服务器名称，json使用默认字段名serverName， 可修改，例如@JSONField("s")，则段名改为"s"
     */
    @JSONField
    public String serverName;
    /**
     * 命令字符串，json使用默认字段名com， 可修改
     */
    @JSONField
    public String com;
    /**
     * 数据，json使用默认字段名data， 可修改
     */
    @JSONField
    public String data;
}
