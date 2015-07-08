package la.ggu.m16.m16chat.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesControl {
    public static String USER_DATA_PREF = "USER_DATA";
    public static String USER_NAME = "user_id";
    public static String USER_PWD = "user_pw";
    public static String ALARM_DATA_PREF = "ALARM_DATA";
    public static String ALARM_SET = "ALARM_SET";
    public static String ALARM_ON = "ON";
    public static String ALARM_OFF = "OFF";

    private Context mContext;

    private PreferencesControl(Context context) {
        this.mContext = context;
    }

    public static PreferencesControl instance;

    public synchronized static PreferencesControl getInstance(Context context) {

        if (instance == null) {
            instance = new PreferencesControl(context);
        }
        return instance;
    }

    public void set(String PREF_NAME, String key, String value) {
        SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public String get(String PREF_NAME, String key, String dftValue) {
        SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        try {
            return pref.getString(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }
    }
}
