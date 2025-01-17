package ru.hepolise.cre;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;


public class CallReceiver extends PhonecallReceiver {
    private static String TAG = "CRE";

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {
        enableCallRecording(ctx, false);
        Log.d(TAG, "incoming call received");
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end)
    {
        //
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start)
    {
        enableCallRecording(ctx, false);
        Log.d(TAG, "outgoing call started");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end)
    {
        //
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start)
    {
        //
    }
    static Context contextGlobal;
    public static void enableCallRecording(Context context, boolean force) {
        Log.d(TAG, "Call Recording Enabler started");
        try {
            if ((Settings.Global.getInt(context.getContentResolver(), "op_voice_recording_supported_by_mcc") == 0) || force ) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                try {
                    Settings.Global.putInt(context.getContentResolver(), "op_voice_recording_supported_by_mcc", 1);
                    Settings.Global.putInt(context.getContentResolver(), "oplus_customize_has_enter_auto_record_activity", 1);
                    Settings.Global.putInt(context.getContentResolver(), "oplus_customize_all_call_audio_record", 1);
                    if (sharedPreferences.getBoolean(MainActivity.getPrefName(), true))
                        Toast.makeText(context, "Call Recording is enabled", Toast.LENGTH_SHORT).show();
                } catch (SecurityException e) {
                    Toast.makeText(context, "Trying to get secure permission...", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Permission not granted: " + e.getLocalizedMessage());
                    contextGlobal = context;
                    new GrantPermission().execute();
                }
            } else {
                Log.d(TAG, "Call Recording already enabled");
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Setting not found: " + e.getLocalizedMessage());
        }
    }
    private static class GrantPermission extends AsyncTask<String, Void, String> {
        String buffer = "";
        @Override
        public String doInBackground(String... path) {
            try {
                Process su = Runtime.getRuntime().exec("su");
                DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
                String c = "pm grant "+ contextGlobal.getPackageName() +" android.permission.WRITE_SECURE_SETTINGS";
                Log.d(TAG,"command: "+ c);
                outputStream.writeBytes(c+"\n");
                outputStream.flush();
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                su.waitFor();
                BufferedReader rdr = new BufferedReader(new InputStreamReader(su.getErrorStream()));
                StringBuilder buf = new StringBuilder();
                String line;
                while ((line = rdr.readLine()) != null) {
                    buf.append(line + " ");
                }
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (buffer.equals("")) {
                Log.d(TAG, "Attempting to try write global settings");
                Settings.Global.putInt(contextGlobal.getApplicationContext().getContentResolver(), "op_voice_recording_supported_by_mcc", 1);
                Settings.Global.putInt(context.getContentResolver(), "oplus_customize_has_enter_auto_record_activity", 1);
                Settings.Global.putInt(context.getContentResolver(), "oplus_customize_all_call_audio_record", 1);
            } else {
                Log.d(TAG, buffer);
                Toast.makeText(contextGlobal.getApplicationContext(), "Error while executing command: " + buffer, Toast.LENGTH_LONG).show();
            }
        }

    }

}
