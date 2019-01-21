package nickgao.com.viewpagerswitchexample;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import nickgao.com.viewpagerswitchexample.Util.DeviceUtils;
import nickgao.com.viewpagerswitchexample.fragment.PersonalFragmentPagerAdapter;
import nickgao.com.viewpagerswitchexample.model.PersonalTabModel;
import nickgao.com.viewpagerswitchexample.model.Tabs;
import nickgao.com.viewpagerswitchexample.model.UserHomePage;
import nickgao.com.viewpagerswitchexample.view.CircleShapeImageView;
import nickgao.com.viewpagerswitchexample.view.HomeSlidingTabLayout;
import nickgao.com.viewpagerswitchexample.view.HomeSlidingTabStrip;
import nickgao.com.viewpagerswitchexample.view.NewsHomeParallaxListview;
import nickgao.com.viewpagerswitchexample.view.NewsHomeViewPager;
import nickgao.com.viewpagerswitchexample.view.ScrollableLayout;


/**
 * Created by gaoyoujian on 2017/3/7.
 */

public class PersonalFragment extends Fragment {

    private View mRootView;
    HomeSlidingTabLayout news_home_sliding_tab;
    NewsHomeParallaxListview listView;
    PersonalFragmentPagerAdapter homePagerAdapter;
    private NewsHomeViewPager news_home_viewpager;
    private int mCurrentPosition;
    private Activity mActivity;
    private ImageView emptyView;
    private ScrollableLayout news_home_scroll_layout;
    private TextView tvName;
    private RelativeLayout rlRank;
    private TextView tvRank;
    private TextView tvHeadMsg;
    private TextView tvAttention, tvFans;
    private RelativeLayout rlAttentionOrFans;
    private TextView tv_publish_dynamic;
    //透明titleBar
    private RelativeLayout rl_custom_title_bar;
    private TextView custom_tv_title;
    private ImageView iv_custom_iv_left, iv_custom_iv_right;
    private TextView tv_PersonalDescription;
    private Button btn_personal_head_attention;
    private RelativeLayout rl_personal_header_without_info_layout;
    private RelativeLayout ll_personal_header_with_info_layout;
    private LinearLayout ll_header_layout;
    boolean isShowPersonalInfo = false;
    private RelativeLayout personal_header_layout;
    private boolean mIsSelected = false;
    private int preCurrentY = 0;
    private Handler mHandler = new Handler();
    private TextView tvFansCount;
    private TextView tvAttentionCount;
    private CircleShapeImageView imgHead;
    private ImageView mMask;

