package io.github.tetsuji16.swingribbonui;

import java.util.List;
import java.util.Objects;

/** A titled command group within a ribbon tab. */
public record RibbonBand(String id, String title, List<RibbonItem> items) {
	public RibbonBand {
		Objects.requireNonNull(id);
		Objects.requireNonNull(title);
		items = List.copyOf(items);
	}
}
