package com.google.android.gms.samples.vision.ocrreader.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class OcrFromImage {

    private static OcrFromImage instance;
    public static final String TAG = OcrFromImage.class.getName();


    /**
     * empty private constructor
     */
    private OcrFromImage() {
    }
    /**
     * get instance of this class
     * @return
     */
    public static OcrFromImage getInstance() {
        if (instance == null) {
            synchronized (OcrFromImage.class) {
                if (instance == null) {
                    instance = new OcrFromImage();
                }
            }
        }
        return instance;
    }

    /**
     * public method caled by object from this class take bitmap image and context as parametere
     * @param ctx
     * @param image
     * @return  String
     */
    public String getTextFromImage(Context ctx, Bitmap image) {
        String value = "";  // variable to store the text recognized from image and this is the return value
        SparseArray<TextBlock> mTextBlockSparseArray = readImage(ctx, image);
        List<TextBlock> mTextBlocksList = getDataAsList(mTextBlockSparseArray);
        Map<Integer, List<TextBlock>> preFinalMap = getMapWithListOfTextBlock(mTextBlocksList);
        value = getFinalString(preFinalMap);
        return value;
    }

    /**
     * this part is the textrecognizer operation to take
     * bitmap image and return SparesArray from textblock
     *
     * @param ctx   --> context
     * @param image --> bitmap
     * @return SparseArray<TextBlock>
     */
    private SparseArray<TextBlock> readImage(Context ctx, Bitmap image) {
        TextRecognizer ocrFrame = new TextRecognizer.Builder(ctx).build();
        Frame frame = new Frame.Builder().setBitmap(image).build();
        if (ocrFrame.isOperational()) {
            Log.e(TAG, "Textrecognizer is operational");
        }
        // SparseArray<TextBlock> textBlocks = ocrFrame.detect(frame);
        return ocrFrame.detect(frame);
    }

    /**
     * Take SparseArray of TextBlock and convert it to list of TextBlock
     *
     * @param textBlockSparseArray --> SparseArray
     * @return list<TextBlock>
     */
    private List<TextBlock> getDataAsList(SparseArray<TextBlock> textBlockSparseArray) {
        List<TextBlock> textBlockList = new ArrayList<>();
        for (int i = 0; i < textBlockSparseArray.size(); i++) {
            TextBlock textBlock = textBlockSparseArray.get(textBlockSparseArray.keyAt(i));
            textBlockList.add(textBlock);
            System.out.println(i + "       " + textBlock.getBoundingBox().top + "          " + textBlock.getValue());
        }
        return textBlockList;
    }

    /**
     * @param mTextBlockList --> List<TextBlock>
     * @return map with integer key and value as List<TextBlock>
     */
    private Map<Integer, List<TextBlock>> getMapWithListOfTextBlock(List<TextBlock> mTextBlockList) {
        Set<Integer> topBoundingValues = new HashSet<>();  // set to store top value in it without duplicate

        // this part to  loop on list <textblock> and get top bounding and store it in set
        for (TextBlock textBlock : mTextBlockList) {
            topBoundingValues.add(textBlock.getBoundingBox().top);
        }
        List<Integer> sortedTopBoundingValues = new ArrayList<>(topBoundingValues);                  // list of top bounding box to be sorted
        Collections.sort(sortedTopBoundingValues);                                                   // sort list of bounding

        /**
         * map to store list <textblock>  as value with top as key
         *      ===========================================================================================
         *      ||         Key                    ||               Value  ( must be sorted by left )     ||
         *      ||================================||=====================================================||
         *      ||           20                   ||            List<TextBlock>  have the textblock      ||
         *      ||                                ||              with the same top bounding             ||
         *      ||================================||=====================================================||
         *      ||           30                   ||            List<TextBlock>  have the textblock      ||
         *      ||                                ||            with the same top bounding               ||
         *      ||================================||=====================================================||
         *      ||           57                   ||            List<TextBlock>  have the textblock      ||
         *      ||                                ||            with the same top bounding               ||
         *      ||                                ||                                                     ||
         *      ===========================================================================================
         */
        Map<Integer, List<TextBlock>> preFinalMap = new HashMap<>();
        for (Integer i : sortedTopBoundingValues) {
            System.out.println(i);
            List<TextBlock> values = new ArrayList<>();
            for (TextBlock textBlock : mTextBlockList) {
                if (textBlock.getBoundingBox().top == i) {
                    values.add(textBlock);
                }
            }
            preFinalMap.put(i, values);
        }
        return preFinalMap;
    }

    /**
     * get the list of each top value and sort this list by the left value
     * then convert the Textblock to list of text and store it in map<integer, string> and this is the final map
     * iterate on the prefinal map and store each top as key and text as calue in this map
     * ** final step ** iterate on the map and create the final string
     *
     * @param preFinalMap
     * @return
     */
    private String getFinalString(Map<Integer, List<TextBlock>> preFinalMap) {
        String result = "";
        TreeMap<Integer, List<TextBlock>> sorted = new TreeMap<>(preFinalMap);  // to sort the map
        Map<Integer, String> finalMapStringUnSorted = new HashMap<>();
        for (Map.Entry<Integer, List<TextBlock>> entry : sorted.entrySet()) {

            /**
             * this part to sort the list by the left bounding to make sure the the smallest left bounding is the first element
             */
            Collections.sort(entry.getValue(), new Comparator<TextBlock>() {
                @Override
                public int compare(TextBlock textBlock, TextBlock t1) {
                    return textBlock.getBoundingBox().left - t1.getBoundingBox().left;
                }
            });
            for (TextBlock s : entry.getValue()) {
                if (s.getComponents().size() > 1) {
                    List<Text> texts = (List<Text>) s.getComponents();
                    for (Text text : texts) {

                        /**
                         * this part to check if the top value is inserted before in the map or not
                         * if it in the map get the value of it in temp variable then concat the new
                         * value with this temp value --- it will generate right value to be stored in the map
                         */
                        if (finalMapStringUnSorted.containsKey(text.getBoundingBox().top)) {
                            String temp = finalMapStringUnSorted.get(text.getBoundingBox().top);
                            finalMapStringUnSorted.put(text.getBoundingBox().top, temp + " " + text.getValue());
                        } else {
                            finalMapStringUnSorted.put(text.getBoundingBox().top, text.getValue());
                        }
                    }
                } else {
                    if (finalMapStringUnSorted.containsKey(s.getBoundingBox().top)) {
                        String temp = finalMapStringUnSorted.get(s.getBoundingBox().top);
                        finalMapStringUnSorted.put(s.getBoundingBox().top, temp + " " + s.getValue());
                    } else {
                        finalMapStringUnSorted.put(s.getBoundingBox().top, s.getValue());
                    }
                }
            }
        }

        /**
         * sortef map of the final map
         */
        TreeMap<Integer, String> finalMapSorted = new TreeMap<>(finalMapStringUnSorted);

        /**
         * iterate on the map of string and create the final string result
         */
        for (Map.Entry<Integer, String> entry : finalMapSorted.entrySet()) {
            result += entry.getValue()
                    + "\n";
        }

        return result;
    }


}
