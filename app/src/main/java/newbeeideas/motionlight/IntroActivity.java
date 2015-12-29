package newbeeideas.motionlight;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import java.util.Map;
import java.util.Set;

/**
 * Created by presisco on 2015/12/27.
 */
public class IntroActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TelephonyManager mTelephonyMgr=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String tel=mTelephonyMgr.getLine1Number().substring(3);
        Log.d("IntroActivity", "phone numbers:" + tel);
        SharedPreferences sharedPreferences= getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        if(!sharedPreferences.getString(Constants.USER_PHONE_NUMBER,"0").equals(tel)){
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString(Constants.USER_PHONE_NUMBER,tel);
            editor.commit();
        }

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        }.execute();


    }
}
