package amgg.reject_log4j_rce_vulnerable_clients;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.text.TextComponentString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

@Mod(
    modid = Properties.MODID,
    name = Properties.NAME,
    version = Properties.VERSION,
    acceptableRemoteVersions = "*"
)
@Mod.EventBusSubscriber
public class ModMain {
    // lol
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
    }

    enum Log4jVulnStatus { UNPATCHED, PATCHED, UNKNOWN }
    static final Pattern forgeVersionRegex = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)$");

    static Integer tryParseInt(String s) {
        if(s == null) return null;
        try { return Integer.parseInt(s); }
        catch(NumberFormatException e) { return null; }
    }

    static class ForgeVersion implements Comparable<ForgeVersion> {
        final int a, b, c, d;
        ForgeVersion(int a, int b, int c, int d) { this.a = a; this.b = b; this.c = c; this.d = d; }
        public static ForgeVersion parseVersionString(String versionStr) {
            Matcher matcher = forgeVersionRegex.matcher(versionStr);
            if(matcher.find()) {
                // if(matcher.groupCount() != 4) return null;
                Integer a = tryParseInt(matcher.group(1)), b = tryParseInt(matcher.group(2)), c = tryParseInt(matcher.group(3)), d = tryParseInt(matcher.group(4));
                if(a == null || b == null || c == null || d == null) return null;
                return new ForgeVersion(a, b, c, d);
            }
            else return null;
        }
        @Override
        public int compareTo(ForgeVersion other) {
            int bigDif = Integer.compare(this.a, other.a);
            int medDif = Integer.compare(this.b, other.b);
            int smlDif = Integer.compare(this.c, other.c);
            int tnyDif = Integer.compare(this.d, other.d);
            if(bigDif != 0) return bigDif;
            else if(medDif != 0) return medDif;
            else if(smlDif != 0) return smlDif;
            else return tnyDif;
        }
        public Log4jVulnStatus isVulnerable() {
            ForgeVersion firstLog4jFixedVersion = parseVersionString(Configuration.minForgeVersion);
            if(firstLog4jFixedVersion == null) {
                logger.error("couldn't parse minimum version from config, defaulting to 14.23.5.2856.");
                firstLog4jFixedVersion = new ForgeVersion(14, 23, 5, 2856);
            }
            if(compareTo(firstLog4jFixedVersion) < 0) return Log4jVulnStatus.UNPATCHED;
            else return Log4jVulnStatus.PATCHED;
        }
    }

    static Log4jVulnStatus checkPlayerModList(Map<String, String> modList) {
        if(modList == null) return Log4jVulnStatus.UNKNOWN;
        String forgeVersionStr = modList.get("forge");
        if(forgeVersionStr == null) return Log4jVulnStatus.UNKNOWN;
        ForgeVersion playerVersion = ForgeVersion.parseVersionString(forgeVersionStr);
        if(playerVersion == null) return Log4jVulnStatus.UNKNOWN;
        return playerVersion.isVulnerable();
    }

    @SubscribeEvent
    public static void connectEvent(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        if(!event.getHandlerType().isAssignableFrom(NetHandlerPlayServer.class)) return;
        NetHandlerPlayServer netHandler = (NetHandlerPlayServer)event.getHandler();
        NetworkDispatcher networkDispatcher = NetworkDispatcher.get(netHandler.getNetworkManager());
        Log4jVulnStatus playerVulnStatus = checkPlayerModList(networkDispatcher.getModList());
        if(playerVulnStatus == Log4jVulnStatus.UNPATCHED) {
            logger.info("player appears to not have vuln fixed, disconnecting them");
            netHandler.disconnect(new TextComponentString(Configuration.vulnerableClientRejectionMessage));
        }
    }
}
