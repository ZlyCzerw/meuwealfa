package eu.meuwe.app.meuwealfa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class DisplayMeuweActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private String EventUUID;
    private DocumentReference documentReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    private Post mPost;

    //UI definitions
    private LayoutInflater layoutInflater;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private EditText messageText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_meuwe);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        EventUUID = intent.getStringExtra("EventUUID");

        //Init UI references
        recyclerView = findViewById(R.id.recyclerView);
        messageText = findViewById(R.id.messageText);


        //Initialise screen with data from database
        mFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();

        documentReference = mFirestore.collection("posts").document(EventUUID);


        RefreshPost();
        //get instance of layout inflater
        layoutInflater = this.getLayoutInflater();
        // Configure recycler view
        linearLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    private Bitmap bitmap;
    private void RefreshPost()
    {
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(DisplayMeuweActivity.this,
                            "Post " + documentReference.getId() + " not found",
                            Toast.LENGTH_SHORT).show();
                } else //fill window
                {
                    mPost = documentSnapshot.toObject(Post.class);
                    //get image from firebase
                    if (mPost.getImageUrl()!=null&& !mPost.getImageUrl().isEmpty())
                    {
                        StorageReference mStorageReference = firebaseStorage.getReference()
                                .child(mPost.getImageUrl());
                        mStorageReference.getBytes(1024 * 1024)
                                .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                    @Override
                                    public void onComplete(@NonNull Task<byte[]> task) {
                                        if (task.isSuccessful()) {
                                            bitmap = BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length);
                                            AttachRecyclerViewAdapter();
                                        }

                                    }
                                });
                    }else {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logosplashred);
                        AttachRecyclerViewAdapter();
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DisplayMeuweActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void AttachRecyclerViewAdapter()
    {
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(getCacheDir(), getString(R.string.postbitmapCache)));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (IOException e) {
            Toast.makeText(DisplayMeuweActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        mPost.incrementViewsCounter();
        //attach recycleview adapter
        adapter = new MessageAdapter(mPost, firebaseUser.getUid());
        recyclerView.setAdapter(adapter);
    }

    private void RefreshMessages () {
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(DisplayMeuweActivity.this,
                            "Post " + documentReference.getId() + " not found",
                            Toast.LENGTH_SHORT).show();
                } else //fill window
                {
                    Post post = documentSnapshot.toObject(Post.class);
                    if (!post.getMessages().isEmpty()) {
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(post.getMessages().size()); //go to the bottom of messages
                        messageText.setText("");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DisplayMeuweActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void sendMessage(View view)
    {
        if(!messageText.getText().toString().isEmpty())
        {
            mPost.addMessage(messageText.getText().toString(),firebaseAuth.getCurrentUser().getEmail());
            documentReference.set(mPost);
            RefreshMessages();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * If user is the owner of event, then we open activity to edit.
     * @param view
     */
    public void onHeaderClick (View view)
    {
        if(mPost.getUser().compareToIgnoreCase(firebaseUser.getUid())==0)
        {
            Intent MeuweActivity = new Intent (this, eu.meuwe.app.meuwealfa.MeuweActivity.class);
            MeuweActivity.putExtra("EventUUID",EventUUID);
            startActivity(MeuweActivity);
            finish();
        }

    }

}
