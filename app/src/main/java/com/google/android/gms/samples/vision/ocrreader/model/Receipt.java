package com.google.android.gms.samples.vision.ocrreader.model;


import com.google.android.gms.samples.vision.ocrreader.data.MyDataBase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = MyDataBase.class)
public class Receipt extends BaseModel {

    @PrimaryKey
    @Column
    private String UUID;
    @Column
    private String imageUrl;
    @Column
    private int totalAmount;

    /*
        start of getter method
     */

    public String getUUID() {
        return UUID;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    /*
        start of setter method here
     */

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

}
