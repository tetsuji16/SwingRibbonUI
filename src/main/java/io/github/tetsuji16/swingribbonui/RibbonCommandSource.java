package io.github.tetsuji16.swingribbonui;

import javax.swing.Action;

/** Resolves a host application's command without coupling the ribbon to that application. */
@FunctionalInterface
public interface RibbonCommandSource {
	Action actionFor(String commandId);
}
