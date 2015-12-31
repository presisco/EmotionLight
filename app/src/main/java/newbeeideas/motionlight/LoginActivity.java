package newbeeideas.motionlight;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by presisco on 2015/12/30.
 */
public class LoginActivity extends Activity {
    public static final String TAG = LoginActivity.class.getSimpleName();

    public static final Integer LOGIN_REQUEST = 6666;
    public static final Integer LOGIN_RESULT_FINISHED = 4444;
    private EditText userPhoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        userPhoneNum = (EditText) findViewById(R.id.userPhoneNumEditText);
    }

    public void onApply(View v) {
        String input = userPhoneNum.getText().toString().trim();
        if (input.length() != 11 || input.charAt(0) != '1') {
            Toast.makeText(this, "手机号码不符合格式", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putString(Constants.USER_PHONE_NUMBER, input);
        editor.commit();
        setResult(LOGIN_RESULT_FINISHED, new Intent());
        finish();
    }
}
