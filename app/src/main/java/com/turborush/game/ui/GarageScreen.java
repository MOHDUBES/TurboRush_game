package com.turborush.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import com.turborush.game.models.PlayerProgress;
import com.turborush.game.models.Vehicle;

import java.util.List;

public class GarageScreen {

    private float screenW, screenH, animTimer = 0f;

    public RectF btnBack   = new RectF();
    public RectF btnPrev   = new RectF();
    public RectF btnNext   = new RectF();
    public RectF btnAction = new RectF();
    public RectF[] btnColors = new RectF[8];
    
    // Confirmation dialog
    public boolean showingColorConfirm = false;
    public int pendingColorIndex = -1;
    public RectF btnConfirmYes = new RectF();
    public RectF btnConfirmNo = new RectF();

    private final Paint pBg    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCard  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pText  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBtn   = new Paint(Paint.ANTI_ALIAS_FLAG);

    public GarageScreen() {
        pBg.setStyle(Paint.Style.FILL);
        pCard.setColor(Color.parseColor("#151821")); pCard.setStyle(Paint.Style.FILL);
        pText.setStyle(Paint.Style.FILL); pText.setTextAlign(Paint.Align.CENTER);
        pTitle.setStyle(Paint.Style.FILL); pTitle.setTextAlign(Paint.Align.CENTER); pTitle.setFakeBoldText(true);
        pBtn.setStyle(Paint.Style.FILL);
        for(int i=0; i<8; i++) btnColors[i] = new RectF();
    }

    public void init(float w, float h) {
        screenW = w; screenH = h;
        btnBack.set(w*0.05f, h*0.05f, w*0.05f+h*0.07f, h*0.05f+h*0.07f);
        
        float cy = h*0.35f, bw = w*0.12f;
        btnPrev.set(w*0.02f, cy-bw/2, w*0.02f+bw, cy+bw/2);
        btnNext.set(w*0.98f-bw, cy-bw/2, w*0.98f, cy+bw/2);
        
        float aw = w*0.7f, ah = h*0.08f;
        btnAction.set(w/2f-aw/2, h*0.88f, w/2f+aw/2, h*0.88f+ah);
        
        float cw = w*0.09f, startX = w/2f - (cw*4+cw*0.3f*3)/2f;
        for(int i=0; i<8; i++) {
            float x = startX + (cw*1.3f)*(i%4);
            float y = h*0.72f + (cw*1.3f)*(i/4);
            btnColors[i].set(x, y, x+cw, y+cw);
        }
        
        float cwDialog = w * 0.35f, chDialog = h * 0.08f;
        float cyDialog = h * 0.6f;
        btnConfirmYes.set(w/2f - cwDialog - w*0.02f, cyDialog, w/2f - w*0.02f, cyDialog + chDialog);
        btnConfirmNo.set(w/2f + w*0.02f, cyDialog, w/2f + w*0.02f + cwDialog, cyDialog + chDialog);
    }

    public void update(float dt) { animTimer += dt; }

