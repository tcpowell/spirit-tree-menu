package com.spirit;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SpiritTreeMenuPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SpiritTreeMenuPlugin.class);
		RuneLite.main(args);
	}
}