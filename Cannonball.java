package br.edu.uemg.cannongame;

public final class Cannonball {
    public float x,y,velocityX,velocityY;
    public final float radius;
    public boolean reflected;
    public Cannonball(float x,float y,float radius,float vx,float vy) {
        this.x=x; this.y=y; this.radius=radius; velocityX=vx; velocityY=vy;
    }
    public void update(float dt) { x+=velocityX*dt; y+=velocityY*dt; }
    public boolean collidesWith(GameElement element) {
        float nearestX=Math.max(element.x,Math.min(x,element.x+element.width));
        float nearestY=Math.max(element.y,Math.min(y,element.y+element.height));
        float dx=x-nearestX,dy=y-nearestY;
        return dx*dx+dy*dy <= radius*radius;
    }
    public boolean isOnScreen(float width,float height) {
        return x+radius>=0 && x-radius<=width && y+radius>=0 && y-radius<=height;
    }
    public void reverseVelocityX() { velocityX=-Math.abs(velocityX); reflected=true; }
}
