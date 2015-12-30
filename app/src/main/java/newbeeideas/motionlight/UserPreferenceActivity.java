package newbeeideas.motionlight;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by presisco on 2015/12/30.
 */

public class UserPreferenceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new UserPreferenceFragment())
                .commit();
    }
}
