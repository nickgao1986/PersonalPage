package nickgao.com.viewpagerswitchexample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class PersonalActivity extends FragmentActivity {

    public static void startActivity(Context context) {
        Intent intent = new Intent();
        intent.setClass(context,PersonalActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


}
