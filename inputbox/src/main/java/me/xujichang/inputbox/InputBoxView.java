package me.xujichang.inputbox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

/**
 * 自定义View实现支付宝支付的密码输入样式
 * Created by xujichang on 2017/2/27.
 */

public class InputBoxView extends View {

    /**
     * 默认大小
     */
    private int defaultSize;
    /**
     * 数量
     */
    private int count = 6;

    private ArrayList<Object> array = new ArrayList<>();
    private int size;
    private RectF roundRect;
    private float roundRadius;
    private Paint borderPaint;
    private Paint dotPaint;
    private int dotColor;
    private int borderColor;
    private float dp;
    private InputMethodManager inputManager;

    public InputBoxView(Context context) {
        super(context);
        init();
    }

    public InputBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InputBoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public InputBoxView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    void init() {
        dp = getResources().getDisplayMetrics().density;
        dotColor = Color.LTGRAY;
        borderColor = Color.GRAY;
        defaultSize = (int) (50 * dp);
        roundRect = new RectF();
        roundRadius = 10 * dp;
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStrokeWidth(2);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(dotColor);
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStrokeWidth(3);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(borderColor);
        setFocusable(true);
        setFocusableInTouchMode(true);
        inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        setOnKeyListener(new onKeyDownListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            inputManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            inputManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //现先得到宽高使用的模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //如果宽度未指定
        size = (int) defaultSize;
        if (widthMode == MeasureSpec.AT_MOST) {
            if (heightMode == MeasureSpec.AT_MOST) {
                //都是未设置宽高，则取默认，size 为每一个格子大小，则宽度为个格子大小*数量
                widthSize = defaultSize * count;
                heightSize = defaultSize;
                size = defaultSize;
            } else {
                widthSize = heightSize * count;
                size = heightSize;
            }
        } else {
            heightSize = widthSize / count;
            size = heightSize;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth() - 1 * dp;
        float height = getHeight() - 1 * dp;
        roundRect.set(0, 0, width, height);
        canvas.drawRoundRect(roundRect, roundRadius, roundRadius, borderPaint);
        for (int i = 1; i < count; i++) {
            int x = size * i;
            canvas.drawLine(x, 0, x, height, borderPaint);
        }

        int dotRadius = size / 6;
        int arrayLength = array.size();
        for (int i = 0; i < arrayLength; i++) {
            float x = (size * (i + 0.5f));
            float y = height / 2;
            canvas.drawCircle(x, y, dotRadius, dotPaint);
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        outAttrs.inputType = EditorInfo.TYPE_CLASS_NUMBER;
        return new SelfInputConnection(this, false);
    }

    /**
     * Android 中的键盘设计 是通过对标准键盘的映射组合实现的，所以会有MetaState标志位
     */
    private class onKeyDownListener implements OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.isShiftPressed()) {
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return false;
            }
            if (event.getAction() == KeyEvent.ACTION_DOWN) {

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    inputManager.hideSoftInputFromWindow(getWindowToken(), 0);
                    ensureFinished();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (array.size() == 0) {
                        return true;
                    }
                    array.remove(array.size() - 1);
                    invalidate();
                }
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    array.add(keyCode - KeyEvent.KEYCODE_0);
                    invalidate();
                    ensureFinished();
                    return true;
                }
            }
            return false;
        }
    }

    private void ensureFinished() {
        if (array.size() == count) {
            inputManager.hideSoftInputFromWindow(getWindowToken(), 0);
            if (null != callBack) {
                StringBuilder builder = new StringBuilder();
                for (Object o : array) {
                    builder.append(o);
                }
                callBack.onFinish(builder.toString());
            }
        }
    }

    private BoxCallBack callBack;

    public void setCallBack(BoxCallBack callBack) {
        this.callBack = callBack;
    }

    public interface BoxCallBack {
        void onFinish(String str);
    }
}
