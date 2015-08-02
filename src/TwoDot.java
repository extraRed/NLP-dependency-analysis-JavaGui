import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.geom.Ellipse2D.*;

class TwoDot {
	Dot firstDot;
	Dot secondDot;
	private int state;
	public static final int NO_DOT = 0;
	public static final int ONE_DOT = 1;
	public static final int TWO_DOT = 2;

	public TwoDot() {
		firstDot = null;
		secondDot = null;
		state = NO_DOT;
	}

	public void addOneDot(Dot dot) {
		if (state == NO_DOT || state == TWO_DOT) {
			firstDot = dot;
			firstDot.setColor(Dot.SELECTED_COLOR);
			firstDot.setSelectedState(true);
			state = ONE_DOT;
		} else if (state == ONE_DOT) {
			if (dot == firstDot) {
				firstDot.setToMyNormalColor();
				firstDot.setSelectedState(false);
				state = NO_DOT;
				return;
			}
			secondDot = dot;
			secondDot.setColor(Dot.SELECTED_COLOR);
			secondDot.setSelectedState(true);
			state = TWO_DOT;
		}
	}

	public int getState() {
		return state;
	}

	public void resetState() {
		if (state > NO_DOT) {
			firstDot.setToMyNormalColor();
			firstDot.setSelectedState(false);
		}
		if (state > ONE_DOT) {
			secondDot.setToMyNormalColor();
			secondDot.setSelectedState(false);
		}
		state = NO_DOT;
	}

	public Dot getFirstDot() {
		return firstDot;
	}

	public Dot getSecondDot() {
		return secondDot;
	}
}
