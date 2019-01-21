package nickgao.com.viewpagerswitchexample.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by gaoyoujian on 2017/4/21.
 */

public class NewsHomeViewPager extends ViewPager {
    private boolean scrollble = false;
    private int lastX, lastY;
    private OnCallBackListener listener;
    private int mTouchSlop;

    public NewsHomeViewPager(Context context) {
        super(context);
    }

    public NewsHomeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        final ViewConfiguration configuration = ViewConfiguration.get(context);

        mTouchSlop = configuration.getScaledPagingTouchSlop();
    }

    public void setScrollble(boolean scrollble) {
        this.scrollble = scrollble;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) ev.getX();
                lastY = (int) ev.getY();
                break;
        }
        if (!scrollble) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                int mCurX = (int) ev.getX();
                int mCurY = (int) ev.getY();
                final float xDiff = Math.abs(mCurX - lastX);
                final float yDiff = Math.abs(mCurY - lastY);
                if (xDiff > mTouchSlop && xDiff > yDiff) {
                    if ((mCurX - lastX) > 300 && getCurrentItem() == 0) {
                        if (listener != null) {
                            listener.OnCallBack(0);
                        }
                    }
                }
                lastX = 0;
                lastY = 0;
                break;
        }
        if (!scrollble) {
            return false;
        }
        return super.onTouchEvent(ev);
    }

    public void setOnCallBackLisenter(OnCallBackListener listener) {
        this.listener = listener;
    }
}
