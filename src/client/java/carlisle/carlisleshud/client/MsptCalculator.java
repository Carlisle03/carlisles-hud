package carlisle.carlisleshud.client;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;

public class MsptCalculator {
    public static double getMspt() {
        Minecraft client = Minecraft.getInstance();
        MinecraftServer server = client.getSingleplayerServer();
        long[] ticks = server.getTickTimesNanos();
        double avgMspt = Arrays.stream(ticks).average().orElse(0) * 1.0E-6D;
        return avgMspt;
    }
}
