package eu.meuwe.app.meuwealfa;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class ChooseLoginRegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button mLogin, mRegister, mMyProfile;
    private void setUIonUser (){
        // Check if user is already logged in, and change UI
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            setUIUserLoggedIn();
        }
        else{
            setUIUserLoggedOut();
        }
    }
    private void setUIUserLoggedIn (){
        mLogin.setText(R.string.logoutText);
        mRegister.setText(R.string.showMapText);
        mMyProfile.setVisibility(View.VISIBLE);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                setUIonUser();
                return;
            }
        });
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseLoginRegistrationActivity.this, MapsActivity.class);
                startActivity(intent);
                return;

            }
        });

    }

    private void setUIUserLoggedOut (){
        mLogin.setText(R.string.LoginText);
        mRegister.setText(R.string.fui_title_register_email);
        mMyProfile.setVisibility(View.INVISIBLE);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseLoginRegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                return;
            }
        });
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseLoginRegistrationActivity.this, RegistrationActivity.class);
                startActivity(intent);
                return;

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_registration);

        mLogin = findViewById(R.id.login);
        mRegister = findViewById(R.id.register);
        mMyProfile = findViewById(R.id.myposts);

        // Check if user is already logged in, and change UI
        setUIonUser();
        mAuth = FirebaseAuth.getInstance();
        /*if (mAuth.getCurrentUser() != null){
            Intent intent = new Intent(ChooseLoginRegistrationActivity.this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }*/

        mMyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseLoginRegistrationActivity.this, MyProfileActivity.class);
                startActivity(intent);
                return;

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUIonUser();
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}
