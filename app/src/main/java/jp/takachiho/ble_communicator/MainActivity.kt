package jp.takachiho.ble_communicator

//import BluetoothConnect
//import android.bluetooth.le.BluetoothLeScanner
//import android.bluetooth.le.ScanCallback
//import android.bluetooth.le.ScanResult

//import android.location.LocationManager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
//import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_device_list.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.listitem_device.*



//var mBluetoothLeScanner: BluetoothLeScanner? = null
//var mScanCallback: ScanCallback? = null

class MainActivity : AppCompatActivity() {

    // 定数
    // Bluetooth機能の有効化要求時の識別コード
    private val REQUEST_ENABLEBLUETOOTH = 1
    // デバイス接続要求時の識別コード
    private val REQUEST_CONNECTDEVICE = 2


    // メンバー変数
    // BluetoothAdapter : Bluetooth処理で必要
    private var mBluetoothAdapter : BluetoothAdapter? = null
    // デバイスアドレス
    private var mDeviceAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android端末がBLEをサポートしてるかの確認
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_is_not_supported, Toast.LENGTH_SHORT).show()
            finish() // アプリ終了宣言
            return
        }

        // Bluetoothアダプタの取得
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        if (null == mBluetoothAdapter) {    // Android端末がBluetoothをサポートしていない
            Toast.makeText(this, R.string.bluetooth_is_not_supported, Toast.LENGTH_SHORT).show()
            finish() // アプリ終了宣言
            return
        }
    }

    // 機能の有効化ダイアログの操作結果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ENABLEBLUETOOTH -> if (RESULT_CANCELED == resultCode) {    // 有効にされなかった
                Toast.makeText(this, R.string.bluetooth_is_not_working, Toast.LENGTH_SHORT).show()
                finish() // アプリ終了宣言
                return
            }
            REQUEST_CONNECTDEVICE -> {
                val strDeviceName: String?
                if (RESULT_OK == resultCode) {
                    // デバイスリストアクティビティからの情報の取得
                    if (data != null) {
//                        strDeviceName = data.getStringExtra(DeviceListActivity.EXTRAS_DEVICE)
                    }
                    if (data != null) {
//                        mDeviceAddress = data.getStringExtra(DeviceListActivity.EXTRAS_DEVICE_ADDRESS)!!
                    }
                } else {
                    strDeviceName = "1"
                    mDeviceAddress = "2"
                }
//                (findViewById<TextView>(R.id.textview_devicename)).text = strDeviceName
//                (findViewById<TextView>(R.id.textview_deviceaddress)).text = mDeviceAddress
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }



    // 初回表示時、および、ポーズからの復帰時
    override fun onResume() {
        super.onResume()

        // Android端末のBluetooth機能の有効化要求
        requestBluetoothFeature()
    }

    // Android端末のBluetooth機能の有効化要求
    private fun requestBluetoothFeature() {
        if (mBluetoothAdapter!!.isEnabled) {
            return
        }
        // デバイスのBluetooth機能が有効になっていないときは、有効化要求（ダイアログ表示）
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLEBLUETOOTH)
    }


    // オプションメニュー作成時の処理
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    // オプションメニューのアイテム選択時の処理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuitem_search -> {
                val devicelistactivityIntent = Intent(this, DeviceListActivity::class.java)
                startActivityForResult(devicelistactivityIntent, REQUEST_CONNECTDEVICE)
                return true
            }
        }
        return false
    }

}

