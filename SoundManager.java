package br.edu.uemg.cannongame;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Carregamento assíncrono; nunca tenta tocar um som ainda não carregado. */
public final class SoundManager implements GameEngine.Events {
    private final SoundPool pool;
    private final Set<Integer> loaded=ConcurrentHashMap.newKeySet();
    private final int fireId,targetId,blockerId;
    public SoundManager(Context context) {
        AudioAttributes attributes=new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        pool=new SoundPool.Builder().setMaxStreams(3).setAudioAttributes(attributes).build();
        pool.setOnLoadCompleteListener((soundPool,id,status)->{if(status==0) loaded.add(id);});
        fireId=pool.load(context,R.raw.cannon_fire,1);
        targetId=pool.load(context,R.raw.target_hit,1);
        blockerId=pool.load(context,R.raw.blocker_hit,1);
    }
    private void play(int id) { if(loaded.contains(id)) pool.play(id,1,1,1,0,1); }
    @Override public void fire() { play(fireId); }
    @Override public void targetHit() { play(targetId); }
    @Override public void blockerHit() { play(blockerId); }
    public void pause() { pool.autoPause(); }
    public void release() { pool.release(); loaded.clear(); }
}
