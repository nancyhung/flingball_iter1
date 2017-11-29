package flingball;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.mit.eecs.parserlib.ParseTree;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;
import edu.mit.eecs.parserlib.Visualizer;

/**
 * BoardParser compiles a grammar, which lays out which words have meaning to our game
 * (i.e. ball, absorber, other gadgets, etc.), parses files to be processed into a game board,
 * and creates an abstract syntax tree that organizes the gadgets and their individual properties
 * on the board.
 */
public class BoardParser {
    
    /**
     * Method used for testing to parse sample files
     * @param args additional arguments, should be empty
     * @throws UnableToParseException if file cannot be opened
     */
    public static void main(final String[] args) throws UnableToParseException {
        // saves as board object to instantiate all the objects and their locations
        File f = new File("test/flingball/default.fb");
        BoardParser.parse(f);
    }
    
    // the nonterminals of the grammar
    private enum BoardGrammar {
        BOARD, BALL, GADGETS, 
        ACTION, SQUAREBUMPER, TRIANGLEBUMPER, CIRCLEBUMPER, 
        ABSORBER, ORIENTATION,
        COMMENT, CONTENTTOSKIP, WHITESPACE, INTEGER, FLOAT,
        NAME, GRAVITY, FRICTION1, FRICTION2
    }
    
    private static Parser<BoardGrammar> parser = makeParser();
    
    /**
     * Compile grammar into parser, which is given type from ParserLib package.
     * @param grammarFilename is a valid grammar file as described in p-set3
     *       <b>Must be in this class's Java package.</b>
     * @return parser for the grammar
     * @throws RuntimeException if grammar file can't be read or has syntax errors
     */
    
    private static Parser<BoardGrammar> makeParser() {
        try {
            // read the grammar as a file, relative to the project root.
            final InputStream grammarStream = GameExpression.class.getResourceAsStream("Board.g");
            return Parser.compile(grammarStream, BoardGrammar.BOARD);
        }
        // Parser.compile() throws two checked exceptions.
        // Translate these checked exceptions into unchecked RuntimeExceptions,
        // because these failures indicate internal bugs rather than client errors
        catch (IOException e) {
            throw new RuntimeException("can't read the grammar file", e);
        } catch (UnableToParseException e) {
            throw new RuntimeException("the grammar has a syntax error", e);
        }
    }
    
    /**
     * Parse file into a GameBoard that contains info on all the properties and gadgets within
     * that board.
     * @param file is the file we pass in to initialize the board. File must have a board
     *      with properties that are grammatically correct. It may contain gadgets, ball,
     *      and actions, but it must contain a non-zero length name. The file cannot fire trigger
     *      more than once on the same cause of the trigger.
     * @throws UnableToParseException if expression could not be made. This could include
     *      grammar errors or errors in this method
     */
    public static GameExpression parse(final File file) throws UnableToParseException {
        // parse the example into a parse tree
        try {
            final ParseTree<BoardGrammar> parseTree = parser.parse(file);
            // show in browser parse tree
            Visualizer.showInBrowser(parseTree);
            // make an AST from the parse tree
            final GameExpression expression = makeAbstractSyntaxTree(parseTree);
            
            return expression;
        }
        catch (IOException e) {
            throw new IllegalArgumentException("trouble reading file");
        }
    }
    
    /**
     * Helper function to makeAbstractSyntaxTree in order to make a SquareBumper object
     * from the given expression, specifically the name, x position, and y position.
     * @param parseTree is valid parse tree as defined by pset3 and given concrete syntax tree
     *        from ParserLib. As a precondition, the file must have this info if a SquareBumper
     *        is going to be rendered on the board. This guarantees parseTree will contain the 
     *        correct number of children to make object
     * @return SquareBumper based on information parsed in given the parse tree
     */
    private static GameExpression makeSquareBumper(final ParseTree<BoardGrammar> parseTree) {
        final List<ParseTree<BoardGrammar>> children = parseTree.children();
        String name = children.get(0).text();
        int x = Integer.parseInt(children.get(1).text());
        int y = Integer.parseInt(children.get(2).text());
        return new SquareBumper(name, x, y);
    }
    
    /**
     * Helper function to makeAbstractSyntaxTree in order to make a TriangleBumper object
     * from the given expression, specifically the name, x position, y position, and orientation.
     * Orientation is default to 0 if no info is in the file we are parsing. Otherwise, we set the
     * orientation that is given from the file.
     * @param parseTree is valid parse tree as defined by pset3 and given concrete syntax tree
     *        from ParserLib as well as specification of parseTree in makeSquareBumper.
     * @return TriangleBumper based on information parsed in given the parse tree
     */
    
