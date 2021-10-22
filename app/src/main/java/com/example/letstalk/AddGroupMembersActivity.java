package com.example.letstalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddGroupMembersActivity extends AppCompatActivity {

    private DatabaseReference ContactsRef,currentGroupRef,otherGroupRef;
    private FirebaseAuth mAuth;
    private String currentUserID,currentGroupId;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    String currentGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_members);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        currentGroupId = getIntent().getExtras().get("currentGroupId").toString();
        currentGroupName = getIntent().getExtras().get("currentGroupName").toString();
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        currentGroupRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId);

        recyclerView = (RecyclerView) findViewById(R.id.add_friends_recycler_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ContactsRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, AddGroupMembersActivity.FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, AddGroupMembersActivity.FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final AddGroupMembersActivity.FindFriendViewHolder holder, final int position, @NonNull Contacts model)
                    {
                        final String newMemberId = getRef(position).getKey();
                        final String newMemberName = model.getName();
                       // final String newMeberId = FirebaseDatabase.getInstance().getReference().child("Users")..toString();
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                               // Toast.makeText(AddGroupMembersActivity.this,"clicked",Toast.LENGTH_SHORT).show();
                                String visit_user_id = getRef(position).getKey();

//                                Intent profileIntent = new Intent(AddGroupMembersActivity.this, ProfileActivity.class);
//                                profileIntent.putExtra("visit_user_id", visit_user_id);
//                                startActivity(profileIntent);
                                CharSequence options[] = new CharSequence[]
                                        {
                                                "Add",
                                                "Cancel"
                                        };
                                AlertDialog.Builder builder = new AlertDialog.Builder(AddGroupMembersActivity.this);
                                builder.setTitle( "Add New Member" + newMemberName);

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i == 0)
                                        {
//                                            currentGroupRef.child(currentGroupName).child("members").child(newMemberId).child("name")
//                                                    .setValue(newMemberName);
                                            currentGroupRef.child("members").child(newMemberId).child("id").setValue(newMemberId);
                                            FirebaseDatabase.getInstance().getReference().child("UserGroups").child(newMemberId).child(currentGroupId).child("id").setValue(currentGroupId);
                                            FirebaseDatabase.getInstance().getReference().child("UserGroups").child(newMemberId).child(currentGroupId).child("name").setValue(currentGroupName)

//                                            FirebaseDatabase.getInstance().getReference().child("Groups").child(newMemberId).child(currentGroupName).setValue("")
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {
                                                            if(task.isSuccessful())
                                                            {
                                                               // FirebaseDatabase.getInstance().getReference().child("Groups").child(newMemberId)
                                                                Toast.makeText(AddGroupMembersActivity.this,"new member added",Toast.LENGTH_SHORT).show();
                                                                Intent groupIntent = new Intent(AddGroupMembersActivity.this,GroupChatActivity.class);
                                                                groupIntent.putExtra("groupId", currentGroupId);
                                                                groupIntent.putExtra("groupName", currentGroupName);
                                                                groupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(groupIntent);
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(AddGroupMembersActivity.this,"can't add member...this member may already exist...",Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                                builder.show();
                            }

                        });
                    }

                    @NonNull
                    @Override
                    public AddGroupMembersActivity.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        AddGroupMembersActivity.FindFriendViewHolder viewHolder = new AddGroupMembersActivity.FindFriendViewHolder(view);
                        return viewHolder;
                    }
                };

        recyclerView.setAdapter(adapter);

        adapter.startListening();
    }



    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;


        public FindFriendViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }


}
