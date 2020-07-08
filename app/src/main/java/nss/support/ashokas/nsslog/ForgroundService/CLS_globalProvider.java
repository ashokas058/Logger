package nss.support.ashokas.nsslog.ForgroundService;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import nss.support.ashokas.nsslog.Networking.RequestNetwork;
import nss.support.ashokas.nsslog.Networking.RequestNetworkController;
import nss.support.ashokas.nsslog.SysInterface.OnserviceComplete;

/**
 * Created by DARK-DEVIL on 5/2/2020.
 */

public class CLS_globalProvider extends Application {

    public static final String  channel_id="CallLogChannel";
    String tempUrl="https://ktunss.pythonanywhere.com/console/service/";
    String tempKey="";
    public static  SQLiteDatabase db;
    SharedPreferences preferences;
    static Cursor cursorApi;
    public  static boolean ISRUNNING=false;
    public static Instant SERVICE_START_TIME;
    public  static  String LOGSUC="sucess";
    public  static  String LOGCACHE="cached";
    public  static  String LOGNTWRKF="network error/qued";
    public final static String BRD_ACTIVITY_Action="HOME_BRD_ACT";
    public final static String BRD_SERVICE_Action="SERVICE_BRD_ACT";
    public final static String BRD_SStateData ="SERVICE_STATE";
    public final static String BRD_AData ="PING_SERVICE";
    private  static int NUMBER_OF_CORE=Runtime.getRuntime().availableProcessors();
    private  static final int TIME_ALIVE_SEC=1;
    private  static  final java.util.concurrent.TimeUnit TIME_ALIVE_UNIT= java.util.concurrent.TimeUnit.SECONDS;
    private  final BlockingQueue<Runnable> workQ=new LinkedBlockingQueue<Runnable>();
    public  static ThreadPoolExecutor  poolExecutor;
    public  static ExecutorService  executorService;
    public   static ScheduledExecutorService scheduledExec;

