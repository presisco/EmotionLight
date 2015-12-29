package newbeeideas.motionlight;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * Created by presisco on 2015/12/28.
 */
public class NotifyService extends Service {
    public static final String TAG = NotifyService.class.getSimpleName();

    private static final String ERR_BT_DISABLED = "err_bt_disabled";
    private static final String ERR_BT_DISCONNECTED = "err_bt_disconnected";
    private static final String ERR_BT_FAILED = "err_bt_failed";
    private static final String ERR_NO_NET = "err_no_net";

    private static final String BT_CONNECTED = "bt_connected";

    private static final String HEAD_LIGHT_CHANGE = "light_change";
    private static final String HEAD_PAIR_REQUEST = "pair_request";
    private static final String HEAD_PAIR_REQUEST_ACCEPTED = "pair_request_accepted";
    private static final String HEAD_UNACCEPTABLE = "####";
    private static final String HEAD_LOGIN = "head_login";
    private static final String SIGNAL_REQUEST_ACCEPTED = "request_accepted";

    private final IBinder mBinder = new NotifyServiceBinder();
    private BluetoothSPP mBluetoothHelper;
    private SharedPreferences sharedPreferences;
    private Socket socket;

    private String comming_pair_num;

    @Override
    public void onCreate() {
        super.onCreate();

        mBluetoothHelper = new BluetoothSPP(this);
        mBluetoothHelper.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.d(TAG, "Received message:" + message);
            }
        });
        mBluetoothHelper.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                notifyUser(ERR_BT_DISCONNECTED);
            }

            public void onDeviceConnectionFailed() {
                notifyUser(ERR_BT_FAILED);
            }

            public void onDeviceConnected(String name, String address) {
                Log.d(TAG, "Bluetooth device connected");
            }
        });

        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

        initNetwork();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        checkBluetoothStatus();

        waitForNext();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void notifyUser(String msg_type) {
        Log.d(TAG, "notify()");
        NotificationManager notificationManager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent intent;

        switch (msg_type) {
            case ERR_BT_DISABLED:
                builder.setContentTitle(getResources().getString(R.string.notification_title_enable_bt))
                        .setContentText(getResources().getString(R.string.notification_content_enable_bt));

                intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                break;
            case ERR_BT_DISCONNECTED:
                builder.setContentTitle(getResources().getString(R.string.notification_title_bt_failed))
                        .setContentText(getResources().getString(R.string.notification_content_bt_failed));

                intent = new Intent(this, MainActivity.class);
                break;
            case ERR_BT_FAILED:
                builder.setContentTitle(getResources().getString(R.string.notification_title_bt_disconnected))
                        .setContentText(getResources().getString(R.string.notification_content_bt_disconnected));

                intent = new Intent(this, MainActivity.class);
                break;
            case ERR_NO_NET:
                builder.setContentTitle(getResources().getString(R.string.notification_title_no_network))
                        .setContentText(getResources().getString(R.string.notification_content_no_network));

                intent = new Intent(this, MainActivity.class);
                break;
            case HEAD_LIGHT_CHANGE:
                builder.setContentTitle(getResources().getString(R.string.notification_title_no_network))
                        .setContentText(getResources().getString(R.string.notification_content_no_network));

                intent = new Intent(this, MainActivity.class);
                break;
            case HEAD_PAIR_REQUEST:
                builder.setContentTitle(getResources().getString(R.string.notification_title_new_pair_request))
                        .setContentText(getResources().getString(R.string.notification_content_new_pair_request));

                intent = new Intent(this, PairActivity.class);
                intent.putExtra(PairActivity.MODE, PairActivity.MODE_ACCEPT_REQUEST);
                intent.putExtra(PairActivity.DISPLAY_ITEM, comming_pair_num);
                break;
            case HEAD_PAIR_REQUEST_ACCEPTED:
                builder.setContentTitle(getResources().getString(R.string.notification_title_new_pair_request))
                        .setContentText(getResources().getString(R.string.notification_content_new_pair_request));

                intent = new Intent(this, MainActivity.class);
            default:
                Log.d(TAG, "notifyUser():unhandled msg_type:" + msg_type);
                return;
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        notificationManager.notify(Constants.NOTIFY_ENABLE_BT_TAG, Constants.NOTIFT_ENABLE_BT_ID, builder.build());
        Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        long[] pattern = {200, 200, 200, 200};
        vibrator.vibrate(pattern, -1);
    }

    public void initNetwork() {
        String socket_host_addr = getResources().getString(R.string.socket_host_addr);
        int socket_host_port = Integer.parseInt(getResources().getString(R.string.socket_host_port));
        try {
            socket = new Socket(socket_host_addr, socket_host_port);
            login();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitForNext() {
        new ServerListener().execute();
    }

    public Boolean checkBluetoothStatus() {
        if (mBluetoothHelper.isBluetoothEnabled()) {
            if (mBluetoothHelper.isServiceAvailable()) {
                mBluetoothHelper.setupService();
                mBluetoothHelper.startService(BluetoothState.DEVICE_OTHER);
                if (sharedPreferences.getString(BluetoothState.EXTRA_DEVICE_ADDRESS, " ").equals(" ")) {
                    return false;
                } else {
                    mBluetoothHelper.connect(sharedPreferences.getString(BluetoothState.EXTRA_DEVICE_ADDRESS, " "));
                    return true;
                }
            }
        } else {
            notifyUser(ERR_BT_DISABLED);
            return false;
        }
        return false;
    }

    public void login() {
        new PostMsg().execute(HEAD_LOGIN);
    }

    public void pair(String pair_id) {
        new PostMsg().execute(HEAD_PAIR_REQUEST, pair_id);
    }

    public void signal(String cmd) {
        new PostMsg().execute(HEAD_LIGHT_CHANGE, cmd);
    }

    public void acceptedRequest() {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(HEAD_PAIR_REQUEST);
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class NotifyServiceBinder extends Binder {
        public NotifyService getNotifyService() {
            return NotifyService.this;
        }
    }

    private class ServerListener extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPostExecute(String mode) {
            super.onPostExecute(mode);
            notifyUser(mode);
            waitForNext();
        }

        @Override
        protected String doInBackground(Void... params) {
            String mode = HEAD_UNACCEPTABLE;
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                String msg = dis.readUTF();
                mode = msg;
                switch (msg) {
                    case HEAD_LIGHT_CHANGE:
                        msg = dis.readUTF();
                        if (DefinedKeyword.isValidBTCmd(msg) && checkBluetoothStatus()) {
                            mBluetoothHelper.send(msg, true);
                        }
                        break;
                    case HEAD_PAIR_REQUEST:
                        comming_pair_num = dis.readUTF();
                        break;
                    case HEAD_PAIR_REQUEST_ACCEPTED:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return mode;
        }
    }

    private class PostMsg extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == HEAD_UNACCEPTABLE)
                notifyUser(s);
        }

        @Override
        protected String doInBackground(String... params) {
            String result = HEAD_UNACCEPTABLE;
            try {
                String mode = params[0];
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(mode);
                switch (mode) {
                    case HEAD_LOGIN:
                        dos.writeUTF(sharedPreferences.getString(Constants.USER_PHONE_NUMBER, " "));
                        break;
                    case HEAD_PAIR_REQUEST:
                        dos.writeUTF(params[1]);
                        break;
                    case HEAD_LIGHT_CHANGE:
                        dos.writeUTF(params[1]);
                        break;
                    default:
                        result = HEAD_UNACCEPTABLE;
                        break;
                }
                dos.flush();
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }

}
