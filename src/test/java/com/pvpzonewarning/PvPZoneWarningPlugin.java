package com.pvpzonewarning;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.WorldType;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
        name = "PvP Zone Warning",
        description = "Warns when leaving a PvP safe zone via chat, notification (optional flash/sound), and center text.",
        tags = {"pvp", "safe", "zone", "warning", "wilderness", "combat"}
)
public class PvPZoneWarningPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private PvPZoneWarningConfig config;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private Notifier notifier;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PvPZoneWarningOverlay overlay;

    private boolean wasInPvPWorldSafeZone = false;
    private boolean wasInWilderness = false;
    private boolean wasOnPvPWorld = false;
    @Getter
    private long warningDisplayUntil = 0;
    private boolean checkLoginCondition = false;

    @Provides
    PvPZoneWarningConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PvPZoneWarningConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
        resetState();
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        resetState();
    }

    private void resetState() {
        wasInPvPWorldSafeZone = false;
        wasInWilderness = false;
        wasOnPvPWorld = false;
        warningDisplayUntil = 0;
        checkLoginCondition = false;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            checkLoginCondition = true;
        } else if (gameStateChanged.getGameState() == GameState.HOPPING ||
                gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
            resetState();
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            if (wasOnPvPWorld || wasInWilderness || warningDisplayUntil > 0 || checkLoginCondition) {
                resetState();
            }
            return;
        }

        boolean currentOnPvPWorld = client.getWorldType().contains(WorldType.PVP);
        boolean currentPvPSafeWidgetVisible = isPvPWorldSafeZoneWidgetVisible();
        boolean currentInWilderness = checkIsInWilderness();

        boolean currentlyUnsafe;

        if (currentInWilderness) {
            currentlyUnsafe = true;
        } else if (currentOnPvPWorld && !currentPvPSafeWidgetVisible) {
            currentlyUnsafe = true;
        } else {
            currentlyUnsafe = false;
        }

        boolean previouslyUnsafe;

        if (wasInWilderness) {
            previouslyUnsafe = true;
        } else if (wasOnPvPWorld && !wasInPvPWorldSafeZone) {
            previouslyUnsafe = true;
        } else {
            previouslyUnsafe = false;
        }

        if (checkLoginCondition) {
            if (currentlyUnsafe) {
                triggerWarnings();
            }
            checkLoginCondition = false;
        }

        else if (!previouslyUnsafe && currentlyUnsafe) {
            triggerWarnings();
        }

        wasOnPvPWorld = currentOnPvPWorld;
        wasInPvPWorldSafeZone = currentPvPSafeWidgetVisible;
        wasInWilderness = currentInWilderness;

        if (warningDisplayUntil > 0 && System.currentTimeMillis() < warningDisplayUntil) {
            if (!currentlyUnsafe) {
                warningDisplayUntil = 0;
            }
        }
    }

    private void triggerWarnings() {
        if (System.currentTimeMillis() < warningDisplayUntil && config.enableCenterText()) {
            return;
        }

        String notifyMsg = config.warningMessage();

        if (config.enableSystemNotification()) {
            notifier.notify(notifyMsg);
        }

        if (config.enableCenterText()) {
            warningDisplayUntil = System.currentTimeMillis() + (config.centerTextDurationSeconds() * 1000L);
        }
    }

    private boolean checkIsInWilderness() {
        return client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) > 0;
    }

    private boolean isPvPWorldSafeZoneWidgetVisible() {
        Widget safeZoneWidget = client.getWidget(InterfaceID.PvpIcons.PVPW_SAFE);

        return safeZoneWidget != null && !safeZoneWidget.isHidden();
    }
}