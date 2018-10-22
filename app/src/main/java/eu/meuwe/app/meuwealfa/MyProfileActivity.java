package eu.meuwe.app.meuwealfa;

import android.content.Intent;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class MyProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;

    private ListView myPostsListView;

    private List <Post> myPosts;
    private ArrayAdapter<Post> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        //Init UI
        myPostsListView = findViewById(R.id.myPostsListView);

        //Initialise screen with data from database
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Create a reference to the posts collection
        CollectionReference postsRef = firestore.collection("posts");

        // Create a query against the collection.
        String userUID =firebaseAuth.getCurrentUser().getUid();

        postsRef.whereEqualTo("user", userUID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    //myPosts = queryDocumentSnapshots.getDocuments();
                    myPosts = queryDocumentSnapshots.toObjects(Post.class);

                    //fill the ListView with retrieved query
                    if(myPosts!=null)
                    {

                        adapter = new ArrayAdapter(MyProfileActivity.this, android.R.layout.simple_list_item_1,myPosts.toArray());
                        myPostsListView.setAdapter(adapter);
                    }
                }
                else
                {
                    Toast.makeText(MyProfileActivity.this, task.getException().getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        myPostsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MyProfileActivity.this, DisplayMeuweActivity.class);
                intent.putExtra("EventUUID",myPosts.get(position).getUuid());
                startActivity(intent);
            }
        });

    }

}
