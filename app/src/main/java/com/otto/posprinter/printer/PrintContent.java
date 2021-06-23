package com.otto.posprinter.printer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.otto.posprinter.BaseActivity;
import com.otto.posprinter.R;
import com.printer.command.EscCommand;

import java.util.Map;
import java.util.Vector;


public class PrintContent {
      /**
       * 票据打印测试页
       * @return
       */
      public static Vector<Byte> getReceipt(Map<String,Object> map){
            EscCommand esc = new EscCommand();
            //初始化打印机
            esc.addInitializePrinter();
            //打印走纸多少个单位
            esc.addPrintAndFeedLines((byte)3);
            // 设置打印居中
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
            // 设置为倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
            //  打印文字
            esc.addText("***************");
            esc.addPrintAndLineFeed();
            esc.addText("Demo");
            esc.addPrintAndLineFeed();
            esc.addText("***************");
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();

            /*打印文字*/
            esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
            //  设置打印左对齐
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_2, EscCommand.HEIGHT_ZOOM.MUL_2);
            esc.addText("RECEIPT SAMPLE");
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();
            //  打印文字
            if(!map.isEmpty()){
                  esc.addText("AMOUNT RECEIVED");
                  esc.addPrintAndLineFeed();
                  esc.addPrintAndLineFeed();
                  String amount = map.get("amount").toString();
                  amount = BaseActivity.amountDigitalCheck(amount);
                  esc.addText("$"+amount);
                  esc.addPrintAndLineFeed();
                  esc.addPrintAndLineFeed();
            }else{
                  esc.addText("System Error! nothing to be print");
                  esc.addPrintAndLineFeed();
                  esc.addPrintAndLineFeed();
            }
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();

            esc.addText("SIGNATURE");
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();

            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON);
            esc.addText("__________________");
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
            /*打印QRCode*/
            //设置纠错等级
            esc.addSelectErrorCorrectionLevelForQRCode((byte)0x31);
            // 设置打印居中对齐
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            //设置qrcode模块大小
            esc.addSelectSizeOfModuleForQRCode((byte)6);
            //设置qrcode内容
            esc.addStoreQRCodeData("https://www.ofdhq.com");
            //打印QRCode
            esc.addPrintQRCode();
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();

            /*打印结束文字*/
            // 设置打印居中对齐
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
            //  设置打印左对齐
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            //  打印结束
            esc.addText("www.ofdhq.com");
            esc.addPrintAndLineFeed();
            esc.addCutAndFeedPaper((byte) 4);

            Vector<Byte> datas = esc.getCommand();
            return datas;
      }

}
