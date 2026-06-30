package com.turborush.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

public class SettingsScreen {

    private Paint pBg, pText, pDivider, pIcon, pBtn, pToggleBg, pToggleKnob;

    public boolean musicOn = true;
    public boolean sfxOn = true;
    public boolean tiltOn = false;
    public boolean nightMode = false;
    public int tiltSens = 5;
    public int languageIdx = 0; // 0=EN, 1=UR, 2=HI

    public RectF btnBack, btnReset;
    public RectF[] rowRects = new RectF[6];
    public RectF[] langRects = new RectF[3];
    public RectF sliderRect;
    
    public boolean showingResetConfirm = false;
    public RectF btnConfirmYes = new RectF();
    public RectF btnConfirmNo = new RectF();

    private float w, h;

    public SettingsScreen() {
        pBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pText = new Paint(Paint.ANTI_ALIAS_FLAG);
        pText.setColor(Color.WHITE);
        pDivider = new Paint(Paint.ANTI_ALIAS_FLAG);
        pDivider.setColor(Color.parseColor("#1E3A5F"));
        pIcon = new Paint(Paint.ANTI_ALIAS_FLAG);
        pBtn = new Paint(Paint.ANTI_ALIAS_FLAG);
        pToggleBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pToggleKnob = new Paint(Paint.ANTI_ALIAS_FLAG);

        btnBack = new RectF();
        btnReset = new RectF();
        for (int i=0; i<6; i++) rowRects[i] = new RectF();
        for (int i=0; i<3; i++) langRects[i] = new RectF();
        sliderRect = new RectF();
    }

    public void init(float w, float h) {
        this.w = w;
        this.h = h;
        pBg.setShader(new LinearGradient(0, 0, 0, h, 
            Color.parseColor("#0D0D1A"), Color.parseColor("#1A1A2E"), Shader.TileMode.CLAMP));

        float headerY = h * 0.08f;
        btnBack.set(w * 0.04f, headerY - h * 0.03f, w * 0.18f, headerY + h * 0.03f);

        float rowH = h * 0.1f;
        float startY = headerY + h * 0.08f;

        for (int i=0; i<6; i++) {
            rowRects[i].set(0, startY + (i * rowH), w, startY + ((i+1) * rowH));
        }

        float resetY = rowRects[5].bottom + h * 0.05f;
        btnReset.set(w * 0.1f, resetY, w * 0.9f, resetY + h * 0.07f);

        // Sliders & langs
        float sliderW = w * 0.4f;
        sliderRect.set(w * 0.9f - sliderW, rowRects[4].centerY() - 10f, w * 0.9f, rowRects[4].centerY() + 10f);

        float langW = w * 0.12f;
        float langX = w * 0.9f - (langW * 3f);
        float langH = rowH * 0.4f;
        langRects[0].set(langX, rowRects[5].centerY() - langH/2, langX + langW, rowRects[5].centerY() + langH/2);
        langRects[1].set(langX + langW, rowRects[5].centerY() - langH/2, langX + langW*2, rowRects[5].centerY() + langH/2);
        langRects[2].set(langX + langW*2, rowRects[5].centerY() - langH/2, langX + langW*3, rowRects[5].centerY() + langH/2);
    }

