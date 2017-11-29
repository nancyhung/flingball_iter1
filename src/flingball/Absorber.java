package flingball;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import physics.Circle;
import physics.LineSegment;
import physics.Physics;
import physics.Vect;

/**
 * A mutable absorber gadget representing a flingball board component that can absorb balls that collide
 * with it, hold them in a given position within the absorber's area, and shoot them upward when
 * triggered. Absorber must be mutable in order to keep track of the balls it's absorbing and releasing.
 */
public class Absorber implements GameExpression {
    
    private final String name;
    private final Set<LineSegment> sides;
    private final Set<Vect> endpoints;
    private final Vect origin;
    private GameExpression triggerTarget = null;
    private final Queue<Ball> absorbedBalls = new LinkedList<Ball>();
    private final Vect absorbedBallPosition;
    private final Vect ejectedBallVelocity = new Vect(0, -50);
    private final Color color = Color.green;
    private final LineSegment topSide;
    private final LineSegment leftSide;
    private Ball released = null;
    private boolean triggerable = true;
    
    // Abstraction function:
    // AF(name, sides, endpoints, origin, absorbedBalls, absorbedBallPosition, ejectedBallVelocity, triggerTarget, color) 
    // = an absorber with the unique identifier name, sides corresponding to the LineSegments in sides, which have endpoints
    // corresponding to the Vects in endpoints. Its top left corner is at origin. It has absorbed the balls in absorbedBalls,
    // and these balls are stored at absorbedBallPosition. When triggered and if it has at least one absorbed ball, the absorber 
    // fires a ball with the velocity ejectedBallVelocity. It has the color color. 
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
    // - absorbedBalls is private and final, but must remain mutable since this is essential to the absorber's functionality
    //   only need to be called by code within this package
    // - ejectedBallVelocity has an immutable type
    // - return defensive copy of mutable objects sides and origin.
    
    private final int numberOfSides = 4;
    private final int boardSize = 20; // constant
    private final double ballSize = 0.25;
    
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
     * Construct an absorber.
     * @param name unique identifying name of this absorber
     * @param x x coordinate of origin reference point of absorber (top left corner of its bounding box in
     *        the flingball board)
     * @param y y coordinate of origin reference point
     * @param width width of absorber
     * @param height height of absorber
     */
    public Absorber(String name, int x, int y, int width, int height) {
        this.name = name;
        this.origin = new Vect((double) x, (double) y);
        this.absorbedBallPosition = new Vect(((double) x) + width - ballSize, ((double) y) +height - ballSize);
        LineSegment top = new LineSegment(origin.x(), origin.y(), origin.x()+width, origin.y());
        this.topSide = top;
        LineSegment left = new LineSegment(origin.x(), origin.y(), origin.x(), origin.y()+height);
        this.leftSide = left;
        LineSegment right = new LineSegment(origin.x()+width, origin.y(), origin.x()+width, origin.y()+height);
        LineSegment bottom = new LineSegment(origin.x(), origin.y()+height, origin.x()+width, origin.y()+height);
        this.sides = new HashSet<>();
        this.sides.addAll(Arrays.asList(top, left, right, bottom));
        // store vertices of absorber gadget
        this.endpoints = new HashSet<>();
        for (LineSegment side : sides) {
            this.endpoints.add(side.p1());
            this.endpoints.add(side.p2());
        }
        checkRep();
    }
    
    public void checkTriggerable() {
        if(this.released != null) {
            if (this.contains(this.released)) {
                triggerable = false;
            } else {
                triggerable = true;
                this.released = null;
            }
        } else {
            triggerable = true;
        }
    }
    
    @Override public boolean action() {
        // must call collision() before action() so that ball is added to queue first
        
        //only call action of absorber if previously released ball has left the absorber
        if (triggerable) {
            Ball nextBall = absorbedBalls.poll();
            checkRep();
            try {
                nextBall.setVelocity(this.ejectedBallVelocity);
                nextBall.setAbsorbed(false);
                this.released = nextBall;
            } catch (NullPointerException e) {
                checkRep();
                return false;
            }
            // if the code makes it here, successfully called the action of the absorber
            checkRep();
            return true;
        } else {
            return false;
        }

    }

    @Override public String getName() {
        return this.name;
    }
    
    public boolean contains(Ball ball) {
        return (ball.getLocation().x() < Math.max(topSide.p1().x(), topSide.p2().x()) 
                && ball.getLocation().x() > Math.min(topSide.p1().x(), topSide.p2().x())) &&
                (ball.getLocation().y() < Math.max(leftSide.p1().y(), leftSide.p2().y()) 
                        && ball.getLocation().y() > Math.min(leftSide.p1().y(), leftSide.p2().y()));
    }

    @Override public double timeUntilCollision(Ball ball) {
        checkTriggerable();
        double minTime = Double.MAX_VALUE/2;
        if (this.contains(ball)) {
            return minTime;
        }
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
    
    /**
     * Updates ball's velocity with its initial velocity after colliding with this
     * absorber. Absorb ball (i.e. set its velocity to zero and move it to the bottom right corner 
     * of this absorber). Otherwise, if no collision is possible, then do nothing.
     * @param ball the ball that may or may not collide with this gadget
     */
    @Override public void collision(Ball ball) {
        trigger();
        ball.setLocation(this.absorbedBallPosition);
        this.absorbedBalls.offer(ball);
        ball.setVelocity(new Vect(0, 0));
        ball.setAbsorbed(true);
        checkRep();
    }

    @Override public boolean setTrigger(GameExpression target) {
        if (this.triggerTarget == null) {
            this.triggerTarget = target;
            checkRep();
            return true;
        }
        checkRep();
        return false; 
    }

    @Override public boolean trigger() { 
        // trigger this gadget's target gadget's action if it has a target
        checkTriggerable();
        if (this.triggerTarget != null) {
            this.triggerTarget.action();
            checkRep();
            return true;
        }
        checkRep();
        return false;
    }
    
    @Override public Vect getLocation() {
        return new Vect(this.origin.x(), this.origin.y());
    }
    
    /**
     * Gets a set of all the top-left corners of every 1Lx1L unit box spanned by this absorber. Helper method
     * for Board.java's checkRep(), to ensure no gadget overlaps 
     * @return Set of all the top-left corners of every 1Lx1L unit box spanned by this absorber
     */
    protected Set<Vect> getGridSpan() {
        Set<Vect> gridSpan = new HashSet<>();
        gridSpan.add(this.origin);
        for (int i=1; i < (int) this.topSide.length(); i++) {
            gridSpan.add(new Vect(this.origin.x() + i, this.origin.y()));
        }
        for (int i=1; i < (int) this.leftSide.length(); i++) {
            gridSpan.add(new Vect(this.origin.x(), this.origin.y() + i));
        }
        return gridSpan;
    }
    
    @Override public Set<LineSegment> getSides() {
        return new HashSet<>(this.sides);
    }
    
    @Override public Color getColor() {
        return this.color;
    }

    @Override public boolean equals(Object that) {
        Absorber thatAbsorber = (Absorber) that;
        return that instanceof Absorber && this.name.equals(thatAbsorber.getName())
                && this.origin.equals(thatAbsorber.getLocation());
    }

    @Override public int hashCode() {
        return this.name.hashCode();
    }
}
