package newbeeideas.motionlight;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.SpeechRecognizerAidl;

public class MainActivity extends Activity {

    private InitListener mInitListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInitListener=new InitListener() {
            @Override
            public void onInit(int i) {

            }
        };
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=567e7f91,"+SpeechConstant.FORCE_LOGIN+"=true");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RecognizerDialog iatDialog=new RecognizerDialog(MainActivity.this,mInitListener);
                iatDialog.setListener(new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        onRecognizeComplete(recognizerResult.getResultString());
                    }

                    @Override
                    public void onError(SpeechError speechError) {

                    }
                });
                iatDialog.show();
//                SpeechRecognizer mlat=SpeechRecognizer.createRecognizer(MainActivity.this,null);
//
//                mlat.setParameter(SpeechConstant.DOMAIN,"iat");
//                mlat.setParameter(SpeechConstant.LANGUAGE,"zh_cn");
//                mlat.setParameter(SpeechConstant.ACCENT,"mandarin");
//                mlat.startListening(mRecoListener);

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    public void onRecognizeComplete(String result){
        Toast.makeText(this,"Voice result:"+JsonParser.parseIatResult(result),Toast.LENGTH_LONG).show();
    }
}
