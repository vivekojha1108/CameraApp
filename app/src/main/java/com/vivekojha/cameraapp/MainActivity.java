package com.vivekojha.cameraapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_APP_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    private static final String TAG = "MainActivity";
    public static final int All_PERMS_CODE = 1101;
    String currentPhotoPath;

    ImageView SelectedImage;
    Button CameraBtn, GalleryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SelectedImage = findViewById(R.id.imagePreview);
        CameraBtn = findViewById(R.id.btn_takeSelfie);
        GalleryBtn = findViewById(R.id.btn_gallery);

        CameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Toast.makeText(MainActivity.this, "Camera Button is Clicked", Toast.LENGTH_SHORT).show();
                AskPermissions();
            }
        });

        GalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(MainActivity.this, "Gallery Button is clicked", Toast.LENGTH_SHORT).show();
                Intent gallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });
    }

    private void AskPermissions() {
        //Ask to give only camera permission
       /* if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
//            OpenCamera();
            dispatchTakePictureIntent();
        }*/

        // Camera and Storage permission on same time
        Log.d(TAG, "VerifyingPermission : Asking for permission ");
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // index 0 = camera, index 1 = readStorage , index 2 = write Storage

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[2]) == PackageManager.PERMISSION_GRANTED) {

            dispatchTakePictureIntent();

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, All_PERMS_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == All_PERMS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //User Has Camera Permission
                //OpenCamera();
                dispatchTakePictureIntent();

            } else {
                Toast.makeText(this, "We need Camera Permission To click the photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

   /* private void OpenCamera() {
        //for opening Default Camera App
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, CAMERA_APP_REQUEST_CODE);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_APP_REQUEST_CODE) {
            /*Bitmap thumbnailImage = (Bitmap) data.getExtras().get("data");
            SelectedImage.setImageBitmap(thumbnailImage);*/

            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                SelectedImage.setImageURI(Uri.fromFile(f));
                Log.d(TAG, "Absolute Uri of Image : " + Uri.fromFile(f));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            }

        }

        if(requestCode == GALLERY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                Uri contentUri = data.getData();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp +"." + getFileExt(contentUri);
                Log.d(TAG, "onActivityResult: Gallery Image Uri:  " +  imageFileName);
                SelectedImage.setImageURI(contentUri);

             //   uploadImageToFirebase(imageFileName,contentUri);

            }
        }


    }

    //For extracting media type.
    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }



    //https://developer.android.com/training/camera/photobasics : android documentation

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); // it is private to our app only.

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); // Req.: Give storage Permission manually if not

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("TAG", "Error: " + ex.getMessage().toString());
                // Error occurred while creating the File...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.vivekojha.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_APP_REQUEST_CODE);
            }
        }
    }


}