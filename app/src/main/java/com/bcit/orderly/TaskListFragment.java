package com.bcit.orderly;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class TaskListFragment extends Fragment {

    private static final String PROJECT_ID = "PROJECT_ID";

    private String projectId;
    private Project project;
    private List<Task> tasks;
    private List<String> taskReferences;
    private RecyclerView rv;
    private FirebaseFirestore db;
    private List<Task> toDoTasks;
    private List<Task> completedTasks;
    private ItemTaskAdapter adapter;

    public TaskListFragment() {
    }

    public static TaskListFragment newInstance(String projectId) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putString(PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(PROJECT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        rv = view.findViewById(R.id.recyclerView_fragment_taskList);
        toDoTasks = new ArrayList<>();
        completedTasks = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        if (projectId != null) {
            System.out.println("project id:");
            System.out.println(projectId);
            DocumentReference docRef = db.collection("projects").document(projectId);
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("TAG", "Listen .setProjectId() failed.", e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Log.d("TAG", "Current data: " + snapshot.getData());
                        project = snapshot.toObject(Project.class);
                        // TODO - Handle onFailure (add error toast?)
                        assert project != null;
                        taskReferences = project.getTaskIds();
                        setProjectTasks();
                    } else {
                        Log.d("TAG", "Current data: null");
                    }
                }
            });
        }

        TabLayout tabLayout = view.findViewById(R.id.tabLayout_taskList_isCompletedOrNot);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Tab 0 is to do, tab 1 is completed
                if (tab.getPosition() == 0 && toDoTasks.size() > 0) {
                    System.out.println("to do");
                    displayToDoTasks();
                } else if (tab.getPosition() == 1 && completedTasks.size() > 0) {
                    System.out.println("completed");
                    displayCompletedTasks();
                } else if (tab.getPosition() == 0 || tab.getPosition() == 1) {
                    adapter = new ItemTaskAdapter(new Task[0]);
                    rv.setAdapter(adapter);
                    rv.setLayoutManager(new LinearLayoutManager(getContext()));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (toDoTasks.size() > 0) {
                    displayToDoTasks();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (toDoTasks.size() > 0) {
                    displayToDoTasks();
                }
            }
        });

        return view;
    }

    /**
     * Set up the project task references for the ProjectActivity in the global space.
     */
    public void setProjectTasks() {
        CollectionReference tasksRef = db.collection("tasks");
        if (taskReferences != null && taskReferences.size() != 0 ) {
            tasksRef.whereIn(FieldPath.documentId(), taskReferences)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    tasks = new ArrayList<Task>();
                    for (DocumentSnapshot docSnapshot : documents) {
                        Task task = docSnapshot.toObject(Task.class);
                        assert task != null;
                        task.setProjectId(projectId);
                        task.setId(docSnapshot.getId());
                        tasks.add(task);
                    }
                    splitTasksCompletedOrNot();
                }
            });
        } else {
            System.out.println("No tasks");
        }
    }

    /**
     * Categorize tasks by completion status.
     */
    public void splitTasksCompletedOrNot() {
        if (tasks.size() > 0) {
            for (Task task: tasks) {
                if (!task.getIsCompleted()) {
                    toDoTasks.add(task);
                } else {
                    completedTasks.add(task);
                }
                if (toDoTasks.size() > 0) {
                    displayToDoTasks();
                }
            }
        }
    }

    /**
     * Display completed tasks in a ItemTaskAdapter.
     */
    public void displayCompletedTasks() {
        adapter = new ItemTaskAdapter(completedTasks.toArray(new Task[0]));
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * Display to do tasks in a ItemTaskAdapter.
     */
    public void displayToDoTasks() {
        adapter = new ItemTaskAdapter(toDoTasks.toArray(new Task[0]));
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

}
