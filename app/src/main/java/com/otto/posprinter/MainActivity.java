package com.otto.posprinter;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bbpos.bbdevice.BBDeviceController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;

public class MainActivity extends BaseActivity {

	protected static String webAutoConfigString = "";
	protected static boolean isLoadedLocalSettingFile = false;
	protected static boolean isLoadedWebServiceAutoConfig = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);

		((TextView) findViewById(R.id.modelTextView)).setText(Build.MANUFACTURER.toUpperCase(Locale.ENGLISH) + " - " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ")");

		startButton = (Button) findViewById(R.id.startButton);
		amountEditText = (EditText) findViewById(R.id.amountEditText);
		statusEditText = (EditText) findViewById(R.id.statusEditText);
		posText = (EditText)findViewById(R.id.posText);
		printerText = (EditText)findViewById(R.id.printerText);
		tipAmountTipsPercentageCashbackCheckBox = (CheckBox) findViewById(R.id.tipsAmountTipsPercentageCashbackCheckBox);

		if(printerConnect==true){
			printerText.setText(getResources().getText(R.string.printer_connected));
		}
		if(posConnect==true){
			posText.setText(getString(R.string.pos_connected));
		}

		if (Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("CHB8")
				|| Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPC3")
				|| Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPC4")
				|| Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPS3")
				|| Utils.hexString2AsciiString(sessionData.getProductId()).startsWith("WPD3")) {
			tipAmountTipsPercentageCashbackCheckBox.setVisibility(View.VISIBLE);
		} else {
			tipAmountTipsPercentageCashbackCheckBox.setVisibility(View.GONE);
		}
		tipAmountTipsPercentageCashbackCheckBox.setChecked(sessionData.isTipAmountTipsPercentageCashback());

		MyOnClickListener myOnClickListener = new MyOnClickListener();
		startButton.setOnClickListener(myOnClickListener);

		currentActivity = this;
		
		try {
			String filename = "settings.txt";
			String inputDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.bbpos.bbdevice.ui/";
			
			FileInputStream fis = new FileInputStream(inputDirectory + filename);
			byte[] temp = new byte[fis.available()];
			fis.read(temp);
			fis.close();
			
			isLoadedLocalSettingFile = true;
			bbDeviceController.setAudioAutoConfig(new String(temp));
			
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(currentActivity, getString(R.string.setting_config), Toast.LENGTH_LONG).show();
				}
			});
		} catch(Exception e) {
		}
		
		//Create instance for AsyncCallWS
        AsyncCallWS task = new AsyncCallWS();
        //Call execute 
        task.execute();
	}

	class MyOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			statusEditText.setText("");
			sessionData.setTipAmountTipsPercentageCashback(tipAmountTipsPercentageCashbackCheckBox.isChecked());
			if (v == startButton) {
				isPinCanceled = false;
				amountEditText.setText("");

				statusEditText.setText(R.string.starting);
				promptForStartEmv();
				//promptForCheckCard();
			}
		}
	}
	
	private class AsyncCallWS extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			if (isLoadedWebServiceAutoConfig == false) {
				webAutoConfigString = WebService.invokeGetAutoConfigString(Build.MANUFACTURER.toUpperCase(Locale.US), Build.MODEL.toUpperCase(Locale.US), BBDeviceController.getApiVersion(), "getAutoConfigString");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (isLoadedWebServiceAutoConfig == false) {
				isLoadedWebServiceAutoConfig = true;
				if (isLoadedLocalSettingFile == false) {
					if (!webAutoConfigString.equalsIgnoreCase("Error occured") && !webAutoConfigString.equalsIgnoreCase("")) {
						bbDeviceController.setAudioAutoConfig(webAutoConfigString);
						
						try {
							String filename = "settings.txt";
							String outputDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.bbpos.emvswipe.ui/";

							File directory = new File(outputDirectory);
							if (!directory.isDirectory()) {
								directory.mkdirs();
							}
							FileOutputStream fos = new FileOutputStream(outputDirectory + filename, true);
							fos.write(webAutoConfigString.getBytes());
							fos.flush();
							fos.close();
						} catch (Exception e) {
						}
						
						new Handler().post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(currentActivity, getString(R.string.setting_config_from_web_service), Toast.LENGTH_LONG).show();
							}
						});
					}
				}
			}
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

	}
}
