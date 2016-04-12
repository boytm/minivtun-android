package groovish.groovyandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.net.VpnService;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";

    private TextView mServerAddress;
    private TextView mServerPort;
    private TextView mSharedSecret;
    private Spinner mCryptoType;
    private TextView mTunAddress;
    private TextView mTunPrefixLen;
    private Button btn;

    private ArrayAdapter crypto_methods_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 关联 layout

        mServerAddress = (TextView) findViewById(R.id.address);
        mServerPort = (TextView) findViewById(R.id.port);
        mSharedSecret = (TextView) findViewById(R.id.secret);
        mCryptoType = (Spinner) findViewById(R.id.crytpo_type);
        mTunAddress = (TextView) findViewById(R.id.tun_address);
        mTunPrefixLen = (TextView) findViewById(R.id.tun_prefix_len);

        // button
        btn = (Button) findViewById(R.id.connect);
        btn.setOnClickListener(this);

        // Spinner
        // 将可选内容与ArrayAdapter连接起来
        crypto_methods_adapter = ArrayAdapter.createFromResource(this, R.array.crypt_methods, android.R.layout.simple_spinner_item);
        mCryptoType.setAdapter(crypto_methods_adapter);

        mCryptoType.setOnItemSelectedListener(this);
    }
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
        //crypto_methods_adapter.getItem(arg2));
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public void onClick(View v) {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, RESULT_OK, null);
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            String prefix = getPackageName();
            Intent intent = new Intent(this, MinivtunService.class)
                    .putExtra(prefix + ".ADDRESS", mServerAddress.getText().toString())
                    .putExtra(prefix + ".PORT", mServerPort.getText().toString())
                    .putExtra(prefix + ".SECRET", mSharedSecret.getText().toString())
                    .putExtra(prefix + ".TUN_ADDRESS", mTunAddress.getText().toString())
                    .putExtra(prefix + ".TUN_PREFIX_LEN", mTunPrefixLen.getText().toString());

            String crypto_type = mCryptoType.getSelectedItem().toString();
            if (crypto_type == null ||
                    mCryptoType.getSelectedItemPosition() == mCryptoType.getCount() - 1) // last item
                crypto_type = "";

            intent.putExtra(prefix + ".CRYPTO_TYPE", crypto_type);

            startService(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
