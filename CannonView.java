package br.edu.uemg.cannongame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public final class CannonView extends SurfaceView implements SurfaceHolder.Callback {
    private final Activity activity;
    private final SoundManager sounds;
    private final SharedPreferences preferences;
    private final Handler mainHandler=new Handler(Looper.getMainLooper());
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    // O toque é transferido atomicamente: a UI não altera os objetos da física.
    private final AtomicReference<float[]> pendingTouch=new AtomicReference<>();
    private CannonThread thread;
    private GameEngine engine;
    private AlertDialog dialog;
    private boolean resumed,surfaceReady,started,resultPosted,released;
    private volatile boolean playing;
    private float offsetX,offsetY,arenaWidth,arenaHeight;
    public CannonView(Activity activity) {
        super(activity); this.activity=activity;
        sounds=new SoundManager(activity);
        preferences=activity.getSharedPreferences("cannon_records",Context.MODE_PRIVATE);
        getHolder().addCallback(this); setFocusable(true);
        setContentDescription("Cannon Game. Toque na área do jogo para mirar e disparar.");
    }
    public void resumeGame() {
        resumed=true;
        if(surfaceReady && !released) {
            if(!started) showWelcome();
            else if(engine!=null && engine.finished) showResult();
            else startThread();
        }
    }
    public void pauseGame() {
        resumed=false; stopThread(); pendingTouch.set(null); sounds.pause();
        if(dialog!=null) { dialog.dismiss(); dialog=null; }
    }
    private void startThread() {
        if(!released && resumed && surfaceReady && playing && thread==null) {
            thread=new CannonThread(this,getHolder()); thread.start();
        }
    }
    private void stopThread() {
        CannonThread old=thread;
        if(old!=null) {
            old.requestStop();
            // join garante que a Surface não seja destruída durante um desenho.
            boolean interrupted=false;
            while(old.isAlive()) {
                try { old.join(); } catch(InterruptedException e) { interrupted=true; }
            }
            thread=null;
            if(interrupted) Thread.currentThread().interrupt();
        }
    }
    private void newGame() {
        stopThread(); pendingTouch.set(null);
        engine=new GameEngine(arenaWidth,arenaHeight,new Random(),sounds);
        resultPosted=false; started=true; playing=true; startThread();
    }
    private void showWelcome() {
        if(!resumed || dialog!=null || released) return;
        dialog=new AlertDialog.Builder(activity).setTitle(R.string.app_name)
            .setMessage(R.string.help).setCancelable(false)
            .setPositiveButton(R.string.start,(d,w)->{dialog=null; newGame();})
            .setNegativeButton(R.string.exit,(d,w)->activity.finish()).create();
        dialog.show();
    }
    private void showResult() {
        if(released || !resumed || dialog!=null || engine==null || !engine.finished) return;
        stopThread();
        int record=Math.max(preferences.getInt("high_score",0),engine.score);
        preferences.edit().putInt("high_score",record).apply();
        float accuracy=engine.shotsFired==0?0:100f*engine.hits/engine.shotsFired;
        String result=getResources().getString(R.string.results,engine.score,record,
            engine.hits,engine.shotsFired,accuracy,engine.elapsed);
        dialog=new AlertDialog.Builder(activity).setTitle(engine.won?R.string.win:R.string.lose)
            .setMessage(result).setCancelable(false)
            .setPositiveButton(R.string.restart,(d,w)->{dialog=null; newGame();})
            .setNegativeButton(R.string.exit,(d,w)->activity.finish()).create();
        dialog.show();
    }
    void updateGame(float dt) {
        if(engine==null || !playing) return;
        float[] touch=pendingTouch.getAndSet(null);
        if(touch!=null) engine.aimAndFire(touch[0],touch[1]);
        engine.update(dt);
        if(engine.finished && !resultPosted) {
            playing=false; resultPosted=true;
            mainHandler.post(this::showResult);
        }
    }
    void drawGame(Canvas canvas) {
        canvas.drawColor(0xfff4f7f7);
        if(engine==null) return;
        canvas.save(); canvas.translate(offsetX,offsetY);
        canvas.clipRect(0,0,arenaWidth,arenaHeight);
        paint.setColor(0xffffffff); canvas.drawRect(0,0,arenaWidth,arenaHeight,paint);
        drawElement(canvas,engine.blocker);
        for(Target target:engine.targets) drawElement(canvas,target);
        Cannon cannon=engine.cannon;
        paint.setColor(0xff37474f); paint.setStrokeWidth(cannon.barrelWidth);
        canvas.drawLine(0,cannon.centerY,(float)Math.cos(cannon.angle)*cannon.barrelLength,
            cannon.centerY+(float)Math.sin(cannon.angle)*cannon.barrelLength,paint);
        paint.setColor(0xff00796b); canvas.drawCircle(0,cannon.centerY,cannon.baseRadius,paint);
        Cannonball ball=cannon.getCannonball();
        if(ball!=null) { paint.setColor(0xff263238); canvas.drawCircle(ball.x,ball.y,ball.radius,paint); }
        canvas.restore();
        float textSize=Math.min(getHeight()*0.055f,getWidth()*0.025f);
        paint.setTextSize(textSize); paint.setColor(0xff163b36);
        String hud=getResources().getString(R.string.hud,engine.timeLeft,engine.hits,engine.score,engine.multiplier());
        canvas.drawText(hud,offsetX,offsetY*0.68f,paint);
        paint.setTextSize(textSize*0.67f); paint.setColor(0xff546e7a);
        canvas.drawText(getResources().getString(R.string.hint),offsetX,getHeight()-8,paint);
    }
    private void drawElement(Canvas canvas,GameElement element) {
        paint.setColor(element.color);
        canvas.drawRect(element.x,element.y,element.x+element.width,element.y+element.height,paint);
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        if(event.getActionMasked()==MotionEvent.ACTION_DOWN || event.getActionMasked()==MotionEvent.ACTION_MOVE) {
            float x=event.getX()-offsetX,y=event.getY()-offsetY;
            if(playing && x>=0 && x<=arenaWidth && y>=0 && y<=arenaHeight)
                pendingTouch.set(new float[]{x,y});
            return true;
        }
        if(event.getActionMasked()==MotionEvent.ACTION_UP) { performClick(); return true; }
        return true;
    }
    @Override public boolean performClick() { super.performClick(); return true; }
    @Override public void surfaceCreated(SurfaceHolder holder) { /* Aguarda dimensões em surfaceChanged. */ }
    @Override public void surfaceChanged(SurfaceHolder holder,int format,int width,int height) {
        stopThread(); surfaceReady=true;
        float newWidth=width*0.96f,newHeight=height*0.78f;
        boolean resized=engine!=null && (Math.abs(arenaWidth-newWidth)>1 || Math.abs(arenaHeight-newHeight)>1);
        offsetX=width*0.02f; offsetY=height*0.13f; arenaWidth=newWidth; arenaHeight=newHeight;
        if(resized) {
            // Mudança de janela invalida coordenadas anteriores; inicia uma nova rodada com aviso.
            if(dialog!=null) { dialog.dismiss(); dialog=null; }
            started=false; playing=false; resultPosted=false; engine=null;
        }
        if(resumed) {
            if(!started) showWelcome();
            else if(engine.finished) showResult();
            else startThread();
        }
    }
    @Override public void surfaceDestroyed(SurfaceHolder holder) { surfaceReady=false; stopThread(); }
    public void releaseResources() {
        pauseGame(); released=true; mainHandler.removeCallbacksAndMessages(null); sounds.release();
    }
}
