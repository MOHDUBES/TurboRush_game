package com.turborush.game.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

import com.turborush.game.models.GameState;

import java.util.Random;

public class Road {
    private float screenW, screenH;
    public float roadTop, roadBottom;
    public float roadWidth;
    public float laneWidth;
    public float roadLeft, roadRight;

    private float scrollOffset = 0f;
    private float envScrollOffset = 0f; // Slower scrolling for environment
    private float animTimer = 0f;

    private final Paint pBg = new Paint();
    private final Paint pRoad = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pGrass = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pSky = new Paint();
    private final Paint pMarking = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pDeco = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    // Pre-calculated decorative elements
    private float[] treeY = new float[10];
    private float[] potholeY = new float[5];
    private float[] lightY = new float[10];
    private float[] coneY = new float[15];

    public void init(float width, float height) {
        screenW = width;
        screenH = height;

        roadTop = 0f;
        roadBottom = screenH * 0.88f; // Above control buttons
        roadWidth = screenW * 0.72f;
        laneWidth = roadWidth / 3f;
        roadLeft = screenW / 2f - roadWidth / 2f;
        roadRight = screenW / 2f + roadWidth / 2f;

        Random rand = new Random();
        for (int i=0; i<10; i++) { treeY[i] = rand.nextFloat() * screenH * 2; lightY[i] = i * 300f; }
        for (int i=0; i<5; i++) potholeY[i] = rand.nextFloat() * screenH * 2;
        for (int i=0; i<15; i++) coneY[i] = i * 200f;
    }

    public void update(float speed, float dt) {
        animTimer += dt;
        scrollOffset += speed * dt;
        envScrollOffset += (speed * 0.8f) * dt; // Environment moves slightly slower for depth
        
        if (scrollOffset > 200f) scrollOffset -= 200f;
        if (envScrollOffset > screenH * 2) envScrollOffset -= screenH * 2;
    }

    public void draw(Canvas canvas, GameState.WorldTheme theme, boolean isNightMode) {
        // Base Background
        pBg.setColor(isNightMode ? Color.parseColor("#050510") : Color.parseColor("#1A1A2E"));
        canvas.drawRect(0, 0, screenW, screenH, pBg);

        drawSky(canvas, theme, isNightMode);
        drawSkyline(canvas, theme, isNightMode);
        drawGrass(canvas, theme, isNightMode);
        drawRoadSurface(canvas, theme, isNightMode);
        drawRoadMarkings(canvas, theme, isNightMode);
        drawDecorations(canvas, theme, isNightMode);
    }

