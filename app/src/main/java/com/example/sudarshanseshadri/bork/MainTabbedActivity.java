package com.example.sudarshanseshadri.bork;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class MainTabbedActivity extends AppCompatActivity {


    private CameraFragment homeFragment;
    private LogFragment logFragment;
    private SettingsFragment settingsFragment;


    private boolean barkAtSquirrel = true;
    private boolean barkAtGrackle = true;



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home: {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();

                    ft.replace(R.id.id_container, homeFragment);
                    ft.commit();
                    setTitle("Bork");
                    return true;
                }
                case R.id.navigation_log: {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();

                    ft.replace(R.id.id_container, logFragment);
                    ft.commit();
                    setTitle("Log");
                    return true;
                }
                case R.id.navigation_settings: {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();

                    ft.replace(R.id.id_container, settingsFragment);
                    ft.commit();
                    setTitle("Settings");
                    return true;
                }
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabbed);


        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        homeFragment = new CameraFragment();
        logFragment = new LogFragment();
        settingsFragment = new SettingsFragment();

        ft.add(R.id.id_container, homeFragment);
        ft.commit();


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    public boolean getBarkAtSquirrel() {
        return barkAtSquirrel;
    }

    public void setBarkAtSquirrel(boolean barkAtSquirrel) {
        this.barkAtSquirrel = barkAtSquirrel;
    }

    public boolean getBarkAtGrackle() {
        return barkAtGrackle;
    }

    public void setBarkAtGrackle(boolean barkAtGrackle) {
        this.barkAtGrackle = barkAtGrackle;
    }
}
