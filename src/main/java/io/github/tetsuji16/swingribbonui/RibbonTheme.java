package io.github.tetsuji16.swingribbonui;

import java.awt.Color;
import java.awt.Font;

/** Look-and-feel contract; applications may supply a theme without a FlatLaf dependency. */
public interface RibbonTheme {
	Color chromeBackground();
	Color surfaceBackground();
	Color borderColor();
	Color selectedTabColor();
	Color unselectedTabColor();
	Font tabFont();
	Font commandFont();
	int tabHeight();
	int commandHeight();
}
