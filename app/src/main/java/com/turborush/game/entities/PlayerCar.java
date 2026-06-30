package com.turborush.game.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Path;
import android.graphics.RectF;

import com.turborush.game.models.Vehicle;

public class PlayerCar {

    private static final float LERP_SPEED = 8f;
    private static final float SLIP_DURATION = 1.8f;

    public float width = 80f;
    public float height = 150f;
    public float x, y;
    
    public float currentX;
    public float targetX;
    public float lerpSpeed = 8f;
    public float fuel = 100f;
    public float fuelDrainRate = 1.0f;
    public Vehicle vehicleStats;

    private boolean steeringLeft  = false;
    private boolean steeringRight = false;
    private float   currentTiltX  = 0f;

    public boolean isSlipping       = false;
    private float  slipTimer        = 0f;
    private float  slipVelX         = 0f;

    public float collisionCooldown  = 0f;

    private int   bodyColor   = Color.parseColor("#E53935");
    private int   vehicleId   = 0;

    private float roadLeft, roadRight, roadWidth;

    private final Paint pBody   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pWindow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pDetail = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pWheel  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pLight  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pShadow = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PlayerCar() {
        pBody.setStyle(Paint.Style.FILL);
        pWindow.setStyle(Paint.Style.FILL);
        pDetail.setStyle(Paint.Style.FILL);
        pWheel.setColor(Color.parseColor("#1A1A1A"));
        pWheel.setStyle(Paint.Style.FILL);
        pLight.setStyle(Paint.Style.FILL);
        pShadow.setColor(Color.parseColor("#55000000"));
        pShadow.setStyle(Paint.Style.FILL);
    }

    public void init(float screenW, float screenH, float rLeft, float rRight) {
        this.roadLeft = rLeft;
        this.roadRight = rRight;
        this.roadWidth = rRight - rLeft;
        
        y = screenH * 0.8f;
        
        currentX = rLeft + roadWidth / 2f;
        targetX = currentX;
        x = currentX;
        
        isSlipping = false;
        slipTimer = 0f;
        collisionCooldown = 0f;
        
        setVehicleType(vehicleId);
    }

    public void setVehicleType(int vId) {
        this.vehicleId = vId;
        switch(vId) {
            case Vehicle.TYPE_SPORTS_CAR: width=56f; height=110f; break;
            case Vehicle.TYPE_MUSCLE_CAR: width=64f; height=110f; break;
            case Vehicle.TYPE_SUV:        width=70f; height=125f; break;
            case Vehicle.TYPE_TRUCK:      width=80f; height=160f; break;
            case Vehicle.TYPE_MOTORCYCLE: width=28f; height=90f;  break;
            case Vehicle.TYPE_HYPERCAR:   width=60f; height=115f; break;
            default: width=56f; height=100f; break;
        }
    }

    public void setBodyColor(int c) { this.bodyColor = c; }
    
    public void setSteeringLeft(boolean b)  { this.steeringLeft = b; }
    public void setSteeringRight(boolean b) { this.steeringRight = b; }
    public void setTilt(float tx) { this.currentTiltX = tx; }

    public void triggerSlip() {
        isSlipping = true;
        slipTimer = SLIP_DURATION;
        slipVelX = (Math.random() < 0.5 ? -1 : 1) * (150f + (float)Math.random() * 100f);
        collisionCooldown = 1.0f;
    }

    public void triggerTapLeft() {
        if (!isSlipping) {
            targetX -= (roadWidth * 0.28f);
            clampTarget();
        }
    }

    public void triggerTapRight() {
        if (!isSlipping) {
            targetX += (roadWidth * 0.28f);
            clampTarget();
        }
    }

    private void clampTarget() {
        float minX = roadLeft + width / 2f;
        float maxX = roadRight - width / 2f;
        if (targetX < minX) targetX = minX;
        if (targetX > maxX) targetX = maxX;
    }