    public static RequestNetwork internetapi=null;
    public  static RequestNetwork.RequestListener _internetapi_request_listener = null;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences=getSharedPreferences("appConf", Context.MODE_PRIVATE);
        poolExecutor=new ThreadPoolExecutor(NUMBER_OF_CORE,NUMBER_OF_CORE,TIME_ALIVE_SEC,TIME_ALIVE_UNIT,workQ);
       // executorService= Executors.newFixedThreadPool(2);
        scheduledExec= Executors.newScheduledThreadPool(1);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel callLogCh=new NotificationChannel(channel_id,"CallLog Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager= getSystemService(NotificationManager.class);
            manager.createNotificationChannel(callLogCh);
        }
        createDB();
        initialize();

    }

    private  void createDB (){
        try {
            db = openOrCreateDatabase("serviceStat", MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS SERVICE (ID INTEGER PRIMARY KEY,STATE TEXT,APIURL TEXT,APIKEY TEXT,CALLCENTER TEXT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS LOGDATA (LID INTEGER PRIMARY KEY AUTOINCREMENT,CALLNUMBER TEXT,CALLDATE TEXT,CALLTIME TEXT)");
            Cursor cursor = db.rawQuery("SELECT * FROM SERVICE", null);
            Log.d("er_check db global",String.valueOf(cursor.getCount()));
            if (cursor.getCount() < 1) {
                ContentValues valueDb=new ContentValues();
                valueDb.putNull("ID");
                valueDb.put("STATE","false");
                valueDb.put("APIKEY",tempKey);
                valueDb.put("APIURL",tempUrl);
                valueDb.put("CALLCENTER","xxxxx");
                db.insert("SERVICE",null,valueDb);
            }
        }catch (Exception e){
            Log.d("Error Application",e.getMessage());}
    }

    public  static  String  getApikey(){
        cursorApi =db.rawQuery("SELECT * FROM SERVICE",null);
        cursorApi.moveToNext();
        return  cursorApi.getString(cursorApi.getColumnIndex("APIKEY"));
    }
    public  static  String getApiUrl(){
        cursorApi =db.rawQuery("SELECT * FROM SERVICE",null);
        cursorApi.moveToNext();
        return cursorApi.getString(cursorApi.getColumnIndex("APIURL"));

    }

    public  static  String getVolentiNumber(){
        cursorApi =db.rawQuery("SELECT * FROM SERVICE",null);
        cursorApi.moveToNext();
        return  cursorApi.getString(cursorApi.getColumnIndex("CALLCENTER"));
    }

    public static String[] getDateTimeCall(String date) {
        long seconds = Long.parseLong(date);
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        String dateString = formatter.format(new Date(seconds));
        return dateString.split(" ", 2);
    }

    public static String getSysTime() {
        //String sec = String.valueOf(calendar.get(Calendar.SECOND));
        DateFormat dateFormat = new SimpleDateFormat("hh.mm aa");
        return dateFormat.format(new Date()).toString();
    }
    public static String getSysDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        return formatter.format(new Date()).toString();
    }
    public  static  Cursor getCursorApi(){
        return  cursorApi;
    }
    public  static String  getCallcenter(){
        try{
            cursorApi =db.rawQuery("SELECT * FROM SERVICE",null);
            cursorApi.moveToNext();
        }
        catch (Exception e){createLogdataExtnl(e.getMessage()+"-"+getSysTime());}

        finally {
            return  cursorApi.getString(cursorApi.getColumnIndex("CALLCENTER"));
        }
    }

    public  static void  setApiKey(String key){
        try {
            ContentValues values=new ContentValues();
            values.put("APIKEY",key);
            String where="id=?";
            db.update("SERVICE",values,where,new String[]{String.valueOf(1)});

        }catch (Exception e){Log.d("error Service",e.getMessage());createLogdataExtnl(e.getMessage()+"-"+getSysTime());}
    }

    public  static  Cursor getCacheDb(){
        Cursor cursor=null;
     try{
         cursor=db.rawQuery("SELECT * FROM LOGDATA",null);


     }catch(Exception e){}
     finally {
         return  cursor;
     }

}
    public  static  void  setcacheDb(HashMap hash,OnserviceComplete onserviceComplete){
        try{
            ContentValues cacheValue=new ContentValues();
            cacheValue.put("CALLNUMBER",hash.get("C_MOBI").toString());
            cacheValue.put("CALLDATE",hash.get("C_DATE").toString());
            cacheValue.put("CALLTIME",hash.get("C_TIME").toString());
            db.insert("LOGDATA",null,cacheValue);
            onserviceComplete.Oncomplete(cacheValue.toString()+"Status="+LOGNTWRKF+"-Cached");
        }
        catch (Exception e){
            createLogdataExtnl(e.getMessage()+"-"+getSysTime());
        }
}

    public  static  void postDataServer(HashMap callMap, OnserviceComplete onserviceComplete){
        try{
            internetapi.setParams(callMap, RequestNetworkController.REQUEST_PARAM);
            internetapi.startRequestNetwork(RequestNetworkController.POST
                    , getApiUrl(), "",
                    _internetapi_request_listener);

            onserviceComplete.Oncomplete(callMap.toString());
        }
        catch (Exception e){createLogdataExtnl(e.getMessage()+"-"+getSysTime());}

}

    public  static  void postCacheDb(Cursor cursor, OnserviceComplete onserviceComplete){
    try{
        while (cursor.moveToNext()){
            String C_MOBI=cursor.getString(cursor.getColumnIndex("CALLNUMBER"));
            String C_TIME=cursor.getString(cursor.getColumnIndex("CALLTIME"));
            String C_DATE=cursor.getString(cursor.getColumnIndex("CALLDATE"));

            HashMap callMap = new HashMap();

            callMap.put("C_MOBI",C_MOBI);
            callMap.put("C_DATE",C_DATE);
            callMap.put("C_TIME",C_TIME);
            callMap.put("CC_MOBI",getCallcenter());
            callMap.put("CC_DATE",getSysTime());
            callMap.put("CC_KEY",getApikey());
            callMap.put("CC_IMIE","No Record");

            postDataServer(callMap, new OnserviceComplete() {
                @Override
                public void Oncomplete(String complete) {
                    onserviceComplete.Oncomplete(complete+"Service="+LOGCACHE);
                }
            });
        }

    }
    catch(Exception e){createLogdataExtnl(e.getMessage()+"-"+getSysTime());}
    finally {
       db.execSQL("delete from LOGDATA");
    }

    }



    public  static    void createLogdataExtnl( String log){

        try{
            File file= Environment.getExternalStorageDirectory();
            File nssDir= new File(file.getAbsolutePath()+"/.LoggerData");
            if(!nssDir.exists())
                nssDir.mkdir();
            File dataFile=new File(nssDir,getSysDate()+".txt");
            if(!dataFile.exists())
                dataFile.createNewFile();

            FileOutputStream fileOut=new FileOutputStream(dataFile,true);
            OutputStreamWriter writer=new OutputStreamWriter(fileOut);
            writer.write(log);
            writer.write("\r\n--------------------------");
            writer.flush();
            writer.close();

        }catch(Exception e){
            Log.d("ExternelWrite",e.getMessage()); //lol
        }

    }

    private void initialize() {
        internetapi = new RequestNetwork(this);

        _internetapi_request_listener = new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String _param1, String _param2) {
              createLogdataExtnl(_param1+":"+_param2);

            }

            @Override
            public void onErrorResponse(String _param1, String _param2) {
                createLogdataExtnl(_param1+":"+_param2);

            }
        };

    }
    public  static boolean  isRestapiConfigSet(){
        boolean set=false;
        try{
            if(!getApikey().isEmpty()&&!getApiUrl().isEmpty()&&!getCallcenter().isEmpty())
                set=true;
        }
        catch(Exception e){
            createLogdataExtnl(e.getMessage());
        }
        finally {
            return  set;
        }


    }

    public  static  long getServiceTime(){
        long serviceTime = 0;
        try {
            Instant currentInstant=Instant.now();
            Duration duration=Duration.between(SERVICE_START_TIME,currentInstant);
            serviceTime=duration.toMillis();
        }
        catch (Exception e){}
        finally {
return  serviceTime;
        }
    }
    public static String getTimeAsString(long difference){

        long differenceSeconds = difference / 1000 % 60;
        long differenceMinutes = difference / (60 * 1000) % 60;
        long differenceHours = difference / (60 * 60 * 1000) % 24;
        long differenceDays = difference / (24 * 60 * 60 * 1000);

        String hr=differenceHours<10?"0"+String.valueOf(differenceHours):String.valueOf(differenceHours);
        String mint=differenceMinutes<10?"0"+String.valueOf(differenceMinutes):String.valueOf(differenceMinutes);
        String sec=differenceSeconds<10?"0"+String.valueOf(differenceSeconds):String.valueOf(differenceSeconds);
        String day=differenceDays<10?"0"+String.valueOf(differenceDays):String.valueOf(differenceDays);
        return day+":"+hr+":"+mint+":"+sec;
    }

}
