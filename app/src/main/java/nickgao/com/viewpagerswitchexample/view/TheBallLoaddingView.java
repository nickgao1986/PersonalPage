package nickgao.com.viewpagerswitchexample.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**
 * 两个颜色的小球循环旋转，目前只支持代码设置颜色、最大半径、最小半径等属性
 */
public class TheBallLoaddingView extends View {

    //默认小球最大半径
    private final static int DEFAULT_MAX_RADIUS = 12;
    //默认小球颜色
    private int DEFAULT_TWO_BALL_COLOR = Color.parseColor("#ff5073");
    //画笔
    private Paint mPaint;

    private float mCenterX;
    private float mCenterY;

    private float rotateProcess;

    float valueY, radiusR = 12;
    private boolean isRotate;

    public TheBallLoaddingView(Context context) {
        this(context, null);
    }

    public TheBallLoaddingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TheBallLoaddingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
       // DEFAULT_TWO_BALL_COLOR = SkinManager.getInstance().getAdapterColor(R.color.red_b);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setColor(DEFAULT_TWO_BALL_COLOR);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isRotate) {
            canvas.rotate(360 * rotateProcess, mCenterX, mCenterY);
        } else {
            canvas.rotate(0, mCenterX, mCenterY);
        }
        canvas.drawCircle(mCenterX, mCenterY - valueY, radiusR, mPaint);
        canvas.drawCircle(mCenterX, mCenterY + valueY, radiusR, mPaint);

    }

    public void setProcess(float process) {
        this.isRotate = false;
        valueY = process * 15f;
        radiusR = DEFAULT_MAX_RADIUS - (process * 5);
        invalidate();
    }

    /**
     * 旋转动画
     */
    public void handleRotationAnimation(float rotateProcess) {
        this.rotateProcess = rotateProcess;
        this.isRotate = true;
        invalidate();
    }

    public void setIsRotate(boolean isRotate) {
        this.isRotate = isRotate;
        invalidate();
    }

    public void setBallColor(int resouceColor) {
        if (mPaint != null) {
            mPaint.setColor(resouceColor);
        }
        invalidate();
    }
}

