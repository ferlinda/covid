package com.example.covid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 101;
    private Context context;
    ChipNavigationBar bottomNav;
    FragmentManager fragmentManager;
    private BluetoothAdapter bluetoothAdapter;
    private ListView listPairedDevices, listAvailableDevices;
    private ProgressBar progressScanDevices;
    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;
    private FirebaseAuth fAuth;
    private Button activate_scan, deactivate_scan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //transparent statusBar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //hide actionBar
        try
        {
            Objects.requireNonNull(this.getSupportActionBar()).hide();
        }
        catch (NullPointerException ignored){}
        setContentView(R.layout.activity_main);

        // Bluetooth Permission Settings

        context = this;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivity(discoveryIntent);
        }
        checkPermissions();

        //default fragment
        setFragment(new HomeFragment());

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                Fragment selectedFragment = null;

                switch (id) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.nav_favorites:
                        selectedFragment = new CheckFragment();
                        break;
                    case R.id.nav_search:
                        selectedFragment = new LocationFragment();
                        break;
                }

                if (selectedFragment!=null){
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }

            }
        });
    }

    public void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit();
    }

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context,"Location enabled", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setMessage("Location permission is required.\n Please grant")
                        .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                checkPermissions();
                            }
                        })
                        .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.this.finish();
                            }
                        }).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), IntroActivity.class));
        finish();
    }

    public void go_bluetooth(View view){
        activate_scan = findViewById(R.id.scan_act);
        deactivate_scan= findViewById(R.id.scan_deact);
        activate_scan.setVisibility(View.INVISIBLE);
        deactivate_scan.setVisibility(View.VISIBLE);
        init();
    }

    public void init(){
        context = this;

        listPairedDevices = findViewById(R.id.list_paired_devices);
        listAvailableDevices = findViewById(R.id.list_available_devices);
        progressScanDevices = findViewById(R.id.progress_scan_devices);

        adapterPairedDevices = new ArrayAdapter<String>(context, R.layout.device_list_item);
        adapterAvailableDevices = new ArrayAdapter<String>(context, R.layout.device_list_item);

        listPairedDevices.setAdapter(adapterPairedDevices);
        listAvailableDevices.setAdapter(adapterAvailableDevices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapterPairedDevices.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

}
