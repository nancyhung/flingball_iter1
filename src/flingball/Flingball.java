package flingball;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import edu.mit.eecs.parserlib.UnableToParseException;
import physics.LineSegment;
import physics.Vect;

/**
 * A Flingball game composed of gadgets (ball, bumpers, and absorbers) and a 20L by 20L playing area 
 * (with walls and physics) that interact with each other, displayed running as a graphical user interface 
 * that pops up in a new window.
 */
public class Flingball {
    
    private static final int GAMEBOARD_SIZE = 20;
    private static final int PIXELS_PER_L = 20;
    private static final int DRAWING_AREA_SIZE_IN_PIXELS = GAMEBOARD_SIZE * PIXELS_PER_L;
    private static final int TIMER_INTERVAL_MILLISECONDS = 40; // for ~25 frames per second
    
    /**
     * Simulates the board game setup by the .fb file specified in the command-line or 
     * a default.fb file and animates the visualization of the game in a separate window
     */
    public static void main(String[] args) throws UnableToParseException{
        if (args.length == 0) {
            String testFile = "test/flingball/default.fb";
            System.out.println(testFile);
            animate(testFile);
        } else {
            for (int i = 0; i < args.length; i ++) {
                String testFile = args[i].toString(); //read in file name from command-line
                System.out.println(testFile);
                animate(testFile);
            }
        }
    }
    
