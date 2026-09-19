package io.github.tetsuji16.swingribbonui;

import java.util.List;
import java.util.Objects;

/** A top-level ribbon tab. Contextual tabs are opt-in through {@link SwingRibbon#setContextualTabs}. */
public record RibbonTab(String id, String title, List<RibbonBand> bands, boolean contextual) {
	public RibbonTab {
		Objects.requireNonNull(id);
		Objects.requireNonNull(title);
		bands = List.copyOf(bands);
	}

	public RibbonTab(String id, String title, List<RibbonBand> bands) {
		this(id, title, bands, false);
	}
}
