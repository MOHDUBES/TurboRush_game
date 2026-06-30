package com.turborush.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import com.turborush.game.models.PlayerProgress;
import com.turborush.game.models.Track;

import java.util.List;

public class TrackSelectionScreen {

    private float screenW, screenH, animTimer = 0f;

    public RectF btnBack   = new RectF();
    public RectF btnPrev   = new RectF();
    public RectF btnNext   = new RectF();
    public RectF btnAction = new RectF();

    private final Paint pBg    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCard  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pText  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBtn   = new Paint(Paint.ANTI_ALIAS_FLAG);

    public TrackSelectionScreen() {
        pBg.setStyle(Paint.Style.FILL);
        pCard.setColor(Color.parseColor("#151821")); pCard.setStyle(Paint.Style.FILL);
        pText.setStyle(Paint.Style.FILL); pText.setTextAlign(Paint.Align.CENTER);
        pTitle.setStyle(Paint.Style.FILL); pTitle.setTextAlign(Paint.Align.CENTER); pTitle.setFakeBoldText(true);
        pBtn.setStyle(Paint.Style.FILL);
    }

    public void init(float w, float h) {
        screenW = w; screenH = h;
        btnBack.set(w*0.05f, h*0.05f, w*0.05f+h*0.07f, h*0.05f+h*0.07f);
        
        float cy = h*0.45f, bw = w*0.12f;
        btnPrev.set(w*0.02f, cy-bw/2, w*0.02f+bw, cy+bw/2);
        btnNext.set(w*0.98f-bw, cy-bw/2, w*0.98f, cy+bw/2);
        
        float aw = w*0.7f, ah = h*0.08f;
        btnAction.set(w/2f-aw/2, h*0.88f, w/2f+aw/2, h*0.88f+ah);
    }

    public void update(float dt) { animTimer += dt; }

    public void draw(Canvas canvas, PlayerProgress prog, List<Track> tracks, int curIdx) {
        pBg.setShader(new LinearGradient(0,0,0,screenH, Color.parseColor("#1A1A2E"), Color.parseColor("#0F0F1A"), Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,screenW,screenH, pBg); pBg.setShader(null);
        
        pTitle.setColor(Color.WHITE); pTitle.setTextSize(screenW*0.08f);
        canvas.drawText("TRACKS", screenW/2f, screenH*0.10f, pTitle);
        
        pText.setColor(Color.parseColor("#FFD700")); pText.setTextSize(screenW*0.06f); pText.setFakeBoldText(true);
        canvas.drawText("Coins: " + prog.totalCoins, screenW/2f, screenH*0.15f, pText); pText.setFakeBoldText(false);
        
        drawBtn(canvas, btnBack, "◀", Color.parseColor("#33FFFFFF"));
        if(curIdx > 0) drawBtn(canvas, btnPrev, "◀", Color.parseColor("#22FFFFFF"));
        if(curIdx < tracks.size()-1) drawBtn(canvas, btnNext, "▶", Color.parseColor("#22FFFFFF"));
        
        Track t = tracks.get(curIdx);
        
        // Preview Card
        float cardW = screenW * 0.7f;
        float cardH = cardW * 1.55f;
        float cx = screenW/2f, cy = screenH*0.45f;
        
        canvas.drawRoundRect(new RectF(cx-cardW/2, cy-cardH/2, cx+cardW/2, cy+cardH/2), 24, 24, pCard);
        
        // Spotlight inside card
        float gA = (float)(0.6+0.4*Math.sin(animTimer*3));
        pBg.setShader(new RadialGradient(cx, cy, cardW*0.8f, Color.argb((int)(gA*60),255,255,255), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(cx-cardW/2, cy-cardH/2, cx+cardW/2, cy+cardH/2), 24, 24, pBg); pBg.setShader(null);
        
        // Icon / Emoji representing theme
        pText.setColor(Color.WHITE);
        pText.setTextSize(screenW*0.25f);
        canvas.drawText(getThemeEmoji(t.theme), cx, cy - cardH*0.15f, pText);
        
        // Name
        pText.setColor(Color.WHITE); pText.setTextSize(screenW*0.08f);
        canvas.drawText(t.name.toUpperCase(), cx, cy + cardH*0.1f, pText);
        pText.setColor(Color.LTGRAY); pText.setTextSize(screenW*0.045f);
        canvas.drawText("Theme: " + t.theme.name(), cx, cy + cardH*0.2f, pText);
        
        // Action Button
        if(t.id == prog.selectedTrackId) {
            drawBtn(canvas, btnAction, "SELECTED ✓", Color.parseColor("#4CAF50")); // Green highlight
        } else if (prog.ownedTrackIds.contains(String.valueOf(t.id))) {
            drawBtn(canvas, btnAction, "SELECT", Color.parseColor("#2196F3"));
        } else {
            boolean canAfford = prog.totalCoins >= t.unlockCost;
            drawBtn(canvas, btnAction, "UNLOCK ("+t.unlockCost+")", canAfford ? Color.parseColor("#FF9800") : Color.parseColor("#9E9E9E"));
        }
    }
    
    private String getThemeEmoji(com.turborush.game.models.GameState.WorldTheme theme) {
        switch (theme) {
            case CITY: return "🏙️";
            case VILLAGE: return "🏡";
            case MOUNTAIN: return "⛰️";
            case OCEAN: return "🌊";
            case DESERT: return "🏜️";
            case SNOW: return "❄️";
            case FOREST: return "🌲";
            case CYBERPUNK: return "🌆";
            case VOLCANO: return "🌋";
            case CANYON: return "🏞️";
            case NEON_CITY: return "🌃";
            case RETRO: return "🕹️";
            default: return "🛣️";
        }
    }
    
    private void drawBtn(Canvas canvas, RectF r, String l, int c) {
        pBtn.setColor(c); canvas.drawRoundRect(r, 20, 20, pBtn);
        pText.setColor(Color.WHITE); pText.setTextSize(r.height()*0.4f);
        canvas.drawText(l, r.centerX(), r.centerY()+pText.getTextSize()*0.38f, pText);
    }
}
