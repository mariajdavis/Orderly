package com.bcit.orderly;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.SecureRandom;
import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddProjectDialogFragment extends DialogFragment {

    FirebaseFirestore db;
    DocumentReference userDocRef;

    /**
     * Public constructor to create an AddProjectDialogFragment instance.
     *
     * @param docRef A document reference of the current user.
     */
    public AddProjectDialogFragment(DocumentReference docRef) {
        userDocRef = docRef;
    }

    /**
     * Factory method to create an AddProjectDialogFragment alert dialog.
     * Called by GroupsActivity.
     *
     * @param docRef A document reference of the current user.
     * @return A new instance of fragment AddProjectDialogFragment.
     */
    public static AddProjectDialogFragment newInstance(DocumentReference docRef) {
        return new AddProjectDialogFragment(docRef);
    }

    /**
     * Inflate and set functionality to the AlertDialog.
     *
     * @param savedInstanceState State information.
     * @return AlertDialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();

        View view = View.inflate(getActivity(), R.layout.fragment_add_project, null);
        DatePicker datePicker = view.findViewById(R.id.datePicker_add_project);
        EditText projectNameEditText = view.findViewById(R.id.editText_add_project);

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());

        // Set onClick to the OK button in the alert dialog
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String projectNameInput = projectNameEditText.getText().toString();

                if (!projectNameInput.isEmpty()) {

                    String groupCode = generateRandomCode(5);
                    Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                            datePicker.getMonth(),
                            datePicker.getDayOfMonth());
                    long time = calendar.getTimeInMillis();

                    ArrayList<DocumentReference> users = new ArrayList<>();
                    users.add(userDocRef);

                    Project newProject = new Project(projectNameInput, time, groupCode, new ArrayList<>(), users);

                    addProject(newProject);
                }
            }
        });

        // Set onClick to the Cancel button in the alert dialog
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });

        adb.setTitle("Add Project");
        adb.setView(view);

        return adb.create();
    }

    /**
     * Generate a String containing 5 uppercase alphanumeric characters.
     *
     * @param length Desired length of the returned String.
     * @return String.
     */
    public String generateRandomCode(int length) {
        Random random = new SecureRandom();
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        char[] res = new char[length];

        for (int i = 0; i < res.length; i++) {
            int j = random.nextInt(chars.length);
            res[i] = chars[j];
        }

        return new String(res);
    }

    /**
     * Add the project with the specified projectId to the user's projects array.
     *
     * @param projectId Project ID of the project to be added to the database.
     */
    public void addToUsersProjectsArray(String projectId) {
        userDocRef.update("projects",
                FieldValue.arrayUnion(db.collection("projects").document(projectId)))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("added to user's project array");
                    }
                });
    }

    /**
     * Add a project to the database.
     *
     * @param project Project to be added to the database.
     */
    public void addProject(Project project) {
        db.collection("projects")
                .add(project)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        addToUsersProjectsArray(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Debug", "Error");
                    }
                });
    }

    /**
     * onDismiss handler.
     */
    public void onDismiss(DialogInterface.OnDismissListener onDismissListener) {
    }
}
