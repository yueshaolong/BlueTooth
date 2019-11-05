//package com.ysl.bluetooth;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.le.ScanCallback;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//import java.util.UUID;
//
//public class BleManager {
//    private BluetoothAdapter bleAdapter;
//    private BluetoothManager bleManager;
//    private BluetoothGatt bleGatt;
//    private BluetoothGattService gattService;
//    private BluetoothDevice device;
//    private Context mContext;
//    private static volatile BleManager singleton;
//    private final int REQUEST_ENABLE_BT = 1;
//    private StringBuffer buffer = new StringBuffer();
//    private String TAG = getClass().getName();
//
//    private BleManager(Context mContext) {
//        this.mContext = mContext;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            bleManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
//            bleAdapter = bleManager.getAdapter();
//        } else {
//            bleAdapter = BluetoothAdapter.getDefaultAdapter();
//        }
//    }
//
//    /**
//     * 获取蓝牙管理类的单例
//     *
//     * @param context
//     * @return
//     */
//    public static BleManager getInstance(Context context) {
//        if (singleton == null) {
//            synchronized (BleManager.class) {
//                if (singleton == null) {
//                    singleton = new BleManager(context);
//                }
//            }
//        }
//        return singleton;
//    }
//
//    /**
//     * 判断是否支持蓝牙设备
//     *
//     * @return
//     */
//    public boolean checkIfSupportBle() {
//        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
//    }
//
//    /**
//     * 开启蓝牙设备
//     */
//    public void enableBluetooth(Activity activity) {
//        if (bleAdapter == null || !bleAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }
//    }
//
//
//    public void enableBluetooth() {
//        if (bleAdapter == null || !bleAdapter.isEnabled()) {
//            bleAdapter.enable();
//        }
//    }
//
//
//    /**
//     * 获取蓝牙绑定了的列表设备
//     */
//    public List<BluetoothDevice> getBoundDevices() {
//        List<BluetoothDevice> devices = new ArrayList<>();
//        Set<BluetoothDevice> boundDevices = bleAdapter.getBondedDevices();
//        for (BluetoothDevice device : boundDevices) {
//            //对device进行其他操作，比如连接等。
//            devices.add(device);
//        }
//        return devices;
//    }
//
//    /**
//     * 通用蓝牙扫描方法，持续时间大概10s，扫描到蓝牙发出广播进行接收
//     */
//    public void startDiscover() {
//        bleAdapter.startDiscovery();
//    }
//
//    /**
//     * 该方法目前稳定
//     *
//     * @param leScanCallback
//     */
//    public void startLeScan(BluetoothAdapter.LeScanCallback leScanCallback) {
//        bleAdapter.startLeScan(leScanCallback);
//    }
//
//    /**
//     * 通过UUID过滤筛选出合适的蓝牙设备
//     *
//     * @param uuids
//     * @param leScanCallback
//     */
//    public void startLeScan(UUID[] uuids, BluetoothAdapter.LeScanCallback leScanCallback) {
//        bleAdapter.startLeScan(uuids, leScanCallback);
//    }
//
//    /**
//     * 代替果实的startLeScan(),但是不稳定
//     *
//     * @param scanCallback
//     */
//    public void startBleScan(ScanCallback scanCallback) {
//        bleAdapter.getBluetoothLeScanner().startScan(scanCallback);
//    }
//
//    /**
//     * 关闭蓝牙搜索
//     *
//     * @param leScanCallback
//     */
//    public void stopLeScan(BluetoothAdapter.LeScanCallback leScanCallback) {
//        bleAdapter.stopLeScan(leScanCallback);
//    }
//
//    /**
//     * 蓝牙是否开启
//     *
//     * @return
//     */
//    public boolean isOpen() {
//        return bleAdapter.isEnabled();
//    }
//
//    /**
//     * 关闭蓝牙设备
//     *
//     * @param scanCallback
//     */
//    public void stopBleScan(ScanCallback scanCallback) {
//        bleAdapter.getBluetoothLeScanner().stopScan(scanCallback);
//    }
//
//    public void setBleGatt(BluetoothGatt bleGatt) {
//        this.bleGatt = bleGatt;
//    }
//
//    public void setGattService(BluetoothGattService gattService) {
//        this.gattService = gattService;
//    }
//
//    public BluetoothGatt getBleGatt() {
//        return bleGatt;
//    }
//
//    public BluetoothGattService getGattService() {
//        return gattService;
//    }
//
//    public BluetoothDevice getDevice() {
//        return device;
//    }
//
//    public void setDevice(BluetoothDevice device) {
//        this.device = device;
//    }
//
//    /**
//     * 开启蓝牙设备
//     */
//    public void openBle() {
//        bleAdapter.enable();
//    }
//
//    /**
//     * 关闭蓝牙设备
//     */
//    public void closeBle() {
//        bleAdapter.disable();
//    }
//
//    /**
//     * 通过mac地址直接得到BluetoothDevice
//     *
//     * @param mac
//     * @return
//     */
//    public BluetoothDevice getRemoteDevice(String mac) {
//        device = bleAdapter.getRemoteDevice(mac);
//        return device;
//    }
//
//    /**
//     * 连接蓝牙设备
//     *
//     * @param auto
//     * @param mGattCallback
//     * @return
//     */
//    public BluetoothGatt connectDevice(boolean auto, BluetoothGattCallback mGattCallback) {
//        bleGatt = device.connectGatt(mContext, auto, mGattCallback);
//        Log.e(TAG, "生成BluetoothGatt----->" + this.bleGatt);
//        return bleGatt;
//    }
//
//    /**
//     * 获取对应应用的服务
//     * @return
//     */
//    public BluetoothGattService getDefaultGattService() {
//        this.gattService = bleGatt.getService(UUID.fromString(UUIDManager.SERVICE_UUID));
//        return gattService;
//    }
//
//
//    @SuppressLint("NewApi")
//    public boolean enableNotification(BluetoothGatt bluetoothGatt, boolean enable) {
//        if (gattService == null) {
//            getDefaultGattService();
//        }
//        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString(
//                UUIDManager.NOTIFY_UUID));
//        if (bluetoothGatt == null || characteristic == null) {
//            return false;
//        }
//        if (!bluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
//            return false;
//        }
//        //获取到Notify当中的Descriptor通道  然后再进行注册
//        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(UUID.fromString(UUIDManager.NOTIFY_DESCRIPTOR));
//        if (clientConfig == null) {
//            return false;
//        }
//        if (enable) {
//            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        } else {
//            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//        }
//        return bluetoothGatt.writeDescriptor(clientConfig);
//    }
//
//    /**
//     * 蓝牙设备传数据
//     *
//     * @param data
//     * @return
//     */
//    private boolean writeBluetoothData(String data) {
//        BluetoothGattCharacteristic writeCharacter = null;
//        if (gattService == null) {
//            gattService = getDefaultGattService();
//            if (gattService == null) {
//                return false;
//            }
//        }
//        writeCharacter = gattService.getCharacteristic(UUID.fromString(UUIDManager.WRITE_UUID));
//        // 设置监听
//        this.bleGatt.setCharacteristicNotification(writeCharacter, true);
//        // 当数据传递到蓝牙之后
//        // 会回调BluetoothGattCallback里面的write方法
//        writeCharacter.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//        // 将需要传递的数据 打碎成16进制
//        try {
//            writeCharacter.setValue(CommonUtils.getHexBytes(data));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return this.bleGatt.writeCharacteristic(writeCharacter);
//    }
//
//    /**
//     * 向蓝牙设备写入数据
//     */
//    public void writeData(String content) {
//        buffer.append(content);
//        writeData();
//    }
//
//    public void writeData() {
//        int length = buffer.length();
//        String writeData = "";
//        if (length >= 20) {
//            writeData = buffer.substring(0, 20);
//        } else {
//            writeData = buffer.toString();
//        }
//        if (writeData.length() > 0) {
//            writeBluetoothData(writeData);
//        }
//        if (writeData.length() > 0) {
//            buffer.delete(0, writeData.length());
//        }
//    }
//
//
//    /**
//     * 关闭蓝牙连接
//     */
//    public void closeGatt() {
//        if (bleGatt != null) {
//            bleGatt.disconnect();
//            bleGatt.close();
//            Log.e(TAG, "连接蓝牙断开bleGatt" + this.bleGatt);
//            Log.e(TAG, "连接蓝牙断开gattService" + this.gattService);
//            bleGatt = null;
//            gattService = null;
//        }
//    }
//}
//
