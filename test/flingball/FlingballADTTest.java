package flingball;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import physics.Vect;

public class FlingballADTTest {
    // Testing strategy:
    //
    // Partition based on instance methods of ADT and fire/trigger relationship
    // between objects that can have this property (ball is always object that causes).
    // Board
    //   gravity: 0, 0<gravity<25, 25, >25
    //   friction1: 0, >0
    //   friction2: 0, >0
    //   length of name: 1, >1
    //   trigger() method
    //     cause object initial state: on, off
    //     effect object initial state: on, off 
    //     cause object final state: on, off
    //     effect object final state: on, off
    //
    // Universal to all gadgets
    //   getColor(): not need to test because color is a private final value
    //              initialized
    //   trigger(), setTrigger(), action(), collision, timeUntilCollision() will be tested in 
    //            SimulationTests because these methods require interaction between objects, 
    //            which makes more sense on a board where access 
    // 
    // OuterWall
    //   length of name: 1, >1
    //   location of origin: (0,0), positive x, positive y
    //   
    // Ball
    //   length of name: 1, >1
    //   x position: 0, 0 < x < boardWidth
    //   y position: 0, 0 < y < boardHeight
    //   xVelocity: 0, 1.0, >1.0
    //   yVelocity: 0, 1.0, >1.0
    //   
    // SquareBumper
    //   length of name: 1, >1
    //   x position: 0, 0 < x < boardWidth
    //   y position: 0, 0 < y < boardHeight
    // 
    // TriangleBumper
    //   length of name: 1, >1
    //   x position: 0, 0 < x < boardWidth
    //   y position: 0, 0 < y < boardHeight
    //   orientation: 0, 90, 180, 270
    // 
    // CircleBumper
    //   length of name: 1, >1
    //   x position: 0, 0 < x < boardWidth
    //   y position: 0, 0 < y < boardHeight
    // 
    // Absorber
    //   length of name: 1, >1
    //   x position: 0, 0 < x < boardWidth
    //   y position: 0, 0 < y < boardHeight
    //   width: 1, >1
    //   height: 1, >1
    // Tests will cover all parts of partitions.
    

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testBallNameEqualsTrigger() {
        String name = "testBall";
        double x = 1.0;
        double y = 1.0;
        double xVelocity = 15.0;
        double yVelocity = 10.0;
        Ball ball = new Ball(name, x, y, xVelocity, yVelocity);
        Ball ballDup = new Ball(name, x, y, xVelocity, yVelocity);
        Ball ballDiff = new Ball("yes", x, y, xVelocity, yVelocity);
        
        assertEquals("expected name", "testBall", ball.getName());
        assertEquals("expected equals true", true, ball.equals(ballDup));
        assertEquals("expected equals f", false, ball.equals(ballDiff));
        assertEquals("expected equals f", false, ball.equals(new Ball(name, x+1, y, xVelocity, yVelocity)));
        
        // setTrigger and trigger
        assertEquals("expected couldn't set trigger", false, ball.setTrigger(new OuterWall()));
        assertEquals("expected couldn't trigger", false, ball.trigger());
    }
    
    @Test
    public void testEmptyBoardInitialization() {
        String name = "testBoard";
        double gravity = 9.25;
        double friction1 = 1.0;
        double friction2 = 1.0;
        Set<GameExpression> setOfGadgets = new HashSet<>();
        Set<GameExpression> balls = new HashSet<>();
        Board board = new Board(name, gravity, friction1, friction2, setOfGadgets, balls);
        
        assertEquals("expected name", "testBoard", board.getName());
        assertEquals("expected gravity", gravity, board.getGravity(), 0.01);
        assertEquals("expected friction1", friction1, board.getFriction1(), 0.01);
        assertEquals("expected friction2", friction2, board.getFriction2(), 0.01);
        assertEquals("expected no gadgets", setOfGadgets, board.getGadgets());
        assertEquals("expected no gadgets", balls, board.getBalls());
    }
    
    @Test
    public void testSquareBumperNameGetLocationEquals() {
        String name = "sb";
        int x = 5;
        int y = 3;
        SquareBumper sb = new SquareBumper(name, x, y);
        
        assertEquals("expected name", name, sb.getName());
        // get location
        assertEquals("expected location", new Vect(x, y), sb.getLocation());
        assertEquals("expected equals", true, sb.equals(new SquareBumper(name, x, y)));
        assertEquals("expected not equal because diff position", false, sb.equals(new SquareBumper(name, 15, y)));
    }
    
    @Test
    public void testCircleBumperNameGetLocationEquals() {
        String name = "cb";
        int x = 3;
        int y = 4;
        CircleBumper cb = new CircleBumper(name, x, y);
        
        assertEquals("expected name", name, cb.getName());
        // get location
        assertEquals("expected location", new Vect(x+0.5, y+0.5), cb.getLocation());
        assertEquals("expected equals", true, cb.equals(new CircleBumper(name, x, y)));
        assertEquals("expected not equal because diff position", false, cb.equals(new CircleBumper(name, 15, y)));
    }
    
    @Test
    public void testTriangleBumperNameGetLocationEqualsNoOrientation() {
        String name = "t";
        int x = 0;
        int y = 0;
        TriangleBumper t = new TriangleBumper(name, x, y, 0);
        
        assertEquals("expected name", name, t.getName());
        // get location
        assertEquals("expected location", new Vect(x, y), t.getLocation());
        assertEquals("expected equals", t.equals(new TriangleBumper(name, x, y, 0)) ,true);
        assertEquals("expected not equal because diff position", t.equals(new TriangleBumper(name, x, y+2, 0)) ,false);
    }
    
    @Test
    public void testTriangleBumperNameGetLocationEqualsOrientation() {
        String name = "t";
        int x = 0;
        int y = 0;
        int orientation = 180;
        TriangleBumper t = new TriangleBumper(name, x, y, orientation);
        
        assertEquals("expected name", name, t.getName());
        assertEquals("expected location", new Vect(x, y), t.getLocation());
        assertEquals("expected equals", true, t.equals(new TriangleBumper(name, x, y, orientation)));
        assertEquals("expected not equal because diff position", true, t.equals(new TriangleBumper(name, x, y, orientation)));
        // TODO would it be good to get orientation?
    }
    
    @Test
    public void testOuterWallGetLocationGetSidesEquals() {
        Vect v = new Vect(0,0);
        OuterWall o = new OuterWall();
        assertEquals("expected same", v, o.getLocation());
    }
    
    @Test
    public void testAbsorber() {
        String name = "t";
        int x = 0;
        int y = 0;
        int width = 5;
        int height = 10;
        Absorber a = new Absorber(name, x, y, width, height);
        assertEquals("expected name", name, a.getName());
        assertEquals("expected not equals", false, a.equals(new Absorber(name+"what", x, y, width, height)));
        assertEquals("expected equals", true, a.equals(new Absorber(name, x, y, width, height)));
    }
}
