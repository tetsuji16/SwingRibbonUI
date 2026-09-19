package io.github.tetsuji16.swingribbonui;

import java.util.Objects;

import javax.swing.Icon;

/** One host-owned entry in a drop-down or split-button menu. */
public record RibbonMenuItem(String id, String text, Icon icon) {
	public RibbonMenuItem {
		Objects.requireNonNull(id);
		Objects.requireNonNull(text);
	}

	public RibbonMenuItem(String id, String text) {
		this(id, text, null);
	}
}
