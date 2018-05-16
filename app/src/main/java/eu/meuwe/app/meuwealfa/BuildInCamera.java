package eu.meuwe.app.meuwealfa;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by HP on 23.01.2018.
 */
/**
 public class BuildInCamera {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_CAPTURE = 1;
    private Uri fileUri;


    btn.setOnClickListener(new View.OnClickListener()

    {

        @Override
        public void onClick (View v){
        capturepic();
    }
    });

    private void capturepic() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(intent, CAMERA_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
// if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE) {
            if (resultCode == RESULT_OK) {
                capturedImage();
            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(Activity.this,
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(Activity.this,
                        "Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void capturedImage() {
        try {

            BitmapFactory.Options options = new BitmapFactory.Options();


            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            img.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable("file_uri");
    }
}
 */