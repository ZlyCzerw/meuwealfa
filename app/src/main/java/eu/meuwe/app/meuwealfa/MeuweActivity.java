package eu.meuwe.app.meuwealfa;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MeuweActivity extends AppCompatActivity {
    // UI definitions
    private Button postButton;
    private ImageView imageTaken;
    private EditText nameText,mTitle;
    private ProgressBar progressBar;
    private TextView loadingText;
    private MultiAutoCompleteTextView tagsList;
    //helpers

    private double Latitude,Longitude;
    private boolean isUserImage; //User made a picture
    private boolean isEdit; //this is not a new post, but an edit of an old one
    private StorageReference mStorageReference;
    private String EventUUID ; //Unique ID of a post
    private String imageName;
    private String imagePath ;
    private Uri imageURI;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    //request codes
    private final int REQUEST_GALLERY = 100;
    private final int REQUEST_IMAGE_CAPTURE = 101;
    private final int PERMISSION_CAMERA =102;
    private final int MAX_RESOLUTION = 512;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meuwe);

        //Get user
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        //Get access to Firestore
        firestore = FirebaseFirestore.getInstance();

        //Get UI references
        postButton =  findViewById(R.id.post);
        nameText = findViewById(R.id.nameText);
        imageTaken = findViewById(R.id.imageTaken);
        mTitle = findViewById(R.id.title);
        progressBar = findViewById(R.id.progressBar);
        loadingText = findViewById(R.id.loadingText);
        tagsList = findViewById(R.id.tagsList);

        /** Add an adapter to the tags line to help user add proper tags
         *
         */

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.tagsList));
        tagsList.setAdapter(adapter);
        tagsList.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());


        //Get extras from previous activity
        Intent intent = getIntent();
        Latitude=intent.getDoubleExtra("Latitude",0);
        Longitude=intent.getDoubleExtra("Longitude",0);
        EventUUID = intent.getStringExtra("EventUUID");

        //If we got UUID it means we edit post not create new one
        if(EventUUID != null) {
            isEdit = true;
            RefreshPost(); //get data from database

        }

    }

    private Post mPost;
    private DocumentReference documentReference;
    private Bitmap bitmap;
    private void RefreshPost() {
        mPost = new Post();
        documentReference = firestore.collection("posts").document(EventUUID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(MeuweActivity.this,
                            "Post " + documentReference.getId() + " not found",
                            Toast.LENGTH_SHORT).show();
                } else //fill window
                {
                    mPost = documentSnapshot.toObject(Post.class);
                    //get image from firebase
                    if (!mPost.getImageUrl().isEmpty())
                    {
                        StorageReference mStorageReference = firebaseStorage.getReference()
                                .child(mPost.getImageUrl());
                        mStorageReference.getBytes(1024 * 1024)
                                .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                    @Override
                                    public void onComplete(@NonNull Task<byte[]> task) {
                                        if (task.isSuccessful()) {
                                            bitmap = BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length);
                                            imageTaken.setImageBitmap(bitmap);
                                        }

                                    }
                                });
                    }
                    //fill the fields
                    mTitle.setText(mPost.getTitle());
                    tagsList.setText(mPost.getTags().toString());
                    nameText.setText(mPost.getText());
                    Latitude = mPost.getLatitude();
                    Longitude = mPost.getLongitude();
                    imagePath = mPost.getImageUrl();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MeuweActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    /**
     * Called when the user taps the Send button
     */
    public void sendMessage(View view) {

        //Add all the data to firestore
        mStorageReference = firebaseStorage.getReference();
        if(EventUUID.isEmpty())
            EventUUID = UUID.randomUUID().toString(); // generate unique ID for image
        imageName = "Images/" + EventUUID + ".png";

        //Upload img to firebase storage
        if(isUserImage) { //User has made a picture

            //Convert Bitmap to Byte Stream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bm = ((BitmapDrawable) imageTaken.getDrawable()).getBitmap();
            //rescale to MAX_RESOLUTION
            if (bm.getHeight()>bm.getWidth())
            {//Height is in MAX Resolution, and Width is proportional
                float aspectRatio = (float)bm.getWidth()/  bm.getHeight();
                bm = Bitmap.createScaledBitmap(bm,Math.round(MAX_RESOLUTION*aspectRatio),MAX_RESOLUTION,true);
            }
            else{//Width is MAX, Height is proportional
                float aspectRatio = (float) bm.getHeight()/bm.getWidth();
                bm = Bitmap.createScaledBitmap(bm,MAX_RESOLUTION, Math.round(MAX_RESOLUTION*aspectRatio),true);
            }

            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            final byte[] data = baos.toByteArray();
            //Upload Image to Firebase Storage
            UploadTask uploadTask = mStorageReference.child(imageName).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MeuweActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imagePath = mStorageReference.child(imageName).getPath();
                    CreatePost();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    //loading popup
                    int progress = (int)taskSnapshot.getBytesTransferred() *100 / data.length;

                    loadingText.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }
            });
        }else //user didn't upload any photo so we leave image path empty. Be sure to check it in Display Meuwe Activity
        {

            CreatePost();
        }


    }

    /**
     * Function to gather all the data from UI, save it to Post object and upload it to database
     */
    private void CreatePost ()
    {
        //Separate tags from the string list in Tags line
        List<String> tags = Arrays.asList(tagsList.getText().toString().split("\\s*,|;\\s*"));

        if(mPost ==null) { //create new post
            mPost = new Post(
                    EventUUID,
                    firebaseUser.getUid(),
                    Latitude,
                    Longitude,
                    nameText.getText().toString(),
                    imagePath,
                    mTitle.getText().toString(),
                    tags);
        }else { //Update content in case of edit
            mPost.setText(nameText.getText().toString());
            mPost.setTitle(mTitle.getText().toString());
            mPost.setTags(tags);
            mPost.setImageUrl(imagePath);
        }

        //Create document
        firestore.collection("posts")
                .document(EventUUID).set(mPost)
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
                mDisplayMeuweActivity.putExtra("EventUUID",EventUUID);
                startActivity(mDisplayMeuweActivity);
                finishActivity(1);
            }
        });
    }
    //Start Camera App and expect to get image
    public void onClickImageTaken (View view)
    {
        showPictureSourceDialog();
    }

    /**
     * This is what happens when picture-giving activity returns to our app
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && imageURI != null)
                imageTaken.setImageURI(imageURI);
            else if (requestCode == REQUEST_GALLERY) {
                Uri selectedImage = data.getData();
                imageTaken.setImageURI(selectedImage);
            }
            isUserImage = true;
        }
    }
        @Override
        public void onBackPressed() {
            super.onBackPressed();
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            finish();
        }

        private void showPictureSourceDialog (){
            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
            // TODO Localise this, change to icons or whatever
            pictureDialog.setTitle("Select Action");
            String[] pictureDialogItems = {
                    "Select photo from gallery",
                    "Capture photo from camera" };
            pictureDialog.setItems(pictureDialogItems,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    choosePhotoFromGallery();
                                    break;
                                case 1:
                                    takePhotoFromCamera();
                                    break;
                            }
                        }
                    });
            pictureDialog.show();
        }
        private void choosePhotoFromGallery()
        {
            Intent getImageFromGallery = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI).setType("image/*");
            startActivityForResult(getImageFromGallery, REQUEST_GALLERY);
        }

        private void takePhotoFromCamera()
        {
            Intent takeImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                if (takeImageIntent.resolveActivity(getPackageManager()) != null) {

                    File imgFile=null;
                    try {
                        imgFile = File.createTempFile("tmp", ".png",getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    imageURI =FileProvider.getUriForFile(this,BuildConfig.APPLICATION_ID+".fileprovider",imgFile);
                    takeImageIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                            imageURI);
                    startActivityForResult(takeImageIntent, REQUEST_IMAGE_CAPTURE);
                }
                else Toast.makeText(this, R.string.ErrNoAppToMakePhotos, Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},PERMISSION_CAMERA);
            }
        }

    }
