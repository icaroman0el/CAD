package cad.regulusxv.cad.network;

import cad.regulusxv.cad.CAD;
import cad.regulusxv.cad.psion.PsionPulse;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CadPulsePayload() implements CustomPacketPayload {
    public static final CadPulsePayload INSTANCE = new CadPulsePayload();
    public static final Type<CadPulsePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CAD.MODID, "psion_pulse"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CadPulsePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CadPulsePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            PsionPulse.cast(player);
        }
    }
}
