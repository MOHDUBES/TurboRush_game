package com.turborush.game;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EmailAuthManager {

    private final MainActivity activity;
    private final FirebaseAuth mAuth;
    
    // UI Elements
    private AlertDialog dialog;
    private TextView tvAuthTitle;
    private EditText etAuthName;
    private EditText etAuthEmail;
    private EditText etAuthPassword;
    private EditText etAuthConfirmPassword;
    private CheckBox cbShowPassword;
    private Button btnAuthAction;
    private TextView tvAuthForgotPassword;
    private TextView tvAuthToggleMode;

    private enum Mode {
        SIGN_IN, SIGN_UP, FORGOT_PASSWORD
    }

    private Mode currentMode = Mode.SIGN_IN;

    public EmailAuthManager(MainActivity activity) {
        this.activity = activity;
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_auth, null);

        tvAuthTitle = view.findViewById(R.id.tvAuthTitle);
        etAuthName = view.findViewById(R.id.etAuthName);
        etAuthEmail = view.findViewById(R.id.etAuthEmail);
        etAuthPassword = view.findViewById(R.id.etAuthPassword);
        etAuthConfirmPassword = view.findViewById(R.id.etAuthConfirmPassword);
        cbShowPassword = view.findViewById(R.id.cbShowPassword);
        btnAuthAction = view.findViewById(R.id.btnAuthAction);
        tvAuthForgotPassword = view.findViewById(R.id.tvAuthForgotPassword);
        tvAuthToggleMode = view.findViewById(R.id.tvAuthToggleMode);

        builder.setView(view);
        dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        updateUIForMode(Mode.SIGN_IN);

        tvAuthToggleMode.setOnClickListener(v -> {
            if (currentMode == Mode.SIGN_IN || currentMode == Mode.FORGOT_PASSWORD) {
                updateUIForMode(Mode.SIGN_UP);
            } else {
                updateUIForMode(Mode.SIGN_IN);
            }
        });

        tvAuthForgotPassword.setOnClickListener(v -> {
            updateUIForMode(Mode.FORGOT_PASSWORD);
        });

        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int inputType = isChecked ? 
                (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) : 
                (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            
            etAuthPassword.setInputType(inputType);
            etAuthConfirmPassword.setInputType(inputType);
            
            etAuthPassword.setSelection(etAuthPassword.getText().length());
            etAuthConfirmPassword.setSelection(etAuthConfirmPassword.getText().length());
        });

        btnAuthAction.setOnClickListener(v -> handleAction());

        dialog.show();
    }

    private void updateUIForMode(Mode mode) {
        currentMode = mode;
        etAuthEmail.setVisibility(View.VISIBLE);
        
        switch (mode) {
            case SIGN_IN:
                tvAuthTitle.setText("Sign In");
                etAuthName.setVisibility(View.GONE);
                etAuthPassword.setVisibility(View.VISIBLE);
                etAuthConfirmPassword.setVisibility(View.GONE);
                cbShowPassword.setVisibility(View.VISIBLE);
                tvAuthForgotPassword.setVisibility(View.VISIBLE);
                btnAuthAction.setText("Sign In");
                tvAuthToggleMode.setText("Don't have an account? Sign Up");
                break;

            case SIGN_UP:
                tvAuthTitle.setText("Create Account");
                etAuthName.setVisibility(View.VISIBLE);
                etAuthPassword.setVisibility(View.VISIBLE);
                etAuthConfirmPassword.setVisibility(View.VISIBLE);
                cbShowPassword.setVisibility(View.VISIBLE);
                tvAuthForgotPassword.setVisibility(View.GONE);
                btnAuthAction.setText("Sign Up");
                tvAuthToggleMode.setText("Already have an account? Sign In");
                break;

            case FORGOT_PASSWORD:
                tvAuthTitle.setText("Reset Password");
                etAuthName.setVisibility(View.GONE);
                etAuthPassword.setVisibility(View.GONE);
                etAuthConfirmPassword.setVisibility(View.GONE);
                cbShowPassword.setVisibility(View.GONE);
                tvAuthForgotPassword.setVisibility(View.GONE);
                btnAuthAction.setText("Send Reset Link");
                tvAuthToggleMode.setText("Back to Sign In");
                break;
        }
    }

    private void handleAction() {
        String email = etAuthEmail.getText().toString().trim();
        String password = etAuthPassword.getText().toString().trim();
        
        if (email.isEmpty()) {
            Toast.makeText(activity, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAuthAction.setEnabled(false);

        if (currentMode == Mode.SIGN_IN) {
            if (password.isEmpty()) {
                Toast.makeText(activity, "Please enter your password", Toast.LENGTH_SHORT).show();
                btnAuthAction.setEnabled(true);
                return;
            }
            signInUser(email, password);
        } else if (currentMode == Mode.SIGN_UP) {
            String name = etAuthName.getText().toString().trim();
            String confirmPassword = etAuthConfirmPassword.getText().toString().trim();
            
            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(activity, "Please fill all fields", Toast.LENGTH_SHORT).show();
                btnAuthAction.setEnabled(true);
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(activity, "Passwords do not match", Toast.LENGTH_SHORT).show();
                btnAuthAction.setEnabled(true);
                return;
            }
            signUpUser(name, email, password);
        } else if (currentMode == Mode.FORGOT_PASSWORD) {
            resetPassword(email);
        }
    }

    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    btnAuthAction.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            /*
                            if (!user.isEmailVerified()) {
                                user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                    if (verifyTask.isSuccessful()) {
                                        Toast.makeText(activity, "Please verify your email first. Check your Inbox (and Spam folder).", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(activity, "Failed to send verification email: " + verifyTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                                mAuth.signOut();
                            } else {
                            */
                                // Successful Login
                                dialog.dismiss();
                                String name = user.getDisplayName();
                                if (name == null || name.isEmpty()) name = email.split("@")[0];
                                activity.onGoogleLoginSuccess(name, "Signed in successfully!");
                            // }
                        }
                    } else {
                        Toast.makeText(activity, "Sign In Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signUpUser(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
                                // Send verification email
                                user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                    btnAuthAction.setEnabled(true);
                                    if (verifyTask.isSuccessful()) {
                                        Toast.makeText(activity, "Account created! Verification link sent to " + email + ". Check Inbox & Spam folder.", Toast.LENGTH_LONG).show();
                                        updateUIForMode(Mode.SIGN_IN);
                                        mAuth.signOut(); // Force them to verify before login
                                    } else {
                                        Toast.makeText(activity, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        }
                    } else {
                        btnAuthAction.setEnabled(true);
                        Toast.makeText(activity, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(activity, task -> {
                    btnAuthAction.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(activity, "Password reset link sent! Please check your Inbox and Spam folder.", Toast.LENGTH_LONG).show();
                        updateUIForMode(Mode.SIGN_IN);
                    } else {
                        Toast.makeText(activity, "Failed to send reset link: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
