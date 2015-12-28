package newbeeideas.motionlight;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
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
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class MainActivity extends Activity {

    private InitListener mInitListener;
    private BluetoothSPP mBluetoothHelper;

    @Override
    public void onStart(){
        super.onStart();
        if(mBluetoothHelper.isBluetoothEnabled()){
            mBluetoothHelper.setupService();
            mBluetoothHelper.startService(BluetoothState.DEVICE_OTHER);
        }else{
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothHelper=new BluetoothSPP(this);

        mInitListener=new InitListener() {
            @Override
            public void onInit(int i) {

            }
        };
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=567e7f91," + SpeechConstant.FORCE_LOGIN + "=true");
    }

    public void onRecognizeComplete(String result){
        Toast.makeText(this,"Voice result:"+JsonParser.parseIatResult(result),Toast.LENGTH_LONG).show();
        mBluetoothHelper.send(DefinedKeyword.getBTCmd(result),true);
    }

    public void onVoiceCmd(){
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
    }

    public void onSwitchCmd(){

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                mBluetoothHelper.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                mBluetoothHelper.setupService();
                mBluetoothHelper.startService(BluetoothState.DEVICE_ANDROID);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
