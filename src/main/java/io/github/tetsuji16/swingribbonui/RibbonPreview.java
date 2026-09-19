package io.github.tetsuji16.swingribbonui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Generates the README image without reading or capturing the user desktop. */
public final class RibbonPreview {
	private RibbonPreview() { }

	public static void main(String[] arguments) throws Exception {
		SwingUtilities.invokeAndWait(RibbonPreview::render);
	}

	private static void render() {
		try {
			SwingRibbon ribbon = new SwingRibbon(List.of(
				new RibbonTab("file", "File", List.of(new RibbonBand("document", "Document", List.of(
					new RibbonItem("new", "New", new PreviewIcon(PreviewIcon.Kind.NEW), RibbonItem.Size.LARGE),
					new RibbonItem("open", "Open", new PreviewIcon(PreviewIcon.Kind.OPEN), RibbonItem.Size.LARGE),
					new RibbonItem("save", "Save", new PreviewIcon(PreviewIcon.Kind.SAVE), RibbonItem.Size.LARGE))))),
				new RibbonTab("home", "Home", List.of(
					new RibbonBand("clipboard", "Clipboard", List.of(
						new RibbonItem("paste", "Paste", new PreviewIcon(PreviewIcon.Kind.PASTE), RibbonItem.Size.MEDIUM),
						new RibbonItem("copy", "Copy", new PreviewIcon(PreviewIcon.Kind.COPY), RibbonItem.Size.MEDIUM))),
					new RibbonBand("font", "Font", List.of(
						new RibbonItem("bold", "Bold", new PreviewIcon(PreviewIcon.Kind.NEW), RibbonItem.Size.MEDIUM),
						new RibbonItem("color", "Color", new PreviewIcon(PreviewIcon.Kind.REPLACE), RibbonItem.Size.MEDIUM))),
					new RibbonBand("paragraph", "Paragraph", List.of(
						new RibbonItem("bullets", "Bullets", new PreviewIcon(PreviewIcon.Kind.ARRANGE), RibbonItem.Size.MEDIUM),
						new RibbonItem("align", "Align", new PreviewIcon(PreviewIcon.Kind.COPY), RibbonItem.Size.MEDIUM))),
					new RibbonBand("editing", "Editing", List.of(
						new RibbonItem("find", "Find", new PreviewIcon(PreviewIcon.Kind.FIND), RibbonItem.Size.MEDIUM),
						new RibbonItem("replace", "Replace", new PreviewIcon(PreviewIcon.Kind.REPLACE), RibbonItem.Size.MEDIUM))))),
				new RibbonTab("view", "View", List.of(new RibbonBand("window", "Window", List.of(
					new RibbonItem("zoom", "Zoom", new PreviewIcon(PreviewIcon.Kind.ZOOM), RibbonItem.Size.MEDIUM),
					new RibbonItem("arrange", "Arrange", new PreviewIcon(PreviewIcon.Kind.ARRANGE), RibbonItem.Size.MEDIUM)))))
			), id -> new AbstractAction(id) {
				@Override public void actionPerformed(java.awt.event.ActionEvent event) { }
			});
			ribbon.selectTab("home");
			JPanel canvas = new JPanel(new java.awt.BorderLayout());
			canvas.setBackground(new Color(0xf1f3f5));
			canvas.add(ribbon, java.awt.BorderLayout.NORTH);
			canvas.setSize(new Dimension(1200, 86));
			layoutRecursively(canvas);
			BufferedImage image = new BufferedImage(1200, 86, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			try {
				canvas.printAll(graphics);
			} finally {
				graphics.dispose();
			}
			Path output = Path.of("docs", "images", "swing-ribbon-ui.png");
			Files.createDirectories(output.getParent());
			ImageIO.write(image, "png", output.toFile());
		} catch (IOException error) {
			throw new IllegalStateException("Unable to write the SwingRibbonUI preview", error);
		}
	}

	private static void layoutRecursively(Component component) {
		component.doLayout();
		if (component instanceof Container container) {
			for (Component child : container.getComponents()) layoutRecursively(child);
		}
	}

	private static final class PreviewIcon implements Icon {
		enum Kind { NEW, OPEN, SAVE, PASTE, COPY, FIND, REPLACE, ZOOM, ARRANGE }
		private final Kind kind;
		private PreviewIcon(Kind kind) { this.kind = kind; }
		@Override public int getIconWidth() { return 20; }
		@Override public int getIconHeight() { return 20; }
		@Override public void paintIcon(Component component, java.awt.Graphics graphics, int x, int y) {
			Graphics2D g = (Graphics2D) graphics.create();
			try {
				g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
				g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.setColor(switch (kind) {
					case NEW, PASTE -> new Color(0x2563eb);
					case OPEN, COPY -> new Color(0x7c3aed);
					case SAVE, ARRANGE -> new Color(0x059669);
					case FIND, ZOOM, REPLACE -> new Color(0xea580c);
				});
				switch (kind) {
					case NEW -> { g.drawRoundRect(x + 3, y + 2, 12, 16, 2, 2); g.drawLine(x + 9, y + 6, x + 9, y + 14); g.drawLine(x + 5, y + 10, x + 13, y + 10); }
					case OPEN -> { g.drawRoundRect(x + 2, y + 6, 16, 11, 2, 2); g.drawLine(x + 3, y + 6, x + 7, y + 2); g.drawLine(x + 7, y + 2, x + 12, y + 6); }
					case SAVE -> { g.drawRoundRect(x + 3, y + 2, 14, 16, 2, 2); g.drawLine(x + 6, y + 3, x + 6, y + 8); g.drawLine(x + 9, y + 12, x + 14, y + 12); }
					case PASTE -> { g.drawRoundRect(x + 5, y + 4, 11, 14, 2, 2); g.drawRoundRect(x + 7, y + 1, 7, 5, 2, 2); g.drawLine(x + 8, y + 11, x + 13, y + 11); }
					case COPY -> { g.drawRoundRect(x + 6, y + 3, 11, 13, 2, 2); g.drawRoundRect(x + 3, y + 6, 11, 11, 2, 2); }
					case FIND, ZOOM -> { g.drawOval(x + 3, y + 3, 10, 10); g.drawLine(x + 11, y + 11, x + 17, y + 17); if (kind == Kind.ZOOM) { g.drawLine(x + 8, y + 5, x + 8, y + 11); g.drawLine(x + 5, y + 8, x + 11, y + 8); } }
					case REPLACE -> { g.drawArc(x + 3, y + 3, 12, 12, 30, 210); g.drawLine(x + 14, y + 3, x + 17, y + 6); g.drawArc(x + 5, y + 5, 12, 12, 210, 210); }
					case ARRANGE -> { g.drawRoundRect(x + 2, y + 3, 6, 6, 1, 1); g.drawRoundRect(x + 12, y + 3, 6, 6, 1, 1); g.drawRoundRect(x + 2, y + 12, 6, 6, 1, 1); g.drawRoundRect(x + 12, y + 12, 6, 6, 1, 1); }
				}
			} finally { g.dispose(); }
		}
	}
}
