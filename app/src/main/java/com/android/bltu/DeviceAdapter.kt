package com.android.bltu

import android.bluetooth.BluetoothDevice
import com.android.bltu.databinding.ItemDeviceListBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

/**
 *  author : wsy
 *  date   : 2023/5/11
 *  desc   :
 */
class DeviceAdapter :
    BaseQuickAdapter<BluetoothDevice, BaseDataBindingHolder<ItemDeviceListBinding>>(R.layout.item_device_list) {
    override fun convert(holder: BaseDataBindingHolder<ItemDeviceListBinding>, item: BluetoothDevice) {
        holder.dataBinding?.run {
            setVariable(BR.model, item)
            executePendingBindings()
        }
    }
}