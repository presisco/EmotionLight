package newbeeideas.motionlight;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

/**
 * Created by presisco on 2016/1/1.
 */
public class BluetoothHelper {
    public static final String TAG = BluetoothState.class.getSimpleName();
    public static final Integer ERR_NO_DEV_ADDR = 33;
    public static final Integer ERR_BT_DISABLED = 34;
    public static final Integer NO_ERR = 30;
    public static BluetoothSPP mBluetoothSPP;
    public static String dev_addr;

    public static void initService(Context context) {
        mBluetoothSPP = new BluetoothSPP(context);
        mBluetoothSPP.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.d(TAG, "Received message:" + message);
            }
        });
        SharedPreferences sp = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        dev_addr = sp.getString(BluetoothState.EXTRA_DEVICE_ADDRESS, " ");

        mBluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
            }

            public void onDeviceConnectionFailed() {
            }

            public void onDeviceConnected(String name, String address) {
                Log.d(TAG, "Bluetooth device connected");
            }
        });
    }

    public static Integer checkAvailability() {
        if (!mBluetoothSPP.isBluetoothEnabled()) {
            return ERR_BT_DISABLED;
        } else if (dev_addr.equals(" ")) {
            return ERR_NO_DEV_ADDR;
        }
        return NO_ERR;
    }

    public static Integer startService(String addr) {
        dev_addr = addr;
        return startService();
    }

    public static Integer startService() {
        if (!mBluetoothSPP.isServiceAvailable()) {
            mBluetoothSPP.setupService();
            mBluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            mBluetoothSPP.connect(dev_addr);
            return NO_ERR;
        } else {
            return ERR_BT_DISABLED;
        }
    }

    public static void reconnectDevice(String addr) {
        dev_addr = addr;
        mBluetoothSPP.disconnect();
        mBluetoothSPP.connect(dev_addr);
    }

    public static BluetoothSPP getBluetoothSPP() {
        return mBluetoothSPP;
    }

    public static void sendBTCmd(String cmd) {
        mBluetoothSPP.send(cmd, true);
    }
}
