package com.ysl.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
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

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private Button btn_open_light;
    private Button btn_close_light;
    private Button btn_open_once;
    private ListView listview;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> adapter;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        requestPermission();

//        bleFunc();
        setAdapter();

        // 获取蓝牙适配器
        if(checkIfSupportBle()){
//            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    private void bleFunc() {
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setSplitWriteNum(20)
                .setConnectOverTime(10000)
                .setOperateTimeout(5000);
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
//                .setServiceUuids(serviceUuids)
//                .setDeviceName(true, names)
//                .setDeviceMac("C4:06:83:1A:DC:AF")
                .setAutoConnect(false)
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                Log.d(TAG, "onScanStarted: "+success);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                Log.d(TAG, "onScanning: "+bleDevice.getName()+bleDevice.getMac());
                MainActivity.this.bleDevice = bleDevice;
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                Log.d(TAG, "onScanFinished: "+scanResultList.size());
                for (BleDevice device : scanResultList) {
                    Log.d(TAG, "onScanFinished: "+device.getName()+"    "+device.getMac());
                }
            }
        });
        BleManager.getInstance().connect("C4:06:83:1A:DC:AF", new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect: ");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                Log.d(TAG, "onConnectFail: "+exception);
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onConnectSuccess: "+status);
//                MainActivity.this.bleDevice = bleDevice;
                MainActivity.this.gatt = gatt;
                queryGattServices(gatt.getServices());
                BleManager.getInstance().write(
                        bleDevice,
                        uuid_service,
                        uuid_write,
                        "abcd".getBytes(),
                        new BleWriteCallback() {
                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                Log.d(TAG, "onWriteSuccess: "+current+"  "+total+"  "+ Arrays.toString(justWrite));
                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                Log.d(TAG, "onWriteFailure: "+exception);
                            }
                        });
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onDisConnected: "+isActiveDisConnected);
//                BleManager.getInstance().cancelScan();
//                BleManager.getInstance().disconnect(bleDevice);
            }
        });
    }

    private BluetoothGattCallback bleGattCallback = new BluetoothGattCallback(){
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange: ");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "成功建立蓝牙通道");
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //发现服务
                    gatt.discoverServices();
                    Log.e(TAG, "蓝牙连接成功!");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.e(TAG, "蓝牙连接断开!");
                    gatt.disconnect();
                    gatt.close();
                    Log.e(TAG, "关闭蓝牙!");
                }
            }else {
                Log.e(TAG, "建立蓝牙通道失败" + status);
                gatt.disconnect();
                gatt.close();
            }
        }

        // service 发现后就会触发这个方法，然后你就需要找到你和 ble 约定好的 service
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            MainActivity.this.gatt = gatt;
            Log.d(TAG, "onServicesDiscovered: "+status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> serviceList = gatt.getServices();
                queryGattServices(serviceList);
                gatt.setCharacteristicNotification(gattCharacteristic, true);
//                for (BluetoothGattService service : serviceList) {
//                    UUID uuid_service = service.getUuid();
//                    System.out.println("uuid_service--->"+uuid_service);
//                    List<BluetoothGattCharacteristic> characteristicList= service.getCharacteristics();
//                    for(BluetoothGattCharacteristic characteristic1 : characteristicList) {
//                        UUID uuid_chara = characteristic1.getUuid();
//                        System.out.println("uuid_chara--->"+uuid_chara);
//                        gatt.writeCharacteristic(characteristic1);
//                        gatt.setCharacteristicNotification(characteristic1, true);
//                        characteristic1.setValue("aba".getBytes());
//                    }
//
            }
        }

        //收到消息
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            byte[] value = characteristic.getValue();
            Log.d(TAG, "onCharacteristicWrite: "+Arrays.toString(value));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged: ");
        }
    };
    /**
     * 展示服务Services和characteristic对应的UUID，以及具备的属性。
     */
    public boolean queryGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return false;
        }
        Log.e(TAG, "服务的数量: "+gattServices.size());
        for (BluetoothGattService myService : gattServices) {
            //找出服务的UUID
            Log.e(TAG,"---->server uuid:"+myService.getUuid().toString());
            if (myService.getUuid().toString().equalsIgnoreCase(UUID_SERVER)) {
                uuid_service = myService.getUuid().toString();
                List<BluetoothGattCharacteristic> gattCharacteristics = myService.getCharacteristics();
                for (final BluetoothGattCharacteristic  gattCharacteristic: gattCharacteristics) {
                    Log.e(TAG,"---->characteristic uuid:"+gattCharacteristic.getUuid());

                    //所有Characteristics按属性分类
                    int charaProp = gattCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        Log.d(TAG, "gattCharacteristic的UUID为:" + gattCharacteristic.getUuid());
                        Log.d(TAG, "gattCharacteristic的属性为:  可读");
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                        Log.d(TAG, "gattCharacteristic的UUID为:" + gattCharacteristic.getUuid());
                        Log.d(TAG, "gattCharacteristic的属性为:  可写");
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        Log.d(TAG, "gattCharacteristic的UUID为:" + gattCharacteristic.getUuid());
                        Log.d(TAG, "gattCharacteristic的属性为:  具备通知属性");
                    }

                    if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY)) {
                        MainActivity.this.gattCharacteristic = gattCharacteristic;
                        uuid_write = gattCharacteristic.getUuid().toString();
                    }
                }
            }
