package com.example.letstalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SendMessageButton,SendFilesButton;
    private ScrollView mScrollView;
    private TextView displayTextMessages,SenderTextMessages,RecieverTextMessages;
    private StorageTask uploadTask;
    private Uri fileUri;
    private String saveCurrentTime, saveCurrentDate;
    private ProgressDialog loadingBar;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private Toolbar ChatToolBar;
    private RelativeLayout messageBox;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef,friendsGroupRef,otherGroupMsgRef,otherMsgKeyRef,deepu;

    private String  currentDate, currentTime,currentGroupId,checker,myUrl,MessageStatus,groupImage;
    public  String currentGroupName, currentUserID, currentUserName;
    public TextView GroupName;
    public CircleImageView GroupImage;
    private ImageView emojiButton;
    private EmojiconEditText userMessageInput, emojiconEditText2;
    private EmojIconActions emojIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupId = getIntent().getExtras().get("groupId").toString();
        currentGroupName = getIntent().getExtras().get("groupName").toString();
//        Toast.makeText(GroupChatActivity.this, currentGroupId, Toast.LENGTH_SHORT).show();
//        Toast.makeText(GroupChatActivity.this,currentGroupName,Toast.LENGTH_LONG).show();


        checker = "text";
        MessageStatus = "not sent";
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).child("messages");
//        friendsGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentUserID).child(currentGroupName).child("members");
//        deepu =  FirebaseDatabase.getInstance().getReference().child("Groups");



           InitializeFields();

        emojIcon = new EmojIconActions(this, userMessagesList, userMessageInput, emojiButton);
        emojIcon.ShowEmojIcon();
        emojIcon.setUseSystemEmoji(true);
        // textView.setUseSystemDefault(true);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "open");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "close");
            }
        });
        emojIcon.addEmojiconEditTextList(emojiconEditText2);

        emojIcon.addEmojiconEditTextList(emojiconEditText2);
        emojiconEditText2.setEmojiconSize(100);
           FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot)
               {
                   if(dataSnapshot.exists()&& dataSnapshot.hasChild("image")) {
                       groupImage = dataSnapshot.child("image").getValue().toString();
                       Picasso.get().load(groupImage).placeholder(R.drawable.profile_image).into(GroupImage);
                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
           GroupName.setText(currentGroupName);



        GetUserInfo();


        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
               SaveMessageInfoToDatabase();
               userMessageInput.getText().toString();


               userMessageInput.setText("");

                //mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        GroupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(GroupChatActivity.this,ImageViewerActivity.class);
                intent.putExtra("url", groupImage);
                startActivity(intent);
            }
        });
        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF File",
                                "MS Word File",
                                "Video File",
                                "Audio File"
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Select File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (i == 0)
                        {
                            checker = "images";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }
                        if (i == 1)
                        {
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select Pdf file"), 438);
                        }
                        if (i == 2)
                        {
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/docx");
                            startActivityForResult(intent.createChooser(intent, "Select Word file"), 438);
                        }
                        if (i == 3)
                        {
                            checker = "video";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("video/*");
                            startActivityForResult(intent.createChooser(intent, "Select Video file"), 438);
                        }
                        if (i == 4)
                        {
                            checker = "audio";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("audio/*");
                            startActivityForResult(intent.createChooser(intent, "Select Word file"), 438);
                        }

                    }
                });
                builder.show();
            }
        });
   }

    @Override
    protected void onActivityResult(int requestcode, int resultcode, @Nullable Intent data) {
        super.onActivityResult(requestcode, resultcode, data);
        if (requestcode == 438 && resultcode == RESULT_OK && data != null && data.getData() != null)
        {
            loadingBar.setTitle("Sending file");
            loadingBar.setMessage("Please wait, sending file.......");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();
            if (checker.equals("pdf") || checker.equals("docx"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                final String messagekEY = GroupNameRef.push().getKey();
                final StorageReference filePath = storageReference.child(messagekEY + "." + checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                HashMap<String, Object> groupMessageKey = new HashMap<>();
                                GroupNameRef.updateChildren(groupMessageKey);
                                GroupMessageKeyRef = GroupNameRef.child(messagekEY);

                                HashMap<String, Object> messageInfoMap = new HashMap<>();
                                messageInfoMap.put("name", currentUserName);
                                messageInfoMap.put("from",currentUserID);
                                messageInfoMap.put("messageID",messagekEY);
                                messageInfoMap.put("to",currentGroupId);
                                messageInfoMap.put("message", downloadUrl);
                                messageInfoMap.put("type", checker);
                                messageInfoMap.put("date", currentDate);
                                messageInfoMap.put("time", currentTime);
                                messageInfoMap.put("status","sent");

                                GroupMessageKeyRef.updateChildren(messageInfoMap);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                            }
                        });


                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0* taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading...");

                    }
                });
            }

            else if(checker.equals("images"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messagekEY = GroupNameRef.push().getKey();
                final StorageReference filePath = storageReference.child(messagekEY + "."+"jpg");

                uploadTask = filePath.putFile(fileUri);
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri)
                            {

                                myUrl=uri.toString();
                                HashMap<String, Object> messageInfoMap = new HashMap<>();
                                messageInfoMap.put("name", currentUserName);
                                messageInfoMap.put("from",currentUserID);
                                messageInfoMap.put("messageID",messagekEY);
                                messageInfoMap.put("to",currentGroupId);
                                messageInfoMap.put("message", myUrl);
                                messageInfoMap.put("type", checker);
                                messageInfoMap.put("date", currentDate);
                                messageInfoMap.put("time", currentTime);
                                messageInfoMap.put("status","sent");
                                GroupMessageKeyRef.updateChildren(messageInfoMap);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0* taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading...");
                    }
                });
