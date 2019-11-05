package zz.itcast.bluetoothz13;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
//   dlan  --->   airplay   ---->    (windows)   smba 文件共享系统
//


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btn_open_bluetooth;
    private Button btn_close_bluetooth;
    private Button btn_start_discovery;
    private Button btn_stop_discovery;
    private Button btn_open_light;
    private Button btn_close_light;
    private Button btn_open_once;
    private ListView listview;
    private BluetoothAdapter bluetoothAdapter;
    private BlueToothReceiver blueToothReceiver;
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> adapter;
    private InputStream reader;
    private OutputStream writer;
    private BluetoothSocket bluetoothSocket;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(blueToothReceiver);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


        adapter = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, bluetoothDevices) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {


                TextView view = (TextView) super.getView(position, convertView, parent);
                BluetoothDevice bluetoothDevice = bluetoothDevices.get(position);

                //textview   里边子体可以改变

                //  1.  Html
//                2. SpannableString
//                ImageSpan
//                ClickableSpan
//                view.setText(bluetoothDevice.getName() + "\r\n" + bluetoothDevice.getAddress());
                //   <br/>
                view.setText(Html.fromHtml(bluetoothDevice.getName() + "<br/>" + "<font color=red>" + bluetoothDevice.getAddress() + "</font>"));


                return view;
            }
        };
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);

        // 获取蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

       /* if (bluetoothAdapter == null){
            //
        }*/

        //  查找设备的广播接收者


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        blueToothReceiver = new BlueToothReceiver();
        registerReceiver(blueToothReceiver, intentFilter);

    }


    private class BlueToothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 接收 广播
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
//                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Log.d("bluetooth", device.getName() + "::" + device.getAddress());
//                bluetoothDevices.add(device);
//                adapter.notifyDataSetChanged();

                adapter.add(device);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice bluetoothDevice = bluetoothDevices.get(position);
        ConnectThread thread = new ConnectThread(bluetoothDevice);
        thread.start();
    }

    private class ConnectThread extends Thread {

        private BluetoothDevice device;


        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            // base-uuid
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_bluetooth:
                if (!bluetoothAdapter.isEnabled()) //  之前没有打开蓝牙
                    bluetoothAdapter.enable();// 打开蓝牙
                break;
            case R.id.btn_close_bluetooth:
                if (bluetoothAdapter.isEnabled())
                    bluetoothAdapter.disable();// 关闭蓝牙
                break;
            case R.id.btn_start_discovery:
                bluetoothAdapter.startDiscovery();// 开始查询设备 ---> 广播

                break;
            case R.id.btn_stop_discovery:
                bluetoothAdapter.cancelDiscovery();// 开始查询设备 ---> 广播
                break;
            case R.id.btn_open_light:
                // 发送  开灯 指令      01 99 10 10 99
                // alt + ctrl + t --->6
                sendCtrl(1);
                break;
            case R.id.btn_close_light:
                //  01 99 11 11 99
                sendCtrl(2);
                break;
            case R.id.btn_open_once:
                //01 99 19 19 99
                sendCtrl(3);
                break;
        }

        //  流对象
        //  cursor
        //  bitmap

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
}
