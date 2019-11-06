package com.example.appserver;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import rx.Observer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getName();
    private Button btn_open_bluetooth;
    private Button btn_close_bluetooth;
    private Button btn_start_discovery;
    private Button btn_stop_discovery;
    private Button btn_write;
    private Button btn_read;
    private Button btn_write_ble;
    private Button btn_read_ble;
    private ListView listview1;
    private ListView listview2;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private List<BluetoothDevice> bleDevices = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> adapter1;
    private ArrayAdapter<BluetoothDevice> adapter2;
    private InputStream reader;
    private OutputStream writer;
    private BluetoothSocket bluetoothSocket;
    private BleDevice bleDevice;
    private BluetoothGatt gatt;
    private String uuid_service;
    private String uuid_write;
    private BluetoothGattCharacteristic  gattCharacteristic;
    public final static  String UUID_SERVER="0000ffe0-0000-1000-8000-00805f9b34fb";
    public final static  String UUID_NOTIFY="0000ffe1-0000-1000-8000-00805f9b34fb";
    public final static  String UUID_MY="abcdefaf-0000-1000-8000-00805f9b34fb";
    private BlueToothReceiver blueToothReceiver;
    private BluetoothServerSocket serverSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        requestPermission();

//        setAdapter1();
//        setAdapter2();

        // 获取蓝牙适配器
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        AcceptThread acceptThread = new AcceptThread();
        acceptThread.start();

        //  查找设备的广播接收者
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
//        blueToothReceiver = new BlueToothReceiver();
//        registerReceiver(blueToothReceiver, intentFilter);
    }

    //服务端监听客户端的线程类
    private class AcceptThread extends Thread {
        public AcceptThread() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("abc",
                        UUID.fromString(UUID_MY));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void run() {
            try {
                BluetoothSocket socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                while(true) {
                    byte[] buffer =new byte[1024];
                    int count = inputStream.read(buffer);
                    final String s = new String(buffer, 0, count, "utf-8");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "服务端收到消息："+s, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class BlueToothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("bluetooth----->", device.getName() + "::" + device.getAddress());
                if (!bluetoothDevices.contains(device)) {
                    adapter1.add(device);
                    adapter1.notifyDataSetChanged();
                }
            }
        }
    }

//    private void bleFunc() {
//        BleManager.getInstance().init(getApplication());
//        BleManager.getInstance()
//                .enableLog(true)
//                .setReConnectCount(1, 5000)
//                .setSplitWriteNum(20)
//                .setConnectOverTime(10000)
//                .setOperateTimeout(5000);
//        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
////                .setServiceUuids(serviceUuids)
////                .setDeviceName(true, names)
////                .setDeviceMac("C4:06:83:1A:DC:AF")
//                .setAutoConnect(false)
//                .setScanTimeOut(10000)
//                .build();
//        BleManager.getInstance().initScanRule(scanRuleConfig);
//        BleManager.getInstance().scan(new BleScanCallback() {
//            @Override
//            public void onScanStarted(boolean success) {
//                Log.d(TAG, "onScanStarted: "+success);
//            }
//
//            @Override
//            public void onScanning(BleDevice bleDevice) {
//                Log.d(TAG, "onScanning: "+bleDevice.getName()+bleDevice.getMac());
//                MainActivity.this.bleDevice = bleDevice;
//            }
//
//            @Override
//            public void onScanFinished(List<BleDevice> scanResultList) {
//                Log.d(TAG, "onScanFinished: "+scanResultList.size());
//                for (BleDevice device : scanResultList) {
//                    Log.d(TAG, "onScanFinished: "+device.getName()+"    "+device.getMac());
//                }
//            }
//        });
//        BleManager.getInstance().connect("C4:06:83:1A:DC:AF", new BleGattCallback() {
//            @Override
//            public void onStartConnect() {
//                Log.d(TAG, "onStartConnect: ");
//            }
//
//            @Override
//            public void onConnectFail(BleDevice bleDevice, BleException exception) {
//                Log.d(TAG, "onConnectFail: "+exception);
//            }
//
//            @Override
//            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
//                Log.d(TAG, "onConnectSuccess: "+status);
////                MainActivity.this.bleDevice = bleDevice;
//                MainActivity.this.gatt = gatt;
//                queryGattServices(gatt.getServices());
//                BleManager.getInstance().write(
//                        bleDevice,
//                        uuid_service,
//                        uuid_write,
//                        "abcd".getBytes(),
//                        new BleWriteCallback() {
//                            @Override
//                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                                Log.d(TAG, "onWriteSuccess: "+current+"  "+total+"  "+ Arrays.toString(justWrite));
//                            }
//
//                            @Override
//                            public void onWriteFailure(BleException exception) {
//                                Log.d(TAG, "onWriteFailure: "+exception);
//                            }
//                        });
//            }
//
//            @Override
//            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
//                Log.d(TAG, "onDisConnected: "+isActiveDisConnected);
////                BleManager.getInstance().cancelScan();
////                BleManager.getInstance().disconnect(bleDevice);
//            }
//        });
//    }

//    private BluetoothGattCallback bleGattCallback = new BluetoothGattCallback(){
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            super.onConnectionStateChange(gatt, status, newState);
//            Log.d(TAG, "onConnectionStateChange: ");
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.e(TAG, "成功建立蓝牙通道");
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    Log.e(TAG, "蓝牙连接成功!");
//                    //发现服务
//                    gatt.discoverServices();
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                    Log.e(TAG, "蓝牙连接断开!");
//                    gatt.disconnect();
//                    gatt.close();
//                    Log.e(TAG, "关闭蓝牙!");
//                }
//            }else {
//                Log.e(TAG, "建立蓝牙通道失败" + status);
//                gatt.disconnect();
//                gatt.close();
//            }
//        }
//
//        // service 发现后就会触发这个方法，然后你就需要找到你和 ble 约定好的 service
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            super.onServicesDiscovered(gatt, status);
//            MainActivity.this.gatt = gatt;
//            Log.d(TAG, "onServicesDiscovered: "+status);
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                List<BluetoothGattService> serviceList = gatt.getServices();
//                queryGattServices(serviceList);
//                gatt.setCharacteristicNotification(gattCharacteristic, true);
////                for (BluetoothGattService service : serviceList) {
////                    UUID uuid_service = service.getUuid();
////                    System.out.println("uuid_service--->"+uuid_service);
////                    List<BluetoothGattCharacteristic> characteristicList= service.getCharacteristics();
////                    for(BluetoothGattCharacteristic characteristic1 : characteristicList) {
////                        UUID uuid_chara = characteristic1.getUuid();
////                        System.out.println("uuid_chara--->"+uuid_chara);
////                        gatt.writeCharacteristic(characteristic1);
////                        gatt.setCharacteristicNotification(characteristic1, true);
////                        characteristic1.setValue("aba".getBytes());
////                    }
////
//            }
//        }
//
//        //收到消息
//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
//                characteristic, int status) {
//            super.onCharacteristicWrite(gatt, characteristic, status);
//            byte[] value = characteristic.getValue();
//            Log.d(TAG, "onCharacteristicWrite: "+Arrays.toString(value));
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
//                characteristic) {
//            super.onCharacteristicChanged(gatt, characteristic);
//            Log.d(TAG, "onCharacteristicChanged: ");
//        }
//    };
    /**
     * 展示服务Services和characteristic对应的UUID，以及具备的属性。
     */
//    public void queryGattServices(List<BluetoothGattService> gattServices) {
//        if (gattServices == null) {
//            return;
//        }
//        Log.e(TAG, "服务的数量: "+gattServices.size());
//        for (BluetoothGattService myService : gattServices) {
//            //找出服务的UUID
//            Log.e(TAG,"---->server uuid:"+myService.getUuid().toString());
//            if (myService.getUuid().toString().equalsIgnoreCase(UUID_SERVER)) {
//                uuid_service = myService.getUuid().toString();
//                List<BluetoothGattCharacteristic> gattCharacteristics = myService.getCharacteristics();
//                for (final BluetoothGattCharacteristic  gattCharacteristic: gattCharacteristics) {
//                    Log.e(TAG,"---->characteristic uuid:"+gattCharacteristic.getUuid());
//
//                    //所有Characteristics按属性分类
//                    int charaProp = gattCharacteristic.getProperties();
//                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                        Log.d(TAG, "gattCharacteristic的UUID为:" + gattCharacteristic.getUuid());
//                        Log.d(TAG, "gattCharacteristic的属性为:  可读");
//                    }
//                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
//                        Log.d(TAG, "gattCharacteristic的UUID为:" + gattCharacteristic.getUuid());
//                        Log.d(TAG, "gattCharacteristic的属性为:  可写");
//                    }
//                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                        Log.d(TAG, "gattCharacteristic的UUID为:" + gattCharacteristic.getUuid());
//                        Log.d(TAG, "gattCharacteristic的属性为:  具备通知属性");
//                    }
//
//                    if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY)) {
//                        MainActivity.this.gattCharacteristic = gattCharacteristic;
//                        uuid_write = gattCharacteristic.getUuid().toString();
//                    }
//                }
//            }
//        }
//    }

//    public boolean write(String str) {
//        if (gatt == null)
//            return false;
//        if (gattCharacteristic == null)
//            return false;
//        gattCharacteristic.setValue(str);
//        return gatt.writeCharacteristic(gattCharacteristic);
//    }
//    private String read() {
//        if (gatt == null)
//            return null;
//        if (gattCharacteristic == null)
//            return null;
//        gatt.readCharacteristic(gattCharacteristic);
//        return gattCharacteristic.getValue().toString();
//    }

//    private void setAdapter1() {
//        adapter1 = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1,
//                bluetoothDevices) {
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                TextView view = (TextView) super.getView(position, convertView, parent);
//                BluetoothDevice bluetoothDevice = bluetoothDevices.get(position);
//                view.setText(Html.fromHtml(bluetoothDevice.getName() + "<br/>" + "<font color=red>"
//                        + bluetoothDevice.getAddress() + "</font>"));
//                return view;
//            }
//        };
//        listview1.setAdapter(adapter1);
//        listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                BluetoothDevice bluetoothDevice = bluetoothDevices.get(position);
//                //配对之前把扫描关闭
//                if (bluetoothAdapter.isDiscovering()){
//                    bluetoothAdapter.cancelDiscovery();
//                }
//                //判断设备是否配对，没有配对在配，配对了就不需要配了
//                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
//                    Log.d(TAG, "attemp to bond:" + bluetoothDevice.getName());
//                    try {
//                        Method createBondMethod = bluetoothDevice.getClass().getMethod("createBond");
//                        Boolean returnValue = (Boolean) createBondMethod.invoke(bluetoothDevice);
//                        returnValue.booleanValue();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Log.e(TAG, "attemp to bond fail!");
//                    }
//                }
//                //经典蓝牙的连接
//                ConnectThread thread = new ConnectThread(bluetoothDevice);
//                thread.start();
//            }
//        });
//    }
//    private void setAdapter2() {
//        adapter2 = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1,
//                bleDevices) {
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                TextView view = (TextView) super.getView(position, convertView, parent);
//                BluetoothDevice bluetoothDevice = bleDevices.get(position);
//                view.setText(Html.fromHtml(bluetoothDevice.getName() + "<br/>" + "<font color=red>"
//                        + bluetoothDevice.getAddress() + "</font>"));
//                return view;
//            }
//        };
//        listview2.setAdapter(adapter2);
//        listview2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                BluetoothDevice bluetoothDevice = bleDevices.get(position);
//                //ble蓝牙连接
//                bluetoothDevice.connectGatt(getApplicationContext(), false, bleGattCallback);
//            }
//        });
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_bluetooth:
                if (!bluetoothAdapter.isEnabled())
                    bluetoothAdapter.enable();// 打开蓝牙
                break;
            case R.id.btn_close_bluetooth:
                if (bluetoothAdapter.isEnabled())
                    bluetoothAdapter.disable();// 关闭蓝牙
                break;
            case R.id.btn_start_discovery:
                bluetoothAdapter.startDiscovery();//扫描经典蓝牙
                bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);//扫描ble
                break;
            case R.id.btn_stop_discovery:
                bluetoothAdapter.cancelDiscovery();// 停止扫描
                bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                break;
            case R.id.btn_write:
                try {
                    outputStream.write("收到啦".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_read:
                break;
            case R.id.btn_write_ble:
                break;
            case R.id.btn_read_ble:
                break;
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            Log.d("ble--------->", device.getName() + "::" + device.getAddress());
            // Add the name and address to an array adapter to show in a ListView
            if(!bleDevices.contains(device)){
                adapter2.add(device);
                adapter2.notifyDataSetChanged();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            System.out.println("onBatchScanResults----->"+results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            System.out.println("errorCode----->"+errorCode);
        }
    };

    public boolean checkIfSupportBle() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
    public List<BluetoothDevice> getBoundDevices() {
        List<BluetoothDevice> devices = new ArrayList<>();
        Set<BluetoothDevice> boundDevices = bluetoothAdapter.getBondedDevices();
        //对device进行其他操作，比如连接等。
        devices.addAll(boundDevices);
        return devices;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(blueToothReceiver);

        BleManager.getInstance().destroy();

        if (reader!= null){
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (writer != null){
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (bluetoothSocket != null){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void requestPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }
                });
    }

    private class ConnectThread extends Thread {
        private BluetoothDevice device;
        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(
                        UUID.fromString("00001000-0000-1000-8000-00805F9B34FB"));
//                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(
//                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();

            if (bluetoothSocket != null) {
                try {
                    bluetoothSocket.connect();
                    // 获取流对象
                    reader = bluetoothSocket.getInputStream();
                    writer = bluetoothSocket.getOutputStream();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                    try {
                        Method m = device.getClass().getMethod("createRfcommSocket",
                                new Class[] {int.class});
                        bluetoothSocket = (BluetoothSocket) m.invoke(device, 1);
                        if (bluetoothSocket != null) {
                            bluetoothSocket.connect();
                            // 获取流对象
                            reader = bluetoothSocket.getInputStream();
                            writer = bluetoothSocket.getOutputStream();
                            System.out.println(reader+"---------->"+writer);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        try{
                            bluetoothSocket.close();
                        }catch (IOException ie){
                            ie.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void initView() {
        btn_open_bluetooth = (Button) findViewById(R.id.btn_open_bluetooth);
        btn_close_bluetooth = (Button) findViewById(R.id.btn_close_bluetooth);
        btn_start_discovery = (Button) findViewById(R.id.btn_start_discovery);
        btn_stop_discovery = (Button) findViewById(R.id.btn_stop_discovery);
        btn_write = (Button) findViewById(R.id.btn_write);
        btn_read = (Button) findViewById(R.id.btn_read);
        btn_write_ble = (Button) findViewById(R.id.btn_write_ble);
        btn_read_ble = (Button) findViewById(R.id.btn_read_ble);
        listview1 = (ListView) findViewById(R.id.listview1);
        listview2 = (ListView) findViewById(R.id.listview2);

        btn_open_bluetooth.setOnClickListener(this);
        btn_close_bluetooth.setOnClickListener(this);
        btn_start_discovery.setOnClickListener(this);
        btn_stop_discovery.setOnClickListener(this);
        btn_write.setOnClickListener(this);
        btn_read.setOnClickListener(this);
        btn_write_ble.setOnClickListener(this);
        btn_read_ble.setOnClickListener(this);
    }
}
