package br.edu.uemg.cannongame;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import java.util.concurrent.locks.LockSupport;

/** Loop manual: tempo real -> física -> Canvas -> espera pelo próximo frame. */
public final class CannonThread extends Thread {
    private final CannonView view;
    private final SurfaceHolder holder;
    private volatile boolean running=true;
    public CannonThread(CannonView view,SurfaceHolder holder) {
        super("CannonThread"); this.view=view; this.holder=holder;
    }
    public void requestStop() { running=false; interrupt(); }
    @Override public void run() {
        long previous=System.nanoTime();
        while(running) {
            long now=System.nanoTime();
            float dt=Math.min((now-previous)/1_000_000_000f,0.05f);
            previous=now;
            view.updateGame(dt);
            Canvas canvas=null;
            try {
                if(holder.getSurface().isValid()) {
                    canvas=holder.lockCanvas();
                    if(canvas!=null) view.drawGame(canvas);
                }
            } finally { if(canvas!=null) holder.unlockCanvasAndPost(canvas); }
            long remaining=16_666_667L-(System.nanoTime()-now);
            if(remaining>0 && running) LockSupport.parkNanos(remaining);
        }
    }
}
