package com.example.pawprints
import android.net.wifi.ScanResult
import android.os.Build
import android.os.SystemClock
import android.telephony.CellInfoLte
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.Q)
class WiFiInfo(wifiScanResult:ScanResult, refTime: Long) {
    private var bssid: String = ""
    private var capablites: String = ""
    private var centerFreq0: Int = -1
    private var centerFreq1: Int = -1
    private var channelWidth: Int = -1
    private var frequency: Int = -1
    private var level: Int = -1
    private var update_timestamp: Long = -1
    private var ssid: String = ""
    private var standard: Int = -1
    private var bIs80211mcResponder: Boolean

    init {
        this.bssid = wifiScanResult.BSSID
        this.capablites = wifiScanResult.capabilities.toString()
        this.centerFreq0 = wifiScanResult.centerFreq0
        this.centerFreq1 = wifiScanResult.centerFreq1
        this.channelWidth = wifiScanResult.channelWidth
        this.frequency = wifiScanResult.frequency
        this.level = wifiScanResult.level
        this.update_timestamp = wifiScanResult.timestamp
        this.ssid = wifiScanResult.SSID
        this.standard = wifiScanResult.wifiStandard
        this.bIs80211mcResponder = wifiScanResult.is80211mcResponder
    }

    public fun toJSON(): JSONObject{
        val jsonObject = JSONObject()
        jsonObject.put("bssid", this.bssid)
        jsonObject.put("ssid", this.ssid)
        jsonObject.put("capablites", this.capablites)
        jsonObject.put("centerFreq0", this.centerFreq0)
        jsonObject.put("centerFreq1", this.centerFreq1)
        jsonObject.put("channelWidth", this.channelWidth)
        jsonObject.put("frequency", this.frequency)
        jsonObject.put("level", this.level)
        jsonObject.put("update_timestamp", this.update_timestamp)
        jsonObject.put("is80211mcResponder", this.bIs80211mcResponder)
        // Add key-value pairs to the JSON object
//        jsonObject.put("is_connected", this.bConnected)
        return jsonObject
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public fun toDisplayString(): String
    {
        var displayString = "\nBSSID = " + this.bssid.toString()
        displayString += "\nSSID = " + this.ssid.toString()
        return displayString
    }
}



