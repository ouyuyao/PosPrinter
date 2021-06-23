package com.otto.posprinter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bbpos.bbdevice.BBDeviceController;
import com.bbpos.bbdevice.BBDeviceController.AccountSelectionResult;
import com.bbpos.bbdevice.BBDeviceController.AmountInputType;
import com.bbpos.bbdevice.BBDeviceController.AudioAutoConfigError;
import com.bbpos.bbdevice.BBDeviceController.BBDeviceControllerListener;
import com.bbpos.bbdevice.BBDeviceController.BatteryStatus;
import com.bbpos.bbdevice.BBDeviceController.CheckCardMode;
import com.bbpos.bbdevice.BBDeviceController.CheckCardResult;
import com.bbpos.bbdevice.BBDeviceController.ConnectionMode;
import com.bbpos.bbdevice.BBDeviceController.ContactlessStatus;
import com.bbpos.bbdevice.BBDeviceController.ContactlessStatusTone;
import com.bbpos.bbdevice.BBDeviceController.CurrencyCharacter;
import com.bbpos.bbdevice.BBDeviceController.DisplayPromptIcon;
import com.bbpos.bbdevice.BBDeviceController.DisplayPromptOption;
import com.bbpos.bbdevice.BBDeviceController.DisplayPromptResult;
import com.bbpos.bbdevice.BBDeviceController.DisplayText;
import com.bbpos.bbdevice.BBDeviceController.EmvOption;
import com.bbpos.bbdevice.BBDeviceController.EncryptionKeySource;
import com.bbpos.bbdevice.BBDeviceController.EncryptionKeyUsage;
import com.bbpos.bbdevice.BBDeviceController.EncryptionMethod;
import com.bbpos.bbdevice.BBDeviceController.EncryptionPaddingMethod;
import com.bbpos.bbdevice.BBDeviceController.Error;
import com.bbpos.bbdevice.BBDeviceController.LEDMode;
import com.bbpos.bbdevice.BBDeviceController.NfcDetectCardResult;
import com.bbpos.bbdevice.BBDeviceController.PhoneEntryResult;
import com.bbpos.bbdevice.BBDeviceController.PinEntryResult;
import com.bbpos.bbdevice.BBDeviceController.PinEntrySource;
import com.bbpos.bbdevice.BBDeviceController.PrintResult;
import com.bbpos.bbdevice.BBDeviceController.ReadNdefRecord;
import com.bbpos.bbdevice.BBDeviceController.SessionError;
import com.bbpos.bbdevice.BBDeviceController.TerminalSettingStatus;
import com.bbpos.bbdevice.BBDeviceController.TransactionResult;
import com.bbpos.bbdevice.BBDeviceController.TransactionType;
import com.bbpos.bbdevice.BBDeviceController.VASResult;
import com.bbpos.bbdevice.CAPK;
import com.bbpos.bbdevice.VASMerchantConfig;
import com.bbpos.bbdevice.ota.BBDeviceOTAController;
import com.otto.posprinter.printer.BluetoothListActivity;
import com.otto.posprinter.printer.DeviceConnFactoryManager;
import com.otto.posprinter.printer.PrintContent;
import com.otto.posprinter.printer.PrinterCommand;
import com.otto.posprinter.printer.ThreadPool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.otto.posprinter.printer.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.otto.posprinter.printer.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class BaseActivity extends Activity {

	enum State {
		GETTING_PSE, READING_RECORD, READING_AID, GETTING_PROCESS_OPTION, READING_DATA
	}

	static final String[] DEVICE_NAMES = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	static final int PERMISSION_REQUEST_CODE = 200;
	static BBDeviceController bbDeviceController;
	static BBDeviceOTAController otaController;
	static MyBBDeviceControllerListener listener;
	static MyBBDeviceOTAControllerListener otaListener;
    static MyBBDeviceControllerSpocListener spocListener;
	static BaseActivity currentActivity;

	static String masterKey = "B93EEC40D5A2F45885BC831AA4DC131F";

	static String pinSessionKey = "A1223344556677889900AABBCCDDEEFF";
	static String encryptedPinSessionKey = "";
	static String pinKcv = "";

	static String dataSessionKey = "A2223344556677889900AABBCCDDEEFF";
	static String encryptedDataSessionKey = "";
	static String dataKcv = "";

	static String trackSessionKey = "A4223344556677889900AABBCCDDEEFF";
	static String encryptedTrackSessionKey = "";
	static String trackKcv = "";

	static String macSessionKey = "A6223344556677889900AABBCCDDEEFF";
	static String encryptedMacSessionKey = "";
	static String macKcv = "";

	static String fid65WorkingKey = "A1223344556677889900AABBCCDDEEFF";
	static String fid65MasterKey = "0123456789ABCDEFFEDCBA9876543210";

	static Button startButton;
	static EditText amountEditText;
	static EditText statusEditText;
	static CheckBox tipAmountTipsPercentageCashbackCheckBox;
	static ListView appListView;
	static Dialog dialog;
	static ProgressDialog progressDialog;

	static Button clearLogButton;
	static Button powerOnIccButton;
	static Button powerOffIccButton;
	static Button apduButton;
	static Button getCAPKListButton;
	static Button getCAPKDetailButton;
	static Button findCAPKButton;
	static Button updateCAPKButton;
	static Button removeCAPKButton;
	static Button getEmvReportListButton;
	static Button getEmvReportButton;
	
	static Button updateGprsSettingButton;
	static Button updateWifiSettingButton;
	static Button readGprsSettingButton;
	static Button readWifiSettingButton;
	
	static Button startNfcDetectionButton;
	static Button startNfcDetectionAppleVasButton;
	static Button stopNfcDetectionButton;
	static Button nfcDataExchangeWriteButton;
	static Button nfcDataExchangeRead1StButton;
	static Button nfcDataExchangeReadNextButton;

	static ArrayAdapter<String> arrayAdapter;
	static String amount = "";
	static String cashbackAmount = "";
	static boolean isPinCanceled = false;

	static List<BluetoothDevice> foundDevices;

	static ArrayList<byte[]> receipts;

	static String cardholderName;
	static String cardNumber;
	static String expiryDate;
	static String track2 = "";
	static String pan = "";
	static String aid;
	static String appLabel;
	static String tc;
	static String batchNum;
	static String tid;
	static String mid;
	static String transactionDateTime;
	static boolean signatureRequired;

	static String ksn = "";
	static boolean isApduEncrypted = true;
	static final String DATA_KEY = "Data Key";
	static final String DATA_KEY_VAR = "Data Key Var";
	static final String PIN_KEY_VAR = "PIN Key Var";

	static final String ECB = "ECB";
	static final String CBC = "CBC";
	static String keyMode = DATA_KEY;
	static String encryptionMode = CBC;
	static State state = null;

	private static int aidCounter = 0;
	private static String[] afls = null;
	private static int aflCounter = 0;
	private static String sfi = "";
	private static int readingFileIndex = 0;
	private static int total = 0;

	static long startTime;
	static boolean isPKCS7 = false;
	boolean isSwitchingActivity = false;

	private static String[] aids = new String[] { "A0000000031010", "A0000000041010", "A00000002501" };
	
	private static CheckCardMode checkCardMode;
	
	private static final String BDK = "0123456789ABCDEFFEDCBA9876543210";
	private String uid = null;

	private static BBDeviceController.VASTerminalMode vasTerminalMode = BBDeviceController.VASTerminalMode.SINGLE;
	private static List<VASMerchantConfig> vasList = new ArrayList<VASMerchantConfig>();
	private static String vasNFCCardDetectionTimeout = "30";
	private static String vasNFCOperationMode = "00";

	static SessionData sessionData = new SessionData();

	private static final boolean DEBUG_MODE = false;

	private final static String LOG_TAG = BaseActivity.class.getName();

	private void log(String msg) {
		if (DEBUG_MODE) {
			Log.d(LOG_TAG, "[BaseActivity] " + msg);
		}
	}

	static TextView transactionStatusTitleTv;
	static TextView transactionAmountTv;
	static Button transactionStatusBtn;
	static boolean trnCompleted = false;

	static boolean posConnect = false;
	static boolean printerConnect = false;

	static EditText posText;
	//打印机所需参数----------start
	static EditText printerText;//打印机连接状态显示
	private static final int REQUEST_CODE = 0x001;//权限请求码
	//蓝牙所需权限
	private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.BLUETOOTH};
	private ArrayList<String> per = new ArrayList<>();//未授予的权限
	public static final int BLUETOOTH_REQUEST_CODE = 0x002;//蓝牙请求码
	private ThreadPool threadPool;//线程
	private int id = 0;//判断打印机所使用指令是否是ESC指令
	private static final int CONN_PRINTER = 0x003;//打印机是否连接
	private static final int PRINTER_COMMAND_ERROR = 0x004;//使用打印机指令错误
	private static final int CONN_STATE_DISCONN = 0x005;//连接状态断开
	//打印机所需参数----------end

	//判断amount是否为数字，如果是数字则转化为两位小数的string
	public static String amountDigitalCheck(String amount){
		String result = "";
		double doubleAmount = 0.00;
		try{
			doubleAmount = Double.parseDouble(amount);
			result = String.format("%.2f", doubleAmount);
			return result;
		}catch (Exception e){
			result = amount;
			return result;
		}
	}
	//打印机所用到的方法函数----------start
	private void printer_checkPermission() {
		for (String permission : permissions) {
			if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission)) {
				per.add(permission);
			}
		}
	}

	private void printer_requestPermission() {
		if (per.size() > 0) {
			String[] p = new String[per.size()];
			ActivityCompat.requestPermissions(this, per.toArray(p), REQUEST_CODE);
		}
	}

	//重新连接回收上次连接的对象，避免内存泄漏
	private void closePort() {
		if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null && DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort != null) {
			if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].reader!=null){
				DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].reader.cancel();
			}
			if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort!=null){
				DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort.closePort();
				DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort = null;
			}
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			//蓝牙连接
			if (requestCode == BLUETOOTH_REQUEST_CODE) {
				closePort();
				//获取蓝牙mac地址
				String macAddress = data.getStringExtra(BluetoothListActivity.EXTRA_DEVICE_ADDRESS);
				//初始化DeviceConnFactoryManager 并设置信息
				new DeviceConnFactoryManager.Build()
						//设置标识符
						.setId(id)
						//设置连接方式
						.setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
						//设置连接的蓝牙mac地址
						.setMacAddress(macAddress)
						.build();
				//配置完信息，就可以打开端口连接了
				Log.i("TAG", "onActivityResult: 连接蓝牙" + id);
				threadPool = ThreadPool.getInstantiation();
				threadPool.addTask(new Runnable() {
					@Override
					public void run() {
						DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
					}
				});
			}
		}
	}
	/**
	 * 打印票据
	 */
	public void printReceipt(Map<String,Object> map) {
		Log.i("TAG", "准备打印");
		threadPool = ThreadPool.getInstantiation();
		threadPool.addTask(new Runnable() {
			@Override
			public void run() {
				//先判断打印机是否连接
				if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
						!DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
					mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
					return;
				}
				if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.TSC) {
					Log.i("TAG", "开始打印"+ PrinterCommand.TSC);
					sendToPrint(PrintContent.getReceipt(map));
				} else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC){
					Log.i("TAG", "开始打印"+ PrinterCommand.ESC);
					sendToPrint(PrintContent.getReceipt(map));
				} else {
					mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
				}
			}
		});
	}
	/**
	 * 发送打印内容
	 */
	private void sendToPrint(Vector<Byte> data) {
		if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null) {
			Log.i("TAG", "sendLabel: 打印机为空");
			return;
		}
		DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(data);
	}
	//打印机所用到的方法函数----------end

	//打印机所用到的蓝牙连接线程----------start
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CONN_STATE_DISCONN:
					if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null || !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
						DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
						Toast.makeText(BaseActivity.this, R.string.stop_printer_connection_success, Toast.LENGTH_SHORT).show();
					}
					break;
				case PRINTER_COMMAND_ERROR:
					Toast.makeText(BaseActivity.this, R.string.printer_need_valid_command, Toast.LENGTH_SHORT).show();
					break;
				case CONN_PRINTER:
					Toast.makeText(BaseActivity.this, R.string.please_connect_printer, Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};
	//打印机所用到的蓝牙连接线程----------end

	//打印机所用到的蓝牙广播接收者----------start
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (DeviceConnFactoryManager.ACTION_CONN_STATE.equals(action)) {
				int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
				int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
				switch (state) {
					case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
						if (id == deviceId) {
							printerText.setText(getResources().getText(R.string.printer_not_connect));
						}
						break;
					case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
						printerText.setText(getResources().getText(R.string.printer_connecting));
						break;
					case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
						printerText.setText(getResources().getText(R.string.printer_connected));
						printerConnect = true;
						Toast.makeText(BaseActivity.this, R.string.printer_connected, Toast.LENGTH_SHORT).show();
						break;
					case CONN_STATE_FAILED:
						printerText.setText(getResources().getText(R.string.printer_connect_failed));
						Toast.makeText(BaseActivity.this, R.string.printer_connect_failed, Toast.LENGTH_SHORT).show();
						break;
				}
				/* Usb连接断开、蓝牙连接断开广播 */
			} else if (ACTION_USB_DEVICE_DETACHED.equals(action)) {
				mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
			}
		}
	};
	//打印机所用到的蓝牙广播接收者----------end

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//打印机所需初始化方法调用----start
		printer_checkPermission();
		printer_requestPermission();
		//打印机所需初始化方法调用----end

		// Check permission
		if (Build.VERSION.SDK_INT >= 23) {
			if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
					|| (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
					|| (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
					|| (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
					|| (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
					|| (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
					|| (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
					|| (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
					|| (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)) {
				// Permission is not granted

				String[] permissionList = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE};
				ActivityCompat.requestPermissions(this, permissionList, PERMISSION_REQUEST_CODE);
			}
		}

		if (bbDeviceController == null) {
			listener = new MyBBDeviceControllerListener();
            spocListener = new MyBBDeviceControllerSpocListener();
            otaListener = new MyBBDeviceOTAControllerListener();
			bbDeviceController = BBDeviceController.getInstance(getApplicationContext(), listener);
			otaController = BBDeviceOTAController.getInstance(getApplicationContext(), otaListener);
            bbDeviceController.setSPoCListener(spocListener);
			BBDeviceController.setDebugLogEnabled(true);
			BBDeviceOTAController.setDebugLogEnabled(true);
			bbDeviceController.setDetectAudioDevicePlugged(true);
		}
		setTitle(getString(R.string.app_name) + " " + BBDeviceController.getApiVersion() + ", \nOTA : " + BBDeviceOTAController.getApiVersion());
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		//打印机onStop所需要的注销广播---------start
		unregisterReceiver(receiver);
		//打印机onStop所需要的注销广播---------end
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (isSwitchingActivity) {
			isSwitchingActivity = false;
		} else {
			if (bbDeviceController.getConnectionMode() == ConnectionMode.BLUETOOTH) {
				bbDeviceController.disconnectBT();
			}
		}
		//打印机onDestroy所需要的停用线程端口---------start
		DeviceConnFactoryManager.closeAllPort();
		if (threadPool != null) {
			threadPool.stopThreadPool();
			threadPool = null;
		}
		//打印机onDestroy所需要的停用线程端口---------end
	}

	//@Override
	@SuppressLint("Override")
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_CODE: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted.
				} else {
					// permission denied.
				}
				return;
			}
		}
	}

	public void dismissProgressDialog() {
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	public void dismissDialog() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	public String encrypt(String data, String key) {
		if (key.length() == 16) {
			key += key.substring(0, 8);
		}
		byte[] d = hexToByteArray(data);
		byte[] k = hexToByteArray(key);

		SecretKey sk = new SecretKeySpec(k, "DESede");
		try {
			Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, sk);
			byte[] enc = cipher.doFinal(d);
			return toHexString(enc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void onStart() {
		super.onStart();

		//打印机onStart方法中需要设置的广播----------start
		//注册接收连接状态的广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_QUERY_PRINTER_STATE);
		filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
		registerReceiver(receiver, filter);
		//打印机onStart方法中需要设置的广播----------end

		statusEditText.setText("BBDevice API : " + BBDeviceController.getApiVersion() + ", OTA : " + BBDeviceOTAController.getApiVersion());
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void injectNextSessionKey() {
		if (!encryptedPinSessionKey.equals("")) {
			Hashtable<String, String> data = new Hashtable<String, String>();
			data.put("index", "1");
			data.put("encSK", encryptedPinSessionKey);
			data.put("kcv", pinKcv);
			statusEditText.setText(getString(R.string.sending_encrypted_pin_session_key));
			encryptedPinSessionKey = "";
			bbDeviceController.injectSessionKey(data);
			return;
		}

		if (!encryptedDataSessionKey.equals("")) {
			Hashtable<String, String> data = new Hashtable<String, String>();
			data.put("index", "2");
			data.put("encSK", encryptedDataSessionKey);
			data.put("kcv", dataKcv);
			statusEditText.setText(getString(R.string.sending_encrypted_data_session_key));
			encryptedDataSessionKey = "";
			bbDeviceController.injectSessionKey(data);
			return;
		}

		if (!encryptedTrackSessionKey.equals("")) {
			Hashtable<String, String> data = new Hashtable<String, String>();
			data.put("index", "3");
			data.put("encSK", encryptedTrackSessionKey);
			data.put("kcv", trackKcv);
			statusEditText.setText(getString(R.string.sending_encrypted_track_session_key));
			encryptedTrackSessionKey = "";
			bbDeviceController.injectSessionKey(data);
			return;
		}

		if (!encryptedMacSessionKey.equals("")) {
			Hashtable<String, String> data = new Hashtable<String, String>();
			data.put("index", "4");
			data.put("encSK", encryptedMacSessionKey);
			data.put("kcv", macKcv);
			statusEditText.setText(getString(R.string.sending_encrypted_mac_session_key));
			encryptedMacSessionKey = "";
			bbDeviceController.injectSessionKey(data);
			return;
		}
	}

	public void stopConnection() {
		ConnectionMode connectionMode = bbDeviceController.getConnectionMode();
		if (connectionMode == ConnectionMode.BLUETOOTH) {
			bbDeviceController.disconnectBT();
		} else if (connectionMode == ConnectionMode.AUDIO) {
			bbDeviceController.stopAudio();
		} else if (connectionMode == ConnectionMode.SERIAL) {
			bbDeviceController.stopSerial();
		} else if (connectionMode == ConnectionMode.USB) {
			bbDeviceController.stopUsb();
		}
	}

	public void promptForConnection() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.connection_dialog);
		dialog.setTitle(getString(R.string.connection));
		dialog.setCanceledOnTouchOutside(false);

		String[] connections = new String[4];
		connections[0] = "Bluetooth";
		connections[1] = "Audio";
		connections[2] = "Serial";
		connections[3] = "USB";

		ListView listView = (ListView) dialog.findViewById(R.id.connectionList);
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, connections));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dismissDialog();
				if (position == 0) {
					if (checkBluetoothPermission()) {
						Object[] pairedObjects = BluetoothAdapter.getDefaultAdapter().getBondedDevices().toArray();
						final BluetoothDevice[] pairedDevices = new BluetoothDevice[pairedObjects.length];
						for (int i = 0; i < pairedObjects.length; ++i) {
							pairedDevices[i] = (BluetoothDevice) pairedObjects[i];
						}

						final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(currentActivity, android.R.layout.simple_list_item_1);
						for (int i = 0; i < pairedDevices.length; ++i) {
							mArrayAdapter.add(pairedDevices[i].getName());
						}

						dismissDialog();

						dialog = new Dialog(currentActivity);
						dialog.setContentView(R.layout.bluetooth_2_device_list_dialog);
						dialog.setTitle(R.string.bluetooth_devices);

						ListView listView1 = (ListView) dialog.findViewById(R.id.pairedDeviceList);
						listView1.setAdapter(mArrayAdapter);
						listView1.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
								statusEditText.setText(getString(R.string.connecting_bluetooth));
								posText.setText(getString(R.string.pos_connecting));
								bbDeviceController.connectBT(pairedDevices[position]);
								dismissDialog();
							}

						});

						arrayAdapter = new ArrayAdapter<String>(currentActivity, android.R.layout.simple_list_item_1);
						ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
						listView2.setAdapter(arrayAdapter);
						listView2.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

								statusEditText.setText(getString(R.string.connecting_bluetooth));
								posText.setText(getString(R.string.pos_connecting));
								bbDeviceController.connectBT(foundDevices.get(position));
								dismissDialog();
							}

						});

						dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								bbDeviceController.stopBTScan();
								dismissDialog();
							}
						});
						dialog.setCancelable(false);
						dialog.show();

						bbDeviceController.startBTScan(DEVICE_NAMES, 120);
					} else {
						setStatus(getString(R.string.bluetooth_permission_denied));
						dismissDialog();
					}
				} else if (position == 1) {
					bbDeviceController.startAudio();
				} else if (position == 2) {
					bbDeviceController.startSerial();
				} else if (position == 3) {
					bbDeviceController.startUsb();
				}
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismissDialog();
			}
		});

		dialog.show();
	}

	public void promptForAmount(final AmountInputType amountInputType,Hashtable<String, Object> data) {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.amount_dialog);
		dialog.setTitle(getString(R.string.set_amount));
		dialog.setCanceledOnTouchOutside(false);

		String[] symbols = new String[] { "DOLLAR", "RUPEE", "YEN", "POUND", "EURO", "WON", "DIRHAM", "RIYAL", "AED", "BS.", "YUAN", "NEW_SHEKEL", "DONG", "RUPIAH", "SOL", "PESO", "NULL" };
		((Spinner) dialog.findViewById(R.id.symbolSpinner)).setAdapter(new ArrayAdapter<String>(currentActivity, android.R.layout.simple_spinner_item, symbols));

		String[] transactionTypes = new String[] { "GOODS", "SERVICES", "CASHBACK", "INQUIRY", "TRANSFER", "PAYMENT", "REFUND", "CASH", "VOID", "REVERSAL" };
		((Spinner) dialog.findViewById(R.id.transactionTypeSpinner)).setAdapter(new ArrayAdapter<String>(currentActivity, android.R.layout.simple_spinner_item, transactionTypes));

		if (amountInputType == AmountInputType.TIPS_ONLY) {
			dialog.findViewById(R.id.amountEditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.cashbackAmountEditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.transactionTypeSpinner).setVisibility(View.GONE);
			dialog.findViewById(R.id.symbolSpinner).setVisibility(View.GONE);
		} else {
			dialog.findViewById(R.id.tipsAmountEditText).setVisibility(View.GONE);
		}

		dialog.findViewById(R.id.setButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String amount = ((EditText) (dialog.findViewById(R.id.amountEditText))).getText().toString();
				String cashbackAmount = ((EditText) (dialog.findViewById(R.id.cashbackAmountEditText))).getText().toString();
				String transactionTypeString = (String) ((Spinner) dialog.findViewById(R.id.transactionTypeSpinner)).getSelectedItem();
				String symbolString = (String) ((Spinner) dialog.findViewById(R.id.symbolSpinner)).getSelectedItem();
				String tipsAmount = ((EditText)(dialog.findViewById(R.id.tipsAmountEditText))).getText().toString();

				TransactionType transactionType = TransactionType.GOODS;
				if (transactionTypeString.equals("GOODS")) {
					transactionType = TransactionType.GOODS;
				} else if (transactionTypeString.equals("SERVICES")) {
					transactionType = TransactionType.SERVICES;
				} else if (transactionTypeString.equals("CASHBACK")) {
					transactionType = TransactionType.CASHBACK;
				} else if (transactionTypeString.equals("INQUIRY")) {
					transactionType = TransactionType.INQUIRY;
				} else if (transactionTypeString.equals("TRANSFER")) {
					transactionType = TransactionType.TRANSFER;
				} else if (transactionTypeString.equals("PAYMENT")) {
					transactionType = TransactionType.PAYMENT;
				} else if (transactionTypeString.equals("REFUND")) {
					transactionType = TransactionType.REFUND;
				} else if (transactionTypeString.equals("CASH")) {
					transactionType = TransactionType.CASH;
				} else if (transactionTypeString.equals("VOID")) {
					transactionType = TransactionType.VOID;
				} else if (transactionTypeString.equals("REVERSAL")) {
					transactionType = TransactionType.REVERSAL;
				}

				CurrencyCharacter[] currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.A, CurrencyCharacter.B, CurrencyCharacter.C };
				if (symbolString.equals("DOLLAR")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.DOLLAR };
				} else if (symbolString.equals("RUPEE")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.RUPEE };
				} else if (symbolString.equals("YEN")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.YEN };
				} else if (symbolString.equals("POUND")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.POUND };
				} else if (symbolString.equals("EURO")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.EURO };
				} else if (symbolString.equals("WON")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.WON };
				} else if (symbolString.equals("DIRHAM")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.DIRHAM };
				} else if (symbolString.equals("RIYAL")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.RIYAL, CurrencyCharacter.RIYAL_2 };
				} else if (symbolString.equals("AED")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.A, CurrencyCharacter.E, CurrencyCharacter.D };
				} else if (symbolString.equals("BS.")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.B, CurrencyCharacter.S, CurrencyCharacter.DOT };
				} else if (symbolString.equals("YUAN")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.YUAN };
				} else if (symbolString.equals("NEW_SHEKEL")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.NEW_SHEKEL };
				} else if (symbolString.equals("DONG")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.DONG };
				} else if (symbolString.equals("RUPIAH")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.RUPIAH };
				} else if (symbolString.equals("SOL")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.SOL };
				} else if (symbolString.equals("PESO")) {
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.PESO };
				} else if (symbolString.equals("NULL")) {
					currencyCharacters = null;
				}

				String currencyCode;
				if (Locale.getDefault().getCountry().equalsIgnoreCase("CN")) {
					currencyCode = "156";
					currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.YUAN };
				} else {
					currencyCode = "840";
				}

				if (amountInputType == AmountInputType.TIPS_ONLY) {
					Hashtable<String, Object> input = new Hashtable<String, Object>();
					input.put("tipsAmount", tipsAmount);
					input.put("currencyCode", currencyCode);
					if (bbDeviceController.setAmount(input)) {
						dismissDialog();
					} else {
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								promptForAmount(amountInputType,null);
							}
						}, 500);
					}
				} else {
					Hashtable<String, Object> input = new Hashtable<String, Object>();
					input.put("amount", amount);
					input.put("cashbackAmount", cashbackAmount);
					input.put("transactionType", transactionType);
					input.put("currencyCode", currencyCode);
					input.put("currencyCharacters", currencyCharacters);
					Intent intent = new Intent(BaseActivity.this,TransactionProcessAcitivity.class);
					intent.putExtra("amount",amount);
					intent.putExtra("startDate",data);
					intent.putExtra("startInput",input);
					intent.putExtra("amountInputType",amountInputType);
					startActivity(intent);
//					bbDeviceController.startEmv(data);
//					if (bbDeviceController.setAmount(input)) {
//						amountEditText.setText("$" + amount);
//						currentActivity.amount = amount;
//						currentActivity.cashbackAmount = cashbackAmount;
//						statusEditText.setText(getString(R.string.please_confirm_amount));
//						dismissDialog();
//					} else {
//						new Handler().postDelayed(new Runnable() {
//							@Override
//							public void run() {
//								promptForAmount(amountInputType,null);
//							}
//						}, 500);
//					}
				}
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				bbDeviceController.cancelSetAmount();
				dialog.dismiss();
			}

		});

		dialog.show();
	}
	
	public void promptForCheckCard() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.check_card_mode_dialog);
		dialog.setTitle(getString(R.string.select_mode));
		dialog.setCanceledOnTouchOutside(false);

		String[] swipeInsertTap = new String[7];
		swipeInsertTap[0] = getString(R.string.swipe_or_insert);
		swipeInsertTap[1] = getString(R.string.swipe);
		swipeInsertTap[2] = getString(R.string.insert);
		swipeInsertTap[3] = getString(R.string.tap);
		swipeInsertTap[4] = getString(R.string.swipe_or_tap);
		swipeInsertTap[5] = getString(R.string.insert_or_tap);
		swipeInsertTap[6] = getString(R.string.swipe_or_insert_or_tap);

		ListView listView = (ListView) dialog.findViewById(R.id.swipeInsertTapList);
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, swipeInsertTap));

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dismissDialog();
				if (position == 0) {
					checkCardMode = CheckCardMode.SWIPE_OR_INSERT;
					checkCard();
				} else if (position == 1) {
					checkCardMode = CheckCardMode.SWIPE;
					checkCard();
				} else if (position == 2) {
					checkCardMode = CheckCardMode.INSERT;
					checkCard();
				} else if (position == 3) {
					checkCardMode = CheckCardMode.TAP;
					startEmv();
				} else if (position == 4) {
					checkCardMode = CheckCardMode.SWIPE_OR_TAP;
					startEmv();
				} else if (position == 5) {
					checkCardMode = CheckCardMode.INSERT_OR_TAP;
					startEmv();
				} else if (position == 6) {
					checkCardMode = CheckCardMode.SWIPE_OR_INSERT_OR_TAP;
					startEmv();
				}
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismissDialog();
				statusEditText.setText("");
			}
		});

		dialog.show();
	}
	
	public void promptForStartEmv() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.check_card_mode_dialog);
		dialog.setTitle(getString(R.string.select_mode));
		dialog.setCanceledOnTouchOutside(false);

		String[] swipeInsertTap = new String[8];
		swipeInsertTap[0] = getString(R.string.swipe_or_insert);
		swipeInsertTap[1] = getString(R.string.swipe);
		swipeInsertTap[2] = getString(R.string.insert);
		swipeInsertTap[3] = getString(R.string.tap);
		swipeInsertTap[4] = getString(R.string.swipe_or_tap);
		swipeInsertTap[5] = getString(R.string.insert_or_tap);
		swipeInsertTap[6] = getString(R.string.swipe_or_insert_or_tap);
		swipeInsertTap[7] = getString(R.string.manual_pan_entry);

		ListView listView = (ListView) dialog.findViewById(R.id.swipeInsertTapList);
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, swipeInsertTap));

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dismissDialog();
				if (position == 0) {
					checkCardMode = CheckCardMode.SWIPE_OR_INSERT;
				} else if (position == 1) {
					checkCardMode = CheckCardMode.SWIPE;
				} else if (position == 2) {
					checkCardMode = CheckCardMode.INSERT;
				} else if (position == 3) {
					checkCardMode = CheckCardMode.TAP;
				} else if (position == 4) {
					checkCardMode = CheckCardMode.SWIPE_OR_TAP;
				} else if (position == 5) {
					checkCardMode = CheckCardMode.INSERT_OR_TAP;
				} else if (position == 6) {
					checkCardMode = CheckCardMode.SWIPE_OR_INSERT_OR_TAP;
				} else if (position == 7) {
					checkCardMode = CheckCardMode.MANUAL_PAN_ENTRY;
				}

				if (sessionData.isTipAmountTipsPercentageCashback()) {
					promptForStartEmvPart2();
					return;
				}

				startEmv();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismissDialog();
				statusEditText.setText("");
			}
		});

		dialog.show();
	}

	public void promptForStartEmvPart2() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_and_spinner);
		dialog.setTitle(getString(R.string.tips_amount_tips_percentage_cashback));

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("currencyCode");
		((TextView)(dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText("0840");

		dialog.findViewById(R.id.general2TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general2EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general2CheckBox).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general3TextView))).setText("currencyExponent");
		((TextView)(dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general3EditText))).setText("2");

		((TextView)(dialog.findViewById(R.id.general4TextView))).setText("inputAmountOption");
		((TextView)(dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general4EditText))).setText("0");

		((TextView)(dialog.findViewById(R.id.general5TextView))).setText("tipsAmount1");
		((TextView)(dialog.findViewById(R.id.general5TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general5EditText))).setText("3.45");

		((TextView)(dialog.findViewById(R.id.general6TextView))).setText("tipsAmount2");
		((TextView)(dialog.findViewById(R.id.general6TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general6EditText))).setText("5.67");

		((TextView)(dialog.findViewById(R.id.general7TextView))).setText("tipsAmount3");
		((TextView)(dialog.findViewById(R.id.general7TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general7EditText))).setText("8.90");

		((TextView)(dialog.findViewById(R.id.general8TextView))).setText("tipsPercentage1");
		((TextView)(dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general8EditText))).setText("4");

		((TextView)(dialog.findViewById(R.id.general11TextView))).setText("tipsPercentage2");
		((TextView)(dialog.findViewById(R.id.general11TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general11EditText))).setText("8");

		((TextView)(dialog.findViewById(R.id.general12TextView))).setText("tipsPercentage3");
		((TextView)(dialog.findViewById(R.id.general12TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general12EditText))).setText("16");

		((TextView)(dialog.findViewById(R.id.general13TextView))).setText("cashback1");
		((TextView)(dialog.findViewById(R.id.general13TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general13EditText))).setText("10");

		((TextView)(dialog.findViewById(R.id.general14TextView))).setText("cashback2");
		((TextView)(dialog.findViewById(R.id.general14TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general14EditText))).setText("20");

		((TextView)(dialog.findViewById(R.id.general15TextView))).setText("cashback3");
		((TextView)(dialog.findViewById(R.id.general15TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general15EditText))).setText("40");

		dialog.findViewById(R.id.general16TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general17TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general18TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general19TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general20TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general20EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general20CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general24TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general25TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general26TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26CheckBox).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("otherAmountOption ");
		((Spinner)dialog.findViewById(R.id.general9Spinner)).setAdapter(new ArrayAdapter<BBDeviceController.OtherAmountOption>(BaseActivity.this, android.R.layout.simple_spinner_item, BBDeviceController.OtherAmountOption.values()));

		dialog.findViewById(R.id.general10CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general21CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general21TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general21Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general22CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general23CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).isChecked()) {
					sessionData.setCurrencyCodeForInputAmountOptionExist(true);
					sessionData.setCurrencyCodeForInputAmountOption(((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());
				} else {
					sessionData.setCurrencyCodeForInputAmountOptionExist(false);
					sessionData.setCurrencyCodeForInputAmountOption("");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general3CheckBox))).isChecked()) {
					sessionData.setCurrencyExponentForInputAmountOptionExist(true);
					sessionData.setCurrencyExponentForInputAmountOption(((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString());
				} else {
					sessionData.setCurrencyExponentForInputAmountOptionExist(false);
					sessionData.setCurrencyExponentForInputAmountOption("");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general4CheckBox))).isChecked()) {
					sessionData.setInputAmountOptionExist(true);
					sessionData.setInputAmountOption(Integer.parseInt(((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString()));
				} else {
					sessionData.setInputAmountOptionExist(false);
					sessionData.setInputAmountOption(0);
				}

				if ((((CheckBox)(dialog.findViewById(R.id.general5CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general6CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general7CheckBox))).isChecked())) {
					List<String> tipsAmountList = new ArrayList<>();
					if (((CheckBox) (dialog.findViewById(R.id.general5CheckBox))).isChecked()) {
						tipsAmountList.add(((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general6CheckBox))).isChecked()) {
						tipsAmountList.add(((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general7CheckBox))).isChecked()) {
						tipsAmountList.add(((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString());
					}
					sessionData.setTipsAmountOptionsExist(true);
					sessionData.setTipsAmountOptions(tipsAmountList.toArray(new String[0]));
				} else {
					sessionData.setTipsAmountOptionsExist(false);
					sessionData.setTipsAmountOptions(new String[0]);
				}

				if ((((CheckBox)(dialog.findViewById(R.id.general8CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general11CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).isChecked())) {
					List<String> tipsPercentageList = new ArrayList<>();
					if (((CheckBox) (dialog.findViewById(R.id.general8CheckBox))).isChecked()) {
						tipsPercentageList.add(((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general11CheckBox))).isChecked()) {
						tipsPercentageList.add(((EditText) (dialog.findViewById(R.id.general11EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general12CheckBox))).isChecked()) {
						tipsPercentageList.add(((EditText) (dialog.findViewById(R.id.general12EditText))).getText().toString());
					}
					sessionData.setTipsPercentageOptionsExist(true);
					sessionData.setTipsPercentageOptions(tipsPercentageList.toArray(new String[0]));
				} else {
					sessionData.setTipsPercentageOptionsExist(false);
					sessionData.setTipsPercentageOptions(new String[0]);
				}

				if ((((CheckBox)(dialog.findViewById(R.id.general13CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general14CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general15CheckBox))).isChecked())) {
					List<String> cashbackList = new ArrayList<>();
					if (((CheckBox) (dialog.findViewById(R.id.general13CheckBox))).isChecked()) {
						cashbackList.add(((EditText) (dialog.findViewById(R.id.general13EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general14CheckBox))).isChecked()) {
						cashbackList.add(((EditText) (dialog.findViewById(R.id.general14EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general15CheckBox))).isChecked()) {
						cashbackList.add(((EditText) (dialog.findViewById(R.id.general15EditText))).getText().toString());
					}
					sessionData.setCashbackAmountOptionsExist(true);
					sessionData.setCashbackAmountOptions(cashbackList.toArray(new String[0]));
				} else {
					sessionData.setCashbackAmountOptionsExist(false);
					sessionData.setCashbackAmountOptions(new String[0]);
				}

				if (((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).isChecked()) {
					sessionData.setFlagHasOtherAmountOption(true);
					sessionData.setOtherAmountOption((BBDeviceController.OtherAmountOption)((Spinner)dialog.findViewById(R.id.general9Spinner)).getSelectedItem());
				} else {
					sessionData.setFlagHasOtherAmountOption(false);
					sessionData.setOtherAmountOption(BBDeviceController.OtherAmountOption.CURRENCY);
				}

				startEmv();
				dialog.dismiss();
			}
		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}
	
	public void promptForGprs() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.gprs_dialog);
		dialog.setTitle(getString(R.string.gprs));
		dialog.setCanceledOnTouchOutside(false);

		((EditText) (dialog.findViewById(R.id.gprsOperatorEditText))).setText("CSL");
		((EditText) (dialog.findViewById(R.id.gprsAPNEditText))).setText("hkcsl");
		((EditText) (dialog.findViewById(R.id.gprsUsernameEditText))).setText("guest");
		((EditText) (dialog.findViewById(R.id.gprsPasswordEditText))).setText("guest");

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String gprsOperator = ((EditText) (dialog.findViewById(R.id.gprsOperatorEditText))).getText().toString();
				String gprsAPN = ((EditText) (dialog.findViewById(R.id.gprsAPNEditText))).getText().toString();
				String gprsUsername = ((EditText) (dialog.findViewById(R.id.gprsUsernameEditText))).getText().toString();
				String gprsPassword = ((EditText) (dialog.findViewById(R.id.gprsPasswordEditText))).getText().toString();
				
				Hashtable<String, String> gprsData = new Hashtable<String, String>();
				gprsData.put("operator", gprsOperator);
				gprsData.put("apn", gprsAPN);
				gprsData.put("username", gprsUsername);
				gprsData.put("password", gprsPassword);
				
				bbDeviceController.updateGprsSettings(gprsData);

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}
	
	public void promptForWifi() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.wifi_dialog);
		dialog.setTitle(getString(R.string.wifi));
		dialog.setCanceledOnTouchOutside(false);

		((EditText) (dialog.findViewById(R.id.wifiSSIDEditText))).setText("BBPOS_AP");
		((EditText) (dialog.findViewById(R.id.wifiPasswordEditText))).setText("bb1904@AP");
		((EditText) (dialog.findViewById(R.id.wifiUrlEditText))).setText("ws://chip.mswipetech.com/mswipeGWG2/TxHandlerG2.ashx");
		((EditText) (dialog.findViewById(R.id.wifiPortNumberEditText))).setText("8080");

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String wifiSSID = ((EditText) (dialog.findViewById(R.id.wifiSSIDEditText))).getText().toString();
				String wifiPassword = ((EditText) (dialog.findViewById(R.id.wifiPasswordEditText))).getText().toString();
				String wifiUrl = ((EditText) (dialog.findViewById(R.id.wifiUrlEditText))).getText().toString();
				String wifiPortNumber = ((EditText) (dialog.findViewById(R.id.wifiPortNumberEditText))).getText().toString();
				
				Hashtable<String, String> wifiData = new Hashtable<String, String>();
				if (((CheckBox)(dialog.findViewById(R.id.enableWifiSSIDCheckBox))).isChecked()) {
					wifiData.put("ssid", wifiSSID);
				}
				
				if (((CheckBox)(dialog.findViewById(R.id.enableWifiPasswordCheckBox))).isChecked()) {
					wifiData.put("password", wifiPassword);
				}
				
				if (((CheckBox)(dialog.findViewById(R.id.enableWifiUrlCheckBox))).isChecked()) {
					wifiData.put("url", wifiUrl);
				}
				
				if (((CheckBox)(dialog.findViewById(R.id.enableWifiPortNumberCheckBox))).isChecked()) {
					wifiData.put("portNumber", wifiPortNumber);
				}
				
				bbDeviceController.updateWiFiSettings(wifiData);

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}	
	
	public void promptForInitSession() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.init_session));
		dialog.setCanceledOnTouchOutside(false);
		
		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("Vendor Token");
		((EditText) (dialog.findViewById(R.id.general1EditText))).setText("2BC1EF345F564C7C");
		
		((TextView)(dialog.findViewById(R.id.general2TextView))).setVisibility(View.GONE);
		((EditText) (dialog.findViewById(R.id.general2EditText))).setVisibility(View.GONE);
		
		((TextView)(dialog.findViewById(R.id.general3TextView))).setVisibility(View.GONE);
		((EditText) (dialog.findViewById(R.id.general3EditText))).setVisibility(View.GONE);
		
		((TextView)(dialog.findViewById(R.id.general4TextView))).setVisibility(View.GONE);
		((EditText) (dialog.findViewById(R.id.general4EditText))).setVisibility(View.GONE);
		
		((TextView)(dialog.findViewById(R.id.general5TextView))).setVisibility(View.GONE);
		((EditText) (dialog.findViewById(R.id.general5EditText))).setVisibility(View.GONE);
		
		((TextView)(dialog.findViewById(R.id.general6TextView))).setVisibility(View.GONE);
		((EditText) (dialog.findViewById(R.id.general6EditText))).setVisibility(View.GONE);
		
		((TextView)(dialog.findViewById(R.id.general7TextView))).setVisibility(View.GONE);
		((EditText) (dialog.findViewById(R.id.general7EditText))).setVisibility(View.GONE);
		
		((TextView)(dialog.findViewById(R.id.general8TextView))).setVisibility(View.GONE);
		((EditText) (dialog.findViewById(R.id.general8EditText))).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				bbDeviceController.initSession(((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());
				
				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}
	
	public void promptForStartNfcDetection() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.start_nfc_detection));
		dialog.setCanceledOnTouchOutside(false);
		
		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("NFC card detection timeout");
		dialog.findViewById(R.id.general1TextView).getLayoutParams().width = 500;
		((EditText) (dialog.findViewById(R.id.general1EditText))).setText("15");
		
		((TextView)(dialog.findViewById(R.id.general2TextView))).setText("NFC Operation Mode");
		dialog.findViewById(R.id.general2TextView).getLayoutParams().width = 500;
		((EditText) (dialog.findViewById(R.id.general2EditText))).setText("3");
				
		dialog.findViewById(R.id.general3TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general6TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Hashtable<String, Object> data = new Hashtable<String, Object>();
				String nfcCardDetectionTimeout = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
				if ((nfcCardDetectionTimeout != null) && (!nfcCardDetectionTimeout.equalsIgnoreCase(""))) {
					data.put("nfcCardDetectionTimeout", nfcCardDetectionTimeout);
				}
				String nfcOperationMode = ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString();
				if ((nfcOperationMode != null) && (!nfcOperationMode.equalsIgnoreCase(""))) {
					data.put("nfcOperationMode", nfcOperationMode);
				}
				bbDeviceController.startNfcDetection(data);
				
				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForStartNfcDetectionAppleVasStep1() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_and_radio_box_dialog);
		dialog.setTitle(getString(R.string.start_nfc_detection_apple_vas));
		dialog.setCanceledOnTouchOutside(false);

		vasTerminalMode = BBDeviceController.VASTerminalMode.SINGLE;
		vasList = new ArrayList<VASMerchantConfig>();

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("nfcCardDetectionTimeout");
		dialog.findViewById(R.id.general1TextView).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText(vasNFCCardDetectionTimeout);

		((TextView)(dialog.findViewById(R.id.general2TextView))).setText("nfcOperationMode");
		dialog.findViewById(R.id.general2TextView).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general2EditText))).setText(vasNFCOperationMode);

		dialog.findViewById(R.id.general3TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general6TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);

		((RadioButton)(dialog.findViewById(R.id.radio1Button))).setText("VAS_MODE");
		((RadioButton)(dialog.findViewById(R.id.radio2Button))).setText("DUAL_MODE");
		((RadioButton)(dialog.findViewById(R.id.radio3Button))).setText("SINGLE_MODE");
		((RadioButton)(dialog.findViewById(R.id.radio4Button))).setText("PAYMENT_MODE");

		dialog.findViewById(R.id.general10TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.confirmButton).setVisibility(View.GONE);

		dialog.findViewById(R.id.nextButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String nfcCardDetectionTimeout = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
				if ((nfcCardDetectionTimeout != null) && (!nfcCardDetectionTimeout.equalsIgnoreCase(""))) {
					vasNFCCardDetectionTimeout = nfcCardDetectionTimeout;
				}
				String nfcOperationMode = ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString();
				if ((nfcOperationMode != null) && (!nfcOperationMode.equalsIgnoreCase(""))) {
					vasNFCOperationMode = nfcOperationMode;
				}

				if (((RadioButton)(dialog.findViewById(R.id.radio1Button))).isChecked()) {
					vasTerminalMode = BBDeviceController.VASTerminalMode.VAS;
				} else if (((RadioButton)(dialog.findViewById(R.id.radio2Button))).isChecked()) {
					vasTerminalMode = BBDeviceController.VASTerminalMode.DUAL;
				} else if (((RadioButton)(dialog.findViewById(R.id.radio3Button))).isChecked()) {
					vasTerminalMode = BBDeviceController.VASTerminalMode.SINGLE;
				} else if (((RadioButton)(dialog.findViewById(R.id.radio4Button))).isChecked()) {
					vasTerminalMode = BBDeviceController.VASTerminalMode.PAYMENT;
				}

				promptForStartNfcDetectionAppleVasStep2();
			}
		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForStartNfcDetectionAppleVasStep2() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_and_radio_box_dialog);
		dialog.setTitle(getString(R.string.start_nfc_detection_apple_vas));
		dialog.setCanceledOnTouchOutside(false);

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("merchantID");
		dialog.findViewById(R.id.general1TextView).getLayoutParams().width = 250;
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText("pass.com.xxx.yyy");

		((TextView)(dialog.findViewById(R.id.general2TextView))).setText("url");
		dialog.findViewById(R.id.general2TextView).getLayoutParams().width = 250;
		((EditText)(dialog.findViewById(R.id.general2EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general3TextView))).setText("filter");
		dialog.findViewById(R.id.general3TextView).getLayoutParams().width = 250;
		((EditText)(dialog.findViewById(R.id.general3EditText))).setText("");

		dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general6TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);

		((RadioButton)(dialog.findViewById(R.id.radio1Button))).setText("URL");
		((RadioButton)(dialog.findViewById(R.id.radio2Button))).setText("FULL");
		dialog.findViewById(R.id.radio3Button).setVisibility(View.GONE);
		dialog.findViewById(R.id.radio4Button).setVisibility(View.GONE);

		dialog.findViewById(R.id.general10TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				VASMerchantConfig vasObj = new VASMerchantConfig();
				vasObj.merchantID = ((EditText)(dialog.findViewById(R.id.general1EditText))).getText().toString();
				vasObj.url = ((EditText)(dialog.findViewById(R.id.general2EditText))).getText().toString();
				vasObj.filter = ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString();

				if (((RadioButton)(dialog.findViewById(R.id.radio1Button))).isChecked()) {
					vasObj.protocolMode = BBDeviceController.VASProtocolMode.URL;
				} else if (((RadioButton)(dialog.findViewById(R.id.radio2Button))).isChecked()) {
					vasObj.protocolMode = BBDeviceController.VASProtocolMode.FULL;
				}

				vasList.add(vasObj);

				Hashtable<String, Object> data = new Hashtable<String, Object>();
				if ((vasNFCCardDetectionTimeout != null) && (!vasNFCCardDetectionTimeout.equalsIgnoreCase(""))) {
					data.put("nfcCardDetectionTimeout", vasNFCCardDetectionTimeout);
				}
				if ((vasNFCOperationMode != null) && (!vasNFCOperationMode.equalsIgnoreCase(""))) {
					data.put("nfcOperationMode", vasNFCOperationMode);
				}
				data.put("vasTerminalMode", vasTerminalMode);
				data.put("vasMerchantConfigs", vasList);
				bbDeviceController.startNfcDetection(data);

				dialog.dismiss();
			}
		});

		dialog.findViewById(R.id.nextButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				VASMerchantConfig vasObj = new VASMerchantConfig();
				vasObj.merchantID = ((EditText)(dialog.findViewById(R.id.general1EditText))).getText().toString();
				vasObj.url = ((EditText)(dialog.findViewById(R.id.general2EditText))).getText().toString();
				vasObj.filter = ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString();

				if (((RadioButton)(dialog.findViewById(R.id.radio1Button))).isChecked()) {
					vasObj.protocolMode = BBDeviceController.VASProtocolMode.URL;
				} else if (((RadioButton)(dialog.findViewById(R.id.radio2Button))).isChecked()) {
					vasObj.protocolMode = BBDeviceController.VASProtocolMode.FULL;
				}

				vasList.add(vasObj);

				promptForStartNfcDetectionAppleVasStep2();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}
	
	public void promptForStopNfcDetection() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.stop_nfc_detection));
		dialog.setCanceledOnTouchOutside(false);
		
		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("NFC card removal timeout");
		dialog.findViewById(R.id.general1TextView).getLayoutParams().width = 500;
		((EditText) (dialog.findViewById(R.id.general1EditText))).setText("15");
		
		dialog.findViewById(R.id.general2TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general2EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general3TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general6TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Hashtable<String, Object> data = new Hashtable<String, Object>();
				String nfcCardRemovalTimeout = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
				if ((nfcCardRemovalTimeout != null) && (!nfcCardRemovalTimeout.equalsIgnoreCase(""))) {
					data.put("nfcCardRemovalTimeout", ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());
				}
				bbDeviceController.stopNfcDetection(data);
				
				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForNfcDataExchangeWrite() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.nfc_data_exchange_write));
		dialog.setCanceledOnTouchOutside(false);

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("Write NDEF Record");
		((TextView)(dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText("1234567890");

		((TextView)(dialog.findViewById(R.id.general2TextView))).setVisibility(View.GONE);
		((EditText)(dialog.findViewById(R.id.general2EditText))).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general3TextView))).setText("Mifare Card Key");
		((TextView)(dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 500;
		//((EditText)(dialog.findViewById(R.id.general3EditText))).setText("FFFFFFFFFFFF");
		((EditText)(dialog.findViewById(R.id.general3EditText))).setText("0000000000000000");

		((TextView)(dialog.findViewById(R.id.general4TextView))).setText("Mifare Card Key Number");
		((TextView)(dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general4EditText))).setText("00");

		((TextView)(dialog.findViewById(R.id.general5TextView))).setText("Mifare Card Block Number");
		((TextView)(dialog.findViewById(R.id.general5TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general5EditText))).setText("02");

		((TextView)(dialog.findViewById(R.id.general6TextView))).setText("Mifare Desfire Card AID");
		((TextView)(dialog.findViewById(R.id.general6TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general6EditText))).setText("01");

		((TextView)(dialog.findViewById(R.id.general7TextView))).setText("Mifare Desfire Card FID");
		((TextView)(dialog.findViewById(R.id.general7TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general7EditText))).setText("00");

		((TextView)(dialog.findViewById(R.id.general8TextView))).setText("Mifare Desfire Card File Offset");
		((TextView)(dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general8EditText))).setText("00");

		((TextView)(dialog.findViewById(R.id.general9TextView))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general9EditText))).setVisibility(View.VISIBLE);
		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("Mifare Card Data Length");
		((TextView)(dialog.findViewById(R.id.general9TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general9EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general10TextView))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general10EditText))).setVisibility(View.VISIBLE);
		((TextView)(dialog.findViewById(R.id.general10TextView))).setText("Mifare Card Command");
		((TextView)(dialog.findViewById(R.id.general10TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general10EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general11TextView))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general11EditText))).setVisibility(View.VISIBLE);
		((TextView)(dialog.findViewById(R.id.general11TextView))).setText("Mifare Card Command Data");
		((TextView)(dialog.findViewById(R.id.general11TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general11EditText))).setText("");

		((LinearLayout)(dialog.findViewById(R.id.general1LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general2LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general3LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general4LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general5LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general6LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general7LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general8LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general9LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general10LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general11LinearLayout))).setOrientation(LinearLayout.VERTICAL);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Hashtable<String, Object> data = new Hashtable<String, Object>();
				String writeNdefRecord = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
				if ((writeNdefRecord != null) && (!writeNdefRecord.equalsIgnoreCase(""))) {
					data.put("writeNdefRecord", Utils.encodeNdefFormat(writeNdefRecord));
				}
				String mifareCardKey = ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString();
				if ((mifareCardKey != null) && (!mifareCardKey.equalsIgnoreCase(""))) {
					data.put("mifareCardKey", mifareCardKey);
				}
				String mifareCardKeyNumber = ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString();
				if ((mifareCardKeyNumber != null) && (!mifareCardKeyNumber.equalsIgnoreCase(""))) {
					data.put("mifareCardKeyNumber", mifareCardKeyNumber);
				}
				String mifareCardBlockNumber = ((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString();
				if ((mifareCardBlockNumber != null) && (!mifareCardBlockNumber.equalsIgnoreCase(""))) {
					data.put("mifareCardBlockNumber", mifareCardBlockNumber);
				}
				String mifareDesFireCardAID = ((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString();
				if ((mifareDesFireCardAID != null) && (!mifareDesFireCardAID.equalsIgnoreCase(""))) {
					data.put("mifareDesFireCardAID", mifareDesFireCardAID);
				}
				String mifareDesFireCardFID = ((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString();
				if ((mifareDesFireCardFID != null) && (!mifareDesFireCardFID.equalsIgnoreCase(""))) {
					data.put("mifareDesFireCardFID", mifareDesFireCardFID);
				}
				String mifareDesFireCardFileOffset = ((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString();
				if ((mifareDesFireCardFileOffset != null) && (!mifareDesFireCardFileOffset.equalsIgnoreCase(""))) {
					data.put("mifareDesFireCardFileOffset", mifareDesFireCardFileOffset);
				}
				String mifareCardDataLength = ((EditText) (dialog.findViewById(R.id.general9EditText))).getText().toString();
				if ((mifareCardDataLength != null) && (!mifareCardDataLength.equalsIgnoreCase(""))) {
					data.put("mifareCardDataLength", mifareCardDataLength);
				}
				String mifareCardCommand = ((EditText) (dialog.findViewById(R.id.general10EditText))).getText().toString();
				if ((mifareCardCommand != null) && (!mifareCardCommand.equalsIgnoreCase(""))) {
					data.put("mifareCardCommand", mifareCardCommand);
				}
				String mifareCardCommandData = ((EditText) (dialog.findViewById(R.id.general11EditText))).getText().toString();
				if ((mifareCardCommandData != null) && (!mifareCardCommandData.equalsIgnoreCase(""))) {
					data.put("mifareCardCommandData", mifareCardCommandData);
				}
				bbDeviceController.nfcDataExchange(data);

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForNfcDataExchangeRead1St() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.nfc_data_exchange_read_1st));
		dialog.setCanceledOnTouchOutside(false);

		((TextView)(dialog.findViewById(R.id.general1TextView))).setVisibility(View.GONE);
		((EditText)(dialog.findViewById(R.id.general1EditText))).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general2TextView))).setVisibility(View.GONE);
		((EditText)(dialog.findViewById(R.id.general2EditText))).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general3TextView))).setText("Mifare Card Key");
		((TextView)(dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general3EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general4TextView))).setText("Mifare Card Key Number");
		((TextView)(dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general4EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general5TextView))).setText("Mifare Card Block Number");
		((TextView)(dialog.findViewById(R.id.general5TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general5EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general6TextView))).setText("Mifare Desfire Card AID");
		((TextView)(dialog.findViewById(R.id.general6TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general6EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general7TextView))).setText("Mifare Desfire Card FID");
		((TextView)(dialog.findViewById(R.id.general7TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general7EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general8TextView))).setText("Mifare Desfire Card File Offset");
		((TextView)(dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general8EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general9TextView))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general9EditText))).setVisibility(View.VISIBLE);
		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("Mifare Card Data Length");
		((TextView)(dialog.findViewById(R.id.general9TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general9EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general10TextView))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general10EditText))).setVisibility(View.VISIBLE);
		((TextView)(dialog.findViewById(R.id.general10TextView))).setText("Mifare Card Command");
		((TextView)(dialog.findViewById(R.id.general10TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general10EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general11TextView))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general11EditText))).setVisibility(View.VISIBLE);
		((TextView)(dialog.findViewById(R.id.general11TextView))).setText("Mifare Card Command Data");
		((TextView)(dialog.findViewById(R.id.general11TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general11EditText))).setText("");

		((LinearLayout)(dialog.findViewById(R.id.general1LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general2LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general3LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general4LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general5LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general6LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general7LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general8LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general9LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general10LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general11LinearLayout))).setOrientation(LinearLayout.VERTICAL);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Hashtable<String, Object> data = new Hashtable<String, Object>();
				data.put("readNdefRecord", ReadNdefRecord.READ_1ST);
				String mifareCardKey = ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString();
				if ((mifareCardKey != null) && (!mifareCardKey.equalsIgnoreCase(""))) {
					data.put("mifareCardKey", mifareCardKey);
				}
				String mifareCardKeyNumber = ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString();
				if ((mifareCardKeyNumber != null) && (!mifareCardKeyNumber.equalsIgnoreCase(""))) {
					data.put("mifareCardKeyNumber", mifareCardKeyNumber);
				}
				String mifareCardBlockNumber = ((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString();
				if ((mifareCardBlockNumber != null) && (!mifareCardBlockNumber.equalsIgnoreCase(""))) {
					data.put("mifareCardBlockNumber", mifareCardBlockNumber);
				}
				String mifareDesFireCardAID = ((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString();
				if ((mifareDesFireCardAID != null) && (!mifareDesFireCardAID.equalsIgnoreCase(""))) {
					data.put("mifareDesFireCardAID", mifareDesFireCardAID);
				}
				String mifareDesFireCardFID = ((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString();
				if ((mifareDesFireCardFID != null) && (!mifareDesFireCardFID.equalsIgnoreCase(""))) {
					data.put("mifareDesFireCardFID", mifareDesFireCardFID);
				}
				String mifareDesFireCardFileOffset = ((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString();
				if ((mifareDesFireCardFileOffset != null) && (!mifareDesFireCardFileOffset.equalsIgnoreCase(""))) {
					data.put("mifareDesFireCardFileOffset", mifareDesFireCardFileOffset);
				}
				String mifareCardDataLength = ((EditText) (dialog.findViewById(R.id.general9EditText))).getText().toString();
				if ((mifareCardDataLength != null) && (!mifareCardDataLength.equalsIgnoreCase(""))) {
					data.put("mifareCardDataLength", mifareCardDataLength);
				}
				String mifareCardCommand = ((EditText) (dialog.findViewById(R.id.general10EditText))).getText().toString();
				if ((mifareCardCommand != null) && (!mifareCardCommand.equalsIgnoreCase(""))) {
					data.put("mifareCardCommand", mifareCardCommand);
				}
				String mifareCardCommandData = ((EditText) (dialog.findViewById(R.id.general11EditText))).getText().toString();
				if ((mifareCardCommandData != null) && (!mifareCardCommandData.equalsIgnoreCase(""))) {
					data.put("mifareCardCommandData", mifareCardCommandData);
				}
				bbDeviceController.nfcDataExchange(data);

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForNfcDataExchangeReadNext() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.nfc_data_exchange_read_next));
		dialog.setCanceledOnTouchOutside(false);

		((TextView)(dialog.findViewById(R.id.general1TextView))).setVisibility(View.GONE);
		((EditText)(dialog.findViewById(R.id.general1EditText))).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general2TextView))).setVisibility(View.GONE);
		((EditText)(dialog.findViewById(R.id.general2EditText))).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general3TextView))).setText("Mifare Card Key");
		((TextView)(dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general3EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general4TextView))).setText("Mifare Card Key Number");
		((TextView)(dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general4EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general5TextView))).setText("Mifare Card Block Number");
		((TextView)(dialog.findViewById(R.id.general5TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general5EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general6TextView))).setText("Mifare Desfire Card AID");
		((TextView)(dialog.findViewById(R.id.general6TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general6EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general7TextView))).setText("Mifare Desfire Card FID");
		((TextView)(dialog.findViewById(R.id.general7TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general7EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general8TextView))).setText("Mifare Desfire Card File Offset");
		((TextView)(dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general8EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general9TextView))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general9EditText))).setVisibility(View.VISIBLE);
		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("Mifare Card Data Length");
		((TextView)(dialog.findViewById(R.id.general9TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general9EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general10TextView))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general10EditText))).setVisibility(View.VISIBLE);
		((TextView)(dialog.findViewById(R.id.general10TextView))).setText("Mifare Card Command");
		((TextView)(dialog.findViewById(R.id.general10TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general10EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general11TextView))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general11EditText))).setVisibility(View.VISIBLE);
		((TextView)(dialog.findViewById(R.id.general11TextView))).setText("Mifare Card Command Data");
		((TextView)(dialog.findViewById(R.id.general11TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general11EditText))).setText("");

		((LinearLayout)(dialog.findViewById(R.id.general1LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general2LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general3LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general4LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general5LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general6LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general7LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general8LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general9LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general10LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general11LinearLayout))).setOrientation(LinearLayout.VERTICAL);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Hashtable<String, Object> data = new Hashtable<String, Object>();
				data.put("readNdefRecord", ReadNdefRecord.READ_NEXT);
				String mifareCardKey = ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString();
				if ((mifareCardKey != null) && (!mifareCardKey.equalsIgnoreCase(""))) {
					data.put("mifareCardKey", mifareCardKey);
				}
				String mifareCardKeyNumber = ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString();
				if ((mifareCardKeyNumber != null) && (!mifareCardKeyNumber.equalsIgnoreCase(""))) {
					data.put("mifareCardKeyNumber", mifareCardKeyNumber);
				}
				String mifareCardBlockNumber = ((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString();
				if ((mifareCardBlockNumber != null) && (!mifareCardBlockNumber.equalsIgnoreCase(""))) {
					data.put("mifareCardBlockNumber", mifareCardBlockNumber);
				}
				String mifareDesFireCardAID = ((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString();
				if ((mifareDesFireCardAID != null) && (!mifareDesFireCardAID.equalsIgnoreCase(""))) {
					data.put("mifareDesFireCardAID", mifareDesFireCardAID);
				}
				String mifareDesFireCardFID = ((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString();
				if ((mifareDesFireCardFID != null) && (!mifareDesFireCardFID.equalsIgnoreCase(""))) {
					data.put("mifareDesFireCardFID", mifareDesFireCardFID);
				}
				String mifareDesFireCardFileOffset = ((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString();
				if ((mifareDesFireCardFileOffset != null) && (!mifareDesFireCardFileOffset.equalsIgnoreCase(""))) {
					data.put("mifareDesFireCardFileOffset", mifareDesFireCardFileOffset);
				}
				String mifareCardDataLength = ((EditText) (dialog.findViewById(R.id.general9EditText))).getText().toString();
				if ((mifareCardDataLength != null) && (!mifareCardDataLength.equalsIgnoreCase(""))) {
					data.put("mifareCardDataLength", mifareCardDataLength);
				}
				String mifareCardCommand = ((EditText) (dialog.findViewById(R.id.general10EditText))).getText().toString();
				if ((mifareCardCommand != null) && (!mifareCardCommand.equalsIgnoreCase(""))) {
					data.put("mifareCardCommand", mifareCardCommand);
				}
				String mifareCardCommandData = ((EditText) (dialog.findViewById(R.id.general11EditText))).getText().toString();
				if ((mifareCardCommandData != null) && (!mifareCardCommandData.equalsIgnoreCase(""))) {
					data.put("mifareCardCommandData", mifareCardCommandData);
				}
				bbDeviceController.nfcDataExchange(data);

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForReadTerminalSetting() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.read_terminal_setting));

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("tag");
		((EditText) (dialog.findViewById(R.id.general1EditText))).setText("");

		dialog.findViewById(R.id.general2TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general2EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general3TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general6TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				bbDeviceController.readTerminalSetting(((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForUpdateTerminalSettings() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.update_terminal_settings));

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("tlv");
		((EditText) (dialog.findViewById(R.id.general1EditText))).setText("");

		dialog.findViewById(R.id.general2TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general2EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general3TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general6TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				bbDeviceController.updateTerminalSettings(((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}
	
	public void promptForReadAID() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.read_aid));
		dialog.setCanceledOnTouchOutside(false);
		
		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("appIndex");
		dialog.findViewById(R.id.general1TextView).getLayoutParams().width = 500;
		((EditText) (dialog.findViewById(R.id.general1EditText))).setText("00000001");
				
		dialog.findViewById(R.id.general2TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general2EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general3TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general6TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6EditText).setVisibility(View.GONE);
				
		dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);
		
		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				bbDeviceController.readAID(((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());
				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForUpdateAID() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_with_checkbox_dialog);
		dialog.setTitle(getString(R.string.update_aid));

		((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).setText("appIndex");
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText("00000001");

		((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).setText("aid");
		((EditText)(dialog.findViewById(R.id.general2EditText))).setText("");

		((CheckBox)(dialog.findViewById(R.id.general3CheckBox))).setText("appVersion");
		((EditText)(dialog.findViewById(R.id.general3EditText))).setText("008C");

		((CheckBox)(dialog.findViewById(R.id.general4CheckBox))).setText("terminalFloorLimit");
		((EditText)(dialog.findViewById(R.id.general4EditText))).setText("00000000");

		((CheckBox)(dialog.findViewById(R.id.general5CheckBox))).setText("contactTACDefault");
		((EditText)(dialog.findViewById(R.id.general5EditText))).setText("DC4000A800");

		((CheckBox)(dialog.findViewById(R.id.general6CheckBox))).setText("contactTACDenial");
		((EditText)(dialog.findViewById(R.id.general6EditText))).setText("0010000000");

		((CheckBox)(dialog.findViewById(R.id.general7CheckBox))).setText("contactTACOnline");
		((EditText)(dialog.findViewById(R.id.general7EditText))).setText("DC4004F800");

		((CheckBox)(dialog.findViewById(R.id.general8CheckBox))).setText("defaultTDOL");
		((EditText)(dialog.findViewById(R.id.general8EditText))).setText("5F2403");

		((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general9EditText))).setVisibility(View.VISIBLE);
		((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).setText("defaultDDOL");
		((EditText)(dialog.findViewById(R.id.general9EditText))).setText("9F37045A085F34019A03");

		((CheckBox)(dialog.findViewById(R.id.general10CheckBox))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general10EditText))).setVisibility(View.VISIBLE);
		((CheckBox)(dialog.findViewById(R.id.general10CheckBox))).setText("contactlessTransactionLimit");
		((EditText)(dialog.findViewById(R.id.general10EditText))).setText("999999999999");

		((CheckBox)(dialog.findViewById(R.id.general11CheckBox))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general11EditText))).setVisibility(View.VISIBLE);
		((CheckBox)(dialog.findViewById(R.id.general11CheckBox))).setText("contactlessCVMRequiredLimit");
		((EditText)(dialog.findViewById(R.id.general11EditText))).setText("999999999999");

		((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general12EditText))).setVisibility(View.VISIBLE);
		((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).setText("contactlessFloorLimit");
		((EditText)(dialog.findViewById(R.id.general12EditText))).setText("000000000000");

		((CheckBox)(dialog.findViewById(R.id.general13CheckBox))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general13EditText))).setVisibility(View.VISIBLE);
		((CheckBox)(dialog.findViewById(R.id.general13CheckBox))).setText("contactlessTACDefault");
		((EditText)(dialog.findViewById(R.id.general13EditText))).setText("584000A800");

		((CheckBox)(dialog.findViewById(R.id.general14CheckBox))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general14EditText))).setVisibility(View.VISIBLE);
		((CheckBox)(dialog.findViewById(R.id.general14CheckBox))).setText("contactlessTACDenial");
		((EditText)(dialog.findViewById(R.id.general14EditText))).setText("0010000000");

		((CheckBox)(dialog.findViewById(R.id.general15CheckBox))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general15EditText))).setVisibility(View.VISIBLE);
		((CheckBox)(dialog.findViewById(R.id.general15CheckBox))).setText("contactlessTACOnline");
		((EditText)(dialog.findViewById(R.id.general15EditText))).setText("584000A800");

		((CheckBox)(dialog.findViewById(R.id.general16CheckBox))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general16EditText))).setVisibility(View.VISIBLE);
		((CheckBox)(dialog.findViewById(R.id.general16CheckBox))).setText("contactlessTransactionLimitODCV");
		((EditText)(dialog.findViewById(R.id.general16EditText))).setText("999999999999");

		((CheckBox)(dialog.findViewById(R.id.general17CheckBox))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general17EditText))).setVisibility(View.VISIBLE);
		((CheckBox)(dialog.findViewById(R.id.general17CheckBox))).setText("terminalCapabilities");
		((EditText)(dialog.findViewById(R.id.general17EditText))).setText("");

		((CheckBox)(dialog.findViewById(R.id.general18CheckBox))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general18EditText))).setVisibility(View.VISIBLE);
		((CheckBox)(dialog.findViewById(R.id.general18CheckBox))).setText("terminalType");
		((EditText)(dialog.findViewById(R.id.general18EditText))).setText("");

		((CheckBox)(dialog.findViewById(R.id.general19CheckBox))).setVisibility(View.VISIBLE);
		((EditText)(dialog.findViewById(R.id.general19EditText))).setVisibility(View.VISIBLE);
		((CheckBox)(dialog.findViewById(R.id.general19CheckBox))).setText("contactlessKernelID");
		((EditText)(dialog.findViewById(R.id.general19EditText))).setText("03");

		((LinearLayout)(dialog.findViewById(R.id.general1LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general2LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general3LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general4LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general5LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general6LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general7LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general8LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general9LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general10LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general11LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general12LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general13LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general14LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general15LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general16LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general17LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general18LinearLayout))).setOrientation(LinearLayout.VERTICAL);
		((LinearLayout)(dialog.findViewById(R.id.general19LinearLayout))).setOrientation(LinearLayout.VERTICAL);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Hashtable<String, String> data = new Hashtable<String, String>();

				String appIndex = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).isChecked()) {
					data.put("appIndex", appIndex);
				}
				String aid = ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).isChecked()) {
					data.put("aid", aid);
				}
				String appVersion = ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general3CheckBox))).isChecked()) {
					data.put("appVersion", appVersion);
				}
				String terminalFloorLimit = ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general4CheckBox))).isChecked()) {
					data.put("terminalFloorLimit", terminalFloorLimit);
				}
				String contactTACDefault = ((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general5CheckBox))).isChecked()) {
					data.put("contactTACDefault", contactTACDefault);
				}
				String contactTACDenial = ((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general6CheckBox))).isChecked()) {
					data.put("contactTACDenial", contactTACDenial);
				}
				String contactTACOnline = ((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general7CheckBox))).isChecked()) {
					data.put("contactTACOnline", contactTACOnline);
				}
				String defaultTDOL = ((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general8CheckBox))).isChecked()) {
					data.put("defaultTDOL", defaultTDOL);
				}
				String defaultDDOL = ((EditText) (dialog.findViewById(R.id.general9EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).isChecked()) {
					data.put("defaultDDOL", defaultDDOL);
				}
				String contactlessTransactionLimit = ((EditText) (dialog.findViewById(R.id.general10EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general10CheckBox))).isChecked()) {
					data.put("contactlessTransactionLimit", contactlessTransactionLimit);
				}
				String contactlessCVMRequiredLimit = ((EditText) (dialog.findViewById(R.id.general11EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general11CheckBox))).isChecked()) {
					data.put("contactlessCVMRequiredLimit", contactlessCVMRequiredLimit);
				}
				String contactlessFloorLimit = ((EditText) (dialog.findViewById(R.id.general12EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).isChecked()) {
					data.put("contactlessFloorLimit", contactlessFloorLimit);
				}
				String contactlessTACDefault = ((EditText) (dialog.findViewById(R.id.general13EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general13CheckBox))).isChecked()) {
					data.put("contactlessTACDefault", contactlessTACDefault);
				}
				String contactlessTACDenial = ((EditText) (dialog.findViewById(R.id.general14EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general14CheckBox))).isChecked()) {
					data.put("contactlessTACDenial", contactlessTACDenial);
				}
				String contactlessTACOnline = ((EditText) (dialog.findViewById(R.id.general15EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general15CheckBox))).isChecked()) {
					data.put("contactlessTACOnline", contactlessTACOnline);
				}
				String contactlessTransactionLimitODCV = ((EditText) (dialog.findViewById(R.id.general16EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general16CheckBox))).isChecked()) {
					data.put("contactlessTransactionLimitODCV", contactlessTransactionLimitODCV);
				}
				String terminalCapabilities = ((EditText) (dialog.findViewById(R.id.general17EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general17CheckBox))).isChecked()) {
					data.put("terminalCapabilities", terminalCapabilities);
				}
				String terminalType = ((EditText) (dialog.findViewById(R.id.general18EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general18CheckBox))).isChecked()) {
					data.put("terminalType", terminalType);
				}
				String contactlessKernelID = ((EditText) (dialog.findViewById(R.id.general19EditText))).getText().toString();
				if (((CheckBox)(dialog.findViewById(R.id.general19CheckBox))).isChecked()) {
					data.put("contactlessKernelID", contactlessKernelID);
				}
				bbDeviceController.updateAID(data);

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}
	
	public void promptForControlLED() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.control_led_dialog);
		dialog.setTitle(getString(R.string.control_led));
		dialog.setCanceledOnTouchOutside(false);
		
		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("duration (HEX)");
		((TextView)(dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText("10");
		
		((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).setText("ledIndex1");
		
		((TextView)(dialog.findViewById(R.id.general3TextView))).setText("flashOnInterval (HEX)");
		((TextView)(dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general3EditText))).setText("50");
		
		((TextView)(dialog.findViewById(R.id.general4TextView))).setText("flashOffInterval (HEX)");
		((TextView)(dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general4EditText))).setText("20");
		
		((TextView)(dialog.findViewById(R.id.general5TextView))).setVisibility(View.GONE);
		((EditText)(dialog.findViewById(R.id.general5EditText))).setVisibility(View.GONE);
		
		((CheckBox)(dialog.findViewById(R.id.general7CheckBox))).setText("ledIndex2");
		
		((TextView)(dialog.findViewById(R.id.general8TextView))).setText("flashOnInterval (HEX)");
		((TextView)(dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general8EditText))).setText("50");
		
		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("flashOffInterval (HEX)");
		((TextView)(dialog.findViewById(R.id.general9TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general9EditText))).setText("20");
		
		((TextView)(dialog.findViewById(R.id.general10TextView))).setVisibility(View.GONE);
		((EditText)(dialog.findViewById(R.id.general10EditText))).setVisibility(View.GONE);
		
		((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).setText("ledIndex3");
		
		((TextView)(dialog.findViewById(R.id.general13TextView))).setText("flashOnInterval (HEX)");
		((TextView)(dialog.findViewById(R.id.general13TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general13EditText))).setText("50");
		
		((TextView)(dialog.findViewById(R.id.general14TextView))).setText("flashOffInterval (HEX)");
		((TextView)(dialog.findViewById(R.id.general14TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general14EditText))).setText("20");
		
		((TextView)(dialog.findViewById(R.id.general15TextView))).setVisibility(View.GONE);
		((EditText)(dialog.findViewById(R.id.general15EditText))).setVisibility(View.GONE);
		
		((RadioButton)(dialog.findViewById(R.id.radio1Button))).setText("DEFAULT");
		((RadioButton)(dialog.findViewById(R.id.radio2Button))).setText("ON");
		((RadioButton)(dialog.findViewById(R.id.radio3Button))).setText("OFF");
		((RadioButton)(dialog.findViewById(R.id.radio4Button))).setText("FLASH");
		
		((RadioButton)(dialog.findViewById(R.id.radio5Button))).setText("DEFAULT");
		((RadioButton)(dialog.findViewById(R.id.radio6Button))).setText("ON");
		((RadioButton)(dialog.findViewById(R.id.radio7Button))).setText("OFF");
		((RadioButton)(dialog.findViewById(R.id.radio8Button))).setText("FLASH");
		
		((RadioButton)(dialog.findViewById(R.id.radio9Button))).setText("DEFAULT");
		((RadioButton)(dialog.findViewById(R.id.radio10Button))).setText("ON");
		((RadioButton)(dialog.findViewById(R.id.radio11Button))).setText("OFF");
		((RadioButton)(dialog.findViewById(R.id.radio12Button))).setText("FLASH");
		
		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				Hashtable<String, Object> data = new Hashtable<String, Object>();
				data.put("duration", ((EditText)(dialog.findViewById(R.id.general1EditText))).getText().toString());
				if (((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).isChecked()) {
					Hashtable<String, Object> led1Data = new Hashtable<String, Object>();
					String led1FlashOnInterval = ((EditText)(dialog.findViewById(R.id.general3EditText))).getText().toString();
					if ((led1FlashOnInterval != null) && (!led1FlashOnInterval.equalsIgnoreCase(""))) {
						led1Data.put("flashOnInterval", led1FlashOnInterval);
					}
					String led1FlashOffInterval = ((EditText)(dialog.findViewById(R.id.general4EditText))).getText().toString();
					if ((led1FlashOffInterval != null) && (!led1FlashOffInterval.equalsIgnoreCase(""))) {
						led1Data.put("flashOffInterval", led1FlashOffInterval);
					}
					if (((RadioButton)(dialog.findViewById(R.id.radio1Button))).isChecked()) {
						led1Data.put("mode", LEDMode.DEFAULT);
					} else if (((RadioButton)(dialog.findViewById(R.id.radio2Button))).isChecked()) {
						led1Data.put("mode", LEDMode.ON);
					} else if (((RadioButton)(dialog.findViewById(R.id.radio3Button))).isChecked()) {
						led1Data.put("mode", LEDMode.OFF);
					} else if (((RadioButton)(dialog.findViewById(R.id.radio4Button))).isChecked()) {
						led1Data.put("mode", LEDMode.FLASH);					
					}
					data.put("ledIndex1", led1Data);
				}
				
				if (((CheckBox)(dialog.findViewById(R.id.general7CheckBox))).isChecked()) {
					Hashtable<String, Object> led2Data = new Hashtable<String, Object>();
					String led2FlashOnInterval = ((EditText)(dialog.findViewById(R.id.general8EditText))).getText().toString();
					if ((led2FlashOnInterval != null) && (!led2FlashOnInterval.equalsIgnoreCase(""))) {
						led2Data.put("flashOnInterval", led2FlashOnInterval);
					}
					String led2FlashOffInterval = ((EditText)(dialog.findViewById(R.id.general9EditText))).getText().toString();
					if ((led2FlashOffInterval != null) && (!led2FlashOffInterval.equalsIgnoreCase(""))) {
						led2Data.put("flashOffInterval", led2FlashOffInterval);
					}
					if (((RadioButton)(dialog.findViewById(R.id.radio5Button))).isChecked()) {
						led2Data.put("mode", LEDMode.DEFAULT);
					} else if (((RadioButton)(dialog.findViewById(R.id.radio6Button))).isChecked()) {
						led2Data.put("mode", LEDMode.ON);
					} else if (((RadioButton)(dialog.findViewById(R.id.radio7Button))).isChecked()) {
						led2Data.put("mode", LEDMode.OFF);
					} else if (((RadioButton)(dialog.findViewById(R.id.radio8Button))).isChecked()) {
						led2Data.put("mode", LEDMode.FLASH);					
					}
					data.put("ledIndex2", led2Data);
				}
				
				if (((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).isChecked()) {
					Hashtable<String, Object> led3Data = new Hashtable<String, Object>();
					String led3FlashOnInterval = ((EditText)(dialog.findViewById(R.id.general13EditText))).getText().toString();
					if ((led3FlashOnInterval != null) && (!led3FlashOnInterval.equalsIgnoreCase(""))) {
						led3Data.put("flashOnInterval", led3FlashOnInterval);
					}
					String led3FlashOffInterval = ((EditText)(dialog.findViewById(R.id.general14EditText))).getText().toString();
					if ((led3FlashOffInterval != null) && (!led3FlashOffInterval.equalsIgnoreCase(""))) {
						led3Data.put("flashOffInterval", led3FlashOffInterval);
					}
					if (((RadioButton)(dialog.findViewById(R.id.radio9Button))).isChecked()) {
						led3Data.put("mode", LEDMode.DEFAULT);
					} else if (((RadioButton)(dialog.findViewById(R.id.radio10Button))).isChecked()) {
						led3Data.put("mode", LEDMode.ON);
					} else if (((RadioButton)(dialog.findViewById(R.id.radio11Button))).isChecked()) {
						led3Data.put("mode", LEDMode.OFF);
					} else if (((RadioButton)(dialog.findViewById(R.id.radio12Button))).isChecked()) {
						led3Data.put("mode", LEDMode.FLASH);					
					}
					data.put("ledIndex3", led3Data);
				}
				bbDeviceController.controlLED(data);
				
				dialog.dismiss();
			}
		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForEnableInputAmount() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_and_spinner);
		dialog.setTitle(getString(R.string.enable_input_amount));

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("currencyCode");
		((TextView)(dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText("0840");

		((TextView)(dialog.findViewById(R.id.general2TextView))).setText("setAmountTimeout");
		((TextView)(dialog.findViewById(R.id.general2TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general2EditText))).setText("30");

		((TextView)(dialog.findViewById(R.id.general3TextView))).setText("currencyExponent");
		((TextView)(dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general3EditText))).setText("2");

		((TextView)(dialog.findViewById(R.id.general4TextView))).setText("amount");
		((TextView)(dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general4EditText))).setText("10");

		((TextView)(dialog.findViewById(R.id.general5TextView))).setText("tipsAmount1");
		((TextView)(dialog.findViewById(R.id.general5TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general5EditText))).setText("3.45");

		((TextView)(dialog.findViewById(R.id.general6TextView))).setText("tipsAmount2");
		((TextView)(dialog.findViewById(R.id.general6TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general6EditText))).setText("5.67");

		((TextView)(dialog.findViewById(R.id.general7TextView))).setText("tipsAmount3");
		((TextView)(dialog.findViewById(R.id.general7TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general7EditText))).setText("8.90");

		((TextView)(dialog.findViewById(R.id.general8TextView))).setText("tipsPercentage1");
		((TextView)(dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general8EditText))).setText("4");

		((TextView)(dialog.findViewById(R.id.general11TextView))).setText("tipsPercentage2");
		((TextView)(dialog.findViewById(R.id.general11TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general11EditText))).setText("8");

		((TextView)(dialog.findViewById(R.id.general12TextView))).setText("tipsPercentage3");
		((TextView)(dialog.findViewById(R.id.general12TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general12EditText))).setText("16");

		((TextView)(dialog.findViewById(R.id.general13TextView))).setText("cashback1");
		((TextView)(dialog.findViewById(R.id.general13TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general13EditText))).setText("10");

		((TextView)(dialog.findViewById(R.id.general14TextView))).setText("cashback2");
		((TextView)(dialog.findViewById(R.id.general14TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general14EditText))).setText("20");

		((TextView)(dialog.findViewById(R.id.general15TextView))).setText("cashback3");
		((TextView)(dialog.findViewById(R.id.general15TextView))).getLayoutParams().width = 500;
		((EditText)(dialog.findViewById(R.id.general15EditText))).setText("40");

		dialog.findViewById(R.id.general16TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general17TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general18TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general19TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general20TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general20EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general20CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general24TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general25TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general26TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26CheckBox).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("currencyCharacters");
		String[] symbols = new String[] { "DOLLAR", "RUPEE", "YEN", "POUND", "EURO", "WON", "DIRHAM", "RIYAL", "YUAN", "A", "BC", "DEF", "G H", "NULL", "RP", "PEN", "NEW_SHEKEL", "DONG", "RUPIAH", "SOL", "PESO" };
		((Spinner)dialog.findViewById(R.id.general9Spinner)).setAdapter(new ArrayAdapter<String>(BaseActivity.this, android.R.layout.simple_spinner_item, symbols));

		((TextView)(dialog.findViewById(R.id.general10TextView))).setText("amountInputType");
		((Spinner)dialog.findViewById(R.id.general10Spinner)).setAdapter(new ArrayAdapter<AmountInputType>(BaseActivity.this, android.R.layout.simple_spinner_item, AmountInputType.values()));

		((TextView)(dialog.findViewById(R.id.general21TextView))).setText("otherAmountOption");
		((Spinner)dialog.findViewById(R.id.general21Spinner)).setAdapter(new ArrayAdapter<BBDeviceController.OtherAmountOption>(BaseActivity.this, android.R.layout.simple_spinner_item, BBDeviceController.OtherAmountOption.values()));

		dialog.findViewById(R.id.general22CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general23CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23Spinner).setVisibility(View.GONE);

		if (bbDeviceController.getConnectionMode() == ConnectionMode.SERIAL) {
			((TextView)(dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 200;
			((TextView)(dialog.findViewById(R.id.general2TextView))).getLayoutParams().width = 200;
			((TextView)(dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 200;
			((TextView)(dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 200;
			((TextView)(dialog.findViewById(R.id.general5TextView))).getLayoutParams().width = 200;
			((TextView)(dialog.findViewById(R.id.general6TextView))).getLayoutParams().width = 200;
			((TextView)(dialog.findViewById(R.id.general7TextView))).getLayoutParams().width = 200;
			((TextView)(dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 200;
			((TextView)(dialog.findViewById(R.id.general11TextView))).getLayoutParams().width = 200;
			((TextView)(dialog.findViewById(R.id.general12TextView))).getLayoutParams().width = 200;
		}

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Hashtable<String, Object> data = new Hashtable<String, Object>();

				if (((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).isChecked()) {
					data.put("currencyCode", ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).isChecked()) {
					data.put("setAmountTimeout", ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general3CheckBox))).isChecked()) {
					data.put("currencyExponent", ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general4CheckBox))).isChecked()) {
					data.put("amount", ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString());
				}

				if ((((CheckBox)(dialog.findViewById(R.id.general5CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general6CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general7CheckBox))).isChecked())) {
					List<String> tipsAmountList = new ArrayList<>();
					if (((CheckBox) (dialog.findViewById(R.id.general5CheckBox))).isChecked()) {
						tipsAmountList.add(((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general6CheckBox))).isChecked()) {
						tipsAmountList.add(((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general7CheckBox))).isChecked()) {
						tipsAmountList.add(((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString());
					}
					data.put("tipsAmountOptions", tipsAmountList.toArray());
				}


				if ((((CheckBox)(dialog.findViewById(R.id.general8CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general11CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).isChecked())) {
					List<String> tipsPercentageList = new ArrayList<>();
					if (((CheckBox) (dialog.findViewById(R.id.general8CheckBox))).isChecked()) {
						tipsPercentageList.add(((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general11CheckBox))).isChecked()) {
						tipsPercentageList.add(((EditText) (dialog.findViewById(R.id.general11EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general12CheckBox))).isChecked()) {
						tipsPercentageList.add(((EditText) (dialog.findViewById(R.id.general12EditText))).getText().toString());
					}
					data.put("tipsPercentageOptions", tipsPercentageList.toArray());
				}

				if ((((CheckBox)(dialog.findViewById(R.id.general13CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general14CheckBox))).isChecked()) || (((CheckBox)(dialog.findViewById(R.id.general15CheckBox))).isChecked())) {
					List<String> cashbackList = new ArrayList<>();
					if (((CheckBox) (dialog.findViewById(R.id.general13CheckBox))).isChecked()) {
						cashbackList.add(((EditText) (dialog.findViewById(R.id.general13EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general14CheckBox))).isChecked()) {
						cashbackList.add(((EditText) (dialog.findViewById(R.id.general14EditText))).getText().toString());
					}

					if (((CheckBox) (dialog.findViewById(R.id.general15CheckBox))).isChecked()) {
						cashbackList.add(((EditText) (dialog.findViewById(R.id.general15EditText))).getText().toString());
					}
					data.put("cashbackAmountOptions", cashbackList.toArray());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).isChecked()) {
					String symbolString = (String)((Spinner)dialog.findViewById(R.id.general9Spinner)).getSelectedItem();
					CurrencyCharacter[] currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.A, CurrencyCharacter.B, CurrencyCharacter.C };
					if (symbolString.equals("DOLLAR")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.DOLLAR };
					} else if (symbolString.equals("RUPEE")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.RUPEE };
					} else if (symbolString.equals("YEN")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.YEN };
					} else if (symbolString.equals("POUND")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.POUND };
					} else if (symbolString.equals("EURO")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.EURO };
					} else if (symbolString.equals("WON")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.WON };
					} else if (symbolString.equals("DIRHAM")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.DIRHAM };
					} else if (symbolString.equals("RIYAL")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.RIYAL, CurrencyCharacter.RIYAL_2 };
					} else if (symbolString.equals("YUAN")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.YUAN };
					} else if (symbolString.equals("A")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.A };
					} else if (symbolString.equals("BC")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.B, CurrencyCharacter.C };
					} else if (symbolString.equals("DEF")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.D, CurrencyCharacter.E, CurrencyCharacter.F };
					} else if (symbolString.equals("G H")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.G, CurrencyCharacter.SPACE, CurrencyCharacter.H };
					} else if (symbolString.equals("RP")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.R, CurrencyCharacter.P };
					} else if (symbolString.equals("PEN")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.S, CurrencyCharacter.SLASH_AND_DOT };
					} else if (symbolString.equals("NEW_SHEKEL")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.NEW_SHEKEL };
					} else if (symbolString.equals("DONG")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.DONG };
					} else if (symbolString.equals("RUPIAH")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.RUPIAH };
					} else if (symbolString.equals("SOL")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.SOL };
					} else if (symbolString.equals("PESO")) {
						currencyCharacters = new CurrencyCharacter[] { CurrencyCharacter.PESO };
					} else if (symbolString.equals("NULL")) {
						currencyCharacters = null;
					}
					data.put("currencyCharacters", currencyCharacters);
				}

				if (((CheckBox)(dialog.findViewById(R.id.general10CheckBox))).isChecked()) {
					AmountInputType amountInputType = (AmountInputType)((Spinner)dialog.findViewById(R.id.general10Spinner)).getSelectedItem();
					data.put("amountInputType", amountInputType);
				}

				if (((CheckBox)(dialog.findViewById(R.id.general21CheckBox))).isChecked()) {
					BBDeviceController.OtherAmountOption otherAmountOption1 = (BBDeviceController.OtherAmountOption) ((Spinner)dialog.findViewById(R.id.general21Spinner)).getSelectedItem();
					data.put("otherAmountOption", otherAmountOption1);
				}

				bbDeviceController.enableInputAmount(data);

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForDisplayPrompt() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_and_spinner);
		dialog.setTitle(getString(R.string.display_prompt));

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("promptData");
		((TextView)(dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText("Hello");

		((TextView)(dialog.findViewById(R.id.general2TextView))).setText("timeout");
		((TextView)(dialog.findViewById(R.id.general2TextView))).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general2EditText))).setText("30");

		((TextView)(dialog.findViewById(R.id.general3TextView))).setText("promptDataEnum");
		((TextView)(dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general3EditText))).setText("0135656E");

		((TextView)(dialog.findViewById(R.id.general4TextView))).setText("horizontalScrollSpeed");
		((TextView)(dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general4EditText))).setText("2");

		((TextView)(dialog.findViewById(R.id.general5TextView))).setText("scrollDirection");
		((TextView)(dialog.findViewById(R.id.general5TextView))).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general5EditText))).setText("0");

		((TextView)(dialog.findViewById(R.id.general6TextView))).setText("promptAnimation");
		((TextView)(dialog.findViewById(R.id.general6TextView))).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general6EditText))).setText("0");

		((TextView)(dialog.findViewById(R.id.general7TextView))).setText("verticalScrollSpeed");
		((TextView)(dialog.findViewById(R.id.general7TextView))).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general7EditText))).setText("2");

		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general11TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general11EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general11CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general12TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general12EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general12CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general13TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general13EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general13CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general14TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general14EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general14CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general15TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general15EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general15CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general16TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general17TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general18TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general19TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general20TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general20EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general20CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general24TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general25TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general26TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26CheckBox).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("promptIcon");
		((Spinner)dialog.findViewById(R.id.general9Spinner)).setAdapter(new ArrayAdapter<DisplayPromptIcon>(BaseActivity.this, android.R.layout.simple_spinner_item, DisplayPromptIcon.values()));

		((TextView)(dialog.findViewById(R.id.general10TextView))).setText("option");
		((Spinner)dialog.findViewById(R.id.general10Spinner)).setAdapter(new ArrayAdapter<DisplayPromptOption>(BaseActivity.this, android.R.layout.simple_spinner_item, DisplayPromptOption.values()));

		((TextView)(dialog.findViewById(R.id.general21TextView))).setText("promptTone");
		((Spinner)dialog.findViewById(R.id.general21Spinner)).setAdapter(new ArrayAdapter<BBDeviceController.DisplayPromptTone>(BaseActivity.this, android.R.layout.simple_spinner_item, BBDeviceController.DisplayPromptTone.values()));

		dialog.findViewById(R.id.general22CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general23CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23Spinner).setVisibility(View.GONE);

		if (bbDeviceController.getConnectionMode() == ConnectionMode.SERIAL) {
			dialog.findViewById(R.id.general1TextView).getLayoutParams().width = 200;
			dialog.findViewById(R.id.general2TextView).getLayoutParams().width = 200;
		}

		if (Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("CHB8")) {
			((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general2TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general2EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general2CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general3CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general3TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general3EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general3CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general9TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general9Spinner).setVisibility(View.GONE);
			dialog.findViewById(R.id.general9CheckBox).setVisibility(View.GONE);
		} else if (Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPC3") || Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPS3")) {
			((CheckBox)(dialog.findViewById(R.id.general4CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general4CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general5CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general5CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general6CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general6TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general6EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general6CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general7CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general7CheckBox).setVisibility(View.GONE);
		}

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Hashtable<String, Object> data = new Hashtable<String, Object>();

				if (((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).isChecked()) {
					data.put("promptData", Utils.asciiStringToHexString(((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString()));
				}

				if (((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).isChecked()) {
					data.put("timeout", ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general3CheckBox))).isChecked()) {
					data.put("promptDataEnum", ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general4CheckBox))).isChecked()) {
					data.put("horizontalScrollSpeed", ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general5CheckBox))).isChecked()) {
					data.put("scrollDirection", ((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general6CheckBox))).isChecked()) {
					data.put("promptAnimation", ((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general7CheckBox))).isChecked()) {
					data.put("verticalScrollSpeed", ((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).isChecked()) {
					DisplayPromptIcon displayPromptIcon = (DisplayPromptIcon)((Spinner)dialog.findViewById(R.id.general9Spinner)).getSelectedItem();
					data.put("promptIcon", displayPromptIcon);
				}

				if (((CheckBox)(dialog.findViewById(R.id.general10CheckBox))).isChecked()) {
					DisplayPromptOption displayPromptOption = (DisplayPromptOption)((Spinner)dialog.findViewById(R.id.general10Spinner)).getSelectedItem();
					data.put("option", displayPromptOption);
				}

				if (((CheckBox)(dialog.findViewById(R.id.general21CheckBox))).isChecked()) {
					BBDeviceController.DisplayPromptTone displayPromptTone = (BBDeviceController.DisplayPromptTone)((Spinner)dialog.findViewById(R.id.general21Spinner)).getSelectedItem();
					data.put("promptTone", displayPromptTone);
				}

				bbDeviceController.displayPrompt(data);

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForUpdateDisplayString() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_and_spinner);
		dialog.setTitle(getString(R.string.update_display_string));

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("data");
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText("Hello");

		((TextView)(dialog.findViewById(R.id.general2TextView))).setText("dataEnum");
		((EditText)(dialog.findViewById(R.id.general2EditText))).setText("0135656E");

		dialog.findViewById(R.id.general3TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general6TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general11TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general11EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general11CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general12TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general12EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general12CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general13TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general13EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general13CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general14TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general14EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general14CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general15TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general15EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general15CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general16TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general17TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general18TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general19TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general20TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general20EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general20CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general24TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general25TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general26TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general9CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general9TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general9Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general10CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general21CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general21TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general21Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general22CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general23CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Hashtable<String, String> data = new Hashtable<>();

				if (((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).isChecked()) {
					data.put("data", Utils.asciiStringToHexString(((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString()));
				}

				if (((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).isChecked()) {
					data.put("dataEnum", ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString());
				}

				bbDeviceController.updateDisplayString(data);

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForReadDisplayString() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_and_spinner);
		dialog.setTitle(getString(R.string.read_display_string));

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("dataEnum");
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText("0135656E");

		dialog.findViewById(R.id.general2TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general2EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general2CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general3TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general6TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general11TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general11EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general11CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general12TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general12EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general12CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general13TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general13EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general13CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general14TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general14EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general14CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general15TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general15EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general15CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general16TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general17TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general18TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general19TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general20TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general20EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general20CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general24TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general25TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general26TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general9CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general9TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general9Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general10CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general21CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general21TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general21Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general22CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general23CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String dataEnum = "";

				if (((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).isChecked()) {
					dataEnum = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
				}

				bbDeviceController.readDisplayString(dataEnum);

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

    public void promptForSetDisplayImage() {
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.general_string_and_spinner);
        dialog.setTitle(getString(R.string.update_display_settings));

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.getWindow().setAttributes(lp);

		String displayData = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000007FFF7FFF7FFF7FFF3FFF3FFF1FFF0FFF03FF000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000007FFF7FFF7FFF7FFF3FFF3FFF1FFF0FFF03FF000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000000000000000FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FFC0FFC0FFC07FE07FE07FF03FF83FFC1FFE1FFF0FFF07FF07FF03FF01FF00FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000000000000000FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FFC0FFC0FFC07FE07FF07FF03FF83FFC1FFE1FFF0FFF07FF07FF03FF01FF00FF003F001F0007000000000000000000000000000000000007001F003F00FF01FF03FF07FF07FF0FFF1FFF1FFE3FFC3FF87FF07FE07FE0FFC0FFC0FFC0FF80FF80FF80FF80FF80FF80FF80FFC0FFC0FFC07FE07FE07FF03FF83FFC1FFE1FFF0FFF07FF07FF03FF01FF00FF003F001F000F003F007F01FF03FF03FF07FF0FFF1FFF1FFF3FFC3FF87FF07FF07FE0FFC0FFC0FFC0FF80FF80FF80FF80FF80FF80FF80FF80FFC0FFC07FE07FE07FF03FF83FFC1FFE1FFF0FFF0FFF07FF03FF01FF00FF003F007F01FF07FF0FFF1FFF3FFF3FFF7FFF7FFFFFF0FFE0FFC0FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF80FF00FF00FE00FC00F800E000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FFF0FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF003F0007000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008001E007FE7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF003F0007000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008001E007FE7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF0FF000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFE00E000800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008001E007FC3FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFE007800100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000C003F81FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF8FF80FFC0FFE0FFE0FFF0FFF07FF03FF01FF81FF81FF81FF81FF81FF81FF81FF81FF81FF81FF81FF81FF81FF80FF80FFC0FFE0FFF07FF07FF03FF03FF01FF00FF003F001F000300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000E000F800FC00FF00FF80FFC0FFE0FFE0FFF0FFF87FF83FFC1FFC0FFE07FE07FE03FF03FF03FF01FF01FF01FF01FF01FF01FF01FF03FF03FF03FF07FE07FE0FFE1FFC3FFC7FF8FFF8FFF0FFE0FFE0FFC0FF80FF00FC00F800E000F800FC00FF00FF80FFC0FFE0FFE0FFF0FFF87FF83FFC1FFC0FFE07FE07FE03FF03FF03FF01FF01FF01FF01FF01FF01FF01FF03FF03FF03FF07FE0FFE0FFE1FFC3FFC7FF8FFF8FFF0FFE0FFE0FFC0FF80FF00FC00F800E000000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF0000000000000000000001FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF03FF03FF03FF07FE07FE0FFE1FFC3FFC7FF8FFF8FFF0FFE0FFE0FFC0FF80FF00FC00F800F000FC00FE00FF80FFC0FFE0FFE0FFF0FFF8FFF83FFC1FFC0FFE0FFE07FE03FF03FF03FF01FF01FF01FF01FF01FF01FF01FF01FF03FF03FF07FE07FE0FFE1FFC3FFC7FF8FFF8FFF0FFF0FFE0FFC0FF80FF00FC00F80FE03F807F007F00FF00FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF01FF03FF03FF07FF9FFFFFFEFFFEFFFCFFF8FFF8FFF0FFC0FF00F8000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FFFEFFFEFFFEFFFEFFFCFFFCFFF8FFF0FFC0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        ((TextView)(dialog.findViewById(R.id.general1TextView))).setText("data");
        ((EditText)(dialog.findViewById(R.id.general1EditText))).setText("");

        ((TextView)(dialog.findViewById(R.id.general2TextView))).setText("foregroundColor");
		dialog.findViewById(R.id.general2TextView).getLayoutParams().width = 350;
        ((EditText)(dialog.findViewById(R.id.general2EditText))).setText("0100000000004001F000FFFF0000");

        ((TextView)(dialog.findViewById(R.id.general3TextView))).setText("backgroundColor");
		dialog.findViewById(R.id.general3TextView).getLayoutParams().width = 350;
        ((EditText)(dialog.findViewById(R.id.general3EditText))).setText("0100000000004001F000E73F0000");

        ((TextView)(dialog.findViewById(R.id.general4TextView))).setText("statusBarColor");
		dialog.findViewById(R.id.general4TextView).getLayoutParams().width = 350;
        ((EditText)(dialog.findViewById(R.id.general4EditText))).setText("E73FC121");

        ((TextView)(dialog.findViewById(R.id.general5TextView))).setText("screenColor");
		dialog.findViewById(R.id.general5TextView).getLayoutParams().width = 350;
        ((EditText)(dialog.findViewById(R.id.general5EditText))).setText("FFFFE73F");

        ((TextView)(dialog.findViewById(R.id.general6TextView))).setText("brightnessLevel");
		dialog.findViewById(R.id.general6TextView).getLayoutParams().width = 350;
        ((EditText)(dialog.findViewById(R.id.general6EditText))).setText("10");

        ((TextView)(dialog.findViewById(R.id.general7TextView))).setText("backlightTimeout");
		dialog.findViewById(R.id.general7TextView).getLayoutParams().width = 350;
        ((EditText)(dialog.findViewById(R.id.general7EditText))).setText("050A0F");

        ((TextView)(dialog.findViewById(R.id.general8TextView))).setText("time");
		dialog.findViewById(R.id.general8TextView).getLayoutParams().width = 350;
        ((EditText)(dialog.findViewById(R.id.general8EditText))).setText("1");

        ((TextView)(dialog.findViewById(R.id.general11TextView))).setText("bluetooth");
		dialog.findViewById(R.id.general11TextView).getLayoutParams().width = 350;
        ((EditText)(dialog.findViewById(R.id.general11EditText))).setText("1");

        ((TextView)(dialog.findViewById(R.id.general12TextView))).setText("battery");
		dialog.findViewById(R.id.general12TextView).getLayoutParams().width = 350;
        ((EditText)(dialog.findViewById(R.id.general12EditText))).setText("1");

		((TextView)(dialog.findViewById(R.id.general13TextView))).setText("statusBarVisibility");
		dialog.findViewById(R.id.general13TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general13EditText))).setText("true");

		((TextView)(dialog.findViewById(R.id.general14TextView))).setText("statusIconSet");
		dialog.findViewById(R.id.general14TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general14EditText))).setText("1");

		((TextView)(dialog.findViewById(R.id.general15TextView))).setText("highlightColor");
		dialog.findViewById(R.id.general15TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general15EditText))).setText("82F4E73F");

		((TextView)(dialog.findViewById(R.id.general16TextView))).setText("userActionIconColor");
		dialog.findViewById(R.id.general16TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general16EditText))).setText("82F4E73F");

		((TextView)(dialog.findViewById(R.id.general17TextView))).setText("spinnerIconColor");
		dialog.findViewById(R.id.general17TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general17EditText))).setText("82F4E73F");

		((TextView)(dialog.findViewById(R.id.general18TextView))).setText("adaptiveBrightness");
		dialog.findViewById(R.id.general18TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general18EditText))).setText("1");

		((TextView)(dialog.findViewById(R.id.general19TextView))).setText("capacitiveSensor");
		dialog.findViewById(R.id.general19TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general19EditText))).setText("01010101");

		((TextView)(dialog.findViewById(R.id.general20TextView))).setText("horizontalScrollSpeed");
		dialog.findViewById(R.id.general20TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general20EditText))).setText("2");

		((TextView)(dialog.findViewById(R.id.general24TextView))).setText("scrollDirection");
		dialog.findViewById(R.id.general24TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general24EditText))).setText("0");

		((TextView)(dialog.findViewById(R.id.general25TextView))).setText("verticalScrollSpeed");
		dialog.findViewById(R.id.general25TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general25EditText))).setText("2");

		dialog.findViewById(R.id.general26TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26CheckBox).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("language");
		dialog.findViewById(R.id.general9TextView).getLayoutParams().width = 350;
		String[] languages = new String[] { "EN", "FR", "ES", "DE", "IT", "NL", "" };
		((Spinner)dialog.findViewById(R.id.general9Spinner)).setAdapter(new ArrayAdapter<String>(BaseActivity.this, android.R.layout.simple_spinner_item, languages));

		dialog.findViewById(R.id.general10CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general21CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general21TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general21Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general22CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general23CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23Spinner).setVisibility(View.GONE);

		if (Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("CHB8")) {
			((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general1TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general1EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general1CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general2TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general2EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general2CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general3CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general3TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general3EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general3CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general4CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general4CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general5CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general5CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general7CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general7CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general8CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general8CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general11CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general11TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general11EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general11CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general12TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general12EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general12CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general13CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general13TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general13EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general13CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general14CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general14TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general14EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general14CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general15CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general15TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general15EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general15CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general16CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general16TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general16EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general16CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general17CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general17TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general17EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general17CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general9TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general9Spinner).setVisibility(View.GONE);
			dialog.findViewById(R.id.general9CheckBox).setVisibility(View.GONE);
		} else if (Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPC3") || Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPS3")) {
			//((TextView)dialog.findViewById(R.id.general1EditText)).setText(displayData);
			((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general1TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general1EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general1CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general18CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general18TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general18EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general18CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general19CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general19TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general19EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general19CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general20CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general20TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general20EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general20CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general24CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general24TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general24EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general24CheckBox).setVisibility(View.GONE);

			((CheckBox)(dialog.findViewById(R.id.general25CheckBox))).setChecked(false);
			dialog.findViewById(R.id.general25TextView).setVisibility(View.GONE);
			dialog.findViewById(R.id.general25EditText).setVisibility(View.GONE);
			dialog.findViewById(R.id.general25CheckBox).setVisibility(View.GONE);
		} else {
			((TextView)dialog.findViewById(R.id.general1EditText)).setText(displayData);
		}

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Hashtable<String, Object> image = new Hashtable<String, Object>();
                if (((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).isChecked()) {
					String dataStr = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
					File sd = Environment.getExternalStorageDirectory();
					if (dataStr.startsWith(sd.getAbsolutePath())) {
						File targetImage = new File(dataStr);
						BitmapFactory.Options bmOptions = new BitmapFactory.Options();
						Bitmap bitmap = BitmapFactory.decodeFile(targetImage.getAbsolutePath(), bmOptions);
						image.put("data", bitmap);
					} else {
						image.put("data", ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());
					}
                }
                if (((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).isChecked()) {
                    image.put("foregroundColor", ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString());
                }
                if (((CheckBox)(dialog.findViewById(R.id.general3CheckBox))).isChecked()) {
                    image.put("backgroundColor", ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString());
                }

                Hashtable<String, Object> theme = new Hashtable<String, Object>();
                if (((CheckBox)(dialog.findViewById(R.id.general4CheckBox))).isChecked()) {
                    theme.put("statusBarColor", ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString());
                }
                if (((CheckBox)(dialog.findViewById(R.id.general5CheckBox))).isChecked()) {
                    theme.put("screenColor", ((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString());
                }

                Hashtable<String, Object> statusBarInfo = new Hashtable<String, Object>();
                if (((CheckBox)(dialog.findViewById(R.id.general8CheckBox))).isChecked() || ((CheckBox)(dialog.findViewById(R.id.general11CheckBox))).isChecked() || ((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).isChecked()) {
                    if (((CheckBox)(dialog.findViewById(R.id.general8CheckBox))).isChecked()) {
                        statusBarInfo.put("time", ((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString());
                    }
                    if (((CheckBox)(dialog.findViewById(R.id.general11CheckBox))).isChecked()) {
                        statusBarInfo.put("bluetooth", ((EditText) (dialog.findViewById(R.id.general11EditText))).getText().toString());
                    }
                    if (((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).isChecked()) {
                        statusBarInfo.put("battery", ((EditText) (dialog.findViewById(R.id.general12EditText))).getText().toString());
                    }
                    theme.put("statusBarInfo", statusBarInfo);
                }

				if (((CheckBox)(dialog.findViewById(R.id.general13CheckBox))).isChecked()) {
					theme.put("statusBarVisibility", ((EditText) (dialog.findViewById(R.id.general13EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general14CheckBox))).isChecked()) {
					theme.put("statusIconSet", ((EditText) (dialog.findViewById(R.id.general14EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general15CheckBox))).isChecked()) {
					theme.put("highlightColor", ((EditText) (dialog.findViewById(R.id.general15EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general16CheckBox))).isChecked()) {
					theme.put("userActionIconColor", ((EditText) (dialog.findViewById(R.id.general16EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general17CheckBox))).isChecked()) {
					theme.put("spinnerIconColor", ((EditText) (dialog.findViewById(R.id.general17EditText))).getText().toString());
				}


				Hashtable<String, Object> data = new Hashtable<String, Object>();
				if (image.size() > 0) {
					data.put("image", image);
				}
                data.put("theme", theme);

                if (((CheckBox)(dialog.findViewById(R.id.general6CheckBox))).isChecked()) {
                    data.put("brightnessLevel", ((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString());
                }

                if (((CheckBox)(dialog.findViewById(R.id.general7CheckBox))).isChecked()) {
                    data.put("backlightTimeout", ((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString());
                }

				if (((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).isChecked()) {
					String language = (String)((Spinner)dialog.findViewById(R.id.general9Spinner)).getSelectedItem();
					data.put("language", language);
				}

				if (((CheckBox)(dialog.findViewById(R.id.general18CheckBox))).isChecked()) {
					data.put("adaptiveBrightness", ((EditText) (dialog.findViewById(R.id.general18EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general19CheckBox))).isChecked()) {
					data.put("capacitiveSensor", ((EditText) (dialog.findViewById(R.id.general19EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general20CheckBox))).isChecked()) {
					data.put("horizontalScrollSpeed", ((EditText) (dialog.findViewById(R.id.general20EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general24CheckBox))).isChecked()) {
					data.put("scrollDirection", ((EditText) (dialog.findViewById(R.id.general24EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general25CheckBox))).isChecked()) {
					data.put("verticalScrollSpeed", ((EditText) (dialog.findViewById(R.id.general25EditText))).getText().toString());
				}

                bbDeviceController.updateDisplaySettings(data);
                dialog.dismiss();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

	public void promptForReadDisplaySettings() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_and_spinner);
		dialog.setTitle(getString(R.string.read_display_settings));

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.getWindow().setAttributes(lp);

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("data");
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general2TextView))).setText("foregroundColor");
		dialog.findViewById(R.id.general2TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general2EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general3TextView))).setText("backgroundColor");
		dialog.findViewById(R.id.general3TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general3EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general4TextView))).setText("statusBarColor");
		dialog.findViewById(R.id.general4TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general4EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general5TextView))).setText("screenColor");
		dialog.findViewById(R.id.general5TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general5EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general6TextView))).setText("brightnessLevel");
		dialog.findViewById(R.id.general6TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general6EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general7TextView))).setText("backlightTimeout");
		dialog.findViewById(R.id.general7TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general7EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general8TextView))).setText("time");
		dialog.findViewById(R.id.general8TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general8EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general11TextView))).setText("bluetooth");
		dialog.findViewById(R.id.general11TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general11EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general12TextView))).setText("battery");
		dialog.findViewById(R.id.general12TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general12EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general13TextView))).setText("statusBarVisibility");
		dialog.findViewById(R.id.general13TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general13EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general14TextView))).setText("statusIconSet");
		dialog.findViewById(R.id.general14TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general14EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general15TextView))).setText("highlightColor");
		dialog.findViewById(R.id.general15TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general15EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general16TextView))).setText("userActionIconColor");
		dialog.findViewById(R.id.general16TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general16EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general17TextView))).setText("spinnerIconColor");
		dialog.findViewById(R.id.general17TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general17EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general18TextView))).setText("adaptiveBrightness");
		dialog.findViewById(R.id.general18TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general18EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general19TextView))).setText("capacitiveSensor");
		dialog.findViewById(R.id.general19TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general19EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general20TextView))).setText("horizontalScrollSpeed");
		dialog.findViewById(R.id.general20TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general20EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general24TextView))).setText("scrollDirection");
		dialog.findViewById(R.id.general24TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general24EditText))).setText("");

		((TextView)(dialog.findViewById(R.id.general25TextView))).setText("verticalScrollSpeed");
		dialog.findViewById(R.id.general25TextView).getLayoutParams().width = 350;
		((EditText)(dialog.findViewById(R.id.general25EditText))).setText("");

		dialog.findViewById(R.id.general26TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26CheckBox).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("language");
		dialog.findViewById(R.id.general9TextView).getLayoutParams().width = 350;
		String[] languages = new String[] { "" };
		((Spinner)dialog.findViewById(R.id.general9Spinner)).setAdapter(new ArrayAdapter<String>(BaseActivity.this, android.R.layout.simple_spinner_item, languages));

		dialog.findViewById(R.id.general10CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general21CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general21TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general21Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general22CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general22Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.general23CheckBox).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general23Spinner).setVisibility(View.GONE);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Hashtable<String, Object> image = new Hashtable<String, Object>();
				if (((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).isChecked()) {
					image.put("data", "");
				}
				if (((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).isChecked()) {
					image.put("foregroundColor", "");
				}
				if (((CheckBox)(dialog.findViewById(R.id.general3CheckBox))).isChecked()) {
					image.put("backgroundColor", "");
				}

				Hashtable<String, Object> theme = new Hashtable<String, Object>();
				if (((CheckBox)(dialog.findViewById(R.id.general4CheckBox))).isChecked()) {
					theme.put("statusBarColor", "");
				}
				if (((CheckBox)(dialog.findViewById(R.id.general5CheckBox))).isChecked()) {
					theme.put("screenColor", "");
				}

				Hashtable<String, Object> statusBarInfo = new Hashtable<String, Object>();
				if (((CheckBox)(dialog.findViewById(R.id.general8CheckBox))).isChecked() || ((CheckBox)(dialog.findViewById(R.id.general11CheckBox))).isChecked() || ((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).isChecked()) {
					if (((CheckBox)(dialog.findViewById(R.id.general8CheckBox))).isChecked()) {
						statusBarInfo.put("time", "");
					}
					if (((CheckBox)(dialog.findViewById(R.id.general11CheckBox))).isChecked()) {
						statusBarInfo.put("bluetooth", "");
					}
					if (((CheckBox)(dialog.findViewById(R.id.general12CheckBox))).isChecked()) {
						statusBarInfo.put("battery", "");
					}
					theme.put("statusBarInfo", statusBarInfo);
				}

				if (((CheckBox)(dialog.findViewById(R.id.general13CheckBox))).isChecked()) {
					theme.put("statusBarVisibility", "");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general14CheckBox))).isChecked()) {
					theme.put("statusIconSet", "");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general15CheckBox))).isChecked()) {
					theme.put("highlightColor", "");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general16CheckBox))).isChecked()) {
					theme.put("userActionIconColor", "");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general17CheckBox))).isChecked()) {
					theme.put("spinnerIconColor", "");
				}

				Hashtable<String, Object> data = new Hashtable<String, Object>();
				if (image.size() > 0) {
					data.put("image", image);
				}
				data.put("theme", theme);

				if (((CheckBox)(dialog.findViewById(R.id.general6CheckBox))).isChecked()) {
					data.put("brightnessLevel", "");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general7CheckBox))).isChecked()) {
					data.put("backlightTimeout", "");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).isChecked()) {
					data.put("language", "");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general18CheckBox))).isChecked()) {
					data.put("adaptiveBrightness", "");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general19CheckBox))).isChecked()) {
					data.put("capacitiveSensor", "");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general20CheckBox))).isChecked()) {
					data.put("horizontalScrollSpeed", "");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general24CheckBox))).isChecked()) {
					data.put("scrollDirection", "");
				}

				if (((CheckBox)(dialog.findViewById(R.id.general25CheckBox))).isChecked()) {
					data.put("verticalScrollSpeed", "");
				}

				bbDeviceController.readDisplaySettings(data);
				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public void promptForDeviceInfoWithKeys() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_and_spinner);
		dialog.setTitle(getString(R.string.get_info_with_key));

		dialog.findViewById(R.id.general1TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general1EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general1CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general2TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general2EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general2CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general3TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general3CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general4TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general4CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general5TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general5CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general6TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general6CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general11TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general11EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general11CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general12TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general12EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general12CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general13TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general13EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general13CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general14TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general14EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general14CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general15TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general15EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general15CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general16TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general16CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general17TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general17CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general18TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general18CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general19TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general19CheckBox).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("Device info keys");
		String[] deviceInfoKeys = new String[] { "sdkVersion", "productID", "formatID", "vendorID", "isSupportedTrack1", "isSupportedTrack2", "isSupportedTrack3", "isSupportedNfc", "batteryPercentage", "bootloaderVersion", "firmwareVersion", "isUsbConnected", "isCharging", "batteryLevel", "hardwareVersion", "pinKsn", "trackKsn", "emvKsn", "csn", "uid", "terminalSettingVersion", "deviceSettingVersion", "coprocessorVersion", "coprocessorBootloaderVersion", "serialNumber", "modelName", "macKsn", "nfcKsn", "messageKsn", "bID", "publicKeyVersion", "caKeyVersion", "df8602", "firmwareID", "bootloaderID", "mcuInfo", "supportCubeSecurityMode", "supportSPoCFeature", "isSupportedSoftwarePinPad", "backupBatteryVoltage", "rssi", "deviceTamperStatus", "iccReadSucc", "iccReadFail", "ctlReadSucc", "ctlReadFail", "msrReadSucc", "msrReadFail", "isSupportedPowerSaving", "healthCheckStatus", "internalTamperState", "pinKeyProfileID", "emvKeyProfileID", "trackKeyProfileID", "macKeyProfileID", "spocKeyProfileID", "hardwareID", "defaultLanguage", "supportedLanguages", "emvKeyModulusHash", "trackKeyModulusHash" };
		((Spinner)dialog.findViewById(R.id.general9Spinner)).setAdapter(new ArrayAdapter<String>(BaseActivity.this, android.R.layout.simple_spinner_item, deviceInfoKeys));

		((TextView)(dialog.findViewById(R.id.general10TextView))).setText("Device info keys");
		((Spinner)dialog.findViewById(R.id.general10Spinner)).setAdapter(new ArrayAdapter<String>(BaseActivity.this, android.R.layout.simple_spinner_item, deviceInfoKeys));

		((TextView)(dialog.findViewById(R.id.general21TextView))).setText("Device info keys");
		((Spinner)dialog.findViewById(R.id.general21Spinner)).setAdapter(new ArrayAdapter<String>(BaseActivity.this, android.R.layout.simple_spinner_item, deviceInfoKeys));

		((TextView)(dialog.findViewById(R.id.general22TextView))).setText("Device info keys");
		((Spinner)dialog.findViewById(R.id.general22Spinner)).setAdapter(new ArrayAdapter<String>(BaseActivity.this, android.R.layout.simple_spinner_item, deviceInfoKeys));

		((TextView)(dialog.findViewById(R.id.general23TextView))).setText("Device info keys");
		((Spinner)dialog.findViewById(R.id.general23Spinner)).setAdapter(new ArrayAdapter<String>(BaseActivity.this, android.R.layout.simple_spinner_item, deviceInfoKeys));

		dialog.findViewById(R.id.general20TextView).setVisibility(View.INVISIBLE);
		dialog.findViewById(R.id.general20EditText).setVisibility(View.INVISIBLE);
		dialog.findViewById(R.id.general20CheckBox).setVisibility(View.INVISIBLE);

		dialog.findViewById(R.id.general24TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general24CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general25TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general25CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general26TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general26CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ArrayList<String> deviceInfoKeys = new ArrayList<String>();

				if (((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).isChecked()) {
					deviceInfoKeys.add((String)((Spinner)dialog.findViewById(R.id.general9Spinner)).getSelectedItem());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general10CheckBox))).isChecked()) {
					deviceInfoKeys.add((String)((Spinner)dialog.findViewById(R.id.general10Spinner)).getSelectedItem());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general21CheckBox))).isChecked()) {
					deviceInfoKeys.add((String)((Spinner)dialog.findViewById(R.id.general21Spinner)).getSelectedItem());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general22CheckBox))).isChecked()) {
					deviceInfoKeys.add((String)((Spinner)dialog.findViewById(R.id.general22Spinner)).getSelectedItem());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general23CheckBox))).isChecked()) {
					deviceInfoKeys.add((String)((Spinner)dialog.findViewById(R.id.general23Spinner)).getSelectedItem());
				}

				bbDeviceController.getDeviceInfo(deviceInfoKeys);

				dialog.dismiss();
			}

		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		dialog.show();
	}
	
	public void checkCard() {
		Hashtable<String, Object> data = new Hashtable<String, Object>();
		if(checkCardMode != null) {
			data.put("checkCardMode", checkCardMode);
		}
		data.put("checkCardMode", checkCardMode);
		data.put("checkCardTimeout", "120");
		bbDeviceController.checkCard(data);
	}
	
	public void startEmv() {
		Hashtable<String, Object> data = new Hashtable<String, Object>();
		data.put("emvOption", EmvOption.START);
		if(checkCardMode != null) {
			data.put("checkCardMode", checkCardMode);
		}
		
		String terminalTime = new SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().getTime());
		data.put("terminalTime", terminalTime);
		if (sessionData.isTipAmountTipsPercentageCashback()) {
			if (sessionData.isCurrencyCodeForInputAmountOptionExist()) {
				data.put("currencyCode", sessionData.getCurrencyCodeForInputAmountOption());
			}
			if (sessionData.isCurrencyExponentForInputAmountOptionExist()) {
				data.put("currencyExponent", sessionData.getCurrencyExponentForInputAmountOption());
			}
			if (sessionData.isInputAmountOptionExist()) {
				data.put("inputAmountOption", sessionData.getInputAmountOption());
			}
			if (sessionData.isTipsAmountOptionsExist()) {
				data.put("cashbackAmountOptions", sessionData.getCashbackAmountOptions());
			}
			if (sessionData.isTipsPercentageOptionsExist()) {
				data.put("tipsAmountOptions", sessionData.getTipsAmountOptions());
			}
			if (sessionData.isCashbackAmountOptionsExist()) {
				data.put("tipsPercentageOptions", sessionData.getTipsPercentageOptions());
			}
			if (sessionData.isFlagHasOtherAmountOption()) {
				data.put("otherAmountOption", sessionData.getOtherAmountOption());
			}
		}
		promptForAmount(AmountInputType.AMOUNT_AND_CASHBACK,data);
