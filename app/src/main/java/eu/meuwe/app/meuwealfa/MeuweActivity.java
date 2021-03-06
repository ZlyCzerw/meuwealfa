package eu.meuwe.app.meuwealfa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MeuweActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "eu.meuwe.app.meuwealfa";

    String Uid;
    Bitmap bitmap;
    Bitmap rotateBitmap;

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

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        byte[] b = extras.getByteArray("capture");

        if(b!=null){
            ImageView image = findViewById(R.id.imageTaken);

            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(b, 0, b.length);

            rotateBitmap = rotate(decodedBitmap);


            image.setImageBitmap(rotateBitmap);
        }

        Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Button post = findViewById(R.id.post);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToEvent();
            }
        });
    }

    private void  saveToEvent() {

        final DatabaseReference userEventDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Uid").child("event");
        final String key = userEventDb.push().getKey();

        StorageReference filePath = FirebaseStorage.getInstance().getReference().child("pictures").child(key);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotateBitmap.compress(Bitmap.CompressFormat.JPEG,20 ,baos);
        byte[] dataToUpload = baos.toByteArray();
        UploadTask uploadTask = filePath.putBytes(dataToUpload);


        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri imageUrl = taskSnapshot.getDownloadUrl();

                Long startTimestamp = System.currentTimeMillis();
                Long endTimestamp = startTimestamp + (24*60*60*1000);

                Map<String, Object> mapToUpload = new HashMap<>();
                mapToUpload.put("imageUrl", imageUrl.toString());
                mapToUpload.put("startTimestamp", startTimestamp);
                mapToUpload.put("endTimestamp", endTimestamp);

                userEventDb.child(key).setValue(mapToUpload);
                finish();
                return;
            }
        });



        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                finish();
                return;

            }
        });

    }


    private Bitmap rotate(Bitmap decodedBitmap) {
        int w = decodedBitmap.getWidth();
        int h = decodedBitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        return Bitmap.createBitmap(decodedBitmap, 0, 0, w, h, matrix, true);

    }

    /**
     * Called when the user taps the Send button
     */
    public void sendMessage(View view) {

        Intent intent = new Intent(this, DisplayMueweActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    //not used now
    public void gobackhome(View home) {

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
