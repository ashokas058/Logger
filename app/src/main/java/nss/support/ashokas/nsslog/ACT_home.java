package nss.support.ashokas.nsslog;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import nss.support.ashokas.nsslog.ForgroundService.CLS_callLogService;

import static java.lang.Thread.sleep;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.BRD_ACTIVITY_Action;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.ISRUNNING;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.createLogdataExtnl;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getServiceTime;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getTimeAsString;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.isRestapiConfigSet;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.isRestapiConfigSet;

public class ACT_home extends AppCompatActivity implements View.OnClickListener {
Button svcStartBt;
TextView txtVw_Isconn,txtMenu,txtNtwrkIndc,txtServiceTime;
FloatingActionButton fltbt_callMnl;
boolean isInActivity;
boolean isrunning;
IntentFilter intentHomeFilter;
NetworkUiThread nwrkThread;
ServiceTimeUiThread srvcThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_home);
        isrunning=false;
       if(getSupportActionBar()!=null)
           getSupportActionBar().hide();
        initComponents();
        registerViewListener();
        updateUi();
        initBrodcastRcvr();
        initNetworkThread();
        initServiceUiThread();
        initViewVisibility();
        initVarValues();


    }

    private void initServiceUiThread() {
        try{
            if(ISRUNNING){
                if(srvcThread==null||!srvcThread.isAlive()){
                    srvcThread=new ServiceTimeUiThread();
                    srvcThread.start();
                }
            }
        }
        catch (Exception e){}
    }

    private void initNetworkThread() {
        try{
            if(nwrkThread==null||!nwrkThread.isAlive()) {
                nwrkThread = new NetworkUiThread();
                nwrkThread.start();
            }
        }catch (Exception e){}

    }

    private void updateUi() {
        if (ISRUNNING){
            svcStartBt.setText("stop service");
        }
        else{
            svcStartBt.setText("start service");
        }
    }


    @Override
    public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), CLS_callLogService.class);
            switch (view.getId()) {

                case R.id.xml_service_id:
                    if(!isRestapiConfigSet())
                        startRestApiConfigAlert();
                    else {
                        if (getServiceState()) {
                            this.stopService(intent);

                        } else {
                            if (requestPermission()) {
                                this.startForegroundService(intent);
                            }
                        }
                    }
                    break;
                case R.id.txt_menu:
                    Intent iintentSetgs = new Intent(this, ACT_settings.class);
                    startActivity(iintentSetgs);
                    break;
                case R.id.xml_fltbt_mnlcall:
                    Intent intManualCallWrt = new Intent(this, ACT_viewAllLog.class);
                    startActivity(intManualCallWrt);
                    break;

            }

    }
    private boolean requestPermission() {
        boolean perState=false;
        if (    ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CALL_LOG)!=PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_CALL_LOG)!=PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_NETWORK_STATE)!=PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this,
                                new String[]{
                                Manifest.permission.READ_CALL_LOG,
                                Manifest.permission.WRITE_CALL_LOG,Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        else {
            perState = true;}

       return  perState;
    }

    private   boolean isNetwrkConnected(){
        ConnectivityManager manager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nwrkInfo=manager.getActiveNetworkInfo();
        return  nwrkInfo!=null &&nwrkInfo.isConnected();

    }

    private boolean  getServiceState(){
        return  ISRUNNING;
    }

 private  void initComponents(){
     svcStartBt=findViewById(R.id.xml_service_id);
     fltbt_callMnl=findViewById(R.id.xml_fltbt_mnlcall);
     txtVw_Isconn=findViewById(R.id.xml_txt_isconn);
     txtMenu=findViewById(R.id.txt_menu);
     txtNtwrkIndc=findViewById(R.id.xml_netwrkIndcate);
     txtServiceTime=findViewById(R.id.xml_service_time_ui);
     requestPermission();

 }
 private  void  initViewVisibility(){
     txtVw_Isconn.setVisibility(View.INVISIBLE);
     txtNtwrkIndc.setVisibility(View.INVISIBLE);

 }
 private  void initVarValues(){
     isInActivity=true;
     txtServiceTime.setText("");
 }
 private  void  registerViewListener(){
     svcStartBt.setOnClickListener(this);
     fltbt_callMnl.setOnClickListener(this);
     txtMenu.setOnClickListener(this);
 }
/*
private ServiceConnection  callServiceConn=new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        CLS_callLogService.localServicePvdr binder=(CLS_callLogService.localServicePvdr)service;
        callServiceOb=binder.getServiceInstance();
        bound=true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
bound=false;
    }
};
    */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isInActivity=false;
    }


    private  void  startRestApiConfigAlert(){

    AlertDialog.Builder alertBuilder=new AlertDialog.Builder(this);
    alertBuilder.setPositiveButton("start", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            startSettingActivity();
        }
    }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
    }).setMessage("*All fields are mandatory").setTitle("update configuration").setCancelable(false);

    AlertDialog dialog=alertBuilder.create();
    dialog.show();

}

    private void startSettingActivity() {
    Intent intentSettingAct=new Intent(this,ACT_settings.class);
    startActivity(intentSettingAct);
    this.finish();
    }
private  class NetworkUiThread extends Thread{

    @Override
    public void run() {
        while (isInActivity) {
            super.run();
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isNetwrkConnected()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtNtwrkIndc.setVisibility(View.INVISIBLE);

                    }
                });

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtNtwrkIndc.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }
}

private  class  ServiceTimeUiThread extends  Thread{

    @Override
    public void run() {
        super.run();
        while (isInActivity&&ISRUNNING){
            String runTime= getTimeAsString(getServiceTime());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtServiceTime.setText(runTime);
                }
            });
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


public  class  ServiceStateRCVR extends  BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean stat=intent.getBooleanExtra("state",false);
        if(stat) {
            Toast.makeText(context, "Service running", Toast.LENGTH_SHORT).show();
            try{
                svcStartBt.setText("stop service");
                initServiceUiThread();



            }
            catch (Exception e){}

        }

        else {
            Toast.makeText(context, "Service Stopped", Toast.LENGTH_SHORT).show();
            svcStartBt.setText("start service");
            String srvcStopped=txtServiceTime.getText().toString();
            createLogdataExtnl("Service stoped -"+srvcStopped);
            txtServiceTime.setText("");
        }

    }
}

    private  void  initBrodcastRcvr(){
        try {
            intentHomeFilter = new IntentFilter();
            intentHomeFilter.addAction(BRD_ACTIVITY_Action);
            registerReceiver(new ServiceStateRCVR(), intentHomeFilter);
        }catch (Exception e){
            createLogdataExtnl(e.getMessage());
        }
    }

}
