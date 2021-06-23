package com.otto.posprinter;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Hashtable;
import java.util.Locale;

public class ApduActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= 9) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		setContentView(R.layout.activity_apdu);

		((TextView) findViewById(R.id.modelTextView)).setText(Build.MANUFACTURER.toUpperCase(Locale.ENGLISH) + " - " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ")");

		clearLogButton = (Button) findViewById(R.id.clearLogButton);
		powerOnIccButton = (Button) findViewById(R.id.powerOnIccButton);
		powerOffIccButton = (Button) findViewById(R.id.powerOffIccButton);
		apduButton = (Button) findViewById(R.id.apduButton);
		statusEditText = (EditText)findViewById(R.id.statusEditText);

		statusEditText.setMovementMethod(new ScrollingMovementMethod());

		MyOnClickListener myOnClickListener = new MyOnClickListener();
		clearLogButton.setOnClickListener(myOnClickListener);
		powerOnIccButton.setOnClickListener(myOnClickListener);
		powerOffIccButton.setOnClickListener(myOnClickListener);
		apduButton.setOnClickListener(myOnClickListener);

		currentActivity = this;
	}

	public void promptForPowerOnIcc() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.power_on_icc));

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("Sam Card Slot Number");
		((TextView)(dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 350;
		((EditText) (dialog.findViewById(R.id.general1EditText))).setText("");

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
				String samCardSlotNumber = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
				if (samCardSlotNumber.equals("")) {
					Hashtable<String, Object> data = new Hashtable<String, Object>();
					bbDeviceController.powerOnIcc(data);
				} else {
					Hashtable<String, Object> data = new Hashtable<String, Object>();
					data.put("samCardSlotNumber", samCardSlotNumber);
					bbDeviceController.powerOnIcc(data);
				}
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


	class MyOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v == powerOnIccButton) {
				promptForPowerOnIcc();
			} else if (v == clearLogButton) {
				statusEditText.setText("");
			} else if (v == powerOffIccButton) {
				bbDeviceController.powerOffIcc();
			} else if (v == apduButton) {
				if (ksn.equals("")) {
					setStatus(getString(R.string.please_power_on_icc));
					return;
				}
				cardholderName = "";
				expiryDate = "";
				pan = "";
				track2 = "";

				state = State.GETTING_PSE;
				sendApdu("00A404000E315041592E5359532E444446303100");
				setStatus("Getting PSE...");

				startTime = System.currentTimeMillis();
			}
		}
	}
}
