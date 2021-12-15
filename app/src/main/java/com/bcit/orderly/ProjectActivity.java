package com.bcit.orderly;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProjectActivity extends AppCompatActivity {

    FirebaseFirestore db;
    Button button_teamDetails;
    Button button_addTask;
    DocumentReference projectDocRef;
    Project project;
    List<String> taskReferences;
    List<Task> tasks;
    String projectId;
    List<String> userIdList;
    HashMap<String, String> userHashMap;
    String selectedTaskId;
    FragmentManager fManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        projectId = bundle.getString("PROJECT_OBJECT");

        this.fManager = getSupportFragmentManager();

        setUp();

        button_teamDetails = findViewById(R.id.button_projects_team_details);
        button_addTask = findViewById(R.id.button_projects_add_task);

        button_teamDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainerView_tasks, GroupDetailsFragment.newInstance(userHashMap, projectId)); //pass in group documentid as param?
                ft.commit();
            }
        });

        button_addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainerView_tasks, AddTaskFragment.newInstance(userHashMap, projectId));
                ft.commit();
            }
        });

    }

    /**
     * Set up the global references for the ProjectActivity.
     */
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        FirebaseUser firebase_user = FirebaseAuth.getInstance().getCurrentUser();
        if (firebase_user != null) {
            String user_id = firebase_user.getUid();
            projectDocRef = db.collection("projects").document(projectId);
            projectDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("TAG", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.d("TAG", "Current data: " + snapshot.getData());
                        project = snapshot.toObject(Project.class);
                        // TODO - Handle onFailure (add error toast?)
                        assert project != null;
                        if (project.getTaskList() != null) {
                            taskReferences = project.getTaskIds();
                        }
                        if (project.getUserList() != null) {
                            userIdList = project.getUserIds();
                            setUserHash();
                        }
                        setProjectTasks();
                    } else {
                        Log.d("TAG", "Current data: null");
                    }
                }
            });
        }
    }

    /**
     * Set up the user HashMap for the ProjectActivity in the global space.
     */
    public void setUserHash() {
        userHashMap = new HashMap<String, String>();
        for (String userId : userIdList) {
            db.collection("users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    userHashMap.put((String) documentSnapshot.get("name"), userId);
                }
            });
        }

        setProjectTasks();
    }

    /**
     * Set up the project task references for the ProjectActivity in the global space.
     */
    public void setProjectTasks() {
        CollectionReference projectsRef = db.collection("projects");
        if (taskReferences != null && taskReferences.size() != 0 ) {
            projectsRef.whereIn(FieldPath.documentId(), taskReferences)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                            tasks = new ArrayList<Task>();
                            for (DocumentSnapshot docSnapshot : documents) {
                                Task task = docSnapshot.toObject(Task.class);
                                assert task != null;
                                task.setProjectId(projectId);
                                tasks.add(task);
                            }
                            displayTasks();
                        }
                    });
        } else {
            System.out.println("missing project");
        }
    }

    /**
     * Display the tasks of the project in a recyclerView.
     */
    public void displayTasks() {
        if (!this.fManager.isDestroyed()) {
            FragmentTransaction ft = this.fManager.beginTransaction();
            ft.replace(R.id.fragmentContainerView_tasks, TaskListFragment.newInstance(projectId));
            ft.commit();
        }
    }

}
