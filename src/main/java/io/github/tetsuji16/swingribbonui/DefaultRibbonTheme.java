package io.github.tetsuji16.swingribbonui;

import java.awt.Color;
import java.awt.Font;

/** A compact, dependency-free Office-inspired default theme. */
public final class DefaultRibbonTheme implements RibbonTheme {
	public Color chromeBackground() { return new Color(0xf7f8fa); }
	public Color surfaceBackground() { return Color.WHITE; }
	public Color borderColor() { return new Color(0xd4d8dd); }
	public Color selectedTabColor() { return new Color(0x0f6cbd); }
	public Color unselectedTabColor() { return new Color(0x343a40); }
	public Font tabFont() { return new Font(Font.SANS_SERIF, Font.PLAIN, 13); }
	public Font commandFont() { return new Font(Font.SANS_SERIF, Font.PLAIN, 12); }
	public int tabHeight() { return 30; }
	public int commandHeight() { return 88; }
}
