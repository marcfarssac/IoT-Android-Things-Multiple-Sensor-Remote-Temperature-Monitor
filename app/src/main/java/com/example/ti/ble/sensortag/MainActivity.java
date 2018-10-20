/**************************************************************************************************
  Filename:       MainActivity.java
  Revised:        $Date: 2014-01-02 18:55:00 +0100 (to, 02 jan 2014) $
  Revision:       $Revision: 28743 $

  Copyright (c) 2013 - 2014 Texas Instruments Incorporated

  All rights reserved not granted herein.
  Limited License.

  Texas Instruments Incorporated grants a world-wide, royalty-free,
  non-exclusive license under copyrights and patents it now or hereafter
  owns or controls to make, have made, use, import, offer to sell and sell ("Utilize")
  this software subject to the terms herein.  With respect to the foregoing patent
  license, such license is granted  solely to the extent that any such patent is necessary
  to Utilize the software alone.  The patent license shall not apply to any combinations which
  include this software, other than combinations with devices manufactured by or for TI ( TI Devices  ).
  No hardware patent is licensed hereunder.

  Redistributions must preserve existing copyright notices and reproduce this license (including the
  above copyright notice and the disclaimer and (if applicable) source code license limitations below)
  in the documentation and/or other materials provided with the distribution

  Redistribution and use in binary form, without modification, are permitted provided that the following
  conditions are met:

    * No reverse engineering, decompilation, or disassembly of this software is permitted with respect to any
      software provided in binary form.
    * any redistribution and use are licensed by TI for use only with TI Devices.
    * Nothing shall obligate TI to provide you with source code for the software licensed and provided to you in object code.

  If software source code is provided to you, modification and redistribution of the source code are permitted
  provided that the following conditions are met:

    * any redistribution and use of the source code, including any resulting derivative works, are licensed by
      TI for use only with TI Devices.
    * any redistribution and use of any object code compiled from the source code and any resulting derivative
      works, are licensed by TI for use only with TI Devices.

  Neither the name of Texas Instruments Incorporated nor the names of its suppliers may be used to endorse or
  promote products derived from this software without specific prior written permission.

  DISCLAIMER.

  THIS SOFTWARE IS PROVIDED BY TI AND TI   S LICENSORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL TI AND TI   S LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.


 **************************************************************************************************/
package com.example.ti.ble.sensortag;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.ti.ble.common.BleDeviceInfo;
import com.example.ti.ble.common.BluetoothLeService;
import com.example.ti.ble.common.HelpView;
import com.example.ti.io.LCD;
import com.example.ti.model.SensorData;
import com.example.ti.util.CustomToast;
import com.example.ti.util.SensorScan;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.example.ti.util.SensorScan.INIT;
import static com.example.ti.util.SensorScan.START;
import static com.example.ti.util.SensorScan.UPDATE_LCDS;

// import android.util.Log;

public class MainActivity extends ViewPagerActivity {
    // Log
    // private static final String TAG = "MainActivity";

    // URLs
    private static final Uri URL_FORUM = Uri
            .parse("http://e2e.ti.com/support/low_power_rf/default.aspx?DCMP=hpa_hpa_community&HQS=NotApplicable+OT+lprf-forum");
    private static final Uri URL_STHOME = Uri
            .parse("http://www.ti.com/ww/en/wireless_connectivity/sensortag/index.shtml?INTC=SensorTagGatt&HQS=sensortag");

    // Requests to other activities
    private static final int REQ_ENABLE_BT = 0;
    private static final int REQ_DEVICE_ACT = 1;

    // GUI
    private static MainActivity mThis = null;
    private ScanView mScanView;
    private Intent mDeviceIntent;
    private static final int STATUS_DURATION = 5;

    // BLE management
    private boolean mBtAdapterEnabled = false;
    private boolean mBleSupported = true;
    private boolean mScanning = false;
    private int mNumDevs = 0;
    private int mConnIndex = NO_DEVICE;
    private List<BleDeviceInfo> mDeviceInfoList, mDeviceScanedList;
    private static BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothLeService mBluetoothLeService = null;
    private IntentFilter mFilter;
    private String[] mDeviceFilter = null;

