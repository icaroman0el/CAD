package cad.regulusxv.cad;

import cad.regulusxv.cad.block.CadCalibrationTableBlock;
import cad.regulusxv.cad.block.entity.CadCalibrationTableBlockEntity;
import cad.regulusxv.cad.network.CadPulsePayload;
import cad.regulusxv.cad.psion.PsionData;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CAD.MODID)
public class CAD {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "cad";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "cad" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "cad" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "cad" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> PSIONITE_ORE = BLOCKS.registerSimpleBlock("psionite_ore",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> PSIONITE_ORE_ITEM = ITEMS.registerSimpleBlockItem("psionite_ore", PSIONITE_ORE);

    public static final DeferredBlock<Block> PSIONITE_BLOCK = BLOCKS.registerSimpleBlock("psionite_block",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(5.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> PSIONITE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("psionite_block", PSIONITE_BLOCK);

    public static final DeferredItem<Item> RAW_PSIONITE = ITEMS.registerSimpleItem("raw_psionite");
    public static final DeferredItem<Item> PSIONITE_INGOT = ITEMS.registerSimpleItem("psionite_ingot");
    public static final DeferredItem<Item> CAD_BASIC = ITEMS.registerSimpleItem("cad_basic", new Item.Properties().stacksTo(1));

    public static final DeferredBlock<CadCalibrationTableBlock> CAD_CALIBRATION_TABLE = BLOCKS.register("cad_calibration_table",
            () -> new CadCalibrationTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0f, 8.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .sound(SoundType.METAL)));
    public static final DeferredItem<BlockItem> CAD_CALIBRATION_TABLE_ITEM = ITEMS.registerSimpleBlockItem("cad_calibration_table", CAD_CALIBRATION_TABLE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CadCalibrationTableBlockEntity>> CAD_CALIBRATION_TABLE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("cad_calibration_table",
                    () -> BlockEntityType.Builder.of(CadCalibrationTableBlockEntity::new, CAD_CALIBRATION_TABLE.get()).build(null));

    public static final SimpleTier PSIONITE_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 8.0f, 3.0f, 10,
            () -> net.minecraft.world.item.crafting.Ingredient.of(PSIONITE_INGOT.get()));

    public static final DeferredItem<SwordItem> PSIONITE_SWORD = ITEMS.register("psionite_sword",
            () -> new SwordItem(PSIONITE_TIER, new Item.Properties().attributes(SwordItem.createAttributes(PSIONITE_TIER, 3, -2.4f))));
    public static final DeferredItem<ShovelItem> PSIONITE_SHOVEL = ITEMS.register("psionite_shovel",
            () -> new ShovelItem(PSIONITE_TIER, new Item.Properties().attributes(ShovelItem.createAttributes(PSIONITE_TIER, 1.5f, -3.0f))));
    public static final DeferredItem<PickaxeItem> PSIONITE_PICKAXE = ITEMS.register("psionite_pickaxe",
            () -> new PickaxeItem(PSIONITE_TIER, new Item.Properties().attributes(PickaxeItem.createAttributes(PSIONITE_TIER, 1.0f, -2.8f))));
    public static final DeferredItem<AxeItem> PSIONITE_AXE = ITEMS.register("psionite_axe",
            () -> new AxeItem(PSIONITE_TIER, new Item.Properties().attributes(AxeItem.createAttributes(PSIONITE_TIER, 5.0f, -3.0f))));
    public static final DeferredItem<HoeItem> PSIONITE_HOE = ITEMS.register("psionite_hoe",
            () -> new HoeItem(PSIONITE_TIER, new Item.Properties().attributes(HoeItem.createAttributes(PSIONITE_TIER, -3.0f, 0.0f))));

    // Creates a creative tab with the id "cad:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cad")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> PSIONITE_INGOT.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(RAW_PSIONITE.get());
                output.accept(PSIONITE_INGOT.get());
                output.accept(CAD_BASIC.get());
                output.accept(PSIONITE_ORE_ITEM.get());
                output.accept(PSIONITE_BLOCK_ITEM.get());
                output.accept(CAD_CALIBRATION_TABLE_ITEM.get());
                output.accept(PSIONITE_SWORD.get());
                output.accept(PSIONITE_SHOVEL.get());
                output.accept(PSIONITE_PICKAXE.get());
                output.accept(PSIONITE_AXE.get());
                output.accept(PSIONITE_HOE.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CAD(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloadHandlers);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (CAD) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar(MODID)
                .playToServer(CadPulsePayload.TYPE, CadPulsePayload.STREAM_CODEC, CadPulsePayload::handle);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(RAW_PSIONITE);
            event.accept(PSIONITE_INGOT);
            event.accept(CAD_BASIC);
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(PSIONITE_ORE_ITEM);
        }

        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(PSIONITE_BLOCK_ITEM);
            event.accept(CAD_CALIBRATION_TABLE_ITEM);
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PSIONITE_SHOVEL);
            event.accept(PSIONITE_PICKAXE);
            event.accept(PSIONITE_AXE);
            event.accept(PSIONITE_HOE);
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(PSIONITE_SWORD);
            event.accept(PSIONITE_AXE);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onPsioniteItemUse(PlayerInteractEvent.RightClickItem event) {
        if (tryUsePsionite(event.getEntity(), event.getItemStack(), true)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPsioniteBlockUse(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown()) {
            return;
        }

        if (tryUsePsionite(event.getEntity(), event.getItemStack(), true)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        PsionData.copy(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!PsionData.isUnlocked(event.getEntity())) {
            return;
        }

        if (event.getEntity().tickCount % 80 == 0) {
            PsionData.add(event.getEntity(), 1);
        }
    }

    private boolean tryUsePsionite(net.minecraft.world.entity.player.Player player, ItemStack stack, boolean allowBlockUpgrade) {
        if (stack.isEmpty()) {
            return false;
        }

        boolean changed = false;
        boolean consumesItem = false;

        if (stack.is(RAW_PSIONITE.get())) {
            if (!PsionData.isUnlocked(player)) {
                PsionData.unlock(player);
                PsionData.add(player, PsionData.RAW_RECHARGE - PsionData.BASE_UNLOCK_AMOUNT);
            } else {
                PsionData.add(player, PsionData.RAW_RECHARGE);
            }
            changed = true;
            consumesItem = true;
        } else if (stack.is(PSIONITE_INGOT.get())) {
            if (!PsionData.isUnlocked(player)) {
                PsionData.unlock(player);
            } else {
                PsionData.add(player, PsionData.INGOT_RECHARGE);
            }
            changed = true;
            consumesItem = true;
        } else if (allowBlockUpgrade && stack.is(PSIONITE_BLOCK_ITEM.get())) {
            if (!PsionData.isUnlocked(player)) {
                PsionData.unlock(player);
                PsionData.add(player, PsionData.BLOCK_RECHARGE);
            } else if (!PsionData.upgradeCapacity(player)) {
                PsionData.add(player, PsionData.BLOCK_RECHARGE);
            }
            changed = true;
            consumesItem = true;
        }

        if (!changed) {
            return false;
        }

        if (!player.level().isClientSide()) {
            if (consumesItem && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.displayClientMessage(PsionData.status(player), true);
        }

        return true;
    }
}
