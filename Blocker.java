package br.edu.uemg.cannongame;

public final class Blocker extends GameElement {
    public static final int MISS_PENALTY = 2;
    public Blocker(float x,float y,float width,float height,float speed) {
        super(x,y,width,height,speed,0xff263238);
    }
}