//            return false;
        }
        return false;
    }

    public boolean write(String str) {
        if (gatt == null)
            return false;
        if (gattCharacteristic == null)
            return false;
        gattCharacteristic.setValue(str);
        return gatt.writeCharacteristic(gattCharacteristic);
    }

    private void setAdapter() {
        adapter = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1,
                bluetoothDevices) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                BluetoothDevice bluetoothDevice = bluetoothDevices.get(position);
                view.setText(Html.fromHtml(bluetoothDevice.getName() + "<br/>" + "<font color=red>"
                        + bluetoothDevice.getAddress() + "</font>"));
                return view;
            }
        };
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice bluetoothDevice = bluetoothDevices.get(position);

//                BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(/*"24:DA:33:00:22:BC"*/
//                "C4:06:83:1A:DC:AF"/*"5C:1C:B9:1C:0D:28"*/);

                bluetoothDevice.connectGatt(getApplicationContext(), false, bleGattCallback);
            }
        });
    }

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
//                boolean b = bluetoothAdapter.startDiscovery();// 开始扫描设备 ---> 广播
                bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
                break;
            case R.id.btn_stop_discovery:
//                boolean b1 = bluetoothAdapter.cancelDiscovery();// 停止扫描设备 ---> 广播
//                System.out.println("停止扫描："+b1);
                bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                break;
            case R.id.btn_open_light:
                // 发送  开灯 指令      01 99 10 10 99
//                sendCtrl(1);
                List<BluetoothDevice> boundDevices = getBoundDevices();
                for (BluetoothDevice boundDevice : boundDevices) {
                    System.out.println(boundDevice.getName()+"======"+boundDevice.getAddress());
                }
                break;
            case R.id.btn_close_light:
                //  01 99 11 11 99
//                sendCtrl(2);
//                BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice("24:DA:33:00:22:BC");
//                System.out.println(remoteDevice.getName()+"-=-=-=-=-"+remoteDevice.getAddress());
                break;
            case R.id.btn_open_once:
                //01 99 19 19 99
//                sendCtrl(3);
                write("lalala");
                break;
        }
    }
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            Log.d("--------->", device.getName() + "::" + device.getAddress());
            // Add the name and address to an array adapter to show in a ListView
            if(!bluetoothDevices.contains(device)){
                adapter.add(device);
                adapter.notifyDataSetChanged();
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

    private void sendCtrl(int type) {
        try {
            byte[] buffer = new byte[5];
            //  - 127 ----> 128
            buffer[0] = 0x01;
            buffer[1] = (byte) 0x99; // 0x99
            if (type == 1) {
                buffer[2] = 0x10;
                buffer[3] = 0x10;
            }else if(type == 2){
                //关灯
                buffer[2] = 0x11;
                buffer[3] = 0x11;
            }else if(type == 3){
                // 点动4秒
                buffer[2] = 0x19;
                buffer[3] = 0x19;
            }

            buffer[4] = (byte) 0x99;
            writer.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
//                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString
//                        ("00001101-0000-1000-8000-00805F9B34FB"));
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
                }
            }
        }
    }

    private void initView() {
        btn_open_bluetooth = (Button) findViewById(R.id.btn_open_bluetooth);
        btn_close_bluetooth = (Button) findViewById(R.id.btn_close_bluetooth);
        btn_start_discovery = (Button) findViewById(R.id.btn_start_discovery);
        btn_stop_discovery = (Button) findViewById(R.id.btn_stop_discovery);
        btn_open_light = (Button) findViewById(R.id.btn_open_light);
        btn_close_light = (Button) findViewById(R.id.btn_close_light);
        btn_open_once = (Button) findViewById(R.id.btn_open_once);
        listview = (ListView) findViewById(R.id.listview);

        btn_open_bluetooth.setOnClickListener(this);
        btn_close_bluetooth.setOnClickListener(this);
        btn_start_discovery.setOnClickListener(this);
        btn_stop_discovery.setOnClickListener(this);
        btn_open_light.setOnClickListener(this);
        btn_close_light.setOnClickListener(this);
        btn_open_once.setOnClickListener(this);
    }
}
