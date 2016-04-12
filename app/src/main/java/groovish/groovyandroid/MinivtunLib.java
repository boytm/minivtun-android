package groovish.groovyandroid;

import android.util.Log;

/**
 * Created by Jesse on 2016/2/17.
 */
public class MinivtunLib {
    private static final String TAG = "MinivtunLib";
    static {
        Log.d(TAG, "try to load dynamic library minivtun");
        try {
            System.loadLibrary("minivtun");
        } catch (Exception e) {
            Log.e(TAG, "Got " + e.toString());
        }
    }

    public native static void configClient(String crypto_passwd, String crypto_type, String vaddr, int keepalive_timeo, int reconnect_timeo);
    public native static int runClient(int tunfd, int sockfd);
    public native static void stopClient();
}
