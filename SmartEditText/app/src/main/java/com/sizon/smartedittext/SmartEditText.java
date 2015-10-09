/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.sizon.smartedittext;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.EditText;

public class SmartEditText extends EditText{
	
	private Context context;
	private Paint paint ;
	private TextPaint tPaint;
	private int width;
	private int height;
	private float ratioOut = 0f;
	private float ratioIn = 0f;
	private float INCREMENT = 0.1f;//增量，控制动画速度
	
	private boolean isLableOut = false;//标签是否在外面
	
	private int paddingLeft = 15;
	private int paddingRight = 15;
	private int paddingBottom = 10;
	private int paddingTop = 50;
	private float lineLeft = 7.5f;
	private int defaultLineColor = 0x4d000000;
	private int defaultTextColor = 0x4d000000;
	private int endLineColor = 0xff00ff00;
	private int endTextColor = 0xff00ff00;
	
	private float hintTextSize = 15f;
	private float mainTextSize = 30f;//由getText获得的是像素值
	private float cursorSize = mainTextSize + mainTextSize * 3 / 4;
	private String hintText= "test";
	private int lineHeight = 3;//控制线的高度
	private float textMarginTop = 12.5f;//控制字体的绘制位置
	
	public enum AnimateMode{
		LEFT_TO_RIGHT,RIGHT_TO_LEFT,MIDDLE_TO_SIDES;
	}
	private AnimateMode curMode = AnimateMode.LEFT_TO_RIGHT;
	//547326
    public SmartEditText(Context context) {
        this(context, null);
    }

    public SmartEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public SmartEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init(){
    	setWillNotDraw(false);
    	paint = new Paint();
    	paint.setFlags(Paint.ANTI_ALIAS_FLAG);//消除锯齿
    	tPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    	tPaint.setTextSize(dp2px(hintTextSize));
    	super.setPadding((int)dp2px(paddingLeft), (int)dp2px(paddingTop),
    			(int)dp2px(paddingRight), (int)dp2px(paddingBottom) );
    	mainTextSize = getTextSize();
    }
    @Override
    protected boolean getDefaultEditable() {
        return true;
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    @Override
    public Editable getText() {
        return (Editable) super.getText();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }

    /**
     * Convenience for {@link Selection#setSelection(Spannable, int, int)}.
     */
    public void setSelection(int start, int stop) {
        Selection.setSelection(getText(), start, stop);
    }

    /**
     * Convenience for {@link Selection#setSelection(Spannable, int)}.
     */
    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }

    /**
     * Convenience for {@link Selection#selectAll}.
     */
    public void selectAll() {
        Selection.selectAll(getText());
    }

    /**
     * Convenience for {@link Selection#extendSelection}.
     */
    public void extendSelection(int index) {
        Selection.extendSelection(getText(), index);
    }

    @Override
    public void setEllipsize(TextUtils.TruncateAt ellipsis) {
        if (ellipsis == TextUtils.TruncateAt.MARQUEE) {
            throw new IllegalArgumentException("EditText cannot use the ellipsize mode "
                    + "TextUtils.TruncateAt.MARQUEE");
        }
        super.setEllipsize(ellipsis);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	width = getWidth();
    	height = getHeight();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	if (!hasFocus()) {
    		drawDefaultLineAndText(canvas);
		}else {
			animateLableOut(canvas);
		}
    	if(ratioOut >=0 && ratioOut <=1  || ratioIn >=0 && ratioIn <= 1) {
			invalidate();
		}
    	super.onDraw(canvas);
    }
    
    private void drawDefaultLineAndText(Canvas canvas) {
    	ratioOut = 0;
		paint.setColor(defaultLineColor);
//		tPaint.setColor(defaultTextColor);
		canvas.drawRect(dp2px(lineLeft), height-dp2px(lineHeight), width-dp2px(lineLeft), height, paint);
		ratioIn -= INCREMENT;
		if (ratioIn <= 0) {
			ratioIn = 0;
			ratioOut = ratioIn;
		}
		paint.setColor(endLineColor);
		if (ratioIn == 0) {
			canvas.drawRect(dp2px(lineLeft), height-dp2px(lineHeight), dp2px(lineLeft), height, paint);//解决一些手机上会绘制0-left的横线
		}else {
			canvas.drawRect(dp2px(lineLeft), height-dp2px(lineHeight), ratioIn*((float)(width-dp2px(lineLeft))), height, paint);
		}
		if (TextUtils.isEmpty(this.getText().toString().trim())) {
			if (isLableOut) {
				if (ratioIn == 0) {
					tPaint.setColor(defaultTextColor);
					canvas.drawText(hintText, dp2px(paddingLeft), (float)(height-dp2px(textMarginTop)), tPaint);
				}else {
					tPaint.setColor(endTextColor);
					canvas.drawText(hintText, dp2px(paddingLeft), (float)(height-dp2px(textMarginTop))-(ratioIn*(mainTextSize + dp2px(10f))), tPaint);
				}
			}else{
				if (ratioIn == 0) {
					tPaint.setColor(defaultTextColor);
					canvas.drawText(hintText, dp2px(paddingLeft), (float)(height-dp2px(textMarginTop)), tPaint);
				}
			}
		}else{
			if (ratioIn == 0) {
				tPaint.setColor(defaultTextColor);
			}else {
				tPaint.setColor(endTextColor);
			}
			canvas.drawText(hintText, dp2px(paddingLeft), (height-(cursorSize + dp2px(textMarginTop))), tPaint);
		}
    }
    
    private void animateLableOut(Canvas canvas) {
		ratioOut += INCREMENT;
		paint.setColor(defaultLineColor);
		canvas.drawRect(dp2px(lineLeft), height-dp2px(lineHeight), width-dp2px(lineLeft), height, paint);
		paint.setColor(endLineColor);
		tPaint.setColor(endTextColor);
		if (ratioOut >= 1) {
			ratioOut = 1;
			ratioIn = ratioOut;
		}
		canvas.drawRect(dp2px(lineLeft), height-dp2px(lineHeight), ratioOut*((float)(width-dp2px(lineLeft))), height, paint);
		if (TextUtils.isEmpty(this.getText().toString().trim())) {
//			canvas.drawText(hintText, dp2px(paddingLeft), (float)(height-dp2px(textMarginTop))-(ratioOut*(mainTextSize + dp2px(10f))), tPaint);
			canvas.drawText(hintText, dp2px(paddingLeft), (float)(height-dp2px(textMarginTop))-(ratioOut*(cursorSize)), tPaint);
		}else {
//			canvas.drawText(hintText, dp2px(paddingLeft), (height-(mainTextSize + dp2px(22.5f))), tPaint);
			canvas.drawText(hintText, dp2px(paddingLeft), (height-(cursorSize + dp2px(textMarginTop))), tPaint);
//			ratio = 1.1f;//阻止重绘
		}
		
		isLableOut = true;
    }
    
    @Override
    public boolean hasFocus() {
    	return super.hasFocus();
    }
    
    public void setMode(AnimateMode mode) {
    	curMode = mode;
    }
    
    public AnimateMode getMode() {
    	return curMode;
    }
    
    /**
	 * dp2px
	 * 
	 * @param dpValue
	 *            dp
	 * @return int px
	 * @throws
	 */
	public float dp2px( float dpValue) {
		return (float) (dpValue * getDensity() + 0.5f);
	}
	
	public float getDensity() {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		return dm.density;
	}
	
}
