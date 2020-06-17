package nss.support.ashokas.nsslog;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mva2.adapter.ListSection;
import mva2.adapter.MultiViewAdapter;
import mva2.adapter.util.Mode;
import mva2.adapter.util.OnSelectionChangedListener;
import nss.support.ashokas.nsslog.Networking.RequestNetwork;
import nss.support.ashokas.nsslog.RecyclerView.CLS_callBinder;
import nss.support.ashokas.nsslog.RecyclerView.CLS_callsModel;
import nss.support.ashokas.nsslog.SysInterface.OnserviceComplete;

import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.LOGSUC;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.createLogdataExtnl;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getApikey;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getCacheDb;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getCallcenter;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getDateTimeCall;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getSysTime;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.isRestapiConfigSet;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.poolExecutor;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.postCacheDb;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.postDataServer;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.setcacheDb;

public class ACT_viewAllLog extends AppCompatActivity {
RecyclerView rcy_callView;
MultiViewAdapter adapter;
ListSection<CLS_callsModel> section;
List<CLS_callsModel> selectionList;
Cursor cursor;
RequestNetwork internetapi=null;
RequestNetwork.RequestListener _internetapi_request_listener = null;
int ALL_CALL=20;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_view_all_log);

        getSupportActionBar().setTitle("All Calls");
        initRcyView();
        selectionList=new ArrayList<>();

        OnSelectionChangedListener<CLS_callsModel> selectionChangedListener =new OnSelectionChangedListener<CLS_callsModel>() {
            @Override
            public void onSelectionChanged(CLS_callsModel item, boolean isSelected, List<CLS_callsModel> selectedItems) {
                   selectionList=selectedItems;

            }
        };
        section.setOnSelectionChangedListener(selectionChangedListener);
    }
    private Cursor getCursorCallLg(int type) {
        final String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";
        StringBuffer sb = new StringBuffer();
        sb.append(CallLog.Calls.TYPE).append("=?");
        if(type==ALL_CALL){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                cursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null,null,null, sortOrder);


            }
        }
        else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                cursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, sb.toString(), new String[]{String.valueOf(type)}, sortOrder);


            }
        }
        return  cursor;

    }
    private ArrayList<CLS_callsModel> getCallasList(int type){
        ArrayList<CLS_callsModel> calls =new ArrayList<>();
            Cursor cursor = getCursorCallLg(type);
            while (cursor.moveToNext()) {
                CLS_callsModel cal = new CLS_callsModel();
                cal.setCallerName(cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
                cal.setCallDate(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));
                cal.setCallType(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
                cal.setCallernumber(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
                calls.add(cal);
            }
        return  calls;
    }
    private  void initRcyView(){
        rcy_callView=findViewById(R.id.xml_rcy_calllog);
        LinearLayoutManager manager=new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,false);
        rcy_callView.setLayoutManager(manager);
        adapter=new MultiViewAdapter();
        adapter.registerItemBinders(new CLS_callBinder());
        section=new ListSection<>();
        section.addAll(getCallasList(ALL_CALL));
        adapter.addSection(section);
        adapter.setSelectionMode(Mode.MULTIPLE);
        rcy_callView.setAdapter(adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.xml_menu_allcalls:
               section.clear();
               cursor.close();
               section.addAll(getCallasList(ALL_CALL));
               adapter.notifyDataSetChanged();
                        break;
            case  R.id.xml_menu_mssd:
                section.clear();
                cursor.close();
                section.addAll(getCallasList(CallLog.Calls.MISSED_TYPE));
                adapter.notifyDataSetChanged();
                getSupportActionBar().setTitle("Missed Calls");
                break;
            case R.id.xml_menu_recv:
                section.clear();
                cursor.close();
                section.addAll(getCallasList(CallLog.Calls.INCOMING_TYPE));
                adapter.notifyDataSetChanged();
                getSupportActionBar().setTitle("Incomming Calls");
                break;
            case R.id.xml_menu_save:
                if(!isRestapiConfigSet())
                    startRestApiConfigAlert();
                else {
                    poolExecutor.execute(getExecutableRunnable());
                    poolExecutor.shutdown();
                }
                break;
            case android.R.id.home:
                onBackPressed();
                break;

        }
        return  true;
    }
    public   boolean isNetwrkConnected(){
        ConnectivityManager manager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nwrkInfo=manager.getActiveNetworkInfo();
        return  nwrkInfo!=null &&nwrkInfo.isConnected();

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
    private  Runnable getExecutableRunnable(){
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                if(requestPermission()) {
                    HashMap callMap = new HashMap();
                    if (selectionList.size() < 1)
                        Log.d("Selection", "empty");
                    //Toast.makeText(ACT_viewAllLog.this, "", Toast.LENGTH_SHORT).show();
                    else {
                        for (CLS_callsModel call : selectionList) {
                            callMap.put("C_MOBI", call.getCallernumber());
                            String a[] = getDateTimeCall(call.getCallDate());
                            callMap.put("C_DATE", a[0]);
                            callMap.put("C_TIME", a[1]);
                            callMap.put("CC_MOBI", getCallcenter());
                            String v = getCallcenter();
                            callMap.put("CC_DATE", getSysTime());
                            String v1 = getSysTime();
                            String v2 = getApikey();
                            callMap.put("CC_KEY", getApikey());
                            callMap.put("CC_IMIE", "No Record");

                            if (isNetwrkConnected()) {
                                Log.d("netwrk", " Avilable");
                                if (getCacheDb().getCount() < 1 || getCacheDb() == null) {
                                    Log.d("cache", "cache not Avilable");
                                    postDataServer(callMap, new OnserviceComplete() {
                                        @Override
                                        public void Oncomplete(String complete) {
                                            createLogdataExtnl(complete + "Status=" + LOGSUC);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(ACT_viewAllLog.this, "done...", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }
                                    });

                                } else {
                                    Log.d("cache", "cache data Avilable");
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
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(ACT_viewAllLog.this, "done...", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }
                                    });

                                }

                            } else {
                                Log.d("Netwrk", " not Avilable");
                                setcacheDb(callMap, new OnserviceComplete() {

                                    @Override
                                    public void Oncomplete(String complete) {
                                        createLogdataExtnl(complete);
                                        Toast.makeText(ACT_viewAllLog.this, "Cached Data", Toast.LENGTH_SHORT).show();

                                    }
                                });


                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.clearAllSelections();
                                }
                            });

                        }
                        selectionList.clear();
                    }
                }
            }
        };
        return  runnable;

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
}
