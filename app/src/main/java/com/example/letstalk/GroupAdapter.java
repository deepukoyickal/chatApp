package com.example.letstalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MessageViewHolder>
{
    private List<Messages> GroupMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference GroupRef,userRef;
    private String currentGroupId,currentGroupName;

    public GroupAdapter (List<Messages> userMessagesList)
    {
        this.GroupMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText,sender_status_icon,senderTime,receiverTime;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            sender_status_icon = (TextView)itemView.findViewById(R.id.sender_message_icon);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            senderTime = (TextView) itemView.findViewById(R.id.sender_time);
            receiverTime = (TextView) itemView.findViewById(R.id.receiver_time);


        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_group_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }
    public void getGroupId(String currentGroupId1,String currentGroupName1)

    {
        currentGroupId = currentGroupId1;
        currentGroupName = currentGroupName1;
//        messageId = messageId1;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = GroupMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();


        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).child("messages");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.sender_status_icon.setVisibility(View.GONE);
        messageViewHolder.senderTime.setVisibility(View.GONE);
        messageViewHolder.receiverTime.setVisibility(View.GONE);


        if (fromMessageType.equals("text")) {

           if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderTime.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.template2);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.senderTime.setText(messages.getTime() + " - " + messages.getDate());
               if(messages.getStatus().equals("sent"))
               {
                   messageViewHolder.sender_status_icon.setVisibility(View.VISIBLE);
               }


            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTime.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.template1);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.receiverTime.setText(messages.getTime() + " - " + messages.getDate());
            }
        } else if (fromMessageType.equals("images")) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.senderTime.setVisibility(View.VISIBLE);
                messageViewHolder.messageSenderPicture.setBackgroundResource(R.drawable.template2);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderTime.setText(messages.getTime() + " - " + messages.getDate());
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
                if(messages.getStatus().equals("sent"))
                {
                    messageViewHolder.sender_status_icon.setVisibility(View.VISIBLE);
                }
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTime.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setBackgroundResource(R.drawable.template1);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverTime.setText(messages.getTime() + " - " + messages.getDate());
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);

            }
        }
        else if (fromMessageType.equals("pdf"))
        {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.senderTime.setVisibility(View.VISIBLE);
                messageViewHolder.senderTime.setText(messages.getTime() + " - " + messages.getDate());
                Picasso.get().load(R.drawable.pdf).into(messageViewHolder.messageSenderPicture);
                if(messages.getStatus().equals("sent"))
                {
                    messageViewHolder.sender_status_icon.setVisibility(View.VISIBLE);
                }
            }
            else
                {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTime.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverTime.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverTime.setText(messages.getTime() + " - " + messages.getDate());
                    Picasso.get().load(R.drawable.pdf).into(messageViewHolder.messageSenderPicture);

            }
        }
        else if(fromMessageType.equals("docx"))
        {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.senderTime.setVisibility(View.VISIBLE);
                messageViewHolder.senderTime.setText(messages.getTime() + " - " + messages.getDate());
                Picasso.get().load(R.drawable.word).into(messageViewHolder.messageSenderPicture);
                if(messages.getStatus().equals("sent"))
                {
                    messageViewHolder.sender_status_icon.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTime.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTime.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTime.setText(messages.getTime() + " - " + messages.getDate());
                Picasso.get().load(R.drawable.word).into(messageViewHolder.messageSenderPicture);
            }
        }
        else if (fromMessageType.equals("video"))
        {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.senderTime.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.video).into(messageViewHolder.messageSenderPicture);
                messageViewHolder.senderTime.setText(messages.getTime() + " - " + messages.getDate());


                if(messages.getStatus().equals("sent"))
                {
                    messageViewHolder.sender_status_icon.setVisibility(View.VISIBLE);
                }

            } else  {
                messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTime.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.video).into(messageViewHolder.messageSenderPicture);
                messageViewHolder.receiverTime.setText(messages.getTime() + " - " + messages.getDate());

            }
        }
        else if (fromMessageType.equals("audio"))
        {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.senderTime.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);
                messageViewHolder.senderTime.setText(messages.getTime() + " - " + messages.getDate());
                Picasso.get().load(R.drawable.audio).into(messageViewHolder.messageSenderPicture);

                if(messages.getStatus().equals("sent"))
                {
                    messageViewHolder.sender_status_icon.setVisibility(View.VISIBLE);
                }

            } else  {
                messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTime.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTime.setText(messages.getTime() + " - " + messages.getDate());
                Picasso.get().load(R.drawable.audio).into(messageViewHolder.messageSenderPicture);

            }
        }
        if(fromUserID.equals(messageSenderId))
        {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (GroupMessagesList.get(position).getType().equals("pdf") || GroupMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "delete for me",
                                        "delete for everyone",
                                        "download and view file",
                                        "cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if (i == 0)
                                {
                                    deleteSentMessage(position,messageViewHolder);
//
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);
                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 2)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(GroupMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 3)
                                {

                                }
                            }
                        });
                        builder.show();
                    }
                    else if (GroupMessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "delete for me",
                                        "delete for everyone",
                                        "cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if (i == 0)
                                {
                                    deleteSentMessage(position,messageViewHolder);
//                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);
                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (GroupMessagesList.get(position).getType().equals("images"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "delete for me",
                                        "delete for everyone",
                                        "view this image",
                                        "cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if (i == 0)
                                {
                                    deleteSentMessage(position,messageViewHolder);
//                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);
//                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 2)
                                {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url", GroupMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 3)
                                {

                                }
                            }
                        });
                        builder.show();
                    }
                    else if (GroupMessagesList.get(position).getType().equals("video"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "delete for me",
                                        "delete for everyone",
                                        "download and play",
                                        "cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if (i == 0)
                                {
                                    deleteSentMessage(position,messageViewHolder);
//                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);
//                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 2)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(GroupMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 3)
                                {

                                }
                            }
                        });
                        builder.show();
                    }
                    else if (GroupMessagesList.get(position).getType().equals("audio"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "delete for me",
                                        "delete for everyone",
                                        "download and play",
                                        "cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if (i == 0)
                                {
                                    deleteSentMessage(position,messageViewHolder);
//                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);
//                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 2)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(GroupMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 3)
                                {

                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (GroupMessagesList.get(position).getType().equals("pdf") || GroupMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "delete for me",
                                        "download and view file",
                                        "cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if (i == 0)
                                {
                                    deleteRecieveMessage(position,messageViewHolder);
//                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(GroupMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (GroupMessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "delete for me",
                                        "cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if (i == 0)
                                {
                                    deleteRecieveMessage(position,messageViewHolder);
//                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (GroupMessagesList.get(position).getType().equals("images"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "delete for me",
                                        "view this image",
                                        "cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if (i == 0)
                                {
                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1)
                                {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url", GroupMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (GroupMessagesList.get(position).getType().equals("video"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "delete for me",
                                        "download and play",
                                        "cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if (i == 0)
                                {
                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(GroupMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (GroupMessagesList.get(position).getType().equals("audio"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "delete for me",
                                        "download and play",
                                        "cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if (i == 0)
                                {
                                    deleteRecieveMessage(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(GroupMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }


    private void deleteSentMessage(final int position, final GroupAdapter.MessageViewHolder holder)
    {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId);
        rootRef.child("messages")
                .child(GroupMessagesList.get(position).getFrom())
                .child(GroupMessagesList.get(position).getTo())
                .child(GroupMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(),"deleted",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"error occurred",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void deleteRecieveMessage(final int position, final GroupAdapter.MessageViewHolder holder)
    {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId);
        rootRef.child("messages")
                .child(GroupMessagesList.get(position).getTo())
                .child(GroupMessagesList.get(position).getFrom())
                .child(GroupMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(),"deleted",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"error occurred",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void deleteMessageForEveryone(final int position, final GroupAdapter.MessageViewHolder holder)
    {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId);
        rootRef.child("messages")
                .child(GroupMessagesList.get(position).getFrom())
                .child(GroupMessagesList.get(position).getTo())
                .child(GroupMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    rootRef.child("messages")
                            .child(GroupMessagesList.get(position).getTo())
                            .child(GroupMessagesList.get(position).getFrom())
                            .child(GroupMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(),"deleted",Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"error occurred",Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public int getItemCount()
    {
        return GroupMessagesList.size();
    }
}
