package com.turborush.game.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.Random;

public class AICar {
    public float x, y, width, height;
    public int lane;
    public boolean isActive = false;
    public Direction direction;
    public Type type;
    public int color;

    private Paint pBody;

    public enum Direction {
        ONCOMING, SAME_LANE
    }

    public enum Type {
        SEDAN, TRUCK, MOTORCYCLE
    }

    private static final int[] COLORS = {
        Color.parseColor("#E53935"), // Red
        Color.parseColor("#1E88E5"), // Blue
        Color.parseColor("#43A047"), // Green
        Color.parseColor("#8E24AA"), // Purple
        Color.parseColor("#FDD835"), // Yellow
        Color.parseColor("#757575"), // Grey
        Color.parseColor("#000000"), // Black
        Color.parseColor("#FFFFFF")  // White
    };

    public AICar() {
        pBody = new Paint(Paint.ANTI_ALIAS_FLAG);
        pBody.setStyle(Paint.Style.FILL);
    }

    public AICar(float x, float y, int lane, Direction direction) {
        this();
        spawn(x, y, lane, direction);
    }

    public void spawn(float x, float y, int lane, Direction direction) {
        this.x = x;
        this.y = y;
        this.lane = lane;
        this.direction = direction;
        this.isActive = true;

        Random r = new Random();
        this.color = COLORS[r.nextInt(COLORS.length)];

        int t = r.nextInt(100);
        if (t < 50) {
            type = Type.SEDAN;
            width = 80f; height = 150f;
        } else if (t < 80) {
            type = Type.TRUCK;
            width = 90f; height = 190f;
        } else {
            type = Type.MOTORCYCLE;
            width = 40f; height = 100f;
        }
    }

    public void update(float dt, float roadScrollSpeed) {
        if (direction == Direction.ONCOMING) {
            y += (roadScrollSpeed + (roadScrollSpeed * 0.5f)) * dt;
        } else {
            // Overtaking player from behind (moving up)
            y -= (roadScrollSpeed * 0.25f) * dt; 
        }
    }

    public RectF getCollisionRect() {
        return new RectF(x - width/2, y - height/2, x + width/2, y + height/2);
    }

    public void draw(Canvas canvas) {
        if (!isActive) return;
        
        canvas.save();
        if (direction == Direction.SAME_LANE) {
            canvas.translate(x, y);
            canvas.rotate(180f);
            canvas.translate(-x, -y);
        }

        switch (type) {
            case SEDAN: drawSedan(canvas); break;
            case TRUCK: drawTruck(canvas); break;
            case MOTORCYCLE: drawMotorcycle(canvas); break;
        }

        canvas.restore();
    }

    private void drawSedan(Canvas canvas) {
        float cx = x; float cy = y;
        float carW = width; float carH = height;

        // Shadow
        pBody.setColor(Color.parseColor("#66000000"));
        canvas.drawOval(new RectF(cx - carW/2 - 2, cy - carH/2 + 4, cx + carW/2 + 4, cy + carH/2 + 8), pBody);

        // Main body
        pBody.setColor(color);
        canvas.drawRoundRect(new RectF(cx - carW / 2, cy - carH / 2, cx + carW / 2, cy + carH / 2), 12, 12, pBody);

        // Hood
        pBody.setColor(darkenColor(color, 0.85f));
        canvas.drawRoundRect(new RectF(cx - carW*0.4f, cy - carH/2, cx + carW*0.4f, cy - carH*0.22f), 8, 8, pBody);

        // Windshield (Front)
        pBody.setColor(Color.parseColor("#D91A237E")); // 85% alpha
        pBody.setStyle(Paint.Style.FILL);
        RectF fWind = new RectF(cx - carW*0.325f, cy - carH*0.22f, cx + carW*0.325f, cy - carH*0.04f);
        canvas.drawRoundRect(fWind, 6, 6, pBody);

        // Roof
        pBody.setColor(darkenColor(color, 0.75f));
        canvas.drawRoundRect(new RectF(cx - carW*0.275f, cy - carH*0.08f, cx + carW*0.275f, cy + carH*0.14f), 5, 5, pBody);

        // Rear Windshield
        pBody.setColor(Color.parseColor("#D91A237E"));
        RectF rWind = new RectF(cx - carW*0.3f, cy + carH*0.22f, cx + carW*0.3f, cy + carH*0.40f);
        canvas.drawRoundRect(rWind, 6, 6, pBody);

        // Headlights
        pBody.setColor(Color.parseColor("#FFFDE7"));
        pBody.setShadowLayer(8f, 0, 0, Color.parseColor("#FFFF88"));
        canvas.drawRoundRect(new RectF(cx - carW/2 + 4, cy - carH/2 + 4, cx - carW/2 + 28, cy - carH/2 + 16), 4, 4, pBody);
        canvas.drawRoundRect(new RectF(cx + carW/2 - 28, cy - carH/2 + 4, cx + carW/2 - 4, cy - carH/2 + 16), 4, 4, pBody);
        pBody.clearShadowLayer();

        // Taillights
        pBody.setColor(Color.parseColor("#FF1744"));
        canvas.drawRoundRect(new RectF(cx - carW/2 + 6, cy + carH/2 - 10, cx - carW/2 + 26, cy + carH/2 - 4), 2, 2, pBody);
        canvas.drawRoundRect(new RectF(cx + carW/2 - 26, cy + carH/2 - 10, cx + carW/2 - 6, cy + carH/2 - 4), 2, 2, pBody);

        // Side mirrors
        pBody.setColor(darkenColor(color, 0.8f));
        canvas.drawRect(cx - carW/2 - 12, cy - carH*0.15f, cx - carW/2, cy - carH*0.15f + 8, pBody);
        canvas.drawRect(cx + carW/2, cy - carH*0.15f, cx + carW/2 + 12, cy - carH*0.15f + 8, pBody);

        // Door seam
        pBody.setColor(Color.parseColor("#44000000"));
        canvas.drawRect(cx - carW/2, cy, cx + carW/2, cy + 2, pBody);
    }

