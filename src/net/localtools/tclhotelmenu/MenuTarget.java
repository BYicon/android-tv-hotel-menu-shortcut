package net.localtools.tclhotelmenu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

final class MenuTarget {
    private static final String PREFERENCES = "hotel_menu";
    private static final String COMPONENT = "component";
    private static final String ACTION = "action";
    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_VALUE = "extra_value";

    static final MenuTarget TCL_PRESET = new MenuTarget(
            new ComponentName("com.tcl.factory.view", "com.tcl.factory.view.MainMenu"),
            "com.tcl.factory.intent.action.FACTORYMENU", "select", 5);

    final ComponentName component;
    final String action;
    final String extraName;
    final Integer extraValue;

    private MenuTarget(ComponentName component, String action, String extraName, Integer extraValue) {
        this.component = component;
        this.action = action;
        this.extraName = extraName;
        this.extraValue = extraValue;
    }

    static boolean isTestedFirmware() {
        return "tcl_mt5879_cn".equals(Build.MODEL)
                && Build.VERSION.SDK_INT == Build.VERSION_CODES.R
                && "2026012200".equals(Build.VERSION.INCREMENTAL);
    }

    static MenuTarget parse(Context context, String componentText, String action,
                            String extraName, String valueText) {
        ComponentName component = ComponentName.unflattenFromString(componentText.trim());
        if (component == null || context.getPackageName().equals(component.getPackageName())
                || !component.getPackageName().matches("[\\w.]+")
                || !component.getClassName().matches("[\\w.$]+")) {
            return null;
        }

        String name = extraName.trim();
        String value = valueText.trim();
        if (name.isEmpty() != value.isEmpty()) {
            return null;
        }

        try {
            return new MenuTarget(component, action.trim(), name,
                    value.isEmpty() ? null : Integer.valueOf(value));
        } catch (NumberFormatException error) {
            return null;
        }
    }

    static MenuTarget load(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return parse(context, settings.getString(COMPONENT, ""), settings.getString(ACTION, ""),
                settings.getString(EXTRA_NAME, ""), settings.getString(EXTRA_VALUE, ""));
    }

    boolean save(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
                .putString(COMPONENT, component.flattenToShortString())
                .putString(ACTION, action)
                .putString(EXTRA_NAME, extraName)
                .putString(EXTRA_VALUE, extraValueText())
                .commit();
    }

    String extraValueText() {
        return extraValue == null ? "" : extraValue.toString();
    }

    Intent createIntent() {
        Intent intent = new Intent()
                .setComponent(component)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!action.isEmpty()) {
            intent.setAction(action);
        }
        if (extraValue != null) {
            intent.putExtra(extraName, extraValue.intValue());
        }
        if (TCL_PRESET.component.equals(component)) {
            // TCL reads the selected section when its single-instance activity is created.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        return intent;
    }
}
