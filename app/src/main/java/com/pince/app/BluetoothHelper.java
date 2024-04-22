package com.pince.app;

import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothSocket;

        import java.io.IOException;
        import java.io.OutputStream;
        import java.util.UUID;

public class BluetoothHelper {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    public BluetoothHelper() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connect(String deviceAddress) throws IOException {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        bluetoothSocket.connect();
        outputStream = bluetoothSocket.getOutputStream();
    }

    public void sendCharacter(char character) throws IOException {
        outputStream.write(character);
    }

    public void disconnect() throws IOException {
        if (outputStream != null) {
            outputStream.close();
        }
        if (bluetoothSocket != null) {
            bluetoothSocket.close();
        }
    }

}
