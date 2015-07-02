package la.ggu.m16.m16chat.util;

import android.util.Log;

import java.util.regex.Pattern;

/**
 * Created by DingGGu on 2015-07-02.
 */
public class ParseUsername {
    public ParseUsername() {}
    public static String parseColor(String username) {

        String result = username;
        if(Pattern.matches("^\\|CFF(.*)", username)) {
            result = username.substring(10, username.length());
        }
        return result;
    }
}
