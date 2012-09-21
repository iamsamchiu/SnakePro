/*
 * Copyright (C) 2008 Google Inc.
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

package com.example.android.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


/**
 * DPadView is a virtual D-Pad view. It will draw four direction button on screen and dispatch
 * four direction key event to the mParentView
 */
public class DPadView extends View {
    private static final String TAG = "DPadView";

    private static final int BTN_UP_IDX = 0;
    private static final int BTN_DOWN_IDX = 1;
    private static final int BTN_LEFT_IDX = 2;
    private static final int BTN_RIGHT_IDX = 3;
    
    private static final int FONT_SIZE = 40;
    private final DpadBtn[] mDPadBtns= new DpadBtn[] { new DpadBtn(), new DpadBtn() , new DpadBtn() , new DpadBtn() };
    // State used while dragging a caption around
    private boolean mDragging;
    private int mDragCaptionIndex;  // index of the caption (in mCaptions[]) that's being dragged
    private int mTouchDownX, mTouchDownY;
    private final Rect mTmpRect = new Rect();
    private int mBeforeMoveX=0;
    private int mBeforeMoveY=0;
    
    //
    private View mParentView;
    

    public DPadView(Context context) {
        super(context);
        initDpad();
    }

    public DPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDpad();
    }

    public DPadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDpad();
    }

    private void initDpad(){
    	setCaptions("UP", "DOWN", "LEFT", "RIGHT");
    }
    
    public void setParentView(View view){
    	mParentView = view;
    }
    
    private void setCaptions(String topCaption, String bottomCaption, String leftCaption , String rightCaption) {
        Log.i(TAG, "setCaptions: '" + topCaption + "', '" + bottomCaption + "'"+ "', '" + leftCaption + "'"+ "', '" + rightCaption + "'");
        if (topCaption == null) topCaption = "";
        if (bottomCaption == null) bottomCaption = "";
        if (leftCaption == null) leftCaption = "";
        if (rightCaption == null) rightCaption = "";

        mDPadBtns[0].caption = topCaption;
        mDPadBtns[1].caption = bottomCaption;
        mDPadBtns[2].caption = leftCaption;
        mDPadBtns[3].caption = rightCaption;

        mDPadBtns[0].btnBoundingBox = null;
        mDPadBtns[1].btnBoundingBox = null;
        mDPadBtns[2].btnBoundingBox = null;
        mDPadBtns[3].btnBoundingBox = null;


    }


    private void renderDPad(DpadBtn[] captions,Canvas canvas) {

    	String maxStr = "RIGHT";
    	int maxStrLength = maxStr.length();
        int textWidth, textHeight;
    	
        //Log.i(TAG, "- Canvas: " + canvas + "  dimensions: " + canvas.getWidth() + " x " + canvas.getHeight());

        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(FONT_SIZE);
        textPaint.setColor(0xFFFFFFFF);
        //Log.i(TAG, "- Paint: " + textPaint);
        Typeface face = textPaint.getTypeface();
        face = Typeface.DEFAULT_BOLD;
        textPaint.setTypeface(face);
        textPaint.setTextAlign(Align.CENTER);
        

        final int edgeBorder = 20;
        final int fontHeight = textPaint.getFontMetricsInt(null)+4;
        //Log.i(TAG, "- fontHeight: " + fontHeight);
        textPaint.getTextBounds(maxStr, 0, maxStrLength, mTmpRect);
        textWidth = mTmpRect.width()+4;
        textHeight = mTmpRect.height();
        //Log.i(TAG, "- Caption rect,textWidth:"+textWidth+",textHeight:"+textHeight);
        

        Log.d(TAG, "- Caption positioning:");
        int topX = 0;
        int topY = 0;
        if (mDPadBtns[0].positionValid) {
            topX = mDPadBtns[0].xpos;
            topY = mDPadBtns[0].ypos;
            mDPadBtns[0].setPosition(topX, topY);
            Log.d(TAG, "  - UP: already had a valid position: " + topX + ", " + topY);
        } else {
            topX = edgeBorder*3 + textWidth;
            topY = edgeBorder + (fontHeight * 3 / 4);
            mDPadBtns[0].setPosition(topX, topY);
            Log.d(TAG, "  - UP: initializing to default position: " + topX + ", " + topY);
        }
        

        int bottomX = 0;
        int bottomY = 0;
        bottomX = topX;
        bottomY = topY + (fontHeight+edgeBorder)*2;
        mDPadBtns[1].setPosition(bottomX, bottomY);
        Log.d(TAG, "  - DOWN: initializing to default position: "
              + bottomX + ", " + bottomY);
    
        
        
        int leftX = 0;
        int leftY = 0;
        leftX = topX - textWidth;
        leftY = topY + fontHeight+edgeBorder;
        mDPadBtns[2].setPosition(leftX, leftY);
        Log.d(TAG, "  - LEFT: initializing to default position: "
              + leftX + ", " + leftY);
    
        
        
        int rightX = 0;
        int rightY = 0;
        rightX = topX + textWidth;
        rightY = leftY;
        mDPadBtns[3].setPosition(rightX, rightY);
        Log.d(TAG, "  - RIGHT: initializing to default position: "
              + rightX + ", " + rightY);
    
    



        for (int i = 0 ;i < mDPadBtns.length ; i++){
        	if ( mDPadBtns[i].btnBoundingBox == null) {
        		mDPadBtns[i].btnBoundingBox = new Rect(mDPadBtns[i].xpos, mDPadBtns[i].ypos - textHeight, mDPadBtns[i].xpos + textWidth, mDPadBtns[i].ypos);
        		//Log.i(TAG, "-   RESULTING RECT: " + mDPadBtns[i].btnBoundingBox);
        	}
        }

            
        
        //draw text and button frame
        for (int i = 0 ;i < mDPadBtns.length ; i++){
        	canvas.drawText(mDPadBtns[i].caption, mDPadBtns[i].xpos, mDPadBtns[i].ypos, textPaint);
            Paint p = new Paint();
            p.setColor(0xFFFFFFFF);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2f);
            Rect r = mDPadBtns[i].btnBoundingBox;
            r.inset(0, -edgeBorder);
            r.offset(-(textWidth/2), 0);
            canvas.drawRect(r, p);
        }
       
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "==onDraw==: " + canvas);
        super.onDraw(canvas);
        
        renderDPad(this.mDPadBtns,canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.i(TAG, "onTouchEvent: " + ev);


        Matrix m = getMatrix();

        Matrix invertedMatrix = new Matrix();
        m.invert(invertedMatrix);

        float[] pointArray = new float[] { ev.getX() - getPaddingLeft(),
                                           ev.getY() - getPaddingTop() };
        Log.i(TAG, "  - BEFORE: pointArray = " + pointArray[0] + ", " + pointArray[1]);
        // Transform the X/Y position of the DOWN event back into bitmap coords
        invertedMatrix.mapPoints(pointArray);
        Log.i(TAG, "  - AFTER:  pointArray = " + pointArray[0] + ", " + pointArray[1]);

        int eventX = (int) pointArray[0];
        int eventY = (int) pointArray[1];

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mDragging) {
                    Log.w(TAG, "Got an ACTION_DOWN, but we were already dragging!");
                    mDragging = false;  // and continue as if we weren't already dragging...
                }


                // See if this DOWN event hit one of the caption bounding
                // boxes.  If so, start dragging!
                for (int i = 0; i < mDPadBtns.length; i++) {
                    Rect boundingBox = mDPadBtns[i].btnBoundingBox;
                    Log.i(TAG, "  - boundingBox #" + i + ": " + boundingBox + "...");

                    if (boundingBox != null) {

                        mTmpRect.set(boundingBox);

                        final int touchPositionSlop = 2;  // pixels
                        mTmpRect.inset(-touchPositionSlop, -touchPositionSlop);

                        Log.i(TAG, "  - Checking expanded bounding box #" + i
                              + ": " + mTmpRect + "...");
                        if (mTmpRect.contains(eventX, eventY)) {
                            Log.i(TAG, "    - Hit! " + mDPadBtns[i]);
                            mDragging = true;
                            mDragCaptionIndex = i;
                            
                            //start dispatch key event to parent view
                            switch (i){
                            case BTN_UP_IDX:
                        		mParentView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                        			    KeyEvent.KEYCODE_DPAD_UP, 0));
                        		mParentView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP,
                        			    KeyEvent.KEYCODE_DPAD_UP, 0));
                        		Log.v(TAG,"DPadView onClick:btn_U");
                            	break;
                            case BTN_DOWN_IDX:
                        		mParentView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                        			    KeyEvent.KEYCODE_DPAD_DOWN, 0));
                        		mParentView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP,
                        			    KeyEvent.KEYCODE_DPAD_DOWN, 0));
                        		Log.v(TAG,"DPadView onClick:btn_D");
                            	break;
                            case BTN_LEFT_IDX:
                        		mParentView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                        			    KeyEvent.KEYCODE_DPAD_LEFT, 0));
                        		mParentView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP,
                        			    KeyEvent.KEYCODE_DPAD_LEFT, 0));
                        		Log.v(TAG,"DPadView onClick:btn_L");
                            	break;
                            case BTN_RIGHT_IDX:
                        		mParentView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                        			    KeyEvent.KEYCODE_DPAD_RIGHT, 0));
                        		mParentView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP,
                        			    KeyEvent.KEYCODE_DPAD_RIGHT, 0));
                        		Log.v(TAG,"DPadView onClick:btn_R");
                            	break;
                            }
                            break;
                        }
                    }
                }
                if (!mDragging) {
                    Log.i(TAG, "- ACTION_DOWN event didn't hit any captions; ignoring.");
                    return true;
                }

                mTouchDownX = eventX;
                mTouchDownY = eventY;
                mBeforeMoveX = mDPadBtns[0].xpos;
                mBeforeMoveY = mDPadBtns[0].ypos;


                invalidate();

                return true;

            case MotionEvent.ACTION_MOVE:
                if (!mDragging) {
                    return true;
                }

                int displacementX = eventX - mTouchDownX;
                int displacementY = eventY - mTouchDownY;

                
                mDPadBtns[0].xpos =  mBeforeMoveX+displacementX;
                mDPadBtns[0].ypos =  mBeforeMoveY+displacementY;

                invalidate();

                return true;

            case MotionEvent.ACTION_UP:
                if (!mDragging) {
                    return true;
                }

                mDragging = false;

                // Reposition the selected caption!
                Log.i(TAG, "- Done dragging!  Repositioning caption #" + mDragCaptionIndex + ": "
                      + mDPadBtns[mDragCaptionIndex]);

                int offsetX = eventX - mTouchDownX;
                int offsetY = eventY - mTouchDownY;
                Log.i(TAG, "  - OFFSET: " + offsetX + ", " + offsetY);


                invalidate();
                return true;

            // This case isn't expected to happen.
            case MotionEvent.ACTION_CANCEL:
                if (!mDragging) {
                    return true;
                }

                mDragging = false;

                //drawableStateChanged();
                return true;

            default:
                return super.onTouchEvent(ev);
        }
    }


    /**
     * Structure used to hold the entire state of a single caption.
     */
    class DpadBtn {
        public String caption;
        public Rect btnBoundingBox;  // updated by renderCaptions()
        public int xpos, ypos;
        public boolean positionValid;

        public void setPosition(int x, int y) {
            positionValid = true;
            xpos = x;
            ypos = y;
            // set bounding box as null to re compute it in renderDpad
            btnBoundingBox = null;
        }

        @Override
        public String toString() {
            return "Caption['" + caption + "'; bbox " + btnBoundingBox
                    + "; pos " + xpos + ", " + ypos + "; posValid = " + positionValid + "]";
        }
    }

}
