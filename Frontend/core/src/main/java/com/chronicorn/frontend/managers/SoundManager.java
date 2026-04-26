package com.chronicorn.frontend.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.HashMap;

public class SoundManager implements Disposable {
    private static SoundManager instance;

    private float musicVolume;
    private float sfxVolume;
    private float ambientVolume;

    // Track currently playing file names to prevent restarting the same track
    private String currentMusicName;
    private String currentAmbientName;

    private Music currentMusic;
    private Music currentAmbient;

    private HashMap<String, Music> musicMap;
    private HashMap<String, Music> ambientMap;
    private HashMap<String, Sound> soundMap;
    private HashMap<String, Long> soundCooldownMap;

    private Preferences prefs;

    private SoundManager() {
        musicMap = new HashMap<>();
        soundMap = new HashMap<>();
        ambientMap = new HashMap<>();
        soundCooldownMap = new HashMap<>();
        prefs = Gdx.app.getPreferences("GameSettings");

        // Load preferences with defaults
        musicVolume = prefs.getFloat("musicVol", 1.0f);
        sfxVolume = prefs.getFloat("sfxVol", 1.0f);
        ambientVolume = prefs.getFloat("ambientVol", 1.0f); // Now saving/loading ambient
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // --- MUSIC HANDLING (BGM) ---
    public void playMusic(String fileName) {
        // Optimization: If the requested song is already playing, do nothing.
        if (currentMusic != null && currentMusic.isPlaying() && fileName.equals(currentMusicName)) {
            return;
        }

        // Stop previous music if valid
        if (currentMusic != null) {
            currentMusic.stop();
        }

        Music music = musicMap.get(fileName);
        if (music == null) {
            try {
                music = Gdx.audio.newMusic(Gdx.files.internal("Audio/song/" + fileName));
                musicMap.put(fileName, music);
            } catch (Exception e) {
                Gdx.app.error("AUDIO", "Failed to load music: " + fileName, e);
                return;
            }
        }

        currentMusic = music;
        currentMusicName = fileName; // Track the name
        currentMusic.setLooping(true);
        currentMusic.setVolume(musicVolume);
        currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusicName = null;
        }
    }

    // --- AMBIENT HANDLING ---
    public void playAmbient(String fileName) {
        // Optimization: If the requested ambient is already playing, do nothing.
        if (currentAmbient != null && currentAmbient.isPlaying() && fileName.equals(currentAmbientName)) {
            return;
        }

        if (currentAmbient != null) {
            currentAmbient.stop();
        }

        Music ambient = ambientMap.get(fileName);
        if (ambient == null) {
            try {
                ambient = Gdx.audio.newMusic(Gdx.files.internal("Audio/ambients/" + fileName));
                ambientMap.put(fileName, ambient);
            } catch (Exception e) {
                Gdx.app.error("AUDIO", "Failed to load ambient: " + fileName, e);
                return;
            }
        }

        currentAmbient = ambient;
        currentAmbientName = fileName;
        currentAmbient.setLooping(true);
        currentAmbient.setVolume(ambientVolume);
        currentAmbient.play();
    }

    public void stopAmbient() {
        if (currentAmbient != null) {
            currentAmbient.stop();
            currentAmbientName = null;
        }
    }

    // --- SFX HANDLING ---
    public long playSound(String fileName) {
        return playSound(fileName, 1.0f);
    }

    public long playSound(String fileName, float volumeScale) {
        Sound sound = getSound(fileName);
        if (sound != null) {
            // Clamp final volume between 0 and 1
            float finalVolume = Math.max(0f, Math.min(1f, sfxVolume * volumeScale));
            return sound.play(finalVolume);
        }
        return -1;
    }

    public void playSoundWithCooldown(String fileName, float cooldownSeconds) {
        long currentTime = TimeUtils.millis();
        long lastTime = soundCooldownMap.getOrDefault(fileName, 0L);

        if (currentTime - lastTime > (cooldownSeconds * 1000)) {
            playSound(fileName);
            soundCooldownMap.put(fileName, currentTime);
        }
    }

    public long loopSound(String fileName) {
        Sound sound = getSound(fileName);
        if (sound != null) {
            return sound.loop(sfxVolume);
        }
        return -1;
    }

    public void stopSound(String fileName, long soundId) {
        Sound sound = soundMap.get(fileName);
        if (sound != null) {
            sound.stop(soundId);
        }
    }

    public void updateSoundVolume(String fileName, long soundId, float volume) {
        Sound sound = soundMap.get(fileName);
        if (sound != null) {
            sound.setVolume(soundId, volume);
        }
    }

    private Sound getSound(String fileName) {
        Sound sound = soundMap.get(fileName);
        if (sound == null) {
            try {
                sound = Gdx.audio.newSound(Gdx.files.internal("Audio/sfx/" + fileName));
                soundMap.put(fileName, sound);
            } catch (Exception e) {
                Gdx.app.error("AUDIO", "Failed to load sfx: " + fileName, e);
                return null;
            }
        }
        return sound;
    }

    // --- SETTERS & GETTERS ---
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume)); // Clamp
        if (currentMusic != null) currentMusic.setVolume(this.musicVolume);
        prefs.putFloat("musicVol", this.musicVolume);
        prefs.flush();
    }

    public void setSFXVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume)); // Clamp
        prefs.putFloat("sfxVol", this.sfxVolume);
        prefs.flush();
    }

    public void setAmbientVolume(float volume) {
        this.ambientVolume = Math.max(0f, Math.min(1f, volume)); // Clamp
        if (currentAmbient != null) {
            currentAmbient.setVolume(this.ambientVolume);
        }
        prefs.putFloat("ambientVol", this.ambientVolume); // Save to prefs
        prefs.flush();
    }

    public void stopAllAudio() {
        // Matikan Musik BGM
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusicName = null; // Reset nama agar bisa di-play ulang nanti
        }

        // Matikan Ambient
        if (currentAmbient != null) {
            currentAmbient.stop();
            currentAmbientName = null;
        }
    }

    public float getMusicVolume() { return musicVolume; }
    public float getSfxVolume() { return sfxVolume; }
    public float getAmbientVolume() { return ambientVolume; }

    @Override
    public void dispose() {
        for (Music m : musicMap.values()) m.dispose();
        for (Music a : ambientMap.values()) a.dispose();
        for (Sound s : soundMap.values()) s.dispose();
        musicMap.clear();
        ambientMap.clear();
        soundMap.clear();
    }
}
