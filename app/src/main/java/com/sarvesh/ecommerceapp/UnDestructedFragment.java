package com.sarvesh.ecommerceapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

 /*
 * I have used Fragment because to avoid loss of view after screen rotation
 * setRetainInstance(true) allows fragment not to get destroyed
 * onAttah and onDetach is called in the fragment when the activity associated
 * with it is destroyed and created each time when screen orientaion changes
 */

public class UnDestructedFragment extends Fragment{
    Activity activity;
    MyAsyncTask myAsyncTask;


    public void beginTask(String searchText){
        myAsyncTask =new MyAsyncTask(activity,searchText);
        myAsyncTask.execute(searchText);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity= (Activity) context;
        if(myAsyncTask!=null){
            myAsyncTask.onAttach(context);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(myAsyncTask!=null){
            myAsyncTask.onDetach();
        }

    }
}