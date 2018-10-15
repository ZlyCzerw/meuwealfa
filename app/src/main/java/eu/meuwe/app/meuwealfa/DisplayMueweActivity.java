package eu.meuwe.app.meuwealfa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        //get instance of layout inflater
        layoutInflater = this.getLayoutInflater();


        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists())
                {
                    Toast.makeText(DisplayMueweActivity.this, "Post "+ documentReference.getId()+" not found", Toast.LENGTH_SHORT).show();
                }
                else //fill window
                {
                    post = documentSnapshot.toObject(Post.class);
                    //get image from firebase
                    StorageReference mStorageReference = mFirebaseStorage.getReference()
                            .child(post.getImageUrl());
                    mStorageReference.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            eventImageView.setImageBitmap(mBitmap);
                        }
                    });
                    //for each message
                    for (Message message:post.getMessages()) {
                        //create view as message received or message sent
                        View v;
                        TextView messageText;
                        TextView messageUser;
                        TextView messageTime;
                        if(message.getUser()==firebaseAuth.getCurrentUser().toString())
                        {
                            v = layoutInflater.inflate(R.layout.message_sent, recyclerView);
                        }
                        else
                        {
                           v = layoutInflater.inflate(R.layout.message_received, recyclerView);
                        }
                        messageUser = v.findViewById(R.id.text_message_name);
                        messageTime = v.findViewById(R.id.text_message_time);
                        messageText = v.findViewById(R.id.text_message_time);

                        messageUser.setText(message.getUser());
                        messageText.setText(message.getText());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                        messageTime.setText(dateFormat.format(message.getTime()));
                    }

                    /*
                    documentReference.put("user",mFirebaseUser.getUid());
                    documentReference.put("time", simpleDateFormat.format(GregorianCalendar.getInstance().getTime()));
                    documentReference.put("latitude",Latitude);
                    documentReference.put("longitude",Longitude);
                    documentReference.put("text",mEnterText.getText().toString());
                    documentReference.put("imageUrl",mStorageReference.child(ImageName).getPath());*/
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DisplayMueweActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void RefreshMessages (DocumentSnapshot documentSnapshot)
    {
        Post post = documentSnapshot.toObject(Post.class);
        //get image from firebase
        StorageReference mStorageReference = mFirebaseStorage.getReference()
                .child(post.getImageUrl());
        mStorageReference.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                eventImageView.setImageBitmap(mBitmap);
            }
        });
        //for each message
        for (Message message:post.getMessages()) {
            //create view as message received or message sent
            if(message.getUser()==firebaseAuth.getCurrentUser().toString())
            {
                layoutInflater.inflate(R.layout.message_sent, null);
            }

        }
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
        post.addMessage(messageText.getText().toString(),firebaseAuth.getCurrentUser().toString());
        documentReference.set(post);
    }
}
