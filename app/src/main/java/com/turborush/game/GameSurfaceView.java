package com.turborush.game;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.util.AttributeSet;

/**
 * GameSurfaceView — Core SurfaceView that hosts the game loop, rendering,
 * and touch input dispatching. Acts as the bridge between the Android View
 * system and the game engine.
 */
public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private GameEngine gameEngine;
    private boolean surfaceReady = false;

    public GameSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);
        gameEngine   = new GameEngine(context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceReady = true;
        gameThread = new GameThread(holder, gameEngine);
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        gameEngine.onSurfaceChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
        stopThread();
    }

    private void stopThread() {
        if (gameThread != null) {
            gameThread.setRunning(false);
            boolean retry = true;
            while (retry) {
                try { gameThread.join(); retry = false; }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            gameThread = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameEngine != null) {
            gameEngine.handleTouchEvent(event);
        }
        return true;
    }
    
    public void setTilt(float tiltX) {
        if (gameEngine != null) {
            gameEngine.setTilt(tiltX);
        }
    }

    public boolean handleBackButton() {
        if (gameEngine != null) {
            return gameEngine.handleBackButton();
        }
        return false;
    }

    public void onResume() {
        if (surfaceReady && gameThread == null) {
            gameThread = new GameThread(getHolder(), gameEngine);
            gameThread.setRunning(true);
            gameThread.start();
        }
        gameEngine.onResume();
    }

    public void onPause() {
        gameEngine.onPause();
        stopThread();
    }

    public void onDestroy() {
        gameEngine.onDestroy();
    }

    public void onBackPressed() {
        gameEngine.pauseGame();
    }

    public void setCustomAvatar(android.net.Uri uri) {
        if (gameEngine != null) {
            gameEngine.setCustomAvatar(uri);
        }
    }
    
    public void startGoogleSignIn() {
        if (getContext() instanceof MainActivity) {
            ((MainActivity) getContext()).startGoogleSignIn();
        }
    }
    
    public void showEmailLoginDialog() {
        if (getContext() instanceof MainActivity) {
            ((MainActivity) getContext()).showEmailLoginDialog();
        }
    }
    
    public void signOut() {
        if (getContext() instanceof MainActivity) {
            ((MainActivity) getContext()).signOut();
        }
    }
    
    public GameEngine getGameEngine() {
        return gameEngine;
    }
    
    public void joinMultiplayerRoom(String code) {
        if (gameEngine != null) {
            gameEngine.joinMultiplayerRoom(code);
        }
    }
    
    public void sendChatMessage(String msg) {
        if (gameEngine != null) {
            gameEngine.sendChatMessage(msg);
        }
    }
}
