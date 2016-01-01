package newbeeideas.motionlight;

import android.content.Context;
import android.util.Log;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * Created by presisco on 2016/1/1.
 */
public class BluetoothHelper {
    public static final String TAG = BluetoothState.class.getSimpleName();
    public static BluetoothSPP mBluetoothSPP;

    public static void initService(Context context, String device_addr) {
        mBluetoothSPP = new BluetoothSPP(context);
        mBluetoothSPP.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.d(TAG, "Received message:" + message);
            }
        });
        mBluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
            }

            public void onDeviceConnectionFailed() {
            }

            public void onDeviceConnected(String name, String address) {
                Log.d(TAG, "Bluetooth device connected");
            }
        });
        mBluetoothSPP.setupService();
        mBluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
        mBluetoothSPP.connect(device_addr);
    }

    public static void sendBTCmd(String cmd) {
        mBluetoothSPP.send(cmd, true);
    }
}
