package zouhairkhatouri.tapcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Set;

public class PairingActivity extends AppCompatActivity {

    private static final String BT_NOT_AVAILABLE = "Bluetooth is not available!";
    private static final String BT_ENABLED = "Please turn the bluetooth on.";

    BluetoothAdapter btAdapter;

    private String TapAdress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (getApplicationContext().getSharedPreferences("id",  MODE_MULTI_PROCESS).contains("id")) {
            String id = getApplicationContext().getSharedPreferences("id",  MODE_MULTI_PROCESS).getString("id",null);
            Intent intent = new Intent(this, SendingActivity.class);
            intent.putExtra("id",id);
            startActivity(intent);
        }
        else if(btAdapter == null){
            Toast.makeText(getApplicationContext(), BT_NOT_AVAILABLE, Toast.LENGTH_SHORT).show();
        }
        else {
            (new Thread(new discover(this))).start();
        }

    }

    private class discover implements Runnable {

        private PairingActivity pair;
        discover(PairingActivity pair){
            this.pair = pair;
        }

        @Override
        public void run() {
            while(TapAdress == null) {
                if (!btAdapter.isEnabled()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), BT_ENABLED, Toast.LENGTH_SHORT).show();
                        }
                    });
                    synchronized (this) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {
                        if (device.getName().equals("TapController")) {
                            TapAdress = device.getAddress();
                            break;
                        }
                    }
                }
            }
            Intent intent = new Intent(pair, MasterActivity.class);
            intent.putExtra("TapAdress", TapAdress);
            startActivity(intent);
        }
    }

}
