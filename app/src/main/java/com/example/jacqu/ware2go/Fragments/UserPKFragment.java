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
import android.util.Log;

import com.example.jacqu.ware2go.MainActivity;
import com.example.jacqu.ware2go.R;

/**
 * A simple {@link Fragment} subclass.
 */

public class UserPKFragment extends Fragment {
    static String USERPKLOG = "USERPK";
    TextView userpkfield;
    String pid;
    String idnum;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
       return inflater.inflate(R.layout.set_user_pk, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        final ListView myListView = (ListView) view.findViewById(R.id.pickdevice);
        final Button sendPkButton = (Button) view.findViewById(R.id.senduserpk);
        final TextView curpkfield = (TextView) view.findViewById(R.id.curuserpk);
        final TextView userpkfield = ((TextView) view.findViewById(R.id.setuserpkfield));
        ((MainActivity) this.getActivity()).listBt(view);

        final MainActivity ma = (MainActivity) this.getActivity();
        ma.closeConnection();
        TextView tv1 = new TextView(this.getContext());
        tv1.setText("Select your device and press \"Change ID\" on the Ware2GO.");
        myListView.addHeaderView(tv1);


        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                ma.connectFromListView(position-1);
                String curPK = "Something wrong";
                for(int i = 0; curPK == "Something wrong" && i < 10; i++) {
                    curPK = ma.ReadFromBTDevice();
                }
                curpkfield.setText("Current ID: " + curPK);
                if(ma.getConnected()) {
                    myListView.setVisibility(View.INVISIBLE);
                    sendPkButton.setVisibility(View.VISIBLE);
                    userpkfield.setVisibility(View.VISIBLE);
                    curpkfield.setVisibility(View.VISIBLE);
                }
            }
        });

        sendPkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userpktext = userpkfield.getText().toString();
                Log.v(USERPKLOG, "Sending user pk " + userpktext);
                ma.WriteToBTDevice("setuserpk," + userpktext);
            }
        });

    }
}
