package flingball;

import static org.junit.Assert.*;

import org.junit.Test;

import physics.Vect;

public class FlingballSimulationTest {

    // Testing strategy:
    // Rather than testing the simulation methods in Flingball.java (which may involve retrieving and
    // verifying intermediate values for ball velocities and positions, etc.) we will focus instead on 
    // testing and verifying correctness of "discrete events" such as ball-to-bumper collisions and 
    // absorber firing absorbed balls under certain conditions. Gravity and friction will be disregarded for
    // testing these "discrete events", since their effect on ball velocity is time-dependent.
    //
    // Partition input and output space for the following methods as described below:
    // trigger():
    //      In: triggerTarget: 
    //              gadget triggers nothing
    //              gadget triggers another absorber (i.e. not itself)
    //              gadget triggers itself
    //          gadget type:
    //              Absorber
    //              CircleBumper
    //              TriangleBumper
    //              SquareBumper
    //              (every other class that implements GameExpression cannot trigger any action by the rules
    //               of flingball, so will not test that they do nothing)
    //      Out: true (triggerTarget's action() method was triggered)
    //           false (gadget has no triggerTarget)
    // action():
    //      In: outcome:
    //              gadget is not an absorber and performs no action
    //              gadget is an absorber and performs no action (i.e. had no absorbed balls to fire or previous ball hasn't fully exited yet)
    //              gadget is an absorber and fires a ball
    //          gadget type:
    //              Absorber
    //              (every other class that implements GameExpression cannot carry out any action (i.e. firing a ball
    //               by the rules of flingball, so will not test that they do nothing)
    //      Out: true: action performed
    //           false: gadget cannot perform action (i.e. not an absorber)
    //                  gadget is absorber but performed no action (no absorbed balls or previous ball hasn't fully exited yet)
    //              
    // collision():
    //      In: gadget type:
    //              Absorber (will absorb ball)
    //              CircleBumper (will deflect ball)
    //              TriangleBumper (will deflect ball)
    //              SquareBumper (will deflect ball)
    //              OuterWall (will deflect ball)
    //                  special case: hits corner of OuterWall (cornerCircles)
    //              Ball (will not affect ball)
    //              (the only other class that implements GameExpression is Board, which cannot be involved in any collision
    //               so will not test that it does nothing)
    //      Out: 
    //          ball's velocity is updated to appropriate reflectionVelocity
    //          ball's velocity is updated to zero (absorbed by absorber)
    //          nothing happens
    //
    // collision() methods can be linked to trigger() methods which can be linked to action() methods, so tests will test
    // their functionality together and separately
    
    
    
