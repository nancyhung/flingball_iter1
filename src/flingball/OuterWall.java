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
 * The four bounding outer walls of a flingball board. These walls can deflect balls that collide with them.
 */
public class OuterWall implements GameExpression {

    private final String name = "outerwalls";
    private final LineSegment top;
    private final LineSegment left;
    private final LineSegment right;
    private final LineSegment bottom;
    private final Set<LineSegment> sides;
    private final Circle topLeft;
    private final Circle topRight;
    private final Circle bottomLeft;
    private final Circle bottomRight;
    private final Set<Circle> cornerCircles;
    private final double origin = 0;
    private final double cornerCircleRadius = 0;
    private final int width = 20;
    private final int height = 20;
    private final int numberOfCircles = 4;
    private final int numberOfSides = 4;
    private final Color color = Color.black;

    // Abstraction function:
    // AF(name, top, left, right, bottom, sides, topLeft, topRight, bottomLeft, bottomRight, cornerCircles, origin,
    //     cornerCircleRadius, width, height, color, numberOfCircles, numberOfSides) = the bounding outer walls of a flingball board, 
    //     with unique identifier name, and consisting of the walls top, left, right, and bottom, which comprise the set of sides in sides. Each corner
    //     where walls meet has a circle (topLeft, topRight, bottomLeft, bottomRight, all of which together comprise cornerCircles). 
    //     Each circle has radius cornerCircleRadius. The width and height of the board bounded by the outer walls are equal to width
    //     and height, respectively. The color of the walls is color. NumberOfCircles stores the number of circles the outer wall contains. 
    //     NumberOfSides stores number of sides for this object.
    //
    // Rep invariant:
    // - name is not the empty string
    // - length of top, left, bottom, and right are equal
    // - topLeft, topRight, bottomLeft, bottomRight are all located at the corners of the box formed by top, left, bottom, and right
    // - size of cornerCircles = 4
    // - cornerCircles contains topLeft, topRight, bottomLeft, bottomRight
    // - size of sides = 4
    // - sides contains top, left, right, bottom
    // 
    // Safety from rep exposure argument:
    // - name is private, final, and has an immutable value
    // - top, left, right, bottom, topLeft, topRight, bottomLeft, bottomRight are all private, final, and have immutable values
    // - sides and cornerCircles are private and final.
    // - There are no getter/setter methods for cornerCircles, and the getter method
    //   for sides returns an unmodifiable view of the set, which contains immutable objects
    
    /**
     * Construct the outer walls (i.e. bounding edges) of the flingball board with default dimensions 20Lx20L.
     */
    public OuterWall() {
        this.top = new LineSegment(origin, origin, origin+width, origin);
        this.left = new LineSegment(origin, origin, origin, origin+height);
        this.right = new LineSegment(origin+width, origin, origin+width, origin+height);
        this.bottom = new LineSegment(origin, origin+height, origin+width, origin+height);
        this.sides = new HashSet<>(Arrays.asList(this.top, this.left, this.right, this.bottom));
        
        this.topLeft = new Circle(origin, origin, cornerCircleRadius);
        this.topRight = new Circle(origin+width, origin, cornerCircleRadius);
        this.bottomLeft = new Circle(origin, origin+height, cornerCircleRadius);
        this.bottomRight = new Circle(origin+width, origin+height, cornerCircleRadius);
        this.cornerCircles = new HashSet<>(Arrays.asList(this.topLeft, this.topRight, this.bottomLeft, this.bottomRight));
        checkRep();
    }
    
    private void checkRep() {
        assert name.length()>0;
        assert top.length() == bottom.length()
                && bottom.length() == left.length() 
                && left.length() == right.length();
        // check corners
        assert topLeft.getCenter().equals(new Vect(Math.min(left.p1().x(),  left.p2().x()), Math.min(top.p1().y(), top.p2().y())));
        assert topRight.getCenter().equals(new Vect(Math.max(top.p1().x(),  top.p2().x()), Math.min(right.p1().y(), right.p2().y())));
        assert bottomLeft.getCenter().equals(new Vect(Math.min(bottom.p1().x(),  bottom.p2().x()), Math.max(right.p1().y(), right.p2().y())));
        assert bottomRight.getCenter().equals(new Vect(Math.max(bottom.p1().x(),  bottom.p2().x()), Math.max(right.p1().y(), right.p2().y())));
        // check size of cornerCircles
        assert cornerCircles.size()==numberOfCircles;
        assert cornerCircles.contains(topLeft) && cornerCircles.contains(topRight) && cornerCircles.contains(bottomLeft) && cornerCircles.contains(bottomRight);
        assert sides.size()==numberOfSides;
        assert sides.contains(top) && sides.contains(bottom) && sides.contains(left) && sides.contains(right);
    }

    @Override public String getName() {
        checkRep();
        return this.name; // should never be called
    }

    @Override public Vect getLocation() {
        checkRep();
        return new Vect(origin, origin); // should never be called
    }
    
    @Override public Set<LineSegment> getSides() {
        checkRep();
        return Collections.unmodifiableSet(this.sides);
    }
    
    @Override public Color getColor() {
        checkRep();
        return this.color;
    }

    @Override public boolean equals(Object that) {
        OuterWall thatOuterWall = (OuterWall) that;
        checkRep();
        return that instanceof OuterWall && this.name.equals(thatOuterWall.getName())
                && this.top.equals(thatOuterWall.top)
                && this.bottom.equals(thatOuterWall.bottom)
                && this.right.equals(thatOuterWall.right)
                && this.left.equals(thatOuterWall.left); 
    }

    @Override public double timeUntilCollision(Ball ball) {
        double minTime = Double.MAX_VALUE/2;
        for (LineSegment side : this.sides) {
            double collisionTime = Physics.timeUntilWallCollision(side, ball.getBallCircle(), ball.getVelocity());
            if (collisionTime < minTime) {
                minTime = collisionTime;
            }
        }
        for (Circle circle : this.cornerCircles) {
            double collisionTime = Physics.timeUntilCircleCollision(circle, ball.getBallCircle(), ball.getVelocity());
            if (collisionTime < minTime) {
                minTime = collisionTime;
            }
        }
        checkRep();
        return minTime;
    }

    @Override public void collision(Ball ball) {
        double minTime = Double.MAX_VALUE/2;
        Vect reflectionVelocity = null;
        for (LineSegment side : this.sides) {
            double collisionTime = Physics.timeUntilWallCollision(side, ball.getBallCircle(), ball.getVelocity());
            if (collisionTime < minTime) {
                minTime = collisionTime;
                reflectionVelocity = Physics.reflectWall(side, ball.getVelocity());
            }
        }
        for (Circle circle : this.cornerCircles) {
            double collisionTime = Physics.timeUntilCircleCollision(circle, ball.getBallCircle(), ball.getVelocity());
            if (collisionTime < minTime) {
                minTime = collisionTime;
                reflectionVelocity = Physics.reflectCircle(circle.getCenter(), ball.getLocation(), ball.getVelocity());
            }
        }
        ball.setVelocity(reflectionVelocity);
        checkRep();
    }

    @Override public boolean trigger() {
        return false;
    }

    @Override public boolean setTrigger(GameExpression target) {
        return false;
    }
    
    @Override public boolean action() {
        checkRep();
        return false;
    }

    @Override public int hashCode() {
        return this.name.hashCode();
    }
}
