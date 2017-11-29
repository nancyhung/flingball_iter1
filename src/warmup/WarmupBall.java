package warmup;

import physics.*;

public class WarmupBall {
    private Circle shape;
    private Vect velocity;
    
    public WarmupBall(Circle shape, Vect velocity){
        this.shape = shape;
        this.velocity = velocity;
    }
    
    public Circle getShape() {
        return shape;
    }
    public double getX() {
        return shape.getCenter().x();
    }
    public double getY() {
        return shape.getCenter().y();
    }
    public double getRadius() {
        return shape.getRadius();
    }
    public Vect getVelocity() {
        return velocity;
    }
    public double getXVel() {
        return velocity.x();
    }
    public double getYVel() {
        return velocity.y();
    }
    
    public void updateCenter(double timestep) {
        Vect newCenter = shape.getCenter().plus(velocity.times(timestep));
        double radius = shape.getRadius();
        shape = new Circle(newCenter,radius);
    }
    public void updateCenter(Vect location) {
        double radius = shape.getRadius();
        shape = new Circle(location,radius);
    }
    public void updateVelocity(Vect newVelocity) {
        velocity = newVelocity;
    }
}