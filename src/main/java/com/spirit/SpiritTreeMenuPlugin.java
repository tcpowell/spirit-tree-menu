package com.spirit;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
		name = "Spirit Tree Menu",
		description = "Updates the Spirit Tree navigation menu",
		tags = {"spirit", "teleport" ,"tree"}
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
	protected void startUp()
	{
		log.debug("Spirit Tree Menu started!");
	}

	@Override
	protected void shutDown()
	{
		log.debug("Spirit Tree Menu stopped!");
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e)
	{
		if (e.getGroupId() == WidgetID.ADVENTURE_LOG_ID) {
			log.debug("Adventure Log loaded!");

			clientThread.invokeLater(() ->
			{
				if (Objects.requireNonNull(client.getWidget(WidgetID.ADVENTURE_LOG_ID, ADVENTURE_LOG_TITLE)).getChild(1).getText().equals(MENU_TITLE)) {

					Widget adventureLogList = client.getWidget(WidgetID.ADVENTURE_LOG_ID,ADVENTURE_LOG_LIST);
					List<Widget> treeLocations = new ArrayList<>();

					for (Widget childWidget : Objects.requireNonNull(adventureLogList).getDynamicChildren()) {
						if (config.hideUnavailable() && childWidget.getText().toLowerCase().contains(INACTIVE_COLOR)) {
							childWidget.setHidden(true);
						}
						else {
							treeLocations.add(childWidget);
						}
					}

					int numItems = treeLocations.size();
					int newWidgetWidth = adventureLogList.getOriginalWidth()/2;
					int numEachCol = (numItems - numItems%2)/2;
					int newWidgetHeight = (adventureLogList.getOriginalHeight()/numEachCol)-1;

					//loop through and resize/move
					for (int i=0; i<treeLocations.size(); i++) {
						Widget childWidget = treeLocations.get(i);

						//set position and text of new menu items (first half)
						if (i<numEachCol) {
							childWidget.setOriginalY(newWidgetHeight * (i));
							childWidget.setXTextAlignment(0);
							childWidget.setText(updatedText(childWidget.getText(), false));
							childWidget.setOriginalWidth(newWidgetWidth+LEFT_PIXELS);
						}

						//set position and text (second half)
						else {
							childWidget.setOriginalY((newWidgetHeight * (i-numEachCol)) );
							childWidget.setOriginalX(newWidgetWidth+LEFT_PIXELS);
							childWidget.setXTextAlignment(2);
							childWidget.setText(updatedText(childWidget.getText(), true));
							childWidget.setOriginalWidth(newWidgetWidth-LEFT_PIXELS);
						}

						//hide cancel (if there are an odd number of rows)
						if (numItems%2==1 && childWidget.getText().contains(CANCEL)) {
							childWidget.setHidden(true);
						}

						//height, font, width
						childWidget.setOriginalHeight(newWidgetHeight);
						childWidget.setFontId(config.menuFont().toInt());
						childWidget.setWidthMode(0);
						childWidget.setXPositionMode(0);

						//revalidate everything
						childWidget.revalidate();

						log.debug(childWidget.getText());

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
