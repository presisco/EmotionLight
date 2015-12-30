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

        SharedPreferences sharedPreferences= getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (!sharedPreferences.getString(Constants.USER_PHONE_NUMBER, "0").equals("0")) {
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString(Constants.USER_PHONE_NUMBER, "12345678901");
            editor.commit();
        }

        DefinedKeyword.initFromPreference(this);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    Thread.sleep(5000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        }.execute();


    }
}
