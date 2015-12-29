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
            switch (mode) {
                case MODE_SEND_REQUEST:
                    notifyService.pair(mInputText.getText().toString().trim());
                    break;
                case MODE_ACCEPT_REQUEST:
                    notifyService.acceptedRequest();
                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.PAIRED_PHONE_NUMBER, displayContent);
                    editor.commit();
                    break;
            }
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
                findViewById(R.id.displayRow).setEnabled(false);
                mInputText = (EditText) findViewById(R.id.pairDeviceEditText);
                ((Button) findViewById(R.id.pairPositiveBtn)).setText(getResources().getString(R.string.text_apply));
                ((Button) findViewById(R.id.pairPositiveBtn)).setText(getResources().getString(R.string.text_cancel));
                break;
            case MODE_ACCEPT_REQUEST:
                displayContent = intent.getStringExtra(DISPLAY_ITEM);
                findViewById(R.id.inputRow).setEnabled(false);
                ((TextView) findViewById(R.id.pairDeviceTextView)).setText(displayContent);
                break;
            default:
                Log.d(TAG, "onCreate():unmatch mode:" + mode);
        }
    }

    public void onPositive(View v) {
        Intent intent = new Intent(PairActivity.this, NotifyService.class);
        bindService(intent, mNotifyServiceCon, Context.BIND_AUTO_CREATE);
    }

    public void onNegative(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
