package groovish.groovyandroid;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;
//import groovy.transform.CompileStatic

import java.nio.channels.DatagramChannel;
import java.net.InetSocketAddress;

/**
 * Created by Jesse on 2016/2/3.
 */


public class MinivtunService extends VpnService implements Runnable {
    private static final String TAG = "MinivtunService";

    private String mServerAddress;
    private String mServerPort;
    private String mTunAddress;
    private int mTunPrefixLen;
    private String mSharedSecret;
    private String mCryptoType;
    private int mMTU = 1416;

    private PendingIntent mConfigureIntent;
    private Thread mThread;
    private boolean mQuit;
    private ParcelFileDescriptor mInterface;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Extract information from the intent.
        String prefix = getPackageName();
        mServerAddress = intent.getStringExtra(prefix + ".ADDRESS");
        mServerPort = intent.getStringExtra(prefix + ".PORT");
        mSharedSecret = intent.getStringExtra(prefix + ".SECRET");
        mCryptoType = intent.getStringExtra(prefix + ".CRYPTO_TYPE");
        mTunAddress = intent.getStringExtra(prefix + ".TUN_ADDRESS");
        mTunPrefixLen =  Integer.parseInt(intent.getStringExtra(prefix + ".TUN_PREFIX_LEN"));

        stopThread();

        // Start a new session by creating a new thread.
        mThread = new Thread(this, "MinivtunThread");
        mThread.start();

        Log.i(TAG, "thread create successfully");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRevoke() {
        stopThread();
        super.onRevoke();
    }

    private void stopThread() {
        try {
            mQuit = true;
            if (mThread != null) {
                MinivtunLib.stopClient();
                mThread.join();
                mThread = null;
            }
        } catch (InterruptedException e) {
            Log.i(TAG, "join thread Interrupted");
        }
    }

    @Override
    public synchronized void run() {

        mQuit = false;

        try {
            Log.i(TAG, "Starting");

            InetSocketAddress server = new InetSocketAddress(
                mServerAddress, Integer.parseInt(mServerPort));

            // We try to create the tunnel for several times. The better way
            // is to work with ConnectivityManager, such as trying only when
            // the network is avaiable. Here we just use a counter to keep
            // things simple.
            for (int attempt = 0; !mQuit && attempt < 10; ++attempt) {
                // Reset the counter if we were connected.
                if (run(server)) {
                    attempt = 0;
                }

                // Sleep for a while. This also checks if we got interrupted.
                Thread.sleep(3000);
            }
            Log.i(TAG, "Giving up");
        } catch (Exception e) {
            Log.e(TAG, "Got " + e.toString());
        } finally {
            try {
                mInterface.close();
            } catch (Exception e) {
                // ignore
            }
            mInterface = null;

            Log.i(TAG, "Exiting");
        }
    }

    private boolean run(InetSocketAddress addr) throws Exception {
        DatagramChannel tunnel = null;
        boolean connected = false;
        try {
            // Create a DatagramChannel as the VPN tunnel.
            tunnel = DatagramChannel.open();

            // Protect the tunnel before connecting to avoid loopback.
            if (!protect(tunnel.socket())) {
                throw new IllegalStateException("Cannot protect the tunnel");
            }

            // Connect to the server.
            tunnel.connect(addr);
            // For simplicity, we use the same thread for both reading and
            // writing. Here we put the tunnel into non-blocking mode.
            tunnel.configureBlocking(false);

            // setup tunnel
            configure("");

            ParcelFileDescriptor sockfd = ParcelFileDescriptor.fromDatagramSocket(tunnel.socket());
            MinivtunLib.configClient(mSharedSecret, mCryptoType, mTunAddress, 13, 60);
            MinivtunLib.runClient(mInterface.getFd(), sockfd.getFd());

        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "Got " + e.toString());
        } finally {
            try {
                tunnel.close();
            } catch (Exception e) {
                // ignore
            }
        }
        return false;
    }

    private void configure(String parameters) throws Exception {
        // If the old interface has exactly the same parameters, use it!
        if (mInterface != null) {
            Log.i(TAG, "Using the previous interface");
            return;
        }

        // Configure a builder while parsing the parameters.
        VpnService.Builder builder = new VpnService.Builder();

        builder.addAddress(mTunAddress, mTunPrefixLen);
        Log.i(TAG, "set tunnel address: " + mTunAddress + "/" + mTunPrefixLen);
        builder.setMtu(mMTU);
        // add US routes
        builder.addRoute("0.0.0.0", 0);
        builder.addDnsServer("8.8.8.8");

        // Close the old interface since the parameters have been changed.
        try {
            mInterface.close();
        } catch (Exception e) {
            // ignore
        }

        // Create a new interface using the builder and save the parameters.
        mInterface = builder.setSession(mServerAddress)
                .setConfigureIntent(mConfigureIntent)
                .establish();
        //mParameters = parameters;
        Log.i(TAG, "New interface: " + parameters);
    }
}