    private static GameExpression makeTriangleBumper(final ParseTree<BoardGrammar> parseTree) {
        final List<ParseTree<BoardGrammar>> children = parseTree.children();
        String name = children.get(0).text();
        int x = Integer.parseInt(children.get(1).text());
        int y = Integer.parseInt(children.get(2).text());
        if (children.size()>3) {
            return new TriangleBumper(name, x, y, Integer.parseInt(children.get(3).text()));
        }
        else {
            return new TriangleBumper(name, x, y, 0);
        }
    }
    
    /**
     * Helper function to makeAbstractSyntaxTree in order to make a CircleBumper object
     * from the given expression, specifically the name, x position, and y position.
     * @param parseTree is valid parse tree as defined by pset3 and given concrete syntax tree
     *        from ParserLib as well as specification of parseTree in makeSquareBumper.
     * @return CircleBumper based on information parsed in given the parse tree
     */
    private static GameExpression makeCircleBumper(final ParseTree<BoardGrammar> parseTree) {
        final List<ParseTree<BoardGrammar>> children = parseTree.children();
        String name = children.get(0).text();
        int x = Integer.parseInt(children.get(1).text());
        int y = Integer.parseInt(children.get(2).text());
        return new CircleBumper(name, x, y);
    }
    
    /**
     * Helper function to makeAbstractSyntaxTree in order to make an Absorber object
     * from the given expression, specifically the name, x position, y position, width, and height.
     * @param parseTree is valid parse tree as defined by pset3 and given concrete syntax tree
     *        from ParserLib as well as specification of parseTree in makeSquareBumper.
     * @return Absorber based on information parsed in given the parse tree
     */
    private static GameExpression makeAbsorber(final ParseTree<BoardGrammar> parseTree) {
        final List<ParseTree<BoardGrammar>> children = parseTree.children();
        String name = children.get(0).text();
        int x = Integer.parseInt(children.get(1).text());
        int y = Integer.parseInt(children.get(2).text());
        int width = Integer.parseInt(children.get(3).text());
        int height = Integer.parseInt(children.get(4).text());
        return new Absorber(name, x, y, width, height);
    }
    
    /**
     * Helper function to makeAbstractSyntaxTree in order to make an Ball object
     * from the given expression, specifically the name, x position, y position, x velocity, and y velocity.
     * @param parseTree is valid parse tree as defined by pset3 and given concrete syntax tree
     *        from ParserLib as well as specification of parseTree in makeSquareBumper.
     * @return Ball based on information parsed in given the parse tree
     */
    private static GameExpression makeBall(final ParseTree<BoardGrammar> parseTree) {
        final List<ParseTree<BoardGrammar>> children = parseTree.children();
        String name = children.get(0).text();
        double x = Double.parseDouble(children.get(1).text());
        double y = Double.parseDouble(children.get(2).text());
        double xVelocity = Double.parseDouble(children.get(3).text());
        double yVelocity = Double.parseDouble(children.get(4).text());
        return new Ball(name, x, y, xVelocity, yVelocity);
    }
    
