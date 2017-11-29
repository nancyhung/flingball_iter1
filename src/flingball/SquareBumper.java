package flingball;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import physics.Circle;
import physics.LineSegment;
import physics.Physics;
import physics.Vect;

/**
 * A mutable SquareBumper gadget representing a square flingball board component that can deflect balls 
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
public class SquareBumper implements GameExpression {
    
    private final String name;
    private final Set<LineSegment> sides;
    private final Set<Vect> endpoints;
    private final Vect origin;
    private final int sideLength = 1;
    private GameExpression triggerTarget = null;
    private final Color color = Color.red;
    
    // Abstraction function:
    // AF(name, sides, endpoints, origin, sideLength, triggerTarget, color) = a square bumper gadget with
    // the unique identifier name, sides corresponding to the LineSegments in sides, which are of length sideLength,
    // and have endpoints corresponding to the Vects in endpoints. The bumper is spatially located on the flingball 
    // board in the bounding box whose top left corner is at origin. The bumper triggers the gadget triggerTarget 
    // if it has one, otherwise it does nothing when hit by a ball. It has the color color.
    // 
    // Rep invariant:
    // - name is not the empty string
    // - sides is of size 4
    // - endpoints is of size 4
    // - the endpoints of the LineSegments in size all correspond to Vects in endpoints
    // - origin is on the board
    //
    // Safety from rep exposure argument:
    // - all instance fields are private and final, apart from triggerTarget, which must be resettable to compatible
    //   with our parser
    // - name and origin are immutable, so public getter methods cannot mutate them
    // - getter method for sides returns an unmodifiable view of that set
    // - getter method for location returns defensive copy of location
    
    private void checkRep() {
        assert !this.name.equals("");
        assert this.sides.size()==4;
        assert this.endpoints.size()==4;
        assert 0 <= this.origin.x() && this.origin.x() <= 20;
        assert 0 <= this.origin.y() && this.origin.y() <= 20;
        for (LineSegment l : this.sides) {
            assert this.endpoints.contains(l.p1());
            assert this.endpoints.contains(l.p2());
        }
    }
    
    /**
     * Construct a SquareBumper with given name at the specified location.
     * @param name unique identifying name of this SquareBumper
     * @param x x coordinate of origin reference point of absorber (top left corner of its bounding box in
     *        the flingball board)
     * @param y y coordinate of origin reference point
     */
    public SquareBumper(String name, int x, int y) {
        this.name = name;
        this.origin = new Vect((double) x, (double) y);
        LineSegment top = new LineSegment(origin.x(), origin.y(), origin.x()+sideLength, origin.y());
        LineSegment left = new LineSegment(origin.x(), origin.y(), origin.x(), origin.y()+sideLength);
        LineSegment right = new LineSegment(origin.x()+sideLength, origin.y(), origin.x()+sideLength, origin.y()+sideLength);
        LineSegment bottom = new LineSegment(origin.x(), origin.y()+sideLength, origin.x()+sideLength, origin.y()+sideLength);
        this.sides = new HashSet<>();
        this.sides.addAll(Arrays.asList(top, left, right, bottom));
        this.endpoints = new HashSet<>();
        for (LineSegment side : sides) {
            this.endpoints.add(side.p1());
            this.endpoints.add(side.p2());
        }
        checkRep();
    }
    
    @Override public double timeUntilCollision(Ball ball) {
        double minTime = Double.MAX_VALUE/2;
        for (LineSegment side : this.sides) {
            double collisionTime = Physics.timeUntilWallCollision(side, ball.getBallCircle(), ball.getVelocity());
            if (collisionTime < minTime) {
                minTime = collisionTime;
            }
        }
        for (Vect endpoint : this.endpoints) {
            double collisionTime = Physics.timeUntilCircleCollision(new Circle(endpoint,0), ball.getBallCircle(), ball.getVelocity());
            if (collisionTime < minTime) {
                minTime = collisionTime;
            }
        }
        return minTime;
    }
    
    @Override public void collision(Ball ball) {
        System.out.println(ball.getName() + " collided with square bumper " + this.getName());
        double minTime = Double.MAX_VALUE/2;
        Vect reflectionVelocity = null;
        for (LineSegment side : this.sides) {
            double collisionTime = Physics.timeUntilWallCollision(side, ball.getBallCircle(), ball.getVelocity());
            if (collisionTime < minTime) {
                minTime = collisionTime;
                reflectionVelocity = Physics.reflectWall(side, ball.getVelocity());
            }
        }
        for (Vect endpoint : this.endpoints) {
            double collisionTime = Physics.timeUntilCircleCollision(new Circle(endpoint,0), ball.getBallCircle(), ball.getVelocity());
            if (collisionTime < minTime) {
                minTime = collisionTime;
                reflectionVelocity = Physics.reflectCircle(endpoint, ball.getLocation(), ball.getVelocity());
            }
        }
        ball.setVelocity(reflectionVelocity);
        trigger();
    }

    @Override public String getName() {
        return this.name;
    }
    
    @Override public Color getColor() {
        return this.color;
    }
    
    @Override public boolean trigger() {
        if (this.triggerTarget != null) {
            this.triggerTarget.action();
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
    
    @Override public boolean equals(Object that) {
        SquareBumper thatSquareBumper = (SquareBumper) that;
        return that instanceof SquareBumper && this.name.equals(thatSquareBumper.getName())
                && origin.equals(thatSquareBumper.origin); 
        
    }

    @Override public Vect getLocation() {
        return new Vect(this.origin.x(), this.origin.y());
    }
    
    @Override public Set<LineSegment> getSides() {
        return Collections.unmodifiableSet(this.sides);
    }

    @Override public boolean action() {
        return false;
    }

    @Override public int hashCode() {
        return this.name.hashCode();
    }
}
