package newbeeideas.motionlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

    NotifyService notify_service;
    private InitListener mInitListener;
    //    private BluetoothSPP mBluetoothHelper;
    private SharedPreferences sharedPreferences;
    private String remoteCmd;
    private ImageView mBackground;
    private ServiceConnection mNotifyServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NotifyServiceBinder binder = (NotifyServiceBinder) service;
            notify_service = binder.getNotifyService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onStart(){
        super.onStart();
//        super.onStart();
//        if(mBluetoothHelper.isBluetoothEnabled()){
//            if(!mBluetoothHelper.isServiceAvailable()) {
//                mBluetoothHelper.setupService();
//                mBluetoothHelper.startService(BluetoothState.DEVICE_OTHER);
//            }
//            if(sharedPreferences.getString(BluetoothState.EXTRA_DEVICE_ADDRESS," ").equals(" ")){
//                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
//                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
//            }else{
//                mBluetoothHelper.connect(sharedPreferences.getString(BluetoothState.EXTRA_DEVICE_ADDRESS," "));
//            }
//        }else{
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
//        }
        BluetoothHelper.initService(this, sharedPreferences.getString(BluetoothState.EXTRA_DEVICE_ADDRESS, " "));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBackground = (ImageView) findViewById(R.id.mainBkGdImageView);

        sharedPreferences=getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        updateBackground(sharedPreferences.getString(Constants.CURRENT_LIGHT_COLOR, DefinedKeyword.LIGHT_CLOSE));

//        mBluetoothHelper=new BluetoothSPP(this);
//        mBluetoothHelper.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
//            public void onDataReceived(byte[] data, String message) {
//                Toast.makeText(MainActivity.this,"bluetooth data received",Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        mBluetoothHelper.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
//            public void onDeviceDisconnected() {
//                Toast.makeText(MainActivity.this, "Bluetooth device disconnected", Toast.LENGTH_SHORT).show();
//            }
//
//            public void onDeviceConnectionFailed() {
//                Toast.makeText(MainActivity.this, "Bluetooth device failed", Toast.LENGTH_SHORT).show();
//            }
//
//            public void onDeviceConnected(String name, String address) {
//                Toast.makeText(MainActivity.this, "Bluetooth device connected", Toast.LENGTH_SHORT).show();
//            }
//        });

        mInitListener=new InitListener() {
            @Override
            public void onInit(int i) {

            }
        };
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=567e7f91," + SpeechConstant.FORCE_LOGIN + "=true");

        Intent intent = new Intent(this, NotifyService.class);
        startService(intent);
        if (notify_service == null)
            bindService(intent, mNotifyServiceCon, Context.BIND_AUTO_CREATE);
    }

    public void onRecognizeComplete(String result){
        result=JsonParser.parseIatResult(result);
        Log.d("Raw result", result);
        result=StringCooker.deleteChSymbols(result);
        Log.d("Cooked result", result);
        if (result.length() == 1)
            return;
        String cmd = DefinedKeyword.getBTCmdFromVoice(result);
        if(!cmd.equals(DefinedKeyword.ERR_NOT_PRESET)) {
            updateLightStatusAndBackground(DefinedKeyword.getStatusFromVoice(result));
            remoteCmd = cmd;
            notify_service.signal(cmd);
        } else {
            Toast.makeText(this, "不是可识别的关键词", Toast.LENGTH_SHORT).show();
        }
    }

    public void onVoiceCmd(View v){
        if (sharedPreferences.getString(Constants.PAIRED_PHONE_NUMBER, Constants.DEFAULT_PAIRED_PHONE_NUMBER).equals(Constants.DEFAULT_PAIRED_PHONE_NUMBER)) {
            AlertDialog.Builder fetchDialogBuilder = new AlertDialog.Builder(this);

            fetchDialogBuilder.setMessage(R.string.text_warning_no_pair)
                    .setPositiveButton(R.string.text_apply, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, PairActivity.class);
                            intent.putExtra(PairActivity.MODE, PairActivity.MODE_SEND_REQUEST);
                            startActivityForResult(intent, PairActivity.SEND_REQUEST_CODE);
                            dialog.dismiss();
                        }
                    }).setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            fetchDialogBuilder.show();
            return;
        }

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
        intent.putExtra(ManualSwitchActivity.DISPLAY_MODE, ManualSwitchActivity.DISPLAY_MODE_DIALOG);
        startActivityForResult(intent, ManualSwitchActivity.REQUEST_CODE);
    }

    public void onPreferenceBtn(View v) {
        Intent intent = new Intent(MainActivity.this, UserPreferenceActivity.class);
        startActivity(intent);
    }

    public void updateBackground(String light_status) {
        switch (light_status) {
            case DefinedKeyword.LIGHT_CLOSE:
                mBackground.setImageResource(R.mipmap.main_background_default);
                break;
            case DefinedKeyword.LIGHT_RED:
                mBackground.setImageResource(R.mipmap.main_background_red);
                break;
            case DefinedKeyword.LIGHT_GREEN:
                mBackground.setImageResource(R.mipmap.main_background_green);
                break;
            case DefinedKeyword.LIGHT_BLUE:
                mBackground.setImageResource(R.mipmap.main_background_blue);
                break;
        }
    }

    public void updateLightStatusAndBackground(String light_status) {
        updateBackground(light_status);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.CURRENT_LIGHT_COLOR, light_status);
        editor.commit();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK) {
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString(BluetoothState.EXTRA_DEVICE_ADDRESS,data.getStringExtra(BluetoothState.EXTRA_DEVICE_ADDRESS));
                editor.commit();
                //mBluetoothHelper.connect(data);
            }
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                //mBluetoothHelper.setupService();
                //mBluetoothHelper.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }else if(requestCode==ManualSwitchActivity.REQUEST_CODE){
            if(resultCode==ManualSwitchActivity.RESULT_OK){
                String light_status = data.getStringExtra(ManualSwitchActivity.SELECTED_MODE);
                if (!light_status.equals(DefinedKeyword.ERR_NOT_PRESET)) {
                    updateLightStatusAndBackground(light_status);
                    //mBluetoothHelper.send(DefinedKeyword.getBTCmdFromStatus(light_status), true);
                    //notify_service.sendBTCmd(DefinedKeyword.getBTCmdFromStatus(light_status));
                    BluetoothHelper.sendBTCmd(DefinedKeyword.getBTCmdFromStatus(light_status));
                }
            }
        } else if (requestCode == PairActivity.SEND_REQUEST_CODE) {
            if (resultCode == PairActivity.RESULT_OK) {

            }
        }
    }
}
