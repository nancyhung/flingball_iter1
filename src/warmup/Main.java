package warmup;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import physics.Circle;
import physics.LineSegment;
import physics.Physics;
import physics.Vect;

public class Main {
    
    private static final int GAMEBOARD_SIZE = 20;
    private static final int PIXELS_PER_L = 20;
    private static final int DRAWING_AREA_SIZE_IN_PIXELS = GAMEBOARD_SIZE * PIXELS_PER_L;
    private static final int TIMER_INTERVAL_MILLISECONDS = 50; // for ~20 frames per second
    
    private static WarmupBall ball;
    private static final LineSegment left = new LineSegment(0,0,0,GAMEBOARD_SIZE);
    private static final LineSegment right = new LineSegment(GAMEBOARD_SIZE,0,GAMEBOARD_SIZE,GAMEBOARD_SIZE);
    private static final LineSegment top = new LineSegment(0,0,GAMEBOARD_SIZE,0);
    private static final LineSegment bottom = new LineSegment(0,GAMEBOARD_SIZE,GAMEBOARD_SIZE,GAMEBOARD_SIZE);
    private static final Set<LineSegment> walls = new HashSet<>(Arrays.asList(left,right,top,bottom));

    public static void main(String[] args) {

        // Animate ball bouncing (using Time)
        Animation();
        
        // Just compute ball bouncing (using independent second counter and fixed timeframe)
//        ball = new Ball(new Circle(new Vect(0,0),1.), new Vect(Math.random()*5,Math.random()*5));
//        left = new LineSegment(0,0,0,GAMEBOARD_SIZE);
//        right = new LineSegment(GAMEBOARD_SIZE,0,GAMEBOARD_SIZE,GAMEBOARD_SIZE);
//        top = new LineSegment(0,0,GAMEBOARD_SIZE,0);
//        bottom = new LineSegment(0,GAMEBOARD_SIZE,GAMEBOARD_SIZE,GAMEBOARD_SIZE);
//        walls = new HashSet<>(Arrays.asList(left,right,top,bottom));
//
//        System.out.println("The ball begins at " + ball.getShape().getCenter() + " with velocity " + ball.getVelocity());
//        
//        for (int iter = 0; iter < 5; iter ++) {
//            double mintime = Double.POSITIVE_INFINITY;
//            LineSegment wallColl = null;
//            for (LineSegment wall : walls) {
//                double collision = Physics.timeUntilWallCollision(wall,ball.getShape(),ball.getVelocity());
//                if (collision < mintime) {
//                    mintime = collision;
//                    wallColl = wall;
//                }
//            }
//            double timestep = 20.;
//            System.out.println("time to collision " + mintime);
//            for (int i = 0; i < mintime*timestep; i ++) {
//                ball.updateCenter(1/timestep);
//                System.out.println("time " + i + " location " + ball.getShape().getCenter());
//            }
//            
//            Vect newVelocity = Physics.reflectWall(wallColl,ball.getVelocity());
//            System.out.println("bounced on wall from " + wallColl.p1() + " to " + wallColl.p2() + " with old velocity: " + ball.getVelocity() + " new velocity: " + newVelocity);
//            ball.updateVelocity(newVelocity);
//        }
    }
    
    private static void Animation() {
        final JFrame window = new JFrame("Bounce!");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        final JPanel drawingArea = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                drawBall(g);
            }
        };
        drawingArea.setPreferredSize(new Dimension(DRAWING_AREA_SIZE_IN_PIXELS, DRAWING_AREA_SIZE_IN_PIXELS));
        window.add(drawingArea);
        window.pack();
        window.setVisible(true);

        initializeBall(25);
        // note: the time must be javax.swing.Timer, not java.util.Timer
        new Timer(TIMER_INTERVAL_MILLISECONDS, (ActionEvent e) -> {
            drawingArea.repaint();
        }).start();
    }
    
    private static long timeOfLastDraw;
    private static long mintime;
    private static LineSegment wallColl;
    
    private static void initializeBall(double speed) {
        ball = new WarmupBall(new Circle(new Vect(1,1),1.), new Vect(Math.random()*speed,Math.random()*speed));
        System.out.println("The ball begins at " + ball.getShape().getCenter() + " with velocity " + ball.getVelocity());
        timeOfLastDraw = System.currentTimeMillis();
        mintime = Long.MAX_VALUE/2;
        wallColl = null;
    }
    
    private static void drawBall(final Graphics g) {
        Graphics2D g2 = (Graphics2D) g;  // every Graphics object is also a Graphics2D, which is a stronger spec
        
        // fill the background to erase everything
        g2.setColor(Color.black);
        g2.fill(new Rectangle2D.Double(0, 0, DRAWING_AREA_SIZE_IN_PIXELS, DRAWING_AREA_SIZE_IN_PIXELS));

        // animate the position of the smile by computing a position directly from clock time.
        final long now = System.currentTimeMillis();
        final long timeSinceLastDraw = now - timeOfLastDraw;
        timeOfLastDraw = now;
        
        if (mintime <= timeOfLastDraw | mintime == Long.MAX_VALUE/2) {
            if (mintime != Long.MAX_VALUE/2) {
                Vect newVelocity = Physics.reflectWall(wallColl,ball.getVelocity());
                ball.updateVelocity(newVelocity);
            }
            mintime = Long.MAX_VALUE/2; //reset mintime to find time to next collision
            for (LineSegment wall : walls) {
                double collision = Physics.timeUntilWallCollision(wall,ball.getShape(),ball.getVelocity());
                if ((long)(collision*1000) + timeOfLastDraw < mintime && collision != Double.POSITIVE_INFINITY) { //check for overflow with Infinity + Infinity
                    mintime = (long)(collision*1000) + timeOfLastDraw;
                    System.out.println("new mintime " + mintime + " current " + timeOfLastDraw);
                    wallColl = wall;
                }
            }
        }
        
        ball.updateCenter(timeSinceLastDraw/1000.); //convert to seconds
        double X = Math.max(0,Math.min(GAMEBOARD_SIZE, ball.getX()));
        double Y = Math.max(0,Math.min(GAMEBOARD_SIZE, ball.getY()));
        ball.updateCenter(new Vect(X,Y));
        System.out.println("location of center " + ball.getShape().getCenter());

        // move the origin of drawing
        g2.translate((ball.getX()-ball.getRadius())*PIXELS_PER_L,(ball.getY()-ball.getRadius())*PIXELS_PER_L);

        // draw the blue ball
        g2.setColor(Color.blue);
        g2.fill(new Ellipse2D.Double(0, 0, 2.0 * ball.getRadius()*PIXELS_PER_L, 2.0 * ball.getRadius()*PIXELS_PER_L));
    }
    
}
