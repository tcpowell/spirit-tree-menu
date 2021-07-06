package com.spirit;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@PluginDescriptor(
		name = "Spirit Tree Menu"
)
public class SpiritTreeMenuPlugin extends Plugin
{

	private static final String MENU_TITLE = "Spirit Tree Locations";
	private static final int ADVENTURE_LOG_TITLE = 0;
	private static final int ADVENTURE_LOG_LIST = 3;
	private static final String MENU_REGEX = "(<col[^>]+>)(\\S+)(</col>):\\s+(.+)";
	private static final String KHAZARD_LONG = "Battlefield of Khazard";
	private static final String KHAZARD_SHORT = "B'field of Khazard";
	private static final String YOUR_HOUSE = "Your house";
	private static final int LEFT_PIXELS = 7;//left side is slightly wider to prevent wrapping on certain fonts
	private static final String CANCEL = "Cancel";
	private static final String INACTIVE_COLOR = "5f5f5f";

	@Inject
	private ClientThread clientThread;

	@Inject
	private Client client;

	@Inject
	private SpiritTreeMenuConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Spirit Tree Menu started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Spirit Tree Menu stopped!");
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e)
	{
		if (e.getGroupId() == WidgetID.ADVENTURE_LOG_ID) {
			log.info("Adventure Log loaded!");

			clientThread.invokeLater(() ->
			{
				if (client.getWidget(WidgetID.ADVENTURE_LOG_ID,ADVENTURE_LOG_TITLE).getChild(1).getText().equals(MENU_TITLE)) {

					Widget adventureLogList = client.getWidget(WidgetID.ADVENTURE_LOG_ID,ADVENTURE_LOG_LIST);


					int offset = 0;
					int newWidgetWidth = adventureLogList.getOriginalWidth()/2;
					int numEachCol = (int) Math.ceil((adventureLogList.getDynamicChildren().length-1)/2);
					int newWidgetHeight = (int) Math.floor(adventureLogList.getOriginalHeight()/numEachCol)-1;

					//loop through and resize/move
					for (Widget childWidget : adventureLogList.getDynamicChildren()) {

						//set position and text of new menu items (first half)
						if (childWidget.getIndex()-offset<numEachCol) {
							childWidget.setOriginalY(newWidgetHeight * (childWidget.getIndex()-offset));
							childWidget.setXTextAlignment(0);
							childWidget.setText(updatedText(childWidget.getText(), false));
							childWidget.setOriginalWidth(newWidgetWidth+LEFT_PIXELS);
						}

						//set position and text (second half)
						else {
							childWidget.setOriginalY((newWidgetHeight * (childWidget.getIndex()-numEachCol-offset)) );
							childWidget.setOriginalX(newWidgetWidth+LEFT_PIXELS);
							childWidget.setXTextAlignment(2);
							childWidget.setText(updatedText(childWidget.getText(), true));
							childWidget.setOriginalWidth(newWidgetWidth-LEFT_PIXELS);
						}

						//hide cancel (we have an odd number for now)
						if (childWidget.getText().contains(CANCEL)) {
							childWidget.setHidden(true);
						}

						//
						if (config.hideUnavailable()) {
							if (childWidget.getText().toLowerCase().contains(INACTIVE_COLOR)) {
								childWidget.setHidden(true);
								offset++;
							}
						}

						//height, font, width
						childWidget.setOriginalHeight(newWidgetHeight);
						childWidget.setFontId(config.menuFont().toInt());
						childWidget.setWidthMode(0);
						childWidget.setXPositionMode(0);

						//revalidate everything
						childWidget.revalidate();

						log.info(childWidget.getText());

					}
				}
			});
		}
	}

	private String updatedText(String text, boolean right) {

		Pattern menuItemPattern = Pattern.compile(MENU_REGEX);
		Matcher matcher = menuItemPattern.matcher(text);
		if (!matcher.matches())
			return text;

		String colOpen = matcher.group(1);
		String shortcutKey = matcher.group(2);
		String colClose = matcher.group(3);
		String teleportName =  matcher.group(4);


		if (teleportName.contains(YOUR_HOUSE))
			teleportName = YOUR_HOUSE;

		if (config.menuFont().toInt() > 500)
			if (teleportName.equals(KHAZARD_LONG))
				teleportName = KHAZARD_SHORT;

		if (config.hideShortcuts())
			return teleportName;
		else {
			if (right)
				return teleportName + " " + colOpen + "(" + shortcutKey + ")" + colClose;
			else
				return colOpen + "(" + shortcutKey + ")" + colClose + " " + teleportName;
		}
	}


	@Provides
	SpiritTreeMenuConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpiritTreeMenuConfig.class);
	}
}
