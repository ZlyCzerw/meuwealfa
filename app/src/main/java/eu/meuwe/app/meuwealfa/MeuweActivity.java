package eu.meuwe.app.meuwealfa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.EventLog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MeuweActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "eu.meuwe.app.meuwealfa";

    String Uid;
    Bitmap bitmap;
    Bitmap rotateBitmap;
    private Button mPostIt;
    private ImageView mImageTaken;
    private EditText mEnterText;
    private double Latitude,Longitude;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mFirebaseStorage;
    //request codes
    private final int REQUEST_IMAGE_CAPTURE = 101;
    private final int PERMISSION_CAMERA =102;

  /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meuwe);


        try {
            bitmap = BitmapFactory.decodeStream(getApplication().openFileInput("capture"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            finish();
            return;
        }

        ImageView mImage = findViewById(R.id.imageTaken);
        mImage.setImageBitmap(bitmap);
        Uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meuwe);

        //Get user
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseStorage = FirebaseStorage.getInstance();
        //Get access to Firestore
        mFirestore = FirebaseFirestore.getInstance();

        //Get UI references
        mPostIt =  findViewById(R.id.post);
        mEnterText = findViewById(R.id.nameText);
        mImageTaken = findViewById(R.id.imageTaken);

        //Get extras from previous activity
        Intent intent = getIntent();
        Latitude=intent.getDoubleExtra("Latitude",0);
        Longitude=intent.getDoubleExtra("Longitude",0);

    }






    /**
     * Called when the user taps the Send button
     */
    public void sendMessage(View view) {

        /*Intent intent = new Intent(this, DisplayMeuweActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);*/


        //Add all the data to firestore
        //Get current date
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("yy/MM/dd;HH:mm:ss");
        //Upload img to firebase storage
        StorageReference mStorageReference = mFirebaseStorage.getReference();
        final UUID EventUUID = UUID.randomUUID(); // generate unique ID for image
        String ImageName = "Images/"+EventUUID.toString()+".png";
        //Convert Bitmap to Byte Stream
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        Bitmap bm=((BitmapDrawable)mImageTaken.getDrawable()).getBitmap();
        bm.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte[] data = baos.toByteArray();
        //Upload Image to Firebase Storage
        UploadTask uploadTask = mStorageReference.child(ImageName).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MeuweActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        Post mPost = new Post(
                EventUUID.toString(),
                mFirebaseUser.getUid(),
                Latitude,
                Longitude,
                mEnterText.getText().toString(),
                mStorageReference.child(ImageName).getPath());

        //Create document
        mFirestore.collection("posts")
                .document(EventUUID.toString()).set(mPost)
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MeuweActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Show created activity
                Intent mDisplayMeuweActivity = new Intent(MeuweActivity.this, DisplayMeuweActivity.class);
                mDisplayMeuweActivity.putExtra("EventUUID",EventUUID.toString());
                startActivity(mDisplayMeuweActivity);
            }
        });

    }
    //Start Camera App and expect to get image
    public void onClickImageTaken (View view)
    {
        Intent takeImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (takeImageIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takeImageIntent, REQUEST_IMAGE_CAPTURE);
            }
            else Toast.makeText(this, R.string.ErrNoAppToMakePhotos, Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},PERMISSION_CAMERA);
        }

    }

    //Get image from the provider app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageTaken.setImageBitmap(imageBitmap);
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
