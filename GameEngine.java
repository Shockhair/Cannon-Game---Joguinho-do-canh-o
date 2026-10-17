package br.edu.uemg.cannongame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Toda a simulação é alterada apenas pela thread do jogo. */
public final class GameEngine {
    public interface Events { void fire(); void targetHit(); void blockerHit(); }
    public final float width,height;
    public final Cannon cannon;
    public final Blocker blocker;
    public final List<Target> targets=new ArrayList<>();
    public double timeLeft=10,elapsed;
    public int shotsFired,hits,score,streak;
    public boolean finished,won;
    private final Events events;
    public GameEngine(float width,float height,Random random,Events events) {
        this.width=width; this.height=height; this.events=events;
        cannon=new Cannon(width,height);
        blocker=new Blocker(width/2,height*0.375f,width/40,height/4,height);
        for(int i=0;i<9;i++) {
            float speed=height*(0.75f+random.nextFloat()*0.75f)*(i%2==0?1:-1);
            targets.add(new Target(width*(0.6f+i*(1f/40+1f/60)),height*0.425f,
                width/40,height*0.15f,speed,i%2==0?0xff00796b:0xff80cbc4));
        }
    }
    public void aimAndFire(float x,float y) {
        if(finished) return;
        cannon.align(x,y);
        if(cannon.fireCannonball()) { shotsFired++; events.fire(); }
    }
    public int multiplier() { return Math.max(1,Math.min(5,streak)); }
    public void update(float dt) {
        if(finished || dt<=0) return;
        // Subpassos evitam que um projétil atravesse alvos entre dois frames.
        int steps=Math.max(1,(int)Math.ceil(dt/(1f/480)));
        float step=dt/steps;
        for(int s=0;s<steps && !finished;s++) simulate(step);
    }
    private void simulate(float dt) {
        elapsed+=dt; timeLeft-=dt;
        if(timeLeft<=0) { timeLeft=0; finished=true; return; }
        blocker.update(dt,height);
        for(Target target:targets) target.update(dt,height);
        Cannonball ball=cannon.getCannonball();
        if(ball!=null) {
            ball.update(dt);
            if(!ball.reflected && ball.collidesWith(blocker)) {
                ball.reverseVelocityX();
                ball.x=blocker.x-ball.radius-0.01f;
                timeLeft=Math.max(0,timeLeft-Blocker.MISS_PENALTY);
                streak=0; events.blockerHit();
            } else {
                for(int i=0;i<targets.size();i++) {
                    if(ball.collidesWith(targets.get(i))) {
                        targets.remove(i); cannon.removeCannonball();
                        hits++; streak++; score+=100*multiplier();
                        timeLeft+=Target.HIT_REWARD; events.targetHit(); break;
                    }
                }
            }
            if(cannon.getCannonball()!=null && !ball.isOnScreen(width,height)) {
                cannon.removeCannonball(); streak=0;
            }
        }
        if(targets.isEmpty()) { won=true; finished=true; }
        else if(timeLeft<=0) { timeLeft=0; finished=true; }
    }
}
