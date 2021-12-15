package com.bcit.orderly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuthFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthFragment extends Fragment {

    private static final String AUTH_STATE = "AUTH_STATE";
    private AuthState authState;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public AuthFragment() {
        // Required empty public constructor
    }

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText verifyPasswordEditText;

    /**
     * Factory method to create an AuthFragment instance.
     * Called by MainActivity.
     *
     * @param authStateParam AuthState.
     * @return A new instance of fragment AuthFragment.
     */
    public static AuthFragment newInstance(AuthState authStateParam) {
        AuthFragment fragment = new AuthFragment();
        Bundle args = new Bundle();
        args.putSerializable(AUTH_STATE, authStateParam);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authState = AuthState.LOGIN;
        mAuth = FirebaseAuth.getInstance(); // Figure out a better way to handle auth across the app
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            authState = (AuthState) getArguments().getSerializable(AUTH_STATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nameEditText = view.findViewById(R.id.editText_auth_fragment_name);
        emailEditText = view.findViewById(R.id.editText_auth_fragment_email);
        passwordEditText = view.findViewById(R.id.editText_auth_fragment_password);
        verifyPasswordEditText = view.findViewById(R.id.editText_auth_fragment_password_verify);

        Button submitButton = view.findViewById(R.id.button_auth_fragment_submit);
        if (authState.equals(AuthState.LOGIN)) {
            submitButton.setText("Log in");
            nameEditText.setVisibility(View.GONE);
            verifyPasswordEditText.setVisibility(View.GONE);
        } else if (authState.equals(AuthState.SIGNUP)) {
            submitButton.setText("Sign up");
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nameInput;
                String emailInput;
                String passwordInput;
                // Add input validation checks here

                if (authState.equals(AuthState.LOGIN)) {
                    emailInput = emailEditText.getText().toString();
                    passwordInput = passwordEditText.getText().toString();
                    logIn(emailInput, passwordInput);
                } else if (authState.equals(AuthState.SIGNUP)) {
//                    nameInput = nameET.getText().toString();
                    emailInput = emailEditText.getText().toString();
                    passwordInput = passwordEditText.getText().toString();
                    nameInput = nameEditText.getText().toString();
                    signUp(emailInput, passwordInput, nameInput);
                }
            }
        });

    }

    /**
     * Check that the password input in both fields are equal and longer than 6 characters.
     *
     * @param password Password.
     * @param verifyPassword Verify password.
     * @return Boolean.
     */
    boolean verifyPassword(String password, String verifyPassword) {
        return password.equals(verifyPassword) && password.length() > 6;
    }

    /**
     * Check that the name input contains only alphabet characters.
     *
     * @param name Name.
     * @return Boolean.
     */
    boolean verifyName(String name) {
        return name.matches("[a-zA-Z]+");
    }

    /**
     * Move to the GroupsActivity.
     */
    void moveToGroupsActivity() {
        Intent groupsIntent = new Intent(getContext(), GroupsActivity.class);
        startActivity(groupsIntent);
    }

    /**
     * Adds a new user to the database and moves to the GroupsActivity.
     *
     * @param email Email of the new user.
     * @param name Name of the new user.
     */
    void addNewUserToUsersCollection(String email, String name) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String user_id = user.getUid();
            User new_user = new User(name, email, true, new ArrayList<>());
            db.collection("users")
                    .document(user_id)
                    .set(new_user)
                    .addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            Log.d("TAG", "User added to firestore db");

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error adding user to firestore db", e);
                        }
                    });
        } else {
        }
        moveToGroupsActivity();
    }

    /**
     * Create a user with the email and password and call the addNewUserToUsersCollection handler.
     *
     * @param email Email of the new user.
     * @param password Password of the new user.
     * @param name Name of the new user.
     */
    void signUp(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("", "createUserWithEmail:success");
                            addNewUserToUsersCollection(email, name);

                        } else {
                            Log.w("", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    /**
     * Handler for the login authentication.
     *
     * @param email Email of the user.
     * @param password Password of the user.
     */
    void logIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("", "signInWithEmail:success");
                            moveToGroupsActivity();
                        } else {
                            Log.w("", "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }
}

