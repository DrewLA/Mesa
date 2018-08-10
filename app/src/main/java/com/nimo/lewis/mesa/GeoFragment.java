package com.nimo.lewis.mesa;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GeoFragment extends Fragment {
    public static GeoFragment newInstance(){
        return new GeoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_geo, container, false);
        return view;
    }

}
