package com.example.jacqu.ware2go.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jacqu.ware2go.R;

/**
 * Created by jacqu on 3/2/2017.
 * This view solely exists to act as a parent for the main map view
 */

public class MapFragment extends Fragment{

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_main, container, false);
    }
}