package br.edu.uemg.cannongame;
import java.util.Random;

/** Testes determinísticos, executáveis também sem Android e sem JUnit. */
public final class GameEngineChecks {
    private static int checks;
    private static final class Events implements GameEngine.Events {
        int fires,targets,blocks;
        public void fire(){fires++;}
        public void targetHit(){targets++;}
        public void blockerHit(){blocks++;}
    }
    private static void check(boolean value,String name) {
        checks++; if(!value) throw new AssertionError(name);
    }
    private static void near(double actual,double expected,double tolerance,String name) {
        check(Math.abs(actual-expected)<tolerance,name+": "+actual+" != "+expected);
    }
    private static GameEngine game(Events e) { return new GameEngine(1000,500,new Random(42),e); }
    private static void hit(GameEngine g) {
        Target target=g.targets.get(0); target.velocityY=0;
        g.cannon.fireCannonball();
        Cannonball ball=g.cannon.getCannonball();
        ball.x=target.x+target.width/2; ball.y=target.y+target.height/2;
        ball.velocityX=0; ball.velocityY=0;
        g.update(0.001f);
    }
    public static void runAll() {
        checks=0;
        Events e=new Events(); GameEngine g=game(e);
        check(g.targets.size()==9,"9 alvos iniciais"); near(g.timeLeft,10,0.001,"tempo inicial");
        g.aimAndFire(800,250); g.aimAndFire(700,200);
        check(g.shotsFired==1 && e.fires==1,"um projétil por vez");
        g.cannon.removeCannonball();
        hit(g); check(g.hits==1 && g.targets.size()==8,"remoção do alvo");
        near(g.timeLeft,12.999,0.0001,"bônus de 3 segundos");
        check(g.score==100 && e.targets==1,"pontuação e som do alvo");
        hit(g); check(g.score==300 && g.multiplier()==2,"multiplicador");
        g.cannon.fireCannonball(); Cannonball ball=g.cannon.getCannonball();
        g.blocker.velocityY=0;
        ball.x=g.blocker.x-ball.radius+1; ball.y=g.blocker.y+20;
        ball.velocityX=50; ball.velocityY=0;
        double before=g.timeLeft; g.update(0.001f);
        check(ball.velocityX<0 && ball.reflected,"reflexão no bloqueador");
        near(g.timeLeft,before-2.001,0.0001,"penalidade de 2 segundos");
        check(g.streak==0 && e.blocks==1,"quebra da sequência e som");
        g.update(0.001f); check(e.blocks==1,"não penaliza duas vezes no mesmo contato");
        g.cannon.removeCannonball();
        while(!g.targets.isEmpty()) hit(g);
        check(g.finished && g.won && g.hits==9,"vitória após nove alvos");
        double end=g.timeLeft; g.update(1); near(g.timeLeft,end,0.0001,"fim congela relógio");
        g=game(new Events()); g.update(10.1f);
        check(g.finished && !g.won && g.timeLeft==0,"derrota e limite zero");
        g=game(new Events()); g.streak=3; g.aimAndFire(900,250);
        ball=g.cannon.getCannonball(); ball.x=-100; ball.velocityX=-1;
        g.update(0.001f);
        check(g.cannon.getCannonball()==null && g.streak==0,"tiro fora da tela");
        GameElement rect=new GameElement(10,10,10,10,0,0);
        ball=new Cannonball(7,7,4,0,0);
        check(!ball.collidesWith(rect),"canto fora do círculo");
        ball.x=6; ball.y=15; check(ball.collidesWith(rect),"tangência");
        rect.velocityY=-40; rect.update(0.5f,100);
        near(rect.y,10,0.001,"reflexão na borda"); check(rect.velocityY>0,"direção após reflexão");
        GameEngine a=game(new Events()),b=game(new Events());
        for(int i=0;i<60;i++) a.update(1f/60);
        for(int i=0;i<120;i++) b.update(1f/120);
        near(a.targets.get(0).y,b.targets.get(0).y,0.02,"movimento independente de FPS");
        near(a.timeLeft,b.timeLeft,0.002,"relógio independente de FPS");
        g=game(new Events()); g.blocker.y=0; g.blocker.height=10; g.blocker.velocityY=0;
        Target t=g.targets.get(0); t.velocityY=0;
        g.aimAndFire(900,250); ball=g.cannon.getCannonball();
        ball.x=t.x-60; ball.y=t.y+20; ball.velocityX=2000; ball.velocityY=0;
        g.update(0.05f); check(g.hits==1,"colisão durante frame longo");
        g=game(new Events()); for(int i=0;i<7;i++) hit(g);
        check(g.multiplier()==5 && g.score==2500,"multiplicador limitado a cinco");
    }
    public static void main(String[] args) { runAll(); System.out.println(checks+" verificações aprovadas"); }
}
