package eu.meuwe.app.meuwealfa;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class DisplayMueweActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private FirebaseStorage mFirebaseStorage;
    private String EventUUID;
    private DocumentReference documentReference;
    private Bitmap mBitmap;
    private FirebaseAuth firebaseAuth;
    private Post post;

    //UI definitions
    private ImageView eventImageView;
    private LayoutInflater layoutInflater;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private EditText messageText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_muewe);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        EventUUID = intent.getStringExtra("EventUUID");

        //Init UI references
        eventImageView = findViewById(R.id.eventImageView);
        recyclerView = findViewById(R.id.recyclerView);
        messageText = findViewById(R.id.messageText);


        //Initialise screen with data from database
        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        documentReference = mFirestore.collection("posts").document(EventUUID);
        post = new Post();

        //get instance of layout inflater
        layoutInflater = this.getLayoutInflater();
        // Configure recycler view
        linearLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new MessageAdapter(post, firebaseAuth.getCurrentUser().getDisplayName());
        recyclerView.setAdapter(adapter);


        RefreshMessages();

    }

    private void RefreshMessages () {
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(DisplayMueweActivity.this,
                            "Post " + documentReference.getId() + " not found",
                            Toast.LENGTH_SHORT).show();
                } else //fill window
                {
                    Post post = documentSnapshot.toObject(Post.class);
                    //get image from firebase
                    if(!post.getImageUrl().isEmpty()) {
                        StorageReference mStorageReference = mFirebaseStorage.getReference()
                                .child(post.getImageUrl());
                        mStorageReference.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                eventImageView.setImageBitmap(mBitmap);
                            }
                        });
                    }
                    if (!post.getMessages().isEmpty()) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DisplayMueweActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /*public void publish (View publish) {
        // Do something in response to button
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }*/
    public void goBackHome(View home) {
        // Do something in response to button
        //Intent intent = new Intent(this, MapsActivity.class);
        //startActivity(intent);
    }

    public void sendMessage(View view)
    {
        post.addMessage(messageText.getText().toString(),firebaseAuth.getCurrentUser().getDisplayName());
        documentReference.set(post);

        RefreshMessages();
    }

}
