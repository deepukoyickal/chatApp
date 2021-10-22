package com.example.letstalk;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment {
    private View groupFragmentView;
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups = new ArrayList<>();

    private DatabaseReference GroupRef,currnetGroupRef;
    private FirebaseAuth mAuth;


    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);


        mAuth = FirebaseAuth.getInstance();
        final String uId = mAuth.getCurrentUser().getUid();
        GroupRef = FirebaseDatabase.getInstance().getReference().child("UserGroups").child(uId);
        currnetGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");



        IntializeFields();


        RetrieveAndDisplayGroups();


        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final String currentGroupName = adapterView.getItemAtPosition(position).toString();
                GroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {



                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            if (snapshot.hasChild("id")) {
                                final String key;

                                key = snapshot.child("id").getValue().toString();
                                currnetGroupRef.child(key).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1)
                                    {
                                        String user;
                                        user = dataSnapshot1.child("name").getValue().toString();
                                        if (user.equals(currentGroupName)) {
                                            Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                                            groupChatIntent.putExtra("groupId", key);
                                            groupChatIntent.putExtra("groupName", currentGroupName);
                                            startActivity(groupChatIntent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                // user = FirebaseDatabase.getInstance().getReference().child("Groups").child(key).child("name").toString();

                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
});


        return groupFragmentView;
    }


    private void IntializeFields() {
        list_view = (ListView) groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list_of_groups);
        list_view.setAdapter(arrayAdapter);
    }


    private void RetrieveAndDisplayGroups() {
        String groupNametemp;
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                final Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();


                if (dataSnapshot.exists())
                {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        if (snapshot.hasChild("id"))
                        {
                            String user;


                            user = snapshot.child("id").getValue().toString();

//                           name =  currnetGroupRef.child(user).child("name").toString();
//                           set.add(user);
                           currnetGroupRef.child(user).addValueEventListener(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot dataSnapshot1)
                               {
                                   String name;
                                   if(dataSnapshot1.exists())
                                   {
                                       if (dataSnapshot1.hasChild("name")) {
                                           name = dataSnapshot1.child("name").getValue().toString();
                                           set.add(name);
                                       }
                                       else {
                                           set.add("none");
                                       }

                                   }
                                   else
                                   {
                                       set.add("none");
                                   }
                                   list_of_groups.clear();
                                   list_of_groups.addAll(set);
                                   arrayAdapter.notifyDataSetChanged();
                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError databaseError) {

                               }
                           });
                        }
                    }
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
}