//		bbDeviceController.startEmv(data);
	}

	private static String hexString2AsciiString(String hexString) {
		if (hexString == null) 
			return "";
		hexString = hexString.replaceAll(" ", "");
		if (hexString.length() % 2 != 0) {
			return "";
		}
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < hexString.length(); i+=2) {
			String str = hexString.substring(i, i+2);
			output.append((char)Integer.parseInt(str, 16));
		}
		return output.toString();
	}
	
	private static byte[] hexToByteArray(String s) {
		if (s == null) {
			s = "";
		}
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		for (int i = 0; i < s.length() - 1; i += 2) {
			String data = s.substring(i, i + 2);
			bout.write(Integer.parseInt(data, 16));
		}
		return bout.toByteArray();
	}

	private static String toHexString(byte[] b) {
		if (b == null) {
			return "null";
		}
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xFF) + 0x100, 16).substring(1);
		}
		return result;
	}

	public static void setStatus(String message) {
		String tmp = message + "\n" + statusEditText.getText();
		int maxLength = 20000;
		if (tmp.length() >= maxLength) {
			int index = tmp.indexOf("\n", maxLength);
			if ((index >= maxLength) && (index < maxLength + 1000)) {
				statusEditText.setText(tmp.substring(0, index));
				Log.i("otto", "setStatus:" + tmp.substring(0, index));
				return;
			} else {
				statusEditText.setText(tmp.substring(0, maxLength));
				Log.i("otto", "setStatus:" + tmp.substring(0, maxLength));
				return;
			}
		}
		statusEditText.setText(tmp);
	}

	protected static String toHexString(byte b) {
		return Integer.toString((b & 0xFF) + 0x100, 16).substring(1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_start_connection) {
			promptForConnection();
			return true;
		} else if (item.getItemId() == R.id.menu_stop_connection) {
			stopConnection();
			return true;
		} else if (item.getItemId() == R.id.menu_start_printer_connection) { //连接打印机
			startActivityForResult(new Intent(BaseActivity.this, BluetoothListActivity.class), BLUETOOTH_REQUEST_CODE);
			return true;
		} else if (item.getItemId() == R.id.menu_stop_printer_connection) { //断开连接打印机
			if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
					!DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
				Toast.makeText(this, R.string.please_connect_printer, Toast.LENGTH_SHORT).show();
			} else {
				mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
			}
			return true;
		} else if (item.getItemId() == R.id.menu_init_session) {
			statusEditText.setText(R.string.initializing_session);
			promptForInitSession();
		} else if (item.getItemId() == R.id.menu_reset_session) {
			statusEditText.setText(R.string.reset_session);
			bbDeviceController.resetSession();
		} else if (item.getItemId() == R.id.menu_get_deivce_info) {
			statusEditText.setText(R.string.getting_info);
			bbDeviceController.getDeviceInfo();
		} else if (item.getItemId() == R.id.menu_get_deivce_info_with_keys) {
			statusEditText.setText(R.string.getting_info);
			promptForDeviceInfoWithKeys();
		} else if (item.getItemId() == R.id.menu_get_emv_card_data) {
			statusEditText.setText(R.string.get_emv_card_data);
			bbDeviceController.getEmvCardData();
		} else if(item.getItemId() == R.id.menu_unpair_all) {
    		new Thread(new Runnable() {
				@Override
				public void run() {
					final Handler handler = new Handler(Looper.getMainLooper());
					try {
						handler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(BaseActivity.this, getString(R.string.unpair_all_start), Toast.LENGTH_SHORT).show();
							}
						});
						
						Object[] pairedObjects = BluetoothAdapter.getDefaultAdapter().getBondedDevices().toArray();
						BluetoothDevice pairedDevices;
						for (int i = 0; i < pairedObjects.length; ++i) {
							pairedDevices = (BluetoothDevice) pairedObjects[i];

							try {
								Method m = pairedDevices.getClass().getMethod("removeBond", (Class[]) null);
								m.invoke(pairedDevices, (Object[]) null);
								Thread.sleep(3000);
							} catch (Exception e) {
							}
						}
						
					} catch (Exception e) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(BaseActivity.this, getString(R.string.unpair_all_fail), Toast.LENGTH_SHORT).show();
							}
						});
						return;
					}
					
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(BaseActivity.this, getString(R.string.unpair_all_end), Toast.LENGTH_SHORT).show();
						}
					});

				}
			}).start();
    		
    		return true;
		} else if (item.getItemId() == R.id.menu_cancel_check_card) {
			statusEditText.setText(R.string.cancel_check_card);
			bbDeviceController.cancelCheckCard();
		} else if (item.getItemId() == R.id.menu_auto_config) {
			progressDialog = new ProgressDialog(currentActivity);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setTitle(R.string.auto_configuring);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(100);
			progressDialog.setIndeterminate(false);
			progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					statusEditText.setText(getString(R.string.canceling_auto_config));
					bbDeviceController.cancelAudioAutoConfig();
				}
			});
			progressDialog.show();
			bbDeviceController.startAudioAutoConfig();
		} else if (item.getItemId() == R.id.menu_ota) {
			isSwitchingActivity = true;
			finish();
			Intent intent = new Intent(this, OTAActivity.class);
			startActivity(intent);
		} else if (item.getItemId() == R.id.menu_aamva) {
			Hashtable<String, Object> data = new Hashtable<String, Object>();
			data.put("checkCardMode", CheckCardMode.SWIPE);
			data.put("checkCardTimeout", "120");
			data.put("cardType", 0);
			bbDeviceController.checkCard(data);
		} else if (item.getItemId() == R.id.menu_enable_input_amount) {
			promptForEnableInputAmount();
		} else if(item.getItemId() == R.id.menu_encrypt_pin) {
    		String encWorkingKey = encrypt(fid65WorkingKey, fid65MasterKey);
			String workingKeyKcv = encrypt("0000000000000000", fid65WorkingKey);
			
    		Hashtable<String, Object> data = new Hashtable<String, Object>();
			data.put("pin", "123456");
			data.put("pan", "123456789012345678");
			data.put("encPinKey", encWorkingKey + workingKeyKcv);
			
			bbDeviceController.encryptPin(data);
    	} else if (item.getItemId() == R.id.menu_encrypt_data) {
			String encWorkingKey = encrypt(fid65WorkingKey, fid65MasterKey);
			String workingKeyKcv = encrypt("0000000000000000", fid65WorkingKey);

			Hashtable<String, Object> data = new Hashtable<String, Object>();
			data.put("data", "0123456789ABCDEF0123456789ABCDEF");
			data.put("encWorkingKey", encWorkingKey + workingKeyKcv);
			data.put("encryptionMethod", EncryptionMethod.MAC_METHOD_1);
			data.put("encryptionKeySource", EncryptionKeySource.BY_SERVER_16_BYTES_WORKING_KEY);
			data.put("encryptionPaddingMethod", EncryptionPaddingMethod.ZERO_PADDING);
			data.put("macLength", "8");
			data.put("randomNumber", "0123456789ABCDEF");
			data.put("keyUsage", EncryptionKeyUsage.TAK);
			data.put("initialVector", "0000000000000000");
			bbDeviceController.encryptDataWithSettings(data);
		} else if (item.getItemId() == R.id.menu_print_sample) {
			receipts = new ArrayList<byte[]>();
			if (Locale.getDefault().getCountry().equalsIgnoreCase("CN")) {
				receipts.add(ReceiptUtility.genReceipt2(this));
			} else {
				receipts.add(ReceiptUtility.genReceipt(this));
			}
			bbDeviceController.startPrint(receipts.size(), 60);
		} else if (item.getItemId() == R.id.menu_main) {
			isSwitchingActivity = true;
			finish();
			Intent in = new Intent(this, MainActivity.class);
			startActivity(in);
		} else if (item.getItemId() == R.id.menu_apdu) {
			isSwitchingActivity = true;
			finish();
			Intent in = new Intent(this, ApduActivity.class);
			startActivity(in);
		} else if(item.getItemId() == R.id.capk_activity) {
    		isSwitchingActivity = true;
    		finish();
    		Intent in = new Intent(this, CAPKActivity.class);
    		startActivity(in);
		} else if(item.getItemId() == R.id.menu_gprs_wifi) {
    		isSwitchingActivity = true;
    		finish();
    		Intent in = new Intent(this, GprsWifiActivity.class);
    		startActivity(in);
		} else if(item.getItemId() == R.id.menu_nfc) {
    		isSwitchingActivity = true;
    		finish();
    		Intent in = new Intent(this, NfcActivity.class);
    		startActivity(in);
		} else if (item.getItemId() == R.id.menu_inject_session_key) {
			BaseActivity.encryptedPinSessionKey  = TripleDES.encrypt(BaseActivity.pinSessionKey, BaseActivity.masterKey);
			BaseActivity.pinKcv = TripleDES.encrypt("0000000000000000", BaseActivity.pinSessionKey);
			BaseActivity.pinKcv = BaseActivity.pinKcv.substring(0, 6);
			
			BaseActivity.encryptedDataSessionKey = TripleDES.encrypt(BaseActivity.dataSessionKey, BaseActivity.masterKey);
			BaseActivity.dataKcv = TripleDES.encrypt("0000000000000000", BaseActivity.dataSessionKey);
			BaseActivity.dataKcv = BaseActivity.dataKcv.substring(0, 6);
			
			BaseActivity.encryptedTrackSessionKey = TripleDES.encrypt(BaseActivity.trackSessionKey, BaseActivity.masterKey);
			BaseActivity.trackKcv = TripleDES.encrypt("0000000000000000", BaseActivity.trackSessionKey);
			BaseActivity.trackKcv = BaseActivity.trackKcv.substring(0, 6);
			
			BaseActivity.encryptedMacSessionKey  = TripleDES.encrypt(BaseActivity.macSessionKey , BaseActivity.masterKey);
			BaseActivity.macKcv = TripleDES.encrypt("0000000000000000", BaseActivity.macSessionKey );
			BaseActivity.macKcv = BaseActivity.macKcv.substring(0, 6);
			
			injectNextSessionKey();
		} else if (item.getItemId() == R.id.menu_powerDown) {
			statusEditText.setText(R.string.power_down);
			bbDeviceController.powerDown();
		} else if (item.getItemId() == R.id.menu_enterStandby) {
			statusEditText.setText(R.string.standby);
			bbDeviceController.enterStandbyMode();
        } else if (item.getItemId() == R.id.menu_resetDevice) {
            statusEditText.setText(R.string.reset_device);
            bbDeviceController.resetDevice();
		} else if (item.getItemId() == R.id.menu_control_led) {
			statusEditText.setText(R.string.led);
			promptForControlLED();
		} else if (item.getItemId() == R.id.menu_readAID) {
			statusEditText.setText(R.string.reading_aid);
			promptForReadAID();
		} else if (item.getItemId() == R.id.menu_updateAID) {
			statusEditText.setText(R.string.updating_aid_please_wait);
			promptForUpdateAID();
		} else if (item.getItemId() == R.id.menu_displayPrompt) {
			statusEditText.setText(R.string.display_prompt);
			promptForDisplayPrompt();
		} else if (item.getItemId() == R.id.menu_updateDisplaySettings) {
			statusEditText.setText(R.string.update_display_settings);
			promptForSetDisplayImage();
		} else if (item.getItemId() == R.id.menu_updateDisplayString) {
			statusEditText.setText(R.string.update_display_string);
			promptForUpdateDisplayString();
		} else if (item.getItemId() == R.id.menu_readDisplaySettings) {
			statusEditText.setText(R.string.read_display_settings);
			promptForReadDisplaySettings();
		} else if (item.getItemId() == R.id.menu_readDisplayString) {
			statusEditText.setText(R.string.read_display_string);
			promptForReadDisplayString();
		} else if (item.getItemId() == R.id.menu_updateTerminalSettings) {
			statusEditText.setText(R.string.update_terminal_settings);
			promptForUpdateTerminalSettings();
		} else if (item.getItemId() == R.id.menu_readTerminalSetting) {
			statusEditText.setText(R.string.read_terminal_setting);
			promptForReadTerminalSetting();
		}
		return true;
	}

	protected boolean checkBluetoothPermission() {
		if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED)) {
			return true;
		} else {
			return false;
		}
	}

	private void handleApduResult(boolean isSuccess, String apdu, int apduLength) {
		dismissDialog();

		try {
			if (isSuccess) {

				if (isApduEncrypted) {
					String key;
					if (keyMode.equals(DATA_KEY)) {
						key = DUKPTServer.GetDataKey(ksn, "0123456789ABCDEFFEDCBA9876543210");
					} else if (keyMode.equals(DATA_KEY_VAR)) {
						key = DUKPTServer.GetDataKeyVar(ksn, "0123456789ABCDEFFEDCBA9876543210");
					} else {
						key = DUKPTServer.GetPinKeyVar(ksn, "0123456789ABCDEFFEDCBA9876543210");
					}

					if (encryptionMode.equals(CBC)) {
						apdu = TripleDES.decrypt_CBC(apdu, key);
					} else {
						apdu = TripleDES.decrypt(apdu, key);
					}

					if (apduLength == 0) {
						int padding = Integer.parseInt(apdu.substring(apdu.length() - 2));
						apdu = apdu.substring(0, apdu.length() - padding * 2);
					} else {
						apdu = apdu.substring(0, apduLength * 2);
					}
				}

				setStatus(getString(R.string.apdu_result) + apdu);

				if (apdu.startsWith("61") && apdu.length() == 4) {
					sendApdu("00C00000" + apdu.substring(2));
					return;
				}

				if (state == State.GETTING_PSE) {
					if (apdu.endsWith("9000")) {
						List<TLV> tlvList = TLVParser.parse(apdu.substring(0, apdu.length() - 4));
						TLV tlv = TLVParser.searchTLV(tlvList, "88");
						if (tlv != null && tlv.value.equals("01")) {
							state = State.READING_RECORD;
							sendApdu("00B2010C00");
							// setStatus("Reading record...");
						}
					} else if (apdu.equalsIgnoreCase("6A82")) {
						aidCounter = 0;
						state = State.READING_AID;
						sendApdu("00A40400" + toHexString((byte) (aids[aidCounter].length() / 2)) + aids[aidCounter]);
						// setStatus("Get PSE Failed.");
						// setStatus("Trying to read AID " + aids[aidCounter] +
						// "...");
					}
				} else if (state == State.READING_RECORD) {
					if (apdu.endsWith("9000")) {
						List<TLV> tlvList = TLVParser.parse(apdu.substring(0, apdu.length() - 4));
						TLV tlv = TLVParser.searchTLV(tlvList, "4F");
						if (tlv != null) {
							state = State.READING_AID;
							sendApdu("00A40400" + tlv.length + tlv.value);
							// setStatus("Reading AID...");
						}
					}
				} else if (state == State.READING_AID) {
					if (apdu.endsWith("9000")) {
						List<TLV> tlvList = TLVParser.parse(apdu.substring(0, apdu.length() - 4));
						TLV tlv = TLVParser.searchTLV(tlvList, "9F38");
						state = State.GETTING_PROCESS_OPTION;
						String command = "80A800000283";
						if (tlv != null) {
							int len = 0;
							List<TLV> challenges = TLVParser.parseWithoutValue(tlv.value);
							for (int i = 0; i < challenges.size(); ++i) {
								len += Integer.parseInt(challenges.get(i).length);
							}

							command = "80A80000" + toHexString((byte) (len + 2)) + "83" + toHexString((byte) len);
							for (int i = 0; i < len; ++i) {
								command += "00";
							}
						} else {
							command += "00";
						}

						sendApdu(command);
						// setStatus("Getting Process Option...");
					} else if (apdu.equalsIgnoreCase("6A82")) {
						++aidCounter;
						if (aidCounter < aids.length) {
							sendApdu("00A40400" + toHexString((byte) (aids[aidCounter].length() / 2)) + aids[aidCounter]);
						} else {
							setStatus(getString(R.string.no_aid_matched));
						}
						// setStatus("Read AID failed");
						// setStatus("Trying to read AID " + aids[aidCounter] +
						// "...");
					}
				} else if (state == State.GETTING_PROCESS_OPTION) {
					if (apdu.endsWith("9000")) {
						List<TLV> tlvList = TLVParser.parse(apdu.substring(0, apdu.length() - 4));
						TLV tlv = TLVParser.searchTLV(tlvList, "94");
						if (tlv != null) {
							aflCounter = 0;
							afls = new String[tlv.value.length() / 8];
							for (int i = 0; i < afls.length; ++i) {
								afls[i] = tlv.value.substring(i * 8, i * 8 + 8);
							}
							readingFileIndex = Integer.parseInt(afls[aflCounter].substring(2, 4), 16);
							total = Integer.parseInt(afls[aflCounter].substring(4, 6), 16);
							sfi = toHexString((byte) (((Integer.parseInt(afls[aflCounter].substring(0, 2), 16) & 0xF8) | 0x04)));

							state = State.READING_DATA;

							sendApdu("00B2" + toHexString((byte) readingFileIndex) + sfi + "00");

							// setStatus("Reading record...");
						} else if (apdu.startsWith("80")) {
							afls = new String[(apdu.length() - 12) / 8];
							for (int i = 0; i < afls.length; ++i) {
								afls[i] = apdu.substring(i * 8 + 8, i * 8 + 16);
							}

							aflCounter = 0;
							readingFileIndex = Integer.parseInt(afls[aflCounter].substring(2, 4), 16);
							total = Integer.parseInt(afls[aflCounter].substring(4, 6), 16);
							sfi = toHexString((byte) (((Integer.parseInt(afls[aflCounter].substring(0, 2), 16) & 0xF8) | 0x04)));

							state = State.READING_DATA;

							sendApdu("00B2" + toHexString((byte) readingFileIndex) + sfi + "00");
							// setStatus("Reading record...");
						}
					}
				} else if (state == State.READING_DATA) {
					if (apdu.endsWith("9000")) {
						List<TLV> tlvList = TLVParser.parse(apdu.substring(0, apdu.length() - 4));
						TLV tlv;
						tlv = TLVParser.searchTLV(tlvList, "5F20");
						if (tlv != null) {
							cardholderName = new String(hexToByteArray(tlv.value));
						}

						tlv = TLVParser.searchTLV(tlvList, "5F24");
						if (tlv != null) {
							expiryDate = tlv.value;
						}

						tlv = TLVParser.searchTLV(tlvList, "57");
						if (tlv != null) {
							track2 = tlv.value;
						}

						tlv = TLVParser.searchTLV(tlvList, "5A");
						if (tlv != null) {
							pan = tlv.value;
						}

						if (!cardholderName.equals("") && !expiryDate.equals("") && !track2.equals("") && !pan.equals("")) {
							setStatus("");
							setStatus("Cardholder Name: " + cardholderName);
							setStatus("Expire Date: " + expiryDate);
							setStatus("Track 2: " + track2);
							setStatus("PAN: " + pan);
							if (startTime != 0) {
								setStatus((System.currentTimeMillis() - startTime) + "ms");
								startTime = 0;
							}
							return;
						}

						++readingFileIndex;
						if (readingFileIndex <= total) {
							sendApdu("00B2" + toHexString((byte) readingFileIndex) + sfi + "00");
						} else if (aflCounter < afls.length - 1) {
							++aflCounter;
							readingFileIndex = Integer.parseInt(afls[aflCounter].substring(2, 4), 16);
							total = Integer.parseInt(afls[aflCounter].substring(4, 6), 16);
							sfi = toHexString((byte) (((Integer.parseInt(afls[aflCounter].substring(0, 2), 16) & 0xF8) | 0x04)));

							state = State.READING_DATA;

							sendApdu("00B2" + toHexString((byte) readingFileIndex) + sfi + "00");
							// setStatus("Reading record...");
						}
					}
				}
				/*
				 * ++count; if(count < apduCommands.length) {
				 * 
				 * //setStatus(getString(R.string.sending) + apduCommands[count]); //emvSwipeController.sendApdu(apduCommands[count], apduCommands[count].length() / 2);
				 * 
				 * setStatus(getString(R.string.sending) + apduCommands[count]);
				 * 
				 * String command = apduCommands[count]; while((command.length() / 2) % 8 != 0) { command = command + "00"; } String encryptedCommand = TripleDES.encrypt_CBC(command, key); emvSwipeController.sendApdu(encryptedCommand, apduCommands[count].length() / 2); }
				 */
			} else {
				setStatus(getString(R.string.apdu_failed));
			}
		} catch (Exception e) {
			setStatus(e.getMessage());
			StackTraceElement[] elements = e.getStackTrace();
			for (int i = 0; i < elements.length; ++i) {
				setStatus(elements[i].toString());
			}
		}
	}

	protected void sendApdu(String command) {
		try {
			if (isApduEncrypted) {
				String key;
				if (keyMode.equals(DATA_KEY)) {
					key = DUKPTServer.GetDataKey(ksn, "0123456789ABCDEFFEDCBA9876543210");
				} else if (keyMode.equals(DATA_KEY_VAR)) {
					key = DUKPTServer.GetDataKeyVar(ksn, "0123456789ABCDEFFEDCBA9876543210");
				} else {
					key = DUKPTServer.GetPinKeyVar(ksn, "0123456789ABCDEFFEDCBA9876543210");
				}

				String temp = command;
				if (isPKCS7) {
					int padding = 8 - (temp.length() / 2) % 8;
					for (int i = 0; i < padding; ++i) {
						temp += "0" + padding;
					}
				} else {
					while ((temp.length() / 2) % 8 != 0) {
						temp += "00";
					}
				}

				String encryptedCommand;

				if (encryptionMode.equals(CBC)) {
					encryptedCommand = TripleDES.encrypt_CBC(temp, key);
				} else {
					encryptedCommand = TripleDES.encrypt(temp, key);
				}

				Hashtable<String, Object> apduInput = new Hashtable<String, Object>();
				apduInput.put("apdu", encryptedCommand);
				if (isPKCS7) {
					bbDeviceController.sendApdu(apduInput);
				} else {
					apduInput.put("apduLength", command.length() / 2);
					bbDeviceController.sendApdu(apduInput);
				}

				setStatus(getString(R.string.sending) + command);
			} else {
				Hashtable<String, Object> apduInput = new Hashtable<String, Object>();
				apduInput.put("apdu", command);
				apduInput.put("apduLength", command.length() / 2);
				bbDeviceController.sendApdu(apduInput);
			}
		} catch (Exception e) {
			setStatus(e.getMessage());
			StackTraceElement[] elements = e.getStackTrace();
			for (int i = 0; i < elements.length; ++i) {
				setStatus(elements[i].toString());
			}
		}
	}

	class MyBBDeviceControllerListener implements BBDeviceControllerListener {

		@Override
		public void onWaitingForCard(CheckCardMode checkCardMode) {
			dismissDialog();
			switch (checkCardMode) {
			case INSERT:
				setStatus(getString(R.string.please_insert_card));
				break;
			case SWIPE:
				setStatus(getString(R.string.please_swipe_card));
				break;
			case SWIPE_OR_INSERT:
				setStatus(getString(R.string.please_swipe_insert_card));
				break;
			case TAP:
				setStatus(getString(R.string.please_tap_card));
				break;
			case SWIPE_OR_TAP:
				setStatus(getString(R.string.please_swipe_tap_card));
				break;
			case INSERT_OR_TAP:
				setStatus(getString(R.string.please_insert_tap_card));
				break;
			case SWIPE_OR_INSERT_OR_TAP:
				setStatus(getString(R.string.please_swipe_insert_tap_card));
				break;
			default:
				break;
			}
		}

		@Override
		public void onWaitingReprintOrPrintNext() {
			statusEditText.setText(statusEditText.getText() + "\n" + getString(R.string.please_press_reprint_or_print_next));
		}

		@Override
		public void onBTConnected(BluetoothDevice bluetoothDevice) {
			statusEditText.setText(getString(R.string.bluetooth_connected) + ": " + bluetoothDevice.getAddress());
			posText.setText(getString(R.string.pos_connected));
			posConnect = true;
			sessionData.reset();
			bbDeviceController.getDeviceInfo();
		}

		@Override
		public void onBTDisconnected() {
			statusEditText.setText(getString(R.string.bluetooth_disconnected));
			posText.setText(getString(R.string.pos_not_connect));
			sessionData.reset();
		}

		@Override
		public void onBTReturnScanResults(List<BluetoothDevice> foundDevices) {
			currentActivity.foundDevices = foundDevices;
			if (arrayAdapter != null) {
				arrayAdapter.clear();
				for (int i = 0; i < foundDevices.size(); ++i) {
					arrayAdapter.add(foundDevices.get(i).getName());
				}
				arrayAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onBTScanStopped() {
			statusEditText.setText(getString(R.string.bluetooth_scan_stopped));
		}

		@Override
		public void onBTScanTimeout() {
			statusEditText.setText(getString(R.string.bluetooth_scan_timeout));
		}

		@Override
		public void onBTRequestPairing() {
			statusEditText.setText(getString(R.string.request_pairing));
		}

		@Override
		public void onReturnCheckCardResult(CheckCardResult checkCardResult, Hashtable<String, String> decodeData) {
			dismissDialog();
			String content = "CheckCardResult : " + checkCardResult;
			//存储需要打印的交易结果内容到printContextMap，并调用printReceipt方法激活打印功能
			Map<String, Object> printContextMap = new HashMap<>();
			printContextMap.put("amount", ("".equals(amount)?"0":amount) );
			if (decodeData != null) {
				Object[] keys = decodeData.keySet().toArray();
				Arrays.sort(keys);
				for (Object key : keys) {
					content += "\n" + (String)key + " : ";
					Object obj = decodeData.get(key);
					if (obj instanceof String) {
						content += (String)obj;
					}
				}
				transactionStatusTitleTv.setText(getString(R.string.transaction_amount_received));
				transactionStatusBtn.setText(getString(R.string.transaction_completed));
				transactionAmountTv.setText(" $ "+amountDigitalCheck(amount));
				transactionStatusBtn.setClickable(true);
				trnCompleted = true;
				printReceipt(printContextMap);
				Log.i("otto", "onReturnCheckCardResult:" + content);
				statusEditText.setText(content);
			}
		}

		@Override
		public void onReturnCancelCheckCardResult(boolean isSuccess) {
			if (isSuccess) {
				statusEditText.setText(R.string.cancel_check_card_success);
			} else {
				statusEditText.setText(R.string.cancel_check_card_fail);
			}
		}

		@Override
		public void onReturnDeviceInfo(Hashtable<String, String> deviceInfoData) {
			dismissDialog();
			uid = deviceInfoData.get("uid");
			String productId = deviceInfoData.get("productID");
			sessionData.setProductId(productId);

			String content = "";
			Object[] keys = deviceInfoData.keySet().toArray();
			Arrays.sort(keys);
			for (Object key : keys) {
				content += "\n" + (String)key + " : ";
				Object obj = deviceInfoData.get(key);
				content += (String)obj;
				if (((String)key).equalsIgnoreCase("vendorID")) {
					try {
						String vendorID = deviceInfoData.get("vendorID");
						String vendorIDAscii = "";
						if ((vendorID != null) && (!vendorID.equals(""))) {
							if (!vendorID.substring(0, 2).equalsIgnoreCase("00")) {
								vendorIDAscii = Utils.hexString2AsciiString(vendorID);
								content += "\n" + (String)key + " (ASCII) : " + vendorIDAscii;
							}
						}
					} catch (Exception e) {
					}
				}
			}
			Log.i("otto", "onReturnDeviceInfo:" + content);
			statusEditText.setText(content);

			if (Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("CHB8")
					|| Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPC3")
					|| Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPC4")
					|| Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPS3")
					|| Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPD3")) {
				if (tipAmountTipsPercentageCashbackCheckBox != null) {
					tipAmountTipsPercentageCashbackCheckBox.setVisibility(View.VISIBLE);
				}
			} else {
				if (tipAmountTipsPercentageCashbackCheckBox != null) {
					tipAmountTipsPercentageCashbackCheckBox.setVisibility(View.GONE);
				}
			}
		}

		@Override
		public void onReturnTransactionResult(TransactionResult transactionResult) {
			dismissDialog();
			dialog = new Dialog(currentActivity);
			dialog.setContentView(R.layout.alert_dialog);
			dialog.setTitle(R.string.transaction_result);
			TextView messageTextView = (TextView) dialog.findViewById(R.id.messageTextView);

			String message = "" + transactionResult + "\n";
			if (transactionResult == TransactionResult.APPROVED) {
				message = getString(R.string.amount) + ": $" + amount + "\n";
				if (!cashbackAmount.equals("")) {
					message += getString(R.string.cashback_amount) + ": $" + cashbackAmount;
				}
			}
			Log.i("otto", "onReturnTransactionResult:" + message);
			//存储需要打印的交易结果内容到printContextMap，并调用printReceipt方法激活打印功能
//			Map<String, Object> printContextMap = new HashMap<>();
//			printContextMap.put("amount", ("".equals(amount)?"0":amount) );
//			printReceipt(printContextMap);
//			messageTextView.setText(message);

			amount = "";
			cashbackAmount = "";
			amountEditText.setText("");

			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dismissDialog();
				}
			});

			dialog.show();
		}

		@Override
		public void onReturnBatchData(String tlv) {
			dismissDialog();
			String content = getString(R.string.batch_data) + "\n";
			Hashtable<String, String> decodeData = BBDeviceController.decodeTlv(tlv);
			Object[] keys = decodeData.keySet().toArray();
			Arrays.sort(keys);
			for (Object key : keys) {
				String value = decodeData.get(key);
				content += key + ": " + value + "\n";
			}
			Log.i("otto", "onReturnBatchData:" + content);
			statusEditText.setText(content);
		}

		@Override
		public void onReturnReversalData(String tlv) {
			dismissDialog();
			String content = getString(R.string.reversal_data);
			content += tlv;
			Log.i("otto", "onReturnReversalData:" + content);
			statusEditText.setText(content);
		}

		@Override
		public void onReturnAmountConfirmResult(boolean isSuccess) {
			if (isSuccess) {
				statusEditText.setText(getString(R.string.amount_confirmed));
			} else {
				statusEditText.setText(getString(R.string.amount_not_confirmed));
			}
		}

		@Override
		public void onReturnPinEntryResult(PinEntryResult pinEntryResult, Hashtable<String, String> data) {
			if (pinEntryResult == PinEntryResult.ENTERED) {
				String content = getString(R.string.pin_entered);
				if (data.containsKey("epb")) {
					content += "\n" + getString(R.string.epb) + data.get("epb");
				}
				if (data.containsKey("ksn")) {
					content += "\n" + getString(R.string.ksn) + data.get("ksn");
				}
				if (data.containsKey("randomNumber")) {
					content += "\n" + getString(R.string.random_number) + data.get("randomNumber");
				}
				if (data.containsKey("encWorkingKey")) {
					content += "\n" + getString(R.string.encrypted_working_key) + data.get("encWorkingKey");
				}

				Log.i("otto", "onReturnPinEntryResult:" + content);
				statusEditText.setText(content);
			} else if (pinEntryResult == PinEntryResult.BYPASS) {
				statusEditText.setText(getString(R.string.pin_bypassed));
			} else if (pinEntryResult == PinEntryResult.CANCEL) {
				statusEditText.setText(getString(R.string.pin_canceled));
			} else if (pinEntryResult == PinEntryResult.TIMEOUT) {
				statusEditText.setText(getString(R.string.pin_timeout));
			}
		}

		@Override
		public void onReturnPrintResult(PrintResult printResult) {
			Log.i("otto", "onReturnPrintResult:" + printResult);
			statusEditText.setText("" + printResult);
		}

		@Override
		public void onReturnAmount(Hashtable<String, String> data) {
			String amount = data.get("amount");
			String cashbackAmount = data.get("cashbackAmount");
			String currencyCode = data.get("currencyCode");
			String tipsAmount = data.get("tipsAmount");

			String text = "";
			text += getString(R.string.amount_with_colon) + amount + "\n";
            text += getString(R.string.tips_with_colon) + tipsAmount + "\n";
			text += getString(R.string.cashback_with_colon) + cashbackAmount + "\n";
			text += getString(R.string.currency_with_colon) + currencyCode + "\n";

			Log.i("otto", "onReturnAmount:" + text);
			statusEditText.setText(text);
		}

		@Override
		public void onReturnUpdateTerminalSettingResult(TerminalSettingStatus terminalSettingStatus) {
			dismissDialog();
			if (terminalSettingStatus == TerminalSettingStatus.SUCCESS) {
				statusEditText.setText(getString(R.string.update_terminal_setting_success));
			} else if (terminalSettingStatus == TerminalSettingStatus.TAG_NOT_FOUND) {
				statusEditText.setText(getString(R.string.update_terminal_setting_tag_not_found));
			} else if (terminalSettingStatus == TerminalSettingStatus.LENGTH_INCORRECT) {
				statusEditText.setText(getString(R.string.update_terminal_setting_length_incorrect));
			} else if (terminalSettingStatus == TerminalSettingStatus.TLV_INCORRECT) {
				statusEditText.setText(getString(R.string.update_terminal_setting_tlv_incorrect));
			} else if (terminalSettingStatus == TerminalSettingStatus.BOOTLOADER_NOT_SUPPORT) {
				statusEditText.setText(getString(R.string.update_terminal_setting_bootloader_not_support));
			} else if (terminalSettingStatus == TerminalSettingStatus.TAG_NOT_ALLOWED_TO_ACCESS) {
				statusEditText.setText(getString(R.string.update_terminal_setting_tag_not_allowed_to_change));
			} else if (terminalSettingStatus == TerminalSettingStatus.USER_DEFINED_DATA_NOT_ENALBLED) {
				statusEditText.setText(getString(R.string.update_terminal_setting_user_defined_data_not_allowed_to_change));
			} else if (terminalSettingStatus == TerminalSettingStatus.TAG_NOT_WRITTEN_CORRECTLY) {
				statusEditText.setText(getString(R.string.update_terminal_setting_tag_not_written_correctly));
			}
		}

		@Override
		public void onReturnUpdateTerminalSettingsResult(Hashtable<String, TerminalSettingStatus> data) {
			String text = getString(R.string.update_terminal_settings);

			Object[] keys = data.keySet().toArray();
			Arrays.sort(keys);
			for (Object key : keys) {
				text += "\n" + (String)key + " : ";
				Object obj = data.get(key);
				if (obj instanceof String) {
					text += (String)obj;
				} else if (obj instanceof Boolean) {
					text += ((Boolean)obj) + "";
				} else if (obj instanceof TerminalSettingStatus) {
					text += ((TerminalSettingStatus)obj) + "";
				}
			}

			Log.i("otto", "onReturnUpdateTerminalSettingsResult:" + text);
			statusEditText.setText(text);
		}

		@Override
		public void onReturnReadTerminalSettingResult(Hashtable<String, Object> data) {
			dismissDialog();
			String text = getString(R.string.read_terminal_setting);

			Object[] keys = data.keySet().toArray();
			Arrays.sort(keys);
			for (Object key : keys) {
				text += "\n" + (String)key + " : ";
				Object obj = data.get(key);
				if (obj instanceof String) {
					text += (String)obj;
				} else if (obj instanceof Boolean) {
					text += ((Boolean)obj) + "";
				} else if (obj instanceof TerminalSettingStatus) {
					text += ((TerminalSettingStatus)obj) + "";
				}
			}

			Log.i("otto", "onReturnReadTerminalSettingResult:" + text);
			setStatus(text);
		}

		@Override
		public void onReturnEnableInputAmountResult(boolean isSuccess) {
			if (isSuccess) {
				statusEditText.setText(getString(R.string.enable_input_amount_success));
			} else {
				statusEditText.setText(getString(R.string.enable_input_amount_fail));
			}
		}

		@Override
		public void onReturnDisableInputAmountResult(boolean isSuccess) {
			if (isSuccess) {
				statusEditText.setText(getString(R.string.disable_input_amount_success));
			} else {
				statusEditText.setText(getString(R.string.disable_input_amount_fail));
			}
		}

		@Override
		public void onReturnPhoneNumber(PhoneEntryResult phoneEntryResult, String phoneNumber) {
			if (phoneEntryResult == PhoneEntryResult.ENTERED) {
				statusEditText.setText(getString(R.string.phone_number) + " " + phoneNumber);
			} else if (phoneEntryResult == PhoneEntryResult.TIMEOUT) {
				statusEditText.setText(getString(R.string.timeout));
			} else if (phoneEntryResult == PhoneEntryResult.CANCEL) {
				statusEditText.setText(getString(R.string.canceled));
			} else if (phoneEntryResult == PhoneEntryResult.WRONG_LENGTH) {
				statusEditText.setText(getString(R.string.wrong_length));
			} else if (phoneEntryResult == PhoneEntryResult.BYPASS) {
				statusEditText.setText(getString(R.string.bypass));
			}
		}

		@Override
		public void onReturnEmvCardDataResult(boolean isSuccess, String tlv) {
			if (isSuccess) {
				statusEditText.setText(getString(R.string.emv_card_data_result) + tlv);
			} else {
				statusEditText.setText(getString(R.string.emv_card_data_failed));
			}
		}

		@Override
		public void onReturnEmvCardNumber(boolean isSuccess, String cardNumber) {
			Log.i("otto", "onReturnEmvCardNumber:" + cardNumber);
			statusEditText.setText(getString(R.string.pan) + cardNumber);
		}

		@Override
		public void onReturnEncryptPinResult(boolean isSuccess, Hashtable<String, String> data) {
			String ksn = data.get("ksn");
			String epb = data.get("epb");
			String randomNumber = data.get("randomNumber");
			String encWorkingKey = data.get("encWorkingKey");
			String errorMessage = data.get("errorMessage");
			String content = getString(R.string.ksn) + ksn + "\n";
			content += getString(R.string.epb) + epb + "\n";
			content += getString(R.string.random_number) + randomNumber + "\n";
			content += getString(R.string.encrypted_working_key) + encWorkingKey + "\n";
			content += getString(R.string.error_message) + errorMessage;
			Log.i("otto", "onReturnEncryptPinResult:" + content);
			statusEditText.setText(content);
		}

		@Override
		public void onReturnEncryptDataResult(boolean isSuccess, Hashtable<String, String> data) {
			if (isSuccess) {
				String content = "";
				if (data.containsKey("ksn")) {
					content += getString(R.string.ksn) + data.get("ksn") + "\n";
				}
				if (data.containsKey("randomNumber")) {
					content += getString(R.string.random_number) + data.get("randomNumber") + "\n";
				}
				if (data.containsKey("encData")) {
					content += getString(R.string.encrypted_data) + data.get("encData") + "\n";
				}
				if (data.containsKey("mac")) {
					content += getString(R.string.mac) + data.get("mac") + "\n";
				}
				Log.i("otto", "onReturnEncryptDataResult:" + content);
				statusEditText.setText(content);
			} else {
				statusEditText.setText(getString(R.string.encrypt_data_failed));
			}
		}
		
		@Override
		public void onReturnInjectSessionKeyResult(boolean isSuccess, Hashtable<String, String> data) {
			String content;
			if (isSuccess) {
				content = getString(R.string.inject_session_key_success);
				if (data.size() == 0) {
					injectNextSessionKey();
				}
			} else {
				content = getString(R.string.inject_session_key_failed);
				content += "\n" + getString(R.string.error_message) + data.get("errorMessage");
			}
			Log.i("otto", "onReturnInjectSessionKeyResult:" + content);
			setStatus(content);
		}

		@Override
		public void onReturnApduResult(boolean isSuccess, Hashtable<String, Object> data) {
			try {
				String apdu = "";
				int apduLength = 0;
				
				if ((data != null) && (data.containsKey("apduLength")) && (data.get("apduLength") instanceof String)) {
					apduLength = Integer.parseInt((String)data.get("apduLength"));
				} else if ((data != null) && (data.containsKey("apduLength")) && (data.get("apduLength") instanceof Integer)) {
					apduLength = (Integer)data.get("apduLength");
				}
				
				if ((data != null) && (data.containsKey("apdu"))) {
					apdu = (String)data.get("apdu");
					handleApduResult(isSuccess, apdu, apduLength);
				}
			} catch (Exception e) {
				
			}
		}

		@Override
		public void onReturnPowerOffIccResult(boolean isSuccess) {
			dismissDialog();
			if (isSuccess) {
				setStatus(getString(R.string.power_off_icc_success));
			} else {
				setStatus(getString(R.string.power_off_icc_failed));
			}
		}

		@Override
		public void onReturnPowerOnIccResult(boolean isSuccess, String ksn, String atr, int atrLength) {
			dismissDialog();
			if (isSuccess) {
				BaseActivity.ksn = ksn;

				setStatus(getString(R.string.power_on_icc_success));
				setStatus(getString(R.string.ksn) + ksn);
				setStatus(getString(R.string.atr) + atr);
				setStatus(getString(R.string.atr_length) + atrLength);
			} else {
				setStatus(getString(R.string.power_on_icc_failed));
			}
		}

		@Override
		public void onRequestSelectApplication(ArrayList<String> appList) {
			dismissDialog();

			dialog = new Dialog(currentActivity);
			dialog.setContentView(R.layout.application_dialog);
			dialog.setTitle(R.string.please_select_app);
			dialog.setCanceledOnTouchOutside(false);

			String[] appNameList = new String[appList.size()];
			for (int i = 0; i < appNameList.length; ++i) {
				appNameList[i] = appList.get(i);
			}

			appListView = (ListView) dialog.findViewById(R.id.appList);
			appListView.setAdapter(new ArrayAdapter<String>(currentActivity, android.R.layout.simple_list_item_1, appNameList));
			appListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					bbDeviceController.selectApplication(position);
					dismissDialog();
				}

			});

			dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					bbDeviceController.cancelSelectApplication();
					dismissDialog();
				}
			});
			dialog.show();
		}

		@Override
		public void onRequestSelectAccountType() {
			dismissDialog();

			dialog = new Dialog(currentActivity);
			dialog.setContentView(R.layout.application_dialog);
			dialog.setTitle(R.string.please_select_account_type);

			String[] appNameList = {"Default", "Saving", "Cheque", "Credit"};

			appListView = (ListView) dialog.findViewById(R.id.appList);
			appListView.setAdapter(new ArrayAdapter<String>(currentActivity, android.R.layout.simple_list_item_1, appNameList));
			appListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					bbDeviceController.selectAccountType(position);
					dismissDialog();
				}

			});

			dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					bbDeviceController.cancelSelectAccountType();
					dismissDialog();
				}
			});
			dialog.show();
		}

		@Override
		public void onRequestSetAmount() {
//			promptForAmount(AmountInputType.AMOUNT_AND_CASHBACK);
		}

		@Override
		public void onRequestOtherAmount(AmountInputType amountInputType) {
			promptForAmount(amountInputType,null);
		}

		@Override
		public void onRequestPinEntry(PinEntrySource pinEntrySource) {
			dismissDialog();
			if (pinEntrySource == PinEntrySource.KEYPAD) {
				statusEditText.setText(getString(R.string.enter_pin_on_keypad));
			} else {
				dismissDialog();

				dialog = new Dialog(currentActivity);
				dialog.setContentView(R.layout.pin_dialog);
				dialog.setTitle(getString(R.string.enter_pin));
				dialog.setCanceledOnTouchOutside(false);

				dialog.findViewById(R.id.confirmButton).setOnClickListener(
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								String pin = ((EditText) dialog.findViewById(R.id.pinEditText)).getText().toString();
								bbDeviceController.sendPinEntryResult(pin);
								dismissDialog();
							}
						});

				dialog.findViewById(R.id.bypassButton).setOnClickListener(
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								bbDeviceController.bypassPinEntry();
								dismissDialog();
							}
						});

				dialog.findViewById(R.id.cancelButton).setOnClickListener(
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								isPinCanceled = true;
								bbDeviceController.cancelPinEntry();
								dismissDialog();
							}
						});

				dialog.show();
			}
		}

		@Override
		public void onReturnSetPinPadButtonsResult(boolean isSuccess) {
			dismissDialog();
		}

		@Override
		public void onReturnSetPinPadOrientationResult(boolean b) {
			dismissDialog();
		}

		@Override
		public void onReturnUpdateDisplaySettingsProgress(double v) {
			setStatus(getString(R.string.update_display_settings_progress) + v);
		}

		@Override
		public void onReturnUpdateDisplaySettingsResult(boolean b, String s) {
			setStatus(getString(R.string.update_display_settings_result) + b + " " + s);
		}

		@Override
		public void onReturnReadDisplaySettingsResult(boolean isSuccess, Hashtable<String, Object> data) {
			setStatus(getString(R.string.read_display_settings_result) + isSuccess + "\n" + data);
		}

		@Override
		public void onRequestOnlineProcess(String tlv) {
			String content = getString(R.string.request_data_to_server) + "\n";
			Hashtable<String, String> decodeData = BBDeviceController.decodeTlv(tlv);
			Object[] keys = decodeData.keySet().toArray();
			Arrays.sort(keys);
			for (Object key : keys) {
				String value = decodeData.get(key);
				content += key + ": " + value + "\n";
			}
			statusEditText.setText(content);

			dismissDialog();
			dialog = new Dialog(currentActivity);
			dialog.setContentView(R.layout.alert_dialog);
			dialog.setTitle(R.string.request_data_to_server);
			dialog.setCanceledOnTouchOutside(false);

			if (isPinCanceled) {
				((TextView) dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_failed);
			} else {
				((TextView) dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_success);
			}

			dialog.findViewById(R.id.confirmButton).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (isPinCanceled) {
								bbDeviceController.sendOnlineProcessResult(null);
							} else {
								bbDeviceController.sendOnlineProcessResult("8A023030");
							}
							dismissDialog();
						}
					});

			dialog.show();
		}

		@Override
		public void onRequestTerminalTime() {
			dismissDialog();
			String terminalTime = new SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().getTime());
			bbDeviceController.sendTerminalTime(terminalTime);
			statusEditText.setText(getString(R.string.request_terminal_time) + " " + terminalTime);
		}

		@Override
		public void onRequestDisplayText(DisplayText displayText, String displayTextLanguage) {
			if (displayText == DisplayText.ENTER_AMOUNT) {
			} else {
				dismissDialog();
			}
			setStatus("" + displayText + ", " + displayTextLanguage);
		}

		@Override
		public void onRequestClearDisplay() {
			dismissDialog();
			statusEditText.setText("");
		}

		@Override
		public void onRequestFinalConfirm() {
			dismissDialog();
			if (!isPinCanceled) {
				dialog = new Dialog(currentActivity);
				dialog.setContentView(R.layout.confirm_dialog);
				dialog.setTitle(getString(R.string.confirm_amount));
				dialog.setCanceledOnTouchOutside(false);

				String message = getString(R.string.amount) + ": $" + amount;
				if (!cashbackAmount.equals("")) {
					message += "\n" + getString(R.string.cashback_amount) + ": $" + cashbackAmount;
				}

				((TextView) dialog.findViewById(R.id.messageTextView)).setText(message);

				dialog.findViewById(R.id.confirmButton).setOnClickListener(
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								bbDeviceController.sendFinalConfirmResult(true);
								dialog.dismiss();
							}
						});

				dialog.findViewById(R.id.cancelButton).setOnClickListener(
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								bbDeviceController.sendFinalConfirmResult(false);
								dialog.dismiss();
							}
						});

				dialog.show();
			} else {
				bbDeviceController.sendFinalConfirmResult(false);
			}
		}

		@Override
		public void onRequestAmountConfirm(Hashtable<String, String> data) {
			dismissDialog();
			dialog = new Dialog(currentActivity);
			dialog.setContentView(R.layout.confirm_dialog);
			dialog.setTitle(getString(R.string.confirm_amount));
			dialog.setCanceledOnTouchOutside(false);

			String amount = data.get("amount");
			String cashbackAmount = data.get("cashbackAmount");
			String currencyCode = data.get("currencyCode");
			String tipsAmount = data.get("tipsAmount");
			String content = getString(R.string.amount_with_colon) + amount + "\n";
			content += getString(R.string.tips_with_colon) + tipsAmount + "\n";
			content += getString(R.string.cashback_with_colon) + cashbackAmount + "\n";
			content += getString(R.string.currency_with_colon) + currencyCode;

			((TextView) dialog.findViewById(R.id.messageTextView)).setText(content);

			dialog.findViewById(R.id.confirmButton).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						bbDeviceController.setAmountConfirmResult(true);
						dialog.dismiss();
					}
				});
			dialog.findViewById(R.id.cancelButton).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						bbDeviceController.setAmountConfirmResult(false);
						dialog.dismiss();
					}
				});

			dialog.show();
		}

		@Override
		public void onRequestPrintData(int index, boolean isReprint) {
			bbDeviceController.sendPrintData(receipts.get(index));
			if (isReprint) {
				statusEditText.setText(getString(R.string.request_reprint_data) + index);
			} else {
				statusEditText.setText(getString(R.string.request_printer_data) + index);
			}
		}

		@Override
		public void onPrintDataCancelled() {
			statusEditText.setText(getString(R.string.printer_operation_cancelled));
		}

		@Override
		public void onPrintDataEnd() {
			statusEditText.setText(getString(R.string.printer_operation_end));
		}
		
		@Override
		public void onBatteryLow(BatteryStatus batteryStatus) {
			if (batteryStatus == BatteryStatus.LOW) {
				statusEditText.setText(getString(R.string.battery_low));
			} else if (batteryStatus == BatteryStatus.CRITICALLY_LOW) {
				statusEditText.setText(getString(R.string.battery_critically_low));
			}
		}

		@Override
		public void onAudioDevicePlugged() {
			statusEditText.setText(getString(R.string.device_plugged));
		}

		@Override
		public void onAudioDeviceUnplugged() {
			statusEditText.setText(getString(R.string.device_unplugged));
		}

		@Override
		public void onError(Error errorState, String errorMessage) {
			dismissDialog();
			if(progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			amountEditText.setText("");

			String content = "" + errorState;
			if (errorState == Error.INPUT_INVALID_FORMAT) {
				Toast.makeText(currentActivity, getString(R.string.invalid_format), Toast.LENGTH_LONG).show();
			} else if (errorState == Error.INPUT_INVALID) {
				Toast.makeText(currentActivity, getString(R.string.input_invalid), Toast.LENGTH_LONG).show();
			} else if (errorState == Error.CASHBACK_NOT_SUPPORTED) {
				Toast.makeText(currentActivity, getString(R.string.cashback_not_supported), Toast.LENGTH_LONG).show();
			}

			if (errorMessage != null && !errorMessage.equals("")) {
				content += "\n" + getString(R.string.error_message) + errorMessage;
			}

			statusEditText.setText(content);
		}

		@Override
		public void onReturnCAPKList(List<CAPK> capkList) {
			String content = getString(R.string.capk);
			for (int i = 0; i < capkList.size(); ++i) {
				CAPK capk = capkList.get(i);
				content += "\n" + i + ": ";
				content += "\n" + getString(R.string.location) + capk.location;
				content += "\n" + getString(R.string.rid) + capk.rid;
				content += "\n" + getString(R.string.index) + capk.index;
				content += "\n";
			}
			Log.i("otto", "onReturnCAPKList:" + content);
			setStatus(content);
		}
		
		@Override
		public void onReturnCAPKDetail(CAPK capk) {
			String content = getString(R.string.capk);
			if (capk != null) {
				content += "\n" + getString(R.string.location) + capk.location;
				content += "\n" + getString(R.string.rid) + capk.rid;
				content += "\n" + getString(R.string.index) + capk.index;
				content += "\n" + getString(R.string.exponent) + capk.exponent;
				content += "\n" + getString(R.string.modulus) + capk.modulus;
				content += "\n" + getString(R.string.checksum) + capk.checksum;
				content += "\n" + getString(R.string.size) + capk.size;
				content += "\n";
			} else {
				content += "\nnull \n";
			}
			Log.i("otto", "onReturnCAPKDetail:" + content);
			setStatus(content);
		}

		@Override
		public void onReturnCAPKLocation(String location) {
			Log.i("otto", "onReturnCAPKLocation:" + location);
			setStatus(getString(R.string.location) + location);
		}
		
		@Override
		public void onReturnUpdateCAPKResult(boolean isSuccess) {
			if (isSuccess) {
				setStatus(getString(R.string.update_capk_success));
			} else {
				setStatus(getString(R.string.update_capk_fail));
			}
		}

		@Override
		public void onReturnRemoveCAPKResult(boolean isSuccess) {
			setStatus("[onReturnRemoveCAPKResult]");
			if (isSuccess) {
				setStatus(getString(R.string.remove_capk_success));
			} else {
				setStatus(getString(R.string.remove_capk_fail));
			}
		}

		@Override
		public void onReturnEmvReport(String tlv) {
			String content = getString(R.string.emv_report) + "\n";

			Hashtable<String, String> decodeData = BBDeviceController.decodeTlv(tlv);
			Object[] keys = decodeData.keySet().toArray();
			Arrays.sort(keys);
			for (Object key : keys) {
				if (((String) key).matches(".*[a-z].*") && decodeData.containsKey(((String) key).toUpperCase(Locale.ENGLISH))) {
					continue;
				}
				String value = decodeData.get(key);
				content += key + ": " + value + "\n";
				
				if (((String) key).toUpperCase(Locale.ENGLISH).equalsIgnoreCase(TagList.EMV_REPORT_TEMPLATE)) {
					Hashtable<String, String> innerDecodeData = BBDeviceController.decodeTlv(value);
					Object[] innerKeys = innerDecodeData.keySet().toArray();
					Arrays.sort(innerKeys);
					for (Object innerKey : innerKeys) {
						if (((String) innerKey).matches(".*[a-z].*") && innerDecodeData.containsKey(((String) innerKey).toUpperCase(Locale.ENGLISH))) {
							continue;
						}
						String innerValue = innerDecodeData.get(innerKey);
						content += "\n" + innerKey + ": " + innerValue;						
					}
				}
			}

			Log.i("otto", "onReturnEmvReport:" + content);
			setStatus(content);
		}

		@Override
		public void onReturnEmvReportList(Hashtable<String, String> data) {
			String content = getString(R.string.emv_report_list) + "\n";
			Object[] keys = data.keySet().toArray();
			Arrays.sort(keys);
			for (Object key : keys) {
				String value = data.get(key);
				content += key + ": " + value + "\n";
			}

			Log.i("otto", "onReturnEmvReportList:" + content);
			setStatus(content);
		}
		
		@Override
		public void onSessionInitialized() {
			setStatus(getString(R.string.session_initialized));
		}

		@Override
		public void onSessionError(SessionError sessionError, String errorMessage) {
			if (sessionError == SessionError.FIRMWARE_NOT_SUPPORTED) {
				setStatus(getString(R.string.session_error_firmware_not_supported));
			} else if (sessionError == SessionError.INVALID_SESSION) {
				setStatus(getString(R.string.session_error_invalid_session));
			} else if (sessionError == SessionError.INVALID_VENDOR_TOKEN) {
				setStatus(getString(R.string.session_error_invalid_vendor_token));
			} else if (sessionError == SessionError.SESSION_NOT_INITIALIZED) {
				setStatus(getString(R.string.session_error_session_not_initialized));
			}
			setStatus(getString(R.string.error_message) + errorMessage);
		}

		@Override
		public void onReturnDebugLog(Hashtable<String, Object> hashtable) { }

		@Override
		public void onReturnReadGprsSettingsResult(boolean isSuccess, Hashtable<String, Object> data) {
			if (isSuccess) {
				String text = getString(R.string.read_gprs_setting_success);
				text += "\n" + getString(R.string.operator) + (String)data.get("operator");
				text += "\n" + getString(R.string.apn) + (String)data.get("apn");
				text += "\n" + getString(R.string.username) + (String)data.get("username");
				text += "\n" + getString(R.string.password) + (String)data.get("password");
				setStatus(text);
			} else {
				String text = getString(R.string.read_gprs_setting_fail);
				TerminalSettingStatus terminalSettingStatus = (TerminalSettingStatus)data.get("gprs");
				switch (terminalSettingStatus) {
				case SUCCESS:
					setStatus(getString(R.string.read_terminal_setting_success));
					break;
				case LENGTH_INCORRECT:
					setStatus(getString(R.string.length_incorrect));
					break;
				case TLV_INCORRECT:
					setStatus(getString(R.string.tlv_incorrect));
					break;
				case TAG_NOT_FOUND:
					setStatus(getString(R.string.tag_not_found));
					break;
				case BOOTLOADER_NOT_SUPPORT:
					setStatus(getString(R.string.bootloader_not_support));
					break;
				case TAG_NOT_ALLOWED_TO_ACCESS:
					setStatus(getString(R.string.tag_not_allowed_to_access));
					break;
				case USER_DEFINED_DATA_NOT_ENALBLED:
					setStatus(getString(R.string.user_defined_data_not_allowed_to_change));
					break;
				case TAG_NOT_WRITTEN_CORRECTLY:
					setStatus(getString(R.string.tag_not_written_correctly));
					break;
				default:
					break;
				}
				Log.i("otto", "onReturnReadGprsSettingsResult:" + text);
				setStatus(text);
			}
		}

		@Override
		public void onReturnReadWiFiSettingsResult(boolean isSuccess, Hashtable<String, Object> data) {
			if (isSuccess) {
				String text = getString(R.string.read_wifi_setting_success);
				text += "\n" + getString(R.string.ssid) + data.get("ssid");
				text += "\n" + getString(R.string.password) + data.get("password");
				text += "\n" + getString(R.string.url) + data.get("url");
				text += "\n" + getString(R.string.portNumber) + data.get("portNumber");
				setStatus(text);
			} else {
				String text = getString(R.string.read_wifi_setting_fail);
				Object[] keys = data.keySet().toArray();
				Arrays.sort(keys);
				for (Object key : keys) {
					text += "\n" + (String)key + " : ";
					TerminalSettingStatus terminalSettingStatus = (TerminalSettingStatus)data.get(key);
					switch (terminalSettingStatus) {
					case SUCCESS:
						text += getString(R.string.read_terminal_setting_success);
						break;
					case LENGTH_INCORRECT:
						text += getString(R.string.length_incorrect);
						break;
					case TLV_INCORRECT:
						text += getString(R.string.tlv_incorrect);
						break;
					case TAG_NOT_FOUND:
						text += getString(R.string.tag_not_found);
						break;
					case BOOTLOADER_NOT_SUPPORT:
						text += getString(R.string.bootloader_not_support);
						break;
					case TAG_NOT_ALLOWED_TO_ACCESS:
						text += getString(R.string.tag_not_allowed_to_access);
						break;
					case USER_DEFINED_DATA_NOT_ENALBLED:
						text += getString(R.string.user_defined_data_not_allowed_to_change);
						break;
					case TAG_NOT_WRITTEN_CORRECTLY:
						text += getString(R.string.tag_not_written_correctly);
						break;
					default:
						break;
					}
				}
				Log.i("otto", "onReturnReadWiFiSettingsResult:" + text);
				setStatus(text);
			}
		}

		@Override
		public void onReturnUpdateGprsSettingsResult(boolean isSuccess, Hashtable<String, TerminalSettingStatus> data) {
			if (isSuccess) {
				String text = getString(R.string.update_gprs_setting_success);
				Log.i("otto", "onReturnUpdateGprsSettingsResult:" + text);
				setStatus(text);
			} else {
				String text = getString(R.string.update_gprs_setting_fail);
				text += "\n" + getString(R.string.terminal_setting_status) + data.get("gprs");
				Log.i("otto", "onReturnUpdateGprsSettingsResult:" + text);
				setStatus(text);
			}
		}

		@Override
		public void onReturnUpdateWiFiSettingsResult(boolean isSuccess, Hashtable<String, TerminalSettingStatus> data) {
			if (isSuccess) {
				String text = getString(R.string.update_wifi_setting_success);
				setStatus(text);
			} else {
				String text = getString(R.string.update_wifi_setting_fail);
				Object[] keys = data.keySet().toArray();
				Arrays.sort(keys);
				for (Object key : keys) {
					text += "\n" + (String)key + " : ";
					TerminalSettingStatus terminalSettingStatus = (TerminalSettingStatus)data.get(key);
					switch (terminalSettingStatus) {
					case SUCCESS:
						text += getString(R.string.read_terminal_setting_success);
						break;
					case LENGTH_INCORRECT:
						text += getString(R.string.length_incorrect);
						break;
					case TLV_INCORRECT:
						text += getString(R.string.tlv_incorrect);
						break;
					case TAG_NOT_FOUND:
						text += getString(R.string.tag_not_found);
						break;
					case BOOTLOADER_NOT_SUPPORT:
						text += getString(R.string.bootloader_not_support);
						break;
					case TAG_NOT_ALLOWED_TO_ACCESS:
						text += getString(R.string.tag_not_allowed_to_access);
						break;
					case USER_DEFINED_DATA_NOT_ENALBLED:
						text += getString(R.string.user_defined_data_not_allowed_to_change);
						break;
					case TAG_NOT_WRITTEN_CORRECTLY:
						text += getString(R.string.tag_not_written_correctly);
						break;
					default:
						break;
					}
				}
				Log.i("otto", "onReturnUpdateWiFiSettingsResult:" + text);
				setStatus(text);
			}
		}

		@Override
		public void onReturnUpdateDisplayStringResult(boolean isSuccess, String errorMessage) {
			if (isSuccess) {
				setStatus(getString(R.string.update_display_string_success));
			} else {
				String text = getString(R.string.update_display_string_fail) + ". " + errorMessage;
				Log.i("otto", "onReturnUpdateDisplayStringResult:" + text);
				setStatus(text);
			}
		}

		@Override
		public void onReturnReadDisplayStringResult(boolean isSuccess, Hashtable<String, String> data) {
			try {
				if (isSuccess) {
					setStatus("Display String (ASCII) : " + Utils.hexString2AsciiString(data.get("data")));
				} else {
					setStatus(getString(R.string.read_display_string_fail));
				}
			} catch (Exception e) {

			}
		}

		@Override
		public void onAudioAutoConfigCompleted(boolean isDefaultSettings, String autoConfigSettings) {
			if(progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			
			String outputDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.bbpos.bbdevice/";
			String filename = "settings.txt";
			String content = getString(R.string.auto_config_completed);
			if(isDefaultSettings) {
				content += "\n" + getString(R.string.default_settings);
				new File(outputDirectory + filename).delete();
			} else {
				content += "\n" + getString(R.string.settings) + autoConfigSettings;
				
				try {
					File directory = new File(outputDirectory);
					if(!directory.isDirectory()) {
						directory.mkdirs();
					}
					FileOutputStream fos = new FileOutputStream(outputDirectory + filename, false);
					fos.write(autoConfigSettings.getBytes());
					fos.flush();
					fos.close();
					
					content += "\n" + getString(R.string.settings_written_to_external_storage);
				} catch(Exception e) {
				}
			}
			setStatus(content);
		}

		@Override
		public void onAudioAutoConfigError(AudioAutoConfigError autoConfigError) {
			if(progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			
			if(autoConfigError == AudioAutoConfigError.PHONE_NOT_SUPPORTED) {
				statusEditText.setText(getString(R.string.auto_config_error_phone_not_supported));
			} else if(autoConfigError == AudioAutoConfigError.INTERRUPTED) {
				statusEditText.setText(getString(R.string.auto_config_error_interrupted));
			}
		}

		@Override
		public void onAudioAutoConfigProgressUpdate(double percentage) {
			if(progressDialog != null) {
				progressDialog.setProgress((int)percentage);
			}
		}

		@Override
		public void onDeviceHere(boolean arg0) {
		}

		@Override
		public void onNoAudioDeviceDetected() {
			dismissDialog();
			if(progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			statusEditText.setText(getString(R.string.no_device_detected));
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(currentActivity, getString(R.string.no_device_detected), Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override
		public void onReturnNfcDataExchangeResult(boolean isSuccess, Hashtable<String, String> data) {
			if (isSuccess) {
				String text = getString(R.string.nfc_data_exchange_success);
				text += "\n" + getString(R.string.ndef_record) + data.get("ndefRecord");
				Log.i("otto", "onReturnNfcDataExchangeResult:" + text);
				setStatus(text);
			} else {
				String text = getString(R.string.nfc_data_exchange_fail);
				text += "\n" + getString(R.string.error_message) + data.get("errorMessage");
				Log.i("otto", "onReturnNfcDataExchangeResult:" + text);
				setStatus(text);
			}
		}

		@Override
		public void onReturnNfcDetectCardResult(NfcDetectCardResult nfcDetectCardResult, Hashtable<String, Object> data) {
			String text = "";
			text += getString(R.string.nfc_card_detection_result) + nfcDetectCardResult;
			text += "\n" + getString(R.string.nfc_tag_information) + data.get("nfcTagInfo");
			text += "\n" + getString(R.string.nfc_card_uid) + data.get("nfcCardUID");
			if (data.containsKey("errorMessage")) {
				text += "\n" + getString(R.string.error_message) + data.get("errorMessage");
			}
			Log.i("otto", "onReturnNfcDetectCardResult:" + text);
			setStatus(text);
		}

		@Override
		public void onUsbConnected() {
			setStatus(getString(R.string.usb_connected));
			sessionData.reset();
			bbDeviceController.getDeviceInfo();
		}

		@Override
		public void onUsbDisconnected() {
			setStatus(getString(R.string.usb_disconnected));
			sessionData.reset();
		}

		@Override
		public void onRequestDisplayAsterisk(int arg0) {
		}

		@Override
		public void onSerialConnected() {
			final ProgressDialog progressDialog = ProgressDialog.show(BaseActivity.this, getString(R.string.please_wait), getString(R.string.initializing));
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(6000);
					} catch (InterruptedException e) {
					}
					final Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable() {
						@Override
						public void run() {
							progressDialog.dismiss();
							statusEditText.setText(getString(R.string.serial_connected));
						}
					});
				}
			}).start();
			sessionData.reset();
		}

		@Override
		public void onSerialDisconnected() {
			setStatus(getString(R.string.serial_disconnected));
			sessionData.reset();
		}

		@Override
		public void onBarcodeReaderConnected() {
		}

		@Override
		public void onBarcodeReaderDisconnected() {
		}

		@Override
		public void onReturnBarcode(String arg0) {
		}

		@Override
		public void onRequestDisplayLEDIndicator(ContactlessStatus arg0) {
		}

		@Override
		public void onRequestProduceAudioTone(ContactlessStatusTone arg0) {
		}
		
		@Override
		public void onReturnReadAIDResult(Hashtable<String, Object> data) {
			String text = getString(R.string.read_aid_success);
				
			Object[] keys = data.keySet().toArray();
			Arrays.sort(keys);
			for (Object key : keys) {
				text += "\n" + (String)key + " : ";
				Object obj = data.get(key);
				if (obj instanceof String) {
					text += (String)obj;
				} else if (obj instanceof Boolean) {
					text += ((Boolean)obj) + "";
				} else if (obj instanceof TerminalSettingStatus) {
					text += ((TerminalSettingStatus)obj) + "";
				}
			}

			Log.i("otto", "onReturnReadAIDResult:" + text);
			setStatus(text);
		}

		@Override
		public void onReturnUpdateAIDResult(Hashtable<String, TerminalSettingStatus> data) {
			String text = getString(R.string.update_aid_success);
				
			Object[] keys = data.keySet().toArray();
			Arrays.sort(keys);
			for (Object key : keys) {
				text += "\n" + (String)key + " : ";
				Object obj = data.get(key);
				if (obj instanceof String) {
					text += (String)obj;
				} else if (obj instanceof Boolean) {
					text += ((Boolean)obj) + "";
				} else if (obj instanceof TerminalSettingStatus) {
					text += ((TerminalSettingStatus)obj) + "";
				}
			}

			Log.i("otto", "onReturnUpdateAIDResult:" + text);
			setStatus(text);
		}

		@Override
		public void onDeviceReset(boolean isSuccess, BBDeviceController.DeviceResetReason deviceResetReason) {
			String content = getString(R.string.device_reset);
			if (isSuccess) {
				content += ". Success. ";
			} else {
				content += ". Failed. ";
			}
			content += "Device reset reaseon : " + deviceResetReason;
			Log.i("otto", "onDeviceReset:" + content);
			setStatus(content);
		}

		@Override
		public void onPowerButtonPressed() {
			setStatus(getString(R.string.power_button_pressed));
		}

		@Override
		public void onPowerDown() {
			setStatus(getString(R.string.power_down));
		}

		@Override
		public void onEnterStandbyMode() {
			setStatus(getString(R.string.enter_standby_mode));
		}

		@Override
		public void onReturnAccountSelectionResult(AccountSelectionResult accountSelectionResult, int selectedAccountType) {
		}

		@Override
		public void onReturnDisableAccountSelectionResult(boolean isSuccess) {
		}

		@Override
		public void onReturnEnableAccountSelectionResult(boolean isSuccess) {
		}

		@Override
		public void onRequestStartEmv() {
			dismissDialog();
			setStatus(getString(R.string.request_start_emv));
			checkCardMode = CheckCardMode.TAP;
			startEmv();
		}

		@Override
		public void onReturnControlLEDResult(boolean isSuccess, String errorMessage) {
			if (isSuccess) {
				setStatus(getString(R.string.control_led_success));
			} else {
				setStatus(getString(R.string.control_led_fail) + "\n" + getString(R.string.error_message) + errorMessage);
			}
		}

		@Override
		public void onReturnVasResult(VASResult result, Hashtable<String, Object> data) {
			dismissDialog();
			Log.i("otto", "onReturnVasResult:" + "" + result + "\ndata : " + data);
			setStatus("" + result + "\ndata : " + data);
		}

		@Override
		public void onDeviceDisplayingPrompt() {
			dismissDialog();
			setStatus(getString(R.string.displaying_prompt));
		}

		@Override
		public void onRequestKeypadResponse() {
			dismissDialog();
			setStatus(getString(R.string.request_keypad_response));
		}

		@Override
		public void onReturnDisplayPromptResult(DisplayPromptResult result) {
			setStatus(getString(R.string.displaying_prompt_result) + " : " + result);
			Log.i("otto", "onReturnDisplayPromptResult:" + result);
			dismissDialog();
		}

		@Override
		public void onReturnFunctionKey(BBDeviceController.FunctionKey funcKey) {
			setStatus(getString(R.string.function_key) + " : " + funcKey);
			//dismissDialog();
		}
	}

	class MyBBDeviceOTAControllerListener implements BBDeviceOTAController.BBDeviceOTAControllerListener {

		@Override
		public void onReturnRemoteKeyInjectionResult(BBDeviceOTAController.OTAResult otaResult, String message) {
			dismissProgressDialog();
			String content = "";
			switch (otaResult) {
				case BATTERY_LOW_ERROR:
					content += getString(R.string.remote_key_injection_result) + getString(R.string.battery_low_error) + "\n";
					break;
				case DEVICE_COMM_ERROR:
					content += getString(R.string.remote_key_injection_result) + getString(R.string.device_comm_error) + "\n";
					break;
				case FAILED:
					content += getString(R.string.remote_key_injection_result) + getString(R.string.failed) + "\n";
					break;
				case NO_UPDATE_REQUIRED:
					content += getString(R.string.remote_key_injection_result) + getString(R.string.no_update_required) + "\n";
					break;
				case SERVER_COMM_ERROR:
					content += getString(R.string.remote_key_injection_result) + getString(R.string.server_comm_error) + "\n";
					break;
				case SETUP_ERROR:
					content += getString(R.string.remote_key_injection_result) + getString(R.string.setup_error) + "\n";
					break;
				case STOPPED:
					content += getString(R.string.remote_key_injection_result) + getString(R.string.stopped) + "\n";
					break;
				case SUCCESS:
					content += getString(R.string.remote_key_injection_result) + getString(R.string.success) + "\n";
					break;
			}

			content += getString(R.string.response_message) + message;
			Log.i("otto", "onReturnRemoteKeyInjectionResult:" + content);
			setStatus(content);
		}

		@Override
		public void onReturnRemoteFirmwareUpdateResult(BBDeviceOTAController.OTAResult otaResult, String message) {
			dismissProgressDialog();
			String content = "";
			switch (otaResult) {
				case BATTERY_LOW_ERROR:
					content += getString(R.string.remote_firmware_update_result) + getString(R.string.battery_low_error) + "\n";
					break;
				case DEVICE_COMM_ERROR:
					content += getString(R.string.remote_firmware_update_result) + getString(R.string.device_comm_error) + "\n";
					break;
				case FAILED:
					content += getString(R.string.remote_firmware_update_result) + getString(R.string.failed) + "\n";
					break;
				case NO_UPDATE_REQUIRED:
					content += getString(R.string.remote_firmware_update_result) + getString(R.string.no_update_required) + "\n";
					break;
				case SERVER_COMM_ERROR:
					content += getString(R.string.remote_firmware_update_result) + getString(R.string.server_comm_error) + "\n";
					break;
				case SETUP_ERROR:
					content += getString(R.string.remote_firmware_update_result) + getString(R.string.setup_error) + "\n";
					break;
				case STOPPED:
					content += getString(R.string.remote_firmware_update_result) + getString(R.string.stopped) + "\n";
					break;
				case SUCCESS:
					content += getString(R.string.remote_firmware_update_result) + getString(R.string.success) + "\n";
					break;
			}

			content += getString(R.string.response_message) + message;
			Log.i("otto", "onReturnRemoteFirmwareUpdateResult:" + content);
			setStatus(content);
		}

		@Override
		public void onReturnRemoteConfigUpdateResult(BBDeviceOTAController.OTAResult otaResult, String message) {
			dismissProgressDialog();
			String content = "";
			switch (otaResult) {
				case BATTERY_LOW_ERROR:
					content += getString(R.string.remote_config_update_result) + getString(R.string.battery_low_error) + "\n";
					break;
				case DEVICE_COMM_ERROR:
					content += getString(R.string.remote_config_update_result) + getString(R.string.device_comm_error) + "\n";
					break;
				case FAILED:
					content += getString(R.string.remote_config_update_result) + getString(R.string.failed) + "\n";
					break;
				case NO_UPDATE_REQUIRED:
					content += getString(R.string.remote_config_update_result) + getString(R.string.no_update_required) + "\n";
					break;
				case SERVER_COMM_ERROR:
					content += getString(R.string.remote_config_update_result) + getString(R.string.server_comm_error) + "\n";
					break;
				case SETUP_ERROR:
					content += getString(R.string.remote_config_update_result) + getString(R.string.setup_error) + "\n";
					break;
				case STOPPED:
					content += getString(R.string.remote_config_update_result) + getString(R.string.stopped) + "\n";
					break;
				case SUCCESS:
					content += getString(R.string.remote_config_update_result) + getString(R.string.success) + "\n";
					break;
				default:
					break;
			}

			content += getString(R.string.response_message) + message;
			Log.i("otto", "onReturnRemoteConfigUpdateResult:" + content);
			setStatus(content);
		}

		@Override
		public void onReturnLocalConfigUpdateResult(BBDeviceOTAController.OTAResult otaResult, String message) {
			dismissProgressDialog();
			String content = "";
			switch (otaResult) {
				case BATTERY_LOW_ERROR:
					content += getString(R.string.local_config_update_result) + getString(R.string.battery_low_error) + "\n";
					break;
				case DEVICE_COMM_ERROR:
					content += getString(R.string.local_config_update_result) + getString(R.string.device_comm_error) + "\n";
					break;
				case FAILED:
					content += getString(R.string.local_config_update_result) + getString(R.string.failed) + "\n";
					break;
				case NO_UPDATE_REQUIRED:
					content += getString(R.string.local_config_update_result) + getString(R.string.no_update_required) + "\n";
					break;
				case SERVER_COMM_ERROR:
					content += getString(R.string.local_config_update_result) + getString(R.string.server_comm_error) + "\n";
					break;
				case SETUP_ERROR:
					content += getString(R.string.local_config_update_result) + getString(R.string.setup_error) + "\n";
					break;
				case STOPPED:
					content += getString(R.string.local_config_update_result) + getString(R.string.stopped) + "\n";
					break;
				case SUCCESS:
					content += getString(R.string.local_config_update_result) + getString(R.string.success) + "\n";
					break;
				default:
					break;
			}

			content += getString(R.string.response_message) + message;
			Log.i("otto", "onReturnLocalConfigUpdateResult:" + content);
			setStatus(content);
		}

		@Override
		public void onReturnLocalFirmwareUpdateResult(BBDeviceOTAController.OTAResult otaResult, String message) {
			dismissProgressDialog();
			String content = "";
			switch (otaResult) {
				case BATTERY_LOW_ERROR:
					content += getString(R.string.local_firmware_update_result) + getString(R.string.battery_low_error) + "\n";
					break;
				case DEVICE_COMM_ERROR:
					content += getString(R.string.local_firmware_update_result) + getString(R.string.device_comm_error) + "\n";
					break;
				case FAILED:
					content += getString(R.string.local_firmware_update_result) + getString(R.string.failed) + "\n";
					break;
				case NO_UPDATE_REQUIRED:
					content += getString(R.string.local_firmware_update_result) + getString(R.string.no_update_required) + "\n";
					break;
				case SERVER_COMM_ERROR:
					content += getString(R.string.local_firmware_update_result) + getString(R.string.server_comm_error) + "\n";
					break;
				case SETUP_ERROR:
					content += getString(R.string.local_firmware_update_result) + getString(R.string.setup_error) + "\n";
					break;
				case STOPPED:
					content += getString(R.string.local_firmware_update_result) + getString(R.string.stopped) + "\n";
					break;
				case SUCCESS:
					content += getString(R.string.local_firmware_update_result) + getString(R.string.success) + "\n";
					break;
			}

			content += getString(R.string.response_message) + message;
			Log.i("otto", "onReturnLocalFirmwareUpdateResult:" + content);
			setStatus(content);
		}

		@Override
		public void onReturnTargetVersionResult(BBDeviceOTAController.OTAResult otaResult, Hashtable<String, String> data) {
			dismissProgressDialog();
			String content = "";

			Object[] keys = data.keySet().toArray();
			Arrays.sort(keys);
			for (Object key : keys) {
				content += "\n" + (String)key + " : ";
				Object obj = data.get(key);
				if (obj instanceof String) {
					content += (String)obj;
				} else if (obj instanceof Boolean) {
					content += ((Boolean)obj) + "";
				}
			}

			Log.i("otto", "onReturnTargetVersionResult:" + content);
			setStatus(content);
		}

		@Override
		public void onReturnTargetVersionListResult(BBDeviceOTAController.OTAResult otaResult, List<Hashtable<String, String>> data, String message) {
			dismissProgressDialog();
			setStatus("otaResult : " + otaResult + ", data : " + data + ", message : " + message);
		}

		@Override
		public void onReturnSetTargetVersionResult(BBDeviceOTAController.OTAResult otaResult, String message) {
			dismissProgressDialog();
			setStatus("otaResult : " + otaResult + ", message : " + message);
		}

		@Override
		public void onReturnOTAProgress(double percentage) {
			if(progressDialog != null) {
				progressDialog.setProgress((int)percentage);
			}
		}
	}

    class MyBBDeviceControllerSpocListener implements BBDeviceController.BBDeviceControllerSPoCListener {
        @Override
        public void onSPoCError(BBDeviceController.SPoCError spocError, String errorMessage) {
            dismissDialog();
            if(progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            String content = "" + spocError;
            if (errorMessage != null && !errorMessage.equals("")) {
                content += "\n" + getString(R.string.error_message) + errorMessage;
            }
			Log.i("otto", "MyBBDeviceControllerSpocListener.onSPoCError:" + content);
            statusEditText.setText(content);
        }

		@Override
		public void onSPoCSetupSecureSessionCompleted() {
			statusEditText.setText(getString(R.string.setup_secure_session_completed));
		}

		@Override
		public void onSPoCRequestSetupSecureSession() {
			statusEditText.setText(getString(R.string.request_setup_secure_session));
		}

		@Override
		public void onSPoCAttestationInProgress() {
			statusEditText.setText(getString(R.string.attestation_in_progress));
		}

		@Override
		public void onSPoCAttestationStart() {
			statusEditText.setText(getString(R.string.attestation_in_progress));
		}

		@Override
		public void onSPoCAttestationCompleted() {
			statusEditText.setText(getString(R.string.attestation_in_progress));
		}

		@Override
		public void onSPoCAttestationRescheduled() {
			statusEditText.setText(getString(R.string.attestation_in_progress));
		}
    }
}
