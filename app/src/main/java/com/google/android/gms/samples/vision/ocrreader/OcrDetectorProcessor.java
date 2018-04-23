/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.ocrreader;

import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private View windowView;

    public OnAlignedTextPrepared onAlignedTextPreparedListener;
    private static final String TAG = OcrDetectorProcessor.class.getSimpleName();
    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay,View windowView) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.windowView = windowView;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        //detections.getFrameMetadata().
        List<TextBlock> textBlocksList = new ArrayList<>();

        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
            }
            Rect rect = new Rect();
            rect.top = windowView.getTop();
            rect.right= windowView.getRight();
            rect.left = windowView.getLeft();
            rect.bottom = windowView.getBottom();
            Log.d(TAG,String.format("Rect = %d , %d, %d , %d",rect.top ,rect.bottom,rect.left,rect.right));
            Log.d(TAG,String.format("RectItem = %d , %d, %d , %d",item.getBoundingBox().top ,item.getBoundingBox().bottom,
                    item.getBoundingBox().left,item.getBoundingBox().right));
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
            // Check if the detected TextBlock inside our Scan Window View
            //Note: Coordinate of text is measured relative to the camera not to the View System.
            if(graphic.translateY(item.getBoundingBox().top) >= rect.top && graphic.translateY(item.getBoundingBox().top)
                    <= rect.bottom ) {
                if (item != null && item.getValue() != null) {
                    textBlocksList.add(item);
                    mGraphicOverlay.add(graphic);
                }

            }
        }
        logAlignedBlocks(textBlocksList);
    }

    /**
     * Prepares alist of aligned textblocks that appears inside the scan window and log them
     * @param items
     */
    private void logAlignedBlocks(List<TextBlock> items ){
        if(items.isEmpty())
            return;
        Map<Long,List<String>>  allBlocksAligned = new HashMap<>();
        long id = 0;
        List<TextBlock> sortedBlocks = items;
        Collections.sort(sortedBlocks, new Comparator<TextBlock>() {
            @Override
            public int compare(TextBlock textBlock, TextBlock t1) {
                return textBlock.getBoundingBox().left-t1.getBoundingBox().left;
            }
        });

        for (int i = 0; i < sortedBlocks.size(); ++i){
            TextBlock item = sortedBlocks.get(i);
            if(item == null)
                continue;

            for (int j = i+1; j < sortedBlocks.size(); ++j){
                TextBlock innerItem = sortedBlocks.get(j);
                if(Math.abs(item.getBoundingBox().top - innerItem.getBoundingBox().top)<8){
                    //we found almost aligned items
                   id = item.getBoundingBox().top;//Identify by Y coordinate
                   List<String> alignedItems = allBlocksAligned.get(id);
                    if( alignedItems == null) {
                        alignedItems = new ArrayList<>();
                        allBlocksAligned.put(id,alignedItems);
                        alignedItems.add(item.getValue());
                    }
                    if(!alignedItems.contains(innerItem.getValue()))
                        alignedItems.add(innerItem.getValue());
                }
            }

        }
        if(onAlignedTextPreparedListener != null)
            onAlignedTextPreparedListener.onAlignedTextPrepared(allBlocksAligned);
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }


    public  interface  OnAlignedTextPrepared{
        // Passes you a map where each item containing a list of all items on the same horizontal line.
        void onAlignedTextPrepared( Map<Long,List<String>>  allBlocksAligned);
    }

    public OnAlignedTextPrepared getOnAlignedTextPreparedListener() {
        return onAlignedTextPreparedListener;
    }

    public void setOnAlignedTextPreparedListener(OnAlignedTextPrepared onAlignedTextPreparedListener) {
        this.onAlignedTextPreparedListener = onAlignedTextPreparedListener;
    }

}
