package com.example.jacqu.ware2go.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.jacqu.ware2go.MainActivity;
import com.example.jacqu.ware2go.R;

/**
 * A simple {@link Fragment} subclass.
 */

public class CheckinFragment extends Fragment {
    String pid;

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
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                myListView.setVisibility(View.VISIBLE);
                myButton.setVisibility(View.INVISIBLE);
                ma.connectFromListView(position);
                pid = ma.ReadFromBTDevice();
                Log.d("Got from BT: ", pid);
            }
        });
        /*ArrayAdatper<String> myPairedArrayAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1);
        myPairedArrayAdapter.add("BT ID: 00001");
        */
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ma.WriteToBTDevice("Send a reward here.");
            }
        });

    }
}
