package com.bcit.orderly;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddTaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AddTaskFragment extends Fragment {

    private static final String USER_HASH_MAP = "USER_HASH_MAP";
    private static final String PROJECT_ID = "PROJECT_ID";
//    private static final String PROJECT_DOC_REF = "PROJECT_DOC_REF";

    private HashMap<String, String> userHashMap;
    private String assignedUserName;
    private DocumentReference assignedUserDocRef;
    private FirebaseFirestore db;
    private DocumentReference taskDocRef;
    private String projectId;
    private DocumentReference projectDocRef;
    private List<Task> updatedTasks;
    private String newTaskId;

    /**
     * Factory method to create an AddTaskFragment.
     * Called by ProjectActivity.
     *
     * @param userHashMap HashMap of users.
     * @param projectId Project ID of the current project.
     * @return A new instance of AddTaskFragment.
     */
    public static AddTaskFragment newInstance(HashMap<String, String> userHashMap, String projectId) {
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle args = new Bundle();
        args.putSerializable(USER_HASH_MAP, (Serializable) userHashMap);
        args.putSerializable(PROJECT_ID, (Serializable) projectId);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Public constructor to create an AddTaskFragment instance.
     */
    public AddTaskFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userHashMap = (HashMap<String, String>) getArguments().getSerializable(USER_HASH_MAP);
            projectId = (String) getArguments().getSerializable(PROJECT_ID);
            if (projectId != null) {
                setProjectDocRef(projectId);
            }
        }
    }

    /**
     * Set the document reference to the project in the global space.
     *
     * @param projectId Project ID of the current project.
     */
    public void setProjectDocRef(String projectId) {
        db.collection("projects").document(projectId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                projectDocRef = documentSnapshot.getReference();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();


        Button backButton = view.findViewById(R.id.button_fragment_addTask_backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToTaskListFragment();
            }
        });

        EditText nameTE = view.findViewById(R.id.editText_addTask_name);
        EditText descriptionTE = view.findViewById(R.id.editTextMultiLine_addTask_description);
        DatePicker datePicker = view.findViewById(R.id.datePicker_addTask_dueDate);
        Spinner userSpinner = view.findViewById(R.id.spinner_addTask_assignedUser);

        if (userHashMap != null) {
            ArrayList<String> userNameList = new ArrayList<String>(userHashMap.keySet());
            String[] userNameArray = new String[userNameList.size()];
            userNameArray = userNameList.toArray(userNameArray);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_dropdown_item, userNameArray);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            userSpinner.setAdapter(arrayAdapter);
            userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    assignedUserName = (String) parent.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }

        Button addTaskSubmitButton = view.findViewById(R.id.button_fragment_addTask_addTask);

        addTaskSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameTE.getText().toString();
                String description = descriptionTE.getText().toString();
                Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                        datePicker.getMonth(),
                        datePicker.getDayOfMonth());
                long time = calendar.getTimeInMillis();
                createTaskWithAssignedUserDocRef(name, description, time);
            }
        });
    }

    /**
     * Set the document reference to the project in the global space.
     */
    public void returnToTaskListFragment() {
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainerView_tasks, TaskListFragment.newInstance(projectId));
        ft.commit();
    }

    /**
     * Obtain the task ID of the task to be added and calls the addTaskToProjectArray handler.
     *
     * @param newTask Task to be written.
     */
    public void addTaskToDatabase(Task newTask) {
        db.collection("tasks")
                .add(newTask)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        taskDocRef = documentReference;
                        addTaskToProjectArray(documentReference.getId());

                        newTask.setId(documentReference.getId());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Debug", "Error");
                    }
                });
    }

    public void addTaskToProjectArray(String newTaskId) {
        if (projectDocRef != null) {
            projectDocRef.update("tasks",
                    FieldValue.arrayUnion(db.collection("tasks").document(newTaskId)))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            updateTaskId(newTaskId);
                            returnToTaskListFragment();
                        }
                    });
        }
    }


    public void updateTaskId(String newTaskId) {
        db.collection("tasks").document(newTaskId).update("id", newTaskId);
    }

    /**
     * Create a task with an assigned user and call the addTaskToDatabase handler.
     *
     * @param name Name of the task.
     * @param description Description of the task.
     * @param time Due date of the task.
     */
    public void createTaskWithAssignedUserDocRef(String name, String description, long time) {
        String assignedUserId = userHashMap.get(assignedUserName);
        db.collection("users").document(assignedUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Task newTask = new Task(name, description, false, documentSnapshot.getReference(), new ArrayList<String>(), time, projectId);
                addTaskToDatabase(newTask);
            }
        });
    }

}
