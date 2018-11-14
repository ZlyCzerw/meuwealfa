package eu.meuwe.app.meuwealfa;

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


public class DisplayMeuweActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private FirebaseStorage mFirebaseStorage;
    private String EventUUID;
    private DocumentReference documentReference;
    private Bitmap mBitmap;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private Post mPost;

    //UI definitions
    private ImageView eventImageView;
    private LayoutInflater layoutInflater;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private EditText messageText;
    private TextView eventDescText;


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
        mFirebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        documentReference = mFirestore.collection("posts").document(EventUUID);
        //post = new Post();
        RefreshPost();
        //get instance of layout inflater
        layoutInflater = this.getLayoutInflater();
        // Configure recycler view
        linearLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);





    }

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
                    Post post = documentSnapshot.toObject(Post.class);
                    //get image from firebase
                    post.incrementViewsCounter();
                    //attach recycleview adapter
                    mPost = post;
                    adapter = new MessageAdapter(post, firebaseUser.getEmail());
                    recyclerView.setAdapter(adapter);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DisplayMeuweActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        finishActivity(1);
    }

}
