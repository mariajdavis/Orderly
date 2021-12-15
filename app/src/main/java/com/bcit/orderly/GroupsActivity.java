package com.bcit.orderly;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import java.util.List;

public class GroupsActivity extends AppCompatActivity {

    List<String> projectReferences;
    User firestore_user;
    List<Project> projects;
    FirebaseFirestore db;
    DocumentReference docRef;
    ProjectsAdapter projectsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        setUp();
    }

    /**
     * Obtain the list of project references and calls the setUserProjects handler.
     */
    public void setUp() {
        projectsAdapter = new ProjectsAdapter(new Project[0]);
        db = FirebaseFirestore.getInstance();
        FirebaseUser firebase_user = FirebaseAuth.getInstance().getCurrentUser();
        if (firebase_user != null) {
            String user_id = firebase_user.getUid();
            docRef = db.collection("users").document(user_id);
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("TAG", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.d("TAG", "Current data: " + snapshot.getData());
                        firestore_user = snapshot.toObject(User.class);
                        // TODO - Handle onFailure (add error toast?)
                        assert firestore_user != null;
                        projectReferences = firestore_user.getProjectIds();
                        System.out.println(firestore_user.getProjectIds());
                        setUserProjects();
                    } else {
                        Log.d("TAG", "Current data: null");
                    }
                }
            });
        }
    }

    /**
     * Create a Project object for each project and calls the displayProjects handler.
     */
    public void setUserProjects() {
        CollectionReference projectsRef = db.collection("projects");
        if (projectReferences != null && projectReferences.size() != 0 ) {
            projectsRef.whereIn(FieldPath.documentId(), projectReferences)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                            projects = new ArrayList<Project>();
                            for (DocumentSnapshot docSnapshot : documents) {
                                Project project = docSnapshot.toObject(Project.class);
                                assert project != null;
                                project.setId(docSnapshot.getId());
                                projects.add(project);
                            }
                            displayProjects();
                        }
                    });
        } else {
            System.out.println("missing project");
        }
    }

    /**
     * Displays the projects in a recyclerView.
     */
    public void displayProjects() {
        RecyclerView rv = findViewById(R.id.recylerView_groups_projects);
        projectsAdapter = new ProjectsAdapter(projects.toArray(new Project[0]));
        rv.setAdapter(projectsAdapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * On click handler for the add project button.
     */
    public void onAddProjectButtonClick (View view) {
        FragmentManager fm = getSupportFragmentManager();
        AddProjectDialogFragment alertDialog = AddProjectDialogFragment.newInstance(docRef);
        alertDialog.onDismiss(new DialogInterface.OnDismissListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDismiss(DialogInterface dialog) {
                setUp();
                projectsAdapter.notifyDataSetChanged();
            }
        });
        alertDialog.show(fm, "");
    }

    /**
     * On click handler for the join group button.
     */
    public void onJoinGroupButtonClick (View view) {
        FragmentManager fm = getSupportFragmentManager();
        JoinGroupDialogFragment alertDialog = JoinGroupDialogFragment.newInstance(docRef);
        alertDialog.onDismiss(new DialogInterface.OnDismissListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDismiss(DialogInterface dialog) {
                setUp();
                projectsAdapter.notifyDataSetChanged();
            }
        });
        alertDialog.show(fm, "");
    }
}
