package com.turborush.game.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.turborush.game.models.GameState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ParticleSystem {

    private static class Spark {
        float x, y, vx, vy, life, maxLife;
        int color;
    }

    private static class ExplosionParticle {
        float x, y, vx, vy, life, maxLife, size;
        int color;
    }

    private static class WeatherParticle {
        float x, y, speed, size;
    }

    private final List<Spark> sparks = new ArrayList<>();
    private final List<ExplosionParticle> explosionParticles = new ArrayList<>();
    private final List<WeatherParticle> snowParticles = new ArrayList<>();
    private final List<WeatherParticle> rainParticles = new ArrayList<>();

    private final Paint pSpark = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pWeather = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rand = new Random();

    private float screenW, screenH;

    public void init(float w, float h) {
        screenW = w;
        screenH = h;
        
        // Init Snow
        for(int i=0; i<40; i++) {
            WeatherParticle wp = new WeatherParticle();
            wp.x = rand.nextFloat() * w;
            wp.y = rand.nextFloat() * h;
            wp.speed = (h * 0.05f) + rand.nextFloat() * (h * 0.05f); // Proportional
            wp.size = 2f + rand.nextFloat() * 4f;
            snowParticles.add(wp);
        }

        // Init Rain
        for(int i=0; i<60; i++) {
            WeatherParticle wp = new WeatherParticle();
            wp.x = rand.nextFloat() * w;
            wp.y = rand.nextFloat() * h;
            wp.speed = (h * 0.5f) + rand.nextFloat() * (h * 0.3f); // Proportional
            wp.size = 3f + rand.nextFloat() * 5f;
            rainParticles.add(wp);
        }
    }

    public void spawnSparks(float cx, float cy) {
        for (int i = 0; i < 8; i++) {
            Spark s = new Spark();
            s.x = cx + (rand.nextFloat() - 0.5f) * 20f;
            s.y = cy + (rand.nextFloat() - 0.5f) * 20f;
            float baseSpeed = screenH * 0.2f;
            s.vx = (rand.nextFloat() - 0.5f) * baseSpeed;
            s.vy = (rand.nextFloat() - 0.5f) * baseSpeed - (baseSpeed * 0.3f);
            s.maxLife = 0.3f + rand.nextFloat() * 0.2f;
            s.life = s.maxLife;
            s.color = rand.nextBoolean() ? Color.parseColor("#FFD700") : Color.parseColor("#FFA000");
            sparks.add(s);
        }
    }

    public void spawnExplosion(float cx, float cy) {
        int[] blastColors = {Color.parseColor("#FF3D00"), Color.parseColor("#FF9100"), Color.parseColor("#FFEA00"), Color.parseColor("#424242")};
        for(int i=0; i<16; i++) {
            ExplosionParticle p = new ExplosionParticle();
            p.x = cx; p.y = cy;
            float angle = (float) (rand.nextFloat() * Math.PI * 2);
            float speed = (screenH * 0.05f) + rand.nextFloat() * (screenH * 0.2f);
            p.vx = (float) Math.cos(angle) * speed;
            p.vy = (float) Math.sin(angle) * speed;
            p.maxLife = 0.5f + rand.nextFloat() * 0.3f;
            p.life = p.maxLife;
            p.size = 10f + rand.nextFloat() * 20f;
            p.color = blastColors[rand.nextInt(blastColors.length)];
            explosionParticles.add(p);
        }
    }

    public void update(float dt, GameState.WorldTheme currentTheme) {
        // Update Sparks
        Iterator<Spark> it = sparks.iterator();
        while (it.hasNext()) {
            Spark s = it.next();
            s.x += s.vx * dt;
            s.y += s.vy * dt;
            s.life -= dt;
            if (s.life <= 0) it.remove();
        }

        // Update Explosion
        Iterator<ExplosionParticle> itE = explosionParticles.iterator();
        while(itE.hasNext()) {
            ExplosionParticle p = itE.next();
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.size *= 0.95f; // Shrink over time
            p.life -= dt;
            if(p.life <= 0) itE.remove();
        }

        // Update Weather based on theme
        if (currentTheme == GameState.WorldTheme.MOUNTAIN) {
            for (WeatherParticle wp : snowParticles) {
                wp.y += wp.speed * dt;
                wp.x += (float)Math.sin(wp.y * 0.01f) * 30f * dt; // Sway
                if (wp.y > screenH) {
                    wp.y = -10f;
                    wp.x = rand.nextFloat() * screenW;
                }
            }
        } else if (currentTheme == GameState.WorldTheme.OCEAN) {
            for (WeatherParticle wp : rainParticles) {
                wp.y += wp.speed * dt;
                wp.x -= wp.speed * 0.3f * dt; // Diagonal rain
                if (wp.y > screenH || wp.x < 0) {
                    wp.y = -50f;
                    wp.x = rand.nextFloat() * screenW + screenW * 0.3f;
                }
            }
        }
    }

    public void draw(Canvas canvas, GameState.WorldTheme currentTheme) {
        // Draw Sparks
        for (Spark s : sparks) {
            pSpark.setColor(s.color);
            pSpark.setAlpha((int) ((s.life / s.maxLife) * 255));
            canvas.drawCircle(s.x, s.y, 4f, pSpark);
        }

        // Draw Explosion
        for (ExplosionParticle p : explosionParticles) {
            pSpark.setColor(p.color);
            pSpark.setAlpha((int) ((p.life / p.maxLife) * 255));
            canvas.drawCircle(p.x, p.y, p.size, pSpark);
        }

        // Draw Weather
        if (currentTheme == GameState.WorldTheme.MOUNTAIN) {
            pWeather.setColor(Color.WHITE);
            pWeather.setAlpha(180);
            for (WeatherParticle wp : snowParticles) {
                canvas.drawCircle(wp.x, wp.y, wp.size, pWeather);
            }
        } else if (currentTheme == GameState.WorldTheme.OCEAN) {
            pWeather.setColor(Color.parseColor("#B3E5FC"));
            pWeather.setAlpha(120);
            pWeather.setStrokeWidth(3f);
            for (WeatherParticle wp : rainParticles) {
                canvas.drawLine(wp.x, wp.y, wp.x - wp.size * 5f, wp.y + wp.size * 15f, pWeather);
            }
        }
    }
}
