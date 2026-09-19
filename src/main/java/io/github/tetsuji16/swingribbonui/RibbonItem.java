package io.github.tetsuji16.swingribbonui;

import java.util.Objects;

import javax.swing.Icon;

/** Immutable metadata for one host-owned ribbon command. */
public record RibbonItem(String id, String text, Icon icon, Size size) {
	public enum Size { LARGE, MEDIUM, SMALL }

	public RibbonItem {
		Objects.requireNonNull(id);
		Objects.requireNonNull(text);
		Objects.requireNonNull(size);
	}

	public RibbonItem(String id, String text) {
		this(id, text, null, Size.MEDIUM);
	}
}
