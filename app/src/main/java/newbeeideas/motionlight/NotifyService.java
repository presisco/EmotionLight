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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * Created by presisco on 2015/12/28.
 */
public class NotifyService extends Service {
    public static final String TAG = NotifyService.class.getSimpleName();
    private static final Boolean ENABLE_NETWORK = true;

    private static final String ERR_BT_DISABLED = "err_bt_disabled";
    private static final String ERR_BT_DISCONNECTED = "err_bt_disconnected";
    private static final String ERR_BT_FAILED = "err_bt_failed";
    private static final String ERR_NO_NET = "err_no_net";

    private static final String BT_CONNECTED = "bt_connected";

    private static final String HEAD_SOCKET_CLT2SRV = "head_socket_clt2srv";
    private static final String HEAD_SOCKET_SRV2CLT = "head_socket_srv2clt";
    private static final String HEAD_LIGHT_CHANGE = "light_change";
    private static final String HEAD_PAIR_REQUEST = "pair_request";
    private static final String HEAD_PAIR_REQUEST_ACCEPTED = "pair_request_accepted";
    private static final String HEAD_UNACCEPTABLE = "####";
    private static final String HEAD_LOGIN = "head_login";
    private static final String SIGNAL_REQUEST_ACCEPTED = "request_accepted";

    private static final String EMPTY_STATUS = "empty_status";

    private final IBinder mBinder = new NotifyServiceBinder();
    //private BluetoothSPP mBluetoothHelper;
    private SharedPreferences sharedPreferences;
    private Socket outputSocket;
    private Socket inputSocket;

    private String comming_pair_num;

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        initNetwork();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //waitForNext();
//        mBluetoothHelper = new BluetoothSPP(this);
//        mBluetoothHelper.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
//            public void onDataReceived(byte[] data, String message) {
//                Log.d(TAG, "Received message:" + message);
//            }
//        });
//        mBluetoothHelper.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
//            public void onDeviceDisconnected() {
//                notifyUser(ERR_BT_DISCONNECTED);
//            }
//
//            public void onDeviceConnectionFailed() {
//                notifyUser(ERR_BT_FAILED);
//            }
//
//            public void onDeviceConnected(String name, String address) {
//                Log.d(TAG, "Bluetooth device connected");
//            }
//        });
//        checkBluetoothStatus();
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
        builder.setSmallIcon(R.mipmap.ic_launcher);
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
                builder.setContentTitle(getResources().getString(R.string.notification_title_new_user_msg))
                        .setContentText(getResources().getString(R.string.notification_content_new_user_msg));

                intent = new Intent(this, MainActivity.class);
                break;
            case HEAD_PAIR_REQUEST:
                builder.setContentTitle(getResources().getString(R.string.notification_title_new_pair_request))
                        .setContentText(getResources().getString(R.string.notification_content_new_pair_request));

                intent = new Intent(this, PairActivity.class);
                intent.putExtra(PairActivity.MODE, PairActivity.MODE_ACCEPT_REQUEST);
                intent.putExtra(PairActivity.DISPLAY_ITEM, comming_pair_num);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.PAIR_REQUEST_PHONE_NUM, comming_pair_num);
                editor.commit();
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
        new InitNetwork().execute();
    }

    public void waitForNext() {
        new ServerListener().execute();
    }

