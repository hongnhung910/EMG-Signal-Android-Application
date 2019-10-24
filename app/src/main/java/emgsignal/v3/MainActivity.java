package emgsignal.v3;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;


    //private static EditText textView;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;   // The BluetoothAdapter is required for any and all Bluetooth activity
    private Button btnConnectDisconnect,btnSaveData,btnClose,btnReset;
    private DeviceListActivity deviceListActivity;
    byte[] txValue;
    int fs=1000;
    double[] emg = new double[30];


    private LineGraphSeries<DataPoint> series_maternal;
    private double lastX1 = 0;

    boolean isRunning = false;
    boolean isSaving  = false;

    ArrayList<Double> data1Save = new ArrayList();

    IIR_Filter filter = new IIR_Filter();

    double[] filter_input1 =  {0, 0, 0, 0, 0, 0, 0, 0, 0,0,0};
    double[] filter_output1 = {0, 0, 0, 0, 0, 0, 0, 0,0,0};

    double[] filter_input_for_envelope =  {0, 0, 0, 0, 0};
    double[] filter_output_for_envelope = {0, 0, 0, 0};

    SaveData saveData = new SaveData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Handle menu section
        {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            NavigationView navigationView = findViewById(R.id.nav_view);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);
        }

        deviceListActivity = new DeviceListActivity();

        initGraphMaternal();

        btnSaveData = findViewById(R.id.btn_saveData);
        btnClose = findViewById(R.id.btn_close);
        btnReset = findViewById(R.id.btn_reset);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter(); // lay gia tri default ban dau la null

        // vơi gia tri ban dau la null, bluetooth khong hoat dong
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnConnectDisconnect=  findViewById(R.id.btn_connect);
        service_init();

        // Handle Save data function
        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(data1Save.size() == 0)
                { Toast.makeText(MainActivity.this, "No data available yet.", Toast.LENGTH_SHORT).show();}
                else {
                    isSaving = true;
                    isRunning = false;
                    saveData.save(data1Save);
                    Toast.makeText (MainActivity.this, "Saved", Toast.LENGTH_SHORT).show ();
                    isSaving = false;
                    isRunning = true;
                    mService.disconnect();
                    data1Save.clear();
                    series_maternal.resetData(new DataPoint[]{});
                    initGraphMaternal();
                    btnConnectDisconnect.setText("Connect");
                }
            }
        });


        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {

                    if (btnConnectDisconnect.getText().equals("Connect")){
                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class); // chuyen qua device list activity
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);

                    } else {
                        //Disconnect button pressed
                        mService.disconnect();
                    }
                }
            }
        });

        //Close App
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = false;
                mService.disconnect();
                series_maternal.resetData(new DataPoint[]{});
                initGraphMaternal();
                btnConnectDisconnect.setText("Connect");
                /*mService.initialize();*/

            }
        });
    }

    private void resetData(){
        isRunning = false;
        data1Save.clear();

    }

    private void initGraphMaternal(){
        // we get graph view instance
        GraphView graph =  findViewById(R.id.graph);

        series_maternal = new LineGraphSeries();
        series_maternal.setColor(Color.BLUE);
        graph.addSeries(series_maternal);

        Viewport viewport = graph.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);    // set de test 0
        viewport.setMaxY(5);     // set 4500
        viewport.setMinX(0);
        viewport.setMaxX(10000);
        viewport.setScrollable(true);
        viewport.setScalable(true);

        graph.getGridLabelRenderer().setNumHorizontalLabels(10);
        graph.getGridLabelRenderer().setNumVerticalLabels(10);
        //graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        //graph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX) + "V";
                }
            }
        });
        graph.setTitle("EMG Signal");
    }



    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }
        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;

        }
    };
    // ham doc du lieu la ham nay day:
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();

            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("Disconnect");
                        if(!isSaving) {Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();}
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }


            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("Connect");
                        if(!isSaving) {Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_LONG).show();}

                        mService.close();mState = UART_PROFILE_DISCONNECTED;
                        isRunning = false;
                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            // ham nay la ham nhan a xu l
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                Log.d(TAG,":"+txValue.length);
                // firstDataBuffer = new double[3000];
                for (int i = 0; i < 30; i++) {
                    emg[i] = (txValue[i*2]&0xff&0x3f) + (txValue[i*2+1]&0xff&0x3f)*64;
                    // for 50Hz filter

                    filter_input1 = filter.update_input_filter_array50Hz(filter_input1, emg[i]);
                    double filtered_point_emg = filter.filter50Hz(filter_input1, filter_output1);
                    filter_output1 = filter.update_output_filter_array50Hz(filter_output1, filtered_point_emg);

                    filtered_point_emg=filtered_point_emg-1650;
                    if(filtered_point_emg<0)
                    {
                        filtered_point_emg=-filtered_point_emg;
                    }

                    // for envelope low pass 10Hz
                    filter.update_input_filter_array10Hz(filter_input_for_envelope,filtered_point_emg);
                    double filter_out_putpoint_envelope=filter.filter10Hz(filter_input_for_envelope,filter_output_for_envelope);
                    filter_output_for_envelope=filter.update_output_filter_array10Hz(filter_output_for_envelope,filter_out_putpoint_envelope);

                    // IIR Bandpass notchpass filter
                    filter_out_putpoint_envelope = filter_out_putpoint_envelope/1000;
                    data1Save.add(filter_out_putpoint_envelope);     // fill save array for ECG signal raw data

                    // plot the filtered data points or raw data
                    lastX1=lastX1+  1/fs;
                    series_maternal.appendData(new DataPoint(lastX1,filter_out_putpoint_envelope), true, 10000);
                    Log.d(TAG, lastX1++ + ", " + filter_out_putpoint_envelope);
                }
                //      }
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                if(!isSaving) {
                    showMessage("Device doesn't support UART. Disconnecting");
                    mService.disconnect();
                }
            }
        }

    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }



    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.REQUEST_LOCATION_ENABLE_CODE);
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Your devices that don't support BLE", Toast.LENGTH_LONG).show();
            finish();
        }
        if (!mBtAdapter.enable()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, Constants.REQUEST_BLUETOOTH_ENABLE_CODE);
        }
        registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
        if (mService != null) {
            // final boolean result = mService.connect(mDevice.getAddress());
            //  Log.d(TAG, "Connect request result=" + result);
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
            {
                if (resultCode == Activity.RESULT_OK && data != null) {

                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    //  ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    mService.connect(deviceAddress);
                    Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"Connect Success");
                    resetData();
                    isRunning = true;

                }
                if (resultCode == Activity.RESULT_CANCELED)
                {
                    initGraphMaternal();
                    series_maternal.resetData(new DataPoint[]{});
                }
            }
            break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
            {

                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    //finish();
                }
                if (resultCode == Activity.RESULT_CANCELED)
                    Toast.makeText(MainActivity.this,"No device choosen",Toast.LENGTH_SHORT).show();
            }
            break;

            //case CREATE_REQUEST_CODE:
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.popup_title1)
                    .setMessage(R.string.popup_message)
                    .setNegativeButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setPositiveButton(R.string.popup_no, null)
                    .show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Handle menu section <
    /*@Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }*/

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_home) {

        } else if (id == R.id.menu_saved_data) {
            Intent intent = new Intent(MainActivity.this, ListFilesActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //> Handle menu section

}
