package br.edu.uemg.cannongame;

/** Retângulo móvel, independente de Android para permitir testes de física. */
public class GameElement {
    public float x, y, width, height, velocityY;
    public final int color;
    public GameElement(float x, float y, float width, float height, float velocityY, int color) {
        this.x=x; this.y=y; this.width=width; this.height=height;
        this.velocityY=velocityY; this.color=color;
    }
    public void update(float dt, float arenaHeight) {
        y += velocityY * dt;
        float limit = Math.max(0, arenaHeight - height);
        if (limit == 0) { y=0; return; }
        // Reflete o excesso: não deixa o objeto preso à borda.
        while (y < 0 || y > limit) {
            if (y < 0) { y=-y; velocityY=Math.abs(velocityY); }
            if (y > limit) { y=2*limit-y; velocityY=-Math.abs(velocityY); }
        }
    }
}
