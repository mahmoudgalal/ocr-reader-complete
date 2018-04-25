package com.google.android.gms.samples.vision.ocrreader.data;


import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by Modeso A on 26/03/2018.
 */

@Database(name = MyDataBase.NAME, version = MyDataBase.VERSION)
public class MyDataBase {

    public static final String NAME =  "profix_ocr";
    public static final int VERSION = 1;
}
