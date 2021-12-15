package com.bcit.orderly;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JoinGroupDialogFragment extends DialogFragment {

    FirebaseFirestore db;
    DocumentReference userDocRef;

    /**
     * Public constructor to create an JoinGroupDialogFragment instance.
     *
     * @param docRef A document reference of the current user.
     */
    public JoinGroupDialogFragment(DocumentReference docRef) {
        userDocRef = docRef;
    }

    /**
     * Factory method to create an JoinGroupDialogFragment alert dialog.
     * Called by GroupsActivity.
     *
     * @param docRef A document reference of the current user.
     * @return A new instance of fragment JoinGroupDialogFragment.
     */
    public static JoinGroupDialogFragment newInstance(DocumentReference docRef) {
        return new JoinGroupDialogFragment(docRef);
    }

    /**
     * Inflate and set functionality to the AlertDialog.
     *
     * @param savedInstanceState State information.
     * @return AlertDialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();

        View view = View.inflate(getActivity(), R.layout.fragment_join_group, null);
        EditText joinGroupEditText = view.findViewById(R.id.editText_join_group);

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());

        // Set onClick to the OK button in the alert dialog
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupCodeInput = joinGroupEditText.getText().toString();
                if (!groupCodeInput.isEmpty()) {
                    db.collection("projects")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {

                                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());

                                            Map<String, Object> project = document.getData();
                                            String code =  (String) project.get("groupCode");
                                            if (code.equals(groupCodeInput)) {
                                                DocumentReference docRef = db.collection("projects").document(document.getId());
                                                docRef.update("users", FieldValue.arrayUnion(userDocRef));
                                                docRef.update("userList", FieldValue.arrayUnion(userDocRef));
                                                docRef.update("userIds", FieldValue.arrayUnion(userDocRef.getId()));

                                                userDocRef.update("projects", FieldValue.arrayUnion(docRef));
                                            }
                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                }
            }
        });

        // Set onClick to the Cancel button in the alert dialog
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });

        adb.setTitle("Join Group");
        adb.setView(view);

        return adb.create();
    }

    /**
     * onDismiss handler.
     */
    public void onDismiss(DialogInterface.OnDismissListener onDismissListener) {
    }
}
