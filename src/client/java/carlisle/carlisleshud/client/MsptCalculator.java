package carlisle.carlisleshud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;

public class MsptCalculator {
    public static double getMspt() {
        Minecraft client = Minecraft.getInstance();
        MinecraftServer server = client.getSingleplayerServer();
        long[] ticks = server.getTickTimesNanos();
        if (ticks != null) {
            return Arrays.stream(ticks).average().orElse(0) * 1.0E-6D;
        }
        return 0;
    }
}
