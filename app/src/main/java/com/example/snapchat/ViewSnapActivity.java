package com.example.snapchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ViewSnapActivity extends AppCompatActivity {

    TextView messageTextView;
    ImageView snapImageView;
    private FirebaseAuth mAuth;
//    Intent intent = getIntent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_snap);

        mAuth = FirebaseAuth.getInstance();
        messageTextView = findViewById(R.id.messageTextView);
        snapImageView = findViewById(R.id.snapImageView);

//      1.Getting Text
        Intent intent = getIntent();
        messageTextView.setText(intent.getStringExtra("message"));

//      2.1.Getting image
        ImageDownloader task = new ImageDownloader();
        Bitmap myImage;
        try {
            myImage = task.execute(intent.getStringExtra("imageURL")).get();
            snapImageView.setImageBitmap(myImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //  2.2.Downloading image using URL
    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.connect();       //start the connection
                InputStream in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }
    }

//    3. Deleting Image On Pressing Back Button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = getIntent();

        //Delete from Database
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("snaps").child(intent.getStringExtra("snapKey")).removeValue();

        //Delete from Storage
        FirebaseStorage.getInstance().getReference().child("images").child(intent.getStringExtra("imageName")).delete();

    }
}
