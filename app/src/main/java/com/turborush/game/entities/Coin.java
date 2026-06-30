package com.turborush.game.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class Coin {
    public float x, y;
    public float radius = 16f; // 32dp diameter
    public float hitboxRadius = 48f; // Generous hitbox
    public boolean active = true;
    public boolean collected = false;

    private static Paint pOuter;
    private static Paint pOutline;
    private static Paint pShine;
    private static Path starPath;

    public Coin() {
        initPaints();
    }

    public Coin(float x, float y) {
        this.x = x;
        this.y = y;
        initPaints();
    }

    public void spawn(float x, float y) {
        this.x = x;
        this.y = y;
        this.active = true;
        this.collected = false;
    }

    private void initPaints() {
        if (pOuter == null) {
            pOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
            pOuter.setColor(Color.parseColor("#FFD700"));
            pOuter.setStyle(Paint.Style.FILL);

            pOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
            pOutline.setColor(Color.parseColor("#E65100"));
            pOutline.setStyle(Paint.Style.STROKE);
            pOutline.setStrokeWidth(3f);

            pShine = new Paint(Paint.ANTI_ALIAS_FLAG);
            pShine.setColor(Color.parseColor("#FFF176"));
            pShine.setStyle(Paint.Style.FILL);

            starPath = new Path();
            createStar(starPath, 0, 0, 8f, 4f, 5);
        }
    }

    public void update(float scrollSpeed, float dt) {
        y += scrollSpeed * dt;
    }

    public void draw(Canvas canvas, float animTimer, float roadLeft, float roadRight) {
        if (!active || collected) return;

        if (x < roadLeft || x > roadRight) return;

        canvas.save();
        canvas.translate(x, y);

        // Rotating animation effect
        float scale = 0.8f + 0.2f * (float) Math.sin(animTimer * 5f);
        canvas.scale(scale, 1f);

        canvas.drawCircle(0, 0, radius, pOuter);
        canvas.drawCircle(0, 0, radius, pOutline);

        canvas.drawPath(starPath, pShine);

        // Shine dot
        canvas.drawCircle(-radius * 0.3f, -radius * 0.3f, 3f, pShine);

        canvas.restore();
    }

    private void createStar(Path path, float cx, float cy, float outerRadius, float innerRadius, int numRays) {
        float rot = (float) Math.PI / 2 * 3;
        float step = (float) Math.PI / numRays;

        path.moveTo(cx, cy - outerRadius);
        for (int i = 0; i < numRays; i++) {
            path.lineTo(cx + (float) Math.cos(rot) * outerRadius, cy + (float) Math.sin(rot) * outerRadius);
            rot += step;
            path.lineTo(cx + (float) Math.cos(rot) * innerRadius, cy + (float) Math.sin(rot) * innerRadius);
            rot += step;
        }
        path.lineTo(cx, cy - outerRadius);
        path.close();
    }
}
