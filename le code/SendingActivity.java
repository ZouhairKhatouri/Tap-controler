package zouhairkhatouri.tapcontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

public class SendingActivity extends AppCompatActivity  {

    private static final String NO_DURATION_SPECIFIED = "You have to enter the duration of the sprinkle!";
    private static final String NO_PAIRED_DEVICE = "No paired device.";
    private static final String ONLY_INT_ACCEPTED = "Only integer are accepted as values for the duration of the sprinkle!";

    private String id = null;

    public SeekBar seekBar;
    public EditText editText;

    private double flow = 0;
    private String duration = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);

        if (getIntent().hasExtra("id")) {
            id = getIntent().getStringExtra("id");
        }
        else {
            Toast.makeText(getApplicationContext(), NO_PAIRED_DEVICE, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, PairingActivity.class);
            startActivity(intent);
            throw new IllegalArgumentException("Got no id when launching this activity!");
        }

        seekBar = findViewById(R.id.seekBar);
        editText = findViewById(R.id.editText);
        Button sendingButton = findViewById(R.id.SendCommand);
        Button pairingButton = findViewById(R.id.PairWithNew);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                flow = seekBar.getProgress();
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                duration = editText.getText().toString();
            }
        });
        sendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                duration = editText.getText().toString();
                send();
            }
        });
        pairingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete shared preferences
                getApplicationContext().getSharedPreferences("id",  MODE_MULTI_PROCESS).edit().clear().apply();
                // restarting
                Intent intent = new Intent(getApplicationContext(), PairingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void send() {
        if(duration == null){
            Toast.makeText(getApplicationContext(), NO_DURATION_SPECIFIED, Toast.LENGTH_SHORT).show();
        }
        else if (id == null) {
            Toast.makeText(getApplicationContext(), NO_PAIRED_DEVICE, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, PairingActivity.class);
            startActivity(intent);
            throw new IllegalArgumentException("Got no id when launching this activity!");
        }
        else{
            int d;
            try {
                d = Integer.parseInt(duration);
            }
            catch(Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), ONLY_INT_ACCEPTED, Toast.LENGTH_SHORT).show();
                return;
            }
            new Poster(id, flow, d, this).post();
        }
    }

}

