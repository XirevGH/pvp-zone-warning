package com.pvpzonewarning;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;


public class PvPZoneWarningOverlay extends Overlay {

    private final Client client;
    private final PvPZoneWarningPlugin plugin;
    private final PvPZoneWarningConfig config;


    @Inject
    private PvPZoneWarningOverlay(Client client, PvPZoneWarningPlugin plugin, PvPZoneWarningConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (System.currentTimeMillis() < plugin.getWarningDisplayUntil()) {

            Font originalFont = graphics.getFont();
            try {
                int fontSize = config.centerTextFontSize();
                String text = config.centerTextMessage();

                Font newFont = originalFont.deriveFont((float) fontSize); // Cast size to float for deriveFont

                graphics.setFont(newFont);

                final Rectangle viewport = new Rectangle(
                        client.getViewportXOffset(),
                        client.getViewportYOffset(),
                        client.getViewportWidth(),
                        client.getViewportHeight()
                );

                if (viewport.width <= 0 || viewport.height <= 0) {
                    return null;
                }

                FontMetrics metrics = graphics.getFontMetrics();
                int textWidth = metrics.stringWidth(text);
                int textAscent = metrics.getAscent();

                int drawX = viewport.x + (viewport.width - textWidth) / 2;
                int drawY = viewport.y + (viewport.height + textAscent) / 2;

                graphics.setColor(config.centerTextColor());

                graphics.drawString(text, drawX, drawY);

            } finally {
                graphics.setFont(originalFont);
            }
        }
        return null;
    }
}