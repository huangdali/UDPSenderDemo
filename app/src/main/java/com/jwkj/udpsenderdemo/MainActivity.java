package com.jwkj.udpsenderdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jwkj.udpsenderdemo.bean.ShakeData;
import com.jwkj.udpsenderdemo.udpsender.UDPManger;
import com.jwkj.udpsenderdemo.udpsender.UDPResult;
import com.jwkj.udpsenderdemo.udpsender.UDPResultCallback;
import com.socks.library.KLog;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView tvReuslt;
    private ProgressDialog mProgressDialog;
    private int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvReuslt = (TextView) findViewById(R.id.tv_result);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("扫描中，请稍后");
    }

    /**
     * 开始扫描
     *
     * @param view
     */
    public void onStart(View view) {
//        shakeSimple();//简单调用的例子
        final ShakeData data = new ShakeData();
        data.setCmd(ShakeData.Cmd.CMD_SHAKE_DEVICE);
        UDPManger.getInstance()
                .setInstructions(ShakeData.getShakeDataCastByteArray(data))//设置发送的指令[必须，不可为空]
                .setReceiveTimeOut(10 * 1000)//设置接收超时时间[可不写，默认为8s]--超过10s没有接收到设备就视为无设备了就可以停止当前任务了
                .setTargetPort(ShakeData.Cmd.CMD_SHAKE_DEVICE_DEFAULT_PORT)//设置发送的端口[可不写，默认为8899端口]
                .setLocalReceivePort(ShakeData.Cmd.CMD_SHAKE_DEVICE_DEFAULT_PORT_RECEIVE)//设置本机接收的端口[可不写，默认为8899端口]
                .schedule(2, 3000)//执行2次，间隔三秒执行
                .send(new UDPResultCallback() {
                    /**
                     * 请求开始的时候回调
                     */
                    @Override
                    public void onStart() {
                        count = 1;
                        tvReuslt.setText("");
                        mProgressDialog.show();
                    }

                    /**
                     * 每拿到一个结果的时候就回调
                     *
                     * @param result 请求的结果
                     */
                    @Override
                    public void onNext(UDPResult result) {
                        ShakeData dataResult = ShakeData.getShakeDataResult(result.getResultData());
                        if (dataResult.getCmd() == ShakeData.Cmd.CMD_RECEIVE_MESSAGE_HEADER_CMDID) {

                        }
                        int id = dataResult.getId();
                        String pwd = dataResult.getFlag() == 1 ? "有密码" : "无密码";
                        tvReuslt.append((count++) + ")\t ip = " + result.getIp() + "\t\t\tid = " + id + "\t\t\t" + pwd + "\n\n");
                        KLog.e("result = " + dataResult.toString());
                    }

                    /**
                     * 请求结束的时候回调
                     */
                    @Override
                    public void onCompleted() {
                        mProgressDialog.dismiss();
                        Log.e(TAG, "onCompleted");
                    }

                    /**
                     * 当发生错误的时候回调
                     *
                     * @param throwable
                     */
                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "onError: " + throwable.getMessage());
                        Toast.makeText(MainActivity.this, "任务已在执行中", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 发送UDP广播最简答的一个例子，（可以不用设置超时时间，端口号，不用重写onStart，onError，onCompleted方法即可拿到结果）
     */
    private void shakeSimple() {
        count = 1;
        tvReuslt.setText("");
        ShakeData data = new ShakeData();
        data.setCmd(ShakeData.Cmd.CMD_SHAKE_DEVICE);

        UDPManger.getInstance()
                .setInstructions(ShakeData.getShakeDataCastByteArray(data))
                .send(new UDPResultCallback() {
                    /**
                     * 每拿到一个结果的时候就回调
                     *
                     * @param result 请求的结果
                     */
                    @Override
                    public void onNext(UDPResult result) {
                        ShakeData dataResult = ShakeData.getShakeDataResult(result.getResultData());
                        int id = dataResult.getId();
                        String pwd = dataResult.getFlag() == 1 ? "有密码" : "无密码";
                        tvReuslt.append((count++) + ")\t ip = " + result.getIp() + "\t\t\tid = " + id + "\t\t\t" + pwd + "\n\n");
                    }
                });
    }

    /**
     * 停止任务
     *
     * @param view
     */
    public void onStop(View view) {
        UDPManger.getInstance().stop();
    }
}