//    public Boolean checkBluetoothStatus() {
//        if (mBluetoothHelper.isBluetoothEnabled()) {
//            if (mBluetoothHelper.isServiceAvailable()) {
//                mBluetoothHelper.setupService();
//                mBluetoothHelper.startService(BluetoothState.DEVICE_OTHER);
//                if (sharedPreferences.getString(BluetoothState.EXTRA_DEVICE_ADDRESS, " ").equals(" ")) {
//                    return false;
//                } else {
//                    mBluetoothHelper.connect(sharedPreferences.getString(BluetoothState.EXTRA_DEVICE_ADDRESS, " "));
//                    return true;
//                }
//            }
//
//        } else {
//            notifyUser(ERR_BT_DISABLED);
//            return false;
//        }
//        return false;
//    }

    public void login() {

        new PostMsg().execute(HEAD_LOGIN, sharedPreferences.getString(Constants.USER_PHONE_NUMBER, Constants.DEFAULT_USER_PHONE_NUMBER));
    }

    public void initListener() {
        new ServerListener().execute(getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getString(Constants.USER_PHONE_NUMBER, Constants.DEFAULT_USER_PHONE_NUMBER));
    }

    public void pair(String pair_id) {
        new PostMsg().execute(HEAD_PAIR_REQUEST, pair_id);
    }

    public void signal(String cmd) {
        new PostMsg().executeOnExecutor(Executors.newCachedThreadPool(), HEAD_LIGHT_CHANGE, cmd);
    }

    public void acceptedRequest() {
        new PostMsg().execute(HEAD_PAIR_REQUEST_ACCEPTED);
    }

    public void sendBTCmd(String cmd) {
        //mBluetoothHelper.send(cmd,true);
    }

    public class NotifyServiceBinder extends Binder {
        public NotifyService getNotifyService() {
            return NotifyService.this;
        }
    }

    private class ServerListener extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String mode) {
            super.onPostExecute(mode);
            if (mode != EMPTY_STATUS) {
                notifyUser(mode);
                waitForNext();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String mode = EMPTY_STATUS;
            if (ENABLE_NETWORK) {
                try {
                    if (params.length == 0) {
                        String msg = bufferedReader.readLine();
                        mode = msg;
                        Log.d(TAG, "listener received head:" + msg);
                        switch (msg) {
                            case HEAD_LIGHT_CHANGE:
                                msg = bufferedReader.readLine();
                                Log.d(TAG, "listener received content:" + msg);
                                if (DefinedKeyword.isValidBTCmd(msg)) {
                                    //mBluetoothHelper.send(msg, true);
                                    BluetoothHelper.sendBTCmd(msg);
                                }
                                break;
                            case HEAD_PAIR_REQUEST:
                                comming_pair_num = bufferedReader.readLine();
                                break;
                            case HEAD_PAIR_REQUEST_ACCEPTED:
                                break;
                        }
                    } else {
                        mode = HEAD_SOCKET_SRV2CLT;
                        Log.d(TAG, "setting up listener...");
                        String socket_host_addr = getResources().getString(R.string.socket_host_addr);
                        int socket_host_port = Integer.parseInt(getResources().getString(R.string.socket_srv2clt_port));
                        inputSocket = new Socket(socket_host_addr, socket_host_port);
                        bufferedReader = new BufferedReader(new InputStreamReader(inputSocket.getInputStream()));
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(inputSocket.getOutputStream()));
                        pw.println(params[0]);
                        pw.flush();
                        Log.d(TAG, "listener established");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return mode;
        }
    }

    private class PostMsg extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == EMPTY_STATUS || s == HEAD_UNACCEPTABLE)
                notifyUser(s);
            if (s == HEAD_LOGIN)
                initListener();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "PostMsg.doInBackground():HEAD=" + params[0]);
            String result = EMPTY_STATUS;
            if (ENABLE_NETWORK) {
                String mode = params[0];
                result = mode;
                printWriter.println(params[0]);
                switch (mode) {
                    case HEAD_LOGIN:
                    case HEAD_PAIR_REQUEST:
                    case HEAD_LIGHT_CHANGE:
                        printWriter.println(params[1]);
                        break;
                    case HEAD_PAIR_REQUEST_ACCEPTED:
                        break;
                    default:
                        result = HEAD_UNACCEPTABLE;
                        break;
                }
                printWriter.flush();
            }
            return result;
        }
    }

    private class InitNetwork extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            Log.d(TAG, "Setting up connection...");
            String socket_host_addr = getResources().getString(R.string.socket_host_addr);
            int socket_host_port = Integer.parseInt(getResources().getString(R.string.socket_clt2srv_port));
            try {
                outputSocket = new Socket(socket_host_addr, socket_host_port);
                printWriter = new PrintWriter(new OutputStreamWriter(outputSocket.getOutputStream()));
                login();
                Log.d(TAG, "Connection established");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }
}
