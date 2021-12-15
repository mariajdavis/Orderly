package com.bcit.orderly;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskDetailsFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String TASK_ID = "TASK_ID";
    private static final String TASK = "TASK";

    FirebaseFirestore db;
    Switch switchView;
    Button button_confirm;
    String taskId;
    String projectId;
    Task task;
    String assignedUserName;
    DocumentReference taskDocRef;

    public TaskDetailsFragment() { }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param projectId The project ID.
     * @param taskId The task ID.
     * @param task The Task.
     * @return A new instance of fragment TaskDetailsFragment.
     */
    public static TaskDetailsFragment newInstance(String projectId, String taskId, Task task) {
        TaskDetailsFragment fragment = new TaskDetailsFragment();
        Bundle args = new Bundle();
        args.putString(PROJECT_ID, projectId);
        args.putString(TASK_ID, taskId);
        args.putSerializable(TASK, task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(PROJECT_ID);
            taskId = getArguments().getString(TASK_ID);
            task = (Task) getArguments().getSerializable(TASK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db.collection("users").document(task.getUser().getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User assignedUser = documentSnapshot.toObject(User.class);
                        assert assignedUser != null;
                        assignedUserName = assignedUser.getName();
                        checkCompletionStatus(view);
                        setText(view);
                    }
                });;

        if (taskId != null) {
            taskDocRef = db.collection("tasks").document(taskId);
        }

        switchView = view.findViewById(R.id.switch_fragment_taskDetails_completedSwitch);
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()) {
                    System.out.println("switch enabled");
                    taskDocRef.update("isCompleted", true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    System.out.println("DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Error updating document");
                                }
                            });
                }
                else {
                    System.out.println("switch disabled");
                    taskDocRef.update("isCompleted", false)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    System.out.println("DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Error updating document");
                                }
                            });
                }
            }
        });

        button_confirm = view.findViewById(R.id.button_fragment_taskDetails_backButton);
        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainerView_tasks, TaskListFragment.newInstance(projectId));
                ft.commit();
            }
        });
    }

    void setText(View view) {
        TextView titleTV = view.findViewById(R.id.textView_fragment_taskDetailTitle);
        titleTV.setText(task.getName());

        TextView detailsTV = view.findViewById(R.id.textView_fragment_taskDetailBox);
        detailsTV.setText(task.getDescription());

        TextView assignedToTV = view.findViewById(R.id.textView_fragment_taskAssignedToBox);
        TextView dueDateTV = view.findViewById(R.id.textView_fragment_dueDate);
        dueDateTV.setText(task.getDueDateAsString());

        System.out.println("username out: " + assignedUserName);
        assignedToTV.setText(assignedUserName);
    }

    void checkCompletionStatus(View view) {
        switchView = view.findViewById(R.id.switch_fragment_taskDetails_completedSwitch);

        if (taskDocRef != null) {
            taskDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            if ((boolean) document.get("isCompleted")) {
                                switchView.setChecked(true);
                            } else {
                                switchView.setChecked(false);
                            }
                        } else {
                            System.out.println("document does not exist");
                        }
                    } else {
                        System.out.println("get failed with " + task.getException());
                    }
                }
            });
        }
    }
}
