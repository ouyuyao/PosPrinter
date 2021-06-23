package com.otto.posprinter;

import com.bbpos.bbdevice.BBDeviceController;

class SessionData {
    private String productId = "";
    private boolean isTipAmountTipsPercentageCashback = false;
    private int inputAmountOption = 0;
    private boolean isInputAmountOptionExist = false;
    private String[] tipsAmountOptions = new String[0];
    private boolean isTipsAmountOptionsExist = false;
    private String[] tipsPercentageOptions = new String[0];
    private boolean isTipsPercentageOptionsExist = false;
    private String[] cashbackAmountOptions = new String[0];
    private boolean isCashbackAmountOptionsExist = false;
    private String currencyCodeForInputAmountOption = "";
    private boolean isCurrencyCodeForInputAmountOptionExist = false;
    private String currencyExponentForInputAmountOption = "";
    private boolean isCurrencyExponentForInputAmountOptionExist = false;
    private BBDeviceController.OtherAmountOption otherAmountOption = BBDeviceController.OtherAmountOption.CURRENCY;
    private boolean flagHasOtherAmountOption = false;

    SessionData() {
        reset();
    }

    void reset() {
        productId = "";
        isTipAmountTipsPercentageCashback = false;
        inputAmountOption = 0;
        isInputAmountOptionExist = false;
        tipsAmountOptions = new String[0];
        isTipsAmountOptionsExist = false;
        tipsPercentageOptions = new String[0];
        isTipsPercentageOptionsExist = false;
        cashbackAmountOptions = new String[0];
        isCashbackAmountOptionsExist = false;
        currencyCodeForInputAmountOption = "";
        isCurrencyCodeForInputAmountOptionExist = false;
        currencyExponentForInputAmountOption = "";
        isCurrencyExponentForInputAmountOptionExist = false;
        otherAmountOption = BBDeviceController.OtherAmountOption.CURRENCY;
        flagHasOtherAmountOption = false;
    }

    String getProductId() {
        return productId;
    }

    void setProductId(String productId) {
        this.productId = productId;
    }

    boolean isTipAmountTipsPercentageCashback() {
        return isTipAmountTipsPercentageCashback;
    }

    void setTipAmountTipsPercentageCashback(boolean tipAmountTipsPercentageCashback) {
        isTipAmountTipsPercentageCashback = tipAmountTipsPercentageCashback;
    }

    int getInputAmountOption() {
        return inputAmountOption;
    }

    void setInputAmountOption(int inputAmountOption) {
        this.inputAmountOption = inputAmountOption;
    }

    String[] getTipsAmountOptions() {
        return tipsAmountOptions;
    }

    void setTipsAmountOptions(String[] tipsAmountOptions) {
        this.tipsAmountOptions = tipsAmountOptions;
    }

    String[] getTipsPercentageOptions() {
        return tipsPercentageOptions;
    }

    void setTipsPercentageOptions(String[] tipsPercentageOptions) {
        this.tipsPercentageOptions = tipsPercentageOptions;
    }

    String[] getCashbackAmountOptions() {
        return cashbackAmountOptions;
    }

    void setCashbackAmountOptions(String[] cashbackAmountOptions) {
        this.cashbackAmountOptions = cashbackAmountOptions;
    }

    String getCurrencyCodeForInputAmountOption() {
        return currencyCodeForInputAmountOption;
    }

    void setCurrencyCodeForInputAmountOption(String currencyCodeForInputAmountOption) {
        this.currencyCodeForInputAmountOption = currencyCodeForInputAmountOption;
    }

    String getCurrencyExponentForInputAmountOption() {
        return currencyExponentForInputAmountOption;
    }

    void setCurrencyExponentForInputAmountOption(String currencyExponentForInputAmountOption) {
        this.currencyExponentForInputAmountOption = currencyExponentForInputAmountOption;
    }

    boolean isInputAmountOptionExist() {
        return isInputAmountOptionExist;
    }

    void setInputAmountOptionExist(boolean inputAmountOptionExist) {
        isInputAmountOptionExist = inputAmountOptionExist;
    }

    boolean isTipsAmountOptionsExist() {
        return isTipsAmountOptionsExist;
    }

    void setTipsAmountOptionsExist(boolean tipsAmountOptionsExist) {
        isTipsAmountOptionsExist = tipsAmountOptionsExist;
    }

    boolean isTipsPercentageOptionsExist() {
        return isTipsPercentageOptionsExist;
    }

    void setTipsPercentageOptionsExist(boolean tipsPercentageOptionsExist) {
        isTipsPercentageOptionsExist = tipsPercentageOptionsExist;
    }

    boolean isCashbackAmountOptionsExist() {
        return isCashbackAmountOptionsExist;
    }

    void setCashbackAmountOptionsExist(boolean cashbackAmountOptionsExist) {
        isCashbackAmountOptionsExist = cashbackAmountOptionsExist;
    }

    boolean isCurrencyCodeForInputAmountOptionExist() {
        return isCurrencyCodeForInputAmountOptionExist;
    }

    void setCurrencyCodeForInputAmountOptionExist(boolean currencyCodeForInputAmountOptionExist) {
        isCurrencyCodeForInputAmountOptionExist = currencyCodeForInputAmountOptionExist;
    }

    boolean isCurrencyExponentForInputAmountOptionExist() {
        return isCurrencyExponentForInputAmountOptionExist;
    }

    void setCurrencyExponentForInputAmountOptionExist(boolean currencyExponentForInputAmountOptionExist) {
        isCurrencyExponentForInputAmountOptionExist = currencyExponentForInputAmountOptionExist;
    }

    BBDeviceController.OtherAmountOption getOtherAmountOption() {
        return otherAmountOption;
    }

    void setOtherAmountOption(BBDeviceController.OtherAmountOption otherAmountOption) {
        this.otherAmountOption = otherAmountOption;
    }

    boolean isFlagHasOtherAmountOption() {
        return flagHasOtherAmountOption;
    }

    void setFlagHasOtherAmountOption(boolean flagHasOtherAmountOption) {
        this.flagHasOtherAmountOption = flagHasOtherAmountOption;
    }
}
