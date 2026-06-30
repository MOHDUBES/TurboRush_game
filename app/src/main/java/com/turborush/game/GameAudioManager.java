package com.turborush.game;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class GameAudioManager {

    private final Context ctx;
    private boolean isMuted = false; // For Background/Engine
    private boolean isSfxMuted = false;
    private boolean isEnginePlaying = false;

    private int engineStreamId = -1;
    private int sfxEngine = -1;
    private SoundPool soundPool;

    private int sfxCoin, sfxFuel, sfxCrash, sfxLevelUp, sfxGameOver, sfxWarning, sfxUi;
    
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener focusChangeListener;

    public GameAudioManager(Context ctx) {
        this.ctx = ctx;
        audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
                
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(attrs)
                .build();

        // Focus listener
        focusChangeListener = focusChange -> {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                pauseAll();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                resumeAll();
            }
        };

        generateSounds();
    }
    
    private void requestFocus() {
        if (audioManager != null) {
            audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }
    }

    private AudioTrack engineTrack;
    private short[] engineBuffer;

    private void generateSounds() {
        int sampleRate = 44100;
        
        // Engine loop - Realistic Racing Engine Synthesis
        int engineSamples = (int)(0.5 * sampleRate);
        engineBuffer = new short[engineSamples];
        for (int i = 0; i < engineSamples; i++) {
            double baseFreq = 160.0;
            double t = (double) i / sampleRate;
            double wave1 = Math.sin(2 * Math.PI * baseFreq * t);
            double wave2 = 0.60 * Math.sin(2 * Math.PI * baseFreq * 2.0 * t);
            double wave3 = 0.85 * Math.sin(2 * Math.PI * baseFreq * 3.0 * t);
            double wave4 = 0.40 * Math.sin(2 * Math.PI * baseFreq * 4.0 * t);
            double wave5 = 0.70 * Math.sin(2 * Math.PI * baseFreq * 5.0 * t);
            double wave6 = 0.30 * Math.sin(2 * Math.PI * baseFreq * 6.0 * t);
            double wave8 = 0.20 * Math.sin(2 * Math.PI * baseFreq * 8.0 * t);
            double whine = 0.25 * Math.sin(2 * Math.PI * baseFreq * 4.5 * t);
            double noise = (Math.random() * 2 - 1) * 0.15;
            double envelope = 0.75 + 0.25 * Math.sin(2 * Math.PI * (baseFreq / 2.0) * t);
            double finalWave = (wave1 + wave2 + wave3 + wave4 + wave5 + wave6 + wave8 + whine + noise) * envelope;
            
            if (finalWave > 1.2) finalWave = 1.2 + (finalWave - 1.2) * 0.1;
            if (finalWave < -1.2) finalWave = -1.2 + (finalWave + 1.2) * 0.1;
            
            engineBuffer[i] = (short)(finalWave / 4.5 * 18000); 
        }
        
        int bufferSize = engineBuffer.length * 2;
        engineTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STATIC);
        engineTrack.write(engineBuffer, 0, engineBuffer.length);
        engineTrack.setLoopPoints(0, engineBuffer.length, -1);
        
        // One-shots loaded into SoundPool via temporary WAV files

        // One-shots loaded into SoundPool via temporary WAV files
        sfxCoin     = loadToSoundPool(createCoinPcm(), sampleRate);
        sfxFuel     = loadToSoundPool(createFuelPcm(), sampleRate);
        sfxCrash    = loadToSoundPool(createCrashPcm(), sampleRate);
        sfxLevelUp  = loadToSoundPool(createLevelUpPcm(), sampleRate);
        sfxGameOver = loadToSoundPool(createGameOverPcm(), sampleRate);
        sfxWarning  = loadToSoundPool(createWarningPcm(), sampleRate);
        sfxUi       = loadToSoundPool(createUiPcm(), sampleRate);
    }

    private int loadToSoundPool(short[] pcm, int sampleRate) {
        try {
            File tempWav = File.createTempFile("snd", ".wav", ctx.getCacheDir());
            FileOutputStream out = new FileOutputStream(tempWav);
            writeWavHeader(out, pcm.length, sampleRate);
            for (short s : pcm) {
                out.write(s & 0xff);
                out.write((s >> 8) & 0xff);
            }
            out.close();
            int soundId = soundPool.load(tempWav.getAbsolutePath(), 1);
            tempWav.deleteOnExit();
            return soundId;
        } catch (IOException e) {
            return -1;
        }
    }

    private short[] createCoinPcm() {
        int sr = 44100;
        short[] b = new short[(int)(0.18 * sr)];
        int i = 0;
        for (; i < (int)(0.08 * sr); i++) b[i] = (short)(Math.sin(2 * Math.PI * i / (sr / 880.0)) * 16000);
        for (; i < b.length; i++) b[i] = (short)(Math.sin(2 * Math.PI * i / (sr / 1100.0)) * 16000);
        return b;
    }

    private short[] createFuelPcm() {
        int sr = 44100;
        short[] b = new short[(int)(0.2 * sr)];
        for (int i = 0; i < b.length; i++) {
            double freq = 400.0 + (400.0 * i / b.length);
            b[i] = (short)(Math.sin(2 * Math.PI * i / (sr / freq)) * 16000);
        }
        return b;
    }

    private short[] createCrashPcm() {
        int sr = 44100;
        short[] b = new short[(int)(0.3 * sr)];
        Random r = new Random();
        for (int i = 0; i < b.length; i++) {
            float decay = 1f - (float)i / b.length;
            b[i] = (short)((r.nextInt(32767) - 16384) * decay * 1.5f);
        }
        return b;
    }

    private short[] createLevelUpPcm() {
        int sr = 44100;
        short[] b = new short[(int)(0.36 * sr)];
        int p1 = (int)(0.12 * sr);
        int p2 = (int)(0.24 * sr);
        for (int i = 0; i < p1; i++) b[i] = (short)(Math.sin(2 * Math.PI * i / (sr / 523.0)) * 16000);
        for (int i = p1; i < p2; i++) b[i] = (short)(Math.sin(2 * Math.PI * i / (sr / 659.0)) * 16000);
        for (int i = p2; i < b.length; i++) b[i] = (short)(Math.sin(2 * Math.PI * i / (sr / 784.0)) * 16000);
        return b;
    }

    private short[] createGameOverPcm() {
        int sr = 44100;
        short[] b = new short[(int)(0.6 * sr)];
        int p1 = (int)(0.2 * sr);
        int p2 = (int)(0.4 * sr);
        for (int i = 0; i < p1; i++) b[i] = (short)(Math.sin(2 * Math.PI * i / (sr / 400.0)) * 16000);
        for (int i = p1; i < p2; i++) b[i] = (short)(Math.sin(2 * Math.PI * i / (sr / 300.0)) * 16000);
        for (int i = p2; i < b.length; i++) b[i] = (short)(Math.sin(2 * Math.PI * i / (sr / 200.0)) * 16000);
        return b;
    }

    private short[] createWarningPcm() {
        int sr = 44100;
        short[] b = new short[(int)(0.1 * sr)];
        for (int i = 0; i < b.length; i++) b[i] = (short)(Math.sin(2 * Math.PI * i / (sr / 600.0)) * 20000);
        return b;
    }

    private short[] createUiPcm() {
        int sr = 44100;
        short[] b = new short[(int)(0.05 * sr)];
        for (int i = 0; i < b.length; i++) b[i] = (short)(Math.sin(2 * Math.PI * i / (sr / 1200.0)) * 10000);
        return b;
    }

    private void writeWavHeader(FileOutputStream out, int numSamples, int sampleRate) throws IOException {
        int byteRate = sampleRate * 2;
        int dataSize = numSamples * 2;
        byte[] header = new byte[44];
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        int chunkSize = 36 + dataSize;
        header[4] = (byte)(chunkSize & 0xff); header[5] = (byte)((chunkSize >> 8) & 0xff);
        header[6] = (byte)((chunkSize >> 16) & 0xff); header[7] = (byte)((chunkSize >> 24) & 0xff);
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0;
        header[20] = 1; header[21] = 0;
        header[22] = 1; header[23] = 0;
        header[24] = (byte)(sampleRate & 0xff); header[25] = (byte)((sampleRate >> 8) & 0xff);
        header[26] = (byte)((sampleRate >> 16) & 0xff); header[27] = (byte)((sampleRate >> 24) & 0xff);
        header[28] = (byte)(byteRate & 0xff); header[29] = (byte)((byteRate >> 8) & 0xff);
        header[30] = (byte)((byteRate >> 16) & 0xff); header[31] = (byte)((byteRate >> 24) & 0xff);
        header[32] = 2; header[33] = 0;
        header[34] = 16; header[35] = 0;
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        header[40] = (byte)(dataSize & 0xff); header[41] = (byte)((dataSize >> 8) & 0xff);
        header[42] = (byte)((dataSize >> 16) & 0xff); header[43] = (byte)((dataSize >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    public void setMuted(boolean m) {
        this.isMuted = m;
        if (m) stopEngine();
    }
    
    public void setSfxMuted(boolean m) {
        this.isSfxMuted = m;
    }

    public void playEngine() {
        isEnginePlaying = true;
        if (isMuted || engineTrack == null) return;
        requestFocus();
        if (engineTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            engineTrack.play();
        }
    }

    public void updateEnginePitch(float speedRatio) {
        if (engineTrack != null) {
            if (engineTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING && !isMuted && isEnginePlaying) {
                engineTrack.play();
            }
            if (engineTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                float rate = 1.0f + (speedRatio * 0.88f);
                rate = Math.max(0.5f, Math.min(2.0f, rate));
                int newSampleRate = (int)(44100 * rate);
                engineTrack.setPlaybackRate(newSampleRate);
            }
        }
    }

    public void stopEngine() {
        isEnginePlaying = false;
        if (engineTrack != null && engineTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            engineTrack.pause();
        }
    }

    public void pauseAll() {
        stopEngine();
        if (soundPool != null) soundPool.autoPause();
    }

    public void resumeAll() {
        if (!isMuted && isEnginePlaying) playEngine();
        if (soundPool != null && !isMuted) soundPool.autoResume();
    }

    private void playSfx(int id) {
        if (!isSfxMuted && soundPool != null && id != -1) {
            requestFocus();
            soundPool.play(id, 1f, 1f, 1, 0, 1f);
        }
    }

    public void playUi() { playSfx(sfxUi); }
    public void playCoin() { playSfx(sfxCoin); }
    public void playFuel() { playSfx(sfxFuel); }
    public void playCrash() { playSfx(sfxCrash); }
    public void playLevelUp() { playSfx(sfxLevelUp); }
    public void playGameOver() { playSfx(sfxGameOver); }
    public void playWarning() { playSfx(sfxWarning); }

    public void release() {
        if (soundPool != null) { soundPool.release(); soundPool = null; }
        if (engineTrack != null) {
            engineTrack.stop();
            engineTrack.release();
            engineTrack = null;
        }
    }
}
