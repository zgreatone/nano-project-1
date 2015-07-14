package net.zgreatone.app.spotifystreamer.util;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by zgreatone on 7/13/15.
 */
public class SpotifyStreamerUtil {

    public static boolean isNetworkAvailable(final Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
