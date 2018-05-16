package eu.meuwe.app.meuwealfa;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;

import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    public static Boolean started = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        Thread timer = new Thread(){
            @Override
            public void run() {

                try {
                    sleep(2500);

                    mAuth = FirebaseAuth.getInstance();
                    if (mAuth.getCurrentUser() != null){
                        Intent intent = new Intent(SplashActivity.this, MapsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return;

                    }
                    else {
                        Intent intent = new Intent(SplashActivity.this, ChooseLoginRegistrationActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return;
                    }

                }
                    catch (InterruptedException e) {
                    e.printStackTrace();
                }



            }

        }; timer.start();


    }
    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }


}