package org.mifos.mobile.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager

/**
 * Created by rishabhkhanna on 07/03/17.
 */
object Network {
    /**
     * Get the network info
     *
     * @param context
     * @return
     */
    private fun getNetworkInfo(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    /**
     * Check if there is any connectivity
     *
     * @param context
     * @return
     */
    @kotlin.jvm.JvmStatic
    fun isConnected(context: Context?): Boolean {
        val info = getNetworkInfo(context!!)
        if (info != null) {
            return info.isConnected
        }
        return false
    }

    /**
     * Check if there is any connectivity to a Wifi network
     *
     * @param context
     * @param type
     * @return
     */
    fun isConnectedWifi(context: Context): Boolean {
        val info = getNetworkInfo(context)
        if (info != null) {
            return info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
        }
        return false
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @param context
     * @param type
     * @return
     */
    fun isConnectedMobile(context: Context): Boolean {
        val info = getNetworkInfo(context)
        if (info != null) {
            return info.isConnected && info.type == ConnectivityManager.TYPE_MOBILE
        }
        return false
    }

    /**
     * Check if there is fast connectivity
     *
     * @param context
     * @return
     */
    fun isConnectedFast(context: Context): Boolean {
        val info = getNetworkInfo(context)
        if (info != null) {
            return info.isConnected && isConnectionFast(info.type, info.subtype)
        }
        return false
    }

    /**
     * Check if the connection is fast
     *
     * @param type
     * @param subType
     * @return
     */
    private fun isConnectionFast(type: Int, subType: Int): Boolean {
        return when (type) {
            ConnectivityManager.TYPE_WIFI -> {
                true
            }
            ConnectivityManager.TYPE_MOBILE -> {
                when (subType) {
                    TelephonyManager.NETWORK_TYPE_1xRTT -> false // ~ 50-100 kbps
                    TelephonyManager.NETWORK_TYPE_CDMA -> false // ~ 14-64 kbps
                    TelephonyManager.NETWORK_TYPE_EDGE -> false // ~ 50-100 kbps
                    TelephonyManager.NETWORK_TYPE_EVDO_0 -> true // ~ 400-1000 kbps
                    TelephonyManager.NETWORK_TYPE_EVDO_A -> true // ~ 600-1400 kbps
                    TelephonyManager.NETWORK_TYPE_GPRS -> false // ~ 100 kbps
                    TelephonyManager.NETWORK_TYPE_HSDPA -> true // ~ 2-14 Mbps
                    TelephonyManager.NETWORK_TYPE_HSPA -> true // ~ 700-1700 kbps
                    TelephonyManager.NETWORK_TYPE_HSUPA -> true // ~ 1-23 Mbps
                    TelephonyManager.NETWORK_TYPE_UMTS -> true // ~ 400-7000 kbps
                    TelephonyManager.NETWORK_TYPE_EHRPD -> true // ~ 1-2 Mbps
                    TelephonyManager.NETWORK_TYPE_EVDO_B -> true // ~ 5 Mbps
                    TelephonyManager.NETWORK_TYPE_HSPAP -> true // ~ 10-20 Mbps
                    TelephonyManager.NETWORK_TYPE_IDEN -> false // ~25 kbps
                    TelephonyManager.NETWORK_TYPE_LTE -> true // ~ 10+ Mbps
                    TelephonyManager.NETWORK_TYPE_UNKNOWN -> false
                    else -> false
                }
            }
            else -> {
                false
            }
        }
    }
}