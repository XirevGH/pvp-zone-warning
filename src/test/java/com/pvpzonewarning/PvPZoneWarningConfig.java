package com.pvpzonewarning;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("pvpzonewarning")
public interface PvPZoneWarningConfig extends Config {

    @ConfigItem(
            keyName = "warningMessage",
            name = "Warning Message (Chat/Notify)",
            description = "The message for chat and system notifications",
            position = 2
    )
    default String warningMessage() {
        return "WARNING: You are leaving a safe zone!";
    }

    @ConfigItem(
            keyName = "enableSystemNotification",
            name = "Enable Notification",
            description = "Send an OS-level notification (may include sound based on system settings) along with a chat message and screen flash (if enabled in the base RuneLite plugin)",
            position = 5
    )
    default boolean enableSystemNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "enableCenterText",
            name = "Enable Center Screen Text",
            description = "Show text in the middle of the screen temporarily",
            position = 6
    )
    default boolean enableCenterText() {
        return true;
    }

    @ConfigItem(
            keyName = "centerTextMessage",
            name = "Center Text Message",
            description = "Text to display in the center of the screen",
            position = 7
    )
    default String centerTextMessage() {
        return "LEAVING SAFE ZONE";
    }

    @Alpha
    @ConfigItem(
            keyName = "centerTextColor",
            name = "Center Text Color",
            description = "Color of the center screen text",
            position = 8
    )
    default Color centerTextColor() {
        return Color.RED;
    }

    @Range(min = 12, max = 72)
    @ConfigItem(
            keyName = "centerTextFontSize",
            name = "Center Text Font Size",
            description = "The font size for the center screen warning text.",
            position = 9
    )
    default int centerTextFontSize() {
        return 32;
    }

    @Range(min = 1, max = 10)
    @ConfigItem(
            keyName = "centerTextDuration",
            name = "Center Text Duration (sec)",
            description = "How long the center text stays on screen (in seconds)",
            position = 9
    )
    default int centerTextDurationSeconds() {
        return 3;
    }
}