package com.example.jacqu.ware2go.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jacqu.ware2go.MainActivity;
import com.example.jacqu.ware2go.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bo on 2017-03-30.
 *
 * Code used to manage the Fragement of handling plotting the GPS log sent from DE2 to Android
 */

public class JourneyFragment extends Fragment {
    String gpsLog;
    LinkedList<LatLng> latLngList;
    static String JOURNEYMSG = "JOURNEYMSG";

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.journey_init, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        final ListView myListView = (ListView) view.findViewById(R.id.pickdevice);
        final Button myButton = (Button) view.findViewById(R.id.visit);

        ((MainActivity) this.getActivity()).listBt(view);

        final MainActivity ma = (MainActivity) this.getActivity();
        TextView tv1 = new TextView(this.getContext());
        tv1.setText("Select your Ware2GO device and press \"Send log!\"");
        myListView.addHeaderView(tv1);



        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // CONNECTING CODE
                Log.v(JOURNEYMSG, "connecting to index: " + position);
                ma.connectFromListView(position-1);

                ma.findViewById(R.id.pickdevice).setVisibility(View.INVISIBLE);
                ma.findViewById(R.id.visit).setVisibility(View.VISIBLE);
            }
        });

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsLog = ma.ReadLogFromBTDevice();

                Log.v(JOURNEYMSG, "Got log from BT: " + gpsLog);


                String replacedGpsLog = gpsLog.replace("rn", "\r\n");

                latLngList = new LinkedList<LatLng>();

                processGpsLog(replacedGpsLog, latLngList);

                ma.setJourneyLatLng(latLngList);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.mainFrame, new JourneyMapView());
                ft.commit();
            }
        });

    }

    /*
        Swap big and little Endian of the individual bytes of the GPS Log
     */
    private String swapEndian(String args) {
        if(args.length() % 2 != 0) return "";
        char[] argsArray = args.toCharArray();
        char[] newChar = new char[args.length()];
        int i = 0;
        int j = args.length()-2;
        while(i < args.length() && j >= 0) {
            newChar[i] = argsArray[j];
            newChar[i+1] = argsArray[j+1];
            i += 2;
            j -= 2;
        }
        String swapArgs = new String(newChar);
        return swapArgs;
    }

    /*
        This function processes the GPS log sent from the DE2 and will extract the list of Lattitude and Longitude
     */
    private void processGpsLog(String gpslog, List<LatLng> latLngList){
        String[] gpslines = gpslog.split("\r\n");
        for(int i = 0; i < gpslines.length; i++){
            // check if valid line
            if (gpslines[i].contains("*")){
                // break if last lines
                if (gpslines[i].contains("PMTKLOX,2") || gpslines[i].contains("$PMTK001")) {
                    break;

                } else if (gpslines[i].contains("$PMTKLOX,1,")) { // check if its a valid gps log line
                    // split by comma
                    String[] linesplit = gpslines[i].split(",");

                    if (gpslines[i].length() < 10){
                        continue;
                    }

                    // pop off the first 3 elements
                    ArrayList<String> stringArrayList = new ArrayList<String>(Arrays.asList(linesplit));
                    stringArrayList.remove(0);
                    stringArrayList.remove(0);
                    stringArrayList.remove(0);

                    // remove * from last element
                    String lastelement = stringArrayList.get(stringArrayList.size()-1);
                    stringArrayList.set(stringArrayList.size()-1, lastelement.split("\\*")[0]);

                    // convert back to one string
                    String lineString;
                    if (stringArrayList.size() > 0) {
                        StringBuilder sB = new StringBuilder();
                        for (String n : stringArrayList) {
                            sB.append(n);
                        }
                        lineString = sB.toString();
                    } else {
                        lineString = "";
                    }

                    // get length and verify divisble by 32
                    int len = lineString.length();
                    if (len % 32 != 0){
                        System.out.println("bad line encountered");
                        continue;
                    }

                    // cut line into 32 byte chunks
                    int chunks = lineString.length() / 32;
                    LinkedList<String> chunkList = new LinkedList<String>();

                    for(int j = 0; j < chunks; j++){
                        String chunk = lineString.substring(32 * j, 32 * (j + 1));
                        chunkList.add(chunk);
                    }

                    // parse each chunk
                    for (String chunk : chunkList){
                        String timeString = chunk.substring(0, 8);
                        String latString = chunk.substring(10, 18);
                        String lonString = chunk.substring(18, 26);

                        timeString = swapEndian(timeString);
                        latString = swapEndian(latString);
                        lonString = swapEndian(lonString);

                        // gps will send zero values if no such data exists
                        if ( !(timeString.equals("FFFFFFFF") || latString.equals("FFFFFFFF") || lonString.equals("FFFFFFFF")) ) {
                            // parse time, lat, lon
                            long timeint = 0;
                            Date timedate = new Date();
                            double latfloat = 0.0;
                            double lonfloat = 0.0;
                            boolean success = true;

                            // exception means there was probably something wrong with the data so we just ignore it
                            try {
                                timeint = Long.parseLong(timeString, 16);
                                latfloat = Float.intBitsToFloat((int)Long.parseLong(latString, 16));
                                lonfloat = Float.intBitsToFloat((int)Long.parseLong(lonString, 16));
                                timedate.setTime(timeint * 1000);
                            } catch (Exception e) { // bad data
                                success = false;
                            }
                            // good data
                            if (success) {
                                // store data in LatLng list for plotting on map
                                if (timeint > 1300000000 && latfloat >= -180 && latfloat <= 180 && lonfloat >= -180 && lonfloat <= 180) {
                                    Log.v("GPSLOGDATA:", "time: " + timeint + " timedate: " + timedate + " lat: " + latfloat + " lon: " + lonfloat);
                                    latLngList.add(new LatLng(latfloat, lonfloat));
                                }
                            }
                        }

                    }

                }
            }
        }
    }
}