    /**
     * Display the running simulation of the board specified by file as a GUI in a new window that is redrawn
     * approximately every 50 milliseconds (this implies the simulation is nondeterministic as the physics approximations
     * differ depending on the exact timestep taken between redrawn frames)
     * @param file .fb file that specifies setup of the game board
     * @throws UnableToParseException if file cannot be opened
     */
    public static void animate(String file) throws UnableToParseException {
        final JFrame window = new JFrame("FlingBall!");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //initialize the board/game from the specified file
        Board board = (Board)initializeBoard(file);
        
        final JPanel drawingArea = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                draw(g,board);
            }
        };
        drawingArea.setPreferredSize(new Dimension(DRAWING_AREA_SIZE_IN_PIXELS, DRAWING_AREA_SIZE_IN_PIXELS));
        window.add(drawingArea);
        window.pack();
        window.setVisible(true);
        
        // redraw the board every 50 milliseconds
        new Timer(TIMER_INTERVAL_MILLISECONDS, (ActionEvent e) -> {
            drawingArea.repaint();
        }).start();
    }
    
    private static long timeOfLastDraw;
    
    /**
     * Assembles a GameExpression object (Board) according to the specifications outlined in file
     * @param file .fb file with board specifications
     * @return GameExpression object that contains all of the components of the game outlined in file
     * @throws UnableToParseException if file cannot be opened
     */
    private static GameExpression initializeBoard(String file) throws UnableToParseException{
        timeOfLastDraw = System.currentTimeMillis();
        File f = new File(file);
        return BoardParser.parse(f);
    }
        
    /**
     * Specifies the layout of the board (how each of the individual components/gadgets should be drawn)
     * at a single instance in time by storing instructions in (modifying) graphics g
     * @param g graphics representation of the board
     * @param board game with gadgets to be visualized
     */
    private static void draw(final Graphics g, Board board) {
        Graphics2D g2 = (Graphics2D) g;  // every Graphics object is also a Graphics2D, which is a stronger spec
        
        // fill the background to erase everything
        g2.setColor(Color.black);
        g2.fill(new Rectangle2D.Double(0, 0, DRAWING_AREA_SIZE_IN_PIXELS, DRAWING_AREA_SIZE_IN_PIXELS));
        
        // compute timestep from clock time
        final long now = System.currentTimeMillis();
        long timeSinceLastDraw = now - timeOfLastDraw;
        timeOfLastDraw = now;
        
        // iterate through all of the stationary gadgets and redraw each gadget (erased when the board was redrawn)
        Set<GameExpression> gadgets = new HashSet<>(board.getGadgets());
                
        for(GameExpression gadget : gadgets) {
            Set<LineSegment> sides = gadget.getSides();
            int xMin = DRAWING_AREA_SIZE_IN_PIXELS;
            int xMax = 0;
            int yMin = DRAWING_AREA_SIZE_IN_PIXELS;
            int yMax = 0;
            
            Set<Vect> points = new HashSet<>();
            for (LineSegment side : sides) {
                points.add(side.p1());
                points.add(side.p2());
            }
            int[] x = new int[points.size()];
            int[] y = new int[points.size()];
            List<Vect> pointsList = new ArrayList<Vect>(points);
            for (int index = 0; index < pointsList.size(); index ++) {
                x[index] = (int)(pointsList.get(index).x()*PIXELS_PER_L);
                //find minimum and maximum x coordinates for width
                xMax = Math.max(x[index], xMax);
                xMin = Math.min(x[index], xMin);
                y[index] = (int)(pointsList.get(index).y()*PIXELS_PER_L);
                //find minimum and maximum y coordinates for height
                yMax = Math.max(y[index], yMax);
                yMin = Math.min(y[index], yMin);
            }
            
            int width = xMax - xMin;
            int height = yMax - yMin;
                        
            g2.setColor(gadget.getColor());
            if (sides.size() == 0) { //circle bumper
                g2.fill(new Ellipse2D.Double((gadget.getLocation().x()-.5)*PIXELS_PER_L, (gadget.getLocation().y()-.5)*PIXELS_PER_L, 
                        PIXELS_PER_L, PIXELS_PER_L)); //fixed width and height = 1L (given)
            } else if (sides.size() == 4) { //square bumper or absorber
                g2.fill(new Rectangle2D.Double(gadget.getLocation().x()*PIXELS_PER_L, gadget.getLocation().y()*PIXELS_PER_L, width, height));
            } else { //triangle bumper
                g2.fillPolygon(x,y,x.length);
            }
        }
        
        //include the walls of the board, which aren't drawn
        gadgets.add(board.getWalls());
    
        // iterate through balls (moving gadgets)
        Set<GameExpression> balls = board.getBalls();
        for (GameExpression b : balls) {
            Ball ball = (Ball) b; //only Ball objects, a variant of GameExpression, are stored in the set of balls
            double radius = ball.getBallCircle().getRadius();
            
            if (!ball.getAbsorbed()) { //for balls that are not currently absorbed
                boolean collide = false;
                // collide repeatedly
                if ((long)ball.getNextCollisionTime() <= timeOfLastDraw) {
                    collide = true;
                    GameExpression collisionGadget = ball.getNextCollision();
                    collisionGadget.collision(ball);
                    
                    // move the ball according to time remaining after collision
                    long timePastCollision = timeOfLastDraw - (long)ball.getNextCollisionTime(); //in milliseconds
                    
                    while (timePastCollision > 0 && !ball.getAbsorbed()) {
                        //calculate next collision time
                        long nextCollisionTime = Long.MAX_VALUE/2;
                        GameExpression nextCollisionGadget = null;
                        for (GameExpression gadget : gadgets) {
                            double collision = gadget.timeUntilCollision(ball);
                            if ((long)(collision*1000.) < nextCollisionTime && collision > 0) { //check for overflow with Infinity + Infinity
                                nextCollisionTime = (long)(collision*1000.);
                                nextCollisionGadget = gadget;
                            }
                        }
                        if (nextCollisionTime < timePastCollision) { //another collision occurred within this timestep
                            ball.setLocation(ball.getLocation().plus(ball.getVelocity().times(nextCollisionTime/1000.)));
                            
                            nextCollisionGadget.collision(ball);
                            timePastCollision -= nextCollisionTime;

                        } else { //no other collisions occur
                            ball.setLocation(ball.getLocation().plus(ball.getVelocity().times(timePastCollision/1000.)));
                            timePastCollision = 0;
                        }
                    }
                   
                }
                
                //update the velocity of the ball according to gravity
                Vect velocity = ball.getVelocity();  
                Vect tempVelocity = new Vect(velocity.x(),velocity.y()+board.getGravity()*timeSinceLastDraw/1000.);
                double mu = board.getFriction1();
                double mu2 = board.getFriction2();
                //update velocity of the ball according to friction (use approximation equation provided in lab specification)
                Vect newVelocity = tempVelocity.times(1-mu*timeSinceLastDraw/1000.-mu2*tempVelocity.length()*timeSinceLastDraw/1000.);
                ball.setVelocity(newVelocity);
                
                
                //calculate next collision time given current velocity
                long mintime = Long.MAX_VALUE/2;
                GameExpression collideGadget = null;
                for (GameExpression gadget : gadgets) {
                    double collision = gadget.timeUntilCollision(ball);
                    //compute next collision time with this gadget, account for overflow
                    if ((long)(collision*1000.) + timeOfLastDraw < mintime && (long)(collision*1000.) + timeOfLastDraw > 0 && collision > 0) { 
                        mintime = (long)(collision*1000.) + timeOfLastDraw;
                        collideGadget = gadget;
                    }
                }
                //set the next collision time and collision gadget of the ball
                ball.setNextCollisionTime(mintime);
                ball.setNextCollision(collideGadget);
               
                if (!collide) {
                    // check if ball will collide in next step
                    if ((long)ball.getNextCollisionTime() - timeOfLastDraw < timeSinceLastDraw) {
                        // move to collision point, do not overlap with gadget
                        timeSinceLastDraw = (long)ball.getNextCollisionTime() - timeOfLastDraw; 
                    }
                    double X = Math.max(radius,Math.min(GAMEBOARD_SIZE-radius, ball.getLocation().plus(ball.getVelocity().times(timeSinceLastDraw/1000.)).x())); //convert to seconds
                    double Y = Math.max(radius,Math.min(GAMEBOARD_SIZE-radius, ball.getLocation().plus(ball.getVelocity().times(timeSinceLastDraw/1000.)).y())); //convert to seconds
                    Vect newLocation = new Vect(X,Y);
                    ball.setLocation(newLocation);
                }
                // draw the ball
                g2.setColor(ball.getColor());
                g2.fill(new Ellipse2D.Double((ball.getLocation().x()-radius)*PIXELS_PER_L, (ball.getLocation().y()-radius)*PIXELS_PER_L, 2*radius*PIXELS_PER_L, 2*radius*PIXELS_PER_L));  
            }
        }
    }
}
