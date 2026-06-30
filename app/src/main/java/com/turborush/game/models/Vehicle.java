package com.turborush.game.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

public class Vehicle {

    public static final int TYPE_SEDAN      = 0;
    public static final int TYPE_SPORTS_CAR = 1;
    public static final int TYPE_TRUCK      = 2;
    public static final int TYPE_SUPERCAR   = 3;
    public static final int TYPE_MUSCLE_CAR = 4;
    public static final int TYPE_SUV        = 5;
    public static final int TYPE_MOTORCYCLE = 6;
    public static final int TYPE_HYPERCAR   = 7;

    public static final int[] COLOR_PALETTE = {
            Color.parseColor("#E53935"), // Red
            Color.parseColor("#1E88E5"), // Blue
            Color.parseColor("#43A047"), // Green
            Color.parseColor("#FDD835"), // Yellow
            Color.parseColor("#8E24AA"), // Purple
            Color.parseColor("#F4511E"), // Orange
            Color.parseColor("#757575"), // Silver
            Color.parseColor("#212121")  // Black
    };

    public int id;
    public String name;
    public int vehicleType;
    public String classTier;
    public int unlockCost;
    
    // Core stats (out of 10)
    public int speed;
    public int handling;
    public int fuel;
    public int durability;

    public int selectedColorIndex = 0;

    private transient Paint pBody, pDetail, pWindow, pWheel, pLight;

    public Vehicle(int id, String name, int vehicleType, String classTier, int unlockCost, int speed, int handling, int fuel, int durability) {
        this.id = id;
        this.name = name;
        this.vehicleType = vehicleType;
        this.classTier = classTier;
        this.unlockCost = unlockCost;
        this.speed = speed;
        this.handling = handling;
        this.fuel = fuel;
        this.durability = durability;
        initPaints();
    }

    private void initPaints() {
        if (pBody == null) {
            pBody = new Paint(Paint.ANTI_ALIAS_FLAG); pBody.setStyle(Paint.Style.FILL);
            pDetail = new Paint(Paint.ANTI_ALIAS_FLAG); pDetail.setStyle(Paint.Style.FILL);
            pWindow = new Paint(Paint.ANTI_ALIAS_FLAG); pWindow.setStyle(Paint.Style.FILL);
            pWheel = new Paint(Paint.ANTI_ALIAS_FLAG); pWheel.setStyle(Paint.Style.FILL); pWheel.setColor(Color.parseColor("#1A1A1A"));
            pLight = new Paint(Paint.ANTI_ALIAS_FLAG); pLight.setStyle(Paint.Style.FILL);
        }
    }

    public int getSelectedColor() {
        return COLOR_PALETTE[Math.max(0, Math.min(selectedColorIndex, COLOR_PALETTE.length - 1))];
    }

    public void drawPreview(Canvas canvas, float cx, float cy, float scale, int color) {
        if (pBody == null) initPaints();
        canvas.save();
        canvas.translate(cx, cy);
        canvas.scale(scale, scale);

        switch (id) { 
            case 0: drawViperX(canvas, color); break;
            case 1: drawThunderV8(canvas, color); break;
            case 2: drawRoadKing(canvas, color); break;
            case 3: drawTitanHauler(canvas, color); break;
            case 4: drawPhantomR(canvas, color); break;
            case 5: drawGhostS(canvas, color); break;
            default: drawViperX(canvas, color); break;
        }
        canvas.restore();
    }

    // VEHICLE 1: Sports Car "Viper X"
    private void drawViperX(Canvas canvas, int color) {
        float hw = 28f, hh = 50f;
        pBody.setColor(color);
        pBody.setShader(new LinearGradient(-hw,-hh,hw,hh, lighten(color,0.3f), darken(color,0.2f), Shader.TileMode.CLAMP));
        
        Path body = new Path();
        body.moveTo(-hw+6,-hh+10); body.lineTo(hw-6,-hh+10);
        body.lineTo(hw,hh-10); body.lineTo(hw-8,hh); body.lineTo(-hw+8,hh); body.lineTo(-hw,hh-10); body.close();
        canvas.drawPath(body, pBody); pBody.setShader(null);
        
        pDetail.setColor(Color.WHITE); pDetail.setAlpha(150);
        canvas.drawRect(-3, -hh+10, 3, hh, pDetail); pDetail.setAlpha(255);
        
        pWindow.setColor(Color.parseColor("#2C3E50"));
        canvas.drawRoundRect(new RectF(-hw+8,-hh+25,hw-8,-hh+45), 4, 4, pWindow);
        canvas.drawRect(-hw+6,-hh+45,hw-6,-hh+75, pWindow);
        
        pLight.setColor(Color.parseColor("#E0F7FA"));
        canvas.drawOval(new RectF(-hw+4,-hh+12,-hw+14,-hh+20), pLight);
        canvas.drawOval(new RectF(hw-14,-hh+12,hw-4,-hh+20), pLight);
        
        pDetail.setColor(Color.parseColor("#333333"));
        canvas.drawRect(-hw+10,hh,hw-10,hh+6, pDetail);
        pDetail.setColor(Color.LTGRAY);
        canvas.drawRect(-12,hh,-4,hh+4, pDetail); canvas.drawRect(4,hh,12,hh+4, pDetail);
        
        pDetail.setColor(darken(color,0.4f));
        canvas.drawRect(-hw,hh-8,hw,hh-4, pDetail);
        drawWheels(canvas, hw, hh);
    }

