package newbeeideas.motionlight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

/**
 * Created by presisco on 2015/12/29.
 */
public class UserPreferenceFragment extends PreferenceFragment {
    private SharedPreferences sharedPreferences;
    private Boolean isLoggedin = true;
    private Boolean isPaired = true;
    private Boolean isAcceptRequest = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.user_preference);
        sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        initUserProfile();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LoginActivity.LOGIN_REQUEST) {

        } else if (requestCode == PairActivity.SEND_REQUEST_CODE) {

        } else if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(BluetoothState.EXTRA_DEVICE_ADDRESS, data.getStringExtra(BluetoothState.EXTRA_DEVICE_ADDRESS));
                editor.commit();
                BluetoothHelper.reconnectDevice(data.getStringExtra(BluetoothState.EXTRA_DEVICE_ADDRESS));
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        String key = preference.getKey();
        if (key == getString(R.string.preference_pair_key)) {
            if (isPaired) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.PAIRED_PHONE_NUMBER, Constants.DEFAULT_PAIRED_PHONE_NUMBER);
                editor.commit();
            } else if (!isAcceptRequest) {
                Intent intent = new Intent(getActivity(), PairActivity.class);
                intent.putExtra(PairActivity.MODE, PairActivity.MODE_SEND_REQUEST);
                startActivityForResult(intent, PairActivity.SEND_REQUEST_CODE);
            } else {
                Intent intent = new Intent(getActivity(), PairActivity.class);
                intent.putExtra(PairActivity.MODE, PairActivity.MODE_SEND_REQUEST);
                intent.putExtra(PairActivity.DISPLAY_ITEM, sharedPreferences.getString(Constants.PAIR_REQUEST_PHONE_NUM, Constants.DEFAULT_PAIR_REQUEST_PHONE_NUM));
                startActivityForResult(intent, PairActivity.SEND_REQUEST_CODE);
            }
        } else if (key == getString(R.string.preference_login_key)) {
            if (isLoggedin) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.USER_PHONE_NUMBER, Constants.DEFAULT_USER_PHONE_NUMBER);
                editor.commit();
            } else {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(intent, LoginActivity.LOGIN_REQUEST);
            }
        } else if (key == getString(R.string.preference_select_bt_device_key)) {
            Intent intent = new Intent(getActivity(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else {
            Log.d("onPreferenceTreeClick()", "unhandled click source:" + key);
        }
        initUserProfile();
        return true;
    }

    public void initUserProfile() {
        PreferenceManager pm = getPreferenceManager();
        if (sharedPreferences.getString(Constants.USER_PHONE_NUMBER, Constants.DEFAULT_USER_PHONE_NUMBER).equals(Constants.DEFAULT_USER_PHONE_NUMBER)) {
            isLoggedin = false;
            isPaired = false;
            pm.findPreference(getString(R.string.preference_login_key)).setTitle(getString(R.string.user_account_login));
            pm.findPreference(getString(R.string.preference_pair_key)).setEnabled(false);
            return;
        } else if (sharedPreferences.getString(Constants.PAIRED_PHONE_NUMBER, Constants.DEFAULT_PAIRED_PHONE_NUMBER).equals(Constants.DEFAULT_PAIRED_PHONE_NUMBER)) {
            isPaired = false;
            pm.findPreference(getString(R.string.preference_pair_key)).setTitle(getString(R.string.pair_account_pair));
        } else if (!sharedPreferences.getString(Constants.PAIR_REQUEST_PHONE_NUM, Constants.DEFAULT_PAIR_REQUEST_PHONE_NUM).equals(Constants.DEFAULT_PAIR_REQUEST_PHONE_NUM)) {
            isAcceptRequest = true;
            pm.findPreference(getString(R.string.preference_pair_key)).setTitle(getString(R.string.pair_account_accept));
        }
    }

}
