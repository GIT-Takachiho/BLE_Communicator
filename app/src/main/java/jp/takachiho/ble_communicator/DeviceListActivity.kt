package jp.takachiho.ble_communicator

//import android.Manifest
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
//import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_device_list.*

class DeviceListActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    // 定数
    private val REQUEST_ENABLEBLUETOOTH = 1 // Bluetooth機能の有効化要求時の識別コード
    private val SCAN_PERIOD: Long = 50000 // スキャン時間。単位はミリ秒。
    val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
    val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"

    // メンバー変数
    // BluetoothAdapter : Bluetooth処理で必要
    private var mBluetoothAdapter : BluetoothAdapter? = null
    // リストビューの内容
    private var mDeviceListAdapter : DeviceListAdapter? = null
    // UIスレッド操作ハンドラ : 「一定時間後にスキャンをやめる処理」で必要
    private var mHandler : Handler? = null
    // スキャン中かどうかのフラグ
    private var mScanning = false

    // デバイススキャンコールバック
    private val mLeScanCallback: ScanCallback = object : ScanCallback() {
        // スキャンに成功（アドバタイジングは一定間隔で常に発行されているため、本関数は一定間隔で呼ばれ続ける）
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            runOnUiThread { mDeviceListAdapter!!.addDevice(result.device) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        // 戻り値の初期化
        setResult(RESULT_CANCELED)

        // リストビューの設定
        mDeviceListAdapter = DeviceListAdapter(this, this) // ビューアダプターの初期化

        val listView = findViewById<ListView>(R.id.devicelist) // リストビューの取得

        listView.adapter = mDeviceListAdapter
        listView.onItemClickListener = this // クリックリスナーオブジェクトのセット

        // UIスレッド操作ハンドラの作成（「一定時間後にスキャンをやめる処理」で使用する）
        mHandler = Handler()

        // Bluetoothアダプタの取得
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        if (null == mBluetoothAdapter) {    // デバイス（＝スマホ）がBluetoothをサポートしていない
            Toast.makeText(this, R.string.bluetooth_is_not_supported, Toast.LENGTH_SHORT).show()
            finish() // アプリ終了宣言
            return
        }

    }

    // 初回表示時、および、ポーズからの復帰時
    override fun onResume() {
        super.onResume()

        // デバイスのBluetooth機能の有効化要求
        requestBluetoothFeature()

        // スキャン開始
        startScan()
    }

    // 別のアクティビティ（か別のアプリ）に移行したことで、バックグラウンドに追いやられた時
    override fun onPause() {
        super.onPause()

        // スキャンの停止
        stopScan()
    }

    // デバイスのBluetooth機能の有効化要求
    private fun requestBluetoothFeature() {
        if (mBluetoothAdapter!!.isEnabled) {
            return
        }
        // デバイスのBluetooth機能が有効になっていないときは、有効化要求（ダイアログ表示）
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLEBLUETOOTH)
    }

    // 機能の有効化ダイアログの操作結果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ENABLEBLUETOOTH -> if (RESULT_CANCELED == resultCode) {    // 有効にされなかった
                Toast.makeText(this, R.string.bluetooth_is_not_working, Toast.LENGTH_SHORT).show()
                finish() // アプリ終了宣言
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // スキャンの開始
    private fun startScan() {
        // リストビューの内容を空にする。
        mDeviceListAdapter!!.clear()

        // BluetoothLeScannerの取得
        // ※Runnableオブジェクト内でも使用できるようfinalオブジェクトとする。
        val scanner = mBluetoothAdapter!!.bluetoothLeScanner ?: return

        // スキャン開始（一定時間後にスキャン停止する）
        mHandler!!.postDelayed({
            mScanning = false
            scanner.stopScan(mLeScanCallback)

            // メニューの更新
            invalidateOptionsMenu()
        }, SCAN_PERIOD)
        mScanning = true
        scanner.startScan(mLeScanCallback)

        // メニューの更新
        invalidateOptionsMenu()
    }

    // スキャンの停止
    private fun stopScan() {
        // 一定期間後にスキャン停止するためのHandlerのRunnableの削除
        mHandler!!.removeCallbacksAndMessages(null)

        // BluetoothLeScannerの取得
        val scanner = mBluetoothAdapter!!.bluetoothLeScanner ?: return
        mScanning = false
        scanner.stopScan(mLeScanCallback)

        // メニューの更新
        invalidateOptionsMenu()
    }

    // オプションメニュー作成時の処理
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_device_list, menu)
        if (!mScanning) {
            menu.findItem(R.id.menuitem_stop).isVisible = false
            menu.findItem(R.id.menuitem_scan).isVisible = true
            menu.findItem(R.id.menuitem_progress).actionView = null
        } else {
            menu.findItem(R.id.menuitem_stop).isVisible = true
            menu.findItem(R.id.menuitem_scan).isVisible = false
            menu.findItem(R.id.menuitem_progress)
                .setActionView(R.layout.actionbar_indeterminate_progress)
        }
        return true
    }

    // オプションメニューのアイテム選択時の処理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuitem_scan -> startScan() // スキャンの開始
            R.id.menuitem_stop -> stopScan() // スキャンの停止
        }
        return true
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // クリックされたアイテムの取得
        val device = mDeviceListAdapter!!.getItem(position) as BluetoothDevice
        // 戻り値の設定
        val intent = Intent()
        intent.putExtra(EXTRAS_DEVICE_NAME, device.name)
        intent.putExtra(EXTRAS_DEVICE_ADDRESS, device.address)
        setResult(RESULT_OK, intent)
        finish()
    }

}

