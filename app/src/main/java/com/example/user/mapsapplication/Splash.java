package com.example.user.mapsapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

public class Splash extends AppCompatActivity {


    ImageView imageView;
    TextView textView;
    Animation animation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        textView = (TextView) findViewById(R.id.appizza);
        imageView = (ImageView)findViewById(R.id.pizza);
        animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate);

        final Animation fadein= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein);
        final Animation fadeout = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadeout);






        imageView.startAnimation(animation);

       textView.startAnimation(fadein);

        fadein.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });




        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }



                       // imageView.startAnimation(fadeout);
                        finish();
                        Intent intent = new Intent(Splash.this,MainActivity.class);
                        startActivity(intent);


                    }
                }).start();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });





    }
}
