package com.bcit.orderly;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupDetailsFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER_HASH_MAP = "USER_HASH_MAP";
    private static final String PROJECT_ID = "PROJECT_ID"; // project document id
    private HashMap<String, String> userHashMap;
    Button button_confirm;
    TextView textView_groupId;
    private FirebaseFirestore db;
    private RecyclerView rv;
    private String projectId;

    /**
     * Public constructor to create a GroupDetailsFragment instance.
     *
     * @param projectId project ID of the current project.
     */
    public GroupDetailsFragment(String projectId) {
        // Required empty public constructor
        this.projectId = projectId;
    }

    /**
     * Factory method to create an GroupDetailsFragment instance.
     * Called by ProjectActivity.
     *
     * @param userHashMap HashMap of users.
     * @param projectId project ID of the current project.
     * @return A new instance of GroupDetailsFragment.
     */
    public static GroupDetailsFragment newInstance(HashMap<String, String> userHashMap, String projectId) {
        GroupDetailsFragment fragment = new GroupDetailsFragment(projectId);
        Bundle args = new Bundle();
        args.putSerializable(USER_HASH_MAP, (Serializable) userHashMap);
        args.putString(PROJECT_ID, projectId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            userHashMap = (HashMap<String, String>) getArguments().getSerializable(USER_HASH_MAP);
            projectId = getArguments().getString(PROJECT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Set the "back to tasks" button
        Button backButton = view.findViewById(R.id.button_fragment_taskDetails_backToTasks);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainerView_tasks, TaskListFragment.newInstance(projectId));
                ft.commit();
            }
        });

        // Inflate the recyclerView containing the group members
        if (userHashMap != null) {
            ArrayList<String> userNameList = new ArrayList<String>(userHashMap.keySet());
            String[] userNameArray = new String[userNameList.size()];
            userNameArray = userNameList.toArray(userNameArray);

            rv = view.findViewById(R.id.recyclerView_fragment_teamDetails_userMembers);
            GroupMembersAdapter adapter = new GroupMembersAdapter(userNameArray);
            rv.setAdapter(adapter);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        getGroupId(view);

        button_confirm = view.findViewById(R.id.button_fragment_groupDetails_confirm);
        // ok button, close team detail, go back to task list
        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getParentFragmentManager().beginTransaction();

                ft.replace(R.id.fragmentContainerView_tasks, TaskListFragment.newInstance(projectId));
                ft.commit();
            }
        });
    }

    /**
     * Get the group ID of the current project and display it in the info text.
     *
     * @param view View.
     */
    public void getGroupId(View view){
        textView_groupId = view.findViewById(R.id.textView_fragment_groupDetails_groupId);

        DocumentReference docRef = db.collection("projects").document(projectId);
//        TODO: pass in current project docID, and replace with:
//        DocumentReference docRef = db.collection("projects").document(ARG_PARAM1);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        System.out.println("DocumentSnapshot data: " + document.getData());
                        String groupId_fromDB = (String) document.get("groupCode");
                        textView_groupId.setText(groupId_fromDB);

                    } else {
                        System.out.println("No such document");
                    }
                } else {
                    System.out.println("get failed");
                }
            }
        });
    }


}