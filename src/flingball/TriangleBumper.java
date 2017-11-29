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
 * A mutable TriangleBumper gadget representing a triangular flingball board component that can deflect balls 
 * that collide with it. Takes the shape of an isosceles right triangle. 
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
public class TriangleBumper implements GameExpression {

    private final String name;
    private final Set<LineSegment> sides;
    private final Set<Vect> endpoints;
    private final Vect origin;
    private final int sideLength = 1;
    private GameExpression triggerTarget = null;
    private final Color color = Color.orange;
    
    // Abstraction function:
    // AF(name, sides, endpoints, origin, sideLength, triggerTarget, color) = a triangular bumper gadget with
    // the unique identifier name, sides corresponding to the LineSegments in sides, which are of length sideLength, 
    // (except for the hypotenuse side, which is sqrt(2)*sideLength long) and have endpoints corresponding to the 
    // Vects in endpoints. The bumper is spatially located on the flingball board in the bounding box whose top 
    // left corner is at origin. The bumper triggers the gadget triggerTarget if it has one, otherwise it does 
    //nothing when hit by a ball. It has the color color. 
    // 
    // Rep invariant:
    // - name is not the empty string
    // - sides is of size 3
    // - endpoints is of size 3
    // - the endpoints of the LineSegments in size all correspond to Vects in endpoints
    // - origin is on the board
    //
    // Safety from rep exposure argument:
    // - all instance fields are private and final, apart from triggerTarget, which must be resettable to compatible
    //   with our parser
    // - name and origin are immutable, so public getter methods cannot mutate them
    // - getter method for sides returns an unmodifiable view of that set
    // - getter method for velocity returns defensive copy of rep
    
    private final int numberOfSides = 3;
    private final int boardSize = 20;
    private void checkRep() {
        assert !this.name.equals("");
        assert this.sides.size()==numberOfSides;
        assert this.endpoints.size()==numberOfSides;
        assert 0 <= this.origin.x() && this.origin.x() <= boardSize;
        assert 0 <= this.origin.y() && this.origin.y() <= boardSize;
        for (LineSegment l : this.sides) {
            assert this.endpoints.contains(l.p1());
            assert this.endpoints.contains(l.p2());
        }
    }
    
    /**
     * Construct a TriangleBumper at the specified location.
     * @param name unique identifying name of this TriangleBumper
     * @param x x coordinate of origin reference point of absorber (top left corner of its bounding box in
     *        the flingball board)
     * @param y y coordinate of origin reference point
     * @param orientation degree rotation of this TriangleBumper. Must be either 0, 90, 180, or 270. 0 degrees 
     *        rotation corresponds to a TriangleBumper with vertices at the top-left, top-right, and bottom-left 
     *        of its bounding box; and 90, 180, and 270 degree rotations are defined as turning clockwise from 
     *        the 0 degree position
     */
    public TriangleBumper(String name, int x, int y, int orientation) {
        Set<Integer> orientationValues = new HashSet<>(Arrays.asList(0, 90, 180, 270));
        assert (orientationValues.contains(orientation)) : "orientation must be in {0, 90, 180, 270}";
        this.name = name;
        this.origin = new Vect((double) x, (double) y);
        
        if (orientation==0) {
            LineSegment leg1 = new LineSegment((double) x, (double) y, (double) x+sideLength, (double) y);
            LineSegment leg2 = new LineSegment((double) x, (double) y, (double) x, (double) y+sideLength);
            LineSegment hypotenuse = new LineSegment((double) x, (double) y+sideLength, (double) x+sideLength, (double) y);
            this.sides = new HashSet<>();
            this.sides.addAll(Arrays.asList(leg1, leg2, hypotenuse));
        } else if (orientation==90) {
            LineSegment leg1 = new LineSegment((double) x+sideLength, (double) y, (double) x+sideLength, (double) y+sideLength);
            LineSegment leg2 = new LineSegment((double) x, (double) y, (double) x+sideLength, (double) y);
            LineSegment hypotenuse = new LineSegment((double) x, (double) y, (double)x+sideLength, (double)y+sideLength);
            this.sides = new HashSet<>();
            this.sides.addAll(Arrays.asList(leg1, leg2, hypotenuse));
        } else if (orientation==180) {
            LineSegment leg1 = new LineSegment((double) x, (double) y+sideLength, (double) x+sideLength, (double) y+sideLength);
            LineSegment leg2 = new LineSegment((double) x+sideLength, (double) y, (double) x+sideLength, (double) y+sideLength);
            LineSegment hypotenuse = new LineSegment((double) x, (double) y+sideLength, (double) x+sideLength, (double) y);
            this.sides = new HashSet<>();
            this.sides.addAll(Arrays.asList(leg1, leg2, hypotenuse));
        } else { // orientation==270
            LineSegment leg1 = new LineSegment((double) x, (double) y, (double) x, (double) y+sideLength);
            LineSegment leg2 = new LineSegment((double) x, (double) y+sideLength, (double) x+sideLength, (double) y+sideLength);
            LineSegment hypotenuse = new LineSegment((double) x, (double) y, (double) x+sideLength, (double) y+sideLength);
            this.sides = new HashSet<>();
            this.sides.addAll(Arrays.asList(leg1, leg2, hypotenuse));
        }
        
        //store the vertices of the triangular bumper
        this.endpoints = new HashSet<>();
        for (LineSegment side : sides) {
            this.endpoints.add(side.p1());
            this.endpoints.add(side.p2());
        }
        checkRep();
    }

    @Override public Set<LineSegment> getSides() {
        return Collections.unmodifiableSet(new HashSet<>(sides));
    }
    
    @Override public Color getColor() {
        return new Color(color.getRGB());
    }
    
    @Override public boolean equals(Object that) {
        TriangleBumper thatTriangleBumper = (TriangleBumper) that;
        return that instanceof TriangleBumper && this.name.equals(thatTriangleBumper.getName())
                && this.origin.equals(thatTriangleBumper.origin); 
    }
        
    @Override public String getName() {
        return this.name;
    }

    @Override public Vect getLocation() {
        return new Vect(this.origin.x(), this.origin.y());
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
        double minTime = Double.MAX_VALUE/2;
        Vect reflectionVelocity = null;
        LineSegment reflectionSide = null;
        for (LineSegment side : this.sides) {
            double collisionTime = Physics.timeUntilWallCollision(side, ball.getBallCircle(), ball.getVelocity());
            if (collisionTime < minTime) {
                minTime = collisionTime;
                reflectionVelocity = Physics.reflectWall(side, ball.getVelocity());
                reflectionSide = side;
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

    @Override public boolean setTrigger(GameExpression target) {
        if (this.triggerTarget == null) {
            this.triggerTarget = target;
            checkRep();
            return true;
        }
        return false; 
    }
    
    @Override public boolean trigger() {
        if (this.triggerTarget != null) {
            this.triggerTarget.action();
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