    /**
     * Makes the Abstract Syntax Tree given a parsed concrete syntax tree. Ensures that all the gadgets in game
     * have unique identifiers.
     * @param parseTree is valid parse tree as defined by pset3 and given concrete syntax tree
     *        from ParserLib as well as specification of parseTree in makeSquareBumper.
     * @return GameExpression the board object that contains all the information on what gadgets, balls,
     *        and relationships the objects on the board have with each other
     */
    private static GameExpression makeAbstractSyntaxTree(final ParseTree<BoardGrammar> parseTree) {
        switch(parseTree.name()) {
        case BOARD:
            {
                // regardless of the number of children, the first 2 are the name and the gravity
                
                final List<ParseTree<BoardGrammar>> children = parseTree.children();
                
                String name = "";
                double gravity = 25.0;
                double friction1 = 0.025;
                double friction2 = 0.025;
                Set<GameExpression> gadgets = new HashSet<>();
                Set<String> namesOfGadgets = new HashSet<>();
                Set<GameExpression> balls = new HashSet<>();
                
                for(int index=0; index < children.size(); ++index) {
                    
                    switch(children.get(index).name()) {
                    case NAME:
                    {
                        name = children.get(index).text();
                        continue;
                    }
                    
                    case GADGETS:
                    {
                        final List<ParseTree<BoardGrammar>> childrenGadgets = children.get(index).children();
                        for(int i=0; i<childrenGadgets.size(); ++i) {
                            ParseTree<BoardGrammar> gadget = childrenGadgets.get(i);
                            // parse the entire board starting here
                            switch(gadget.name()) {
                            case SQUAREBUMPER: // squareBumper ::= 'squareBumper' 'name=' NAME 'x' '=' INTEGER 'y' '=' INTEGER [\n]+;
                            {
                                GameExpression squareBumper = makeSquareBumper(gadget);
                                // check unique id
                                assert !(namesOfGadgets.contains(squareBumper.getName()));
                                // add to list of gadgets
                                gadgets.add(squareBumper);
                                namesOfGadgets.add(squareBumper.getName());
                                break;
                                // name, x, y
                            }
                            
                            case CIRCLEBUMPER: // circleBumper ::= 'circleBumper' 'name=' NAME 'x' '=' INTEGER 'y' '=' INTEGER [\n]+;
                            {
                                GameExpression circleBumper = makeCircleBumper(gadget);
                                // check unique id
                                assert !(namesOfGadgets.contains(circleBumper.getName()));
                                // add to list of gadgets gadgets
                                gadgets.add(circleBumper);
                                namesOfGadgets.add(circleBumper.getName());
                                break;
                                // name, x, y
                            }
                            
                            case TRIANGLEBUMPER: // triangleBumper ::= 'triangleBumper' 'name=' NAME 'x' '=' INTEGER 'y' '=' INTEGER ('orientation' '=' ORIENTATION)? [\n]+;
                            {
                                GameExpression triangleBumper = makeTriangleBumper(gadget);
                                // check unique id
                                assert !(namesOfGadgets.contains(triangleBumper.getName()));
                                // add to list of gadgets gadgets
                                gadgets.add(triangleBumper);
                                namesOfGadgets.add(triangleBumper.getName());
                                break;
                                // name, x, y
                            }
                            
                            case ABSORBER: // absorber ::= 'absorber' 'name' '=' NAME 'x' '=' INTEGER 'y' '=' INTEGER 'width' '=' INTEGER 'height' '=' INTEGER [\n]+;
                            {
                                GameExpression absorber = makeAbsorber(gadget);
                                // check unique id
                                assert !(namesOfGadgets.contains(absorber.getName()));
                                // add to list of gadgets gadgets
                                gadgets.add(absorber);
                                namesOfGadgets.add(absorber.getName());
                                break;
                                // name, x, y, width, height
                            }
                            }
                        }
                        break;
                    }
                    
                    case GRAVITY: // GRAVITY ::=  FLOAT;
                    {
                        gravity = Double.parseDouble(children.get(index).text());
                        break;
                        
                    }
                    
                    case FRICTION1: // friction1 ::= 'friction1' '=' FLOAT;
                    {
                        String[] components = children.get(index).text().split("=");
                        friction1 = Double.parseDouble(components[components.length-1]);
                        break;
                    }
                    
                    case FRICTION2: // friction2 ::= 'friction2' '=' FLOAT;
                    {
                        String[] components = children.get(index).text().split("=");
                        friction2 = Double.parseDouble(components[components.length-1]);
                        break;
                    }
                    
                    case BALL: // ball ::= 'ball' 'name' '=' NAME 'x=' FLOAT 'y=' FLOAT 'xVelocity=' FLOAT 'yVelocity=' FLOAT [\n]+;
                    {
                        GameExpression ballObject = makeBall(children.get(index));
                        // check unique id
                        assert !(namesOfGadgets.contains(ballObject.getName()));
                        // add to list of gadgets
                        balls.add(ballObject);
                        namesOfGadgets.add(ballObject.getName());
                        break;
                        // name, x, y, xVelocity, yVelocity
                    }
                    
                    case ACTION: // ACTION ::= 'fire trigger' '=' NAME 'action=' NAME [\n]+;
                    {
                        // first child of action = cause object
                        ParseTree<BoardGrammar> object1 = children.get(index).children().get(0);
                        // 2nd child of action = object affected by trigger
                        ParseTree<BoardGrammar> object2 = children.get(index).children().get(1);
                        assert namesOfGadgets.contains(object1.text()) && namesOfGadgets.contains(object2.text());
                        
                        GameExpression gadget1 = null;
                        GameExpression gadget2 = null;
                        for (GameExpression x : gadgets) {
                            if (x.getName().equals(object1.text())) {
                                gadget1 = x;
                            }
                            if (x.getName().equals(object2.text())) {
                                gadget2 = x;
                            }
                        }
                        // change state of cause
                        gadget1.setTrigger(gadget2);
                        break;
                    }
                    
                    default:
                        throw new AssertionError("should never get here");
                    }
                    }
                return new Board(name, gravity, friction1, friction2, gadgets, balls);
            }
        default:
            throw new AssertionError("should never get here");
        }
    }
}
