package cad.regulusxv.cad.psion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class PsionData {
    private static final String ROOT_KEY = "cad_psions";
    private static final String UNLOCKED_KEY = "unlocked";
    private static final String AMOUNT_KEY = "amount";
    private static final String CAPACITY_KEY = "capacity";
    private static final String STAGE_KEY = "stage";

    public static final int BASE_CAPACITY = 100;
    public static final int BASE_UNLOCK_AMOUNT = 50;
    public static final int RAW_RECHARGE = 10;
    public static final int INGOT_RECHARGE = 25;
    public static final int BLOCK_RECHARGE = 100;
    public static final int CAPACITY_STEP = 50;
    public static final int MAX_STAGE = 10;

    private PsionData() {
    }

    public static boolean isUnlocked(Player player) {
        return getData(player).getBoolean(UNLOCKED_KEY);
    }

    public static int getAmount(Player player) {
        return getData(player).getInt(AMOUNT_KEY);
    }

    public static int getCapacity(Player player) {
        CompoundTag data = getData(player);
        int capacity = data.getInt(CAPACITY_KEY);
        if (capacity <= 0) {
            capacity = BASE_CAPACITY;
            data.putInt(CAPACITY_KEY, capacity);
        }
        return capacity;
    }

    public static int getStage(Player player) {
        return getData(player).getInt(STAGE_KEY);
    }

    public static void unlock(Player player) {
        CompoundTag data = getData(player);
        data.putBoolean(UNLOCKED_KEY, true);
        data.putInt(CAPACITY_KEY, Math.max(getCapacity(player), BASE_CAPACITY));
        data.putInt(AMOUNT_KEY, Math.max(getAmount(player), BASE_UNLOCK_AMOUNT));
        data.putInt(STAGE_KEY, Math.max(getStage(player), 0));
    }

    public static int add(Player player, int amount) {
        if (!isUnlocked(player)) {
            return getAmount(player);
        }

        CompoundTag data = getData(player);
        int updated = Mth.clamp(getAmount(player) + amount, 0, getCapacity(player));
        data.putInt(AMOUNT_KEY, updated);
        return updated;
    }

    public static boolean consume(Player player, int amount) {
        if (!isUnlocked(player) || getAmount(player) < amount) {
            return false;
        }

        getData(player).putInt(AMOUNT_KEY, getAmount(player) - amount);
        return true;
    }

    public static boolean upgradeCapacity(Player player) {
        if (!isUnlocked(player) || getStage(player) >= MAX_STAGE) {
            return false;
        }

        CompoundTag data = getData(player);
        data.putInt(STAGE_KEY, getStage(player) + 1);
        data.putInt(CAPACITY_KEY, getCapacity(player) + CAPACITY_STEP);
        data.putInt(AMOUNT_KEY, getCapacity(player));
        return true;
    }

    public static void copy(Player original, Player target) {
        if (original.getPersistentData().contains(ROOT_KEY)) {
            target.getPersistentData().put(ROOT_KEY, original.getPersistentData().getCompound(ROOT_KEY).copy());
        }
    }

    public static void sync(Player player, boolean unlocked, int amount, int capacity, int stage) {
        CompoundTag data = getData(player);
        data.putBoolean(UNLOCKED_KEY, unlocked);
        data.putInt(AMOUNT_KEY, amount);
        data.putInt(CAPACITY_KEY, capacity);
        data.putInt(STAGE_KEY, stage);
    }

    public static Component status(Player player) {
        if (!isUnlocked(player)) {
            return Component.literal("Psions locked");
        }

        return Component.literal("Psions " + getAmount(player) + "/" + getCapacity(player) + " | Capacity Lv. " + getStage(player));
    }

    private static CompoundTag getData(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(ROOT_KEY)) {
            persistentData.put(ROOT_KEY, new CompoundTag());
        }

        return persistentData.getCompound(ROOT_KEY);
    }
}
