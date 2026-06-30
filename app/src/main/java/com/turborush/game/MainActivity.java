package com.turborush.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.text.InputType;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends Activity implements SensorEventListener {

    private GameSurfaceView gameSurfaceView;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    
    private FirebaseAnalytics mFirebaseAnalytics;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 1002;
    private static final int PERMISSION_REQ_ID = 22;

    private String[] getRequestedPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS
            };
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, getRequestedPermissions(), requestCode);
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, getRequestedPermissions(), requestCode);
                return false;
            }
        }
        return true;
    }

    public void requestVoicePermissions() {
        checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (gameSurfaceView != null && gameSurfaceView.getGameEngine() != null) {
                    VoiceChatManager vcm = gameSurfaceView.getGameEngine().voiceChatManager;
                    if (vcm != null) {
                        vcm.initEngineIfNeeded();
                        // Just enable local audio, Agora handles the rest automatically if already joined
                        vcm.enableLocalAudio();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("151470785874-47p7h0d6h3uvlqdt3gdgod9diftd8ll1.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Game View and set it as content view FIRST
        gameSurfaceView = new GameSurfaceView(this);
        setContentView(gameSurfaceView);

        // Make the app full screen and immersive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            @SuppressWarnings("deprecation")
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Setup Gyroscope / Accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameSurfaceView != null) {
            gameSurfaceView.onResume();
        }
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameSurfaceView != null) {
            gameSurfaceView.onPause();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameSurfaceView != null) {
            if (gameSurfaceView.getGameEngine() != null) {
                gameSurfaceView.getGameEngine().destroy();
            }
            gameSurfaceView.onDestroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (gameSurfaceView != null) {
            gameSurfaceView.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    // --- SensorEventListener implementation ---
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (gameSurfaceView != null) {
                gameSurfaceView.setTilt(event.values[0]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for simple tilt
    }

    public void pickAvatarImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    }

    public void startGoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    
    public void showEmailLoginDialog() {
        runOnUiThread(() -> {
            EmailAuthManager authManager = new EmailAuthManager(this);
            authManager.showDialog();
        });
    }

    public void onGoogleLoginSuccess(String name, String status) {
        if (gameSurfaceView != null && gameSurfaceView.getGameEngine() != null) {
            gameSurfaceView.getGameEngine().onGoogleLoginSuccess(name, status);
        }
    }

    public void onGoogleLoginFailure(String status) {
        if (gameSurfaceView != null && gameSurfaceView.getGameEngine() != null) {
            gameSurfaceView.getGameEngine().onGoogleLoginFailure(status);
        }
    }
    
    public void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Signed out completely
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null && gameSurfaceView != null) {
                gameSurfaceView.setCustomAvatar(imageUri);
            }
        }
        
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken(), account.getDisplayName());
            } catch (ApiException e) {
                Log.w("GoogleSignIn", "Google sign in failed", e);
                if (gameSurfaceView != null && gameSurfaceView.getGameEngine() != null) {
                    gameSurfaceView.getGameEngine().onGoogleLoginFailure("Google Login Failed!");
                }
            }
        }
    }
    
    private void firebaseAuthWithGoogle(String idToken, String displayName) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    String name = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : displayName;
                    if (name == null) name = "Racer";
                    if (gameSurfaceView != null && gameSurfaceView.getGameEngine() != null) {
                        gameSurfaceView.getGameEngine().onGoogleLoginSuccess(name, "Login Successful!");
                    }
                } else {
                    if (gameSurfaceView != null && gameSurfaceView.getGameEngine() != null) {
                        gameSurfaceView.getGameEngine().onGoogleLoginFailure("Firebase Auth Failed!");
                    }
                }
            });
    }

    public void showJoinRoomDialog() {
        runOnUiThread(() -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Join Multiplayer Room");
            
            final android.widget.EditText input = new android.widget.EditText(this);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            input.setHint("Enter Room Code (e.g. 1234)");
            builder.setView(input);
            
            builder.setPositiveButton("Join", (dialog, which) -> {
                String code = input.getText().toString().trim();
                if (!code.isEmpty()) {
                    gameSurfaceView.joinMultiplayerRoom(code);
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }
    
    public void showChatInputDialog() {
        runOnUiThread(() -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Send Chat Message");
            
            final android.widget.EditText input = new android.widget.EditText(this);
            input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            input.setHint("Type message...");
            builder.setView(input);
            
            builder.setPositiveButton("Send", (dialog, which) -> {
                String msg = input.getText().toString().trim();
                if (!msg.isEmpty()) {
                    gameSurfaceView.sendChatMessage(msg);
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }
}
