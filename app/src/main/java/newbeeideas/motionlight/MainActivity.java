package newbeeideas.motionlight;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
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
import app.akexorcist.bluetotohspp.library.DeviceList;

import newbeeideas.motionlight.NotifyService.*;

public class MainActivity extends Activity {

    private InitListener mInitListener;
    private BluetoothSPP mBluetoothHelper;
    private SharedPreferences sharedPreferences;

    private String remoteCmd;

    private ServiceConnection mNotifyServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NotifyServiceBinder binder = (NotifyServiceBinder) service;
            NotifyService notifyService = binder.getNotifyService();
            notifyService.signal(remoteCmd);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onStart(){
        super.onStart();
        if(mBluetoothHelper.isBluetoothEnabled()){
            if(!mBluetoothHelper.isServiceAvailable()) {
                mBluetoothHelper.setupService();
                mBluetoothHelper.startService(BluetoothState.DEVICE_OTHER);
            }
            if(sharedPreferences.getString(BluetoothState.EXTRA_DEVICE_ADDRESS," ").equals(" ")){
                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            }else{
                mBluetoothHelper.connect(sharedPreferences.getString(BluetoothState.EXTRA_DEVICE_ADDRESS," "));
            }
        }else{
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences=getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

        mBluetoothHelper=new BluetoothSPP(this);
        mBluetoothHelper.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(MainActivity.this,"bluetooth data received",Toast.LENGTH_SHORT).show();
            }
        });

        mBluetoothHelper.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                Toast.makeText(MainActivity.this, "Bluetooth device disconnected", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(MainActivity.this, "Bluetooth device failed", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnected(String name, String address) {
                Toast.makeText(MainActivity.this, "Bluetooth device connected", Toast.LENGTH_SHORT).show();
            }
        });

        mInitListener=new InitListener() {
            @Override
            public void onInit(int i) {

            }
        };
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=567e7f91," + SpeechConstant.FORCE_LOGIN + "=true");

        Intent intent = new Intent(this, NotifyService.class);
        startService(intent);
    }

    public void onRecognizeComplete(String result){
        result=JsonParser.parseIatResult(result);
        Log.d("Raw result",result);
        result=StringCooker.deleteChSymbols(result);
        Log.d("Cooked result",result);
        String cmd=DefinedKeyword.getBTCmd(result);
        if(!cmd.equals(DefinedKeyword.ERR_NOT_PRESET)) {
            mBluetoothHelper.send(cmd, true);
            remoteCmd = cmd;
            Intent intent = new Intent(this, NotifyService.class);
            bindService(intent, mNotifyServiceCon, Context.BIND_AUTO_CREATE);
        }
    }

    public void onVoiceCmd(View v){
        RecognizerDialog iatDialog=new RecognizerDialog(MainActivity.this,mInitListener);
        iatDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                onRecognizeComplete(recognizerResult.getResultString());
            }

            @Override
            public void onError(SpeechError speechError) {
                Toast.makeText(MainActivity.this,
                                speechError.getErrorDescription(),
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });
        iatDialog.show();
    }

    public void onSwitchCmd(View v){
        Intent intent=new Intent(MainActivity.this,ManualSwitchActivity.class);
        intent.putExtra(ManualSwitchActivity.DISPLAY_MODE,ManualSwitchActivity.DISPLAY_MODE_DIALOG);
        startActivityForResult(intent,ManualSwitchActivity.REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK) {
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString(BluetoothState.EXTRA_DEVICE_ADDRESS,data.getStringExtra(BluetoothState.EXTRA_DEVICE_ADDRESS));
                editor.commit();
                mBluetoothHelper.connect(data);
            }
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                mBluetoothHelper.setupService();
                mBluetoothHelper.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }else if(requestCode==ManualSwitchActivity.REQUEST_CODE){
            if(resultCode==ManualSwitchActivity.RESULT_OK){
                String cmd=data.getStringExtra(ManualSwitchActivity.SELECTED_MODE);
                if(!cmd.equals(DefinedKeyword.ERR_NOT_PRESET)) {
                    mBluetoothHelper.send(cmd, true);
                }
            }
        }
    }
}
