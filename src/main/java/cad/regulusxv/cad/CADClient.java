package cad.regulusxv.cad;

import cad.regulusxv.cad.network.CadPulsePayload;
import cad.regulusxv.cad.client.renderer.CadCalibrationTableRenderer;
import cad.regulusxv.cad.psion.PsionData;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CAD.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = CAD.MODID, value = Dist.CLIENT)
public class CADClient {
    private static final ResourceLocation PSION_HUD = ResourceLocation.fromNamespaceAndPath(CAD.MODID, "psion_hud");
    private static final KeyMapping PSION_PULSE_KEY = new KeyMapping(
            "key.cad.psion_pulse",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.cad");

    public CADClient(IEventBus modEventBus, ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(CADClient::registerGuiLayers);
        modEventBus.addListener(CADClient::registerRenderers);
        modEventBus.addListener(CADClient::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(CADClient::onClientTick);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        CAD.LOGGER.info("HELLO FROM CLIENT SETUP");
        CAD.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, PSION_HUD, CADClient::renderPsionHud);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CAD.CAD_CALIBRATION_TABLE_BLOCK_ENTITY.get(), CadCalibrationTableRenderer::new);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(PSION_PULSE_KEY);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return;
        }

        while (PSION_PULSE_KEY.consumeClick()) {
            PacketDistributor.sendToServer(CadPulsePayload.INSTANCE);
        }
    }

    private static void renderPsionHud(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null || minecraft.player.isSpectator() || !PsionData.isUnlocked(minecraft.player)) {
            return;
        }

        int capacity = PsionData.getCapacity(minecraft.player);
        int amount = PsionData.getAmount(minecraft.player);
        int barWidth = 58;
        int barHeight = 4;
        int x = 8;
        int y = 8;
        int fillWidth = capacity <= 0 ? 0 : Math.round(barWidth * (amount / (float) capacity));
        String label = amount + "/" + capacity;

        graphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xBB160A24);
        graphics.fill(x, y, x + barWidth, y + barHeight, 0xCC2A123A);
        graphics.fill(x, y, x + fillWidth, y + barHeight, 0xFFE052FF);
        graphics.fill(x, y, x + fillWidth, y + 1, 0xFFFFB8FF);
        graphics.drawString(minecraft.font, label, x, y + 7, 0xFFE9B5FF, true);
    }
}
