package com.example.jacqu.ware2go;

import android.util.Log;

import org.junit.Test;

import static org.junit.Assert.*;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Unit Tests for Ware2GO
 */
public class ExampleUnitTest {
    @Test
    public void test_gps_parser() throws Exception {
        List<LatLng> latLngList = new LinkedList<LatLng>;
        String gps_line = "$PMTKLOX,1,3,A637464F,027FD670,42DD9EC6,411300D6,B537464F,027FD670,42DD9EC6,411300C5,C437464F,027FD670,42DD9EC6,411300B4,D337464F,027FD670,42DD9EC6,411300A3,E237464F,027FD670,42DD9EC6,41130092,F137464F,027FD670,42DD9EC6,41130081*59\r\n" +
                "$PMTKLOX,1,4,0038464F,027FD670,42DD9EC6,4113007F,0F38464F,027FD670,42DC9EC6,41130071,1E38464F,027FD670,42DC9EC6,41130060,2D38464F,027FD670,42DC9EC6,41130053,3C38464F,027FD670,42DC9EC6,41130042,4B38464F,027FD670,42DD9EC6,41130034*58";
        processGpsLog(gps_line, latLngList);
        assertTrue(latLngList.size() > 0);
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