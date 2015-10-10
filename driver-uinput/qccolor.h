// qccolor.h -- Some color effects for your terminal!
//
// By quadcore-dev/loopyd -- Robert Smith
// lupinedreamexpress@gmail.com
//
// Replacement for conio.h.  This module contains some
// eyecandy for your terminal.  Including a color set
// subroutine and a neat little progress bar routine!
//
// By quadcore-dev on GitHub
// "We write code because we like to!"
//
#pragma once

#define RESET       0
#define BRIGHT      1
#define DIM         2
#define UNDERLINE   3
#define BLINK       4
#define REVERSE     7
#define HIDDEN      8
#define BLACK       0
#define RED         1
#define GREEN       2
#define YELLOW      3
#define BLUE        4
#define MAGENTA     5
#define CYAN        6
#define WHITE       7

void textcolor(int attr, int fg, int bg);
void reset_screen(void);
void progbar(short ipVal, short ipMaxVal);

// textcolor by QuadCore -- Sets text color at current cursor 
// position.
//
// int attr - Color attribute
// int fg   - Foreground color
// int bg   - Background color
//
void textcolor(int attr, int fg, int bg)
{   
    char command[13];
    sprintf(command, "%c[%d;%d;%dm", 0x1B, attr, fg + 30, bg + 40);
    printf("%s", command);
}

// cls_screen by QuadCore -- Clears screen of all text
//
// Nothing.  (void)
//
void cls_screen(void)
{
    system("reset");
    return;
}

// progbar by QuadCore -- Draw a simple progress bar
//
// int ipVal    = Current Value.
// int ipMaxVal = Max Value.
//
// Don't pass large values or you'll get a large progress 
// bar!  Convert them first using a ratio!  Also don't
// pass 0 for ipMaxVal or you get divison by zero crash!
//
void progbar(short ipVal, short ipMaxVal) {
	
	short iProgC = ipVal;
	
	int ipColor = 0;
	if ((float) ((float) ipVal / (float) ipMaxVal) <= 0.25f) {
		ipColor = BLUE;
	}
	if ((float) ((float) ipVal / (float) ipMaxVal) > 0.25f) {
		ipColor = GREEN;
	}
	if ((float) ((float) ipVal / (float) ipMaxVal) > 0.65f) {
		ipColor = YELLOW;
	}
	if ((float) ((float) ipVal / (float) ipMaxVal) > 0.85f) {
		ipColor = RED;
 	}
	
	iProgC = ipMaxVal - ipVal;
        while (iProgC++ <= ipMaxVal && iProgC != 0) {
		textcolor (DIM, ipColor, ipColor);
                printf (" ");
        }
	iProgC = ipVal;
	while (iProgC++ <= ipMaxVal) {
		textcolor (DIM, WHITE, WHITE);
		printf ("_"); 
	}
}
