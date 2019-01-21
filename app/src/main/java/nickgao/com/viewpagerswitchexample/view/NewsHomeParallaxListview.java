package nickgao.com.viewpagerswitchexample.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import nickgao.com.viewpagerswitchexample.R;
import nickgao.com.viewpagerswitchexample.Util.DeviceUtils;
import nickgao.com.viewpagerswitchexample.controller.NewsHomeController;


/**
 * 首页新的5.5.8加的下拉刷新交互的listview
 * Created with IntelliJ IDEA. R Date: 13-12-30
 */

public class NewsHomeParallaxListview extends ListView implements AbsListView.OnScrollListener {
    private String TAG = "HomeParallaxListview";
    private float mOldY;
    private int mOrigineHeadViewHeight = 0;
    private int mOrigineLoaddingHeight = 0;
    private int ballAlpaLoaddingHeight = 0;
    private final float mMaxZoom = 1.3f;
    private int mScollUpMinHeight;//回弹回去的最小高度 就要执行正在加载的效果
    private int loaddingViewMaxHeight;//下拉刷新view最大的高度 就可以显示松开即可刷新
    private int screenWidth;
    private int mOrigineLoaddingFinishWidth;//刷新完成后的背景长度
    private int homeStyle;//首页类型
    //刷新的回调
    private OnRefreshListener mOnRefreshListener;
    //动画器
    private ValueAnimator mResizeAnim, mLoaddingResizeAnim;
    private ObjectAnimator rotationAnimator;//滑上来之后 小球自转的 动画 等外面接口刷新完成 就停止该动画
    //外层传进来的view做动画
    private ScrollableLayout scrollableLayout;
    private View mScaleView;
    private RelativeLayout rl_loadding, rl_update;
    private TextView tvLoadding, tvLoaddingFinish;
    private ImageView ivLoaddingBg;
    private TheBallLoaddingView ballLoaddingView;
    private Context context;
    private boolean isUpMove;//是否往上移动

    public NewsHomeParallaxListview(Context context) {
        super(context);
        this.setOnScrollListener(this);
        initWithContext(context);
    }

