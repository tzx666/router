package buct.tzx.javademo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import buct.tzx.routerannotation.Router;

@Router(Path = "/demo/main1")
public class MainActivity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
    }
}