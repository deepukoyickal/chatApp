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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class ChatActivity extends AppCompatActivity {

    String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton SendMessageButton, SendFilesButton;
    private ImageView emojiButton;
    private EmojiconEditText MessageInputText, emojiconEditText2;
    private EmojIconActions emojIcon;



    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    String checker="",myUrl;
    private StorageTask uploadTask;
    private Uri fileUri;
    private String saveCurrentTime, saveCurrentDate;
    private ProgressDialog loadingBar;
    private FirebaseUser currentUser;
    private String currentUserID;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final String id,name,image;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();


//        name = getIntent().getExtras().get("visit_user_name").toString();
//        image = getIntent().getExtras().get("visit_image").toString();
        messageReceiverID =getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage =getIntent().getExtras().get("visit_image").toString();

        //Toast.makeText(ChatActivity.this,messageReceiverID,Toast.LENGTH_LONG).show();

        IntializeControllers();

        emojIcon = new EmojIconActions(this, userMessagesList, MessageInputText, emojiButton);
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

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //MessageInputText.getText().toString();
                SendMessage();
                MessageInputText.setText("");
            }
        });
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ChatActivity.this,ImageViewerActivity.class);
                intent.putExtra("url",messageReceiverImage);
                startActivity(intent);
            }
        });
        ChatToolBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent =new Intent(ChatActivity.this,ProfileActivity.class);
                intent.putExtra("contactId", messageReceiverID);
                startActivity(intent);
            }
        });


        DisplayLastSeen();

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

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
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

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s)
                    {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
    }




    private void IntializeControllers()
    {
        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        emojiButton = (ImageView) findViewById(R.id.chat_emoji_button);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        MessageInputText = (EmojiconEditText) findViewById(R.id.input_chat_messages);
        emojiconEditText2 = (EmojiconEditText) findViewById(R.id.emojicon_edit_text2);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();


        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }
    @Override
    protected void onActivityResult(int requestcode, int resultcode, @Nullable Intent data){
        super.onActivityResult(requestcode,resultcode,data);
        if(requestcode == 438 && resultcode == RESULT_OK && data!=null && data.getData()!=null)
        {
            loadingBar.setTitle("Sending file");
            loadingBar.setMessage("Please wait, sending file.......");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();
            if(checker.equals("docx")|| checker.equals("pdf"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message", downloadUrl);
                                messageTextBody.put("name",fileUri.getLastPathSegment());
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderID);
                                messageTextBody.put("to", messageReceiverID);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);
                                messageTextBody.put("status","sent");

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                                RootRef.updateChildren(messageBodyDetails);
                                loadingBar.dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
            }
            else if(checker.equals("images"))
            {
                StorageReference    storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                            myUrl=uri.toString();
                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);
                            messageTextBody.put("status","sent");

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
//                            Map messageTextBody = new HashMap();
//                            messageTextBody.put("message", myUrl);
//                            messageTextBody.put("name",fileUri.getLastPathSegment());
//                            messageTextBody.put("type", checker);
//                            messageTextBody.put("from", messageSenderID);
//                            messageTextBody.put("to", messageReceiverID);
//                            messageTextBody.put("messageID", messagePushID);
//                            messageTextBody.put("time", saveCurrentTime);
//                            messageTextBody.put("date", saveCurrentDate);
//                            messageTextBody.put("status","sent");
//
//                            Map messageBodyDetails = new HashMap();
//                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
//                            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);
//
//                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
//                                @Override
//                                public void onComplete(@NonNull Task task)
//                                {
//                                    if (task.isSuccessful())
//                                    {
//                                        loadingBar.dismiss();
//                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
//                                    }
//                                    else
//                                    {
//                                        loadingBar.dismiss();
//                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                                    }
//                                    MessageInputText.setText("");
//                                }
//                            });
//                        }
//                    }
//                });

            }
            else if(checker.equals("video"))
            {
                StorageReference    storageReference = FirebaseStorage.getInstance().getReference().child("Video Files");
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "mp4");
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                myUrl=uri.toString();
                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message", myUrl);
                                messageTextBody.put("name",fileUri.getLastPathSegment());
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderID);
                                messageTextBody.put("to", messageReceiverID);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);
                                messageTextBody.put("status","sent");

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                                RootRef.updateChildren(messageBodyDetails);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
            }
            else if(checker.equals("audio"))
            {
                StorageReference    storageReference = FirebaseStorage.getInstance().getReference().child("Audio Files");
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "mp3");
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                myUrl=uri.toString();
                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message", myUrl);
                                messageTextBody.put("name",fileUri.getLastPathSegment());
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderID);
                                messageTextBody.put("to", messageReceiverID);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);
                                messageTextBody.put("status","sent");

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                                RootRef.updateChildren(messageBodyDetails);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this,"error",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                userLastSeen.setText("online");
                            }
                            else if (state.equals("offline"))
                            {
                                userLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        }
                        else
                        {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }



//    @Override
//    protected void onStart()
//    {
//        super.onStart();
//        updateUserStatus("online");
//        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
//                .addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
//                    {
//                        Messages messages = dataSnapshot.getValue(Messages.class);
//
//                        messagesList.add(messages);
//
//                        messageAdapter.notifyDataSetChanged();
//
//                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
//                    }
//
//                    @Override
//                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }



    private void SendMessage()
    {
        String messageText = MessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("status","sent");

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });
        }
    }
    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }

}