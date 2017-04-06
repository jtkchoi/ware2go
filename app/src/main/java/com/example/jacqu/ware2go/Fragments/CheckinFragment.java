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
 * Fragment to handle user check-in view
 */

public class CheckinFragment extends Fragment {
    String pid;
    String idnum;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
       return inflater.inflate(R.layout.check_in_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //Get variables to add onClickListeners / dynamically set attributes
        final ListView myListView = (ListView) view.findViewById(R.id.pickdevice);
        final Button myButton = (Button) view.findViewById(R.id.visit);
        final MainActivity ma = (MainActivity) this.getActivity();
        ma.listBt(view);

        //Display meaningful text
        TextView tv1 = new TextView(this.getContext());
        tv1.setText("Select your device and press broadcast.");
        myListView.addHeaderView(tv1);
        myButton.setText("Check in to " + ma.getBldgName());

        //Set onClickListener of listview to connect to bluetooth device selected
        //Also shows button and hides listview when successfully connected
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

        //When button clicked to receive prize, we get the id from the de2 and send it to the servere
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pid = ma.ReadFromBTDevice();
                idnum = pid.replaceAll("[^0-9]", "");
                ma.send_location(idnum);
                myButton.setVisibility(View.INVISIBLE);
            }
        });

    }
}
