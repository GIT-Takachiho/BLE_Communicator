package jp.takachiho.ble_communicator

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.*
import kotlin.collections.ArrayList


class DeviceListAdapter(context: Context, activity: Activity) : BaseAdapter(){

    private var mDeviceList: ArrayList<BluetoothDevice>? = null
    private var mInflator: LayoutInflater? = null

    init {
        mDeviceList = ArrayList()
        mInflator = activity.layoutInflater
    }

    // リストへの追加
    fun addDevice(device: BluetoothDevice) {
        if (!mDeviceList!!.contains(device)) {    // 加えられていなければ加える
            mDeviceList!!.add(device)
            notifyDataSetChanged() // ListViewの更新
        }
    }

    // リストのクリア
    fun clear() {
        mDeviceList!!.clear()
        notifyDataSetChanged() // ListViewの更新
    }

    override fun getCount(): Int {
        return mDeviceList!!.size
    }

    override fun getItem(position: Int): Any {
        return mDeviceList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    internal class ViewHolder {
        var deviceName: TextView? = null
        var deviceAddress: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertview = convertView
        val viewHolder: ViewHolder
        // General ListView optimization code.
        if (null == convertview) {
            convertview = mInflator!!.inflate(R.layout.listitem_device, parent, false)
            viewHolder = ViewHolder()
            viewHolder.deviceAddress =
                convertview.findViewById<View>(R.id.textview_deviceaddress) as TextView
            viewHolder.deviceName =
                convertview.findViewById<View>(R.id.textview_devicename) as TextView
            convertview.setTag(viewHolder)
        } else {
            viewHolder = convertview.tag as ViewHolder
        }
        val device = mDeviceList!![position]
        val deviceName = device.name
        if (null != deviceName && 0 < deviceName.length) {
            viewHolder.deviceName!!.setText(deviceName)
        } else {
            viewHolder.deviceName!!.setText(R.string.unknown_device)
        }
        viewHolder.deviceAddress!!.setText(device.address)
        return convertview
    }
}