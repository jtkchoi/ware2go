package com.example.jacqu.ware2go.Fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.jacqu.ware2go.MainActivity;
import com.example.jacqu.ware2go.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import static android.view.Gravity.CENTER;

/**
 * Created by Bo on 2017-03-30.
 */

public class JourneyFragment extends Fragment {
    String pid;
    View mapFrame;
    PopupWindow popup;
    ViewGroup c;
    LayoutInflater li;
    LinkedList<LatLng> latLngList;
    static String JOURNEYMSG = "JOURNEYMSG";

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        c = container;
        li = inflater;

        return inflater.inflate(R.layout.btcomms, container, false);

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

                // uncomment
                ma.findViewById(R.id.pickdevice).setVisibility(View.INVISIBLE);
                ma.findViewById(R.id.visit).setVisibility(View.VISIBLE);
            }
        });
        /*ArrayAdatper<String> myPairedArrayAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1);
        myPairedArrayAdapter.add("BT ID: 00001");
        */
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                pid = ma.ReadFromBTDevice();

//                Log.v(JOURNEYMSG, "Got data from BT: " + pid);


                pid = "$PMTKLOX,0,43*6E\r\r\n" +
                        "$PMTKLOX,1,0,0100010B,1F000000,0F000000,0000100B,00000000,7FFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,\r\n" +
                        "FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFF" +
                        "FF,00FC8C1C,0B37464F,027FD670,42DC9EC6,4113007A,1A37464F,027FD670,42DC9EC6,4113006B*25\r\n" +
                        "$PMTKLOX,1,1,2037464F,027FD670,42DD9EC6,41130050,2437464F,027FD670,42DD9EC6,41130054,2837464F," +
                        "027FD670,42DD9EC6,41130058,2B37464F,027FD670,42DD9E" +
                        "C6,4113005B,2E37464F,027FD670,42DD9EC6,4113005E,3D37464F,027FD670,42DC9EC6,4113004C*59\r\n" +
                        "$PMTKLOX,1,2,4C37464F,027FD670,42DC9EC6,4113003D,5B37464F,027FD670,42DC9EC6,4113002A,6A37464" +
                        "F,027FD670,42DD9EC6,4113001A,7937464F,027FD670,42DD9E" +
                        "C6,41130009,8837464F,027FD670,42DD9EC6,411300F8,9737464F,027FD670,42DD9EC6,411300E7*5C\r\n" +
                        "$PMTKLOX,1,3,A637464F,027FD670,42DD9EC6,411300D6,B537464F,027FD670,42DD9EC6,411300C5,C437464" +
                        "F,027FD670,42DD9EC6,411300B4,D337464F,027FD670,42DD9E" +
                        "C6,411300A3,E237464F,027FD670,42DD9EC6,41130092,F137464F,027FD670,42DD9EC6,41130081*59\r\n" +
                        "$PMTKLOX,1,4,0038464F,027FD670,42DD9EC6,4113007F,0F38464F,027FD670,42DC9EC6,41130071,1E38464F," +
                        "027FD670,42DC9EC6,41130060,2D38464F,027FD670,42DC9E" +
                        "C6,41130053,3C38464F,027FD670,42DC9EC6,41130042,4B38464F,027FD670,42DD9EC6,41130034*58\r\n" +
                        "$PMTKLOX,1,5,5A38464F,027FD670,42DD9EC6,41130025,6938464F,027FD670,42DC9EC6,41130017,7838464F" +
                        ",027FD670,42DC9EC6,41130006,8738464F,027FD670,42DC9E" +
                        "C6,411300F9,9638464F,027FD670,42DC9EC6,411300E8,A538464F,027FD670,42DD9EC6,411300DA*5D\r\n" +
                        "$PMTKLOX,1,6,B438464F,027FD670,42DC9EC6,411300CA,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,F" +
                        "FFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFF" +
                        "FF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF*58\r\n" +
                        "$PMTKLOX,1,7,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFF" +
                        "FFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFF" +
                        "FFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF*58\r\n" +
                        "$PMTKLOX,2*47\r\n" +
                        "$PMTK001,622,3*36\r\n";

                processGpsLog(pid);

                latLngList = new LinkedList<LatLng>();
                latLngList.add(new LatLng(49.267935, -123.258235));
                latLngList.add(new LatLng(49.266975, -123.257329));

                ma.setJourneyLatLng(latLngList);

                mapFrame = li.inflate(R.layout.map_journey, c, false);
                popup = new PopupWindow(ma);
                popup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                popup.setContentView(mapFrame);
                popup.setWidth(1920);
                popup.setHeight(1000);
                popup.showAtLocation(c, CENTER, 0, 200);


            }
        });

    }

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

    private void processGpsLog(String gpslog){
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
                                // print the data
                                // TODO: more productive stuff with it
                                if (timeint > 1300000000 && latfloat >= -180 && latfloat <= 180 && lonfloat >= -180 && lonfloat <= 180) {
                                    Log.v("GPSLOGDATA:", "time: " + timeint + " timedate: " + timedate + " lat: " + latfloat + " lon: " + lonfloat);
                                }
                            }
                        }

                    }

                }
            }
        }
    }
}
