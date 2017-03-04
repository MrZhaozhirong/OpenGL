package com;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.particles.opengl.ParticlesActivity;
import com.pixel.opengl.OpenGLActivity;
import com.pixel.opengl.R;

/**
 * Created by zzr on 2017/3/4.
 */

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        findViewById(R.id.particles).setOnClickListener(this);
        findViewById(R.id.table_ball).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.particles:
                startActivity(new Intent(HomeActivity.this,ParticlesActivity.class));
                break;
            case R.id.table_ball:
                startActivity(new Intent(HomeActivity.this,OpenGLActivity.class));
                break;
        }
    }
}
