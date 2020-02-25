 package com.example.snapchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

 public class CreateSnapActivity extends AppCompatActivity {

    ImageView createSnapImageView;
    EditText messageEditText;
    String imageName = UUID.randomUUID().toString() + ".jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_snap);

        createSnapImageView = findViewById(R.id.createSnapImageView);
        messageEditText = findViewById(R.id.messageEditText);

    }

//   1.Photo importing
     public void chooseImageClicked(View view){
         if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){    //check if we have permission
             requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);               //requesting for the permission if we don't have - here we are calling the onRequestPermissionsResult method to get permission
         }
         else {
             getPhoto();                                                                                             //when they already give permission then request photo
         }
     }

//     ask photo
     public void getPhoto(){
         Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);          //here we use intent to move to an android provided activity to pick images
         startActivityForResult(intent,1);
     }

//     this method is called when we get back result from activity
     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         Uri selectedImage = data.getData();//location of selected image
         if (requestCode == 1 && resultCode == RESULT_OK && data != null) {    //checking that we get the correct - result that we needed
             try{
                 Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                 createSnapImageView.setImageBitmap(bitmap);
             }
             catch (Exception e){
                 e.printStackTrace();
             }
         }
     }

//     requesting permission for accessing photos
     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);

         if(requestCode == 1){
             if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                 getPhoto();                 //if we got the permission get the photo
             }
         }
     }


//   2.1.uploading image to Firebase
     public void nextClicked(View view){
         // Get the data from an ImageView as bytes
         createSnapImageView.setDrawingCacheEnabled(true);
         createSnapImageView.buildDrawingCache();
         Bitmap bitmap = ((BitmapDrawable) createSnapImageView.getDrawable()).getBitmap();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
         byte[] data = baos.toByteArray();

         /*
         FirebaseStorage                                   Starting point for the Firebase
          .getInstance().getReference()                    Creating reference for FirebaseStorage
         .child("images").child(imageName);               Creating folder(Child) and image inside it
          */

         UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("images").child(imageName).putBytes(data);
         uploadTask.addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception exception) {
                 Toast.makeText(CreateSnapActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
             }
         }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
             @Override
             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.

//               2.2.To download url Storage Reference is necessary
                 final StorageReference imageRef = FirebaseStorage.getInstance().getReference("images/" + imageName);
                 imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                     @Override
                     public void onComplete(@NonNull Task<Uri> task) {
                         String imageUrl = task.getResult().toString();
                         Log.i("Image URL ", imageUrl);

//               3.Moving to Choose Users Activity
                         Intent intent = new Intent(getApplicationContext(),ChooseUserActivity.class);
                         intent.putExtra("Name",imageName);
                         intent.putExtra("Url", imageUrl);
                         intent.putExtra("Message",messageEditText.getText().toString());

                         startActivity(intent);
                     }
                 });



             }
         });
     }
 }
