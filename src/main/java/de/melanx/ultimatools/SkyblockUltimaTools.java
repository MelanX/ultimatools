package de.melanx.ultimatools;

import de.melanx.ultimatools.item.Registration;
import de.melanx.ultimatools.lib.ListHandlers;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@Mod(SkyblockUltimaTools.MODID)
public class SkyblockUltimaTools {

    public SkyblockUltimaTools() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SERVER_CONFIG);
        ServerConfig.loadConfig(ServerConfig.SERVER_CONFIG, FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve(MODID + "-server.toml"));
        Registration.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onServerStarted);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigChange);
    }

    public static final String MODID = "ultimatools";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final CreativeModeTab TAB = new CreativeModeTab(MODID) {
        
        @Nonnull
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.ultimaGod.get());
        }
    };

    private void onServerStarted(FMLCommonSetupEvent event) {
        ListHandlers.reloadLists();
    }

    private void onConfigChange(ModConfigEvent event) {
        if (event.getConfig().getModId().equals(MODID)) {
            ListHandlers.reloadLists();
        }
    }
}