    // Housekeeping
    private static final int NO_DEVICE = -1;
    private boolean mInitialised = false;
    SharedPreferences prefs = null;

    // Automatic polling
    private boolean mPollDevices = true;
    private boolean mNewValues = false;
    private final long STATE_TIME = 1000; // Loop time between states
    private final long MAX_STATE_TIME = 30000; // Time after we consider that something went wrong
    private final long MAX_SCAN_TIME = 5000; // Time after we consider that all sensors have been read

    private static final String BUS_SENSOR_LED_01 = "BCM14"; //BUS_SensorLed_01
    private static final String BUS_SENSOR_LED_02 = "BCM15"; //BUS_SensorLed_02

    private Handler mHandler;
    private SensorScan state = START;

    private Gpio rs_lcd1, bl_lcd1, e_lcd1, d4_lcd1, d5_lcd1, d6_lcd1, d7_lcd1;
    private Gpio rs_lcd2, bl_lcd2, e_lcd2, d4_lcd2, d5_lcd2, d6_lcd2, d7_lcd2;
    private Gpio rs_lcd3, bl_lcd3, e_lcd3, d4_lcd3, d5_lcd3, d6_lcd3, d7_lcd3;

    private Gpio BUS_SensorLed_01, BUS_SensorLed_02;

    private LCD lcd1, lcd2, lcd3;
    long stateTime;

