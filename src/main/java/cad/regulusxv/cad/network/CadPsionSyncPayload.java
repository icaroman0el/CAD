package cad.regulusxv.cad.network;

import cad.regulusxv.cad.CAD;
import cad.regulusxv.cad.psion.PsionData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CadPsionSyncPayload(boolean unlocked, int amount, int capacity, int stage) implements CustomPacketPayload {
    public static final Type<CadPsionSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CAD.MODID, "psion_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CadPsionSyncPayload> STREAM_CODEC = StreamCodec.of(
            CadPsionSyncPayload::encode,
            CadPsionSyncPayload::decode);

    public static CadPsionSyncPayload from(Player player) {
        return new CadPsionSyncPayload(
                PsionData.isUnlocked(player),
                PsionData.getAmount(player),
                PsionData.getCapacity(player),
                PsionData.getStage(player));
    }

    private static void encode(RegistryFriendlyByteBuf buffer, CadPsionSyncPayload payload) {
        buffer.writeBoolean(payload.unlocked());
        buffer.writeVarInt(payload.amount());
        buffer.writeVarInt(payload.capacity());
        buffer.writeVarInt(payload.stage());
    }

    private static CadPsionSyncPayload decode(RegistryFriendlyByteBuf buffer) {
        return new CadPsionSyncPayload(
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CadPsionSyncPayload payload, IPayloadContext context) {
        PsionData.sync(context.player(), payload.unlocked(), payload.amount(), payload.capacity(), payload.stage());
    }
}