    // private Button btn_personal_title_attention;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, null);
        return mRootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        sendRequest();
    }


    private void sendRequest() {
       new FetchDataTask().execute();
    }


    private class FetchDataTask extends AsyncTask<Void,Void,UserHomePage> {
        @Override
        protected UserHomePage doInBackground(Void... voids) {
            String data = DeviceUtils.getFromAssets("home.json",getContext());
            Gson gson = new Gson();
            return gson.fromJson(data,UserHomePage.class);
        }

        @Override
        protected void onPostExecute(UserHomePage userHomePage) {
            super.onPostExecute(userHomePage);
            initTabs(userHomePage.tabs);
            personalData(userHomePage);
        }
    }

    private void initView() {
        news_home_viewpager = (NewsHomeViewPager) mRootView.findViewById(R.id.news_home_viewpager);
        personal_header_layout = (RelativeLayout) mRootView.findViewById(R.id.personal_header_layout);
        news_home_sliding_tab = (HomeSlidingTabLayout) mRootView.findViewById(R.id.news_home_sliding_tab);
        news_home_sliding_tab.setCustomTabView(R.layout.layout_home_classify_tab_item, R.id.homeTab);
        news_home_sliding_tab.setIsDrawDiver(true);
        emptyView = (ImageView) mRootView.findViewById(R.id.empty_view);
        imgHead = (CircleShapeImageView) mRootView.findViewById(R.id.ivPersonalHead);
        mMask = (ImageView)mRootView.findViewById(R.id.mask);

        btn_personal_head_attention = (Button) mRootView.findViewById(R.id.btn_personal_head_attention);
        btn_personal_head_attention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_personal_head_attention.isSelected()) {
                    btn_personal_head_attention.setSelected(false);
                } else {
                    btn_personal_head_attention.setSelected(true);
                }
            }
        });
        btn_personal_head_attention.setAlpha(0);
        ll_personal_header_with_info_layout = (RelativeLayout) mRootView.findViewById(R.id.rlHeader);
        rl_personal_header_without_info_layout = (RelativeLayout) mRootView.findViewById(R.id.personal_header_without_info_layout);
        ll_header_layout = (LinearLayout) mRootView.findViewById(R.id.ll_header_layout);
        //透明titleBar
        rl_custom_title_bar = (RelativeLayout) mRootView.findViewById(R.id.rl_custom_title_bar);
        rl_custom_title_bar.setAlpha(0);
        rl_custom_title_bar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        tv_publish_dynamic = (TextView) mRootView.findViewById(R.id.tv_publish_dynamic);
        tv_PersonalDescription = (TextView) mRootView.findViewById(R.id.personal_description);
        tvName = (TextView) mRootView.findViewById(R.id.tvPersonalName);
        rlRank = (RelativeLayout) mRootView.findViewById(R.id.rlRank);
        tvRank = (TextView) mRootView.findViewById(R.id.tvRank);
        custom_tv_title = (TextView) mRootView.findViewById(R.id.custom_tv_title);
        iv_custom_iv_left = (ImageView) mRootView.findViewById(R.id.custom_iv_left);
        iv_custom_iv_right = (ImageView) mRootView.findViewById(R.id.custom_iv_right);
        tvFansCount = (TextView) mRootView.findViewById(R.id.tv_fans_count);
        tvAttentionCount = (TextView) mRootView.findViewById(R.id.tv_att_count);


        news_home_scroll_layout = (ScrollableLayout) mRootView.findViewById(R.id.news_home_scroll_layout);
        news_home_scroll_layout.setOnScrollListener(new ScrollableLayout.OnScrollListener() {
            @Override
            public void onScroll(int currentY, int maxY) {
                int headHeight = 0;
                if (isShowPersonalInfo) {
                    headHeight = mActivity.getResources().getDimensionPixelOffset(R.dimen.personal_header_with_personal_info_height);
                } else {
                    headHeight = mActivity.getResources().getDimensionPixelOffset(R.dimen.personal_header_with_one_line_personal_info_height);
                }
                int titleBarHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.personal_titlebar_height);


                int max = headHeight - titleBarHeight;
                int half = max / 2;
                if (currentY < half) {
                    //在下一半

                    float f1 = (currentY * 1.0f / half);
                    personal_header_layout.setAlpha(1 - f1);

                    rl_custom_title_bar.setAlpha(0);
                    btn_personal_head_attention.setAlpha(0);
                } else {

                    float f2 = (currentY - half) * 1.0f / half;
                    rl_custom_title_bar.setAlpha(f2);
                    btn_personal_head_attention.setAlpha(f2);
                }

            }
        });
    }


    private void showTitleBar(float barAlpha) {
        rl_custom_title_bar.setAlpha(barAlpha);
        //  updateTitleBarButton();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }


    private void updateViewPager(List<PersonalTabModel> tabModels) {
        try {
            if (homePagerAdapter == null) {
                homePagerAdapter = new PersonalFragmentPagerAdapter(getChildFragmentManager(), tabModels);
                news_home_viewpager.setAdapter(homePagerAdapter);
            } else {
                homePagerAdapter.notifyDataSetChanged();
            }

            news_home_sliding_tab.setViewPager(news_home_viewpager);
            news_home_viewpager.setCurrentItem(mCurrentPosition);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    news_home_scroll_layout.getHelper().setCurrentScrollableContainer(homePagerAdapter.getPositionFragment());
//                }
//            }, 500);
            setTabSelect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 选中tab的颜色变化
     */
    private void setTabSelect() {
        try {
            HomeSlidingTabStrip tabStrip = (HomeSlidingTabStrip) news_home_sliding_tab.getChildAt(0);
            if (tabStrip != null) {
                TextView textView = (TextView) tabStrip.getChildAt(mCurrentPosition);
                if (textView != null) {
                    if (textView != null) {
                        textView.setText(Color.RED);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_19));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void personalData(final UserHomePage response) {
        if (!TextUtils.isEmpty(response.screen_name)) {
            tvName.setText(response.screen_name);
        } else {
            tvName.setText("还没设置昵称哦~");
        }
        setRank(response);
        tvFansCount.setText("" + response.fans);
        tvAttentionCount.setText("" + response.follows);
    }

    private void setRank(final UserHomePage response) {
        if (response.userrank != -1 && response.userrank >= 0) {
            if (rlRank.getVisibility() == View.GONE) {
                rlRank.setVisibility(View.VISIBLE);
            }
            tvRank.setText("Nick");
        } else {
            if (rlRank.getVisibility() == View.VISIBLE) {
                rlRank.setVisibility(View.GONE);
            }
        }
    }


    private void initTabs(Tabs[] tabs) {
        List<PersonalTabModel> tabModels = new ArrayList<>();
        for (int i = 0; i < tabs.length; i++) {
            PersonalTabModel model = new PersonalTabModel();
            model.id = i;
            model.index = i;
            model.name = tabs[i].name;
            model.url = tabs[i].url;
            model.type = tabs[i].type;
            tabModels.add(model);
        }
        updateViewPager(tabModels);

    }


}