//                uploadTask.continueWithTask(new Continuation() {
//                    @Override
//                    public Object then(@NonNull Task task) throws Exception {
//                        if(!task.isSuccessful())
//                        {
//                            throw task.getException();
//                        }
//                        return filePath.getDownloadUrl();
//                    }
//                }).addOnCompleteListener(new OnCompleteListener<Uri>(){
//                    @Override
//                    public void onComplete(@NonNull Task<Uri> task)
//                    {
//                        if(task.isSuccessful())
//                        {
//                            Uri downloadUrl=task.getResult();
//                            myUrl=downloadUrl.toString();
//                            HashMap<String, Object> messageInfoMap = new HashMap<>();
//                            messageInfoMap.put("name", currentUserName);
//                            messageInfoMap.put("from",currentUserID);
//                            messageInfoMap.put("messageID",messagekEY);
//                            messageInfoMap.put("to",currentGroupId);
//                            messageInfoMap.put("message", myUrl);
//                            messageInfoMap.put("type", checker);
//                            messageInfoMap.put("date", currentDate);
//                            messageInfoMap.put("time", currentTime);
//                            messageInfoMap.put("status","sent");
//
//                            GroupMessageKeyRef.updateChildren(messageInfoMap).addOnCompleteListener(new OnCompleteListener() {
//                                @Override
//                                public void onComplete(@NonNull Task task)
//                                {
//                                    if (task.isSuccessful())
//                                    {
//                                        loadingBar.dismiss();
//                                        Toast.makeText(GroupChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
//                                    }
//                                    else
//                                    {
//                                        loadingBar.dismiss();
//                                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                                    }
//                                    userMessageInput.setText("");
//                                }
//                            });
//                        }
//                    }
//                });

            }
            else if(checker.equals("video"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Video Files");
                final String messagekEY = GroupNameRef.push().getKey();
                final StorageReference filePath = storageReference.child(messagekEY + "."+"mp4");



                uploadTask = filePath.putFile(fileUri);
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri)
                            {

                                myUrl=uri.toString();
                                HashMap<String, Object> messageInfoMap = new HashMap<>();
                                messageInfoMap.put("name", currentUserName);
                                messageInfoMap.put("from",currentUserID);
                                messageInfoMap.put("messageID",messagekEY);
                                messageInfoMap.put("to",currentGroupId);
                                messageInfoMap.put("message", myUrl);
                                messageInfoMap.put("type", checker);
                                messageInfoMap.put("date", currentDate);
                                messageInfoMap.put("time", currentTime);
                                messageInfoMap.put("status","sent");
                                GroupMessageKeyRef.updateChildren(messageInfoMap);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0* taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading...");
                    }
                });

