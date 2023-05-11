package com.android.bltu

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.bltu.databinding.ActivityMainBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    // 蓝牙串口设备的缩写是 SPP
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream = MutableLiveData<OutputStream>()

    private lateinit var mBinding: ActivityMainBinding
    private val deviceAdapter by lazy { DeviceAdapter() }
    private var myReceiver: MyReceiver? = null

    // 获取系统蓝牙适配器
    private var mBtAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initView()
        initData()
        startObserver()
    }

    private fun startObserver() {
        outputStream.observe(this) {
            it?.let {
                send("hello yyx")
            }
        }
    }


    override fun onResume() {
        super.onResume()
        //注册广播
        myReceiver = MyReceiver()
        val foundFilter = IntentFilter()
        foundFilter.apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)// 蓝牙状态改变的广播
            addAction(BluetoothDevice.ACTION_FOUND)// 找到设备的广播
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)// 搜索完成的广播
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)// 开始扫描的广播
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)// 状态改变
        }
        registerReceiver(myReceiver, foundFilter)
    }

    override fun onPause() {
        super.onPause()
        myReceiver?.let {
            unregisterReceiver(it)
        }
    }

    private fun initView() {
        mBinding.apply {
            rvDevice.run {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = deviceAdapter
            }

            btnSearch.setOnClickListener {
                startDiscovery()
            }
        }
        if (!isBtEnable()) {
            openBt()
        }
        mBtAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        checkCameraPermission()


        // 列表监听
        deviceAdapter.setOnItemClickListener { adapter, view, position ->
            startTimer()
            // 获取蓝牙的socket
            bluetoothSocket =
                deviceAdapter.data[position].createRfcommSocketToServiceRecord(SPP_UUID)
            // 建立连接,会阻塞线程，在子线程执行
            lifecycle.coroutineScope.launch(Dispatchers.IO) {
                bluetoothSocket?.connect()
            }
            // 获取输出、输入流
            inputStream = bluetoothSocket?.inputStream
            outputStream.value = bluetoothSocket?.outputStream
        }

    }

    /**
     * 开启定时任务,定时获取流
     * 由于bluetoothSocket连接在子线程执行，可能需要开启定时任务去获取流
     */
    private fun startTimer() {
        // 定时任务
        Timer().schedule(object : TimerTask() {
            override fun run() {
                outputStream.value ?: let {
                    bluetoothSocket?.let {
                        outputStream.value = it.outputStream
                        // 结束任务
                        cancel()
                    }
                }
            }
        }, 1000)
    }


    private fun initData() {

    }

    /**
     * 发送
     * @param msg 内容
     */
    fun send(msg: String) {
        try {
            outputStream.value?.write(msg.toByteArray(Charsets.UTF_8))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 判断蓝牙是否打开
     */
    private fun isBtEnable(): Boolean = mBtAdapter?.isEnabled == true


    /**
     * 打开蓝牙
     */
    private fun openBt() {
        mBtAdapter?.enable()
    }

    /**
     * 开始搜索
     */
    private fun startDiscovery() {
        if (mBtAdapter?.isDiscovering == true)
            mBtAdapter?.cancelDiscovery()
        mBtAdapter?.startDiscovery()
    }

    /**
     * 搜索到新设备广播广播接收器
     */
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                // 找到设备的广播
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    Log.e(packageName, "找到设备${device?.name}")
                    device?.let {
                        deviceAdapter.data.let {
                            if (it.size > 0) deviceAdapter.addData(device)
                            else deviceAdapter.setList(
                                listOf(device)
                            )
                        }
                    }
                }
                // 搜索完成的广播
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Toast.makeText(context, "搜索完成", Toast.LENGTH_SHORT).show()
                }
                // 蓝牙状态改变的广播
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    Toast.makeText(context, "蓝牙状态改变", Toast.LENGTH_SHORT).show()
                }
                // 开始扫描的广播
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Toast.makeText(context, "开始搜索", Toast.LENGTH_SHORT).show()
                }
                // 状态改变
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    when (device?.bondState) {
                        BluetoothDevice.BOND_NONE -> Log.e(packageName, "取消配对")

                        BluetoothDevice.BOND_BONDING -> Log.e(packageName, "配对中")

                        BluetoothDevice.BOND_BONDED -> Log.e(packageName, "配对成功")
                    }
                }
            }
        }
    }

    // 请求权限
    private fun checkCameraPermission() {
        if (EasyPermissions.hasPermissions(this, ACCESS_COARSE_LOCATION)) {
            // 有定位权限
//            Toast.makeText(this, "有权限，为所欲为", Toast.LENGTH_SHORT).show()
        } else {
            // 无定位权限
            EasyPermissions.requestPermissions(
                this,
                "应用程序需要访问您的位置信息,您需要在下个弹窗中允许我们使用定位",
                LOCATION_REQUEST_CODE,
                ACCESS_COARSE_LOCATION
            )
        }
    }

    // 拒绝权限请求
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Toast.makeText(this, "您拒绝授权,并勾选了不在提醒$LOCATION_REQUEST_CODE", Toast.LENGTH_SHORT).show()
            AppSettingsDialog.Builder(this).setTitle("打开应用程序设置修改应用程序权限").build().show()
        } else {
            Toast.makeText(this, "您拒绝授权$LOCATION_REQUEST_CODE", Toast.LENGTH_SHORT).show()
            checkCameraPermission()
        }
    }

    // 同意权限请求
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(this, "您同意了授权$LOCATION_REQUEST_CODE", Toast.LENGTH_SHORT).show()
        checkCameraPermission()
    }
}