    public MainActivity() {
        mThis = this;
        mResourceFragmentPager = R.layout.fragment_pager;
        mResourceIdPager = R.id.pager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Start the application
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG)
                    .show();
            mBleSupported = false;
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to BluetoothAdapter through BluetoothManager.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = mBluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBtAdapter == null) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_LONG).show();
            mBleSupported = false;
        }

        // Initialize device list container and device filter
        mDeviceInfoList = new ArrayList<BleDeviceInfo>();
        mDeviceScanedList = new ArrayList<BleDeviceInfo>();
        Resources res = getResources();
        mDeviceFilter = res.getStringArray(R.array.device_filter);

        // Create the fragments and add them to the view pager and tabs
        mScanView = new ScanView();
        mSectionsPagerAdapter.addSection(mScanView, "BLE Device List");

        HelpView hw = new HelpView();
        hw.setParameters("help_scan.html", R.layout.fragment_help, R.id.webpage);
        mSectionsPagerAdapter.addSection(hw, "Help");

        // Register the BroadcastReceiver
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);

        initLCDs();
        initBusLed();
    }

    @Override
    public void onDestroy() {
        // Log.e(TAG,"onDestroy");
        super.onDestroy();
        if (mBluetoothLeService != null) {
            if (mScanning)
                scanLeDevice(false);
            unregisterReceiver(mReceiver);
            unbindService(mServiceConnection);
            mBluetoothLeService.close();
            mBluetoothLeService = null;
        }

        mBtAdapter = null;

        // Clear cache
        File cache = getCacheDir();
        String path = cache.getPath();
        try {
            Runtime.getRuntime().exec(String.format("rm -rf %s", path));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (BUS_SensorLed_01!=null) {
            try {
                BUS_SensorLed_01.setValue(false);
                BUS_SensorLed_01.close();
            } catch (IOException e) {

            }
        }

        if (BUS_SensorLed_02!=null) {
            try {
                BUS_SensorLed_02.setValue(false);
                BUS_SensorLed_02.close();
            } catch (IOException e) {

            }
        }

        if (lcd1!=null) {
            try {
                lcd1.setBackLight(false);
                lcd1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (lcd2!=null) {
            try {
                lcd2.setBackLight(false);
                lcd2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (lcd3!=null) {
            try {
                lcd3.setBackLight(false);
                lcd3.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.opt_bt:
                onBluetooth();
                break;
            case R.id.opt_e2e:
                onUrl(URL_FORUM);
                break;
            case R.id.opt_sthome:
                onUrl(URL_STHOME);
                break;
            case R.id.opt_license:
                onLicense();
                break;
            case R.id.opt_about:
                onAbout();
                break;
            case R.id.opt_exit:
                Toast.makeText(this, "Exit...", Toast.LENGTH_SHORT).show();
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void onUrl(final Uri uri) {
        Intent web = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(web);
    }

    private void onBluetooth() {
        Intent settingsIntent = new Intent(
                android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(settingsIntent);
    }

    private void onLicense() {
        final Dialog dialog = new LicenseDialog(this);
        dialog.show();
    }

    private void onAbout() {
        final Dialog dialog = new AboutDialog(this);
        dialog.show();
    }

    void onScanViewReady(View view) throws IOException {
        // Initial state of widgets
        updateGuiState();

        // License popup on first run
        if (prefs.getBoolean("firstrun", true)) {
//            onLicense();
            prefs.edit().putBoolean("firstrun", false).commit();
        }

        if (!mInitialised) {
            // Broadcast receiver
            registerReceiver(mReceiver, mFilter);
            mBtAdapterEnabled = mBtAdapter.isEnabled();
            if (mBtAdapterEnabled) {
                // Start straight away
                startBluetoothLeService();
            } else {
                // Request BT adapter to be turned on
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQ_ENABLE_BT);
            }
            mInitialised = true;
        } else {
            mScanView.notifyDataSetChanged();
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    if (!state.isBusy()) {
                        state = state.getNextState();
                        switch (state) {

                            case START:
                                stateTime = getCurrentTime();
                                state.initSensorsReading();
                                turnOffSensorLeds();
                                break;

                            case SCAN_SENSORS:
                                Log.d(TAG, "State: Scan Sensors");
                                stateTime = getCurrentTime();
                                if (!state.isBusy())
                                    scanSensors();
                                break;

                            case UPDATE_LCDS:
                                Log.d(TAG, "State: Update LCDs");
                                SensorScan.READ_SENSOR.setNumSensors(mDeviceInfoList.size());
                                stateTime = getCurrentTime();
                                if (!state.isBusy())
                                    updateLcds();
                                break;

                            case READ_SENSOR:
                                Log.d(TAG, "State: Read Sensors");
                                stateTime = getCurrentTime();
                                int sensorToRead = SensorScan.READ_SENSOR.getSensorToRead();
                                turnOnSensorLed(sensorToRead);
                                if (!state.isBusy()) {
                                    state.setBusy(true);
                                    readSensors(sensorToRead);
                                }
                                break;

                            default:
                                break;
                        }
                    }
                    else {
                        if (elapsedTime(getCurrentTime(), stateTime) > MAX_SCAN_TIME) {
                            Log.d(TAG, "State: Sensors scaned:" + mDeviceInfoList.size());
                            if (mDeviceInfoList.size()>0) {
                                state.setBusy(false);
                                state = UPDATE_LCDS;
                            }
                            else {
                                state.setBusy(false);
                                state = START;
                            }
                        }
                        long timeout;
                        if ((timeout= elapsedTime(getCurrentTime(), stateTime)) > MAX_STATE_TIME) {
                            Log.d(TAG, "State: Timeout= "+ (timeout/1000)%60);
                            state = START; // RESET
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                mHandler.postDelayed(this, STATE_TIME);
            }
        }, STATE_TIME);
    }

    private void turnOffSensorLeds() throws  IOException{
        BUS_SensorLed_01.setValue(false);
        BUS_SensorLed_02.setValue(false);
    }


    private void turnOnSensorLed(int sensor) throws IOException{

        if (mDeviceInfoList.size()>sensor) {
            int lcd = getSensorId(mDeviceInfoList.get(sensor).getBluetoothDevice().getAddress());

            switch (lcd) {
                case 0:
                    BUS_SensorLed_01.setValue(true);
                    BUS_SensorLed_02.setValue(true);
                    break;

                case 1:
                    BUS_SensorLed_01.setValue(false);
                    BUS_SensorLed_02.setValue(true);
                    break;

                case 2:
                    BUS_SensorLed_01.setValue(true);
                    BUS_SensorLed_02.setValue(false);
                    break;
                default:
                    break;
            }
        }
    }


    private long getCurrentTime(){return System.currentTimeMillis();}
    private long elapsedTime(long timeNow, long timeThen){return Math.abs(timeNow-timeThen);}

    public void scanSensors() throws IOException {


        if ((mScanning) && (!state.isBusy()))
            stopScan();
        else {
            if (!state.isBusy()) {
                mDeviceScanedList.clear();
                state.setBusy(true);
                startScan();
            }
        }
    }

    public void updateLcds() throws  IOException {

        state.setBusy(true);

        boolean sensor1On = false;
        boolean sensor2On = false;
        boolean sensor3On = false;

        for (int sensorId = 0; sensorId < mDeviceInfoList.size(); sensorId++) {
            String sensorAdress = mDeviceInfoList.get(sensorId).getBluetoothDevice().getAddress();
            switch(getSensorId(sensorAdress)) {
                case 0:
                    lcd1.setIsEnabled(true);
                    lcd1.setBackLight(true);
                    sensor1On = true;
                    break;
                case 1:
                    lcd2.setIsEnabled(true);
                    lcd2.setBackLight(true);
                    sensor2On = true;
                    break;

                case 2:
                    lcd3.setIsEnabled(true);
                    lcd3.setBackLight(true);
                    sensor3On = true;
                    break;
            }
        }

        if (!sensor1On) {lcd1.setIsEnabled(false); lcd1.setBackLight(false);}
        if (!sensor2On) {lcd2.setIsEnabled(false); lcd2.setBackLight(false);}
        if (!sensor3On) {lcd3.setIsEnabled(false); lcd3.setBackLight(false);}

        state.setBusy(false);

    }

    public void readSensors(int sensor) throws IOException{

        state.setBusy(true);

        if (mScanning) stopScan();
        onDeviceClick(sensor);

    }

    private void displayValues(){

    }

    public void onDeviceClick(final int pos) throws IOException {

        if (mScanning)
            stopScan();

        setBusy(true);

        if (pos<mDeviceInfoList.size()) {

            mBluetoothDevice = mDeviceInfoList.get(pos).getBluetoothDevice();

            if (mConnIndex == NO_DEVICE) {
                mScanView.setStatus("Connecting to " + mBluetoothDevice.getAddress() + " (pos " + pos + ")");
                mConnIndex = pos;
                onConnect();
            } else {
                mScanView.setStatus("Disconnecting from " + mBluetoothDevice.getAddress() + " (pos " + pos + ")");
                if (mConnIndex != NO_DEVICE) {
                    mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
                }
            }
        }
        else
        {
            // A bluetooth termometer has been disconnected
            // We force rescan
            state = INIT;
        }
    }

    public void onScanTimeout() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    stopScan();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onConnectTimeout() {
        runOnUiThread(new Runnable() {
            public void run() {
                setError("Connection timed out");
            }
        });
        if (mConnIndex != NO_DEVICE) {
            mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
            mConnIndex = NO_DEVICE;
        }
    }

    public void onBtnScan(View view) throws IOException{
        if (mScanning) {
            stopScan();
        } else {
            startScan();
        }
    }

    void onConnect() {
        if (mNumDevs > 0) {
            int connState = mBluetoothManager.getConnectionState(mBluetoothDevice,
                    BluetoothGatt.GATT);

            switch (connState) {
                case BluetoothGatt.STATE_CONNECTED:
                    mBluetoothLeService.disconnect(null);
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    boolean ok = mBluetoothLeService.connect(mBluetoothDevice.getAddress());
                    if (!ok) {
                        setError("Connect failed");
                    }
                    break;
                default:
                    setError("Device busy (connecting/disconnecting)");
                    break;
            }
        }
    }

    private void startScan() throws IOException {
        // Start device discovery
        if (mBleSupported) {
            mNumDevs = 0;
            mDeviceInfoList.clear();
            mScanView.notifyDataSetChanged();
            scanLeDevice(true);
            mScanView.updateGui(mScanning);
            if (!mScanning) {
                setError("Device discovery start failed");
                setBusy(false);
            }
        } else {
            setError("BLE not supported on this device");
        }
    }

    private void stopScan() throws IOException{
        mScanning = false;
        mScanView.updateGui(false);
        scanLeDevice(false);

    }

    private void startDeviceActivity() {
        mDeviceIntent = new Intent(this, DeviceActivity.class);
        mDeviceIntent.putExtra(DeviceActivity.EXTRA_DEVICE, mBluetoothDevice);
        startActivityForResult(mDeviceIntent, REQ_DEVICE_ACT);
    }

    private void stopDeviceActivity() {
        finishActivity(REQ_DEVICE_ACT);
    }

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// GUI methods
	//
	public void updateGuiState() throws IOException {
		boolean mBtEnabled = mBtAdapter.isEnabled();

		if (mBtEnabled) {
			if (mScanning) {
				// BLE Host connected
				if (mConnIndex != NO_DEVICE) {
                    if (mBluetoothDevice.getAddress().equals("78:A5:04:8C:1C:42")) {
                        lcd1.setBackLight(true);}
                    if (mBluetoothDevice.getAddress().equals("BC:6A:29:AE:D3:FC")) {
                        lcd2.setBackLight(true);}
                    if (mBluetoothDevice.getAddress().equals("78:A5:04:8C:17:48")) {
                        lcd3.setBackLight(true);}
                    if (mBluetoothDevice.getAddress().equals("B4:99:4C:64:24:A2")) {
                        lcd3.setBackLight(true);}
					String txt = mBluetoothDevice.getName() + " connected";
					mScanView.setStatus(txt);
				} else {
					mScanView.setStatus(mNumDevs + " devices");
				}
			}
		} else {
			mDeviceInfoList.clear();
			mScanView.notifyDataSetChanged();
		}
	}

	private void setBusy(boolean f) {
		mScanView.setBusy(f);
	}

	void setError(String txt) {
		mScanView.setError(txt);
		CustomToast.middleBottom(this, "Turning BT adapter off and on again may fix Android BLE stack problems");
	}

	private BleDeviceInfo createDeviceInfo(BluetoothDevice device, int rssi) {
		BleDeviceInfo deviceInfo = new BleDeviceInfo(device, rssi);

		return deviceInfo;
	}

	boolean checkDeviceFilter(String deviceName) {
		if (deviceName == null)
			return false;

		int n = mDeviceFilter.length;
		if (n > 0) {
			boolean found = false;
			for (int i = 0; i < n && !found; i++) {
				found = deviceName.equals(mDeviceFilter[i]);
			}
			return found;
		} else
			// Allow all devices if the device filter is empty
			return true;
	}

	private void addDevice(BleDeviceInfo device) throws IOException {
		mNumDevs++;
        if (device.getBluetoothDevice().getAddress().equals("78:A5:04:8C:1C:42")) {
            lcd1.setBackLight(true);}
        if (device.getBluetoothDevice().getAddress().equals("BC:6A:29:AE:D3:FC")) {
            lcd2.setBackLight(true);}
        if (device.getBluetoothDevice().getAddress().equals("78:A5:04:8C:17:48")) {
            lcd3.setBackLight(true);}
        if (device.getBluetoothDevice().getAddress().equals("B4:99:4C:64:24:A2")) {
            lcd3.setBackLight(true);}
//        boolean deviceOn = false;
//        for (int i=0; i<mDeviceScanedList.size();i++)
//        {
//            if (mDeviceScanedList.get(i).getBluetoothDevice().getAddress().equals(device.getBluetoothDevice().getAddress())) deviceOn=true;
//        }
//        if (!deviceOn)
  		mDeviceInfoList.add(device);
        state.setNumSensors(mDeviceInfoList.size());

        if (mDeviceInfoList.size() == 3)
            state.setBusy(false);

//        mDeviceScanedList.add(device);
		mScanView.notifyDataSetChanged();
		if (mNumDevs > 1)
			mScanView.setStatus(mNumDevs + " devices");
		else
			mScanView.setStatus("1 device");
	}

	private boolean deviceInfoExists(String address) {
		for (int i = 0; i < mDeviceInfoList.size(); i++) {
			if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
			    .equals(address)) {
				return true;
			}
		}
		return false;
	}

	private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
		for (int i = 0; i < mDeviceInfoList.size(); i++) {
			if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
			    .equals(device.getAddress())) {
				return mDeviceInfoList.get(i);
			}
		}
		return null;
	}

	private boolean scanLeDevice(boolean enable) {
		if (enable) {
			mScanning = mBtAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBtAdapter.stopLeScan(mLeScanCallback);
		}
		return mScanning;
	}

	List<BleDeviceInfo> getDeviceInfoList() {
		return mDeviceInfoList;
	}

	private void startBluetoothLeService() {
		boolean f;

		Intent bindIntent = new Intent(this, BluetoothLeService.class);
		startService(bindIntent);
		f = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
		if (!f) {
			CustomToast.middleBottom(this, "Bind to BluetoothLeService failed");
			finish();
		}
	}

	// Activity result handling
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String room;

		switch (requestCode) {
		case REQ_DEVICE_ACT:
			// When the device activity has finished: disconnect the device
            if (resultCode == RESULT_OK) {
                SensorData mSensorData = data.getParcelableExtra("SensorData");
                mNewValues = true;

                try {
                    if (mDeviceInfoList.get(state.getSensorToRead()).getBluetoothDevice().getAddress().equals("78:A5:04:8C:1C:42")) {
                        room = "Room 1";
                        lcd1.writeText(mSensorData.getmIrtDataRef(), room);
                        lcd1.setBackLight(true);
                    }
                    if (mDeviceInfoList.get(state.getSensorToRead()).getBluetoothDevice().getAddress().equals("BC:6A:29:AE:D3:FC")) {
                        room = "Room 2";
                        lcd2.writeText(mSensorData.getmIrtDataRef(), room);
                        lcd2.setBackLight(true);
                    }
                    if (mDeviceInfoList.get(state.getSensorToRead()).getBluetoothDevice().getAddress().equals("78:A5:04:8C:17:48")) {
                        room = "Room 3";
                        lcd3.writeText(mSensorData.getmIrtDataRef(), room);
                        lcd3.setBackLight(true);
                    }
                    if (mDeviceInfoList.get(state.getSensorToRead()).getBluetoothDevice().getAddress().equals("B4:99:4C:64:24:A2")) {
                        room = "Room 3";
                        lcd3.writeText(mSensorData.getmIrtDataRef(), room);
                        lcd3.setBackLight(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

			if (mConnIndex != NO_DEVICE) {
				mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
                state.setBusy(false);
			}

			break;

		case REQ_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {

				Toast.makeText(this, R.string.bt_on, Toast.LENGTH_SHORT).show();
			} else {
				// User did not enable Bluetooth or an error occurred
				Toast.makeText(this, R.string.bt_not_on, Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		default:
			CustomToast.middleBottom(this, "Unknown request code: " + requestCode);

			// Log.e(TAG, "Unknown request code");
			break;
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Broadcasted actions from Bluetooth adapter and BluetoothLeService
	//
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				// Bluetooth adapter state change
				switch (mBtAdapter.getState()) {
				case BluetoothAdapter.STATE_ON:
					mConnIndex = NO_DEVICE;
					startBluetoothLeService();
					break;
				case BluetoothAdapter.STATE_OFF:
					Toast.makeText(context, R.string.app_closing, Toast.LENGTH_LONG)
					    .show();
					finish();
					break;
				default:
					// Log.w(TAG, "Action STATE CHANGED not processed ");
					break;
				}

                try {
                    updateGuiState();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				// GATT connect
				int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
				    BluetoothGatt.GATT_FAILURE);
				if (status == BluetoothGatt.GATT_SUCCESS) {
					setBusy(false);
					startDeviceActivity();
				} else
					setError("Connect failed. Status: " + status);
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				// GATT disconnect
				int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
				    BluetoothGatt.GATT_FAILURE);
				stopDeviceActivity();
				if (status == BluetoothGatt.GATT_SUCCESS) {
					setBusy(false);
					mScanView.setStatus(mBluetoothDevice.getName() + " disconnected",
					    STATUS_DURATION);
				} else {
					setError("Disconnect failed. Status: " + status);
				}
				mConnIndex = NO_DEVICE;
				mBluetoothLeService.close();
			} else {
				// Log.w(TAG,"Unknown action: " + action);
			}

		}
	};

	// Code to manage Service life cycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
			    .getService();
			if (!mBluetoothLeService.initialize()) {
				Toast.makeText(mThis, "Unable to initialize BluetoothLeService", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			final int n = mBluetoothLeService.numConnectedDevices();
			if (n > 0) {
				runOnUiThread(new Runnable() {
					public void run() {
						mThis.setError("Multiple connections!");
					}
				});
			} else {
                try {
                    startScan();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Log.i(TAG, "BluetoothLeService connected");
			}
		}

		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
			// Log.i(TAG, "BluetoothLeService disconnected");
		}
	};

	// Device scan callback.
	// NB! Nexus 4 and Nexus 7 (2012) only provide one scan result per scan
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		public void onLeScan(final BluetoothDevice device, final int rssi,
		    byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				public void run() {
					// Filter devices
					if (checkDeviceFilter(device.getName())) {
						if (!deviceInfoExists(device.getAddress())) {
							// New device
                            BleDeviceInfo deviceInfo = createDeviceInfo(device, rssi);
                            try {
                                addDevice(deviceInfo);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
							// Already in list, update RSSI info
							BleDeviceInfo deviceInfo = findDeviceInfo(device);
							deviceInfo.updateRssi(rssi);
							mScanView.notifyDataSetChanged();
						}
					}
				}

			});
		}
	};

	private void initBusLed() {

        PeripheralManager pioManager = PeripheralManager.getInstance();
        Log.d(TAG, "Available GPIO: " + pioManager.getGpioList());

        try {
            BUS_SensorLed_01 = pioManager.openGpio(BUS_SENSOR_LED_01);
            BUS_SensorLed_02 = pioManager.openGpio(BUS_SENSOR_LED_02);

            BUS_SensorLed_01.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            BUS_SensorLed_02.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            BUS_SensorLed_01.setValue(false);
            BUS_SensorLed_02.setValue(false);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

	private void initLCDs() {
        PeripheralManager pioManager = PeripheralManager.getInstance();
        Log.d(TAG, "Available GPIO: " + pioManager.getGpioList());

        try {
            rs_lcd1 = pioManager.openGpio("BCM13");
            bl_lcd1 = pioManager.openGpio("BCM12");
            e_lcd1 = pioManager.openGpio("BCM19");
            d4_lcd1 = pioManager.openGpio("BCM26");
            d5_lcd1 = pioManager.openGpio("BCM16");
            d6_lcd1 = pioManager.openGpio("BCM20");
            d7_lcd1 = pioManager.openGpio("BCM21");

            rs_lcd2 = pioManager.openGpio("BCM6");
            bl_lcd2 = pioManager.openGpio("BCM7");
            e_lcd2 = pioManager.openGpio("BCM5");
            d4_lcd2 = pioManager.openGpio("BCM9");
            d5_lcd2 = pioManager.openGpio("BCM11");
            d6_lcd2 = pioManager.openGpio("BCM8");
            d7_lcd2 = pioManager.openGpio("BCM25");

            rs_lcd3 = pioManager.openGpio("BCM17");
            bl_lcd3 = pioManager.openGpio("BCM10");
            e_lcd3 = pioManager.openGpio("BCM18");
            d4_lcd3 = pioManager.openGpio("BCM22");
            d5_lcd3 = pioManager.openGpio("BCM23");
            d6_lcd3 = pioManager.openGpio("BCM24");
            d7_lcd3 = pioManager.openGpio("BCM27");

            lcd1 = new LCD(rs_lcd1, e_lcd1, d4_lcd1, d5_lcd1, d6_lcd1, d7_lcd1, bl_lcd1);
            lcd1.init();
            lcd2 = new LCD(rs_lcd2, e_lcd2, d4_lcd2, d5_lcd2, d6_lcd2, d7_lcd2, bl_lcd2);
            lcd2.init();
            lcd3 = new LCD(rs_lcd3, e_lcd3, d4_lcd3, d5_lcd3, d6_lcd3, d7_lcd3, bl_lcd3);
            lcd3.init();

        } catch (IOException | InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public int getSensorId(String address) {

        TypedArray sensors = getResources().obtainTypedArray(R.array.sensors);

        for (int i = 0; i < sensors.length(); i++)
            if (address.equals(sensors.getString(i)))
                return i;

        return 0;
    }

    public int getSensorSize(){
        TypedArray sensors = getResources().obtainTypedArray(R.array.sensors);

	    return sensors.length();
    }

}
