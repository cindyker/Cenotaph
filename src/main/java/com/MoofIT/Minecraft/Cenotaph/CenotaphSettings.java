package com.MoofIT.Minecraft.Cenotaph;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.MoofIT.Minecraft.Cenotaph.Config.CommentedConfiguration;
import com.MoofIT.Minecraft.Cenotaph.Config.ConfigNodes;

public class CenotaphSettings {

	private static CommentedConfiguration config, newConfig;
	
	public static void loadConfig(String filepath, String version) throws IOException {
		if (checkOrCreateFile(filepath)) {
			File file = new File(filepath);

			// read the config.yml into memory
			config = new CommentedConfiguration(file);
			if (!config.load()) {
				System.out.print("Failed to load Config!");
			}

			setDefaults(version, file);

			config.save();
		}
	}
	
	/**
	 * Builds a new config reading old config data.
	 */
	private static void setDefaults(String version, File file) {

		newConfig = new CommentedConfiguration(file);
		newConfig.load();

		for (ConfigNodes root : ConfigNodes.values()) {
			if (root.getComments().length > 0)
				addComment(root.getRoot(), root.getComments());

				
			if (root.getRoot() == ConfigNodes.VERSION.getRoot()) {
				setNewProperty(root.getRoot(), version);
			} else if (root.getRoot() == ConfigNodes.LAST_RUN_VERSION.getRoot()) {
				setNewProperty(root.getRoot(), getLastRunVersion(version));
			} else
				setNewProperty(root.getRoot(), (config.get(root.getRoot().toLowerCase()) != null) ? config.get(root.getRoot().toLowerCase()) : root.getDefault());

		}

		config = newConfig;
		newConfig = null;
	}
	
	public static void addComment(String root, String... comments) {

		newConfig.addComment(root.toLowerCase(), comments);
	}
	
	private static void setNewProperty(String root, Object value) {

		if (value == null) {
			// System.out.print("value is null for " + root.toLowerCase());
			value = "";
		}
		newConfig.set(root.toLowerCase(), value.toString());
	}
	
	public static String getLastRunVersion(String currentVersion) {

		return getString(ConfigNodes.LAST_RUN_VERSION.getRoot(), currentVersion);
	}
	
	public static boolean getBoolean(ConfigNodes node) {

		return Boolean.parseBoolean(config.getString(node.getRoot().toLowerCase(), node.getDefault()));
	}

