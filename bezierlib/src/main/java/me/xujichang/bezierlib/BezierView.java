package me.xujichang.bezierlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 贝塞尔曲线
 */
public class BezierView extends View {
    private Paint mDefaultLinePaint;
    private Paint mDefaultPointPaint;
    private PointF pointRight;
    private PointF pointLeft;


    public BezierView(Context context) {
        super(context);
        init(null, 0);
    }

    public BezierView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BezierView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        //加载属性
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.BezierView, defStyle, 0);
        a.recycle();
        //初始化 画笔
        mDefaultLinePaint = new Paint();
        mDefaultLinePaint.setColor(Color.BLUE);
        mDefaultPointPaint = new Paint();
        mDefaultPointPaint.setColor(Color.BLACK);
        pointLeft = new PointF();
        pointRight = new PointF();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float x = getX();
        float y = getY();
        int width = getWidth();
        int height = getHeight();
        //左点
        pointLeft.set(20, height / 2);
        pointRight.set(width - 20, height / 2);

        //画两点
        canvas.drawCircle(pointLeft.x, pointLeft.y, 10, mDefaultPointPaint);
        canvas.drawCircle(pointRight.x, pointRight.y, 10, mDefaultPointPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        invalidate();
    }
}
