package me.moolv.demo.apt1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import me.moolv.annotation.DemoAnnotation;

@DemoAnnotation
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
