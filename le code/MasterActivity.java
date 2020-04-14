package zouhairkhatouri.tapcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MasterActivity extends AppCompatActivity {

    private static final String BT_ENABLED = "Please turn the bluetooth on.";
    private static final String DATA_NOT_COMPLETED = "Please fill all the informations first.";
    private static final String UNABLE_TO_SEND = "unable to send data, try again after checking the hardware." ;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address

    private EditText DeviceId;
    private EditText Ssid;
    private EditText Pswd;

    private String TapAdress;

    private String id = null;
    private String ssid = null;
    private String pswd = null;

    private BluetoothAdapter btAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        TapAdress = getIntent().getStringExtra("TapAdress");

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Views
        DeviceId = findViewById(R.id.DeviceId);
        Ssid = findViewById(R.id.Ssid);
        Pswd = findViewById(R.id.Pswd);
        Button pair = findViewById(R.id.Pair);

        // Listeners
        DeviceId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                id = DeviceId.getText().toString();
            }
        });
        Ssid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                ssid = Ssid.getText().toString();
            }
        });
        Pswd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                pswd = Pswd.getText().toString();
            }
        });
        pair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id == null || ssid == null || pswd == null){
                    Toast.makeText(getApplicationContext(), DATA_NOT_COMPLETED, Toast.LENGTH_SHORT).show();
                }
                else {
                    if (btAdapter.isEnabled()) {
                        id = DeviceId.getText().toString();
                        ssid = Ssid.getText().toString();
                        pswd = Pswd.getText().toString();
                        if(sendDataToPaired()){
                            startSending();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), UNABLE_TO_SEND, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), BT_ENABLED, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void startSending() {
        // Save id for next uses. Taken from https://stackoverflow.com/questions/15697735/how-to-change-start-activity-dynamically
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("id",  MODE_MULTI_PROCESS).edit();
        editor.putString("id", id);
        editor.apply();
        //
        Intent intent = new Intent(this, SendingActivity.class);
        intent.putExtra("id",id);
        startActivity(intent);
    }

    // Taken from https://wingoodharry.wordpress.com/2014/04/15/android-sendreceive-data-with-arduino-using-bluetooth-part-2/

    private boolean sendDataToPaired() {
        if(!btAdapter.isEnabled()){
            Toast.makeText(getApplicationContext(), BT_ENABLED, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, PairingActivity.class);
            startActivity(intent);
            return false;
        }
        else {
            //create device and set the MAC address

            BluetoothDevice device = btAdapter.getRemoteDevice(TapAdress);

            BluetoothSocket btSocket;

            try {
                btSocket = createBluetoothSocket(device);
            }
            catch (IOException e) {
                Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
                return false;
            }
            // Establish the Bluetooth socket connection.
            try {
                btSocket.connect();
            }
            catch (IOException e) {
                try {
                    btSocket.close();
                }
                catch (IOException e2) {
                    e2.printStackTrace();
                }
                return false;
            }
            ConnectedThread mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();

            return mConnectedThread.write(ssid+"#"+pswd);
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final OutputStream mmOutStream;

        //creation of the connect thread
        ConnectedThread(BluetoothSocket socket) {
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            // Deleted
        }

        //write method
        boolean write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
                return true;
            }
            catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();
                return false;
            }
        }
    }

}
