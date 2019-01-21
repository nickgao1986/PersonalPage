package nickgao.com.viewpagerswitchexample.view;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import nickgao.com.viewpagerswitchexample.R;
import nickgao.com.viewpagerswitchexample.Util.DeviceUtils;


/**
 * Created by wuminjian
 */
public class ScrollableLayout extends RelativeLayout {
    private Context context;
    //变量值
    private float mDownX;
    private float mDownY;
    private float mLastY;
    private int minY = 0;
    private int maxY = 0;
    private int shiftX = 0;
    private int shiftY = 0;
    private int mHeadHeight;
    private int mExpandHeight;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private DIRECTION mDirection;// 方向
    private int mCurY;
    private int mLastScrollerY;
    private boolean needCheckUpdown;
    private boolean updown;
    private boolean mDisallowIntercept;
    private boolean isClickHead;
    private boolean isClickHeadExpand;

    private View mHeadView;
    private NewsHomeViewPager childViewPager;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    //传进来做动画的view
    private View mScaleView;
    private RelativeLayout rl_loadding, rl_update;
    private TextView tvLoadding, tvLoaddingFinish;
    private ImageView ivLoaddingBg;
    private TheBallLoaddingView ballLoaddingView;

    //view做动画需要用到的变量值
    private final float mMaxZoom = 1.3f;
    private int homeStyle;
    private int mOrigineLoaddingHeight = 0;
    private int loaddingViewMaxHeight;//下拉刷新view最大的高度 就可以显示松开即可刷新
    private int mScollUpMinHeight;//回弹回去的最小高度 就要执行正在加载的效果
    private int screenWidth;
    private int mOrigineLoaddingFinishWidth;//刷新完成后的背景长度
    private int mOrigineHeadViewHeight = 0;
    private int ballAlpaLoaddingHeight = 0;//小球的初始化高度
    //动画器
    private ValueAnimator mResizeAnim, mLoaddingResizeAnim;
    private ObjectAnimator rotationAnimator;//滑上来之后 小球自转的 动画 等外面接口刷新完成 就停止该动画
    //刷新回调
    private NewsHomeParallaxListview.OnRefreshListener mOnRefreshListener;
    private boolean isFirst = true;

    /**
     * 滑动方向 *
     */
    enum DIRECTION {
        UP,// 向上划
        DOWN// 向下划
    }

    public interface OnScrollListener {

        void onScroll(int currentY, int maxY);

    }

    private OnScrollListener onScrollListener;

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    private ScrollableHelper mHelper;

    public ScrollableHelper getHelper() {
        return mHelper;
    }

    public ScrollableLayout(Context context) {
        super(context);
        init(context);
    }

