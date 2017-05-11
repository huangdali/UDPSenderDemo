package com.jwkj.udpsenderdemo.udpsender;


/**
 * UDP管理器，隔离UDP实现层逻辑
 * Created by dali on 2017/4/14.
 */

public class UDPManger {
    private static UDPManger udpManger;

    private UDPManger() {
    }

    public static UDPManger getInstance() {
        if (udpManger == null) {
            synchronized (UDPManger.class) {
                udpManger = new UDPManger();
            }
        }
        return udpManger;
    }

    /**
     * 接收数据超时时间
     */
    private int receiveTimeOut = 8 * 1000;//默认8s

    /**
     * 指定数组（字节）
     */
    private byte[] instructions;
    /**
     * 目标端口，默认为8899
     */
    private int targetPort = UDPSender.DEFAULT_PORT;

    /**
     * 本机接收端口，默认为8899
     */
    private int receivePort = UDPSender.DEFAULT_PORT;


    /**
     * 设置接收超时时间
     *
     * @param receiveTimeOut 超时时间
     * @return
     */
    public UDPManger setReceiveTimeOut(int receiveTimeOut) {
        this.receiveTimeOut = receiveTimeOut;
        return this;
    }

    /**
     * 发送的次数，默认是1次
     */
    private int sendCount = 1;

    /**
     * 上一次结束时到下一次开始的时间，默认10s
     */
    private long delay = 10 * 1000;

    /**
     * 设置指令
     *
     * @param instructions 指令字节数组
     * @return
     */
    public UDPManger setInstructions(byte[] instructions) {
        this.instructions = instructions;
        return this;
    }

    /**
     * 设置请求端口
     *
     * @param targetPort 请求端口号，默认为8899，范围是1024-65535
     * @return 当前发送器对象
     */
    public UDPManger setTargetPort(int targetPort) {
        this.targetPort = targetPort;
        return this;
    }

    /**
     * 设置请求端口
     *
     * @param receivePort 本机接收端口号，默认为8899，范围是1024-65535
     * @return 当前发送器对象
     */
    public UDPManger setReceivePort(int receivePort) {
        this.receivePort = receivePort;
        return this;
    }

    private int currentCount = 1;

    /**
     * 发送UDP广播
     *
     * @param callback 结果回调
     */
    public synchronized void send(final UDPResultCallback callback) {
        this.callback = callback;
        callback.onStart();
        startTask();
    }

    private UDPResultCallback callback;
    UDPSender udpSender;

    private void startTask() {
        if (udpSender != null && udpSender.isRunning()) {
            callback.onError(new Throwable("Task running"));//任务执行中
            callback.onCompleted();
        } else {
            udpSender = new UDPSender();//重新创建对象
            udpSender.setInstructions(instructions)//设置请求指令
                    .setTargetPort(targetPort)//设置搜索设备的端口
                    .setReceivePort(receivePort)//设置搜索设备的端口
                    .setReceiveTimeOut(receiveTimeOut)//设置搜索超时时间
                    .send(new UDPResultCallback() {
                        @Override
                        public void onNext(UDPResult result) {
//                            ELog.hdl("拿到结果了" + result);
                            callback.onNext(result);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            callback.onError(throwable);
                        }

                        @Override
                        public void onCompleted() {
                            currentCount++;
                            if (currentCount <= sendCount) {
                                startTask();
                            } else {
                                currentCount = 0;//要复位
                                callback.onCompleted();
                            }
                        }
                    });
        }
    }

    /**
     * 是否正在运行
     *
     * @return
     */
    public boolean isRunning() {
        if (udpSender == null) {
            return false;
        }
        return udpSender.isRunning();
    }

    /**
     * 停止运行
     */
    public UDPManger stop() {
        udpSender.stop();
        return this;
    }

    /**
     * 发送安排
     *
     * @param sendCount 发送次数
     * @param delay     上一次结束到下一次开始的间隔
     */
    public UDPManger schedule(int sendCount, long delay) {
        this.sendCount = sendCount;
        this.delay = delay;
        return this;
    }
}