//                uploadTask = filePath.putFile(fileUri);
//                uploadTask.continueWithTask(new Continuation() {
//                    @Override
//                    public Object then(@NonNull Task task) throws Exception {
//                        if(!task.isSuccessful())
//                        {
//                            throw task.getException();
//                        }
//                        return filePath.getDownloadUrl();
//                    }
//                }).addOnCompleteListener(new OnCompleteListener<Uri>(){
//                    @Override
//                    public void onComplete(@NonNull Task<Uri> task)
//                    {
//                        if(task.isSuccessful())
//                        {
//                            Uri downloadUrl=task.getResult();
//                            myUrl=downloadUrl.toString();
//                            HashMap<String, Object> messageInfoMap = new HashMap<>();
//                            messageInfoMap.put("name", currentUserName);
//                            messageInfoMap.put("from",currentUserID);
//                            messageInfoMap.put("messageID",messagekEY);
//                            messageInfoMap.put("to",currentGroupId);
//                            messageInfoMap.put("message", myUrl);
//                            messageInfoMap.put("type", checker);
//                            messageInfoMap.put("date", currentDate);
//                            messageInfoMap.put("time", currentTime);
//                            messageInfoMap.put("status","sent");
//
//                            GroupMessageKeyRef.updateChildren(messageInfoMap).addOnCompleteListener(new OnCompleteListener() {
//                                @Override
//                                public void onComplete(@NonNull Task task)
//                                {
//                                    if (task.isSuccessful())
//                                    {
//                                        loadingBar.dismiss();
//                                        Toast.makeText(GroupChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
//                                    }
//                                    else
//                                    {
//                                        loadingBar.dismiss();
//                                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                                    }
//                                    userMessageInput.setText("");
//                                }
//                            });
//                        }
//                    }
//                });

            }
            else if(checker.equals("audio"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Audio Files");
                final String messagekEY = GroupNameRef.push().getKey();
                final StorageReference filePath = storageReference.child(messagekEY + "." +"mp3");


                uploadTask = filePath.putFile(fileUri);
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri)
                            {

                                myUrl=uri.toString();
                                HashMap<String, Object> messageInfoMap = new HashMap<>();
                                messageInfoMap.put("name", currentUserName);
                                messageInfoMap.put("from",currentUserID);
                                messageInfoMap.put("messageID",messagekEY);
                                messageInfoMap.put("to",currentGroupId);
                                messageInfoMap.put("message", myUrl);
                                messageInfoMap.put("type", checker);
                                messageInfoMap.put("date", currentDate);
                                messageInfoMap.put("time", currentTime);
                                messageInfoMap.put("status","sent");
                                GroupMessageKeyRef.updateChildren(messageInfoMap);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0* taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading...");
                    }
                });
//                uploadTask = filePath.putFile(fileUri);
//                uploadTask.continueWithTask(new Continuation() {
//                    @Override
//                    public Object then(@NonNull Task task) throws Exception {
//                        if(!task.isSuccessful())
//                        {
//                            throw task.getException();
//                        }
//                        return filePath.getDownloadUrl();
//                    }
//                }).addOnCompleteListener(new OnCompleteListener<Uri>(){
//                    @Override
//                    public void onComplete(@NonNull Task<Uri> task)
//                    {
//                        if(task.isSuccessful())
//                        {
//                            Uri downloadUrl=task.getResult();
//                            myUrl=downloadUrl.toString();
//                            HashMap<String, Object> messageInfoMap = new HashMap<>();
//                            messageInfoMap.put("name", currentUserName);
//                            messageInfoMap.put("from",currentUserID);
//                            messageInfoMap.put("messageID",messagekEY);
//                            messageInfoMap.put("to",currentGroupId);
//                            messageInfoMap.put("message", myUrl);
//                            messageInfoMap.put("type", checker);
//                            messageInfoMap.put("date", currentDate);
//                            messageInfoMap.put("time", currentTime);
//                            messageInfoMap.put("status","sent");
//
//                            GroupMessageKeyRef.updateChildren(messageInfoMap).addOnCompleteListener(new OnCompleteListener() {
//                                @Override
//                                public void onComplete(@NonNull Task task)
//                                {
//                                    if (task.isSuccessful())
//                                    {
//                                        loadingBar.dismiss();
//                                        Toast.makeText(GroupChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
//                                    }
//                                    else
//                                    {
//                                        loadingBar.dismiss();
//                                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                                    }
//                                    userMessageInput.setText("");
//                                }
//                            });
//                        }
//                    }
//                });

            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this,"error",Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onStart()
    {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
//                if (dataSnapshot.exists())
//                {
//
//                   DisplayMessages(dataSnapshot);
//                }
                messageAdapter.getGroupId(currentGroupId,currentGroupName);
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);


                messageAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
