package carlisle.carlisleshud.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import carlisle.carlisleshud.client.CarlislesHudClient;

public class ConfigScreen {
    public static Screen create(Screen parent) {

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.carlisleshud.config"));

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.carlisleshud.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startIntField(Component.translatable("option.carlisleshud.hudPos"), CarlislesHudClient.config.hudPos)
                .setDefaultValue(1)
                .setSaveConsumer(newValue -> CarlislesHudClient.config.hudPos = newValue)
                .setMin(1)
                .setMax(2)
                .setTooltip(Component.translatable("option.carlisleshud.hudPos.tooltip"))
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.carlisleshud.isWAILAEnabled"), CarlislesHudClient.config.isWAILAEnabled)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> CarlislesHudClient.config.isWAILAEnabled = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.carlisleshud.is12HourFormat"), CarlislesHudClient.config.is12HourFormat)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> CarlislesHudClient.config.is12HourFormat = newValue)
                .build());

        builder.setSavingRunnable(() -> {
            CarlislesHudClient.saveConfig();
        });
        return builder.build();
    }
}
