package flingball;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import physics.Circle;
import physics.LineSegment;
import physics.Vect;

/**
 * A mutable ball component of a flingball board. Can move and collide with other non-ball
 * gadgets on the board. Ball must be mutable because its position, velocity, and state (i.e. triggered)
 * must be constantly changing throughout the game, so it would be more computationally efficient
 * to make it mutable. We ensure through our rep safety argument that this implementation is safe from bugs.
 * See safety from rep exposure for details.
 */
public class Ball implements GameExpression {

    private final double radius = 0.25;
    private final int boardSize = 20;
    private final String name;
    private Circle ballCircle;
    private Vect velocity;
    private GameExpression nextCollision = null;
    private double nextCollisionTime = Long.MAX_VALUE/2;
    private final Color color = Color.blue;
    private boolean absorbed = false;
    
    // Abstraction function:
    // AF(radius, name, ballCircle, velocity, nextCollision, nextCollisionTime, color, absorbed) = a ball with
    // radius equal to radius and unique identifier name, represented by the circle ballCircle, traveling at
    // velocity on the flingball board. The next gadget or wall the ball will collide with is nextCollision, and it
    // will collide at nextCollisionTime. It is currently absorbed by an absorber if absorbed is true. Its color is
    // color.
    //
    // Rep invariant:
    // - radius > 0
    // - name is not the empty string
    // - nextCollisionTime cannot be negative 
    // - ballCircle is located on the flingball board 
    //
    // Safety from rep exposure argument:
    // - radius and name are private, final, and immutable
    // - color is private and final
    // - nextCollisionTime, nextCollisionTime, and absorbed are private, but must be mutable to 
    //   maintain the ball's functionality
    // - ballCircle is private, but must be reassignable to maintain the ball's functionality
    // - All observer and mutator methods (setLocation, getVelocity, and setVelocity) make defensive copies of
    //   the input vector or the rep from this class as applicable depending on the method so client 
    //   can't modify this class's rep
    
    private void checkRep() {
        assert this.radius > 0;
        assert !this.name.equals("");
        assert this.nextCollisionTime >= 0;
        assert this.ballCircle.getCenter().x() >= 0 + this.radius;
        assert this.ballCircle.getCenter().x() <= boardSize - this.radius;
        assert this.ballCircle.getCenter().y() >= 0 + this.radius;
        assert this.ballCircle.getCenter().y() <= boardSize - this.radius;
    }
    
    /**
     * Construct a ball with the given center coordinates and velocity.
     * @param name unique identifying name of this ball
     * @param x x coordinate of center of ball
     * @param y y coordinate of center of ball
     * @param xVelocity initial velocity of ball in x direction
     * @param yVelocity initial velocity of ball in y direction
     */
    public Ball(String name, double x, double y, double xVelocity, double yVelocity) {
        this.name = name;
        this.velocity = new Vect(xVelocity, yVelocity);
        Circle circle = new Circle(new Vect(x ,y), this.radius);
        this.ballCircle = circle;
        checkRep();
    }

    /**
     * Set the next upcoming collision time (i.e. the amount of time until the next collision).
     * of this ball with the gadget it will hit. 
     * @param time next collision time
     */
    protected void setNextCollisionTime(double time) {
        this.nextCollisionTime = time;
        checkRep();
    }
    
    /**
     * Get the next upcoming collision time (i.e. the amount of time until the next collision).
     * @return next collision time
     */
    protected double getNextCollisionTime() {
        return this.nextCollisionTime;
    }
    
    /**
     * Set the next upcoming collision gadget (i.e. the gadget this ball will hit next).
     * @param gadget the gadget this ball will hit next
     */
    protected void setNextCollision(GameExpression gadget) {
        this.nextCollision = gadget;
        checkRep();
    }
    
    /**
     * Get the next upcoming collision gadget (i.e. the gadget this ball will hit next).
     * @return the gadget this ball will hit next
     */
    protected GameExpression getNextCollision() {
        return this.nextCollision;
    }
    
    /**
     * Get the Circle representation of this ball.
     * @return Circle representation of this ball
     */
    protected Circle getBallCircle() {
        checkRep();
        return this.ballCircle;
    }
    
    /**
     * Set the velocity of this ball.
     * @param newVelocity the updated velocity of this ball
     */
    public void setVelocity(Vect newVelocity) {
        this.velocity = new Vect(newVelocity.x(), newVelocity.y());
        checkRep();
    }
    
    /**
     * Get the velocity of this ball.
     * @return velocity of this ball
     */
    public Vect getVelocity() {
        checkRep();
        return new Vect(this.velocity.x(), this.velocity.y());
    }
    
    /**
     * @return returns true if ball has been absorbed by an absorber gadget, false otherwise
     */
    public boolean getAbsorbed() {
        checkRep();
        return this.absorbed;
    }
        
    /**
     * set boolean determining whether the ball can be absorbed
     * @param absorbed boolean indicating whether or not ball is absorbed
     */
    public void setAbsorbed(Boolean absorbed) {
        this.absorbed = absorbed;
        checkRep();
    }

    @Override public boolean action() {
        checkRep();
        return false;
    }

    @Override public String getName() {
        checkRep();
        return this.name;
    }

    @Override public Vect getLocation() {
        checkRep();
        return new Vect(this.ballCircle.getCenter().x(), this.ballCircle.getCenter().y());
    }
    
    @Override public Set<LineSegment> getSides() {
        return new HashSet<>();
    }
    
    @Override public Color getColor() {
        return this.color;
    }
    
    /**
     * Set the location of this ball
     * @param newLocation Vector pointing to the new location of this ball.
     */
    protected void setLocation(Vect newLocation) {
        Vect nL = new Vect(newLocation.x(), newLocation.y());
        this.ballCircle = new Circle(nL, this.radius);
        checkRep();
    }

    @Override public boolean equals(Object that) {
        Ball thatBall = (Ball) that;
        checkRep();
        return that instanceof Ball && this.name.equals(thatBall.getName())
                && this.getLocation().equals(thatBall.getLocation());
    }

    @Override public double timeUntilCollision(Ball ball) {
        checkRep();
        return Double.MAX_VALUE/2;
    }

    @Override public void collision(Ball ball) {
        checkRep();
        return;
    }

    @Override public boolean setTrigger(GameExpression target) {
        checkRep();
        return false;
    }

    @Override public boolean trigger() {
        checkRep();
        return false;
    }

    @Override public int hashCode() {
        checkRep();
        return this.name.hashCode();
    }
}
