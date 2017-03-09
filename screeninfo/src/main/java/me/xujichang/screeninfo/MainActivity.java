package me.xujichang.screeninfo;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvResolution = (TextView) findViewById(R.id.tv_resolution);
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        Point point = new Point();
        display.getSize(point);
        tvResolution.setText(point.x + "x" + point.y);

    }
}
