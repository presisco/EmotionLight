package newbeeideas.motionlight;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by presisco on 2015/12/28.
 */
public class DefinedKeyword {
    public static final String ERR_NOT_PRESET = "####";
    public static final String LIGHT_CLOSE = "light_close";
    public static final String LIGHT_RED = "light_red";
    public static final String LIGHT_GREEN = "light_green";
    public static final String LIGHT_BLUE = "light_blue";
    private static final String BT_CMD_RED = "1";
    private static final String BT_CMD_GREEN = "2";
    private static final String BT_CMD_BLUE = "3";
    private static final String BT_CMD_CLOSE = "4";
    public static String KEYWORD_RED="我很烦";
    public static String KEYWORD_BLUE="我很抑郁";
    public static String KEYWORD_GREEN="我很高兴";
    public static String KEYWORD_CLOSE="关闭";

    public static String getBTCmdFromVoice(String voice_input) {
        String cmd="4";
        if(voice_input.equals(KEYWORD_RED)){
            cmd=BT_CMD_RED;
        }else if(voice_input.equals(KEYWORD_GREEN)){
            cmd=BT_CMD_GREEN;
        }else if(voice_input.equals(KEYWORD_BLUE)){
            cmd=BT_CMD_BLUE;
        }else if(voice_input.equals(KEYWORD_CLOSE)){
            cmd=BT_CMD_CLOSE;
        }else{
            Log.d("DefinedKeyword","voice_input doesnt match any of the preset");
            cmd = ERR_NOT_PRESET;
        }
        return cmd;
    }

    public static String getBTCmdFromStatus(String status) {
        String cmd = "4";
        if (status.equals(LIGHT_RED)) {
            cmd = BT_CMD_RED;
        } else if (status.equals(LIGHT_GREEN)) {
            cmd = BT_CMD_GREEN;
        } else if (status.equals(LIGHT_BLUE)) {
            cmd = BT_CMD_BLUE;
        } else if (status.equals(LIGHT_CLOSE)) {
            cmd = BT_CMD_CLOSE;
        } else {
            Log.d("DefinedKeyword", "voice_input doesnt match any of the preset");
            cmd = ERR_NOT_PRESET;
        }
        return cmd;
    }

    public static String getStatusFromVoice(String voice_input) {
        String light_status;
        if (voice_input.equals(KEYWORD_RED)) {
            light_status = LIGHT_RED;
        } else if (voice_input.equals(KEYWORD_GREEN)) {
            light_status = LIGHT_GREEN;
        } else if (voice_input.equals(KEYWORD_BLUE)) {
            light_status = LIGHT_BLUE;
        } else if (voice_input.equals(KEYWORD_CLOSE)) {
            light_status = LIGHT_CLOSE;
        } else {
            Log.d("DefinedKeyword", "voice_input doesnt match any of the preset");
            light_status = ERR_NOT_PRESET;
        }
        return light_status;
    }

    public static Boolean isValidBTCmd(String cmd) {
        if (cmd.equals(BT_CMD_RED)) {
            return true;
        } else if (cmd.equals(BT_CMD_GREEN)) {
            return true;
        } else if (cmd.equals(BT_CMD_BLUE)) {
            return true;
        } else if (cmd.equals(BT_CMD_CLOSE)) {
            return true;
        } else {
            return false;
        }
    }

    public static void initFromPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        KEYWORD_RED = sharedPreferences.getString(Constants.USER_DEFINED_KEYWORD_RED, context.getResources().getString(R.string.keyword_red_default));
        KEYWORD_GREEN = sharedPreferences.getString(Constants.USER_DEFINED_KEYWORD_GREEN, context.getResources().getString(R.string.keyword_green_default));
        KEYWORD_BLUE = sharedPreferences.getString(Constants.USER_DEFINED_KEYWORD_BLUE, context.getResources().getString(R.string.keyword_blue_default));
        KEYWORD_CLOSE = sharedPreferences.getString(Constants.USER_DEFINED_KEYWORD_CLOSE, context.getResources().getString(R.string.keyword_close_default));
    }

    public static String btCmd2Status(String cmd) {
        String result;
        switch (cmd) {
            case BT_CMD_RED:
                result = LIGHT_RED;
                break;
            case BT_CMD_GREEN:
                result = LIGHT_GREEN;
                break;
            case BT_CMD_BLUE:
                result = LIGHT_BLUE;
                break;
            case BT_CMD_CLOSE:
                result = LIGHT_CLOSE;
                break;
            default:
                result = LIGHT_CLOSE;
                Log.d("btCmd2Status()", "cmd unrecognizable:" + cmd);
                break;
        }
        return result;
    }
}