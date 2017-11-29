@skip contentToSkip {
    BOARD ::= 'board' 'name' '=' NAME ('gravity' '=' GRAVITY)? (friction1)? (friction2)? [\n]+ (ball)* (GADGETS)* (ACTION)*;
    GADGETS ::= squareBumper | circleBumper | triangleBumper | absorber;
    ACTION ::= 'fire trigger' '=' NAME 'action=' NAME [\n]+;
    squareBumper ::= 'squareBumper' 'name=' NAME 'x' '=' INTEGER 'y' '=' INTEGER [\n]+;
    circleBumper ::= 'circleBumper' 'name=' NAME 'x' '=' INTEGER 'y' '=' INTEGER [\n]+;
    triangleBumper ::= 'triangleBumper' 'name=' NAME 'x' '=' INTEGER 'y' '=' INTEGER ('orientation' '=' ORIENTATION)? [\n]+;
    ball ::= 'ball' 'name' '=' NAME 'x=' FLOAT 'y=' FLOAT 'xVelocity=' FLOAT 'yVelocity=' FLOAT [\n]+;
    absorber ::= 'absorber' 'name' '=' NAME 'x' '=' INTEGER 'y' '=' INTEGER 'width' '=' INTEGER 'height' '=' INTEGER [\n]+;
    
    friction1 ::= 'friction1' '=' FLOAT;
    friction2 ::= 'friction2' '=' FLOAT;
    GRAVITY ::=  FLOAT;
    ORIENTATION ::= ('0'|'90'|'180'|'270');
}
contentToSkip ::= whitespace | comment;
comment ::= '#' [^\n]* '\n';
whitespace ::= [ \t\r]+;
INTEGER ::= [0-9]+;
FLOAT ::= '-'?([0-9]+'.'[0-9]* | '.'?[0-9]+);
NAME ::= [A-Za-z_][A-Za-z_0-9]*;
