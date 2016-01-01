package newbeeideas.motionlight;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by presisco on 2015/12/29.
 */
public class PairActivity extends Activity {
    public static final String TAG = PairActivity.class.getSimpleName();

    public static final String MODE = "mode";
    public static final String MODE_SEND_REQUEST = "mode_send_request";
    public static final String MODE_ACCEPT_REQUEST = "mode_accept_request";
    public static final Integer SEND_REQUEST_CODE = 10;
    public static final Integer ACCEPT_REQUEST_CODE = 11;
    public static final String DISPLAY_ITEM = "display_item";
    public static final Integer RESULT_OK = 100;
    public static final Integer RESULT_CANCELED = 101;

    private EditText mInputText;
    private String displayContent;
    private String mode;

    private ServiceConnection mNotifyServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NotifyService.NotifyServiceBinder binder = (NotifyService.NotifyServiceBinder) service;
            NotifyService notifyService = binder.getNotifyService();
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (mode) {
                case MODE_SEND_REQUEST:
                    notifyService.pair(mInputText.getText().toString().trim());
                    editor.putString(Constants.PAIRED_PHONE_NUMBER, mInputText.getText().toString().trim());
                    editor.commit();
                    PairActivity.this.finish();
                    break;
                case MODE_ACCEPT_REQUEST:
                    notifyService.acceptedRequest();
                    editor.putString(Constants.PAIRED_PHONE_NUMBER, displayContent);
                    editor.commit();
                    PairActivity.this.finish();
                    break;
            }
            PairActivity.this.unbindService(mNotifyServiceCon);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pair_activity);

        Intent intent = getIntent();
        mode = intent.getStringExtra(MODE);
        switch (mode) {
            case MODE_SEND_REQUEST:
                findViewById(R.id.displayRow).setVisibility(View.INVISIBLE);
                mInputText = (EditText) findViewById(R.id.pairDeviceEditText);
                ((Button) findViewById(R.id.pairPositiveBtn)).setText(getResources().getString(R.string.text_apply));
                ((Button) findViewById(R.id.pairNegativeBtn)).setText(getResources().getString(R.string.text_cancel));
                break;
            case MODE_ACCEPT_REQUEST:
                displayContent = intent.getStringExtra(DISPLAY_ITEM);
                findViewById(R.id.inputRow).setVisibility(View.INVISIBLE);
                ((TextView) findViewById(R.id.pairDeviceTextView)).setText(displayContent);
                break;
            default:
                Log.d(TAG, "onCreate():unmatch mode:" + mode);
        }
    }

    public void onPositive(View v) {
        Intent intent = new Intent(PairActivity.this, NotifyService.class);
        bindService(intent, mNotifyServiceCon, Context.BIND_AUTO_CREATE);
        SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(Constants.PAIR_REQUEST_PHONE_NUM, Constants.DEFAULT_PAIR_REQUEST_PHONE_NUM);
        editor.commit();
        setResult(RESULT_OK);
    }

    public void onNegative(View v) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(Constants.PAIR_REQUEST_PHONE_NUM, Constants.DEFAULT_PAIR_REQUEST_PHONE_NUM);
        editor.commit();
        setResult(RESULT_CANCELED);
        finish();
    }
}
