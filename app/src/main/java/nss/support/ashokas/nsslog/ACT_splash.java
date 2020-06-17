package nss.support.ashokas.nsslog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ACT_splash extends AppCompatActivity {
SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_splash);
        init();



    }

    private  void  init(){
        preferences=getSharedPreferences("isconn", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("otSplash","true");
        editor.putBoolean("isconn",false);
        editor.commit();
        Intent intent=new Intent(getApplicationContext(),ACT_home.class);
        startActivity(intent);


    }
}
