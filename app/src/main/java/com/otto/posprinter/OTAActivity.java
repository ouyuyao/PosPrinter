package com.otto.posprinter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.bbpos.bbdevice.BBDeviceController;
import com.bbpos.bbdevice.ota.BBDeviceOTAController;
import com.bbpos.bbdevice.ota.BBDeviceOTAController.ConfigType;
import com.bbpos.bbdevice.ota.BBDeviceOTAController.FirmwareType;
import com.bbpos.bbdevice.ota.BBDeviceOTAController.TargetVersionType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Locale;

public class OTAActivity extends BaseActivity {
    private static final int REMOTE_FIRMWARE_UPDATE_WITH_LOCAL_HEX_REQUEST_CODE = 4001;
    private static final int REMOTE_CONFIG_UPDATE_WITH_LOCAL_HEX_REQUEST_CODE = 4002;
    private static final int LOCAL_FIRMWARE_UPDATE_LOCAL_HEX_REQUEST_CODE = 4003;
    private static final int LOCAL_CONFIG_UPDATE_LOCAL_HEX_REQUEST_CODE = 4004;
	private static final int MENU_SETTINGS = 0;
	private static final int MENU_EMV = 1;

	private static final String OTA_SERVER_URL = "https://api.emms.bbpos.com/";
	
	private static Button remoteKeyInjectionButton;
	private static Button remoteFirmwareUpdateButton;
	private static Button remoteConfigUpdateButton;
	private static Button remoteCustomizedConfigUpdateButton;
	private static Button deviceInfoButton;
	private static Button initializeSessionButton;
	private static Button localFirmwareUpdateButton;
	private static Button localConfigUpdateButton;
	private static Button getTargetVersionButton;
	private static Button getTargetVersionListButton;
	private static Button setTargetVersionButton;
	private static Button remoteFirmwareUpdateWithDataButton;
	private static Button remoteConfigUpdateWithDataButton;

	private static OTAActivity currentActivity;
	
	private static String otaServerUrl = OTA_SERVER_URL;
	private static String vendorID = "bbpos1";
	private static String vendorSecret = "bbpos1";
	private static String appID = "bbpos2";
	private static String appSecret = "bbpos2";

