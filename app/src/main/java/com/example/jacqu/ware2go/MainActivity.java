package com.example.jacqu.ware2go;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.jacqu.ware2go.Fragments.AssistanceFragment;
import com.example.jacqu.ware2go.Fragments.CheckinFragment;
import com.example.jacqu.ware2go.Fragments.JourneyFragment;
import com.example.jacqu.ware2go.Fragments.MapFragment;
import com.example.jacqu.ware2go.Fragments.UserPKFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.CENTER_HORIZONTAL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "BTTAG";
    private BluetoothAdapter mBluetoothAdapter;
    private Context context = this;
    private TextView text;
    private Button onBtn, offBtn, listBtn, findBtn, dongleBtn;
    private ListView myListView;
    private ArrayAdapter<String> myDiscoveredArrayAdapter;
    private ArrayAdapter<String> myPairedArrayAdapter;
    private ArrayList <BluetoothDevice> Discovereddevices;
    private BluetoothSocket mmSocket = null;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<BluetoothDevice> indexDevices;
    public static InputStream mmInStream = null;
    public static OutputStream mmOutStream = null;
    private boolean connected = false;
    public static final String PREFS_NAME = "MyPrefsFile";
    private JSONArray bldgInfo = null;
    private LatLng curLocation = new LatLng(49.2677982, -123.2564914);
    private int curAssistanceID = -1;
    private LinkedList<LatLng> journeyLatLng;
    int bldgID;

    public LinkedList<LatLng> getJourneyLatLng() {
        return journeyLatLng;
    }

    public void setJourneyLatLng(LinkedList<LatLng> journeyLatLng) {
        this.journeyLatLng = journeyLatLng;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //NOTE:  Checks first item in the navigation drawer initially
        navigationView.setCheckedItem(R.id.nav_map);
        //NOTE:  Open fragment1 initially.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, new MapFragment());
        ft.commit();

        get_locations(new VolleyCallback() {
            @Override
            public void onSuccessResponse(Object result) {
               bldgInfo = (JSONArray) result;
                try {
                    curLocation = new LatLng(
                            bldgInfo.getJSONObject(bldgID).getDouble("latitude"),
                            bldgInfo.getJSONObject(bldgID).getDouble("longitude")
                    );
                }
                catch (Exception JSONException){
                    curLocation = new LatLng(0,0);
                }
            }
        });

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        bldgID = settings.getInt("bldgID", 0);
    }

    public LatLng getCurLocation(){
        return curLocation;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if( fm.getBackStackEntryCount() != 0){
            fm.popBackStack();
        }
        else {
            if(currFragment != R.id.nav_map) {
                currFragment = R.id.nav_map;
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.mainFrame, new MapFragment());
                ft.commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        View menuItemView = findViewById(R.id.mainFrame);
        if (id == R.id.action_changelocation) {

            final PopupWindow popup = new PopupWindow(this);
            popup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            popup.setWidth(750);
            popup.setHeight(750);
            RelativeLayout frame = new RelativeLayout(this);
            ListView lv = new ListView(this.context);
            ArrayAdapter<String> lvBldgs = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

            //Just for when server is off
            if(bldgInfo == null) {
                lvBldgs.add("1");
                lvBldgs.add("2");
                lvBldgs.add("3");
                lvBldgs.add("4");
                lv.setAdapter(lvBldgs);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        popup.dismiss();
                    }
                });
                frame.addView(lv);
                popup.setFocusable(true);
                popup.setContentView(frame);
                popup.showAtLocation(this.getCurrentFocus(), CENTER, 0, 100);
                return false;
            }

            for(int i = 0; i < bldgInfo.length(); i++){
                JSONObject t;
                String bldgname;
                try {
                    t = bldgInfo.getJSONObject(i);
                    bldgname = t.getString("name");
                }
                catch (Exception JSONException){
                    break;
                }
                lvBldgs.add(bldgname);
            }


            lv.setAdapter(lvBldgs);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    try {
                        bldgID = bldgInfo.getJSONObject(position).getInt("id");
                        curLocation = new LatLng(
                                    bldgInfo.getJSONObject(bldgID).getDouble("latitude"),
                                    bldgInfo.getJSONObject(bldgID).getDouble("longitude")
                            );
                    }
                    catch (Exception JSONException){
                        curLocation = new LatLng(0,0);
                        bldgID = 0;
                    }

                    popup.dismiss();
                }
            });
            frame.addView(lv);
            popup.setFocusable(true);
            popup.setContentView(frame);
            popup.showAtLocation(this.getCurrentFocus(), CENTER_HORIZONTAL, 40, 200);

        }

        return false;
    }

    int currFragment = R.id.nav_map;
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;

        if (id == R.id.nav_map) {
            currFragment = R.id.nav_map;
            fragment = new MapFragment();
        } else if (id == R.id.nav_checkin) {
            currFragment = R.id.nav_checkin;
            fragment = new CheckinFragment();
        }else if (id == R.id.nav_assist) {
            currFragment = R.id.nav_assist;
            fragment = new AssistanceFragment();
        }else if (id == R.id.GPS_Log) {
            currFragment = R.id.GPS_Log;
            fragment = new JourneyFragment();
        }else if (id == R.id.ChangeUserPK) {
            currFragment = R.id.ChangeUserPK;
            fragment = new UserPKFragment();
        }
        //NOTE: Fragment changing code
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void get_locations(final VolleyCallback callback) {
        String url = "http://192.168.43.72:3000/locations";
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            callback.onSuccessResponse(jsonArray);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
            }
        });
        ApplicationController.getInstance().addToRequestQueue(getRequest);
    }

    public void get_users(final VolleyCallback callback) {
        //TODO: fill function
        String url = "http://192.168.43.72:3000/assistances";
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            callback.onSuccessResponse(jsonArray);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
            }
        });
        ApplicationController.getInstance().addToRequestQueue(getRequest);
    }

    public void get_buildings(final VolleyCallback callback) {
        //TODO: fill function
        String url = "http://192.168.43.72:3000/locations";
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            callback.onSuccessResponse(jsonArray);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
            }
        });
        ApplicationController.getInstance().addToRequestQueue(getRequest);
    }

    public void send_location(final String id) {
        final TextView tv = (TextView) this.findViewById(R.id.server_msg);
        String url = "http://192.168.43.72:3000/visited";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                        tv.setText(response);
                        tv.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("user_id", id);
                params.put("location_id", ((Integer) bldgID).toString());

                return params;
            }
        };
        ApplicationController.getInstance().addToRequestQueue(postRequest);
    }

    public void setAssistanceUser(int assistanceID){
        this.curAssistanceID = assistanceID;
    }

    public int getAssistanceUser(){
        return this.curAssistanceID;
    }

    public void btOn(View view){
        Log.v(TAG, "Pushed BT on");
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
                Log.v(TAG, "BT is now on");
            } else {
                Log.v(TAG, "BT is already on");
            }
        } else {
            Log.v(TAG, "No BT device!");
        }
    }

    public void btOff(View view){
        return;
    }

    public void listBt( View v ){
        myPairedArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ArrayAdapter<String> pairedDevicesArray = null;
        Log.v(TAG, "Pushed list BT");
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        indexDevices = new ArrayList<BluetoothDevice>();
        myPairedArrayAdapter.clear();
        for(BluetoothDevice device : pairedDevices){
            indexDevices.add(device);
            myPairedArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
            Toast.makeText(getApplicationContext(),"Select your device",Toast.LENGTH_SHORT).show();
        }
        ((ListView) v.findViewById(R.id.pickdevice)).setAdapter(myPairedArrayAdapter);
    }

    public void connectFromListView( int index ){
        BluetoothDevice btd = indexDevices.get(index);
        CreateSerialBluetoothDeviceSocket(btd);
        ConnectToSerialBlueToothDevice();
    }

    public void connectToDongle(View view){
        Log.v(TAG, "Pushed Connect to BT dongle");
    }



    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TAG, "OnReceive Action " + action);
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                Toast.makeText(context, deviceName, Toast.LENGTH_LONG).show();

                Log.v(TAG, "Found BT name: " + deviceName);
                Log.v(TAG, "Found BT HW Addr: " + deviceHardwareAddress);
                Discovereddevices.add(device);
                myDiscoveredArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                myDiscoveredArrayAdapter.notifyDataSetChanged();
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) ) {
                Log.v(TAG, "Discovery Finsihed");
                Toast.makeText(context, "Discovery Finished", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void findBt(View view){
        Log.v(TAG, "Pushed find BT");
        if (mBluetoothAdapter.isDiscovering()){
            Log.v(TAG, "Cancel find BT");
            mBluetoothAdapter.cancelDiscovery();
        } else {
            myDiscoveredArrayAdapter.clear();
            Log.v(TAG, "Start find BT");
            mBluetoothAdapter.startDiscovery();
        }
    }
    public void CreateSerialBluetoothDeviceSocket(BluetoothDevice device) {
        mmSocket = null;

        // universal UUID for a serial profile RFCOMM blue tooth device
        // this is just one of those “things” that you have to do and just works
        UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        // Get a Bluetooth Socket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
                mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Toast.makeText(context, "Socket Creation Failed", Toast.LENGTH_LONG).show();
        }
    }

    public void closeConnection() {
        try {
            mmInStream.close();
            mmInStream = null;
        } catch (Exception ignored) {}
        try {
            mmOutStream.close();
            mmOutStream = null;
        } catch (Exception ignored) {}
        try {
            mmSocket.close();
            mmSocket = null;
        } catch (Exception ignored) {}

        connected = false ;
    }


    // this call back function is run when an activity that returns a result ends.
    // Check the requestCode (given when we start the activity) to identify which
    // activity is returning a result, and then resultCode is the value returned
    // by the activity. In most cases this is RESULT_OK. If not end the activity
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        if( requestCode == REQUEST_ENABLE_BT) // was it the “enable bluetooth” activity?
            if( resultCode != RESULT_OK ) { // if so did it work OK?
                Toast toast = Toast.makeText(context, "BlueTooth Failed to Start ",
                        Toast.LENGTH_LONG);
                toast.show();
                finish();
            }
    }

    public void ConnectToSerialBlueToothDevice() {
        // Cancel discovery because it will slow down the connection
        try {
            // Attempt connection to the device through the socket.
            mmSocket.connect();
            ((MainActivity)this.context).findViewById(R.id.pickdevice).setVisibility(View.INVISIBLE);
            ((MainActivity)this.context).findViewById(R.id.visit).setVisibility(View.VISIBLE);

            Toast.makeText(context, "Connection Made", Toast.LENGTH_LONG).show();
        }
        catch (IOException connectException) {
            Toast.makeText(context, "Connection Failed", Toast.LENGTH_LONG).show();
            return;
        }

        //create the input/output stream and record fact we have made a connection
        GetInputOutputStreamsForSocket(); // see page 26
        connected = true ;
    }

    // gets the input/output stream associated with the current socket
    public void GetInputOutputStreamsForSocket() {
        try {
            mmInStream = mmSocket.getInputStream();
            mmOutStream = mmSocket.getOutputStream();
        } catch (IOException ignored) {
            Log.v(TAG, "IOException caught in GetInputOutputStreamsForSocket");
        }
    }

    // This function write a line of text (in the form of an array of bytes)
    // to the Bluetooth device and then sends the string “\r\n”
    // (required by the bluetooth dongle)
    //
    public void WriteToBTDevice (String message) {
        String s = "\r\n";
        byte[] msgBuffer = message.getBytes();
        byte[] newline = s.getBytes();

        try {
            mmOutStream.write(msgBuffer) ;
            mmOutStream.write(newline) ;
        } catch (IOException ignored) {
            Log.v(TAG, "Caught IOExcpetion in WriteBT");
        }
    }

    // This function reads a line of text from the Bluetooth device
    public String ReadFromBTDevice()
    {
        byte c ;
        String s = "";
        if(mmSocket == null){
            return s;
        }
        try { // Read from the InputStream using polling and timeout
            for(int i = 0; i < 1000; i ++) { // try to read for 2 seconds max
                SystemClock.sleep (10);
                if( mmInStream.available () > 0) {
                    if((c = (byte) mmInStream.read ()) != '\r') // '\r' terminator
                        s += (char)c; // build up string 1 byte by byte
                    else
                        return s;
                }
            }
        } catch (IOException e) {
            return "-- No Response --";
        }
        return "Something wrong";
    }


    public String swapEndian(String args) {
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

    public void processGpsLog(String gpslog){
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(bReceiver != null) {
                unregisterReceiver(bReceiver);
            }
        } catch (NullPointerException e) {
            Log.v(TAG, "Already unregistered");
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("bldgID", bldgID);

        // Commit the edits!
        editor.commit();
    }

}


