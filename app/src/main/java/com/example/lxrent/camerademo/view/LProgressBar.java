package com.example.lxrent.camerademo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.lxrent.camerademo.R;

import static com.example.lxrent.camerademo.R.attr.innerRadio;


/**
 * Created by 卢东方 on 2017/5/11 下午5:52.
 * <p>
 * God love people
 * <p>
 * description:
 */
public class LProgressBar extends View {
    /**
     * 内圆颜色
     */
    private int innerColor = Color.WHITE;
    /**
     * 外圈颜色
     */
    private int outerColor = Color.GRAY;
    /**
     * 进度颜色
     */
    private int progressColor = Color.parseColor("#00FF00");
    /**
     * 进条度宽
     */
    private int progressWidth = 5;
    /**
     * 外圆半径
     */
    private int outerRadio = 60;
    /**
     * 内圆半径
     */
    private float innerRadio = outerRadio* 0.7f;
    /**
     * 外圈  进度  内圈  paint
     */
    private Paint backgroundPaint, progressPaint, innerPaint;
    /**
     * 手势时事件
     */
    private GestureDetectorCompat mDetector;
    /**
     * 是否为长按录制
     */
    private boolean isLongClick;
    private RectF rectF;
    private float startAngle = -90;
    private float sweepAngle;
    private int progress = 0;
    /**
     * 默认是10秒
     */
    private int maxSecond = 1000;


    private Handler mHandler = new Handler();
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            progress++;
            if (progress > maxSecond) {
                reset();
                if (listener != null) {
                    listener.onLongClickUp(LProgressBar.this);
                }
                return;
            }
            invalidate();
            mHandler.postDelayed(task, 10);
        }
    };

    public LProgressBar(Context context) {
        super(context);
        init(context, null);
    }


    public LProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LProgressBar);
            innerColor = a.getColor(R.styleable.LProgressBar_innerColor, innerColor);
            outerColor = a.getColor(R.styleable.LProgressBar_outerColor, outerColor);
            progressColor = a.getColor(R.styleable.LProgressBar_progressColor, progressColor);
            innerRadio = a.getDimension(R.styleable.LProgressBar_innerRadio, innerRadio);
            progressWidth = a.getDimensionPixelOffset(R.styleable.LProgressBar_progressWidth, progressWidth);
            maxSecond = a.getInt(R.styleable.LProgressBar_maxSecond, maxSecond)*100;//默认扩大100倍
            Log.i("eeeeeeeee",a.getIndexCount()+"");
            a.recycle();
        }

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(outerColor);

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeWidth(progressWidth);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);

        innerPaint = new Paint();
        innerPaint.setAntiAlias(true);
        innerPaint.setColor(innerColor);

        mDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                isLongClick = false;
                if (LProgressBar.this.listener != null) {
                    LProgressBar.this.listener.onClick(LProgressBar.this);
                }
                return super.onSingleTapUp(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                //长按可以处理不断绘制
                isLongClick = true;
                if (LProgressBar.this.listener != null) {
                    LProgressBar.this.listener.onLongClick(LProgressBar.this);
                }
                mHandler.postDelayed(task, 0);
            }


        });


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (width > height) {
            setMeasuredDimension(height, height);
            outerRadio = height / 2;

        } else {
            setMeasuredDimension(width, width);
            outerRadio = width / 2;
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //确定圆心
        float circle = getWidth() / 2.0f;
        if (!isLongClick) {
            //绘制外圆
            canvas.drawCircle(circle, circle, outerRadio * 0.7f, backgroundPaint);
            //绘制内圆
            canvas.drawCircle(circle, circle, innerRadio * 0.7f, innerPaint);
        } else {
            //绘制外圆
            canvas.drawCircle(circle, circle, outerRadio, backgroundPaint);
            //绘制内圆
            canvas.drawCircle(circle, circle, innerRadio * 0.4f, innerPaint);
        }

        rectF = new RectF(progressWidth / 2, progressWidth / 2, outerRadio * 2 - progressWidth / 2, outerRadio * 2 - progressWidth / 2);
        //绘制进度条
        sweepAngle = ((float) progress / maxSecond) * 360;
        canvas.drawArc(rectF, startAngle, sweepAngle, false, progressPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                isLongClick = false;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isLongClick) {
                    isLongClick = false;
                    if (LProgressBar.this.listener != null) {
                        LProgressBar.this.listener.onLongClickUp(LProgressBar.this);
                    }
                    reset();
                }
                break;
        }


        return true;
    }

    /**
     * 还原到初始状态
     */
    public void reset() {
        isLongClick = false;
        this.progress = 0;
        this.sweepAngle = 0;
        mHandler.removeCallbacks(task);
        invalidate();
    }

    private OnProgressTouchListener listener;

    public void setOnProgressTouchListener(OnProgressTouchListener listener) {
        this.listener = listener;
    }

    public interface OnProgressTouchListener {
        /**
         * 单击
         *
         * @param progressBar
         */
        void onClick(LProgressBar progressBar);

        /**
         * 长按
         *
         * @param progressBar
         */
        void onLongClick(LProgressBar progressBar);

        /**
         * 长按抬起
         *
         * @param progressBar
         */
        void onLongClickUp(LProgressBar progressBar);
    }
}
