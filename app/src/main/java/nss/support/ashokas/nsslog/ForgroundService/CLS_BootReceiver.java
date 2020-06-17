package nss.support.ashokas.nsslog.ForgroundService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by DARK-DEVIL on 5/28/2020.
 */

public class CLS_BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent rcvIntent=new Intent(context,CLS_callLogService.class);
        try{context.startForegroundService(rcvIntent);}
        catch (Exception e){
            Log.d("bootFinish",e.getMessage());}

    }
}
