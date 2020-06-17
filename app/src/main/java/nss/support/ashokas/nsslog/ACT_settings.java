package nss.support.ashokas.nsslog;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import nss.support.ashokas.nsslog.Barcode_Qr.CLS_camera;

import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.db;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getApiUrl;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getApikey;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.getVolentiNumber;
import static nss.support.ashokas.nsslog.ForgroundService.CLS_globalProvider.setApiKey;

public class ACT_settings extends AppCompatActivity implements View.OnClickListener {
    AlertDialog alertDialog1;
    AlertDialog.Builder builderAlertApi;
    TextView txt_apiUrl, txt_apiKey, txtapiTestValue, txtMobileCntr;
    View viewApi;
    FloatingActionButton flt_apiKeyQr;
    Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_settings);
        initView();
        txt_apiUrl.setOnClickListener(this);
        txt_apiKey.setOnClickListener(this);
        flt_apiKeyQr.setOnClickListener(this);
        txtMobileCntr.setOnClickListener(this);
        getSupportActionBar().setTitle("Settings");

    }

    private void initView() {
        txtMobileCntr = findViewById(R.id.xml_txt_updatenumber);
        txt_apiKey = findViewById(R.id.xml_txt_updatekey);
        txt_apiUrl = findViewById(R.id.xml_txt_updateurl);
        txtapiTestValue = findViewById(R.id.xml_api_iswork);
        flt_apiKeyQr = findViewById(R.id.xml_flt_apikey_qr);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.xml_txt_updateurl:
                alertDialog1 = getAlertBuilder("APIURL").create();
                alertDialog1.show();
                break;

            case R.id.xml_txt_updatekey:
                alertDialog1 = getAlertBuilder("APIKEY").create();
                alertDialog1.show();
                break;
            case R.id.xml_flt_apikey_qr:
                initBarcodeScanner();
                break;
            case R.id.xml_txt_updatenumber:
                alertDialog1 = getAlertBuilder("CALLCENTER").create();
                alertDialog1.show();
                break;

        }
    }


    private void loadApiTestValue() {
        txtapiTestValue.setText(getApiUrl());
    }


    private AlertDialog.Builder getAlertBuilder(final String api) {
        ViewGroup group = findViewById(android.R.id.content);
        final View view = LayoutInflater.from(this).inflate(R.layout.dilog_lyt_custom, group, false);
        final EditText edt_api = view.findViewById(R.id.xml_dilog_edt);
        builderAlertApi = new AlertDialog.Builder(this);
        builderAlertApi.setView(view);
        switch (api){
            case "APIURL":
                edt_api.setText(getApiUrl());
                break;
            case  "APIKEY":
                //edt_api.setText(getApikey());
                break;
            case  "CALLCENTER":
                edt_api.setHint(getVolentiNumber());
                break;
        }

        builderAlertApi.setPositiveButton("save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String apiUrl = edt_api.getText().toString();
                if (!apiUrl.isEmpty()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(api, apiUrl);
                    String where = "id=?";
                    if (db.update("SERVICE", contentValues, where, new String[]{String.valueOf(1)}) > 1)
                        Toast.makeText(ACT_settings.this, "updated " + api, Toast.LENGTH_SHORT).show();
                } else {
                    edt_api.setHint("Enter valid " + api);
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setCancelable(false);
        return builderAlertApi;
    }


    private void initBarcodeScanner() {
        IntentIntegrator init = new IntentIntegrator(this);
        init.setBeepEnabled(true);
        init.setCaptureActivity(CLS_camera.class);
        init.setOrientationLocked(false);
        init.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        init.setPrompt("Getting Ready Scanner");
        init.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            setApiKey(result.getContents());
            refreshActivity();}
            else
                refreshActivity();


    }

    public  void  refreshActivity(){
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }


}
