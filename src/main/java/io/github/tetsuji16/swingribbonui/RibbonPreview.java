package io.github.tetsuji16.swingribbonui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
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
					new RibbonItem("new", "New", null, RibbonItem.Size.LARGE),
					new RibbonItem("open", "Open", null, RibbonItem.Size.LARGE),
					new RibbonItem("save", "Save", null, RibbonItem.Size.LARGE))))),
				new RibbonTab("home", "Home", List.of(
					new RibbonBand("clipboard", "Clipboard", List.of(new RibbonItem("paste", "Paste"), new RibbonItem("copy", "Copy"))),
					new RibbonBand("editing", "Editing", List.of(new RibbonItem("find", "Find"), new RibbonItem("replace", "Replace"))))),
				new RibbonTab("view", "View", List.of(new RibbonBand("window", "Window", List.of(
					new RibbonItem("zoom", "Zoom"), new RibbonItem("arrange", "Arrange")))))
			), id -> new AbstractAction(id) {
				@Override public void actionPerformed(java.awt.event.ActionEvent event) { }
			});
			ribbon.selectTab("home");
			JPanel canvas = new JPanel(new java.awt.BorderLayout());
			canvas.setBackground(new Color(0xf1f3f5));
			canvas.add(ribbon, java.awt.BorderLayout.NORTH);
			canvas.setSize(new Dimension(1200, 180));
			layoutRecursively(canvas);
			BufferedImage image = new BufferedImage(1200, 180, BufferedImage.TYPE_INT_ARGB);
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
}
