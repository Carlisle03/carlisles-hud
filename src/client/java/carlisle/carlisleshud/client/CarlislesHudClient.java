package carlisle.carlisleshud.client;

import carlisle.carlisleshud.CarlislesHud;
import carlisle.carlisleshud.client.config.ConfigDefaults;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.Math;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CarlislesHudClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(CarlislesHud.MOD_ID);
	public static Minecraft client = Minecraft.getInstance();
	public static final File configFile = FabricLoader.getInstance().getConfigDir().resolve("carlisles-hud.json").toFile();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static ConfigDefaults config = new ConfigDefaults();
	public static ColorHex colors = new ColorHex();

	@Override
	public void onInitializeClient() {
		getConfig();
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(CarlislesHud.MOD_ID, "before_chat"), CarlislesHudClient::extract);
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker tick) {
		Font font = client.font;
		Player player = client.player;

		if (!client.getDebugOverlay().showDebugScreen()) {
			// hud background
			int tx1 = 10;
			int ty1 = 10;
			if (2 == config.hudPos) {
				tx1 = client.getWindow().getGuiScaledWidth() - 150;
			}

			// text
			Component fpsCounter = Component.literal("FPS: " + client.getFps());
			Component pingCounter = Component.literal("Ping: " + playerPing(player.getUUID()));
			Component coordinates = Component.literal("X: " + Math.round(player.getX()) + " Y: " + Math.round(player.getY()) + " Z: " + Math.round(player.getZ()));
			Component sprintStatus;
			Component minecraftVersion = Component.literal("Minecraft " + FabricLoader.getInstance().
					getModContainer("minecraft")
					.map(container -> container.getMetadata().getVersion().getFriendlyString())
					.orElse("Unknown"));

			if (!client.player.isSprinting()) {
				sprintStatus = Component.literal("Walking");
			} else {
				sprintStatus = Component.literal("Sprinting");
			}
			double avgMspt = Math.round(MsptCalculator.getMspt());
			if (client.getSingleplayerServer() != null) {
				pingCounter = Component.literal("MSPT: " + String.valueOf(avgMspt));
			}

			graphics.text(font, minecraftVersion, tx1 + 10, ty1 + 10, colors.white);
			graphics.text(font, fpsCounter, tx1 + 10, ty1 + 20, colors.white);
			if (avgMspt <= 50) {
				graphics.text(font, pingCounter, tx1 + 10, ty1 + 30, colors.green);
			} else if (avgMspt >= 50) {
				graphics.text(font, pingCounter, tx1 + 10, ty1 + 30, colors.red);
			} else if (client.isMultiplayerServer()) {
				graphics.text(font, pingCounter, tx1 + 10, ty1 + 30, colors.white);
			}
			graphics.text(font, coordinates, tx1 + 10, ty1 + 40, colors.white);
			graphics.text(font, sprintStatus, tx1 + 10, ty1 + 50, colors.white);
			graphics.text(font, getTime(), tx1 + 10, ty1 + 60, colors.white);

			// waila
			HitResult hit = client.hitResult;
			Component block = Component.literal("");
			ItemStack fakeItem = ItemStack.EMPTY;
			if (hit != null && hit.getType() != HitResult.Type.ENTITY) {
				BlockHitResult blockHit = (BlockHitResult) hit;
				BlockPos pos = blockHit.getBlockPos();
				BlockState blockState = client.level.getBlockState(pos);
				block = blockState.getBlock().getName();
				fakeItem = blockState.getBlock().asItem().getDefaultInstance();
			}
			int bx = client.getWindow().getGuiScaledWidth() / 2 - 10;
			int by = 15;
			if (config.isWAILAEnabled) {
				graphics.text(font, block, bx, by, colors.white);
				graphics.fakeItem(fakeItem, bx - 20, by - 5);
			}

			// durability hud
			if (config.isDurabilityHudEnabled) {
				int dtX = 30;
				int dtY = client.getWindow().getGuiScaledHeight() - 110;
				int diX = 10;
				int diY = client.getWindow().getGuiScaledHeight() - 115;
				ItemStack helmet = client.player.getInventory().getItem(39);
				if (!helmet.isEmpty()) {
					graphics.fakeItem(helmet, diX, diY);
					int durability = helmet.getMaxDamage() - helmet.getDamageValue();
					if (durability >= helmet.getMaxDamage() / 2) {
						graphics.text(font, String.valueOf(durability), dtX, dtY, colors.green);
					} else if (durability < helmet.getMaxDamage() / 2) {
						graphics.text(font, String.valueOf(durability), dtX, dtY, colors.red);
					}
				}

				ItemStack chestplate = client.player.getInventory().getItem(38);
				if (!chestplate.isEmpty()) {
					graphics.fakeItem(chestplate, diX, diY + 20);
					int durability = chestplate.getMaxDamage() - chestplate.getDamageValue();
					if (durability >= chestplate.getMaxDamage() / 2) {
						graphics.text(font, String.valueOf(durability), dtX, dtY + 20, colors.green);
					} else if (durability < chestplate.getMaxDamage() / 2) {
						graphics.text(font, String.valueOf(durability), dtX, dtY + 20, colors.red);
					}
				}

				ItemStack leggings = client.player.getInventory().getItem(37);
				if (!leggings.isEmpty()) {
					graphics.fakeItem(leggings, diX, diY + 40);
					int durability = leggings.getMaxDamage() - leggings.getDamageValue();
					if (durability >= leggings.getMaxDamage() / 2) {
						graphics.text(font, String.valueOf(durability), dtX, dtY + 40, colors.green);
					} else if (durability < leggings.getMaxDamage() / 2) {
						graphics.text(font, String.valueOf(durability), dtX, dtY + 40, colors.red);
					}
				}

				ItemStack boots = client.player.getInventory().getItem(36);
				if (!boots.isEmpty()) {
					graphics.fakeItem(boots, diX, diY + 60);
					int durability = boots.getMaxDamage() - boots.getDamageValue();
					if (durability >= boots.getMaxDamage() / 2) {
						graphics.text(font, String.valueOf(durability), dtX, dtY + 60, colors.green);
					} else if (durability < boots.getMaxDamage() / 2) {
						graphics.text(font, String.valueOf(durability), dtX, dtY + 60, colors.red);
					}
				}

				ItemStack mainHand = client.player.getMainHandItem();
				if (!mainHand.isEmpty()) {
					graphics.fakeItem(mainHand, diX, diY + 80);
					if (mainHand.isDamageableItem()) {
						int durability = mainHand.getMaxDamage() - mainHand.getDamageValue();
						if (durability >= mainHand.getMaxDamage() / 2) {
							graphics.text(font, String.valueOf(durability), dtX, dtY + 80, colors.green);
						} else if (durability < mainHand.getMaxDamage() / 2) {
							graphics.text(font, String.valueOf(durability), dtX, dtY + 80, colors.red);
						}
					} else {
						String itemString = mainHand.getCount() + "/" + mainHand.getMaxStackSize();
						graphics.text(font, itemString, dtX, dtY + 80, colors.white);
					}
				}

				ItemStack offHand = client.player.getOffhandItem();
				if (!offHand.isEmpty()) {
					graphics.fakeItem(offHand, diX, diY + 100);
					if (offHand.isDamageableItem()) {
						int durability = offHand.getMaxDamage() - offHand.getDamageValue();
						if (durability >= offHand.getMaxDamage() / 2) {
							graphics.text(font, String.valueOf(durability), dtX, dtY + 100, colors.green);
						} else if (durability < offHand.getMaxDamage() / 2) {
							graphics.text(font, String.valueOf(durability), dtX, dtY + 100, colors.red);
						}
					} else {
						String itemString = offHand.getCount() + "/" + offHand.getMaxStackSize();
						graphics.text(font, itemString, dtX, dtY + 80, colors.white);
					}
				}
			}
		}
	}

	public static int playerPing(UUID playerUUID) {

		PlayerInfo info = client.getConnection().getPlayerInfo(playerUUID);
		if (info != null) {
			return info.getLatency();
		} else {
			return 0;
		}
	}

	public static void getConfig() {
		if (configFile.exists()) {
			try (FileReader reader = new FileReader(configFile)) {
				config = GSON.fromJson(reader, ConfigDefaults.class);
				if (config == null) {
					config = new ConfigDefaults();
				}
			} catch (IOException e) {
				LOGGER.error("Failed to load the config, fallback to defaults.", e);
			}

		} else {
			saveConfig();
		}
	}

	public static void saveConfig() {
		try (FileWriter writer = new FileWriter(configFile)) {
			GSON.toJson(config, writer);
		} catch (IOException e) {
			LOGGER.error("Could not write or save configuration files.", e);
		}
	}

	public static String getTime() {
		DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss a ");
		if (config.is12HourFormat) {
			timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a");
		}
		return ZonedDateTime.now(ZoneId.systemDefault()).format(timeFormat);
	}
}
