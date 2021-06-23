package com.otto.posprinter;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

public class NfcActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= 9) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		setContentView(R.layout.activity_nfc);

		((TextView) findViewById(R.id.modelTextView)).setText(Build.MANUFACTURER.toUpperCase(Locale.ENGLISH) + " - " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ")");

		clearLogButton = (Button) findViewById(R.id.clearLogButton);
		startNfcDetectionButton = (Button) findViewById(R.id.startNfcDetectionButton);
		stopNfcDetectionButton = (Button) findViewById(R.id.stopNfcDetectionButton);
		nfcDataExchangeWriteButton = (Button) findViewById(R.id.nfcDataExchangeWriteButton);
		nfcDataExchangeRead1StButton = (Button) findViewById(R.id.nfcDataExchangeRead1StButton);
		nfcDataExchangeReadNextButton = (Button) findViewById(R.id.nfcDataExchangeReadNextButton);
		startNfcDetectionAppleVasButton = (Button) findViewById(R.id.startNfcDetectionAppleVasButton);

		statusEditText = (EditText)findViewById(R.id.statusEditText);

		statusEditText.setMovementMethod(new ScrollingMovementMethod());

		MyOnClickListener myOnClickListener = new MyOnClickListener();
		clearLogButton.setOnClickListener(myOnClickListener);
		startNfcDetectionButton.setOnClickListener(myOnClickListener);
		stopNfcDetectionButton.setOnClickListener(myOnClickListener);
		nfcDataExchangeWriteButton.setOnClickListener(myOnClickListener);
		nfcDataExchangeRead1StButton.setOnClickListener(myOnClickListener);
		nfcDataExchangeReadNextButton.setOnClickListener(myOnClickListener);
		startNfcDetectionAppleVasButton.setOnClickListener(myOnClickListener);
		
		currentActivity = this;
	}

	class MyOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v == clearLogButton) {
				statusEditText.setText("");
			} else if (v == startNfcDetectionButton) {
				promptForStartNfcDetection();
			} else if (v == stopNfcDetectionButton) {
				promptForStopNfcDetection();
			} else if (v == nfcDataExchangeWriteButton) {
				promptForNfcDataExchangeWrite();
			} else if (v == nfcDataExchangeRead1StButton) {
				promptForNfcDataExchangeRead1St();
			} else if (v == nfcDataExchangeReadNextButton) {
				promptForNfcDataExchangeReadNext();
			} else if (v == startNfcDetectionAppleVasButton) {
				promptForStartNfcDetectionAppleVasStep1();
			}
		}
	}
}