    // VEHICLE 2: Muscle Car "Thunder V8"
    private void drawThunderV8(Canvas canvas, int color) {
        float hw = 32f, hh = 48f;
        pBody.setColor(color);
        canvas.drawRoundRect(new RectF(-hw,-hh+5,hw,hh), 6,6, pBody);
        
        pDetail.setColor(darken(color, 0.2f));
        canvas.drawOval(new RectF(-hw-4,-hh+15,-hw+2,-hh+35), pDetail);
        canvas.drawOval(new RectF(hw-2,-hh+15,hw+4,-hh+35), pDetail);
        canvas.drawOval(new RectF(-hw-6,hh-30,-hw+2,hh-10), pDetail);
        canvas.drawOval(new RectF(hw-2,hh-30,hw+6,hh-10), pDetail);
        
        pDetail.setColor(Color.parseColor("#222222"));
        canvas.drawOval(new RectF(-8,-hh+12,8,-hh+28), pDetail);
        
        pWindow.setColor(Color.parseColor("#111111"));
        canvas.drawRect(-hw+6,-hh+35,hw-6,-hh+45, pWindow);
        canvas.drawRect(-hw+6,-hh+45,hw-6,-hh+60, pWindow);
        
        pLight.setColor(Color.parseColor("#EEEEEE"));
        canvas.drawRect(-hw+4,-hh+5,hw-4,-hh+8, pLight);
        canvas.drawRect(-hw+4,hh-4,hw-4,hh, pLight);
        
        pDetail.setColor(Color.parseColor("#666666"));
        canvas.drawOval(new RectF(-16,hh,-8,hh+6), pDetail);
        canvas.drawOval(new RectF(8,hh,16,hh+6), pDetail);
        drawWheels(canvas, hw+2, hh);
    }

    // VEHICLE 3: SUV "Road King"
    private void drawRoadKing(Canvas canvas, int color) {
        float hw = 30f, hh = 55f;
        pBody.setColor(color);
        canvas.drawRoundRect(new RectF(-hw,-hh,hw,hh), 10,10, pBody);
        
        pDetail.setColor(Color.DKGRAY);
        canvas.drawRect(-hw+6,-hh+10,-hw+10,hh-10, pDetail);
        canvas.drawRect(hw-10,-hh+10,hw-6,hh-10, pDetail);
        
        pWindow.setColor(Color.parseColor("#455A64"));
        canvas.drawRect(-hw+4,-hh+25,hw-4,-hh+45, pWindow);
        canvas.drawRect(-hw+4,-hh+45,hw-4,hh-20, pWindow);
        
        pLight.setColor(Color.LTGRAY);
        canvas.drawRect(-15,-hh-4,15,-hh, pLight);
        
        pLight.setColor(Color.parseColor("#FFFDE8"));
        canvas.drawRect(-hw+4,-hh+4,-hw+14,-hh+12, pLight);
        canvas.drawRect(hw-14,-hh+4,hw-4,-hh+12, pLight);
        
        drawWheels(canvas, hw+2, hh-4);
    }

    // VEHICLE 4: Truck "Titan Hauler"
    private void drawTitanHauler(Canvas canvas, int color) {
        float hw = 34f, hh = 60f;
        pDetail.setColor(Color.parseColor("#2A2A2A"));
        canvas.drawRect(-hw,-hh*0.2f,hw,hh, pDetail);
        
        pBody.setColor(color);
        canvas.drawRoundRect(new RectF(-hw,-hh,hw,-hh*0.2f), 8,8, pBody);
        
        pLight.setColor(Color.LTGRAY);
        canvas.drawRect(-12,-hh,12,-hh+6, pLight);
        pLight.setColor(Color.parseColor("#FFEE58"));
        canvas.drawRect(-hw+4,-hh+4,-hw+16,-hh+12, pLight);
        canvas.drawRect(hw-16,-hh+4,hw-4,-hh+12, pLight);
        
        pDetail.setColor(Color.parseColor("#111111"));
        canvas.drawRect(-hw-6,-hh+20,-hw,-hh+30, pDetail);
        canvas.drawRect(hw,-hh+20,hw+6,-hh+30, pDetail);
        pLight.setColor(Color.LTGRAY);
        canvas.drawOval(new RectF(-hw-6,-hh+32,-hw, -hh+40), pLight);
        
        pWindow.setColor(Color.parseColor("#37474F"));
        canvas.drawRect(-hw+6,-hh+16,hw-6,-hh+36, pWindow);
        drawWheels(canvas, hw+2, hh);
    }

