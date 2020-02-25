package com.example.snapchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SnapsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ListView snapsListView;
    ArrayList<String> receivedEmails;                                            //List of emails who have send us the snaps
    ArrayList<DataSnapshot> snaps;                                            //List of Snaps received

//  on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snaps);

        mAuth = FirebaseAuth.getInstance();
        snapsListView = findViewById(R.id.snapsListView);
        receivedEmails = new ArrayList<String>();
        snaps = new ArrayList<DataSnapshot>();

        final ArrayAdapter aad = new ArrayAdapter(this, android.R.layout.simple_list_item_1,receivedEmails);
        snapsListView.setAdapter(aad);

//      2.Getting emails who have sent us snaps
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("snaps").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                String email = dataSnapshot.child("from").getValue().toString();
                receivedEmails.add(email);
                snaps.add(dataSnapshot);
                aad.notifyDataSetChanged();
            }
//          Deleting the email from the list
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                int index = 0;
                for( DataSnapshot snap : snaps){
                    if(snap.getKey() == dataSnapshot.getKey()){
                        snaps.remove(index);
                        receivedEmails.remove(index);
                    }
                    index++;
                }
                aad.notifyDataSetChanged();
            }
            //unused methods
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

//      3.Showing the snap from selected user
        snapsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //moving to View Snap Activity
                Intent intent = new Intent(getApplicationContext(),ViewSnapActivity.class);
                //Accessing correct snap from the firebase and passing it to the next Activty
                intent.putExtra("imageName",snaps.get(position).child("imageName").getValue().toString());
                intent.putExtra("imageURL",snaps.get(position).child("imageURL").getValue().toString());
                intent.putExtra("message",snaps.get(position).child("message").getValue().toString());
                intent.putExtra("snapKey",snaps.get(position).getKey());                      //UID that is created for the new snap
                startActivity(intent);
            }
        });
    }



//  1.Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.snaps_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

//      1.1.Move to CreateSnap Activity
        if(item.getItemId() == R.id.createSnap){
            startActivity(new Intent(getApplicationContext(),CreateSnapActivity.class));
        }
//      1.2.log out
        else if(item.getItemId() == R.id.logout){
            mAuth.signOut();          //Signing out the user
            finish();               //taking to main activity
        }
        return super.onOptionsItemSelected(item);
    }

//  1.3.back button
    @Override
    public void onBackPressed() {       //1.3.Log out on back button
        mAuth.signOut();
        super.onBackPressed();
    }
}
