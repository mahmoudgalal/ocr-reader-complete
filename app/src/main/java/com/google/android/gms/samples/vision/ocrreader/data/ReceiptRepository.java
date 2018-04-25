package com.google.android.gms.samples.vision.ocrreader.data;


import com.google.android.gms.samples.vision.ocrreader.model.Receipt;

public class ReceiptRepository {

    private static ReceiptRepository instance;

    private ReceiptRepository(){
    }

    public static ReceiptRepository getInstance() {
        if (instance == null) {
            synchronized (ReceiptRepository.class){
                if (instance == null) {
                    instance = new ReceiptRepository();
                }
            }
        }
        return instance;
    }

    public void saveReceipt(Receipt receipt){
        receipt.save();
    }

    public void updateReceipt(Receipt receipt){
        receipt.update();
    }


}