    public void draw(Canvas c) {
        c.drawRect(0, 0, w, h, pBg);

        // Header
        pBtn.setColor(Color.parseColor("#1E3A5F"));
        c.drawRoundRect(btnBack, 25f, 25f, pBtn);
        pText.setTextSize(Math.min(60f, h * 0.03f));
        pText.setTextAlign(Paint.Align.CENTER);
        c.drawText("◄", btnBack.centerX(), btnBack.centerY() + pText.getTextSize()*0.3f, pText);

        pText.setTextSize(Math.min(65f, w * 0.07f));
        pText.setFakeBoldText(true);
        c.drawText("SETTINGS", w / 2f, btnBack.centerY() + pText.getTextSize()*0.3f, pText);
        pText.setFakeBoldText(false);

        // Rows
        String[] icons = {"🔊", "🔔", "📱", "🌙", "⚡", "🌐"};
        String[] labels = {"BACKGROUND MUSIC", "SOUND EFFECTS", "TILT STEERING", "NIGHT MODE", "TILT SENSITIVITY", "LANGUAGE"};
        boolean[] states = {musicOn, sfxOn, tiltOn, nightMode};

        pText.setTextAlign(Paint.Align.LEFT);

        float toggleW = w * 0.15f;
        float toggleH = toggleW * 0.5f;

        for (int i=0; i<6; i++) {
            pBtn.setColor(i % 2 == 0 ? Color.parseColor("#111827") : Color.parseColor("#0F172A"));
            c.drawRect(rowRects[i], pBtn);

            float cy = rowRects[i].centerY();
            pIcon.setTextSize(Math.min(60f, w * 0.06f));
            c.drawText(icons[i], w * 0.06f, cy + pIcon.getTextSize()*0.3f, pIcon);
            
            pText.setTextSize(Math.min(40f, w * 0.04f));
            c.drawText(labels[i], w * 0.16f, cy + pText.getTextSize()*0.3f, pText);

            c.drawRect(0, rowRects[i].bottom, w, rowRects[i].bottom + 2f, pDivider);

            if (i < 4) {
                drawToggle(c, w * 0.9f - toggleW, cy - toggleH/2f, toggleW, toggleH, states[i]);
            } else if (i == 4) {
                pToggleBg.setColor(Color.parseColor("#374151"));
                c.drawRoundRect(sliderRect, 10f, 10f, pToggleBg);
                
                float thumbX = sliderRect.left + (sliderRect.width() * (tiltSens - 1) / 9f);
                pToggleBg.setColor(Color.parseColor("#2563EB"));
                c.drawRect(sliderRect.left, sliderRect.top, thumbX, sliderRect.bottom, pToggleBg);
                
                pToggleKnob.setColor(Color.WHITE);
                c.drawCircle(thumbX, cy, sliderRect.height() * 1.2f, pToggleKnob);
                
                pText.setTextSize(Math.min(30f, w * 0.035f));
                pText.setTextAlign(Paint.Align.CENTER);
                c.drawText(String.valueOf(tiltSens), thumbX, cy - sliderRect.height() * 2f, pText);
                pText.setTextAlign(Paint.Align.LEFT);
            } else if (i == 5) {
                String[] lgs = {"EN", "UR", "HI"};
                pText.setTextAlign(Paint.Align.CENTER);
                for(int j=0; j<3; j++) {
                    pBtn.setColor(languageIdx == j ? Color.parseColor("#2563EB") : Color.parseColor("#1E293B"));
                    c.drawRoundRect(langRects[j], 10f, 10f, pBtn);
                    pText.setColor(languageIdx == j ? Color.WHITE : Color.parseColor("#9CA3AF"));
                    c.drawText(lgs[j], langRects[j].centerX(), langRects[j].centerY() + pText.getTextSize()*0.3f, pText);
                }
                pText.setColor(Color.WHITE);
                pText.setTextAlign(Paint.Align.LEFT);
            }
        }

        // Reset Btn
        pBtn.setColor(Color.parseColor("#DC2626"));
        c.drawRoundRect(btnReset, 30f, 30f, pBtn);
        pText.setTextSize(Math.min(45f, w * 0.045f));
        pText.setFakeBoldText(true);
        pText.setTextAlign(Paint.Align.CENTER);
        c.drawText("⚠ RESET PROGRESS", btnReset.centerX(), btnReset.centerY() + pText.getTextSize()*0.3f, pText);
        pText.setFakeBoldText(false);

        // Footer
        pText.setColor(Color.parseColor("#6B7280"));
        pText.setTextSize(Math.min(30f, w * 0.03f));
        pText.setTextAlign(Paint.Align.CENTER);
        c.drawText("TurboRush v2.0 • Made with ❤ in Android Studio", w/2f, h - h * 0.03f, pText);
        pText.setColor(Color.WHITE);
        
        if (showingResetConfirm) {
            drawResetConfirmDialog(c);
        }
    }
    
    private void drawResetConfirmDialog(Canvas c) {
        // Dim background
        pBg.setColor(Color.argb(200, 0, 0, 0));
        pBg.setShader(null);
        c.drawRect(0, 0, w, h, pBg);
        
        // Dialog Panel
        float dw = w * 0.8f;
        float dh = h * 0.35f;
        RectF dialog = new RectF(w/2 - dw/2, h/2 - dh/2, w/2 + dw/2, h/2 + dh/2);
        pBtn.setColor(Color.parseColor("#1E293B"));
        c.drawRoundRect(dialog, 40f, 40f, pBtn);
        
        pText.setTextSize(Math.min(50f, w * 0.05f));
        pText.setColor(Color.WHITE);
        pText.setTextAlign(Paint.Align.CENTER);
        c.drawText("WARNING", w/2, dialog.top + h * 0.08f, pText);
        
        pText.setTextSize(Math.min(35f, w * 0.035f));
        pText.setColor(Color.parseColor("#9CA3AF"));
        c.drawText("This will delete all progress and logout.", w/2, dialog.top + h * 0.15f, pText);
        
        float btnW = dw * 0.4f;
        float btnH = dh * 0.25f;
        btnConfirmNo.set(dialog.left + dw*0.06f, dialog.bottom - btnH - h*0.05f, dialog.left + dw*0.06f + btnW, dialog.bottom - h*0.05f);
        btnConfirmYes.set(dialog.right - dw*0.06f - btnW, dialog.bottom - btnH - h*0.05f, dialog.right - dw*0.06f, dialog.bottom - h*0.05f);
        
        pBtn.setColor(Color.parseColor("#4B5563"));
        c.drawRoundRect(btnConfirmNo, 20f, 20f, pBtn);
        pBtn.setColor(Color.parseColor("#DC2626"));
        c.drawRoundRect(btnConfirmYes, 20f, 20f, pBtn);
        
        pText.setColor(Color.WHITE);
        pText.setTextSize(Math.min(40f, w * 0.04f));
        c.drawText("CANCEL", btnConfirmNo.centerX(), btnConfirmNo.centerY() + pText.getTextSize()*0.3f, pText);
        c.drawText("RESET", btnConfirmYes.centerX(), btnConfirmYes.centerY() + pText.getTextSize()*0.3f, pText);
    }

    private void drawToggle(Canvas c, float x, float y, float tw, float th, boolean on) {
        RectF rect = new RectF(x, y, x + tw, y + th);
        pToggleBg.setColor(on ? Color.parseColor("#16A34A") : Color.parseColor("#374151"));
        c.drawRoundRect(rect, th/2f, th/2f, pToggleBg);

        float knobRadius = th/2f - 4f;
        float knobX = on ? rect.right - knobRadius - 4f : rect.left + knobRadius + 4f;
        pToggleKnob.setColor(on ? Color.WHITE : Color.parseColor("#9CA3AF"));
        c.drawCircle(knobX, rect.centerY(), knobRadius, pToggleKnob);
    }
}
