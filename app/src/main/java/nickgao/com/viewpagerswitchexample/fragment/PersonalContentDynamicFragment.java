package nickgao.com.viewpagerswitchexample.fragment;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import nickgao.com.viewpagerswitchexample.Util.DeviceUtils;
import nickgao.com.viewpagerswitchexample.adapter.HomeDynamicAdapter;
import nickgao.com.viewpagerswitchexample.model.DynamicData;
import nickgao.com.viewpagerswitchexample.model.HomeDynamicModel;

public class PersonalContentDynamicFragment extends PersonalContentFragment {

    HomeDynamicAdapter mAdapter = null;
    private List<HomeDynamicModel> models = new ArrayList<>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        sendRequest();
    }

    private void initView() {
        mAdapter = new HomeDynamicAdapter(mActivity, models);
        mListview.setAdapter(mAdapter);
    }

    @Override
    public void subClassLoadMore() {

    }

    @Override
    void sendRequest() {
        new FetchDataTask().execute();
    }


    private class FetchDataTask extends AsyncTask<Void,Void,DynamicData> {
        @Override
        protected DynamicData doInBackground(Void... voids) {
            String data = DeviceUtils.getFromAssets("dynamic_model.json",getContext());
            Gson gson = new Gson();
            return gson.fromJson(data,DynamicData.class);
        }

        @Override
        protected void onPostExecute(DynamicData userHomePage) {
            super.onPostExecute(userHomePage);
            models = userHomePage.content;
            mAdapter.setList(models);

        }
    }

}
