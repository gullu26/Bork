package com.example.sudarshanseshadri.bork;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class SettingsFragment extends Fragment {


    View rootview;
    RadioGroup radioGroup;
    RadioButton r1, r2, r3, r4, r5;

    MediaPlayer barkPlayer;

    CheckBox cs, cg;
    //squirrel and grackle

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.fragment_settings, container, false);




        final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        final SharedPreferences.Editor editor = sharedPref.edit();




        radioGroup = rootview.findViewById(R.id.radioGroup);
        r1 = rootview.findViewById(R.id.radioButton);
        r2 = rootview.findViewById(R.id.radioButton2);
        r3 = rootview.findViewById(R.id.radioButton3);
        r4 = rootview.findViewById(R.id.radioButton4);
        r5 = rootview.findViewById(R.id.radioButton5);

        int barkID = sharedPref.getInt("barkID", R.id.radioButton);
        Log.d("SETTINGS", barkID + " is current");
        try {
            radioGroup.check(barkID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d("SETTINGS", "error");
        }
        Log.d("SETTINGS", R.id.radioButton2+"");

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                editor.putInt("barkID", checkedId);
                editor.commit();
                Log.d("SETTINGS", checkedId+"");
                int barkSound=R.raw.bruno_british_lab;
                if (checkedId == R.id.radioButton)
                {
                    barkSound=R.raw.bruno_british_lab;

                }
                else if (checkedId == R.id.radioButton2)
                {
                    barkSound=R.raw.daisy_labradoodle;
                }
                else if (checkedId == R.id.radioButton3)
                {
                    barkSound=R.raw.simba_german_shepard;
                }
                else if (checkedId == R.id.radioButton4)
                {
                    barkSound=R.raw.bailey_chloe;
                }
                else if (checkedId == R.id.radioButton5)
                {
                    barkSound=R.raw.sudarshan_human;
                }

                editor.putInt("barkSound", barkSound);
                editor.commit();

                barkPlayer = MediaPlayer.create(getContext(), barkSound);
                barkPlayer.start();
                barkPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        barkPlayer.release();
                    }
                });

            }
        });


        cs = rootview.findViewById(R.id.checkBox);
        cg = rootview.findViewById(R.id.checkBox2);

        cs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("barkAtSquirrel", isChecked);
                editor.commit();
            }
        });

        cg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("barkAtGrackle", isChecked);
                editor.commit();
            }
        });



        cs.setChecked(sharedPref.getBoolean("barkAtSquirrel", false));
        cg.setChecked(sharedPref.getBoolean("barkAtGrackle", true));

        return rootview;

    }


}