    public void update(float dt, float speed) {
        if (collisionCooldown > 0) collisionCooldown -= dt;

        if (isSlipping) {
            slipTimer -= dt;
            targetX += slipVelX * dt;
            slipVelX *= (float)Math.pow(0.85, dt * 60f); // friction
            if (slipTimer <= 0) isSlipping = false;
            clampTarget();
        } else {
            // Touch steering
            if (steeringLeft) targetX -= (roadWidth * 0.7f) * dt;
            if (steeringRight) targetX += (roadWidth * 0.7f) * dt;
            
            // Gyro handling
            if (Math.abs(currentTiltX) >= 0.5f) {
                targetX += (-currentTiltX) * 120f * dt;
            }
            clampTarget();
        }

        // LERP target
        currentX += (targetX - currentX) * lerpSpeed * dt;
        x = currentX;
        fuel -= fuelDrainRate * dt;
        if (fuel < 0) fuel = 0;
    }

    public RectF getCollisionRect() {
        float hw = width / 2f * 0.85f;
        float hh = height / 2f * 0.85f;
        return new RectF(x - hw, y - hh, x + hw, y + hh);
    }

    public boolean canCollide() { return collisionCooldown <= 0; }

    public void drawDetailed(Canvas canvas, Paint p, boolean isNightMode) {
        float cx = x;
        float cy = y;
        float carW = width;
        float carH = height;

        RectF bodyRect = new RectF(cx - carW / 2, cy - carH / 2, cx + carW / 2, cy + carH / 2);

        // Layer 1 - Drop shadow
        p.setColor(Color.parseColor("#66000000"));
        canvas.drawOval(new RectF(cx - carW/2 - 2, cy - carH/2 + 4, cx + carW/2 + 4, cy + carH/2 + 8), p);

        // Layer 2 - Main body
        p.setColor(bodyColor);
        canvas.drawRoundRect(bodyRect, 12, 12, p);

        // Layer 3 - Hood
        p.setColor(darkenColor(bodyColor, 0.85f));
        canvas.drawRoundRect(new RectF(cx - carW*0.4f, cy - carH/2, cx + carW*0.4f, cy - carH*0.22f), 8, 8, p);

        // Layer 4 - Windshield (Front)
        p.setColor(Color.parseColor("#D91A237E")); // 85% alpha
        p.setStyle(Paint.Style.FILL);
        RectF fWind = new RectF(cx - carW*0.325f, cy - carH*0.22f, cx + carW*0.325f, cy - carH*0.04f);
        canvas.drawRoundRect(fWind, 6, 6, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3f);
        p.setColor(Color.WHITE);
        canvas.drawRoundRect(fWind, 6, 6, p);
        p.setStyle(Paint.Style.FILL);

        // Layer 5 - Roof
        p.setColor(darkenColor(bodyColor, 0.75f));
        canvas.drawRoundRect(new RectF(cx - carW*0.275f, cy - carH*0.08f, cx + carW*0.275f, cy + carH*0.14f), 5, 5, p);

        // Layer 6 - Rear Windshield
        p.setColor(Color.parseColor("#D91A237E"));
        RectF rWind = new RectF(cx - carW*0.3f, cy + carH*0.22f, cx + carW*0.3f, cy + carH*0.40f);
        canvas.drawRoundRect(rWind, 6, 6, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.WHITE);
        canvas.drawRoundRect(rWind, 6, 6, p);
        p.setStyle(Paint.Style.FILL);

        // Layer 7 - Headlights
        p.setColor(Color.parseColor("#FFFDE7"));
        p.setShadowLayer(8f, 0, 0, Color.parseColor("#FFFF88"));
        canvas.drawRoundRect(new RectF(cx - carW/2 + 4, cy - carH/2 + 4, cx - carW/2 + 28, cy - carH/2 + 16), 4, 4, p);
        canvas.drawRoundRect(new RectF(cx + carW/2 - 28, cy - carH/2 + 4, cx + carW/2 - 4, cy - carH/2 + 16), 4, 4, p);
        p.clearShadowLayer();

        // Layer 8 - Taillights
        p.setColor(Color.parseColor("#FF1744"));
        canvas.drawRoundRect(new RectF(cx - carW/2 + 6, cy + carH/2 - 10, cx - carW/2 + 26, cy + carH/2 - 4), 2, 2, p);
        canvas.drawRoundRect(new RectF(cx + carW/2 - 26, cy + carH/2 - 10, cx + carW/2 - 6, cy + carH/2 - 4), 2, 2, p);

        // Layer 9 - Side mirrors
        p.setColor(darkenColor(bodyColor, 0.8f));
        canvas.drawRect(cx - carW/2 - 16, cy - carH*0.15f, cx - carW/2, cy - carH*0.15f + 8, p);
        canvas.drawRect(cx + carW/2, cy - carH*0.15f, cx + carW/2 + 16, cy - carH*0.15f + 8, p);

        // Layer 10 - Racing stripe
        p.setColor(Color.parseColor("#66FFFFFF")); // 40% alpha
        canvas.drawRect(cx - 4, cy - carH/2, cx + 4, cy + carH/2, p);

        // Layer 11 - Wheel arches
        p.setColor(Color.parseColor("#212121"));
        canvas.drawCircle(cx - carW/2, cy - carH*0.35f, 8, p); // FL
        canvas.drawCircle(cx + carW/2, cy - carH*0.35f, 8, p); // FR
        canvas.drawCircle(cx - carW/2, cy + carH*0.35f, 8, p); // RL
        canvas.drawCircle(cx + carW/2, cy + carH*0.35f, 8, p); // RR

        // Layer 12 - Night Mode Headlight Beams
        if (isNightMode) {
            Paint beamP = new Paint(Paint.ANTI_ALIAS_FLAG);
            beamP.setShader(new android.graphics.LinearGradient(
                0, cy - carH/2 + 16, 0, cy - carH * 2.5f, 
                Color.parseColor("#99FFFFCC"), Color.TRANSPARENT, android.graphics.Shader.TileMode.CLAMP));
            
            Path leftBeam = new Path();
            leftBeam.moveTo(cx - carW/2 + 4, cy - carH/2 + 10);
            leftBeam.lineTo(cx - carW/2 + 28, cy - carH/2 + 10);
            leftBeam.lineTo(cx - carW/2 + 80, cy - carH * 2.5f);
            leftBeam.lineTo(cx - carW/2 - 80, cy - carH * 2.5f);
            leftBeam.close();
            canvas.drawPath(leftBeam, beamP);
            
            Path rightBeam = new Path();
            rightBeam.moveTo(cx + carW/2 - 28, cy - carH/2 + 10);
            rightBeam.lineTo(cx + carW/2 - 4, cy - carH/2 + 10);
            rightBeam.lineTo(cx + carW/2 + 80, cy - carH * 2.5f);
            rightBeam.lineTo(cx + carW/2 - 80, cy - carH * 2.5f);
            rightBeam.close();
            canvas.drawPath(rightBeam, beamP);
        }
    }
    
