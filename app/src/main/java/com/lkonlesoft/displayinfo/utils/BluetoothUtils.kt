package com.lkonlesoft.displayinfo.utils

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.BondedDeviceInfo
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo

class BluetoothUtils(private val context: Context) {
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val connectionState = mapOf (
        BluetoothProfile.STATE_CONNECTED to context.getString(R.string.connected),
        BluetoothProfile.STATE_DISCONNECTED to context.getString(R.string.disconnected),
    )

    private val deviceType = mapOf(
        BluetoothDevice.DEVICE_TYPE_CLASSIC to context.getString(R.string.classic),
        BluetoothDevice.DEVICE_TYPE_LE to context.getString(R.string.le),
        BluetoothDevice.DEVICE_TYPE_DUAL to context.getString(R.string.dual),
        BluetoothDevice.DEVICE_TYPE_UNKNOWN to context.getString(R.string.unknown)
    )

    private fun getMajorDeviceClassName(major: Int): String = when (major) {
        0x0000 -> context.getString(R.string.miscellaneous)
        0x0100 -> context.getString(R.string.computer)
        0x0200 -> context.getString(R.string.phone)
        0x0300 -> context.getString(R.string.audio_video)
        0x0400 -> context.getString(R.string.peripheral)
        0x0500 -> context.getString(R.string.imaging)
        0x0600 -> context.getString(R.string.network)
        0x0700 -> context.getString(R.string.wearable)
        0x0800 -> context.getString(R.string.toy)
        0x0900 -> context.getString(R.string.health)
        else   -> context.getString(R.string.unknown) + " (0x${major.toString(16)})"
    }

    fun isEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getName(): String {
        return bluetoothAdapter?.name ?: context.getString(R.string.unknown)
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getHeadsetConnectionState(): String {
        val state = bluetoothAdapter?.getProfileConnectionState(BluetoothProfile.HEADSET)
        return connectionState[state] ?: context.getString(R.string.unknown)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getD2dpConnectionState(): String {
        val state = bluetoothAdapter?.getProfileConnectionState(BluetoothProfile.A2DP)
        return connectionState[state] ?: context.getString(R.string.unknown)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getLEAudioConnectionState(): String {
        val state = bluetoothAdapter?.getProfileConnectionState(BluetoothProfile.LE_AUDIO)
        return connectionState[state] ?: context.getString(R.string.unknown)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getStateData(): List<DeviceInfo> {
        return listOf(
            DeviceInfo(R.string.bluetooth, if (isEnabled()) context.getString(R.string.enabled) else context.getString(R.string.disabled)),
            DeviceInfo(R.string.name, getName()),
            DeviceInfo(R.string.headset_connection, getHeadsetConnectionState()),
            DeviceInfo(R.string.d2dp_connection, getD2dpConnectionState()),
            DeviceInfo(R.string.le_audio_connection, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getLEAudioConnectionState()
            else context.getString(R.string.n_a)),
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getFeatures(): List<DeviceInfo> {
        val multiAdvertise = bluetoothAdapter?.isMultipleAdvertisementSupported ?: false
        val offloadFiltering = bluetoothAdapter?.isOffloadedFilteringSupported ?: false
        val offloadScanning = bluetoothAdapter?.isOffloadedScanBatchingSupported ?: false
        val lePeriodicAdvertise = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bluetoothAdapter?.isLePeriodicAdvertisingSupported ?: false
        } else {
            false
        }
        val leExtendedAdvertise = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bluetoothAdapter?.isLeExtendedAdvertisingSupported ?: false
        } else {
            false
        }
        val le2MPhy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bluetoothAdapter?.isLe2MPhySupported ?: false
        } else {
            false
        }
        val leCodedPhy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bluetoothAdapter?.isLeCodedPhySupported ?: false
        } else {
            false
        }
        val leAudio = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothAdapter?.isLeAudioSupported == BluetoothStatusCodes.FEATURE_SUPPORTED
        } else {
            false
        }
        return listOf(
            DeviceInfo(R.string.bl_multiple_advertise, if (multiAdvertise) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
            DeviceInfo(R.string.bl_offloaded_filtering, if (offloadFiltering) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
            DeviceInfo(R.string.bl_offloaded_scan_batching, if (offloadScanning) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
            DeviceInfo(R.string.bl_le_periodic_advertise, if (lePeriodicAdvertise) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
            DeviceInfo(R.string.bl_le_extended_advertise, if (leExtendedAdvertise) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
            DeviceInfo(R.string.bl_le_2m_phy, if (le2MPhy) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
            DeviceInfo(R.string.bl_le_coded_phy, if (leCodedPhy) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
            DeviceInfo(R.string.le_audio, if (leAudio) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
        )
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getDeviceData(): List<List<DeviceInfo>> {
        if (bluetoothAdapter == null || !isEnabled())
            return emptyList()
        val devices = bluetoothAdapter?.bondedDevices ?: emptySet()
        if (devices.isEmpty())
            return emptyList()
        val deviceList = mutableListOf<BondedDeviceInfo>()
        devices.filterNotNull().forEach { device ->
            deviceList.add(
                BondedDeviceInfo(
                    uuid = device.uuids?.joinToString("\n") ?: context.getString(R.string.unknown),
                    name = device.name ?: context.getString(R.string.unknown),
                    address = device.address ?: context.getString(R.string.unknown),
                    type = deviceType[device.type] ?: context.getString(R.string.unknown),
                    bluetoothClass = getMajorDeviceClassName(device.bluetoothClass.majorDeviceClass),
                )
            )
        }
        return deviceList.map { device ->
            listOf(
                DeviceInfo(R.string.uuid, device.uuid),
                DeviceInfo(R.string.name, device.name),
                DeviceInfo(R.string.address, device.address),
                DeviceInfo(R.string.type, device.type),
                DeviceInfo(R.string.major_device_class, device.bluetoothClass)
            )
        }
    }
}