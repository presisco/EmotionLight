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
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

/**
 * Created by presisco on 2015/12/28.
 */
public class NotifyService extends Service {
    public static final String TAG = NotifyService.class.getSimpleName();

    private static final Integer ERR_BT_DISABLED = 777;
    private static final Integer ERR_BT_DISCONNECTED = 778;
    private static final Integer ERR_BT_FAILED = 779;
    private static final Integer ERR_NO_NET = 801;

    private static final Integer BT_CONNECTED = 666;

    private static final Integer NEW_USER_MSG = 500;
    private static final Integer VALID_MSG = 501;
    private static final Integer INVALID_MSG = 502;

    private static final String HEAD_LIGHT_CHANGE = "light_change";
    private static final String HEAD_PAIR_REQUEST = "pair_request";
    private final IBinder mBinder = new NotifyServiceBinder();
    private BluetoothSPP mBluetoothHelper;
    private SharedPreferences sharedPreferences;
    private Socket socket;

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

    private void notifyUser(int msg_type) {
        Log.d(TAG, "notify()");
        NotificationManager notificationManager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent intent;

        if (msg_type == ERR_BT_DISABLED) {
            builder.setContentTitle(getResources().getString(R.string.notification_title_enable_bt))
                    .setContentText(getResources().getString(R.string.notification_content_enable_bt));

            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        } else if (msg_type == ERR_BT_FAILED) {
            builder.setContentTitle(getResources().getString(R.string.notification_title_bt_failed))
                    .setContentText(getResources().getString(R.string.notification_content_bt_failed));

            intent = new Intent(this, MainActivity.class);
        } else if (msg_type == ERR_BT_DISCONNECTED) {
            builder.setContentTitle(getResources().getString(R.string.notification_title_bt_disconnected))
                    .setContentText(getResources().getString(R.string.notification_content_bt_disconnected));

            intent = new Intent(this, MainActivity.class);
        } else if (msg_type == ERR_NO_NET) {
            builder.setContentTitle(getResources().getString(R.string.notification_title_no_network))
                    .setContentText(getResources().getString(R.string.notification_content_no_network));

            intent = new Intent(this, MainActivity.class);
        } else if (msg_type == NEW_USER_MSG) {
            builder.setContentTitle(getResources().getString(R.string.notification_title_no_network))
                    .setContentText(getResources().getString(R.string.notification_content_no_network));

            intent = new Intent(this, MainActivity.class);
        } else {
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
        try {
            String cmd = sharedPreferences.getString(Constants.USER_PHONE_NUMBER, "000");
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(cmd);
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void pair(String pair_id) {
        try {
            String cmd = pair_id;
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(cmd);
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void signal(String cmd) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(cmd);
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

    private class ServerListener extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer == VALID_MSG)
                notifyUser(NEW_USER_MSG);
            waitForNext();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                String msg = dis.readUTF();
                switch (msg) {
                    case HEAD_LIGHT_CHANGE:
                        msg = dis.readUTF();
                        if (DefinedKeyword.isValidBTCmd(msg) && checkBluetoothStatus()) {
                            mBluetoothHelper.send(msg, true);
                            return VALID_MSG;
                        } else {
                            return INVALID_MSG;
                        }
                    case HEAD_PAIR_REQUEST:
                        msg = dis.readUTF();
                        return VALID_MSG;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return INVALID_MSG;
        }
    }
}