	private static String dekBDK = "000111222333444555666777888999AAABBBCCCDDDEEEFFF";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ota);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		((TextView)findViewById(R.id.modelTextView)).setText(Build.MANUFACTURER.toUpperCase(Locale.ENGLISH) + " - " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ")");
		
		remoteKeyInjectionButton = findViewById(R.id.remoteKeyInjectionButton);
		remoteFirmwareUpdateButton = findViewById(R.id.remoteFirmwareUpdateButton);
		remoteConfigUpdateButton = findViewById(R.id.remoteConfigUpdateButton);
		remoteCustomizedConfigUpdateButton = findViewById(R.id.remoteCustomizedConfigUpdateButton);
		deviceInfoButton = findViewById(R.id.deviceInfoButton);
		initializeSessionButton = findViewById(R.id.initializeSessionButton);
		localFirmwareUpdateButton = findViewById(R.id.localFirmwareUpdateButton);
		localConfigUpdateButton = findViewById(R.id.localConfigUpdateButton);
		getTargetVersionButton = findViewById(R.id.getTargetVersionButton);
		getTargetVersionListButton = findViewById(R.id.getTargetVersionListButton);
		setTargetVersionButton = findViewById(R.id.setTargetVersionButton);
		remoteFirmwareUpdateWithDataButton = (Button)findViewById(R.id.remoteFirmwareUpdateWithDataButton);
		remoteConfigUpdateWithDataButton = (Button)findViewById(R.id.remoteConfigUpdateWithDataButton);
		
		MyOnClickListener onClickListener = new MyOnClickListener();
		remoteKeyInjectionButton.setOnClickListener(onClickListener);
		remoteFirmwareUpdateButton.setOnClickListener(onClickListener);
		remoteConfigUpdateButton.setOnClickListener(onClickListener);
		remoteCustomizedConfigUpdateButton.setOnClickListener(onClickListener);
		deviceInfoButton.setOnClickListener(onClickListener);
		initializeSessionButton.setOnClickListener(onClickListener);
		localFirmwareUpdateButton.setOnClickListener(onClickListener);
		localConfigUpdateButton.setOnClickListener(onClickListener);
		getTargetVersionButton.setOnClickListener(onClickListener);
		getTargetVersionListButton.setOnClickListener(onClickListener);
		setTargetVersionButton.setOnClickListener(onClickListener);
		remoteFirmwareUpdateWithDataButton.setOnClickListener(onClickListener);
		remoteConfigUpdateWithDataButton.setOnClickListener(onClickListener);

		statusEditText = findViewById(R.id.status);

		try {
			otaController.setBBDeviceController(bbDeviceController);	
		} catch(Exception e) {
			String content = e.toString() + "\n";
			StackTraceElement[] elements = e.getStackTrace();
			for(int i = 0; i < elements.length; ++i) {
				content += elements[i].toString() + "\n";
			}
			setStatus(content);
		}
		
		otaController.setOTAServerURL(otaServerUrl);

		currentActivity = this;
		setStatus("BBDevice OTA Controller : " + BBDeviceOTAController.getApiVersion() + ", BBDevice Controller : " + BBDeviceController.getApiVersion());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, MENU_SETTINGS, 0, R.string.settings);
		menu.add(0, MENU_EMV, 1, R.string.emv);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == MENU_EMV) {
			isSwitchingActivity = true;
			finish();
    		Intent intent = new Intent(this, MainActivity.class);
    		startActivity(intent);
			return true;
		} else if(item.getItemId() == MENU_SETTINGS) {
			dismissDialog();
    		
    		dialog = new Dialog(currentActivity);
			dialog.setContentView(R.layout.settings_dialog);
			dialog.setTitle(R.string.settings);
			
			final EditText otaServerUrlEditText = dialog.findViewById(R.id.otaServerUrlEditText);
			final EditText vendorIDEditText = dialog.findViewById(R.id.vendorIDEditText);;
			final EditText vendorSecretEditText = dialog.findViewById(R.id.vendorSecretEditText);
			final EditText appIDEditText = dialog.findViewById(R.id.appIDEditText);
			final EditText appSecretEditText = dialog.findViewById(R.id.appSecretEditText);
			    		    		
    		otaServerUrlEditText.setText(otaServerUrl);
    		vendorIDEditText.setText(vendorID);
    		vendorSecretEditText.setText(vendorSecret);
    		appIDEditText.setText(appID);
    		appSecretEditText.setText(appSecret);
    		
    		dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					otaServerUrl = otaServerUrlEditText.getText().toString();
					otaController.setOTAServerURL(otaServerUrl);
					vendorID = vendorIDEditText.getText().toString();
					vendorSecret = vendorSecretEditText.getText().toString();
					appID = appIDEditText.getText().toString();
					appSecret = appSecretEditText.getText().toString();
					
					dismissDialog();
				}
			});
    		
    		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dismissDialog();
				}
			});
    		
    		dialog.show();
    		return true;
		}
		return false;
	}
	
	public void promptForFirmwareType() {
    	dismissDialog();
    	dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.firmware_type_dialog);
		dialog.setTitle(getString(R.string.firmware_type));
		
		OnClickListener onClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				RadioButton mainProcessorRadioButton = dialog.findViewById(R.id.mainProcessorRadioButton);
				RadioButton coprocessorRadioButton = dialog.findViewById(R.id.coprocessorRadioButton);
				
				FirmwareType firmwareType;
				
				if(mainProcessorRadioButton.isChecked()) {
					firmwareType = FirmwareType.MAIN_PROCESSOR;
				} else if(coprocessorRadioButton.isChecked()) {
					firmwareType = FirmwareType.COPROCESSOR;
				} else {
					return;
				}
				
				Hashtable<String, Object> input = new Hashtable<String, Object>();
				input.put("vendorID", vendorID);
				input.put("appID", appID);
				input.put("vendorSecret", vendorSecret);
				input.put("appSecret", appSecret);
				input.put("forceUpdate", true);
				input.put("firmwareType", firmwareType);
				
				try {
					createProgressDialog(R.string.remote_firmware_update);
					otaController.startRemoteFirmwareUpdate(input);
				} catch (Exception e) {
					String content = e.toString() + "\n";
					StackTraceElement[] elements = e.getStackTrace();
					for(int i = 0; i < elements.length; ++i) {
						content += elements[i].toString() + "\n";
					}
					setStatus(content);
					dismissProgressDialog();
				}
				
				dismissDialog();
			}
		};
		
		RadioButton mainProcessorRadioButton = dialog.findViewById(R.id.mainProcessorRadioButton);
		RadioButton coprocessorRadioButton = dialog.findViewById(R.id.coprocessorRadioButton);
		
		mainProcessorRadioButton.setOnClickListener(onClickListener);
		coprocessorRadioButton.setOnClickListener(onClickListener);

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismissDialog();
			}
			
		});
		
		dialog.show();
    }
	
	public void promptForInitSession() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.init_session));
		
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
	
	public void promptForLocalFirmwareUpdate() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.init_session));
		
		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("dekBDK");
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText(dekBDK);
		
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
			    dekBDK = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select HEX file for local firmware update"), LOCAL_FIRMWARE_UPDATE_LOCAL_HEX_REQUEST_CODE);
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
	
	public void promptForLocalConfigUpdate() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_input_dialog);
		dialog.setTitle(getString(R.string.init_session));
		
		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("dekBDK");
		((EditText) (dialog.findViewById(R.id.general1EditText))).setText(dekBDK);
		
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
				dekBDK = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
				Intent intent = new Intent()
						.setType("*/*")
						.setAction(Intent.ACTION_OPEN_DOCUMENT);
				intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				startActivityForResult(Intent.createChooser(intent, "Select HEX file for local config update"), LOCAL_CONFIG_UPDATE_LOCAL_HEX_REQUEST_CODE);
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

	public void promptForGetTargetVersionList() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_and_spinner);
		dialog.setTitle(getString(R.string.get_target_version_list));

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

		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("targetVersionType");
		((Spinner)dialog.findViewById(R.id.general9Spinner)).setAdapter(new ArrayAdapter<TargetVersionType>(OTAActivity.this, android.R.layout.simple_spinner_item, TargetVersionType.values()));

		dialog.findViewById(R.id.general10TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10Spinner).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10CheckBox).setVisibility(View.GONE);

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
				Hashtable<String, Object> input = new Hashtable<String, Object>();
				input.put("vendorID", vendorID);
				input.put("appID", appID);
				input.put("vendorSecret", vendorSecret);
				input.put("appSecret", appSecret);

				if (((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).isChecked()) {
					TargetVersionType targetVersionType = (TargetVersionType)((Spinner)dialog.findViewById(R.id.general9Spinner)).getSelectedItem();
					input.put("targetVersionType", targetVersionType);
				}

				try {
					otaController.getTargetVersionListWithData(input);
				} catch (Exception e) {
					String content = e.toString() + "\n";
					StackTraceElement[] elements = e.getStackTrace();
					for(int i = 0; i < elements.length; ++i) {
						content += elements[i].toString() + "\n";
					}
					setStatus(content);
					dismissProgressDialog();
				}

				dismissDialog();
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

	public void promptForSetTargetVersion() {
		dismissDialog();
		dialog = new Dialog(currentActivity);
		dialog.setContentView(R.layout.general_string_and_spinner);
		dialog.setTitle(getString(R.string.set_target_version));

		((TextView)(dialog.findViewById(R.id.general1TextView))).setText("firmwareVersion");
		((EditText)(dialog.findViewById(R.id.general1EditText))).setText("10.00.05.07");

		((TextView)(dialog.findViewById(R.id.general2TextView))).setText("deviceSettingVersion");
		((EditText)(dialog.findViewById(R.id.general2EditText))).setText("BBZZ_GEN_v3");

		((TextView)(dialog.findViewById(R.id.general3TextView))).setText("terminalSettingVersion");
		((EditText)(dialog.findViewById(R.id.general3EditText))).setText("BBZZ_GEN_v3");

		((TextView)(dialog.findViewById(R.id.general4TextView))).setText("mainProcessorVersion");
		((EditText)(dialog.findViewById(R.id.general4EditText))).setText("x.x.x");

		((TextView)(dialog.findViewById(R.id.general5TextView))).setText("coprocessorVersion");
		((EditText)(dialog.findViewById(R.id.general5EditText))).setText("x.x.x");

		((TextView)(dialog.findViewById(R.id.general6TextView))).setText("keyProfileName");
		((EditText)(dialog.findViewById(R.id.general6EditText))).setText("Test Key - 1");

		dialog.findViewById(R.id.general7TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general7CheckBox).setVisibility(View.GONE);

		dialog.findViewById(R.id.general8TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8EditText).setVisibility(View.GONE);
		dialog.findViewById(R.id.general8CheckBox).setVisibility(View.GONE);

		((TextView)(dialog.findViewById(R.id.general9TextView))).setText("targetVersionType");
		((Spinner)dialog.findViewById(R.id.general9Spinner)).setAdapter(new ArrayAdapter<TargetVersionType>(OTAActivity.this, android.R.layout.simple_spinner_item, TargetVersionType.values()));

		dialog.findViewById(R.id.general10TextView).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10Spinner).setVisibility(View.GONE);
		dialog.findViewById(R.id.general10CheckBox).setVisibility(View.GONE);

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
				Hashtable<String, Object> input = new Hashtable<String, Object>();
				input.put("vendorID", vendorID);
				input.put("appID", appID);
				input.put("vendorSecret", vendorSecret);
				input.put("appSecret", appSecret);

				if (((CheckBox)(dialog.findViewById(R.id.general1CheckBox))).isChecked()) {
					input.put("firmwareVersion", ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general2CheckBox))).isChecked()) {
					input.put("deviceSettingVersion", ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general3CheckBox))).isChecked()) {
					input.put("terminalSettingVersion", ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general4CheckBox))).isChecked()) {
					input.put("mainProcessorVersion", ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general5CheckBox))).isChecked()) {
					input.put("coprocessorVersion", ((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general6CheckBox))).isChecked()) {
					input.put("keyProfileName", ((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString());
				}

				if (((CheckBox)(dialog.findViewById(R.id.general9CheckBox))).isChecked()) {
					TargetVersionType targetVersionType = (TargetVersionType)((Spinner)dialog.findViewById(R.id.general9Spinner)).getSelectedItem();
					input.put("targetVersionType", targetVersionType);
				}

				try {
					otaController.setTargetVersionWithData(input);
				} catch (Exception e) {
					String content = e.toString() + "\n";
					StackTraceElement[] elements = e.getStackTrace();
					for(int i = 0; i < elements.length; ++i) {
						content += elements[i].toString() + "\n";
					}
					setStatus(content);
					dismissProgressDialog();
				}

				dismissDialog();
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
	
	public void createProgressDialog(int titleId) {
		progressDialog = new ProgressDialog(currentActivity);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setTitle(titleId);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMax(100);
		progressDialog.setIndeterminate(false);
		progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				progressDialog.dismiss();
				try {
					otaController.stop();
				} catch (Exception e) {
					setStatus("otaController.stop() Exception : " + e.toString());
				}
			}
		});
		progressDialog.show();
	}

	public static String convertStreamToString(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		Boolean firstLine = true;
		while ((line = reader.readLine()) != null) {
			if (firstLine) {
				sb.append(line);
				firstLine = false;
			} else {
				sb.append("\n").append(line);
			}
		}
		reader.close();
		return sb.toString();
	}

	public static String getStringFromFile(String filePath) throws IOException {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		fin.close();
		return ret;
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REMOTE_FIRMWARE_UPDATE_WITH_LOCAL_HEX_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
			Hashtable<String, Object> input = new Hashtable<String, Object>();
			if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.KITKAT) {
				String encHex = Utils.getContentFromUri(this, selectedfile);
				input.put("encHex", encHex);
			} else {
				input.put("encHexFilePath", Utils.getPathFromUri(this, selectedfile));
			}
			input.put("vendorID", vendorID);
			input.put("appID", appID);
			input.put("vendorSecret", vendorSecret);
			input.put("appSecret", appSecret);
			input.put("forceUpdate", true);
			input.put("firmwareType", FirmwareType.MAIN_PROCESSOR);
			try {
				createProgressDialog(R.string.remote_firmware_update_with_data);
				otaController.startRemoteFirmwareUpdate(input);
			} catch (Exception e) {
				String content = e.toString() + "\n";
				setStatus(content);
				dismissProgressDialog();
			}
			dismissDialog();
        } else if (requestCode == REMOTE_CONFIG_UPDATE_WITH_LOCAL_HEX_REQUEST_CODE && resultCode == RESULT_OK) {
			Uri selectedfile = data.getData(); //The uri with the location of the file
        	Hashtable<String, Object> input = new Hashtable<String, Object>();
			if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.KITKAT) {
				String encHex = Utils.getContentFromUri(this, selectedfile);
				input.put("encHex", encHex);
			} else {
				input.put("encHexFilePath", Utils.getPathFromUri(this, selectedfile));
			}
			input.put("vendorID", vendorID);
			input.put("appID", appID);
			input.put("vendorSecret", vendorSecret);
			input.put("appSecret", appSecret);
			input.put("forceUpdate", true);
			input.put("firmwareType", FirmwareType.MAIN_PROCESSOR);
			try {
				createProgressDialog(R.string.remote_config_update_with_data);
				otaController.startRemoteConfigUpdate(input);
			} catch (Exception e) {
				String content = e.toString() + "\n";
				setStatus(content);
				dismissProgressDialog();
			}
			dismissDialog();
        } else if (requestCode == LOCAL_FIRMWARE_UPDATE_LOCAL_HEX_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
            Hashtable<String, Object> input = new Hashtable<String, Object>();
            input.put("dekBDK", dekBDK);
			if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.KITKAT) {
				String encHex = Utils.getContentFromUri(this, selectedfile);
				input.put("encHex", encHex);
			} else {
				input.put("encHexFilePath", Utils.getPathFromUri(this, selectedfile));
			}
            try {
                createProgressDialog(R.string.local_firmware_update);
                otaController.startLocalFirmwareUpdateWithData(input);
            } catch (Exception e) {
                String content = e.toString() + "\n";
                setStatus(content);
                dismissProgressDialog();
            }
			dismissDialog();
        } else if (requestCode == LOCAL_CONFIG_UPDATE_LOCAL_HEX_REQUEST_CODE && resultCode == RESULT_OK) {
			Uri selectedfile = data.getData(); //The uri with the location of the file
			Hashtable<String, Object> input = new Hashtable<String, Object>();
			input.put("dekBDK", dekBDK);
			if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.KITKAT) {
				String encHex = Utils.getContentFromUri(this, selectedfile);
				input.put("encHex", encHex);
			} else {
				input.put("encHexFilePath", Utils.getPathFromUri(this, selectedfile));
			}
			try {
				createProgressDialog(R.string.local_config_update);
				otaController.startLocalConfigUpdateWithData(input);
			} catch (Exception e) {
				String content = e.toString() + "\n";
				setStatus(content);
				dismissProgressDialog();
			}
			dismissDialog();
        }
    }

	class MyOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(v == remoteKeyInjectionButton) {
				Hashtable<String, Object> input = new Hashtable<String, Object>();
				input.put("vendorID", vendorID);
				input.put("appID", appID);
				input.put("vendorSecret", vendorSecret);
				input.put("appSecret", appSecret);
				
				try {
					createProgressDialog(R.string.remote_key_injection);
					otaController.startRemoteKeyInjection(input);
				} catch (Exception e) {
					String content = e.toString() + "\n";
					StackTraceElement[] elements = e.getStackTrace();
					for(int i = 0; i < elements.length; ++i) {
						content += elements[i].toString() + "\n";
					}
					setStatus(content);
					dismissProgressDialog();
				}
			} else if(v == remoteFirmwareUpdateButton) {
				promptForFirmwareType();
			} else if(v == remoteConfigUpdateButton) {
				Hashtable<String, Object> input = new Hashtable<String, Object>();
				input.put("vendorID", vendorID);
				input.put("appID", appID);
				input.put("vendorSecret", vendorSecret);
				input.put("appSecret", appSecret);
				input.put("forceUpdate", true);
				
				try {
					createProgressDialog(R.string.remote_config_update);
					otaController.startRemoteConfigUpdate(input);
				} catch (Exception e) {
					String content = e.toString() + "\n";
					StackTraceElement[] elements = e.getStackTrace();
					for(int i = 0; i < elements.length; ++i) {
						content += elements[i].toString() + "\n";
					}
					setStatus(content);
					dismissProgressDialog();
				}
			} else if(v == remoteCustomizedConfigUpdateButton) {
				Hashtable<String, Object> input = new Hashtable<String, Object>();
				input.put("vendorID", vendorID);
				input.put("appID", appID);
				input.put("vendorSecret", vendorSecret);
				input.put("appSecret", appSecret);
				input.put("forceUpdate", true);
				input.put("configType", ConfigType.CUSTOMIZED_CONFIG);
				
				try {
					createProgressDialog(R.string.remote_config_update);
					otaController.startRemoteConfigUpdate(input);
				} catch (Exception e) {
					String content = e.toString() + "\n";
					StackTraceElement[] elements = e.getStackTrace();
					for(int i = 0; i < elements.length; ++i) {
						content += elements[i].toString() + "\n";
					}
					setStatus(content);
					dismissProgressDialog();
				}
			} else if(v == deviceInfoButton) {
				bbDeviceController.getDeviceInfo();
			} else if (v == initializeSessionButton) {
				promptForInitSession();
			} else if (v == localFirmwareUpdateButton) {
				promptForLocalFirmwareUpdate();				
			} else if (v == localConfigUpdateButton) {
				promptForLocalConfigUpdate();
			} else if (v == getTargetVersionButton) {
				Hashtable<String, String> input = new Hashtable<String, String>();
				input.put("vendorID", vendorID);
				input.put("appID", appID);
				input.put("vendorSecret", vendorSecret);
				input.put("appSecret", appSecret);
				
				try {
					otaController.getTargetVersionWithData(input);
				} catch (Exception e) {
					String content = e.toString() + "\n";
					StackTraceElement[] elements = e.getStackTrace();
					for(int i = 0; i < elements.length; ++i) {
						content += elements[i].toString() + "\n";
					}
					setStatus(content);
					dismissProgressDialog();
				}
			} else if (v == getTargetVersionListButton) {
				promptForGetTargetVersionList();
			} else if (v == setTargetVersionButton) {
				promptForSetTargetVersion();
			} else if (v == remoteFirmwareUpdateWithDataButton) {
                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_OPEN_DOCUMENT);
				intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select HEX file for remote firmware update"), REMOTE_FIRMWARE_UPDATE_WITH_LOCAL_HEX_REQUEST_CODE);
			} else if (v == remoteConfigUpdateWithDataButton) {
				Intent intent = new Intent()
						.setType("*/*")
						.setAction(Intent.ACTION_OPEN_DOCUMENT);
				intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				startActivityForResult(Intent.createChooser(intent, "Select HEX file for remote config update"), REMOTE_CONFIG_UPDATE_WITH_LOCAL_HEX_REQUEST_CODE);
			}
		}
	}
}