    private void drawTruck(Canvas canvas) {
        float cx = x; float cy = y;
        float w = width; float h = height;

        // Shadow
        pBody.setColor(Color.parseColor("#66000000"));
        canvas.drawRect(new RectF(cx - w/2 - 4, cy - h/2 + 4, cx + w/2 + 8, cy + h/2 + 8), pBody);

        // Cab
        pBody.setColor(color);
        canvas.drawRoundRect(new RectF(cx - w/2, cy - h/2, cx + w/2, cy + h*0.05f), 10, 10, pBody);

        // Cab Windshield
        pBody.setColor(Color.parseColor("#D91A237E"));
        canvas.drawRoundRect(new RectF(cx - w*0.4f, cy - h*0.15f, cx + w*0.4f, cy - h*0.02f), 4, 4, pBody);

        // Cargo Bed
        pBody.setColor(Color.parseColor("#424242"));
        canvas.drawRect(new RectF(cx - w/2 + 2, cy + h*0.05f, cx + w/2 - 2, cy + h/2), pBody);
        pBody.setColor(Color.parseColor("#212121"));
        pBody.setStyle(Paint.Style.STROKE);
        pBody.setStrokeWidth(3f);
        canvas.drawRect(new RectF(cx - w/2 + 2, cy + h*0.05f, cx + w/2 - 2, cy + h/2), pBody);
        pBody.setStyle(Paint.Style.FILL);

        // Cab Roof
        pBody.setColor(darkenColor(color, 0.7f));
        canvas.drawRoundRect(new RectF(cx - w*0.35f, cy - h*0.3f, cx + w*0.35f, cy - h*0.18f), 6, 6, pBody);

        // Truck Mirrors (Large)
        pBody.setColor(Color.parseColor("#212121"));
        canvas.drawRect(cx - w/2 - 16, cy - h*0.25f, cx - w/2, cy - h*0.25f + 16, pBody);
        canvas.drawRect(cx + w/2, cy - h*0.25f, cx + w/2 + 16, cy - h*0.25f + 16, pBody);
        
        // Headlights
        pBody.setColor(Color.parseColor("#FFFDE7"));
        canvas.drawRoundRect(new RectF(cx - w/2 + 4, cy - h/2 + 4, cx - w/2 + 20, cy - h/2 + 12), 2, 2, pBody);
        canvas.drawRoundRect(new RectF(cx + w/2 - 20, cy - h/2 + 4, cx + w/2 - 4, cy - h/2 + 12), 2, 2, pBody);
    }

    private void drawMotorcycle(Canvas canvas) {
        float cx = x; float cy = y;
        float w = width; float h = height;

        // Shadow
        pBody.setColor(Color.parseColor("#66000000"));
        canvas.drawOval(new RectF(cx - w/2 - 2, cy - h/2 + 4, cx + w/2 + 4, cy + h/2 + 8), pBody);

        // Wheels
        pBody.setColor(Color.parseColor("#212121"));
        canvas.drawRoundRect(new RectF(cx - w*0.2f, cy - h/2, cx + w*0.2f, cy - h*0.3f), 4, 4, pBody); // Front
        canvas.drawRoundRect(new RectF(cx - w*0.2f, cy + h*0.3f, cx + w*0.2f, cy + h/2), 4, 4, pBody); // Rear

        // Body
        pBody.setColor(color);
        canvas.drawOval(new RectF(cx - w/2, cy - h*0.3f, cx + w/2, cy + h*0.3f), pBody);

        // Headlight
        pBody.setColor(Color.parseColor("#FFFDE7"));
        canvas.drawCircle(cx, cy - h*0.4f, w*0.25f, pBody);

        // Rider
        pBody.setColor(Color.parseColor("#374151"));
        canvas.drawOval(new RectF(cx - w*0.35f, cy - h*0.1f, cx + w*0.35f, cy + h*0.2f), pBody);

        // Helmet
        pBody.setColor(Color.parseColor("#1E3A5F"));
        canvas.drawCircle(cx, cy + h*0.05f, w*0.3f, pBody);
    }

    private int darkenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.max(0, (int)(Color.red(color) * factor));
        int g = Math.max(0, (int)(Color.green(color) * factor));
        int b = Math.max(0, (int)(Color.blue(color) * factor));
        return Color.argb(a, r, g, b);
    }
}
