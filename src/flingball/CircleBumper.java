package flingball;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import physics.Circle;
import physics.LineSegment;
import physics.Physics;
import physics.Vect;

/**
 * A mutable CircleBumper gadget representing a circular flingball board component that can deflect balls 
 * that collide with it. 
 * We debated making all the bumpers immutable because the only property that changes throughout the game
 * for a bumper is whether it is triggered or if it sets the trigger for another game object. We decided to 
 * stick with a mutable representation and have preempted bugs that are associated with mutable objects (i.e. having
 * references to other mutable objects and changing them accidentally) in the following strategies:
 *   - all getter methods that involve mutable objects make defensive copies of their inputs
 *   - all setter methods that involve returning mutable objects make defensive copies of the class's object
 *   - the mutable set trigger operation can only be performed once in the lifetime of the game 
 *     (for 1 object to trigger another object).
 * This is safe from threading and race conditions because our board parser requires as a precondition that
 * no object can set trigger to more than one object. Thus, since board and simulation handle objects one at a time,
 * we will not encounter any threading problems in this iteration. After discussion with TAs and Professor Goldman,
 * we will consider changing our design to encompass an immutable Trigger class that makes an immutable reference to the
 * 2 objects involved (1 cause, 1 effect) for the next iteration.
 */
public class CircleBumper implements GameExpression {
    
    private final String name;
    private final Circle bumperCircle;
    private final double radius = 0.5;
    private GameExpression triggerTarget = null;
    private final Color color = Color.red;
    
    // Abstraction function:
    // AF(name, bumperCircle, radius, triggerTarget, color) = a circular bumper gadget with the unique identifier name,
    // radius length equal to radius, and whose color is color. When hit by a ball, the bumper causes the action of its
    // triggerTarget to run, if it has a triggerTarget.
    //
    // Rep invariant:
    // - name is not the empty string
    // - radius > 0
    // - bumperCircle is located on the flingball board
    //
    // Safety from rep exposure argument:
    // - name and radius are private, final, and immutable types
    // - triggerTarget is private, but must remain mutable to maintain CircleBumper's functionality
    // - getLocation returns defensive copy of ball's position Vect
    
    private final int boardSize = 20; // constant as outlined by project spec
    
    private void checkRep() {
        assert !this.name.equals("");
        assert this.radius > 0;
        assert this.bumperCircle.getCenter().x() >= 0 + this.radius;
        assert this.bumperCircle.getCenter().y() <= boardSize - this.radius;
        assert this.bumperCircle.getCenter().x() >= 0 + this.radius;
        assert this.bumperCircle.getCenter().y() <= boardSize - this.radius;
    }
 
    
    /**
     * Construct a CircleBumper at the specified location.
     * @param name unique identifying name of this CircleBumper
     * @param x x coordinate of center of this CircleBumper
     * @param y y coordinate of center of this CircleBumper
     */
    public CircleBumper(String name ,int x, int y) { 
        this.name = name;
        this.bumperCircle = new Circle((double) x+radius, (double) y+radius, radius);
        checkRep();
    }

    @Override public String getName() {
        return this.name;
    }

    @Override public Vect getLocation() {
        return new Vect(this.bumperCircle.getCenter().x(), this.bumperCircle.getCenter().y());
    }
    
    @Override public Set<LineSegment> getSides() {
        return new HashSet<>();
    }
    
    @Override public Color getColor() {
        return this.color;
    }

    @Override public boolean equals(Object that) {
        CircleBumper thatCircleBumper = (CircleBumper) that;
        return that instanceof CircleBumper && this.name.equals(thatCircleBumper.getName())
                && this.getLocation().equals(thatCircleBumper.getLocation()); 
    }

    @Override public double timeUntilCollision(Ball ball) {
        return Physics.timeUntilCircleCollision(this.bumperCircle, ball.getBallCircle(), ball.getVelocity());
    }

    @Override public void collision(Ball ball) {
        ball.setVelocity(Physics.reflectCircle(this.bumperCircle.getCenter(), ball.getLocation(), ball.getVelocity()));
        trigger();
        checkRep();
    }

    @Override public boolean trigger() {
        if (this.triggerTarget != null) {
            this.triggerTarget.action();
            checkRep();
            return true;
        }
        return false;
    }

    @Override public boolean setTrigger(GameExpression target) {
        if (this.triggerTarget == null) {
            this.triggerTarget = target;
            checkRep();
            return true;
        }
        return false; 
    }
    
    @Override public boolean action() {
        return false;
    }

    @Override public int hashCode() {
        return this.name.hashCode();
    }

}
