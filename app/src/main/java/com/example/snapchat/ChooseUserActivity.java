package com.example.snapchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChooseUserActivity extends AppCompatActivity {

    ListView chooseUserListView;
    ArrayList<String> emails;
    ArrayList<String> keys;                                                                         //For storing the UID of the receiver(to whom we want to send)

    /*
    If A want to send to B,(A clicks B from the list) then we are going to add child "Snap" which contain the following things in the B's UID
    To add these things we first create a snapMap to store them
    * B's UID
        * email - B's email id(Already added)
        * Snaps
            * From  - A's email id
            * Message   - "How are you"
            * image name - "something.jpg"
            * image url - url of that image

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_user);

//      1.Creating Array list and array adapter
        chooseUserListView = findViewById(R.id.chooseUserListView);
        emails = new ArrayList<String>();
        final ArrayAdapter aad = new ArrayAdapter(this, android.R.layout.simple_list_item_1,emails);
        chooseUserListView.setAdapter(aad);
        keys = new ArrayList<String>();


//      Fetching data from firebase database
        FirebaseDatabase.getInstance().getReference().child("users").addChildEventListener(new com.google.firebase.database.ChildEventListener(){
            //
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String email = dataSnapshot.child("email").getValue().toString();
                //Log.i("onChildAdded : ", email);
                emails.add(email);                      //whenever a new user is added we add it to the email Arraylist
                keys.add(dataSnapshot.getKey());        //here we add the UID of the newly added user to the keys Arraylist
                aad.notifyDataSetChanged();
            }

            //unused methods
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        //Selecting user from email list
        chooseUserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Map<String,String> snapMap = new HashMap<String,String>();
                snapMap.put("from", FirebaseAuth.getInstance().getCurrentUser().getEmail());

                //getting all these details from the previous activity using extra
                Intent secondIntent = getIntent();
                snapMap.put("imageName", secondIntent.getStringExtra("Name"));
                snapMap.put("imageURL", secondIntent.getStringExtra("Url"));
                snapMap.put("message", secondIntent.getStringExtra("Message"));

                //Add map to database
                /*
                FirebaseDatabase.getInstance().getReference().child("users")         referring to the users in the databse
                .child(keys.get(position))                                           referring to the uid of the selected users(receiver)
                .child("snaps")                                                      creating child snap
                .push()                                                              push() creates a new child with random name i.e. new Uid, otherwise w/o push the child is simply added w/o any uniqueness
                .setValue(snapMap);                                                  adding the snapMap to the snaps
                 */
                FirebaseDatabase.getInstance().getReference().child("users").child(keys.get(position)).child("snaps").push().setValue(snapMap);


                //Getting to snaps activity of the user, after selecting to whom they want to senf
                Intent intent = new Intent(getApplicationContext(), SnapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);                       //we dont want the user to get to the select users' screen ,this clears the history of the back button, keeps only what it currently have
                startActivity(intent);




            }
        });
    }
}
