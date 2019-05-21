package com.example.sudarshanseshadri.bork;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class LogFragment extends Fragment {

    ListView listView;
    ArrayList<String> arrayList;
    ArrayList<File> fileList;

    View rootview;

    public LogFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.fragment_log, container, false);

        listView = rootview.findViewById(R.id.id_listView);


        Log.d("LOGF", "in");

        arrayList = getTextFile();
        Log.d("LOGF", "out");
        fileList = getFileList();


        CustomAdapter customAdapter = new CustomAdapter(getContext(), R.layout.layout_log_list_view, arrayList, fileList);
        listView.setAdapter(customAdapter);


        return rootview;
    }

    public class CustomAdapter extends ArrayAdapter<String>
    {
        Context context;
        int resource;
        List<String> list;
        List<File> files;
        public CustomAdapter(@NonNull Context context, int resource, @NonNull List<String> strings, @NonNull List<File> files) {
            super(context, resource, strings);
            this.context=context;
            this.resource=resource;
            this.files=files;
            list=strings;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            final View adapterView = layoutInflater.inflate(resource, null);

            final TextView textView = adapterView.findViewById(R.id.id_textView);
            ImageView image = adapterView.findViewById(R.id.id_imageView);
            ImageView garbage = adapterView.findViewById(R.id.imageView_garbage);

            garbage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    files.get(position).delete();
                    files.remove(position);
                    list.remove(position);
                    notifyDataSetChanged();

                    String newLogFileText = "";
                    for (String s:list)
                    {
                        newLogFileText+= (s+"\n");
                    }

                    try {

                        ContextWrapper cw = new ContextWrapper(getContext());
                        // path to /data/data/yourapp/app_data/imageDir
                        File directory = cw.getDir("images", Context.MODE_PRIVATE);
                        // Create imageDir
                        File file = new File(directory, "log.txt");
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileOutputStream fileOutputStream = new FileOutputStream(file,false);
                        fileOutputStream.write(newLogFileText.getBytes());

                    }  catch(FileNotFoundException e) {
                        e.printStackTrace();
                        Log.d("PRED", "fnfe");
                    }  catch(IOException e) {
                        e.printStackTrace();
                        Log.d("PRED", "ioEX");
                    }


                }
            });

            textView.setText(list.get(position)+"");


            Bitmap myBitmap = BitmapFactory.decodeFile(files.get(position).getAbsolutePath());

            image.setImageBitmap(myBitmap);



            adapterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast t = Toast.makeText(context, "" + textView.getText().toString(), Toast.LENGTH_SHORT);
                    t.show();
                }
            });



            return adapterView;

        }
    }

    public ArrayList<String> getTextFile()
    {
        ArrayList<String> lineArray = new ArrayList<>();


        try {

            ContextWrapper cw = new ContextWrapper(getContext());
            File directory = cw.getDir("images", Context.MODE_PRIVATE);
            // Create imageDir
            File file = new File(directory, "log.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                lineArray.add(line);
                Log.d("LOGF", line);
            }

            br.close();
        }
        catch (IOException e) {
            Log.d("LOGF", "ooof");
            e.printStackTrace();
            //You'll need to add proper error handling here
        }
        return lineArray;

    }

    public ArrayList<File> getFileList() {
        File [] allFiles = null;
        ContextWrapper cw = new ContextWrapper(getContext());
        File folder = cw.getDir("images", Context.MODE_PRIVATE);
        if(folder.exists()) {
             allFiles = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"));
                }
            });

        }

        return new ArrayList<>(Arrays.asList(allFiles));
    }
}
