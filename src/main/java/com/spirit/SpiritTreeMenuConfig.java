package com.spirit;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("spirittreemenu")
public interface SpiritTreeMenuConfig extends Config
{
	public enum FontID
	{
		PLAIN("Plain", 495),
		BOLD("Bold", 496),
		QUILL("Quill", 497),
		BARBARIAN("Barbarian", 764),
		VERANDA("Veranda", 1447)
		;

		private String value;
		private int id;

		FontID(String value, int id)
		{
			this.value = value;
			this.id = id;
		}

		@Override
		public String toString()
		{
			return this.value;
		}

		public int toInt()
		{
			return this.id;
		}
	}





	@ConfigItem(
			keyName = "hideShortcut",
			name = "Hide keyboard shortcut",
			description = "Hide the keyboard shortcut next to the teleport location",
			position = 0
	)
	default boolean hideShortcuts()
	{
		return false;
	}

	@ConfigItem(
			keyName = "hideUnavailable",
			name = "Hide unavailable teleports",
			description = "Hides unavailable teleport locations",
			position = 1
	)
	default boolean hideUnavailable()
	{
		return false;
	}

	@ConfigItem(
			keyName = "fontID",
			name = "Font",
			description = "Configures the font for menu choices",
			position = 2
	)
	default FontID menuFont()
	{
		return FontID.QUILL;
	}
}
