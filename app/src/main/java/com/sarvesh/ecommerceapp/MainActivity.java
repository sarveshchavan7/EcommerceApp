package com.sarvesh.ecommerceapp;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.PersistableBundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    MyAsyncTask myAsyncTask;
    UnDestructedFragment unDestructedFragment;
    ImageView imageView;
    EditText editText;
    ArrayList<HashMap<String, String>> arrayList;
    ArrayList<Bitmap> bitmaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        editText= (EditText) findViewById(R.id.editText);
        listView = (ListView) findViewById(R.id.listView);

        //savedInstanceState is null mean activity is running for the first time
        if (savedInstanceState == null) {
            unDestructedFragment = new UnDestructedFragment();
            getSupportFragmentManager().beginTransaction().add(unDestructedFragment, "MyFragment").commit();
        } else {
            unDestructedFragment = (UnDestructedFragment) getSupportFragmentManager().findFragmentByTag("MyFragment");
            if(bitmaps!=null && arrayList!=null){
                initializeListView(arrayList,bitmaps);
            }

        }


    }


    //If screen get rotated we gonna again get the array and bitmap from the asyncttask class
    public void updateArrayList(ArrayList<HashMap<String, String>> arrayList,ArrayList<Bitmap> bitmaps){
        this.arrayList = arrayList;
        this.bitmaps=bitmaps;
    }

    //Onclick listener for search button
    public void Search(View view) {
        if (myAsyncTask != null) {
            myAsyncTask.cancel(true);
        } else {
            unDestructedFragment.beginTask(editText.getText().toString());
        }
    }

    //This method will be called from our asynctask's onPostExecute() method
    //once we finish our background process
    public void initializeListView(ArrayList<HashMap<String, String>> arrayList,ArrayList<Bitmap> bitmaps) {
        this.arrayList = arrayList;
        this.bitmaps=bitmaps;
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.listview_items, R.id.title, arrayList);
        listView.setAdapter(customAdapter);
    }

    public void showToast() {
        Toast.makeText(this,"Exception occurred please try diff keyword \nOR Request at slower rate",Toast.LENGTH_LONG).show();
    }


    //I can even do this using view holder pattern but for now for the sake of simplicity
    //i have keep it as it is
    class CustomAdapter extends ArrayAdapter {
        Context mContext;

        public CustomAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull ArrayList arrayList) {
            super(context, resource, textViewResourceId, arrayList);
            mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            View v = layoutInflater.inflate(R.layout.listview_items, null);
            TextView title = v.findViewById(R.id.title);
            TextView amount = v.findViewById(R.id.amount);
            ImageView imageView = v.findViewById(R.id.imageView);

            //our arrayList is of type hashmap which holds the value for eash keys that are Title,FormattedPrice and URL
            if(arrayList!=null && arrayList.get(position)!=null){
                title.setText(arrayList.get(position).get("Title"));
                amount.setText(arrayList.get(position).get("FormattedPrice"));
            }

            //bitmaps is an arrayList of type bitmap which hold all the images which are downloaded
            //from the url since we can't do download here on the main thread i have done that in
            //async task itself
            if(bitmaps!=null && bitmaps.get(position)!=null){
                imageView.setImageBitmap(bitmaps.get(position));
            }else{
                imageView.setImageResource(R.drawable.no_image_available);
            }


            return v;
        }
    }

}

