package amgg.reject_log4j_rce_vulnerable_clients;

import net.minecraftforge.common.config.Config;

@Config(modid = Properties.MODID)
class Configuration {
    private static final String standardMinVersion = "14.23.5.2856";

    @Config.Comment("message displayed when rejecting users without a recent enough version of forge")
    public static String vulnerableClientRejectionMessage = "An EXTREMELY CRITICAL security issue was recently found which impacts most versions of Minecraft. WITHOUT A FIX FOR THIS VULNERABILITY, DO NOT JOIN ANY PUBLIC MULTIPLAYER SERVERS IN ANY VERSION OF MINECRAFT. This video (https://youtu.be/bRubzDAC2Sw) provides a good summary of the situation and how to make sure you're protected in other game versions, other modpacks, etc.\nA fix for the vulnerability was added in forge 1.12.2-"+standardMinVersion+", but your current version of forge is older than that so your client (may) still have the vulnerability*. To connect to this server, you will need to update your forge version.\n\n*you might still be protected through some other method, but I don't know if i can check for those lol. i'll add a way for users to manually bypass this screen eventually but i haven't gotten around to that yet";

    @Config.Comment({
        "minimum forge version considered safe.",
        "(should be 4 integers delimeted with periods, like this - \"14.23.5.2856\")",
        "(the version numbers in the warning message won't be automatically updated, so if you change this make sure to change those too)"
    })
    public static String minForgeVersion = standardMinVersion;
}

