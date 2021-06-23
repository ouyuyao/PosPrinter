//package com.otto.posprinter.printer;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.otto.posprinter.R;
//
//import java.util.ArrayList;
//import java.util.Vector;
//
//import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
//import static com.otto.posprinter.printer.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
//import static com.otto.posprinter.printer.DeviceConnFactoryManager.CONN_STATE_FAILED;
//
//public class MainActivity extends AppCompatActivity {
//
//    private TextView mTvState;
//
//    /**
//     * 权限请求码
//     */
//    private static final int REQUEST_CODE = 0x001;
//
//    /**
//     * 蓝牙所需权限
//     */
//    private String[] permissions = {
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.BLUETOOTH
//    };
//
//    /**
//     * 未授予的权限
//     */
//    private ArrayList<String> per = new ArrayList<>();
//
//    /**
//     * 蓝牙请求码
//     */
//    public static final int BLUETOOTH_REQUEST_CODE = 0x002;
//
//    private ThreadPool threadPool;//线程
//
//    /**
//     * 判断打印机所使用指令是否是ESC指令
//     */
//    private int id = 0;
//
//    /**
//     * 打印机是否连接
//     */
//    private static final int CONN_PRINTER = 0x003;
//    /**
//     * 使用打印机指令错误
//     */
//    private static final int PRINTER_COMMAND_ERROR = 0x004;
//
//    /**
//     * 连接状态断开
//     */
//    private static final int CONN_STATE_DISCONN = 0x005;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        mTvState = findViewById(R.id.tv_state);
//
//        printer_checkPermission();
//        printer_requestPermission();
//
//    }
//
//    private void printer_checkPermission() {
//        for (String permission : permissions) {
//            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission)) {
//                per.add(permission);
//            }
//        }
//    }
//
//    private void printer_requestPermission() {
//        if (per.size() > 0) {
//            String[] p = new String[per.size()];
//            ActivityCompat.requestPermissions(this, per.toArray(p), REQUEST_CODE);
//        }
//    }
//
//
//    public void btnConnect(View view) {
//        startActivityForResult(new Intent(MainActivity.this, BluetoothListActivity.class), BLUETOOTH_REQUEST_CODE);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            //蓝牙连接
//            if (requestCode == BLUETOOTH_REQUEST_CODE) {
//                closePort();
//                //获取蓝牙mac地址
//                String macAddress = data.getStringExtra(BluetoothListActivity.EXTRA_DEVICE_ADDRESS);
//                //初始化DeviceConnFactoryManager 并设置信息
//                new DeviceConnFactoryManager.Build()
//                        //设置标识符
//                        .setId(id)
//                        //设置连接方式
//                        .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
//                        //设置连接的蓝牙mac地址
//                        .setMacAddress(macAddress)
//                        .build();
//                //配置完信息，就可以打开端口连接了
//                Log.i("TAG", "onActivityResult: 连接蓝牙" + id);
//                threadPool = ThreadPool.getInstantiation();
//                threadPool.addTask(new Runnable() {
//                    @Override
//                    public void run() {
//                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
//                    }
//                });
//            }
//        }
//    }
//
//    /**
//     * 重新连接回收上次连接的对象，避免内存泄漏
//     */
//    private void closePort() {
//        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null && DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort != null) {
//            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].reader!=null){
//                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].reader.cancel();
//            }
//            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort!=null){
//                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort.closePort();
//                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort = null;
//            }
//        }
//    }
//
//    /**
//     * 连接状态的广播
//     */
//    private BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (DeviceConnFactoryManager.ACTION_CONN_STATE.equals(action)) {
//                int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
//                int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
//                switch (state) {
//                    case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
//                        if (id == deviceId) mTvState.setText("未连接");
//                        break;
//                    case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
//                        mTvState.setText("连接中");
//                        break;
//                    case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
//                        mTvState.setText("已连接");
//                        Toast.makeText(MainActivity.this, "已连接", Toast.LENGTH_SHORT).show();
//                        break;
//                    case CONN_STATE_FAILED:
//                        mTvState.setText("未连接");
//                        Toast.makeText(MainActivity.this, "连接失败！重试或重启打印机试试", Toast.LENGTH_SHORT).show();
//                        break;
//                }
//                /* Usb连接断开、蓝牙连接断开广播 */
//            } else if (ACTION_USB_DEVICE_DETACHED.equals(action)) {
//                mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
//            }
//        }
//    };
//
//    @SuppressLint("HandlerLeak")
//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case CONN_STATE_DISCONN:
//                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null || !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
//                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
//                        Toast.makeText(MainActivity.this, "成功断开连接", Toast.LENGTH_SHORT).show();
//                    }
//                    break;
//                case PRINTER_COMMAND_ERROR:
//                    Toast.makeText(MainActivity.this, "请选择正确的打印机指令", Toast.LENGTH_SHORT).show();
//                    break;
//                case CONN_PRINTER:
//                    Toast.makeText(MainActivity.this, "请先连接打印机", Toast.LENGTH_SHORT).show();
//                    break;
//            }
//        }
//    };
//
//
//    /**
//     * 打印票据
//     */
//    public void btnPrint(View view) {
//        printReceipt();
//    }
//
//    public void printReceipt() {
//        Log.i("TAG", "准备打印");
//        threadPool = ThreadPool.getInstantiation();
//        threadPool.addTask(new Runnable() {
//            @Override
//            public void run() {
//                //先判断打印机是否连接
//                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
//                        !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
//                    mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
//                    return;
//                }
//                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.TSC) {
//                    Log.i("TAG", "开始打印"+ PrinterCommand.TSC);
//                    sendToPrint(PrintContent.getReceipt());
//                } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC){
//                    Log.i("TAG", "开始打印"+ PrinterCommand.ESC);
//                    sendToPrint(PrintContent.getReceipt());
//                } else {
//                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
//                }
//            }
//        });
//    }
//
//    /**
//     * 发送打印内容
//     */
//    private void sendToPrint(Vector<Byte> data) {
//        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null) {
//            Log.i("TAG", "sendLabel: 打印机为空");
//            return;
//        }
//        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(data);
//    }
//
//    /**
//     * 断开连接
//     */
//    public void btnPrinterDisConn(View view) {
//        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
//                !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
//            Toast.makeText(this, "请先连接打印机", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        /*
//         * 注册接收连接状态的广播
//         */
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ACTION_QUERY_PRINTER_STATE);
//        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
//        registerReceiver(receiver, filter);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        unregisterReceiver(receiver);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.i("TAG", "onDestroy");
//        DeviceConnFactoryManager.closeAllPort();
//        if (threadPool != null) {
//            threadPool.stopThreadPool();
//            threadPool = null;
//        }
//    }
//
//}
