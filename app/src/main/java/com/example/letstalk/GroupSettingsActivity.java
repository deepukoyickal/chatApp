package com.example.letstalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupSettingsActivity extends AppCompatActivity {

    private String currentGroupId,currentGroupName;
    private EditText groupName,groupStatus;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GalleryPick = 1;
    private StorageReference GroupProfileImagesRef;
    private ProgressDialog loadingBar;

    private Toolbar SettingsToolBar;
    private CircleImageView groupProfileImage;
    private Button updateSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        GroupProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        currentGroupId = getIntent().getExtras().get("currentGroupId").toString();
        currentGroupName = getIntent().getExtras().get("currentGroupName").toString();

        InitializeFields();
        updateSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                UpdateSettings();
            }
        });


        RetrieveUserInfo();


        groupProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null)
        {
            //Uri ImageUri = data.getData();

            CropImage.ActivityBuilder activity = CropImage.activity();
            activity.setGuidelines(CropImageView.Guidelines.ON);
            activity.setAspectRatio(1, 1);
            activity.start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE )
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resultUri = result.getUri();


                 final StorageReference filePath = GroupProfileImagesRef.child(currentGroupId + ".jpg");

                filePath.putFile(resultUri).continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception
                    {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(GroupSettingsActivity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            //final String downloaedUrl = task.getResult().getStorage().getDownloadUrl().toString();
//
                            Uri link = task.getResult();
                            String  downloaedUrl = link.toString();
                            RootRef.child("Groups").child(currentGroupId).child("image")
                                    .setValue(downloaedUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(GroupSettingsActivity.this, "Image save in Database, Successfully...", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().toString();
                                                Toast.makeText(GroupSettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(GroupSettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }


    private void RetrieveUserInfo()
    {
        RootRef.child("Groups").child(currentGroupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrievesStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                            Uri image = Uri.parse(dataSnapshot.child("image").getValue().toString());

                            groupName.setText(retrieveUserName);
                            groupStatus.setText(retrievesStatus);
                            Picasso.get().load(retrieveProfileImage).into(groupProfileImage);
                            //Picasso.get().load(image).resize(100,100).centerCrop().into(userProfileImage);
                            groupName.setVisibility(View.VISIBLE);
                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrievesStatus = dataSnapshot.child("status").getValue().toString();

                            groupName.setText(retrieveUserName);
                            groupStatus.setText(retrievesStatus);
                            groupName.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            groupName.setVisibility(View.VISIBLE);
                            Toast.makeText(GroupSettingsActivity.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void UpdateSettings()
    {
        final String setgroupName = groupName.getText().toString();
        String setStatus = groupStatus.getText().toString();

        if (TextUtils.isEmpty(setgroupName))
        {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show();
        }
        else
        {
            RootRef.child("Groups").child(currentGroupId).child("name").setValue(setgroupName);
            RootRef.child("Groups").child(currentGroupId).child("status").setValue(setStatus)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(GroupSettingsActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                                Intent group = new Intent(GroupSettingsActivity.this,GroupChatActivity.class);
                                group.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                group.putExtra("groupId", currentGroupId);
                                group.putExtra("groupName",setgroupName);
                                startActivity(group);
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(GroupSettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void InitializeFields()
    {
        updateSettings = (Button) findViewById(R.id.update_group_settings_button);
        groupName = (EditText) findViewById(R.id.set_group_name);
        groupStatus = (EditText) findViewById(R.id.set_group_status);
        groupProfileImage = (CircleImageView) findViewById(R.id.set_group_profile_image);
        loadingBar = new ProgressDialog(this);

        SettingsToolBar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Group Settings");
    }
}