    private int darkenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.max(0, (int)(Color.red(color) * factor));
        int g = Math.max(0, (int)(Color.green(color) * factor));
        int b = Math.max(0, (int)(Color.blue(color) * factor));
        return Color.argb(a, r, g, b);
    }

    public void draw(Canvas canvas) {
        if (collisionCooldown > 0 && ((int)(collisionCooldown * 15)) % 2 == 0) return;

        canvas.save();
        
        float tiltAngle = (targetX - currentX) * 0.15f;
        if (tiltAngle < -18f) tiltAngle = -18f;
        if (tiltAngle > 18f) tiltAngle = 18f;
        
        canvas.translate(x, y);
        canvas.rotate(tiltAngle);

        float hw = width / 2f;
        float hh = height / 2f;

        // Shadow
        canvas.drawOval(new RectF(-hw - 8, -hh + 10, hw + 8, hh + 20), pShadow);

        pBody.setColor(bodyColor);

        switch (vehicleId) {
            case Vehicle.TYPE_SPORTS_CAR:
                pWindow.setColor(Color.parseColor("#111111"));
                canvas.drawRoundRect(new RectF(-hw, -hh, hw, hh), 16f, 16f, pBody);
                canvas.drawRoundRect(new RectF(-hw+6, -hh+20, hw-6, -hh+40), 6f, 6f, pWindow); // Windshield
                canvas.drawRoundRect(new RectF(-hw+8, hh-30, hw-8, hh-10), 4f, 4f, pWindow);   // Rear window
                // Stripes
                pDetail.setColor(Color.WHITE);
                canvas.drawRect(-8, -hh, -4, hh, pDetail);
                canvas.drawRect(4, -hh, 8, hh, pDetail);
                // Headlights
                pLight.setColor(Color.WHITE);
                canvas.drawOval(new RectF(-hw+4, -hh+4, -hw+14, -hh+12), pLight);
                canvas.drawOval(new RectF(hw-14, -hh+4, hw-4, -hh+12), pLight);
                break;
                
            case Vehicle.TYPE_MUSCLE_CAR:
                pWindow.setColor(Color.parseColor("#222222"));
                canvas.drawRect(new RectF(-hw, -hh, hw, hh), pBody);
                canvas.drawRect(new RectF(-hw+8, -hh+25, hw-8, -hh+45), pWindow); 
                pDetail.setColor(Color.BLACK);
                canvas.drawOval(new RectF(-12, -hh+10, 12, -hh+22), pDetail); // Hood scoop
                pLight.setColor(Color.parseColor("#FFFFDD"));
                canvas.drawCircle(-hw+8, -hh+8, 6, pLight);
                canvas.drawCircle(hw-8, -hh+8, 6, pLight);
                break;
                
            case Vehicle.TYPE_SUV:
                pWindow.setColor(Color.parseColor("#0A0A0A"));
                canvas.drawRoundRect(new RectF(-hw, -hh, hw, hh), 8f, 8f, pBody);
                canvas.drawRect(new RectF(-hw+4, -hh+30, hw-4, hh-20), pWindow); 
                canvas.drawRect(new RectF(-hw+10, -hh+35, hw-10, hh-25), pBody); // Roof
                pDetail.setColor(Color.GRAY);
                canvas.drawRect(-hw, -hh-2, hw, -hh+4, pDetail); // Bull bar
                pLight.setColor(Color.WHITE);
                canvas.drawRect(-hw+4, -hh+4, -hw+16, -hh+12, pLight);
                canvas.drawRect(hw-16, -hh+4, hw-4, -hh+12, pLight);
                break;
                
            case Vehicle.TYPE_TRUCK:
                pWindow.setColor(Color.parseColor("#151515"));
                canvas.drawRect(new RectF(-hw, -hh, hw, -hh+60), pBody); // Cab
                pDetail.setColor(Color.parseColor("#2C3E50"));
                canvas.drawRect(new RectF(-hw, -hh+62, hw, hh), pDetail); // Bed
                canvas.drawRect(new RectF(-hw+6, -hh+20, hw-6, -hh+40), pWindow);
                pLight.setColor(Color.parseColor("#FFFFAA"));
                canvas.drawRect(-hw+2, -hh+2, -hw+18, -hh+14, pLight);
                canvas.drawRect(hw-18, -hh+2, hw-2, -hh+14, pLight);
                break;
                
            case Vehicle.TYPE_MOTORCYCLE:
                pWindow.setColor(Color.BLACK);
                canvas.drawOval(new RectF(-hw, -hh+10, hw, hh-10), pBody);
                canvas.drawCircle(0, 0, hw-2, pWindow); // Helmet
                pLight.setColor(Color.WHITE);
                canvas.drawCircle(0, -hh+5, 4, pLight);
                break;
                
            case Vehicle.TYPE_HYPERCAR:
                pWindow.setColor(Color.parseColor("#050505"));
                Path path = new Path();
                path.moveTo(0, -hh);
                path.lineTo(hw, -hh+30);
                path.lineTo(hw-4, hh);
                path.lineTo(-hw+4, hh);
                path.lineTo(-hw, -hh+30);
                path.close();
                canvas.drawPath(path, pBody);
                canvas.drawOval(new RectF(-hw+10, -hh+30, hw-10, hh-20), pWindow);
                pLight.setColor(Color.parseColor("#00FFFF"));
                canvas.drawRect(-hw+6, hh-4, hw-6, hh, pLight); // Underglow tail
                break;

            default:
                canvas.drawRoundRect(new RectF(-hw, -hh, hw, hh), 10f, 10f, pBody);
                break;
        }

        canvas.restore();
    }
}
