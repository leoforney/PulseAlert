package tk.teamprotien.pulsealert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by Leo on 3/10/17.
 */

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        SharedPreferences pref = context.getSharedPreferences("PulseAlert", Context.MODE_PRIVATE);
        boolean callRequested = pref
                .contains("CallRequested");
        pref.edit().remove("CallRequested").apply();
        if (callRequested) {
            PhoneStateListener phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    Log.d(CallReceiver.class.getName(), "Call being processsed!" + state);
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {

                        AudioManager audioManager = (AudioManager) context
                                .getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_NORMAL);
                        audioManager.setSpeakerphoneOn(false);
                    } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {

                        AudioManager audioManager = (AudioManager) context
                                .getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        audioManager.setSpeakerphoneOn(true);
                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
            TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }

    }
}
