package flingball;

import java.awt.Color;
import java.util.Set;

import physics.LineSegment;
import physics.Vect;

/**
 * A mutable data type representing components of a flingball game.
 */
public interface GameExpression {
    // Datatype definition
    //   GameExpression = Board(name, gravity)
    //                     + Gadgets(name, position, optional rotation)
    //                     + Ball(name, position, velocityX, velocityY)
    //                     + Actions(type and name)
    
    /**
     * Get the unique identifying name of this GameExpression.
     * @return GameExpression name 
     */
    public String getName();
    
    /**
     * Get the location of the GameExpression on the flingball board. For circular GameExpressions, this
     * corresponds to the center point of the circle, and for all other GameExpressions, this 
     * corresponds to the origin of the bounding box of the GameExpression.
     * @return Vector to the reference point of the GameExpression
     */
    public Vect getLocation();
    
    /**
     * Get the set of lineSegments representing the sides of this GameExpression
     * @return set of lineSegments of GameExpression sides
     */
    public Set<LineSegment> getSides();
    
    /**
     * Evaluate equality between this GameExpression and that object.
     * @return true if this GameExpression is equal to that, false otherwise
     */
    public boolean equals(Object that);
    
    /**
     * @return this GameExpression's hashCode
     */
    public int hashCode();
    
    /**
     * Calculate the amount of time until ball hits this GameExpression
     * @param ball the ball that may or may not collide with this GameExpression
     * @return amount of time between now and the ball colliding with this GameExpression
     *         or Double.MAX_VALUE/2 if no collision will occur
     */
    public double timeUntilCollision(Ball ball);
    
    /**
     * Updates ball's velocity with its initial velocity after colliding with this
     * gadget. If no collision is possible, then do nothing.
     * @param ball the ball that may or may not collide with this gadget
     */
    public void collision(Ball ball);
    
    /**
     * Trigger the action of this GameExpression's target gadget. If this GameExpression has no
     * target, then do nothing.
     * @return true if this GameExpression's target's action was triggered successfully,
     *         false otherwise.
     */
    public boolean trigger();
    
    /**
     * Perform this GameExpression's action.
     * @return true if action is performed successfully, false otherwise
     */
    public boolean action();
    
    /**
     * Set the target of this GameExpression's trigger event. Target cannot be reset after
     * it is set.
     * @param target GameExpression whose action occurs when this GameExpression is triggered
     * @return true if target was set successfully, false otherwise
     */
    public boolean setTrigger(GameExpression target);
    
    /**
     * Get the color of the gadget for visualization purposes
     * @return Color object specifying the color of the gadget
     */
    public Color getColor();
}
