package com.example.pawprints
import android.icu.text.SimpleDateFormat
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import android.telephony.CellInfo
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

class WifiMeasurementsHandler {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getInfo(wifiManager: WifiManager, settings:SettingsHandler, stringFormat:String, campaignName:String, imei:String, startNanoSec:Long):String
    {
        val results = wifiManager.scanResults
        var infoString:String = ""

        if(stringFormat ==  "json")
        {
            val logLineJSON = JSONObject()
            val wifiAPs = JSONArray()

            logLineJSON.put("rel_time", (SystemClock.elapsedRealtimeNanos() - startNanoSec) / Math.pow(10.0, 9.0))
            logLineJSON.put("campaign_name", campaignName)
            logLineJSON.put("abs_time",  System.currentTimeMillis())
            logLineJSON.put("device_name",  settings.Device_Name)
            logLineJSON.put("device_imei",  imei)
            logLineJSON.put("log_type", "wifi")
            
            val wifiInfo = wifiManager.connectionInfo
            logLineJSON.put("connected_network_id", wifiInfo.networkId)

            if (wifiInfo.networkId != -1) {
                val ssid = wifiInfo.ssid.removeSurrounding("\"")
                val bssid = wifiInfo.bssid
                val rssi = wifiInfo.rssi
                val linkSpeed = wifiInfo.linkSpeed
                val frequency = wifiInfo.frequency
                logLineJSON.put("connected_bssid", wifiInfo.bssid)
                logLineJSON.put("connected_ssid", wifiInfo.ssid)
                logLineJSON.put("connected_rssi", wifiInfo.rssi)
                logLineJSON.put("connected_link_speed", wifiInfo.linkSpeed)
                logLineJSON.put("connected_rx_link_speed_mbps", wifiInfo.rxLinkSpeedMbps)
                logLineJSON.put("connected_tx_link_speed_mbps", wifiInfo.txLinkSpeedMbps)
                logLineJSON.put("connected_max_rx_link_speed_mbps", wifiInfo.maxSupportedRxLinkSpeedMbps)
                logLineJSON.put("connected_max_tx_link_speed_mbps", wifiInfo.maxSupportedTxLinkSpeedMbps)
                logLineJSON.put("connected_frequency", wifiInfo.frequency)
                logLineJSON.put("connected_network_id", wifiInfo.networkId)

            }
            val itr = results.listIterator()

            while(itr.hasNext())
            {
                val wifiAP = getAPInfo(itr.next(), settings, stringFormat, startNanoSec)
                wifiAPs.put(wifiAP)
            }
            logLineJSON.put("num_detected", results.size)

            logLineJSON.put("aps", wifiAPs)
            return logLineJSON.toString()
        }
        else if(stringFormat == "display")
        {
            return infoString
        }
        else
        {
            return ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getAPInfo(apsInfo: ScanResult, settings:SettingsHandler, stringFormat: String, startNanoSec:Long):JSONObject
    {
        var infoJSON = JSONObject()
        var infoString = ""

        if(stringFormat == "display")
        {
            infoJSON.put("string", infoString)
        }
        else if(stringFormat == "json")
        {
            infoJSON = WiFiInfo(apsInfo, startNanoSec).toJSON()

        }
        return infoJSON
    }
}