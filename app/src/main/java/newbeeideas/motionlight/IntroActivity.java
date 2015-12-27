package newbeeideas.motionlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by presisco on 2015/12/27.
 */
public class IntroActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
