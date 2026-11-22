package net.localtools.tclhotelmenu;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public final class OpenHotelMenuActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            MenuTarget target = MenuTarget.load(this);
            if (target == null && MenuTarget.isTestedFirmware()) {
                target = MenuTarget.TCL_PRESET;
            }
            if (target == null) {
                openSetup();
            } else {
                startActivity(target.createIntent());
            }
        } catch (ActivityNotFoundException | SecurityException error) {
            Toast.makeText(this, R.string.menu_unavailable, Toast.LENGTH_SHORT).show();
            openSetup();
        } finally {
            finish();
        }
    }

    private void openSetup() {
        startActivity(new Intent(this, HotelMenuSetupActivity.class));
    }
}
