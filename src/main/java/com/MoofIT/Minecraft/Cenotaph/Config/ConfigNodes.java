package com.MoofIT.Minecraft.Cenotaph.Config;


/**
 * Config consists of an Enum of values which are smartly added to existing configs
 * when there is an update that needs to be done. Default values are only over-
 * written when value is completely removed from an existing config.
 * 
 * To add config options follow these instructions.
 *  - Must have 2 or more values.
 *  - First value is where the option will be placed: section1.subsection2.subsubsection3
 *  - Second value is the default.
 *  - Third and onwards are lines of comments added to the block. 
 *  - To put a gap between config options make the first line of comments (the third value) 
 *    an "".
 *    
 *  Settings of the config are pulled from CenotaphSettings. 
 *  After adding a new config option, remember to add it to the CenotaphSettings.
 * 
 * @author LlmDl, DumpTruckMan, ArticDive
 *
 */
public enum ConfigNodes {
	VERSION_HEADER("version", "", ""),
	VERSION(
			"version.version",
			"",
			"# This is the current version of Cenotaph.  Please do not edit."),
	LAST_RUN_VERSION(
			"version.last_run_version",
			""),	
	CORE_HEADER("core", "core",""),
	CORE_CENOTAPH_SIGN(
			"core.cenotaph_sign",
			"true",
			"",
			"# Place a sign on the top of the Cenotaph if true. Displays info about the deceased."
			),
	CORE_SAVE_CENOTAPH_LIST(
			"core.save_cenotaph_list",
			"true",
			"",
			"# Save Cenotaph listing between server stop and starts if true."),
	CORE_VOID_CHECK(
			"core.void_check",
			"true",
			"",
			"# Cenotaph normally checks to make sure it isn't trying to create a chest in the void.",
			"# If you handle or modify the void with another plugin, you can disable that check here.",
			"# This option should be true for most servers."),
	CORE_ONE_BLOCK_UP_CHECK(
			"core.one_block_up_check",
			"true",
			"",
			"# Cenotaph will normally only check the world around the player for chst placement.",
			"# But if they are standing on a carpeted floor, it will fail to find a good place for",
			"# the chest. This check will allow Cenotaph to look one block up for a safe place."),
	CORE_EXPLOSION_PROTECTION(
			"core.explosion_protection",
			"true",
			"",
			"# Keeps cenotaphs protected against all explosions, even after they are unlocked."),			
	CORE_SIGN(
			"core.sign",
			"",
			"",
			"# Each line may be one of any custom text OR:",
			"# {name} for player name",
			"# {date} for day of death",
			"# {time} for time of death (server time)",
			"# {reason} for cause of death",
			"# REMEMBER: LINES ARE LIMITED TO 15 CHARACTERS!"),
	CORE_SIGN_LINE1(
			"core.Sign.Line1",
			"{name}"),
	CORE_SIGN_LINE2(
			"core.Sign.Line2",
			"RIP"),	
	CORE_SIGN_LINE3(
			"core.Sign.Line3",
			"{date}"),	
	CORE_SIGN_LINE4(
			"core.Sign.Line4",
			"{time}"),
	CORE_SIGN_DATE_FORMAT(
			"core.Sign.date_format",
			"MM/dd/yyyy",
			"",
			"#For formatting, see http://download.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html"),
	CORE_SIGN_TIME_FORMAT(
			"core.Sign.time_format",
			"hh:mm a"),
	CORE_DISABLE_IN_WORLDS(
			"core.disable_in_worlds",
			"someworldyoudontwantcenotaphworkingin,someotherworld",
			"",
			"# List of disabled worlds."),
	CORE_DYNMAP_ENABLE(
			"core.dynmap_enable",
			"false",
			"",
			"# Enables dynmap integration, displaying cenotaphs on the map."),
	CORE_WORLDGUARD_ENABLE(
			"core.worldguard_enable",
			"false",
			"",
			"# Enables WorldGuard integration, preventing cenotaphs from being created in regions the player cannot build."),
	CORE_CENOTAPH_COST(
			"core.cenotaph_cost",
			"0.0",
			"",
			"# If set to more than 0.0, the player will need to pay the amount before a cenotaph is made.", 
			"# Requires Vault and a functioning economy plugin to be installed."),
	REMOVAL_HEADER(
			"removal", "removal", ""),
	REMOVAL_DESTROY_QUICKLOOT(
			"removal.destroy_quick_loot",
			"true",
			"",
			"# Destroy cenotaph on player quickloot if true."),
	REMOVAL_CENOTAPH_REMOVE(
			"removal.cenotaph_remove",
			"true",
			"",
			"# Remove cenotaph after removeTime seconds if true."),
	REMOVAL_REMOVE_TIME(
			"removal.remove_time",
			"3600",
			"",
			"# 3600 seconds = 1 hour."),
	REMOVAL_LEVEL_BASED_REMOVAL(
			"removal.level_based_removal",
			"false",
			"",
			"# Set cenotaph removal time based on player level, with the above removeTime setting the maximum cap."),
	REMOVAL_LEVEL_BASED_TIME(
			"removal.level_based_time",
			"60",
			"",
			"# Default of 60, means 60 seconds per level at time of death."),
	REMOVAL_REMOVE_WHEN_EMPTY(
			"removal.remove_when_empty",
			"false",
			"",
			"# Immediately remove cenotaph once it is empty, overriding all other timeout options."),
	REMOVAL_KEEP_UNTIL_EMPTY(
			"removal.keep_until_empty",
			"false",
			"",
			"# Never remove a cenotaph unless it is empty."),
	SECURITY_HEADER("security","security",""),
	SECURITY_ENABLE(
			"security.security_enable",
			"true",
			"",
			"# If true, Cenotaph will self-protect the tombblock from looters and explosions."),
	SECURITY_REMOVE(
			"security.security_remove",
			"true",
			"",
			"# If true the security protection set above will be removed after the below timeout."),
	SECURITY_TIMEOUT(
			"security.security_time_out",
			"900",
			"",
			"# Number of seconds before the security is removed on the cenotaph, if securityRemove is true. Default of 15 minutes."),
	STARTUP_HEADER("startup","startup",""),
	STARTUP_ENABLE_ASCII(
			"startup.enable_ascii",
			"true",
			"",
			"# For people that want a short startup and that do not appreciate a good ascii tombstone.",
			"# Ascii graphic by jgs.");

	private final String Root;
	private final String Default;
	private String[] comments;

	ConfigNodes(String root, String def, String... comments) {

		this.Root = root;
		this.Default = def;
		this.comments = comments;
	}

	/**
	 * Retrieves the root for a config option
	 *
	 * @return The root for a config option
	 */
	public String getRoot() {

		return Root;
	}

	/**
	 * Retrieves the default value for a config path
	 *
	 * @return The default value for a config path
	 */
	public String getDefault() {

		return Default;
	}

	/**
	 * Retrieves the comment for a config path
	 *
	 * @return The comments for a config path
	 */
	public String[] getComments() {

		if (comments != null) {
			return comments;
		}

		String[] comments = new String[1];
		comments[0] = "";
		return comments;
	}
}
