package eu.meuwe.app.meuwealfa;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import com.wonderkiln.camerakit.*;



import static java.security.AccessController.getContext;

public class CameraPreview extends AppCompatActivity   {


    android.hardware.Camera camera;
    android.hardware.Camera.PictureCallback jpegCallback;

    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    CameraView cameraView; //cameraKit
    final int CAMERA_REQUEST_CODE = 1;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }


 /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       setContentView(R.layout.activity_camera_preview);

        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(CameraPreview.this, new String[] {android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }else{
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

// adds logout button
        Button mLogout = findViewById(R.id.logout);
        ImageButton mCapture = findViewById(R.id.capture);

        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            captureImage();
            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogOut();
            }
        });


        jpegCallback=new android.hardware.Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, android.hardware.Camera camera) {

  /*              Bitmap decodedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap rotateBitmap = rotate(decodedBitmap);

              String fileLocation = SaveImageToStorage(rotateBitmap); //this comes later
                if(fileLocation!= null){
                    Intent intent = new Intent(CameraPreview.this, MeuweActivity.class);
                    intent.putExtra("capture", data );
                    startActivity(intent);
                    return;
                }
*/   /*
                Intent intent = new Intent(CameraPreview.this, MeuweActivity.class);
                intent.putExtra("capture", data );
                startActivity(intent);
                return;
            }
        };

    }*/

   /* public String SaveImageToStorage(Bitmap bitmap){ //this comes later
        String fileName = "imageToSend";
        try{
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
           FileOutputStream fo;
            fo = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
           fo.close();
        }catch(Exception e){
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }*/

  /*  private Bitmap rotate(Bitmap decodedBitmap) {

        int w = decodedBitmap.getWidth();
        int h = decodedBitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        return  Bitmap.createBitmap(decodedBitmap,0,0,w,h,matrix,true);
    }*/


    private void captureImage() {

        camera.takePicture(null,null,jpegCallback);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
       camera = android.hardware.Camera.open();

        android.hardware.Camera.Parameters parameters;
        parameters = camera.getParameters();

        camera.setDisplayOrientation(90);
        parameters.setPreviewFrameRate(30);
        parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        android.hardware.Camera.Size bestSize = null;
        List<android.hardware.Camera.Size> sizeList;
        sizeList = camera.getParameters().getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        for (int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }


        parameters.setPreviewSize(bestSize.width, bestSize.height);

        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

   @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mSurfaceHolder.addCallback(this);
                    mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                }else{
                    Toast.makeText(this, "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

    }
    private void LogOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(CameraPreview.this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return;
    }

}