    public void draw(Canvas canvas, PlayerProgress prog, com.turborush.game.StorageManager storage, List<Vehicle> vehicles, int curIdx) {
        pBg.setShader(new LinearGradient(0,0,0,screenH, Color.parseColor("#1A1A2E"), Color.parseColor("#0F0F1A"), Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,screenW,screenH, pBg); pBg.setShader(null);
        
        pTitle.setColor(Color.WHITE); pTitle.setTextSize(screenW*0.12f);
        canvas.drawText("GARAGE", screenW/2f, screenH*0.10f, pTitle);
        
        pText.setColor(Color.parseColor("#FFD700")); pText.setTextSize(screenW*0.06f); pText.setFakeBoldText(true);
        canvas.drawText("Coins: " + prog.totalCoins, screenW/2f, screenH*0.15f, pText); pText.setFakeBoldText(false);
        
        drawBtn(canvas, btnBack, "◀", Color.parseColor("#33FFFFFF"));
        if(curIdx > 0) drawBtn(canvas, btnPrev, "◀", Color.parseColor("#22FFFFFF"));
        if(curIdx < vehicles.size()-1) drawBtn(canvas, btnNext, "▶", Color.parseColor("#22FFFFFF"));
        
        Vehicle v = vehicles.get(curIdx);
        
        // Preview Card
        float cardW = screenW * 0.9f;
        float cardH = screenH * 0.52f;
        float cx = screenW/2f, cy = screenH*0.44f;
        
        canvas.drawRoundRect(new RectF(cx-cardW/2, cy-cardH/2, cx+cardW/2, cy+cardH/2), 24, 24, pCard);
        
        // Spotlight inside card
        float carCy = cy - cardH * 0.15f;
        float gA = (float)(0.6+0.4*Math.sin(animTimer*3));
        pBg.setShader(new RadialGradient(cx, carCy, cardW*0.8f, Color.argb((int)(gA*60),255,255,255), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(cx-cardW/2, cy-cardH/2, cx+cardW/2, cy+cardH/2), 24, 24, pBg); pBg.setShader(null);
        
        // Vehicle Draw (scale 2.5x)
        v.drawPreview(canvas, cx, carCy, 2.5f, v.getSelectedColor());
        
        // Name & Class
        float detailsTopY = cy + cardH * 0.15f;
        
        pText.setColor(Color.WHITE); pText.setTextSize(screenW*0.1f);
        canvas.drawText(v.name, cx, detailsTopY, pText);
        
        pText.setColor(Color.LTGRAY); pText.setTextSize(screenW*0.05f);
        canvas.drawText("Class: " + v.classTier, cx, detailsTopY + screenW*0.08f, pText);
        
        // Stats Bars
        float barY = detailsTopY + screenW*0.15f;
        float barW = screenW * 0.35f; 
        float barH = screenW * 0.02f;
        float barX = cx - barW*0.1f; 
        
        draw10SegmentStatBar(canvas, "SPEED", v.speed, barX, barY, barW, barH);
        draw10SegmentStatBar(canvas, "HANDLING", v.handling, barX, barY + barH*2.5f, barW, barH);
        draw10SegmentStatBar(canvas, "FUEL", v.fuel, barX, barY + barH*5.0f, barW, barH);
        draw10SegmentStatBar(canvas, "DURABILITY", v.durability, barX, barY + barH*7.5f, barW, barH);
        
        // Colors
        for(int i=0; i<8; i++) {
            pBg.setColor(Vehicle.COLOR_PALETTE[i]);
            canvas.drawOval(btnColors[i], pBg);
            
            boolean isUnlocked = storage.isColorUnlocked(prog, v.id, i);
            
            if (!isUnlocked) {
                // Dim locked color
                pBg.setColor(Color.argb(150, 0, 0, 0));
                canvas.drawOval(btnColors[i], pBg);
                
                // Draw Lock Icon
                pText.setColor(Color.WHITE);
                pText.setTextSize(btnColors[i].width() * 0.4f);
                canvas.drawText("🔒", btnColors[i].centerX(), btnColors[i].centerY() + pText.getTextSize()*0.35f, pText);
            }
            
            if(i == v.selectedColorIndex) {
                Paint b = new Paint(Paint.ANTI_ALIAS_FLAG); b.setStyle(Paint.Style.STROKE); b.setStrokeWidth(6f); b.setColor(Color.WHITE);
                canvas.drawOval(btnColors[i].left-6, btnColors[i].top-6, btnColors[i].right+6, btnColors[i].bottom+6, b);
            }
        }
        
        // Action Button
        if(v.id == prog.selectedVehicleId) {
            drawBtn(canvas, btnAction, "IN RACE ✓", Color.parseColor("#4CAF50")); // Green highlight
        } else if (prog.ownedVehicleIds.contains(String.valueOf(v.id))) {
            drawBtn(canvas, btnAction, "SELECT", Color.parseColor("#2196F3"));
        } else {
            boolean canAfford = prog.totalCoins >= v.unlockCost;
            drawBtn(canvas, btnAction, "UNLOCK ("+v.unlockCost+")", canAfford ? Color.parseColor("#FF9800") : Color.parseColor("#9E9E9E"));
        }
        
        if (showingColorConfirm) {
            drawConfirmDialog(canvas);
        }
    }
    
    private void drawConfirmDialog(Canvas canvas) {
        // Dim background
        pBg.setColor(Color.argb(200, 0, 0, 0));
        canvas.drawRect(0, 0, screenW, screenH, pBg);
        
        // Dialog Box
        float dw = screenW * 0.8f, dh = screenH * 0.4f;
        float dx = screenW/2f, dy = screenH/2f;
        canvas.drawRoundRect(new RectF(dx-dw/2, dy-dh/2, dx+dw/2, dy+dh/2), 30, 30, pCard);
        
        pText.setColor(Color.WHITE); pText.setTextSize(screenW*0.08f); pText.setFakeBoldText(true);
        canvas.drawText("UNLOCK COLOR?", screenW/2f, dy - dh*0.2f, pText); pText.setFakeBoldText(false);
        
        pText.setColor(Color.LTGRAY); pText.setTextSize(screenW*0.05f);
        canvas.drawText("This will cost 200 coins.", screenW/2f, dy, pText);
        
        drawBtn(canvas, btnConfirmYes, "UNLOCK", Color.parseColor("#4CAF50"));
        drawBtn(canvas, btnConfirmNo, "CANCEL", Color.parseColor("#F44336"));
    }
    
    private void draw10SegmentStatBar(Canvas canvas, String label, int statValue, float x, float y, float w, float h) {
        pText.setColor(Color.LTGRAY); pText.setTextSize(h*2.0f); pText.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(label, x - w*0.1f, y + h*1.1f, pText);
        pText.setTextAlign(Paint.Align.CENTER);
        
        int color;
        if (statValue >= 7) color = Color.parseColor("#4CAF50"); // Green
        else if (statValue >= 4) color = Color.parseColor("#FFEB3B"); // Yellow
        else color = Color.parseColor("#F44336"); // Red

        Paint pSeg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pSeg.setStyle(Paint.Style.FILL);
        
        float gap = w * 0.05f;
        float segW = (w - gap * 9) / 10f;
        
        for (int i = 0; i < 10; i++) {
            float sx = x + (segW + gap) * i;
            if (i < statValue) {
                pSeg.setColor(color);
            } else {
                pSeg.setColor(Color.parseColor("#33FFFFFF"));
            }
            canvas.drawRect(sx, y, sx + segW, y + h, pSeg);
        }
    }
    
    private void drawBtn(Canvas canvas, RectF r, String l, int c) {
        pBtn.setColor(c); canvas.drawRoundRect(r, 20, 20, pBtn);
        pText.setColor(Color.WHITE); pText.setTextSize(r.height()*0.4f);
        canvas.drawText(l, r.centerX(), r.centerY()+pText.getTextSize()*0.38f, pText);
    }
}
