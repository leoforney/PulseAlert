package tk.teamprotien.pulsealert;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.picture.ContactPictureType;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getName();

    private FrameLayout container;
    private CoordinatorLayout coordinatorLayout;
    private Context context;
    private BottomBar bottomBar;

    private Fragment heartRateFragment;
    private SharedPreferences pref;

    private final static int REQUEST_CONTACT = 1010;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        pref = context.getSharedPreferences("pulsealert", MODE_PRIVATE);

        heartRateFragment = new HeartRateMainFragment();

        askForPermission(Manifest.permission.CALL_PHONE, 0x01);
        askForPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS, 0x02);
        askForPermission(Manifest.permission.READ_CONTACTS, 0x03);
        askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, 0x04);
        askForPermission(Manifest.permission.SEND_SMS, 0x05);
        askForPermission(Manifest.permission.ACCESS_COARSE_LOCATION, 0x06);

        container = (FrameLayout) findViewById(R.id.fragment_container);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        bottomBar = BottomBar.attach(this, savedInstanceState);

        bottomBar.setMaxFixedTabs(1);

        bottomBar.setItemsFromMenu(R.menu.bottombar_items, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(@IdRes int menuItemId) {
                switch (menuItemId) {
                    case R.id.heart_rate_tab:
                        switchFragment(heartRateFragment);
                        break;
                    case R.id.settings_tab:
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                        View layout = inflater.inflate(R.layout.settings_dialog, new LinearLayout(context));

                        Button emergencyContactsButton = (Button) layout.findViewById(R.id.contact_button);
                        emergencyContactsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                emergencyContactPicker();
                            }
                        });

                        alertDialogBuilder.setView(layout);
                        alertDialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bottomBar.selectTabAtPosition(0, true);
                            }
                        });
                        alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                bottomBar.selectTabAtPosition(0, true);
                                bottomBar.setActiveTabColor(getResources().getString(R.string.tab1));
                            }
                        });
                        alertDialogBuilder.create().show();
                        break;
                }
            }
        });

        bottomBar.mapColorForTab(0, Color.parseColor(getString(R.string.tab1)));
        bottomBar.mapColorForTab(1, Color.parseColor(getString(R.string.tab2)));

        bottomBar.selectTabAtPosition(0, false);
        switchFragment(heartRateFragment);

        if (pref.getString("contacts", "").equals("")) {
            emergencyContactPicker();
        }

    }

    private void emergencyContactPicker() {
        Intent intent = new Intent(context, ContactPickerActivity.class)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                .putExtra(ContactPickerActivity.EXTRA_ONLY_CONTACTS_WITH_PHONE, true)
                .putExtra(ContactPickerActivity.EXTRA_SHOW_CHECK_ALL, true)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name());
        startActivityForResult(intent, REQUEST_CONTACT);
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        }
    }

    public void switchFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK &&
                data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {

            // process contacts
            List<Contact> contacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);

            pref.edit().putString("contacts", new Gson().toJson(contacts)).apply();

            Log.d(TAG, new Gson().toJson(contacts));

        }
    }
}

