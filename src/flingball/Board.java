package flingball;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import physics.LineSegment;
import physics.Vect;

/**
 * A Flingball board is a GameExpression that consists of bounding walls and moving and stationary gadgets, which
 * may interact with each other. This is an immutable object but may contain mutable objects (i.e. ball and absorber).
 * We uphold safety from bugs principle by making defensive copies of the mutable objects so client doesn't the same
 * copy as this implementor.
 */
public class Board implements GameExpression {

    private final String name;
    private final Set<GameExpression> gadgets;
    private final Set<GameExpression> balls;
    private final OuterWall walls; 
    private final double gravity; 
    private final double friction1;
    private final double friction2;
    private final Color color = Color.black;
    
    // Abstraction function:
    // AF(name, gadgets, balls, walls, gravity, friction1, friction2, color) = a flingball board with the unique identifier
    // name, containing the balls in a collection balls and gadgets in a collection gadgets and bound by the outer walls in walls. 
    // Within the board environment, gravity and friction are acting, with strengths corresponding to the coefficients gravity, 
    // friction1, and friction2. The color of the board is color.
    // 
    // Rep invariant:
    // - name is not the empty string
    // - gravity >= 0
    // - friction1 >= 0
    // - friction2 >= 0
    // - every ball in balls and gadget in gadgets has a unique name
    // - the bounding boxes of all gadgets are physically non-overlapping
    //
    // Safety from rep exposure argument:
    // - name, gravity, friction1, friction2, and walls are private, final, and have immutable values
    // - gadgets and balls are private and final, but must remain mutable to maintain game functionality
    
    private void checkRep() {
        assert !this.name.equals("");
        assert this.gravity >= 0;
        assert this.friction1 >= 0;
        assert this.friction2 >= 0;
        
        Set<String> ballNames = new HashSet<>();
        for (GameExpression ball : this.balls) {
            ballNames.add(ball.getName());
        }
        assert ballNames.size()==this.balls.size();
        
        Set<String> gadgetNames = new HashSet<>();
        for (GameExpression gadget : this.gadgets) {
            gadgetNames.add(gadget.getName());
        }
        assert gadgetNames.size()==this.gadgets.size();
        assert ballNames.size() + gadgetNames.size() == this.balls.size() + this.gadgets.size();
        
        Set<Vect> gridSpan = new HashSet<>();
        for (GameExpression gadget : this.gadgets) {
            boolean isLargeAbsorber = false; // if an absorber is any bigger than 1Lx1L, it spans more than 1 grid square
            for (LineSegment side : gadget.getSides()) {
                if (side.length() >= 2) {
                    isLargeAbsorber = true;
                    break;
                }
            }
            if (isLargeAbsorber) {
                Absorber absorberGadget = (Absorber) gadget; 
                for (Vect gridPoint : absorberGadget.getGridSpan()) {
                    assert gridSpan.add(gridPoint);
                }
                continue;
            } else { // gadget is not an absorber or is a 1Lx1L absorber
                assert gridSpan.add(gadget.getLocation());
            }
        }
    }
    
    /**
     * Construct a flingball board with the given gravity and friction environment behavior and
     * containing the given gadgets and balls.
     * @param name unique identifying name of this board
     * @param gravity gravity coefficient, must be >= 0
     * @param friction1 friction coefficient, must be >= 0
     * @param friction2 friction coefficient, must be >= 0
     * @param gadgets set of gadgets to populate this board with
     * @param balls set of balls to place onto this board
     */
    public Board(String name, double gravity, double friction1, double friction2, Set<GameExpression> gadgets, Set<GameExpression> balls) {
        this.name = name;
        this.gravity = gravity;
        this.friction1 = friction1;
        this.friction2 = friction2;
        this.gadgets = gadgets;
        this.balls = balls;
        this.walls = new OuterWall();
        checkRep();
    }

    @Override public boolean equals(Object that) {
        Board thatBoard = (Board) that;
        boolean gadgetsEqual = this.gadgets.size() == thatBoard.getGadgets().size();
        for (GameExpression gadget : this.gadgets) {
            boolean found = false;
            for (GameExpression gadget2 : thatBoard.getGadgets()) {
                if (gadget.getClass().equals(gadget2.getClass()) && gadget.equals(gadget2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                gadgetsEqual = false;
            }
        }
        boolean ballsEqual = this.balls.size() == thatBoard.getBalls().size();
        for (GameExpression ball : this.balls) {
            boolean found = false;
            for (GameExpression ball2 : thatBoard.getBalls()) {
                if (ball.equals(ball2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                ballsEqual = false;
            }
        }
        checkRep();
        return that instanceof Board && this.name.equals(thatBoard.getName()) && gadgetsEqual && ballsEqual
                && this.gravity==thatBoard.getGravity() && this.friction1==thatBoard.getFriction1() && this.friction2==thatBoard.getFriction2();
    }

    @Override public String getName() {
        checkRep();
        return this.name;
    }
    
    /**
     * Gets gadgets on the board.
     * @return set of stationary gadgets in game board as set of GameExpressions
     */
    public Set<GameExpression> getGadgets() {
        checkRep();
        return new HashSet<GameExpression>(gadgets);
    }
    
    /**
     * Gets the balls in the game.
     * @return set of balls in game board as set of GameExpressions
     */
    public Set<GameExpression> getBalls() {
        checkRep();
        return new HashSet<GameExpression>(balls);
    }

    @Override public boolean setTrigger(GameExpression target) {
        return false;
    }

    @Override public Vect getLocation() {
        checkRep();
        return new Vect(this.walls.getLocation().x(), this.walls.getLocation().y());
    }
    
    /**
     * Get this board's gravity coefficient.
     * @return this board's gravity coefficient
     */
    public double getGravity() {
        return this.gravity;
    }
    
    /**
     * Get this board's friction1 (mu) coefficient.
     * @return this board's friction1 coefficient
     */
    public double getFriction1() {
        return this.friction1;
    }
    
    /**
     * Get this board's friction1 (mu2) coefficient.
     * @return this board's friction2 coefficient
     */
    public double getFriction2() {
        return this.friction2;
    }
    
    @Override public Set<LineSegment> getSides() {
        checkRep();
        return new HashSet<LineSegment>(this.walls.getSides());
    }
    
    @Override public Color getColor() {
        return this.color;
    }
    
    public OuterWall getWalls() {
        checkRep();
        return this.walls;
    }

    @Override public double timeUntilCollision(Ball ball) {
        return Double.MAX_VALUE/2;
    }

    @Override public void collision(Ball ball) {
        return;
    }

    @Override public boolean trigger() {
        return false;
    }

    @Override public boolean action() {
        return false;
    }

    @Override public int hashCode() {
        return this.name.hashCode();
    }
}
