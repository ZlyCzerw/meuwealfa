package eu.meuwe.app.meuwealfa;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText mEmail, mPassword, mName;
    private Button mRegister;
    private static final String TAG ="Registration Activity";
    private static final String FIELDS_CANNOT_BE_EMPTY ="Fields cannot be empty";
    private static final String USER_SUCCESSFULLY_ADDED ="User Succesfully added";
    private static final String USER_NOT_ADDED ="User Registration Failed";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();


        mName = findViewById(R.id.name);
        mRegister = findViewById(R.id.registerButton);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String name = mName.getText().toString();
                if( email.isEmpty()
                        ||password.isEmpty()
                        ||name.isEmpty()) Toast.makeText(getApplicationContext(), FIELDS_CANNOT_BE_EMPTY,Toast.LENGTH_SHORT).show();

                // add new user to Firebase
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(getApplicationContext(),USER_SUCCESSFULLY_ADDED,Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                                //Intent MapsActivityIntent = new Intent(RegistrationActivity.this, MapsActivity.class);
                                //startActivity(MapsActivityIntent);
                                finish();
                            }

                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
