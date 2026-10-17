package br.edu.uemg.cannongame;

public final class Cannon {
    public final float centerY,baseRadius,barrelLength,barrelWidth,ballRadius,ballSpeed;
    public double angle;
    private Cannonball cannonball;
    public Cannon(float width,float height) {
        centerY=height/2; baseRadius=height*3/40; barrelLength=width/10;
        barrelWidth=height*3/40; ballRadius=height*3/80; ballSpeed=width*1.5f;
    }
    public void align(float touchX,float touchY) {
        angle=Math.atan2(touchY-centerY,Math.max(1,touchX));
        angle=Math.max(-Math.PI/2+0.05,Math.min(Math.PI/2-0.05,angle));
    }
    public boolean fireCannonball() {
        if (cannonball!=null) return false;
        float dx=(float)Math.cos(angle),dy=(float)Math.sin(angle);
        cannonball=new Cannonball(dx*barrelLength,centerY+dy*barrelLength,
            ballRadius,dx*ballSpeed,dy*ballSpeed);
        return true;
    }
    public Cannonball getCannonball() { return cannonball; }
    public void removeCannonball() { cannonball=null; }
}
