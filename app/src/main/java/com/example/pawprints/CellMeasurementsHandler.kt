package com.example.pawprints

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.icu.text.SimpleDateFormat
import android.net.ConnectivityManager
import android.os.Build
import android.os.SystemClock
import android.telephony.CellInfo
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class CellMeasurementsHandler {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getInfo(telephonyManager: TelephonyManager, connectivityManager: ConnectivityManager, settings:SettingsHandler, stringFormat:String, campaignName:String, imei:String, startNanoSec:Long):String
    {

        val allCellInfo = telephonyManager.allCellInfo
        var infoString:String = ""
        if(stringFormat ==  "json")
        {
            val logLineJSON = JSONObject()
            //infoJSON.put("abs_time","")
            logLineJSON.put("rel_time", (SystemClock.elapsedRealtimeNanos() - startNanoSec) / Math.pow(10.0, 9.0))
            val general_connection = GeneralConnectionInfo(telephonyManager).toJSONObj()
            logLineJSON.put("log_type", "cellular")
            logLineJSON.put("network_operator",general_connection.get("network_operator"))
            logLineJSON.put("sim_operator",general_connection.get("sim_operator"))
            logLineJSON.put("sim_carrier_id",general_connection.get("sim_carrier_id"))
            logLineJSON.put("campaign_name", campaignName)
            logLineJSON.put("abs_time",  System.currentTimeMillis())
            val upstreamKbps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.linkUpstreamBandwidthKbps
            val downstreamKbps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.linkDownstreamBandwidthKbps
            logLineJSON.put("uplink_bandwidth_kbps", upstreamKbps)
            logLineJSON.put("downlink_bandwidth_kbps", downstreamKbps)
            logLineJSON.put("device_name",  settings.Device_Name)
            logLineJSON.put("device_imei",  imei)
            var connected_pci = -1

            val cellsJSON = JSONArray()
            val itr = allCellInfo.listIterator()
            while (itr.hasNext())
            {

                val cell = itr.next()
                if (cell is CellInfoLte)
                {
                    val cellLTE = cell as CellInfoLte
                    if (cellLTE.isRegistered)
                    {
                        connected_pci = cellLTE.cellIdentity.pci
                    }
                    val cellJSON = getCellInfo(cellLTE, settings, stringFormat, startNanoSec)
                    cellsJSON.put(cellJSON)
                }

            }
            val nrSigCellInfo = getNRSigStrengthInfo(telephonyManager, "json")
            logLineJSON.put("nr_signal_strength", nrSigCellInfo)
            logLineJSON.put("connected_pci", connected_pci)
            logLineJSON.put("cells", cellsJSON)
            return logLineJSON.toString()
        }
        else if(stringFormat == "display")
        {
              val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
              val currentDate = sdf.format(Date())
              infoString +=  currentDate + "\n\nNumber of base stations detected = " + allCellInfo.size.toString() + "\n"
              if(android.os.Build.VERSION.SDK_INT < Constants.MIN_NR_SIG_STRENGTH_API_VERSION)
              {
                infoString += "\n5G cellular info cannot be detected on this device. Support for API version >= ${Constants.MIN_NR_SIG_STRENGTH_API_VERSION} required.\n"
              }
              if(allCellInfo.size > 0) {
                  val registeredCellInfo = getRegisteredCellInfo(allCellInfo)

                  infoString += "\nFor the connected base station:"
                  infoString += GeneralConnectionInfo(telephonyManager).toDisplayString()
                  infoString += getCellInfo(
                      registeredCellInfo,
                      settings,
                      stringFormat,
                      startNanoSec
                  ).get("string")
                  infoString += getNRSigStrengthInfo(telephonyManager, stringFormat).get("string")

              }
            val upstreamKbps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.linkUpstreamBandwidthKbps
            val downstreamKbps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.linkDownstreamBandwidthKbps
            infoString += "\n Uplink kbps = " + upstreamKbps.toString()
            infoString += "\n Downlink kbps = " + downstreamKbps.toString()
            return infoString
        }
        else
        {
            return ""
        }
    }

    private fun getNRSigStrengthInfo(telephonyManager: TelephonyManager, stringFormat: String):JSONObject
    {
        var infoJSON = JSONObject()
        infoJSON.put("string", "")
        if (telephonyManager.signalStrength != null && android.os.Build.VERSION.SDK_INT >= Constants.MIN_SIG_STRENGTH_API_VERSION) {
            val nrSSInfo = NRSignalStrengthInfo(telephonyManager.signalStrength)
            if (nrSSInfo.nrType.isNotEmpty())
            {
                if(stringFormat == "json") {
                    infoJSON = nrSSInfo.toJSON()
                }
                else if(stringFormat == "display") {
                    infoJSON.put("string", nrSSInfo.toDisplayString())
                }
            }
        }
        return infoJSON
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getCellInfo(cellInfo: CellInfo, settings:SettingsHandler, stringFormat: String, startNanoSec:Long):JSONObject
    {
        var infoJSON = JSONObject()
        var infoString = ""
        if (cellInfo is CellInfoLte)
        {
            val lteInfo = LTEInfo(cellInfo as CellInfoLte, startNanoSec)
            if(stringFormat == "json")
            {
                infoJSON = lteInfo.toJSON()
            }
            else if(stringFormat == "display")
            {
                infoString += lteInfo.toDisplayString()
            }
        }
        else if (cellInfo is CellInfoNr && android.os.Build.VERSION.SDK_INT >= Constants.CELL_INFO_NR_MIN_API_VERSION)
        {
            val nrInfo = NRInfo(cellInfo as CellInfoNr, startNanoSec)
            if(stringFormat == "json")
            {
                infoJSON = nrInfo.toJSON()
            }
            else if(stringFormat == "display")
            {
                infoString += nrInfo.toDisplayString()
            }
        }

        if(stringFormat == "display")
        {
            infoJSON.put("string", infoString)
        }
        return infoJSON
    }

    private fun getRegisteredCellInfo(allCellInfo: List<CellInfo>):CellInfo {
        var registeredCellInfo: CellInfo = allCellInfo.get(0)
        val itr = allCellInfo.listIterator()
        while (itr.hasNext()) {
            val cellInfo = itr.next()
            if (cellInfo.isRegistered) {
                registeredCellInfo = cellInfo
                return registeredCellInfo
            }
        }
        return registeredCellInfo
    }
}