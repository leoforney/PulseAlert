package tk.teamprotien.pulsealert;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.onegravity.contactpicker.core.ContactImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Dynamic Signals on 1/28/2017.
 */

public class HeartRateMainFragment extends Fragment implements View.OnClickListener {

    int heartRate = 90;
    boolean callplaced = false;
    TextView heartRateTextView;
    Context context;
    private final static String TAG = HeartRateMainFragment.class.getName();
    SharedPreferences pref;
    LocationManager lm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_heart_rate, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();
        context = v.getContext();
        heartRateTextView = (TextView) v.findViewById(R.id.current_heart_rate_textview);
        v.findViewById(R.id.activateCall).setOnClickListener(this);

        pref = context.getSharedPreferences("pulsealert", Context.MODE_PRIVATE);

        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        updateDisplay();
    }

    private void updateDisplay() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
/*
                        if (heartRate > 0) {
                            double randomNumber = getRandomNumberBetween(-0.3, 2.65);
                            if (randomNumber < 0) randomNumber *= 1.25;
                            heartRate = (int) (heartRate + randomNumber);
                        }
                        if (heartRate > 140) {
                            heartRate = -52;

                        }
                        if (heartRate < 0) {
                            heartRate = 0;
                        }
                        heartRateTextView.setText("Current HR: " + String.valueOf(heartRate));
                        if (heartRate < 25) {

                        }*/
                    }
                });

            }

        }, 0, 750);
    }

    public void callHelp() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:8475287275"));
        if (!callplaced) {
            heartRateTextView.setTextColor(getResources().getColor(R.color.colorAccent));
            startActivity(intent);
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);
            callplaced = true;
            Log.d(TAG, "Oh no!!!");

            Log.d(TAG, pref.getString("contacts", ""));

            List<ContactImpl> contacts = Arrays.asList(new Gson().fromJson(pref.getString("contacts", ""), ContactImpl[].class));


            if (location != null) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();

                for (ContactImpl contact : contacts) {
                    Log.d(TAG, contact.getFirstName() + ": " + contact.getPhone(2));
                    sendSMS(contact.getPhone(2), "Help! I just had a heart attack! I'm at Long: " + longitude + " and Lat: " + latitude);
                }
            }
        }
    }

    public static double getRandomNumberBetween(double rangeMin, double rangeMax) {
        Random r = new Random();
        return rangeMin + (rangeMax - rangeMin) * r.nextDouble();
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activateCall:
                callHelp();
        }
    }
}