    private void drawSky(Canvas canvas, GameState.WorldTheme theme, boolean isNightMode) {
        if (isNightMode) {
            pSky.setShader(new LinearGradient(0, 0, 0, roadTop, Color.parseColor("#02020A"), Color.parseColor("#0A0E2A"), Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, screenW, roadTop, pSky);
            
            // Draw Stars
            Paint pStar = new Paint(Paint.ANTI_ALIAS_FLAG);
            pStar.setColor(Color.WHITE);
            Random rand = new Random(42); // Seeded so stars don't flicker
            for (int i=0; i<30; i++) {
                canvas.drawCircle(rand.nextFloat() * screenW, rand.nextFloat() * roadTop, rand.nextFloat() * 2f + 1f, pStar);
            }
        } else {
            switch (theme) {
                case CITY:
                    pSky.setColor(Color.parseColor("#0A0E2A"));
                    break;
                case VILLAGE:
                case FOREST:
                case CANYON:
                    pSky.setShader(new LinearGradient(0, 0, 0, roadTop, Color.parseColor("#87CEEB"), Color.parseColor("#E0F6FF"), Shader.TileMode.CLAMP));
                    break;
                case OCEAN:
                    pSky.setShader(new LinearGradient(0, 0, 0, roadTop, Color.parseColor("#4A148C"), Color.parseColor("#FF6F00"), Shader.TileMode.CLAMP));
                    break;
                case DESERT:
                    pSky.setShader(new LinearGradient(0, 0, 0, roadTop, Color.parseColor("#FF9800"), Color.parseColor("#FFCC80"), Shader.TileMode.CLAMP));
                    break;
                case SNOW:
                case MOUNTAIN:
                    pSky.setShader(new LinearGradient(0, 0, 0, roadTop, Color.parseColor("#B0BEC5"), Color.parseColor("#ECEFF1"), Shader.TileMode.CLAMP));
                    break;
                case CYBERPUNK:
                case NEON_CITY:
                    pSky.setShader(new LinearGradient(0, 0, 0, roadTop, Color.parseColor("#1A0033"), Color.parseColor("#4A0080"), Shader.TileMode.CLAMP));
                    break;
                case VOLCANO:
                    pSky.setShader(new LinearGradient(0, 0, 0, roadTop, Color.parseColor("#3E0000"), Color.parseColor("#FF4500"), Shader.TileMode.CLAMP));
                    break;
                case RETRO:
                    pSky.setShader(new LinearGradient(0, 0, 0, roadTop, Color.parseColor("#000022"), Color.parseColor("#FF00FF"), Shader.TileMode.CLAMP));
                    break;
                default: pSky.setShader(null); pSky.setColor(Color.TRANSPARENT);
            }
            canvas.drawRect(0, 0, screenW, roadTop, pSky);
        }
        pSky.setShader(null);
    }

    private void drawSkyline(Canvas canvas, GameState.WorldTheme theme, boolean isNightMode) {
        Paint pBuild = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (theme == GameState.WorldTheme.CITY) {
            pBuild.setColor(isNightMode ? Color.parseColor("#050818") : Color.parseColor("#101535"));
            float blockH = screenH / 4f;
            for (int i = -1; i <= 5; i++) {
                float by = (i * blockH) + (envScrollOffset % blockH);
                
                // Left building
                float leftBW = roadLeft - 10;
                canvas.drawRect(0, by, leftBW, by + blockH - 20, pBuild);
                
                // Right building
                float rightBX = roadRight + 10;
                canvas.drawRect(rightBX, by, screenW, by + blockH - 20, pBuild);
                
                // Roof details (lighter color to give top-down 3D effect)
                pBuild.setColor(isNightMode ? Color.parseColor("#0A0D20") : Color.parseColor("#1A237E"));
                canvas.drawRect(10, by + 10, leftBW - 10, by + blockH - 30, pBuild);
                canvas.drawRect(rightBX + 10, by + 10, screenW - 10, by + blockH - 30, pBuild);
                
                // Optional: A little yellow glow on edges to look like lights
                if (isNightMode) {
                    pBuild.setColor(Color.parseColor("#FFF59D"));
                    pBuild.setAlpha(150); // Brighter lights at night
                } else {
                    pBuild.setColor(Color.parseColor("#FFF59D"));
                    pBuild.setAlpha(80);
                }
                
                canvas.drawRect(leftBW - 8, by + 20, leftBW, by + blockH - 40, pBuild);
                canvas.drawRect(rightBX, by + 20, rightBX + 8, by + blockH - 40, pBuild);
                
                pBuild.setAlpha(255);
                pBuild.setColor(isNightMode ? Color.parseColor("#050818") : Color.parseColor("#101535"));
            }
        } else if (theme == GameState.WorldTheme.MOUNTAIN) {
            pBuild.setColor(isNightMode ? Color.parseColor("#1A252A") : Color.parseColor("#546E7A"));
            float blockH = screenH / 2f;
            for (int i = -1; i <= 3; i++) {
                float by = (i * blockH) + (envScrollOffset % blockH);
                // Draw rocky shapes on sides
                Path mtn = new Path();
                mtn.moveTo(0, by);
                mtn.lineTo(roadLeft - 20, by + blockH / 2f);
                mtn.lineTo(0, by + blockH);
                mtn.close();
                canvas.drawPath(mtn, pBuild);
                
                Path mtnR = new Path();
                mtnR.moveTo(screenW, by);
                mtnR.lineTo(roadRight + 20, by + blockH / 2f);
                mtnR.lineTo(screenW, by + blockH);
                mtnR.close();
                canvas.drawPath(mtnR, pBuild);
            }
        }
    }

    private void drawGrass(Canvas canvas, GameState.WorldTheme theme, boolean isNightMode) {
        int leftColor = Color.parseColor("#1A1A2E");
        int rightColor = Color.parseColor("#1A1A2E");
        switch (theme) {
            case VILLAGE: case FOREST: leftColor = rightColor = Color.parseColor("#4CAF50"); break;
            case MOUNTAIN: leftColor = rightColor = Color.parseColor("#CFD8DC"); break;
            case OCEAN: leftColor = Color.parseColor("#1565C0"); rightColor = Color.parseColor("#F4A460"); break;
            case DESERT: leftColor = rightColor = Color.parseColor("#EDC9AF"); break;
            case SNOW: leftColor = rightColor = Color.parseColor("#FAFAFA"); break;
            case CYBERPUNK: case NEON_CITY: leftColor = rightColor = Color.parseColor("#0D0D1A"); break;
            case VOLCANO: leftColor = rightColor = Color.parseColor("#2C0E00"); break;
            case CANYON: leftColor = rightColor = Color.parseColor("#8D6E63"); break;
            case RETRO: leftColor = rightColor = Color.parseColor("#000000"); break;
            case CITY: leftColor = rightColor = Color.parseColor("#1A1A2E"); break;
        }
        
        if (isNightMode) {
            leftColor = darkenColor(leftColor, 0.25f);
            rightColor = darkenColor(rightColor, 0.25f);
        }
        
        pGrass.setColor(leftColor);
        canvas.drawRect(0, roadTop, roadLeft, roadBottom, pGrass);
        
        pGrass.setColor(rightColor);
        canvas.drawRect(roadRight, roadTop, screenW, roadBottom, pGrass);
    }

    private void drawRoadSurface(Canvas canvas, GameState.WorldTheme theme, boolean isNightMode) {
        int c = Color.BLACK;
        switch (theme) {
            case CITY: case NEON_CITY: c = Color.parseColor("#2A2A2A"); break;
            case VILLAGE: case FOREST: c = Color.parseColor("#3D2B1A"); break;
            case MOUNTAIN: c = Color.parseColor("#78909C"); break;
            case OCEAN: c = Color.parseColor("#1A3A2A"); break;
            case DESERT: c = Color.parseColor("#A1887F"); break;
            case SNOW: c = Color.parseColor("#CFD8DC"); break;
            case CYBERPUNK: c = Color.parseColor("#121212"); break;
            case VOLCANO: c = Color.parseColor("#212121"); break;
            case CANYON: c = Color.parseColor("#5D4037"); break;
            case RETRO: c = Color.parseColor("#000033"); break;
        }
        
        if (isNightMode) {
            c = darkenColor(c, 0.4f);
        }
        
        pRoad.setColor(c);
        canvas.drawRect(roadLeft, roadTop, roadRight, roadBottom, pRoad);
    }

    private void drawRoadMarkings(Canvas canvas, GameState.WorldTheme theme, boolean isNightMode) {
        float dashH = 80f;
        float gapH = 60f;
        float totalH = dashH + gapH;

        if (theme == GameState.WorldTheme.MOUNTAIN || theme == GameState.WorldTheme.CANYON) {
            // Draw Cones
            pMarking.setColor(isNightMode ? darkenColor(Color.parseColor("#FF5722"), 0.5f) : Color.parseColor("#FF5722"));
            for (int i=0; i<15; i++) {
                float y = (coneY[i] + scrollOffset) % (screenH * 1.5f);
                if (y > 0 && y < roadBottom) {
                    drawCone(canvas, roadLeft + laneWidth, y);
                    drawCone(canvas, roadLeft + laneWidth * 2, y);
                }
            }
        } else {
            // Normal Dashes
            switch (theme) {
                case CITY: case OCEAN: case DESERT: case VOLCANO: case SNOW:
                    pMarking.setColor(Color.WHITE); break;
                case VILLAGE: case FOREST: 
                    pMarking.setColor(Color.parseColor("#BDB76B")); break;
                case CYBERPUNK: case NEON_CITY: 
                    pMarking.setColor(Color.parseColor("#00FFFF")); break;
                case RETRO:
                    pMarking.setColor(Color.parseColor("#FF00FF")); break;
                default: pMarking.setColor(Color.WHITE);
            }
            
            if (isNightMode) {
                pMarking.setAlpha(80); // Darker markings at night
            } else {
                pMarking.setAlpha(180);
            }

            float startY = (scrollOffset % totalH) - totalH;
            for (float y = startY; y < roadBottom; y += totalH) {
                if (y + dashH > roadTop) {
                    float drawY = Math.max(y, roadTop);
                    float drawH = (y + dashH) - drawY;
                    if (drawH > 0) {
                        canvas.drawRect(roadLeft + laneWidth - 4, drawY, roadLeft + laneWidth + 4, drawY + drawH, pMarking);
                        canvas.drawRect(roadLeft + laneWidth * 2 - 4, drawY, roadLeft + laneWidth * 2 + 4, drawY + drawH, pMarking);
                    }
                }
            }
        }
    }

    private void drawDecorations(Canvas canvas, GameState.WorldTheme theme, boolean isNightMode) {
        switch (theme) {
            case CITY: case NEON_CITY: case CYBERPUNK:
                // Streetlights
                pDeco.setColor(theme == GameState.WorldTheme.CITY ? Color.parseColor("#FF9800") : Color.parseColor("#00FFFF"));
                if (isNightMode) {
                    pDeco.setShadowLayer(25, 0, 0, Color.WHITE); // Glowing lights at night
                }
                for(int i=0; i<10; i++) {
                    float y = (lightY[i] + envScrollOffset) % (screenH * 1.5f);
                    if (y < roadBottom) {
                        canvas.drawCircle(roadLeft - 20, y, 15, pDeco);
                        canvas.drawCircle(roadRight + 20, y, 15, pDeco);
                    }
                }
                pDeco.clearShadowLayer();
                break;

            case VILLAGE: case FOREST:
                // Trees & Potholes
                pDeco.setColor(isNightMode ? darkenColor(Color.parseColor("#2E1A11"), 0.2f) : Color.parseColor("#2E1A11"));
                for(int i=0; i<5; i++) {
                    float y = (potholeY[i] + scrollOffset) % (screenH * 1.5f);
                    if (y < roadBottom) canvas.drawOval(new RectF(screenW/2 - 30, y, screenW/2 + 30, y+20), pDeco);
                }
                for(int i=0; i<10; i++) {
                    float y = (treeY[i] + envScrollOffset) % (screenH * 2f);
                    if (y < roadBottom) {
                        float x = i%2==0 ? roadLeft - 60 : roadRight + 60;
                        pDeco.setColor(isNightMode ? darkenColor(Color.parseColor("#5D4037"), 0.3f) : Color.parseColor("#5D4037"));
                        canvas.drawRect(x-10, y, x+10, y+40, pDeco);
                        
                        int leafC = theme == GameState.WorldTheme.FOREST ? Color.parseColor("#1B5E20") : Color.parseColor("#388E3C");
                        pDeco.setColor(isNightMode ? darkenColor(leafC, 0.3f) : leafC);
                        canvas.drawCircle(x, y-10, 30, pDeco);
                    }
                }
                break;

            case MOUNTAIN: case SNOW:
                // Fog Overlay
                if (!isNightMode) {
                    pDeco.setShader(new LinearGradient(0, 0, 0, screenH * 0.2f, Color.argb(150, 255, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP));
                    canvas.drawRect(0, 0, screenW, screenH * 0.2f, pDeco);
                    pDeco.setShader(null);
                }
                if (theme == GameState.WorldTheme.SNOW) {
                    pDeco.setColor(isNightMode ? Color.parseColor("#888888") : Color.WHITE);
                    for(int i=0; i<10; i++) {
                        float y = (treeY[i] + scrollOffset*1.2f) % screenH;
                        canvas.drawCircle((lightY[i]%screenW), y, 5, pDeco);
                    }
                }
                break;

            case OCEAN:
                // Wavy Water Left
                pDeco.setColor(isNightMode ? darkenColor(Color.parseColor("#1976D2"), 0.3f) : Color.parseColor("#1976D2"));
                pDeco.setStyle(Paint.Style.STROKE);
                pDeco.setStrokeWidth(6f);
                for(int i=0; i<5; i++) {
                    Path wave = new Path();
                    float startY = ((i*200) + envScrollOffset * 0.5f) % screenH;
                    wave.moveTo(0, startY);
                    for(float x=0; x<roadLeft; x+=20) wave.lineTo(x, startY + (float)Math.sin(animTimer*2 + x*0.1)*15);
                    canvas.drawPath(wave, pDeco);
                }
                pDeco.setStyle(Paint.Style.FILL);
                break;
                
            case DESERT: case CANYON:
                pDeco.setColor(isNightMode ? darkenColor(Color.parseColor("#A1887F"), 0.2f) : Color.parseColor("#A1887F"));
                for(int i=0; i<5; i++) {
                    float y = (potholeY[i] + envScrollOffset) % (screenH * 1.5f);
                    if (y < roadBottom) {
                        float x = i%2==0 ? roadLeft - 40 : roadRight + 40;
                        canvas.drawCircle(x, y, 20, pDeco);
                        canvas.drawCircle(x+15, y-10, 15, pDeco);
                    }
                }
                break;
                
            case VOLCANO:
                // Lava pools
                pDeco.setColor(Color.parseColor("#FF3D00"));
                if (isNightMode) pDeco.setShadowLayer(20, 0, 0, Color.YELLOW);
                for(int i=0; i<5; i++) {
                    float y = (potholeY[i] + scrollOffset) % (screenH * 1.5f);
                    if (y < roadBottom) {
                        float x = i%2==0 ? roadLeft - 50 : roadRight + 50;
                        canvas.drawOval(new RectF(x-30, y-15, x+30, y+15), pDeco);
                    }
                }
                pDeco.clearShadowLayer();
                break;
                
            case RETRO:
                // Grid lines over grass
                pDeco.setColor(isNightMode ? darkenColor(Color.parseColor("#FF00FF"), 0.5f) : Color.parseColor("#FF00FF"));
                pDeco.setStyle(Paint.Style.STROKE);
                pDeco.setStrokeWidth(3f);
                float gridGap = 100f;
                float startY = (scrollOffset % gridGap) - gridGap;
                for (float y = startY; y < roadBottom; y += gridGap) {
                    canvas.drawLine(0, y, roadLeft, y, pDeco);
                    canvas.drawLine(roadRight, y, screenW, y, pDeco);
                }
                pDeco.setStyle(Paint.Style.FILL);
                break;
        }
    }

    private void drawCone(Canvas canvas, float cx, float cy) {
        Path p = new Path();
        p.moveTo(cx, cy);
        p.lineTo(cx - 10, cy + 30);
        p.lineTo(cx + 10, cy + 30);
        p.close();
        canvas.drawPath(p, pMarking);
    }
    
    private int darkenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.max(0, (int)(Color.red(color) * factor));
        int g = Math.max(0, (int)(Color.green(color) * factor));
        int b = Math.max(0, (int)(Color.blue(color) * factor));
        return Color.argb(a, r, g, b);
    }
}