    public ScrollableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScrollableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        mHelper = new ScrollableHelper();
        mScroller = new Scroller(context);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        mOrigineLoaddingHeight = DeviceUtils.dip2px(context, 0);
        loaddingViewMaxHeight = DeviceUtils.dip2px(context, 40);
        mScollUpMinHeight = DeviceUtils.dip2px(context, 30);
        screenWidth = DeviceUtils.getScreenWidth(context);
        mOrigineLoaddingFinishWidth = DeviceUtils.dip2px(context, 20);
        ballAlpaLoaddingHeight = DeviceUtils.dip2px(context, 5);
    }

    /**
     * 测量布局
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mHeadView = getChildAt(0);
        measureChildWithMargins(mHeadView, widthMeasureSpec, 0, MeasureSpec.UNSPECIFIED, 0);
        maxY = mHeadView.getMeasuredHeight() - context.getResources().getDimensionPixelSize(R.dimen.personal_titlebar_height);
        mHeadHeight = mHeadView.getMeasuredHeight();
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) + maxY, MeasureSpec.EXACTLY));
    }

    /**
     * 页面绘制结束的回调
     */
    @Override
    protected void onFinishInflate() {
        if (mHeadView != null && !mHeadView.isClickable()) {
            mHeadView.setClickable(true);
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt != null && childAt instanceof ViewPager) {
                childViewPager = (NewsHomeViewPager) childAt;
            }
        }
        super.onFinishInflate();
    }



    private void clearUIStatus() {
        if (rl_update == null || rl_loadding == null)
            return;
        rl_loadding.setVisibility(View.GONE);
        rl_update.setVisibility(View.GONE);
    }

    /**
     * 是否卡住状态
     *
     * @return
     */
    public boolean isSticked() {
        return mCurY == maxY;
    }

    /**
     * 滑动的时候要不要拉开间距
     *
     * @return
     */
    public boolean isMove() {
        return mCurY != 0;
    }

    /**
     * 触摸的位置是否在头部
     *
     * @return
     */
    private boolean isLocationHead() {
        return mDownY <= maxY;
    }

    /**
     * 头部view是不是要跟着滚动，判断分类卡住的时候双击tab的时候头部view不需要滚动了
     *
     * @return
     */
    private boolean isHeadViewScoll() {
        return true;
    }

    /**
     * 填充该控件的高度
     */
    private void setRequestLayout(int value) {
        if (mScaleView == null)
            return;
        ViewGroup.LayoutParams lp = mScaleView.getLayoutParams();
        lp.height = value;
        mScaleView.setLayoutParams(lp);
    }

    public int getCurY() {
        return mCurY;
    }

    public void requestScrollableLayoutDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        mDisallowIntercept = disallowIntercept;
    }

    /**
     * 分发事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            mOrigineHeadViewHeight = mOrigineHeadViewHeight == 0 ? (mScaleView != null ? mScaleView.getHeight() : mOrigineHeadViewHeight) : mOrigineHeadViewHeight;
            if (mResizeAnim != null && mResizeAnim.isRunning() && isHeadViewScoll() && !isMove()) {
                mResizeAnim.cancel();
                int value = (Integer) mResizeAnim.getAnimatedValue();
                setRequestLayout(value);
            }
            float currentX = ev.getX();
            float currentY = ev.getY();
            float deltaY;
            shiftX = (int) Math.abs(currentX - mDownX);
            shiftY = (int) Math.abs(currentY - mDownY);
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDisallowIntercept = false;
                    needCheckUpdown = true;
                    updown = true;
                    mDownX = currentX;
                    mDownY = currentY;
                    mLastY = currentY;
                    checkIsClickHead((int) currentY, mHeadHeight, getScrollY());
                    checkIsClickHeadExpand((int) currentY, mHeadHeight, getScrollY());
                    initOrResetVelocityTracker();
                    mVelocityTracker.addMovement(ev);
                    mScroller.forceFinished(true);
                    animationDown();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mDisallowIntercept) {
                        break;
                    }
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                    deltaY = mLastY - currentY;
                    if (needCheckUpdown) {
                        if (shiftX > mTouchSlop && shiftX > shiftY) {
                            needCheckUpdown = false;
                            updown = false;
                        } else if (shiftY > mTouchSlop && shiftY > shiftX) {
                            needCheckUpdown = false;
                            updown = true;
                        }
                    }
                    animationMove(ev, currentY);
                    childViewPager.setScrollble(true);
                    if (updown && shiftY > mTouchSlop && shiftY > shiftX &&
                            (!isSticked() || mHelper.isTop() || isClickHeadExpand)) {
                        if (childViewPager != null) {
                            childViewPager.requestDisallowInterceptTouchEvent(true);
                        }
                        scrollBy(0, (int) (deltaY + 0.5));
                    }
                    mLastY = currentY;
                    break;
                case MotionEvent.ACTION_UP:
                    isFirst = true;
                    animationUp();
                    if (updown && shiftY > shiftX && shiftY > mTouchSlop) {
                        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        float yVelocity = -mVelocityTracker.getYVelocity();
                        boolean dislowChild = false;
                        if (Math.abs(yVelocity) > mMinimumVelocity) {
                            mDirection = yVelocity > 0 ? DIRECTION.UP : DIRECTION.DOWN;
                            if ((mDirection == DIRECTION.UP && isSticked()) || (!isSticked() && getScrollY() == 0 && mDirection == DIRECTION.DOWN)) {
                                dislowChild = true;
                            } else {
                                mScroller.fling(0, getScrollY(), 0, (int) yVelocity, 0, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                                mScroller.computeScrollOffset();
                                mLastScrollerY = getScrollY();
                                invalidate();
                            }
                        }
                        if (!dislowChild && (isClickHead || !isSticked())) {
                            int action = ev.getAction();
                            ev.setAction(MotionEvent.ACTION_CANCEL);
                            boolean dispathResult = super.dispatchTouchEvent(ev);
                            ev.setAction(action);
                            return dispathResult;
                        }
                    }
                    mLastY = 0;
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    /**
     * 按下去的动画
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void animationDown() {
        if (isMove())
            return;
        cleanAnimation();
        if (rl_loadding == null)
            return;
        rl_loadding.setVisibility(View.GONE);
        rl_update.setVisibility(View.VISIBLE);
        ballLoaddingView.setAlpha(0);
        tvLoadding.setAlpha(0);
        tvLoadding.setText("下拉即可刷新");
    }

    /**
     * 移动的效果
     */
    private void animationMove(MotionEvent ev, float currentY) {
        if (isMove())
            return;
        if (mLastY > 0 && isFirst) {
            isFirst = false;
            mLastY = ev.getY();
        }
        if (mLastY < ev.getY() && mHelper.isTop() && isLocationHead()) {//向下
            cleanAnimation();
            if (rl_update.getHeight() >= loaddingViewMaxHeight) {
                tvLoadding.setText("松开即可刷新");
            }
            int differenceY = (int) (Math.abs(mLastY - currentY));
            int topHeight = differenceY > 2 ? differenceY / 2 : differenceY;
            if (isHeadViewScoll() && mScaleView.getHeight() < (mOrigineHeadViewHeight * mMaxZoom)) {//小于最大数 往下滑动
                setRequestLayout(mScaleView.getHeight() + topHeight);
            }
            downAnimation(topHeight);//下拉的效果
        } else if (mLastY > ev.getY() && mHelper.isTop() && isLocationHead()) {//向上滑动
            if (rl_update.getHeight() < loaddingViewMaxHeight) {
                tvLoadding.setText("下拉即可刷新");
            }
            int differenceY = (int) (Math.abs(mLastY - currentY));
            int topHeight = differenceY > 2 ? differenceY / 2 : differenceY;
            if (isHeadViewScoll() && mScaleView.getHeight() > mOrigineHeadViewHeight) {
                mLastY = ev.getY();
                setRequestLayout(mScaleView.getHeight() - topHeight);
            }
            if (rl_update.getHeight() > mOrigineLoaddingHeight) {
                upAnimation(topHeight);//上拉的效果
            }
        }
    }

    /**
     * 手指离开屏幕的方法
     */
    private void animationUp() {
        headBgSpringBack();//头部回弹上去的效果
        middleFaultSpringBack(false);

        if (rl_update != null && rl_update.getHeight() >= loaddingViewMaxHeight && rl_update.getHeight() != 0) {
            requestScrollableLayoutDisallowInterceptTouchEvent(true);

            loadOnRefresh();
            if (tvLoadding != null) {
                tvLoadding.setText("正在刷新列表");
            }
        }
    }

    /**
     * 头部背景回弹的效果
     */
    private void headBgSpringBack() {
        try {
            if (mOrigineHeadViewHeight != 0 && isHeadViewScoll()) {
                mResizeAnim = ValueAnimator.ofInt(mScaleView.getLayoutParams().height, mOrigineHeadViewHeight);
                mResizeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        try {
                            int value = (Integer) valueAnimator.getAnimatedValue();
                            setRequestLayout(value);
                            if (value == mOrigineHeadViewHeight) {
                                mResizeAnim.removeUpdateListener(this);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                mResizeAnim.setDuration(300);
                mResizeAnim.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * listview中间断层回弹效果
     *
     * @param isUpdate
     */
    private void middleFaultSpringBack(final boolean isUpdate) {
        try {
            if (rl_update != null && (isUpdate)) {
                final int loaddingHeight = rl_update.getHeight() + (isUpdate ? 1 : 0);
                final int toScollUpY = loaddingHeight >= loaddingViewMaxHeight ? mScollUpMinHeight : mOrigineLoaddingHeight;
                mLoaddingResizeAnim = ValueAnimator.ofInt(loaddingHeight, toScollUpY);
                mLoaddingResizeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        try {
                            int value = (Integer) valueAnimator.getAnimatedValue();
                            rl_update.setVisibility(View.VISIBLE);
                            if (loaddingHeight < loaddingViewMaxHeight) {
                                float alp = (float) value / loaddingViewMaxHeight;
                                if (value <= mOrigineLoaddingHeight) {
                                    alp = 0;
                                }
                                tvLoadding.setAlpha(alp > 1f ? 1f : alp);
                                ballLoaddingView.setAlpha(alp > 1f ? 1f : alp);
                            }
                            //回弹的值
                            ViewGroup.LayoutParams layoutParams = rl_update.getLayoutParams();
                            layoutParams.height = value;
                            rl_update.setLayoutParams(layoutParams);
                            //模拟下拉效果需要的值
                            if (isUpdate && value <= mScollUpMinHeight) {
                                tvLoadding.setText("正在刷新列表");
                            }
                            if (loaddingHeight < loaddingViewMaxHeight) {
                                //小球的放大缩小 分开的动画
                                float process = (float) (value - (loaddingHeight >= (loaddingViewMaxHeight + mOrigineLoaddingFinishWidth) ? mScollUpMinHeight : mOrigineLoaddingHeight)) / mScollUpMinHeight;
                                ballLoaddingView.setProcess(process > 1 ? 1 : process);
                            }
                            //小球旋转的动画
                            if (value >= mScollUpMinHeight) {//旋转动画开始
                                ballLoaddingView.handleRotationAnimation((float) value / (loaddingViewMaxHeight + mOrigineLoaddingFinishWidth));
                            }
                            if (value == toScollUpY) {
                                mLoaddingResizeAnim.removeUpdateListener(this);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                mLoaddingResizeAnim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (loaddingHeight >= loaddingViewMaxHeight) {
                            if (isUpdate) {
                                loadOnRefresh();
                            }
                            cleanAnimation();
                            LinearInterpolator lin = new LinearInterpolator();
                            rotationAnimator = ObjectAnimator.ofFloat(ballLoaddingView, "rotation", 359F, 0F);//360度旋转
                            rotationAnimator.setRepeatCount(Animation.INFINITE);
                            rotationAnimator.setInterpolator(lin);
                            rotationAnimator.setDuration(1000);
                            rotationAnimator.start();
                        } else {
                            if (isUpdate) {
                                requestScrollableLayoutDisallowInterceptTouchEvent(false);
                            }
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                mLoaddingResizeAnim.setDuration(200);
                mLoaddingResizeAnim.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 手机拉下的状态效果
     *
     * @param topHeight
     */
    private void downAnimation(int topHeight) {

        rl_update.setVisibility(View.VISIBLE);
        rl_loadding.setVisibility(View.GONE);
        int loaddingOrgHeight = rl_update.getHeight() + topHeight;
        float alp = (float) loaddingOrgHeight / loaddingViewMaxHeight;
        tvLoadding.setAlpha(alp > 1f ? 1f : alp);
        //小球的透明度
        float ballAlpha = (float) (rl_update.getHeight() - ballAlpaLoaddingHeight) / ballAlpaLoaddingHeight;
        ballLoaddingView.setAlpha(ballAlpha > 1f ? 1f : ballAlpha);
        //小球的放大缩小 分开的动画
        float process = (float) (rl_update.getHeight() - ballAlpaLoaddingHeight) / mScollUpMinHeight;
        ballLoaddingView.setProcess(process > 1 ? 1 : process);
        //小球旋转的动画
        if (rl_update.getHeight() > mScollUpMinHeight) {//旋转动画开始
            ballLoaddingView.handleRotationAnimation((float) rl_update.getHeight() / (loaddingViewMaxHeight + mOrigineLoaddingFinishWidth));
        }
        //刷新改变高度
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rl_update.getLayoutParams();
        layoutParams.height = loaddingOrgHeight;
        rl_update.setLayoutParams(layoutParams);
    }

    /**
     * 手机往上的效果
     *
     * @param topHeight
     */
    private void upAnimation(int topHeight) {

        rl_update.setVisibility(View.VISIBLE);
        rl_loadding.setVisibility(View.GONE);
        int loaddingOrgHeight = rl_update.getHeight() - topHeight;
        float alp = (float) loaddingOrgHeight / mScollUpMinHeight;
        if (loaddingOrgHeight <= mOrigineLoaddingHeight) {
            alp = 0;
        }
        tvLoadding.setAlpha(alp > 1f ? 1f : alp);
        //小球的透明度
        float ballAlpha = (float) (rl_update.getHeight() - mOrigineLoaddingHeight) / mOrigineLoaddingHeight;
        if (loaddingOrgHeight <= mOrigineLoaddingHeight) {
            ballAlpha = 0;
        }
        ballLoaddingView.setAlpha(ballAlpha > 1f ? 1f : ballAlpha < 0 ? 0 : ballAlpha);
        //小球的放大缩小 分开的动画
        float process = (float) (rl_update.getHeight() - mOrigineLoaddingHeight) / mScollUpMinHeight;
        ballLoaddingView.setProcess(process > 1 ? 1 : process);
        //小球旋转的动画
        if (rl_update.getHeight() >= mScollUpMinHeight) {//旋转动画开始
            ballLoaddingView.handleRotationAnimation((float) rl_update.getHeight() / (loaddingViewMaxHeight + mOrigineLoaddingFinishWidth));
        }
        //刷新改变高度
        ViewGroup.LayoutParams layoutParams = rl_update.getLayoutParams();
        layoutParams.height = loaddingOrgHeight < mOrigineLoaddingHeight ? mOrigineLoaddingHeight : loaddingOrgHeight;
        rl_update.setLayoutParams(layoutParams);
    }

    /**
     * 回调外面listview刷新
     */
    private void loadOnRefresh() {
        if (mOnRefreshListener != null) {
            mOnRefreshListener.OnRefresh();
        }
    }

    /**
     * 刷新完成的回调通知 listview接口请求完成传回来
     */
    public void setRefreshComplete(String msgHint, boolean isHasData) {
        cleanAnimation();
        ballLoaddingView.setRotation(0f);
        ballLoaddingView.setIsRotate(false);

    }

    /**
     * 合并回去的动画
     *
     * @param msgHint
     * @param isHasData
     */
    private void ballHomingAnimation(final String msgHint, final boolean isHasData) {
        final ValueAnimator mResizeAnim = ValueAnimator.ofFloat(1f, 0f);
        mResizeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                ballLoaddingView.setProcess(value);
                if (value == 0) {
                    mResizeAnim.removeUpdateListener(this);
                }
            }
        });
        mResizeAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
                ballLoaddingView.setAlpha(0.2f);
                tvLoadding.setVisibility(View.VISIBLE);

                if (msgHint.equals(context.getResources().getString(R.string.not_network)) && !isHasData) {
                    rl_loadding.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = rl_update.getLayoutParams();
                    layoutParams.height = mOrigineLoaddingHeight;
                    rl_update.setLayoutParams(layoutParams);
                    tvLoadding.setAlpha(0);
                    ballLoaddingView.setAlpha(0);
                    requestScrollableLayoutDisallowInterceptTouchEvent(false);
                } else {
                    rl_loadding.setVisibility(View.VISIBLE);
                    tvLoaddingFinish.setText(msgHint);
                    handleUpdateFinishAnimation();
                }
//                    }
//                }, 10);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mResizeAnim.setDuration(100);
        mResizeAnim.start();
    }

    /**
     * 刷新完成背景扩散的动画
     */
    private void handleUpdateFinishAnimation() {
        tvLoadding.setAlpha(0);
        ballLoaddingView.setAlpha(0);
        ViewGroup.LayoutParams layoutParams = ivLoaddingBg.getLayoutParams();
        layoutParams.width = mOrigineLoaddingFinishWidth;
        ivLoaddingBg.setLayoutParams(layoutParams);

        ValueAnimator updateFinish = ValueAnimator.ofInt(mOrigineLoaddingFinishWidth, screenWidth);
        updateFinish.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = ivLoaddingBg.getLayoutParams();
                layoutParams.width = value;
                ivLoaddingBg.setLayoutParams(layoutParams);
            }
        });
        updateFinish.setDuration(320);
        updateFinish.start();
        updateFinish.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup.LayoutParams layoutParams = rl_update.getLayoutParams();
                layoutParams.height = mOrigineLoaddingHeight;
                rl_update.setLayoutParams(layoutParams);
                tvLoadding.setAlpha(0);
                ballLoaddingView.setAlpha(0);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moveUpAnimation();//两秒之后  红色背景移动上去
                    }
                }, 500);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * 刷新之后 扩散动画之后 要进行 上移和隐藏渐变的动画
     */
    private void moveUpAnimation() {
        ValueAnimator moveUp = ValueAnimator.ofInt(0, mScollUpMinHeight);
        moveUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rl_loadding.getLayoutParams();
                layoutParams.topMargin = -value;
                rl_loadding.setLayoutParams(layoutParams);
            }
        });
        moveUp.setDuration(400);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(rl_loadding, "alpha", 1f, 0f);
        anim2.setDuration(280);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveUp, anim2);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rl_loadding.getLayoutParams();
                layoutParams.topMargin = 0;
                rl_loadding.setLayoutParams(layoutParams);
                rl_loadding.setAlpha(1);
                rl_loadding.setVisibility(View.GONE);
                requestScrollableLayoutDisallowInterceptTouchEvent(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * 清除两个小球的动画
     */
    private void cleanAnimation() {
        if (ballLoaddingView != null) {
            ballLoaddingView.clearAnimation();
        }
        if (rotationAnimator != null) {
            rotationAnimator.end();
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private int getScrollerVelocity(int distance, int duration) {
        if (mScroller == null) {
            return 0;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return (int) mScroller.getCurrVelocity();
        } else {
            return distance / duration;
        }
    }

    public void setScrollBy() {
        scrollTo(0, 0);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            final int currY = mScroller.getCurrY();
            if (mDirection == DIRECTION.UP) {
                // 手势向上划
                if (isSticked()) {//导航栏贴边了
                    int distance = mScroller.getFinalY() - currY;
                    int duration = calcDuration(mScroller.getDuration(), mScroller.timePassed());
                    mHelper.smoothScrollBy(getScrollerVelocity(distance, duration), distance, duration);
                    mScroller.forceFinished(true);
                    return;
                } else {
                    scrollTo(0, currY);
                }
            } else {
                // 手势向下划
                if (mHelper.isTop() || isClickHeadExpand) {
                    int deltaY = (currY - mLastScrollerY);
                    int toY = getScrollY() + deltaY;
                    scrollTo(0, toY);
                    if (mCurY <= minY) {
                        mScroller.forceFinished(true);
                        return;
                    }
                }
                invalidate();
            }
            mLastScrollerY = currY;
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        int scrollY = getScrollY();
        int toY = scrollY + y;
        if (toY >= maxY) {
            toY = maxY;
        } else if (toY <= minY) {
            toY = minY;
        }
        y = toY - scrollY;
        super.scrollBy(x, y);
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y >= maxY) {
            y = maxY;
        } else if (y <= minY) {
            y = minY;
        }
        mCurY = y;
        if (onScrollListener != null) {
            onScrollListener.onScroll(y, maxY);
        }
        super.scrollTo(x, y);
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void checkIsClickHead(int downY, int headHeight, int scrollY) {
        isClickHead = downY + scrollY <= headHeight;
    }

    private void checkIsClickHeadExpand(int downY, int headHeight, int scrollY) {
        if (mExpandHeight <= 0) {
            isClickHeadExpand = false;
        }
        isClickHeadExpand = downY + scrollY <= headHeight + mExpandHeight;
    }

    private int calcDuration(int duration, int timepass) {
        return duration - timepass;
    }

    public boolean isHeadViewScroll() {
        if (isSticked()) {//导航栏贴边了，头部不能跟着滑动
            return false;
        } else {
            return true;
        }
    }

    public void setOnRefreshListener(NewsHomeParallaxListview.OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }
}