    public NewsHomeParallaxListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnScrollListener(this);
        initWithContext(context);
    }


    public NewsHomeParallaxListview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOnScrollListener(this);
        initWithContext(context);
    }

    void initWithContext(Context context) {
        this.context = context;
        homeStyle = 3;
        mOrigineLoaddingHeight = DeviceUtils.dip2px(context, 0);
        ballAlpaLoaddingHeight = DeviceUtils.dip2px(context, 5);
        loaddingViewMaxHeight = DeviceUtils.dip2px(context, 40);
        mScollUpMinHeight = DeviceUtils.dip2px(context, 30);
        screenWidth = DeviceUtils.getScreenWidth(context);
        mOrigineLoaddingFinishWidth = DeviceUtils.dip2px(context, 20);
    }


    public void setScaleView(View v) {
        disableOverScroll();
        if (v != null) {
            mScaleView = v;
        }
    }

    @SuppressLint("NewApi")
    private void disableOverScroll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && getOverScrollMode() != OVER_SCROLL_NEVER) {
            setOverScrollMode(OVER_SCROLL_NEVER);
        }
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
            mOrigineHeadViewHeight = mOrigineHeadViewHeight == 0 ? (mScaleView.getHeight()) : mOrigineHeadViewHeight;
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!NewsHomeController.getInstance().isOnRefresh() && !scrollableLayout.isMove()) {
                        ballLoaddingView.setAlpha(0);
                        tvLoadding.setAlpha(0);
                        tvLoadding.setText("下拉即可刷新");
                    }
                    mOldY = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if ((getChildCount() > 0 && getChildAt(0).getTop() == 0) && !NewsHomeController.getInstance().isOnRefresh() && !scrollableLayout.isMove()) {
                        if (mOldY < ev.getY()) {
                            if (rl_update.getHeight() >= loaddingViewMaxHeight) {
                                tvLoadding.setText("松开即可刷新");
                            }
                        } else if (mOldY > ev.getY()) {
                            if (rl_update.getHeight() < loaddingViewMaxHeight) {
                                tvLoadding.setText("下拉即可刷新");
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return super.dispatchTouchEvent(ev);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;

    }

    /**
     * 触摸事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            mOrigineHeadViewHeight = mOrigineHeadViewHeight == 0 ? (mScaleView.getHeight()) : mOrigineHeadViewHeight;
            if (mResizeAnim != null && mResizeAnim.isRunning() && isHeadViewScoll() && !scrollableLayout.isMove()) {
                mResizeAnim.cancel();
                int value = (Integer) mResizeAnim.getAnimatedValue();
                setRequestLayout(value);
            }

            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isUpMove = false;
                    animationDown();
                    mOldY = ev.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    isUpMove = false;
                    if (!NewsHomeController.getInstance().isOnRefresh() && !scrollableLayout.isMove()) {
                        cleanAnimation();
                        if ((getChildCount() > 0 && scrollableLayout.getHelper().isTop())) {
                            if (mOldY < ev.getY()) {
                                isUpMove = false;
                                int differenceY = (int) (Math.abs(mOldY - ev.getY()));
                                int topHeight = differenceY > 2 ? differenceY / 2 : differenceY;
                                if (isHeadViewScoll() && mScaleView.getHeight() < (mOrigineHeadViewHeight * mMaxZoom)) {//小于最大数 往下滑动
                                    setRequestLayout(mScaleView.getHeight() + topHeight);
                                }
                                downAnimation(topHeight);//下拉的效果
                                mOldY = ev.getY();

                            } else if (mOldY > ev.getY() && rl_update.getHeight() > 0) {//有拉开的时候 到这里可以往回滑
                                isUpMove = true;
                                int differenceY = (int) (Math.abs(mOldY - ev.getY()));
                                int topHeight = differenceY > 2 ? differenceY / 2 : differenceY;
                                if (isHeadViewScoll() && mScaleView.getHeight() > mOrigineHeadViewHeight) {
                                    mOldY = ev.getY();
                                    setRequestLayout(mScaleView.getHeight() - topHeight);
                                }
                                if (rl_update.getHeight() > mOrigineLoaddingHeight) {
                                    upAnimation(topHeight);//上拉的效果
                                }
                                mOldY = ev.getY();
                            }
                            return true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isUpMove = false;
                    mOldY = 0;
                    animationUp();
                    break;
            }
            return super.onTouchEvent(ev);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 按下事件
     */
    private void animationDown() {
        if (NewsHomeController.getInstance().isOnRefresh() || scrollableLayout.isMove())
            return;
        cleanAnimation();
        rl_loadding.setVisibility(View.GONE);
        rl_update.setVisibility(View.VISIBLE);
        ballLoaddingView.setAlpha(0);
        tvLoadding.setAlpha(0);
        tvLoadding.setText("下拉即可刷新");
    }

    /**
     * 离开屏幕事件
     */
    private void animationUp() {
        headBgSpringBack();
        middleFaultSpringBack(false, true);
        if (NewsHomeController.getInstance().isOnRefresh() || scrollableLayout.isMove())
            return;
        if (rl_update.getHeight() >= loaddingViewMaxHeight && rl_update.getHeight() != 0) {
            requestScrollableLayoutDisallowInterceptTouchEvent(true);
            NewsHomeController.getInstance().setOnRefresh(true);
            loadOnRefresh();
            if (tvLoadding != null) {
                tvLoadding.setText("正在刷新列表");
            }
        }
    }

    /**
     * 新的首页交互动画效果 下拉的效果
     */
    private void downAnimation(int topHeight) {
        if (NewsHomeController.getInstance().isOnRefresh())
            return;
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
        ViewGroup.LayoutParams layoutParams = rl_update.getLayoutParams();
        layoutParams.height = loaddingOrgHeight;
        rl_update.setLayoutParams(layoutParams);
    }

    /**
     * 上拉的动画效果
     */
    private void upAnimation(int topHeight) {
        if (NewsHomeController.getInstance().isOnRefresh())
            return;
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
     * 头部背景回弹效果
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
     * @param isUpdate 是否要触发首页的刷新操作
     */
    private void middleFaultSpringBack(final boolean isUpdate, final boolean isUseMaxHeight) {
        try {
            if (rl_update != null && (!NewsHomeController.getInstance().isOnRefresh() || isUpdate)) {
                final int loaddingHeight = rl_update.getHeight() + (isUpdate ? 1 : 0);
                final int toScollUpY = loaddingHeight >= (isUseMaxHeight ? loaddingViewMaxHeight : mScollUpMinHeight) ? mScollUpMinHeight : mOrigineLoaddingHeight;
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
                        if (loaddingHeight >= (isUseMaxHeight ? loaddingViewMaxHeight : mScollUpMinHeight)) {
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
                                NewsHomeController.getInstance().setOnRefresh(false);
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
     * 刷新完成的回调通知
     */
    public void setRefreshComplete(String msgHint, boolean isHasData) {
        cleanAnimation();
        ballLoaddingView.setRotation(0f);
        ballLoaddingView.setIsRotate(false);
        if (NewsHomeController.getInstance().isOnRefresh()) {//有自己执行下拉刷新的操作 才要执行 刷新之后的效果
            ballHomingAnimation(msgHint, isHasData);
        }
    }

    /**
     * 清除两个小球的动画
     */
    private void cleanAnimation() {
        ballLoaddingView.clearAnimation();
        if (rotationAnimator != null) {
            rotationAnimator.end();
        }
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
                ballLoaddingView.setAlpha(0.2f);
                tvLoadding.setVisibility(View.VISIBLE);

                if (msgHint.equals(getResources().getString(R.string.not_network)) && !isHasData) {
                    rl_loadding.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = rl_update.getLayoutParams();
                    layoutParams.height = mOrigineLoaddingHeight;
                    rl_update.setLayoutParams(layoutParams);
                    tvLoadding.setAlpha(0);
                    ballLoaddingView.setAlpha(0);
                    NewsHomeController.getInstance().setOnRefresh(false);
                    requestScrollableLayoutDisallowInterceptTouchEvent(false);
                } else {
                    rl_loadding.setVisibility(View.VISIBLE);
                    tvLoaddingFinish.setText(msgHint);
                    handleUpdateFinishAnimation();
                }
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
                NewsHomeController.getInstance().setOnRefresh(false);
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
     * 模拟下拉的动画 点击看到这里和双击首页tab 要下拉的效果
     */
    public void simulationDownAnimation() {
        if (NewsHomeController.getInstance().isOnRefresh())
            return;
        NewsHomeController.getInstance().setOnRefresh(true);
        mOrigineHeadViewHeight = mScaleView.getHeight();
        final ValueAnimator moveUp = ValueAnimator.ofInt(mOrigineLoaddingHeight, mScollUpMinHeight);
        moveUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                rl_update.setVisibility(View.VISIBLE);
                rl_loadding.setVisibility(View.GONE);
                int loaddingOrgHeight = value;
                tvLoadding.setText("正在刷新列表");
                float alp = (float) loaddingOrgHeight / mScollUpMinHeight;
                tvLoadding.setAlpha(alp > 1f ? 1f : alp);
                //小球的透明度
                float ballAlpha = (float) (rl_update.getHeight() - mOrigineLoaddingHeight) / mOrigineLoaddingHeight;
                ballLoaddingView.setAlpha(ballAlpha > 1f ? 1f : ballAlpha);
                //小球的放大缩小 分开的动画
                float process = (float) (rl_update.getHeight() - mOrigineLoaddingHeight) / mScollUpMinHeight;
                ballLoaddingView.setProcess(process > 1 ? 1 : process);
                //小球旋转的动画
                if (rl_update.getHeight() >= mScollUpMinHeight) {//旋转动画开始
                    ballLoaddingView.handleRotationAnimation((float) rl_update.getHeight() / (loaddingViewMaxHeight + mOrigineLoaddingFinishWidth));
                }
                /*if (isHeadViewScoll()) {
                    //刷新头部背景的高度
                    setRequestLayout(mOrigineHeadViewHeight + (value - mOrigineLoaddingHeight) / 2);
                }*/
                //刷新改变高度
                ViewGroup.LayoutParams layoutParams = rl_update.getLayoutParams();
                layoutParams.height = loaddingOrgHeight;
                rl_update.setLayoutParams(layoutParams);
                if (value == mScollUpMinHeight) {
                    moveUp.removeUpdateListener(this);
                }
            }
        });
        moveUp.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        headBgSpringBack();
                        middleFaultSpringBack(true, false);
                    }
                }, 50);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        moveUp.setDuration(50);
        moveUp.start();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public void updateUI() {
    }


    public interface OnRefreshListener {
        void OnRefresh();
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
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
     * 是否让上一层不做事情
     *
     * @param disallowIntercept
     */
    private void requestScrollableLayoutDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (scrollableLayout != null) {
            scrollableLayout.requestScrollableLayoutDisallowInterceptTouchEvent(disallowIntercept);
        }
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
}