package flingball;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import edu.mit.eecs.parserlib.UnableToParseException;

public class BoardParserTest {
    // Testing strategy:
    // partition on the contents of the files that can be recognized by the grammar and parsed by the parser:
    //      contains each type of gadget: circle, square, triangle, absorber (with 0, 1, > 1 of each)
    //          contains orientation parameter for triangle
    //          contains action triggers for absorbers
    //      contains 0, 1, >1 ball
    //      contains 0 or 1 values for each of gravity, friction1, friction2
    //      contains arbitrary comments
    //      contains arbitrary whitespace
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // test 1 ball, > 1 circle, >1 square, 1 triangle, 0 absorber gadgets; 1 value for gravity, 0 for frictions; 
    // set orientation for triangle bumper; use arbitrary comments
    @Test
    public void testDefaultBoard() throws UnableToParseException{
        Set<GameExpression> gadgets = new HashSet<>();
        gadgets.add(new SquareBumper("SquareA",0,17));
        gadgets.add(new SquareBumper("SquareB",1,17));
        gadgets.add(new SquareBumper("SquareC",2,17));
        gadgets.add(new CircleBumper("CircleA",1,10));
        gadgets.add(new CircleBumper("CircleB",7,18));
        gadgets.add(new CircleBumper("CircleC",8,18));
        gadgets.add(new CircleBumper("CircleD",9,18));
        gadgets.add(new TriangleBumper("Tri",13,15,180));
        Set<GameExpression> balls = new HashSet<>();
        balls.add(new Ball("BallA",1.25,1.25,0,0));
        Board expected = new Board("Default",25.0,0.025,0.025,gadgets,balls);
        Board test = (Board)BoardParser.parse(new File("test/flingball/default.fb"));
        assertEquals("expected able to parse same board", expected, test);
        assertEquals("expected same hashcode", expected.hashCode(), test.hashCode());
    }
    
    // test >1 ball, 0 circle, 0 square, 0 triangle, 1 absorber gadgets; 1 value for gravity, friction1, friction2; 
    // test trigger for absorbers, use arbitrary whitespaces
    @Test
    public void testAbsorberBoard() throws UnableToParseException{
        Set<GameExpression> gadgets = new HashSet<>();
        SquareBumper trigger = new SquareBumper("Square",0,10);
        Absorber abs = new Absorber("Abs",0,19,20,1);
        trigger.setTrigger(abs);
        gadgets.add(trigger);
        gadgets.add(abs);
        Set<GameExpression> balls = new HashSet<>();
        balls.add(new Ball("Ball",1.8,4.5,-3.4,-2.3));
        balls.add(new Ball("Ball2",2.8,4.5,-3.4,-2.3));
        balls.add(new Ball("Ball3",0.5,2,0,-5));
        Board expected = new Board("absorberBoard",10.0,0.01,0.02,gadgets,balls);
        Board test = (Board)BoardParser.parse(new File("test/flingball/absorberBoard.fb"));
        assertEquals("expected able to parse same board", expected, test);
        assertEquals("expected same hashcode", expected.hashCode(), test.hashCode());
    }
    
    // test 0 gadgets; no values for gravity, friction1, friction2; 
    // use arbitrary whitespaces and comments
    @Test
    public void testEmptyBoard() throws UnableToParseException{
        Board expected = new Board("empty",25,0.025,0.025,new HashSet<>(),new HashSet<>());
        Board test = (Board)BoardParser.parse(new File("test/flingball/empty.fb"));
        assertEquals("expected able to parse same board", expected, test);
        assertEquals("expected same hashcode", expected.hashCode(), test.hashCode());
    }
}