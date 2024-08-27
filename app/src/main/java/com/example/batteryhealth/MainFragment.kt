package com.example.batteryhealth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {

    private lateinit var connectedDeviceButton: Button
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        connectedDeviceButton = view.findViewById(R.id.connected_device_button)

        connectedDeviceButton.setOnClickListener {
            val pairedDeviceName = getPairedDeviceName()
            Toast.makeText(requireContext(), "Connected Device: $pairedDeviceName", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun getPairedDeviceName(): String {
        try {
            if (bluetoothAdapter == null) {
                return "Bluetooth is not available"
            }

            if (!bluetoothAdapter.isEnabled) {
                return "Bluetooth is not enabled"
            }

            val connectedHeadset = bluetoothAdapter.bondedDevices
                .filter { it.type == 1 }
                .firstOrNull { it.isConnected(bluetoothAdapter, requireContext()) }

            return connectedHeadset?.name ?: "No connected headset found"
        } catch (e: SecurityException) {
            return "Error accessing Bluetooth: Permission might be missing"
        }
    }

    @SuppressLint("MissingPermission")
    private fun BluetoothDevice.isConnected(bluetoothAdapter: BluetoothAdapter, context: Context): Boolean {
        return bondState == BluetoothDevice.BOND_BONDED && bluetoothAdapter.getProfileConnectionState(
            BluetoothProfile.HEADSET
        ) == BluetoothAdapter.STATE_CONNECTED
    }
}
