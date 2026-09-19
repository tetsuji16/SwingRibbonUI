package io.github.tetsuji16.swingribbonui;

import java.util.List;
import java.util.Objects;

import javax.swing.Icon;

/** Immutable metadata for one host-owned ribbon command. */
public record RibbonItem(String id, String text, Icon icon, Size size, RibbonControlKind kind,
	List<RibbonMenuItem> menuItems) {
	public enum Size { LARGE, MEDIUM, SMALL }

	public RibbonItem {
		Objects.requireNonNull(id);
		Objects.requireNonNull(text);
		Objects.requireNonNull(size);
		Objects.requireNonNull(kind);
		menuItems = List.copyOf(menuItems);
	}

	public RibbonItem(String id, String text, Icon icon, Size size) {
		this(id, text, icon, size, RibbonControlKind.BUTTON, List.of());
	}

	public RibbonItem(String id, String text, Icon icon, Size size, RibbonControlKind kind) {
		this(id, text, icon, size, kind, List.of());
	}

	public RibbonItem(String id, String text) {
		this(id, text, null, Size.MEDIUM, RibbonControlKind.BUTTON, List.of());
	}
}
