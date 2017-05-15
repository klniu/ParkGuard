package cn.com.watchman.utils;

import android.content.Context;
import android.location.LocationManager;

/**
 * 文件名：WMyUtils
 * 描    述：工具类
 * 作    者：stt
 * 时    间：2017.05.12
 * 版    本：V1.0.0
 */

public class WMyUtils {
    /**
     * 方法名：isOPen
     * 功    能：判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * 参    数： Context context
     * 返回值：boolean
     */
    public static final boolean isOpen(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps) {
            return true;
        }

        return false;
    }
}
