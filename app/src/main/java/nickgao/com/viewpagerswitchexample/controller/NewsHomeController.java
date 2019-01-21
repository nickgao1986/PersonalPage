package nickgao.com.viewpagerswitchexample.controller;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by gaoyoujian on 2017/3/7.
 */

public class NewsHomeController {

    private NewsHomeController() {
    }


    public static NewsHomeController getInstance() {
        return new NewsHomeController();
    }

    private final String TAG = "NewsHomeController";
    private boolean mOnRefresh = false;//是否在刷新 外层view和里层共用一个变量值 为了不会产生差异
    private int currentScrollY;//滚动位置


    public String getFromAssets(String fileName, final Context context){
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * 首页动画效果
     *
     * @return
     */
    public boolean isOnRefresh() {
        return mOnRefresh;
    }

    public void setOnRefresh(boolean mOnRefresh) {
        this.mOnRefresh = mOnRefresh;
    }


}
