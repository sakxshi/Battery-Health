package com.example.batteryhealth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {

    private lateinit var connectedDeviceButton: Button
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val REQUEST_BLUETOOTH_PERMISSION = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        // Find the button by its ID
        connectedDeviceButton = view.findViewById(R.id.connected_device_button)

        // Set a click listener for the button
        connectedDeviceButton.setOnClickListener {
            // Check for Bluetooth permissions
            if (checkBluetoothPermissions()) {
                // Get the name of the paired device
                val pairedDeviceName = getPairedDeviceName()

                // Show the device name in a Toast message
                Toast.makeText(requireContext(), "Connected Device: $pairedDeviceName", Toast.LENGTH_SHORT).show()
            } else {
                // Request Bluetooth permissions
                requestBluetoothPermissions()
            }
        }

        return view
    }

    // Helper method to check for Bluetooth permissions
    private fun checkBluetoothPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Helper method to request Bluetooth permissions
    private fun requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT),
            REQUEST_BLUETOOTH_PERMISSION
        )
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions granted, proceed with Bluetooth operations
                val pairedDeviceName = getPairedDeviceName()
                Toast.makeText(requireContext(), "Connected Device: $pairedDeviceName", Toast.LENGTH_SHORT).show()
            } else {
                // Some or all permissions denied, handle accordingly (e.g., show a message to the user)
                Toast.makeText(requireContext(), "Bluetooth permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper method to get the name of the paired device
    private fun getPairedDeviceName(): String {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return "Bluetooth permission not granted"
        }

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
            // Handle SecurityException (e.g., log the error, show a message to the user)
            return "Error accessing Bluetooth: Permission might be missing"
        }
    }

    // Helper method to check if a Bluetooth device is connected
    private fun BluetoothDevice.isConnected(bluetoothAdapter: BluetoothAdapter, context: Context): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission here (using ActivityCompat.requestPermissions)
            // Handle the result in onRequestPermissionsResult
            return false // Return false for now, as the connection cannot be checked without permission
        }

        // Permission granted, proceed with connection check
        return bondState == BluetoothDevice.BOND_BONDED && bluetoothAdapter.getProfileConnectionState(
            BluetoothProfile.HEADSET
        ) == BluetoothAdapter.STATE_CONNECTED
    }
}