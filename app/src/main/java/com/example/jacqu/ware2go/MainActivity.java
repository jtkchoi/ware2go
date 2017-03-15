package com.example.jacqu.ware2go;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.jacqu.ware2go.Fragments.CheckinFragment;
import com.example.jacqu.ware2go.Fragments.MapFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "BTTAG";
    private BluetoothAdapter mBluetoothAdapter;
    private Context context;
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
    private boolean Connected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        if (id == R.id.action_switchuser) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;

        if (id == R.id.nav_map) {
            fragment = new MapFragment();
        } else if (id == R.id.nav_checkin) {
            fragment = new CheckinFragment();
        }else if (id == R.id.nav_assist) {
            fragment = null;
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

    public void send_location(View view) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://192.168.43.72:3000/visited";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
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
                params.put("user_id", "3");
                params.put("location_id", "1");

                return params;
            }
        };
        queue.add(postRequest);
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

    void closeConnection() {
        try {
            mmInStream.close();
            mmInStream = null;
        } catch (IOException ignored) {}
        try {
            mmOutStream.close();
            mmOutStream = null;
        } catch (IOException ignored) {}
        try {
            mmSocket.close();
            mmSocket = null;
        } catch (IOException ignored) {}

        Connected = false ;
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
            Toast.makeText(context, "Connection Made", Toast.LENGTH_LONG).show();
        }
        catch (IOException connectException) {
            Toast.makeText(context, "Connection Failed", Toast.LENGTH_LONG).show();
            return;
        }

        //create the input/output stream and record fact we have made a connection
        GetInputOutputStreamsForSocket(); // see page 26
        Connected = true ;
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

        try { // Read from the InputStream using polling and timeout
            for(int i = 0; i < 200; i ++) { // try to read for 2 seconds max
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }
}
