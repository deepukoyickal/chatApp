package com.example.letstalk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserViewActivity extends AppCompatActivity {

    private CircleImageView contactImage;
    private TextView contactName;
    private TextView contactStatus;
    private String contactId;
    DatabaseReference UserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view);

        contactImage = (CircleImageView)findViewById(R.id.contact_profile_image);
        contactName = (TextView) findViewById(R.id.contact_user_name);
        contactStatus = (TextView) findViewById(R.id.contact_profile_status);

        contactId =  getIntent().getExtras().get("contactId").toString();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");



    }
    private void RetrieveUserInfo()
    {
        UserRef.child(contactId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.exists())  &&  (dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();

                    // Picasso.get().load(Contacts.recieverUserId.getImage()).placeholder(R.drawable.profile).into(profileImageView);

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(contactImage);
                    contactName.setText(userName);
                    contactStatus.setText(userstatus);


                }
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();

                    contactName.setText(userName);
                    contactStatus.setText(userstatus);
                    Picasso.get().load(R.drawable.profile_image).placeholder(R.drawable.profile_image).into(contactImage);



                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
