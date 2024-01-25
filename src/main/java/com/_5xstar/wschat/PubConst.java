package com._5xstar.wschat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 公共常数
 * 庞海文  2024-1-25
 */
public class PubConst {
    //本应用用的执行池,在定时执行器中关闭
    public final static ExecutorService es= Executors.newCachedThreadPool();
}
