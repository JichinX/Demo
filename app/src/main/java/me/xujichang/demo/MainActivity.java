package me.xujichang.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import me.xujichang.inputbox.InputBoxView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputBoxView boxView = (InputBoxView) findViewById(R.id.box_view);
        boxView.setCallBack(new InputBoxView.BoxCallBack() {
            @Override
            public void onFinish(String str) {
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
