package zouhairkhatouri.tapcontroller;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This class proceeds to the POST request.
 * Taken from https://www.journaldev.com/13629/okhttp-android-example-tutorial
 *
 */

public class Poster {

    private static final String CHECK_YOUR_CO = "No internet available!";
    private static final String url = "http://tapcontroller.000webhostapp.com/putValue.php";
    private static final String SUCCESS = "Command successfully sent!";
    private static final String INTERNAL_ERROR = "Internal error has occurred.";

    private String id;
    private double flow;
    private int d;
    private SendingActivity context;

    Poster(String id, double flow, int d, SendingActivity context){
        this.id = id;
        this.flow = flow;
        this.d = d;
        this.context = context;
    }

    public void post(){

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("clientID", id)
                .add("flow",Double.toString(flow))
                .add("duration",Integer.toString(d))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        if(hasNetWorkAccess()){
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    Log.d("Success","sent!");
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, SUCCESS, Toast.LENGTH_SHORT).show();
                            context.seekBar.setProgress(0);
                            context.editText.setText("");
                        }
                    });
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("Failure","not sent!");
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, INTERNAL_ERROR, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        else{
            Log.d("Failure","not sent!");
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, CHECK_YOUR_CO, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Partially taken from https://stackoverflow.com/questions/40713270/how-to-get-network-state-change-on-android
    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasNetWorkAccess() {
        ConnectivityManager cm = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork != null) {
            android.net.ConnectivityManager.setProcessDefaultNetwork(activeNetwork);
            return true;
        }
        return false;
    }
}