	public static double getDouble(ConfigNodes node) {

		try {
			return Double.parseDouble(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
		} catch (NumberFormatException e) {
			sendError(node.getRoot().toLowerCase() + " from config.yml");
			return 0.0;
		}
	}

	public static int getInt(ConfigNodes node) {

		try {
			return Integer.parseInt(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
		} catch (NumberFormatException e) {
			sendError(node.getRoot().toLowerCase() + " from config.yml");
			return 0;
		}
	}

	public static String getString(ConfigNodes node) {

		return config.getString(node.getRoot().toLowerCase(), node.getDefault());
	}

	public static String getString(String root, String def) {

		String data = config.getString(root.toLowerCase(), def);
		if (data == null) {
			sendError(root.toLowerCase() + " from config.yml");
			return "";
		}
		return data;
	}
	
	private static void sendError(String msg) {

		System.out.println("[Cenotaph] Error could not read " + msg);
	}

	
	/**
	 * Checks a filePath to see if it exists, if it doesn't it will attempt
	 * to create the file at the designated path.
	 *
	 * @param filePath {@link String} containing a path to a file.
	 * @return True if the folder exists or if it was successfully created.
	 */
	public static boolean checkOrCreateFile(String filePath) {
		File file = new File(filePath);
		if (!checkOrCreateFolder(file.getParentFile().getPath())) {
			return false;
		}
		try {
			return file.exists() || file.createNewFile();
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Checks a folderPath to see if it exists, if it doesn't it will attempt
	 * to create the folder at the designated path.
	 *
	 * @param folderPath {@link String} containing a path to a folder.
	 * @return True if the folder exists or if it was successfully created.
	 */
	public static boolean checkOrCreateFolder(String folderPath) {
		File file = new File(folderPath);
		return file.exists() || file.mkdirs() || file.isDirectory();
	}
	
	public static boolean cenotaphSign() {		
		return getBoolean(ConfigNodes.CORE_CENOTAPH_SIGN);		
	}
	
	public static boolean saveCenotaphList() {
		return getBoolean(ConfigNodes.CORE_SAVE_CENOTAPH_LIST);
	}

	public static boolean voidCheck() {
		return getBoolean(ConfigNodes.CORE_VOID_CHECK);
	}
	
	public static boolean oneBlockUpCheck() {
		return getBoolean(ConfigNodes.CORE_ONE_BLOCK_UP_CHECK);
	}
	
	public static boolean explosionProtection() {
		return getBoolean(ConfigNodes.CORE_EXPLOSION_PROTECTION);		
	}
	
	public static String signLine1() {
		return getString(ConfigNodes.CORE_SIGN_LINE1);
	}
	
	public static String signLine2() {
		return getString(ConfigNodes.CORE_SIGN_LINE2);
	}
	
	public static String signLine3() {
		return getString(ConfigNodes.CORE_SIGN_LINE3);
	}
	
	public static String signLine4() {
		return getString(ConfigNodes.CORE_SIGN_LINE4);
	}

	public static String dateFormat() {
		return getString(ConfigNodes.CORE_SIGN_DATE_FORMAT);		
	}
	
	public static String timeFormat() {
		return getString(ConfigNodes.CORE_SIGN_TIME_FORMAT);
	}
	
	public static List<String> disabledWorlds() {
		List<String> worlds = Arrays.asList(getString(ConfigNodes.CORE_DISABLE_IN_WORLDS).split("\\s*,\\s*"));
		return worlds;
	}
	
	public static boolean dynmapEnable() { 
		return getBoolean(ConfigNodes.CORE_DYNMAP_ENABLE);		
	}
	
	public static boolean worldguardEnable() { 
		return getBoolean(ConfigNodes.CORE_WORLDGUARD_ENABLE);		
	}
	
	public static boolean townyEnable() {
		return getBoolean(ConfigNodes.CORE_TOWNY_ENABLE);
	}
	
	public static boolean hologramsEnable() {
		return getBoolean(ConfigNodes.CORE_HOLOGRAMS_ENABLE);
	}
	
	public static double cenotaphCost() { 
		return getDouble(ConfigNodes.CORE_CENOTAPH_COST);
	}
	
	public static boolean destroyQuickloot() { 
		return getBoolean(ConfigNodes.REMOVAL_DESTROY_QUICKLOOT);
	}
	
	public static boolean cenotaphRemove() { 
		return getBoolean(ConfigNodes.REMOVAL_CENOTAPH_REMOVE);
	}
	
	public static int cenotaphRemoveTime() { 
		return getInt(ConfigNodes.REMOVAL_REMOVE_TIME);
	}
	
	public static boolean levelBasedRemoval() { 
		return getBoolean(ConfigNodes.REMOVAL_LEVEL_BASED_REMOVAL);		
	}
	
	public static int levelBasedTime() {
		return getInt(ConfigNodes.REMOVAL_LEVEL_BASED_TIME);		
	}
	
	public static boolean removeWhenEmpty() { 
		return getBoolean(ConfigNodes.REMOVAL_REMOVE_WHEN_EMPTY);
	}
	
	public static boolean keepUntilEmpty() { 
		return getBoolean(ConfigNodes.REMOVAL_KEEP_UNTIL_EMPTY);
	}
	
	public static boolean securityEnable() {
		return getBoolean(ConfigNodes.SECURITY_ENABLE);
	}
	
	public static boolean securityRemove() {
		return getBoolean(ConfigNodes.SECURITY_REMOVE);	
	}
	
	public static int securityTimeOut() {
		return getInt(ConfigNodes.SECURITY_TIMEOUT);
	}
	
	public static boolean enableAscii() {
		return getBoolean(ConfigNodes.STARTUP_ENABLE_ASCII);
	}
}
