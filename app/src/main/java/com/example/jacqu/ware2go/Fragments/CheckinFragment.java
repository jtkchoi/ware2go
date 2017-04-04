package com.example.jacqu.ware2go.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jacqu.ware2go.MainActivity;
import com.example.jacqu.ware2go.R;

/**
 * A simple {@link Fragment} subclass.
 */

public class CheckinFragment extends Fragment {
    String pid;
    String idnum;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
       return inflater.inflate(R.layout.btcomms, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        final ListView myListView = (ListView) view.findViewById(R.id.pickdevice);
        final Button myButton = (Button) view.findViewById(R.id.visit);

        ((MainActivity) this.getActivity()).listBt(view);

        final MainActivity ma = (MainActivity) this.getActivity();
        TextView tv1 = new TextView(this.getContext());
        tv1.setText("Select your device and press broadcast.");
        myListView.addHeaderView(tv1);
        myButton.setText("Check in to " + ma.getBldgName());


        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ma.connectFromListView(position-1);
                if(ma.getConnected()) {
                    ma.findViewById(R.id.pickdevice).setVisibility(View.INVISIBLE);
                    ma.findViewById(R.id.visit).setVisibility(View.VISIBLE);
                }
            }
        });

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pid = ma.ReadFromBTDevice();
                idnum = pid.replaceAll("[^0-9]", "");
                ma.WriteToBTDevice("Send a reward here.");
                ma.send_location(idnum);
            }
        });

    }
}
