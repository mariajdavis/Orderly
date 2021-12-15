package com.bcit.orderly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private AuthState authState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        authState = AuthState.LOGIN;

        initialSetup(authState);

        TextView authSwitchTV = findViewById(R.id.textView_main_login_or_signup_button);
        authSwitchTV.setOnClickListener(view -> {
            TextView questionTV = findViewById(R.id.textView_main_question);
            if (authState.equals(AuthState.LOGIN)) {
                authState = AuthState.SIGNUP;
                questionTV.setText("Already have an account?");
                authSwitchTV.setText("Log In");
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainerView_main_form, AuthFragment.newInstance(authState));
                ft.commit();
            } else if (authState.equals(AuthState.SIGNUP)) {
                authState = AuthState.LOGIN;
                questionTV.setText("Don't have an account yet?");
                authSwitchTV.setText("Sign Up");
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainerView_main_form, AuthFragment.newInstance(authState));
                ft.commit();
            }
        });
    }

    /**
     * Set up the sign up page.
     *
     * @param authState AuthState.
     */
    private void initialSetup(AuthState authState){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainerView_main_form, AuthFragment.newInstance(authState));
        ft.commit();
        TextView authSwitchTV = findViewById(R.id.textView_main_login_or_signup_button);
        TextView questionTV = findViewById(R.id.textView_main_question);
        authSwitchTV.setText("Sign Up");
        questionTV.setText("Don't have an account?");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload(); // Not sure if this is the right call (was just reload() before)
        }
    }
}