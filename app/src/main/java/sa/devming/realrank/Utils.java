package sa.devming.realrank;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import sa.devming.realrank.widget.WidgetConfig;

public class Utils {

    public static WidgetConfig getWidgetPreference(Context context) {
        Gson gson = new Gson();
        String jsonStr = context.getSharedPreferences(Constants.PREF_WIDGET_CONFIG, Activity.MODE_PRIVATE)
                .getString(WidgetConfig.class.getSimpleName(), "");
        if (!"".equals(jsonStr)) {
            return gson.fromJson(jsonStr, WidgetConfig.class);
        }
        return defaultWidgetConfig();
    }

    private static WidgetConfig defaultWidgetConfig() {
        WidgetConfig widgetConfig = new WidgetConfig();
        widgetConfig.setInterval(30);
        widgetConfig.setDisturb(false);
        widgetConfig.setDisturbFrom(22);
        widgetConfig.setDisturbTo(8);
        widgetConfig.setUpdateScreenOff(false);
        widgetConfig.setWidgetId(-1);
        return widgetConfig;
    }

    public static void deleteWidgetPreference(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.PREF_WIDGET_CONFIG, Activity.MODE_PRIVATE).edit();
        editor.clear().apply();
    }

    public static void saveWidgetPreference(Context context, WidgetConfig widgetConfig) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.PREF_WIDGET_CONFIG, Activity.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String jsonStr = gson.toJson(widgetConfig);
        editor.putString(WidgetConfig.class.getSimpleName(), jsonStr);
        editor.apply();
    }


}
