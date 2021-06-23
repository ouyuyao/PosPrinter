package com.otto.posprinter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bbpos.bbdevice.BBDeviceController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class TransactionProcessAcitivity extends BaseActivity{

    ImageView logo1;
    ImageView logo2;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_process);

        Intent intent = getIntent();
        String amount = intent.getStringExtra("amount");
        Hashtable<String, Object> startDate = hashMap2hashTable((HashMap<String, Object>) intent.getSerializableExtra("startDate"));
        Hashtable<String, Object> startInput = hashMap2hashTable((HashMap<String, Object>) intent.getSerializableExtra("startInput"));
        BBDeviceController.AmountInputType amountInputType  = (BBDeviceController.AmountInputType) intent.getSerializableExtra("amountInputType");

        transactionStatusTitleTv = (TextView) findViewById(R.id.transactionStatusTitleTv);
        transactionAmountTv = (TextView) findViewById(R.id.transactionAmountTv);
        transactionStatusBtn = (Button) findViewById(R.id.transactionStatusBtn);
        logo1 = (ImageView) findViewById(R.id.logo1);
        logo2 = (ImageView) findViewById(R.id.logo2);

        transactionStatusTitleTv.setText(getString(R.string.transaction_amount_to_pay));
        transactionAmountTv.setText(" $ " + amountDigitalCheck(amount));
        transactionStatusBtn.setText(getString(R.string.transaction_start));

        transactionStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(posConnect==false&&trnCompleted==false){
                    Toast.makeText(TransactionProcessAcitivity.this, R.string.please_connect_pos, Toast.LENGTH_SHORT).show();
                    transactionStatusBtn.setText(getString(R.string.back_to_home));
                    transactionStatusBtn.setClickable(true);
                    trnCompleted = true;
                }else if(printerConnect==false&&trnCompleted==false){
                    Toast.makeText(TransactionProcessAcitivity.this, R.string.please_connect_printer, Toast.LENGTH_SHORT).show();
                    transactionStatusBtn.setText(getString(R.string.back_to_home));
                    transactionStatusBtn.setClickable(true);
                    trnCompleted = true;
                }else{
                    if(trnCompleted==false){
                        //发起交易
                        transactionStatusBtn.setText(getString(R.string.transaction_processing));
                        /******交易*****/
                        bbDeviceController.startEmv(startDate);
                        if (bbDeviceController.setAmount(startInput)) {
                            amountEditText.setText("$" + amount);
                            currentActivity.amount = amount;
                            currentActivity.cashbackAmount = cashbackAmount;
                            statusEditText.setText(getString(R.string.please_confirm_amount));
                            dismissDialog();
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    promptForAmount(amountInputType,null);
                                }
                            }, 500);
                        }
                        transactionStatusBtn.setText(getString(R.string.transaction_processing));
                        transactionStatusBtn.setClickable(false);
                    }else{
                        Intent intent = new Intent(TransactionProcessAcitivity.this,MainActivity.class);
                        trnCompleted = false;
                        startActivity(intent);
                    }
                }
            }
        });
    }
    public Hashtable<String, Object> hashMap2hashTable(HashMap<String, Object> map){
        Hashtable<String, Object> result = new Hashtable<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            result.put(key,value);
        }
        return result;
    }
}