    // VEHICLE 5: Motorcycle "Phantom R"
    private void drawPhantomR(Canvas canvas, int color) {
        float hw = 12f, hh = 35f;
        pBody.setColor(color);
        canvas.drawRoundRect(new RectF(-hw,-hh,hw,hh), 10,10, pBody);
        
        pDetail.setColor(Color.DKGRAY);
        canvas.drawRect(-hw-2,-hh*0.2f,hw+2,hh*0.5f, pDetail);
        
        pLight.setColor(Color.LTGRAY);
        canvas.drawRect(-hw-4,0,-hw-2,hh, pLight);
        
        pDetail.setColor(Color.BLACK);
        canvas.drawOval(new RectF(-hw+2,-hh*0.1f,hw-2,hh*0.4f), pDetail); 
        canvas.drawOval(new RectF(-hw+4,-hh*0.4f,hw-4,-hh*0.1f), pDetail); 
        
        pLight.setColor(Color.parseColor("#B3FFFFCC"));
        Path beam = new Path();
        beam.moveTo(0, -hh); beam.lineTo(-20, -hh-30); beam.lineTo(20, -hh-30); beam.close();
        canvas.drawPath(beam, pLight);
        
        canvas.drawOval(new RectF(-4,-hh-6,4,-hh+6), pWheel);
        canvas.drawOval(new RectF(-6,hh-10,6,hh+10), pWheel);
    }

    // VEHICLE 6: Hypercar "Ghost S"
    private void drawGhostS(Canvas canvas, int color) {
        float hw = 30f, hh = 52f;
        pBody.setColor(color);
        pBody.setShader(new LinearGradient(-hw,-hh,hw,hh, lighten(color,0.4f), darken(color,0.3f), Shader.TileMode.CLAMP));
        
        Path body = new Path();
        body.moveTo(0, -hh); body.lineTo(hw-4, -hh+15);
        body.lineTo(hw, hh-10); body.lineTo(hw-8, hh); body.lineTo(-hw+8, hh);
        body.lineTo(-hw, hh-10); body.lineTo(-hw+4, -hh+15); body.close();
        canvas.drawPath(body, pBody); pBody.setShader(null);
        
        pDetail.setColor(Color.BLACK);
        canvas.drawRect(-16, hh-4, 16, hh+6, pDetail);
        pLight.setColor(Color.parseColor("#FF1744"));
        canvas.drawRect(-24, hh-6, -16, hh-2, pLight);
        canvas.drawRect(16, hh-6, 24, hh-2, pLight);
        
        pDetail.setColor(darken(color, 0.5f));
        pDetail.setStyle(Paint.Style.STROKE); pDetail.setStrokeWidth(2f);
        canvas.drawLine(0, -hh+25, -hw+2, hh-20, pDetail);
        canvas.drawLine(0, -hh+25, hw-2, hh-20, pDetail);
        pDetail.setStyle(Paint.Style.FILL);
        
        pWindow.setColor(Color.BLACK);
        Path wind = new Path();
        wind.moveTo(0, -hh+18); wind.lineTo(hw-8, -hh+35); wind.lineTo(-hw+8, -hh+35); wind.close();
        canvas.drawPath(wind, pWindow);
        
        pLight.setColor(Color.parseColor("#4400E5FF"));
        canvas.drawOval(new RectF(-hw-10,-hh+10,hw+10,hh), pLight);
        
        drawWheels(canvas, hw, hh);
    }

    private void drawWheels(Canvas canvas, float hw, float hh) {
        canvas.drawRoundRect(new RectF(-hw-3,-hh+15,-hw+3,-hh+30), 2,2, pWheel);
        canvas.drawRoundRect(new RectF(hw-3,-hh+15,hw+3,-hh+30), 2,2, pWheel);
        canvas.drawRoundRect(new RectF(-hw-3,hh-30,-hw+3,hh-15), 2,2, pWheel);
        canvas.drawRoundRect(new RectF(hw-3,hh-30,hw+3,hh-15), 2,2, pWheel);
    }

    private int lighten(int c, float a) { return Color.rgb(Math.min(255,Color.red(c)+(int)(a*255)),Math.min(255,Color.green(c)+(int)(a*255)),Math.min(255,Color.blue(c)+(int)(a*255))); }
    private int darken(int c, float a)  { return Color.rgb(Math.max(0,Color.red(c)-(int)(a*255)),Math.max(0,Color.green(c)-(int)(a*255)),Math.max(0,Color.blue(c)-(int)(a*255))); }
}