//                if (dataSnapshot.exists())
//                {
//                    DisplayMessages(dataSnapshot);
//                }
//                messageAdapter.getGroupId(currentGroupId);
//                Messages messages = dataSnapshot.getValue(Messages.class);
//                messagesList.add(messages);
//
//
//                messageAdapter.notifyDataSetChanged();
//
//                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void InitializeFields()
    {
        ChatToolBar = (Toolbar) findViewById(R.id.group_page_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.group_chat_layout, null);
        actionBar.setCustomView(actionBarView);

        GroupName = (TextView) findViewById(R.id.custom_group_name);
        GroupImage = (CircleImageView) findViewById(R.id.custom_group_image);

        //ChatToolBar = (Toolbar) findViewById(R.id.group_page_toolbar);
        setSupportActionBar(ChatToolBar);
        //getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        userMessageInput = (EmojiconEditText) findViewById(R.id.input_group_messages);
        emojiButton = (ImageView) findViewById(R.id.emoji_btn);
        emojiconEditText2 = (EmojiconEditText) findViewById(R.id.emojicon_edit_text2);
       // displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);
        RecieverTextMessages = (TextView) findViewById(R.id.receiver_message);
        SenderTextMessages = (TextView) findViewById(R.id.sender_message);
       // mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
        loadingBar = new ProgressDialog(this);
        //messageBox = (RelativeLayout)findViewById(R.id.message_box);

        messageAdapter = new GroupAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.group_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());


    }




    private void GetUserInfo()
    {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    private void SaveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String messagekEY = GroupNameRef.push().getKey();

//        if (TextUtils.isEmpty(message))
//        {
//            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
//        }
//        else
//        {
        if (!message.equals("")) {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());


            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        MessageStatus ="sent";
                    }
                }
            });

            GroupMessageKeyRef = GroupNameRef.child(messagekEY);

            final HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("from",currentUserID);
            messageInfoMap.put("messageID",messagekEY);
            messageInfoMap.put("to",currentGroupId);
            messageInfoMap.put("message", message);
            messageInfoMap.put("type", checker);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            messageInfoMap.put("status","sent");
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }

    }
    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {



            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String from = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String messageid = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
            String to = (String) ((DataSnapshot) iterator.next()).getValue();
            String type = (String) ((DataSnapshot) iterator.next()).getValue();

//            SenderTextMessages.setVisibility(View.GONE);
//            RecieverTextMessages.setVisibility(View.GONE);

            if(from.equals(currentUserID))
            {
                SenderTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "     " + chatDate + type+ " " + "\n\n\n");
            }
            else
            {
                RecieverTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "     " + chatDate + type+ " " + "\n\n\n");
            }


            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_group, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.group_add_friends_option)
        {
            AddGroupMembers();
        }
        if(item.getItemId() == R.id.group_settings)
        {
            groupSettings();
        }
        if (item.getItemId() == R.id.group_cancel)
        {

        }

        return true;
    }

    private void groupSettings()
    {
        Intent settings = new Intent(GroupChatActivity.this, GroupSettingsActivity.class);
        settings.putExtra("currentGroupId" , currentGroupId);
        settings.putExtra("currentGroupName" , currentGroupName);
        startActivity(settings);
    }

    private void AddGroupMembers()
    {
        Intent settingsIntent = new Intent(GroupChatActivity.this, AddGroupMembersActivity.class);
        settingsIntent.putExtra("currentGroupId" , currentGroupId);
        settingsIntent.putExtra("currentGroupName" , currentGroupName);
        startActivity(settingsIntent);
    }

}