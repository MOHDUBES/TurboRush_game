package com.turborush.game;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * GameThread — The dedicated background thread that drives the game loop.
 * Implements a fixed-timestep loop targeting 60 FPS with elapsed time capping.
 */
public class GameThread extends Thread {

    private static final int TARGET_FPS = 60;
    private static final long TARGET_FRAME_TIME_NS = 1_000_000_000L / TARGET_FPS;
    private static final float MAX_DELTA_SECONDS = 0.05f;

    private final SurfaceHolder surfaceHolder;
    private final GameEngine gameEngine;
    private volatile boolean running = false;

    private long frameCount = 0;
    private long fpsTimer   = 0;
    private int  currentFps = 60;

    public GameThread(SurfaceHolder surfaceHolder, GameEngine gameEngine) {
        super("GameThread");
        this.surfaceHolder = surfaceHolder;
        this.gameEngine    = gameEngine;
        setPriority(Thread.MAX_PRIORITY);
    }

    public void setRunning(boolean running) { this.running = running; }
    public int getCurrentFps()             { return currentFps; }

    @Override
    public void run() {
        long previousTime = System.nanoTime();
        while (running) {
            long currentTime = System.nanoTime();
            long elapsed     = currentTime - previousTime;
            previousTime     = currentTime;

            if (elapsed > (long)(MAX_DELTA_SECONDS * 1_000_000_000L)) {
                elapsed = (long)(MAX_DELTA_SECONDS * 1_000_000_000L);
            }
            float deltaSeconds = elapsed / 1_000_000_000f;

            // Update game logic
            gameEngine.update(deltaSeconds);

            // Render
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas(null);
                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        gameEngine.draw(canvas);
                    }
                }
            } finally {
                if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas);
            }

            // FPS measurement
            frameCount++;
            fpsTimer += elapsed;
            if (fpsTimer >= 1_000_000_000L) {
                currentFps = (int) frameCount;
                frameCount = 0;
                fpsTimer   = 0;
            }

            // Sleep to cap FPS
            long frameTime  = System.nanoTime() - currentTime;
            long sleepTime  = (TARGET_FRAME_TIME_NS - frameTime) / 1_000_000L;
            if (sleepTime > 0) {
                try { Thread.sleep(sleepTime); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }
}