    // Partitions covered by this test:
    // collision(): In: gadget type: SquareBumper
    //              Out: ball's velocity is updated to appropriate reflectionVelocity
    // trigger(): In: triggerTarget: gadget triggers nothing
    //            Out: false (gadget has no triggerTarget)
    @Test
    public void testCollisionSquareNoTarget() {
        SquareBumper square = new SquareBumper("square", 0, 0);
        Ball ball = new Ball("ball", 1, 1, 0, -1);
        square.collision(ball);
        Vect expectedReflectionVelocity = new Vect(0, 1);
        assertEquals("expected ball to bounce off squareBumper and update its velocity to move straight down", 
                expectedReflectionVelocity, ball.getVelocity());
        assertFalse("expected square not to trigger anything", square.trigger());
    }

    
    // Partitions covered by this test:
    // collision(): In: gadget type: CircleBumper, Absorber
    //              Out: ball's velocity is updated to appropriate reflectionVelocity
    // trigger(): In: triggerTarget: gadget triggers another absorber (i.e. not itself)
    //            Out: true (triggerTarget's action() method was triggered)
    // action(): In: gadget is an absorber and fires a ball
    //           Out: action performed
    @Test
    public void testCollisionCircleTriggersAbsorber() {
        Absorber absorber = new Absorber("absorber", 17, 18, 2, 1);
        CircleBumper circle = new CircleBumper("circle", 5, 5);
        circle.setTrigger(absorber);
        
        Ball ball1 = new Ball("ball1", 5, 4, 0, 1);
        Vect ball1VelocityPrior = new Vect(ball1.getVelocity().x(), ball1.getVelocity().y());
        Ball ball2 = new Ball("ball2", 17, 18, 0, 1);
        
        absorber.collision(ball2);
        circle.collision(ball1);
        
        assertFalse("expected ball1 to update its velocity after bouncing off of circleBumper", 
                ball1VelocityPrior.equals(ball1.getVelocity()));
        
        Vect expectedFireVelocity = new Vect(0, -50);
        assertEquals("expected ball2 to be fired straight upwards by absorber "
                + "after absorber is triggered by circleBumper", expectedFireVelocity, ball2.getVelocity());
    }
    
    
    // Partitions covered by this test:
    // collision(): In: gadget type: TriangleBumper, Absorber
    //              Out: ball's velocity is updated to appropriate reflectionVelocity
    // trigger(): In: triggerTarget: gadget triggers another absorber (i.e. not itself)
    //            Out: true (triggerTarget's action() method was triggered)
    // action(): In: gadget is an absorber and performs no action (had no absorbed balls to fire)
    //           Out: gadget is absorber but performed no action
    @Test
    public void testCollisionTriangleTriggersAbsorber() {
        TriangleBumper triangle = new TriangleBumper("triangle", 0, 18, 270);
        Absorber absorber = new Absorber("absorber", 17, 18, 2, 1);
        triangle.setTrigger(absorber);
        
        Ball ball1 = new Ball("ball1", 2, 17, -1, 1);
        triangle.collision(ball1);
        Vect expectedReflectionVelocity = new Vect(1, -1);
        assertEquals("expected ball1 to deflect off triangleBumper and move in the opposite direction. "
                + "Checking for correct xVelocity", 
                expectedReflectionVelocity.x(), ball1.getVelocity().x(), 0.01);
        assertEquals("expected ball1 to deflect off triangleBumper and move in the opposite direction. "
                + "Checking for correct yVelocity", 
                expectedReflectionVelocity.y(), ball1.getVelocity().y(), 0.01);
        assertFalse("expected absorber to do nothing when triggered since it has no absorbed balls", absorber.action());
    }
    
    
    // Partitions covered by this test:
    // collision(): In: gadget type: Absorber
    //              Out: ball's velocity is updated to zero (absorbed by absorber)
    // trigger(): In: triggerTarget: gadget triggers itself
    //            Out: true (triggerTarget's action() method was triggered)
    // action(): In: gadget is an absorber and performs no action (previous ball hasn't fully exited yet)
    //           Out: gadget is absorber but performed no action
    @Test
    public void testAbsorberSelfTriggerNoFire() {
        Absorber absorber = new Absorber("absorber", 17, 18, 2, 1);
        absorber.setTrigger(absorber);
        
        Ball ball1 = new Ball("ball1", 18, 17, 0, 1);
        absorber.collision(ball1);
        
        Ball ball2 = new Ball("ball2", 18, 17, 0, 1);
        absorber.collision(ball2);
        
        Ball ball3 = new Ball("ball3", 18, 17, 0, 1);
        absorber.collision(ball3);
        
        Vect stationary = new Vect(0, 0);
        Vect fireVelocity = new Vect(0, -50);
        assertEquals("expected ball1 to be fired upwards", fireVelocity, ball1.getVelocity());
        assertEquals("expected ball1, ball2, ball3 to have been absorbed, but ball2 is not "
                + "fired when ball3 collides bc ball1 has not completed exited", stationary, ball2.getVelocity());
        assertEquals("expected ball3's velocity to be 0 since it should have been absorbed", stationary, ball3.getVelocity());        
    }
    
    
    // Partitions covered by this test:
    // collision(): In: gadget type: OuterWall (ball hits wall)
    //              Out: ball's velocity is updated to appropriate reflectionVelocity
    // trigger(): In: triggerTarget: gadget triggers nothing
    //            Out: false (gadget has no triggerTarget)
    @Test
    public void testCollisionOuterWallSide() {
        OuterWall outerWall = new OuterWall();
        Ball ball = new Ball("ball", 1, 1, -1, 0);
        Vect expectedReflectionVelocity = new Vect(1, 0);
        outerWall.collision(ball);
        assertEquals("expected ball to bounce off side of outerWall and reverse direction", 
                expectedReflectionVelocity, ball.getVelocity());
    }
    
    
    // Partitions covered by this test:
    // collision(): In: gadget type: OuterWall (ball hits corner)
    //              Out: ball's velocity is updated to appropriate reflectionVelocity
    // trigger(): In: triggerTarget: gadget triggers nothing
    //            Out: false (gadget has no triggerTarget)
    @Test
    public void testCollisionOuterWallCorner() {
        OuterWall outerWall = new OuterWall();
        Ball ball = new Ball("ball", 1, 1, -1, -1);
        Vect expectedReflectionVelocity = new Vect(1, 1);
        outerWall.collision(ball);
        assertFalse("expected ball to bounce off corner of outerWall and change direction", 
                expectedReflectionVelocity==ball.getVelocity());
    }
    
    // Partitions covered by this test:
    // collision(): In: gadget type: Ball
    //              Out: nothing happens
    @Test
    public void testCollisionBallNoEffect() {
        Ball ball1 = new Ball("ball1", 5, 4, 0, 1);
        Ball ball2 = new Ball("ball2", 5, 6, 0, -1);
        Vect ballVelocityPrior = new Vect(ball1.getVelocity().x(), ball1.getVelocity().y());
        Vect ball2VelocityPrior = new Vect(ball2.getVelocity().x(), ball2.getVelocity().y());
        ball1.collision(ball2);
        assertEquals("expected ball1's velocity to remain the same", ballVelocityPrior, ball1.getVelocity());
        assertEquals("expected ball2's velocity to remain the same", ball2VelocityPrior, ball2.getVelocity());
    }
}
