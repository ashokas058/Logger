package nss.support.ashokas.nsslog.ForgroundService;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import nss.support.ashokas.nsslog.ACT_home;
import nss.support.ashokas.nsslog.Networking.RequestNetwork;
import nss.support.ashokas.nsslog.SysInterface.OnserviceComplete;

import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.BRD_ACTIVITY_Action;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.ISRUNNING;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.LOGSUC;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.channel_id;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.createLogdataExtnl;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.db;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.executorService;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getApikey;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getCacheDb;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getCallcenter;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getDateTimeCall;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getSysTime;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.postCacheDb;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.postDataServer;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.scheduledExec;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.setcacheDb;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.SERVICE_START_TIME;

/**
 * Created by DARK-DEVIL on 5/2/2020.
 */

// change in cursor while loop


public class CLS_callLogService extends Service {
    Cursor cursor;
    RequestNetwork internetapi=null;
    RequestNetwork.RequestListener _internetapi_request_listener = null;
    callLgManager callLgManager;
    boolean isRunning;
    IBinder binder=null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder ;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //isRunning=true;
        ISRUNNING=true;
        try{SERVICE_START_TIME= Instant.now();}
        catch (Exception e){}

        binder=new localServicePvdr();
        sendNtwrkbrdCast(true);

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            Intent activityIntent=new Intent(this,ACT_home.class);
            PendingIntent pendingIntent=PendingIntent.getActivity(this,0,activityIntent,0);

            Notification callLogNotfy=new NotificationCompat.Builder(this,channel_id)
                    .setContentTitle("Call Log Service").setContentText("Service started")
                    .setContentIntent(pendingIntent).build();
                     startForeground(1,callLogNotfy);

         callLgManager=new callLgManager();
         callLgManager.start();
        // insertDBServiceStat("true");
            //callThreadPoolExec();
            //scheduledExec.schedule(getScheduledExcRunnable(),2, TimeUnit.SECONDS);

        }


        catch(Exception e){
            Log.d("ErrorService",e.getMessage());
        }


        return  START_NOT_STICKY;
    }

    private void callProvider() {
        try {Log.d("inside","threadSch1");
            cursor = getCursorCallLg();
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    boolean read_log = Boolean.valueOf(cursor.getString(cursor.getColumnIndex(CallLog.Calls.IS_READ)));

                    String callId = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID));
                    //Log.d("callsee", String.valueOf(read_log));

                    String longDate = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
                    String[] datArray =getDateTimeCall(longDate);
                    String callDate = datArray[0];
                    String callTime = datArray[1];
                    String callGiotag = (cursor.getString(cursor.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION))) == null ? "NO LOC" :
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION));
                    String callNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));

                    HashMap callMap = new HashMap();
                    callMap.put("C_MOBI",callNumber);
                    callMap.put("C_DATE",callDate);
                    callMap.put("C_TIME",callTime);
                    callMap.put("CC_MOBI",getCallcenter());
                    callMap.put("CC_DATE", getSysTime());
                    callMap.put("CC_KEY",getApikey());
                    callMap.put("CC_IMIE","No Record");

                    if(isNetwrkConnected()) {
                        Log.d("netavlble","=connected");

                        if (getCacheDb().getCount() < 1 || getCacheDb() == null) {
                            Log.d("cache","No cache");
                            postDataServer(callMap, new OnserviceComplete() {
                                @Override
                                public void Oncomplete(String complete) {
                                    createLogdataExtnl(complete+ "Status=" + LOGSUC);
                                }
                            });

                        } else {
                            Log.d("cache","cache data Avilable");
                            postCacheDb(getCacheDb(), new OnserviceComplete() {
                                @Override
                                public void Oncomplete(String complete) {
                                    createLogdataExtnl(complete);
                                }
                            });
                            postDataServer(callMap, new OnserviceComplete() {
                                @Override
                                public void Oncomplete(String complete) {
                                    createLogdataExtnl(complete.toString() + "Status=" + LOGSUC);
                                }
                            });

                        }

                    }
                    else{
                        Log.d("netwrk","No network");
                        setcacheDb(callMap, new OnserviceComplete() {
                            @Override
                            public void Oncomplete(String  complete) {
                               createLogdataExtnl(complete);
                            }
                        });

                    }
                    markCallLogRead(Integer.parseInt(callId));
                }
            }
            cursor.close();

        }
        catch (Exception e){
            Log.d("callProvider",e.getMessage());}
        // markCallLogRead(Integer.parseInt(callId));
    }


    public void markCallLogRead(int id) {
        Uri CALLLOG_URI = CallLog.Calls.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put("is_read", true);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED)
            this.getContentResolver().update(CALLLOG_URI, values, "_ID=?",new String[]{String.valueOf(id)});
    }

    private Cursor getCursorCallLg() {

        Cursor cursor = null;
        final String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";
        StringBuffer sb = new StringBuffer();
        sb.append(CallLog.Calls.TYPE).append("=?").append(" and ").append(CallLog.Calls.IS_READ).append("=?");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            cursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, sb.toString(), new String[]{String.valueOf(CallLog.Calls.MISSED_TYPE), "0"}, sortOrder);

        }
        return  cursor;

    }
   public  class  callLgManager extends  Thread{
       @Override
       public void run() {
           super.run();

           while (ISRUNNING){

               callProvider();
               try {
                   sleep(4000);
                   if(isNetwrkConnected()){
                   postCacheDb(getCacheDb(), new OnserviceComplete() {
                       @Override
                       public void Oncomplete(String complete) {
                           createLogdataExtnl(complete);
                       }
                   });}
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
       }
   }
   private  void  sendNtwrkbrdCast(boolean state){
        Intent intent=new Intent();
        intent.setAction(BRD_ACTIVITY_Action);
        intent.putExtra("state",state);
        sendBroadcast(intent);
   }
   public   boolean isNetwrkConnected(){
       ConnectivityManager manager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo nwrkInfo=manager.getActiveNetworkInfo();
       return  nwrkInfo!=null &&nwrkInfo.isConnected();

   }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //isRunning=false;
        ISRUNNING=false;
        sendNtwrkbrdCast(false);
        //scheduledExec.shutdown();

        //insertDBServiceStat("false");
    }
@Deprecated
    private  void insertDBServiceStat(String stat){
       try {
         ContentValues values=new ContentValues();
         values.put("STATE",stat);
         String where="id=?";
         db.update("SERVICE",values,where,new String[]{String.valueOf(1)});

       }catch (Exception e){Log.d("error Service",e.getMessage());}
    }
    @Deprecated
    public class localServicePvdr extends Binder{

        public  CLS_callLogService getServiceInstance(){
            return CLS_callLogService.this;
        }

    }
@Deprecated
private  void callThreadPoolExec(){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (ISRUNNING){

                    callProvider();
                    if(isNetwrkConnected()){
                        postCacheDb(getCacheDb(), new OnserviceComplete() {
                            @Override
                            public void Oncomplete(String complete) {
                                createLogdataExtnl(complete);
                            }
                        });}
                }
            }
        });
}
@Deprecated
private  Runnable getScheduledExcRunnable(){


    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            callProvider();
            if(isNetwrkConnected()){
                postCacheDb(getCacheDb(), new OnserviceComplete() {
                    @Override
                    public void Oncomplete(String complete) {
                        createLogdataExtnl(complete);
                    }
                });}
        }
    };
    return  runnable;
}
}
