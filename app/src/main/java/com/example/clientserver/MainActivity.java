package com.example.clientserver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends Activity {

    private ImageButton ctest, stest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        getview();
        setview();

    }

    private void getview() {
        ctest = (ImageButton) this.findViewById(R.id.ctest);
        stest = (ImageButton) this.findViewById(R.id.stest);
    }

    private void setview() {
       ctest.setOnClickListener(clicklister);
       stest.setOnClickListener(clicklister);
        
    }

    private View.OnClickListener clicklister = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            switch (v.getId()) {
            case R.id.ctest:
                intent.setClass(MainActivity.this,client.class);
                startActivity(intent);
                break;
          
            case R.id.stest:
                intent.setClass(MainActivity.this, server.class);
                startActivity(intent);
                break;
          

            }

        }
    };
}
