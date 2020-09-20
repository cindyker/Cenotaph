package com.MoofIT.Minecraft.Cenotaph.Config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;

import com.MoofIT.Minecraft.Cenotaph.CenotaphMessaging;
import com.MoofIT.Minecraft.Cenotaph.CenotaphSettings;

public class Lang {
	
	public static CommentedConfiguration language;

	// This will read the language entry in the config.yml to attempt to load
	// custom languages
	// if the file is not found it will load the default from resource
	public static void loadLanguage(String filepath, String defaultRes) throws IOException {

		String res = CenotaphSettings.getString(ConfigNodes.LANGUAGE.getRoot(), defaultRes);
		String fullPath = filepath + File.separator + File.separator + res;
		File file = FileMgmt.unpackResourceFile(fullPath, res, defaultRes);

		// read the (language).yml into memory
		language = new CommentedConfiguration(file);
		language.load();
		CommentedConfiguration newLanguage = new CommentedConfiguration(file);
		
		try {
			newLanguage.loadFromString(FileMgmt.convertStreamToString("/" + res));
		} catch (IOException e) {
			CenotaphMessaging.sendInfoConsoleMessage("Custom language file detected, not updating.");
			CenotaphMessaging.sendInfoConsoleMessage("Language: " + res + " v" + Lang.string("version") + " loaded.");
			return;
		} catch (InvalidConfigurationException e) {
			CenotaphMessaging.sendSevereConsoleMessage("Invalid Configuration in language file detected.");
		}
		
		String resVersion = newLanguage.getString("version");
		String langVersion = Lang.string("version");

		if (!langVersion.equalsIgnoreCase(resVersion)) {
			language = newLanguage;
			CenotaphMessaging.sendInfoConsoleMessage("Language file replaced with updated version.");
			FileMgmt.stringToFile(FileMgmt.convertStreamToString("/" + res), file);
		}
		CenotaphMessaging.sendInfoConsoleMessage("Language: " + res + " v" + Lang.string("version") + " loaded.");
	}
	
	/**
	 * Translates give key into its respective language. 
	 * 
	 * @param key The language key.
	 * @return The localized string.
	 */
	public static String string(String key) {
		String data = language.getString(key.toLowerCase());

		if (data == null) {
			CenotaphMessaging.sendSevereConsoleMessage("Key: " + key.toLowerCase() + " is missing from the " + CenotaphSettings.getString(ConfigNodes.LANGUAGE) + " file.");
			return "";
		}
		return data;
	}

	/**
	 * Translates give key into its respective language. 
	 *
	 * @param key The language key.
	 * @param args The arguments to format the localized string.   
	 * @return The localized string.
	 */
	public static String string(String key, Object... args) {
		return String.format(string(key), args);
	}

}
