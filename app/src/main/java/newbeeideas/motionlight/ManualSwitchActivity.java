package newbeeideas.motionlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by presisco on 2015/12/28.
 */
public class ManualSwitchActivity extends Activity {
    public static final String DISPLAY_MODE="display_mode";
    public static final String DISPLAY_MODE_DIALOG="dialog";
    public static final String DISPLAY_MODE_FULLSCREEN="fullscreen";
    public static final String SELECTED_MODE="selected_mode";
    public static final Integer RESULT_OK=10;
    public static final Integer REQUEST_CODE=8888;

    private String selected_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent=getIntent();
        if(intent.getStringExtra(DISPLAY_MODE).equals(DISPLAY_MODE_DIALOG)){
            setContentView(R.layout.light_switch_activity_dialog);
        }else{
            setContentView(R.layout.light_switch_activity_fullscreen);
        }
    }

    public void onSelectedRed(View v){
        selected_mode =DefinedKeyword.getBTCmd(DefinedKeyword.KEYWORD_RED);
        returnResult();
    }

    public void onSelectedGreen(View v){
        selected_mode =DefinedKeyword.getBTCmd(DefinedKeyword.KEYWORD_GREEN);
        returnResult();
    }

    public void onSelectedBlue(View v){
        selected_mode =DefinedKeyword.getBTCmd(DefinedKeyword.KEYWORD_BLUE);
        returnResult();
    }

    public void onSelectedClose(View v){
        selected_mode =DefinedKeyword.getBTCmd(DefinedKeyword.KEYWORD_CLOSE);
        returnResult();
    }

    private void returnResult(){
        Intent intent=new Intent();
        intent.putExtra(SELECTED_MODE, selected_mode);
        this.setResult(RESULT_OK, intent);
        this.finish();
    }
}