package com.sisalma.vehicleandusermanagement.model

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleBinding
import com.sisalma.vehicleandusermanagement.databinding.ViewUserSummaryBinding
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sisalma.vehicleandusermanagement.databinding.FragmentEmptyContainerBinding
import com.sisalma.vehicleandusermanagement.fragment_empty_container

class bluetoothLEDevice (adapter: BluetoothAdapter, context: Context){
    private val scanResult: MutableList<BluetoothDevice> = arrayListOf()
    private val deviceScanResult: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    val LiveDeviceScanResult: LiveData<List<BluetoothDevice>> get() = deviceScanResult

    private val bluetoothLeScanner = adapter.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD = 10000L

    init {
        if(ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
    }
    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            scanResult.add(result.device)
        }
    }
    @SuppressLint("MissingPermission")
    private fun scanLeDevice() {
        deviceScanResult.value
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                deviceScanResult.value = scanResult
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
            deviceScanResult.value = scanResult
        }
    }
}

class LeDeviceListAdapter(val listener: (Int) -> Unit): RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder>() {
    val deviceList: MutableList<BluetoothDevice> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewUserSummaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val temp = deviceList.get(position)
        holder.setValue(temp.name,temp.uuids.toString())
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    fun refreshDeviceList(device: List<BluetoothDevice>){
        device.let {
            if(it.count() > 0){
                deviceList.clear()
                deviceList.addAll(device)
            }else{
                deviceList.clear()
            }
        }
        this.notifyDataSetChanged()
    }

    inner class ViewHolder(binding: ViewUserSummaryBinding, listener: (Int) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        val vehicleNameView: TextView = binding.tvMainName
        val vehicleVID: TextView = binding.tvSecondaryValue

        private val binding = binding
        init {
            binding.tvSecondaryName.text = "Vehicle ID:"
        }
        fun setValue(name: String, VID: String){
            vehicleNameView.text = name
            vehicleVID.text = VID
        }
    }
}

class EmptyContainer(): RecyclerView.Adapter<EmptyContainer.ViewHolder>() {
    var message = ""
    inner class ViewHolder(val binding: FragmentEmptyContainerBinding): RecyclerView.ViewHolder(binding.root){
        fun updateText(message: String){
            if(message.isEmpty() or message.isBlank()){
                binding.textView3.text = "Tunggu kami mencari data !"
            }else{
                binding.textView3.text = message
            }
        }
    }

    fun updateMessage(message: String){
        this.message = message
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FragmentEmptyContainerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.updateText(message)
    }

    override fun getItemCount(): Int {
        return 1;
    }
}
