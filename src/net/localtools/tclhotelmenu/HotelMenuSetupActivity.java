package net.localtools.tclhotelmenu;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public final class HotelMenuSetupActivity extends Activity {
    private EditText component;
    private EditText action;
    private EditText extraName;
    private EditText extraValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_menu_setup);

        TextView firmware = findViewById(R.id.firmware);
        firmware.setText(getString(R.string.firmware, Build.MODEL, Build.VERSION.INCREMENTAL));
        component = findViewById(R.id.component);
        action = findViewById(R.id.action);
        extraName = findViewById(R.id.extra_name);
        extraValue = findViewById(R.id.extra_value);

        if (savedInstanceState == null) {
            MenuTarget saved = MenuTarget.load(this);
            fill(saved == null ? MenuTarget.TCL_PRESET : saved);
        }
        findViewById(R.id.try_menu).setOnClickListener(view -> tryMenu());
        findViewById(R.id.save_menu).setOnClickListener(view -> saveMenu());
        findViewById(R.id.load_preset).setOnClickListener(view -> fill(MenuTarget.TCL_PRESET));
    }

    private void fill(MenuTarget target) {
        component.setText(target.component.flattenToShortString());
        action.setText(target.action);
        extraName.setText(target.extraName);
        extraValue.setText(target.extraValueText());
    }

    private MenuTarget enteredTarget() {
        MenuTarget target = MenuTarget.parse(this, component.getText().toString(),
                action.getText().toString(), extraName.getText().toString(),
                extraValue.getText().toString());
        if (target == null) {
            showMessage(R.string.invalid_target);
        }
        return target;
    }

    private void tryMenu() {
        MenuTarget target = enteredTarget();
        if (target == null) {
            return;
        }
        try {
            startActivity(target.createIntent());
        } catch (ActivityNotFoundException | SecurityException error) {
            showMessage(R.string.menu_unavailable);
        }
    }

    private void saveMenu() {
        MenuTarget target = enteredTarget();
        if (target == null) {
            return;
        }
        if (target.save(this)) {
            showMessage(R.string.target_saved);
            finish();
        } else {
            showMessage(R.string.save_failed);
        }
    }

    private void showMessage(int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
