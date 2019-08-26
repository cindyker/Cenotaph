package com.MoofIT.Minecraft.Cenotaph.Config;


/**
 * Config consists of an Enum of values which are smartly added to existing configs
 * when there is an update that needs to be done. Default values are only over-
 * written when value is completely removed from an existing config.
 * 
 * To add config options follow these instructions.
 *  - Must have 3 or more values.
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
			"",
			"# This is for showing the changelog on updates.  Please do not edit."),	
	CORE_HEADER("core", "core",""),
	CORE_CENOTAPH_SIGN(
			"core.cenotaphSign",
			"true",
			"",
			"# Place a sign on the top of the Cenotaph if true. Displays info about the deceased."
			),
	CORE_NO_DESTROY(
			"core.noDestroy",
			"true",
			"",
			"# Prevet non-Op players from destroying cenotaphs if true (destroyQuickLoot overrides this setting."),
	CORE_SAVE_CENOTAPH_LIST(
			"core.SaveCenotaphList",
			"true",
			"",
			"# Save Cenotaph listing between server stop and starts if true."),
	CORE_NO_INTERFERE(
			"core.noInterfere",
			"true",
			"",
			"# Stop Cenotaph creation next to existing chests if true."),
	CORE_VOID_CHECK(
			"core.voidCheck",
			"true",
			"",
			"# Cenotaph normally checks to make sure it isn't trying to create a chest in the void.",
			"# If you handle or modify the void with another plugin, you can disable that check here.",
			"# This option should be true for most servers."),
	CORE_ONE_BLOCK_UP_CHECK(
			"core.oneBlockUpCheck",
			"true",
			"",
			"# Cenotaph will normally only check the world around the player for chst placement.",
			"# But if they are standing on a carpeted floor, it will fail to find a good place for",
			"# the chest. This check will allow Cenotaph to look one block up for a safe place."),
	CORE_CREEPER_PROTECTION(
			"core.creeperProtection",
			"false",
			"",
			"# If you are not locking your chests with Lockette or LWC but still want them to be",
			"# protected against Creeper explosions, or you want your chests to be protected even",
			"# after they are unlocked, enable this."),
	CORE_TNT_PROTECTION(
			"core.tntProtection",
			"true",
			"",
			"# If you are not locking your chests with Lockette or LWC but still want them to be",
			"# protected against TnT explosions, or you want your chests to be protected even",
			"# after they are unlocked, enable this."),
	CORE_SIGN(
			"core.Sign",
			"",
			"",
			"# Each line may be one of any custom text OR:",
			"# {name} for player name",
			"# {date} for day of death",
			"# {time} for time of death (server time)",
			"# {reason} for cause of death",
			"# REMEMBER: LINES ARE LIMITED TO 15 CHARACTERS, AND DON'T FORGET THE QUOTES!"),
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
			"core.Sign.dateFormat",
			"MM/dd/yyyy",
			"",
			"#For formatting, see http://download.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html"),
	CORE_SIGN_TIME_FORMAT(
			"core.Sign.timeFormat",
			"hh:mm a"),
	CORE_DISABLE_IN_WORLDS(
			"core.disableInWorlds",
			"someworldyoudontwantcenotaphworkingin,someotherworld",
			"",
			"# List of disabled worlds."),
	CORE_DYNMAP_ENABLE(
			"core.dynmapEnable",
			"false",
			"",
			"# Enables dynmap integration, displaying cenotaphs on the map."),
	CORE_WORLDGUARD_ENABLE(
			"core.worldguardEnable",
			"false",
			"",
			"# Enables WorldGuard integration, preventing cenotaphs from being created in regions the player cannot build."),
	CORE_CENOTAPH_COST(
			"core.cenotaphCost",
			"0.0",
			"",
			"# If set to more than 0.0, the player will need to pay the amount before a cenotaph is made.", 
			"# Requires Vault and a functioning economy plugin to be installed."),
	REMOVAL_HEADER(
			"removal", "removal", ""),
	REMOVAL_DESTROY_QUICKLOOT(
			"removal.destroyQuickLoot",
			"true",
			"",
			"# Destroy cenotaph on player quickloot if true."),
	REMOVAL_CENOTAPH_REMOVE(
			"removal.cenotaphRemove",
			"true",
			"",
			"# Remove cenotaph after removeTime seconds if true."),
	REMOVAL_REMOVE_TIME(
			"removal.removeTime",
			"3600",
			""),
	REMOVAL_LEVEL_BASED_REMOVAL(
			"removal.levelBasedRemoval",
			"false",
			"",
			"# Set cenotaph removal time based on player level, with the above removeTime setting the maximum cap"),
	REMOVAL_LEVEL_BASED_TIME(
			"removal.levelBasedTime",
			"60",
			""),
	REMOVAL_REMOVE_WHEN_EMPTY(
			"removal.removeWhenEmpty",
			"false",
			"",
			"# Immediately remove cenotaph once it is empty, overriding all other timeout options"),
	REMOVAL_KEEP_UNTIL_EMPTY(
			"removal.keepUntilEmpty",
			"false",
			"",
			"# Never remove a cenotaph unless it is empty"),
	SECURITY_HEADER("security","security",""),
	SECURITY_LOCKETTE_ENABLE(
			"security.locketteEnable",
			"true",
			"",
			"# If true, Cenotaph will add a sign on the chest which matches the format of Lockette, Deadbolt."),
	SECURITY_LWC("security.lwc","",""),	
	SECURITY_LWC_ENABLE(
			"security.lwc.enable",
			"false",
			"",
			"# If true, Cenotaph will attempt to lock the chest with LWC for the deceased player."),
	SECURITY_LWC_PUBLIC(
			"security.lwc.public",
			"false",
			"",
			"# Set LWC protection to Public instead of removing it after the timeout."),
	SECURITY_REMOVE(
			"security.securityRemove",
			"true",
			"",
			"# If true the security protection set above will be removed after the below timeOut."),
	SECURITY_TIMEOUT(
			"security.securityTimeOut",
			"900",
			"",
			"# Number of seconds before the security is removed on the cenotaph. Default of 15 minutes.");

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
