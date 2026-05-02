package cad.regulusxv.cad.network;

import cad.regulusxv.cad.CAD;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CadBeamPayload(Vec3 start, Vec3 end, int durationTicks) implements CustomPacketPayload {
    public static final Type<CadBeamPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CAD.MODID, "cad_beam"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CadBeamPayload> STREAM_CODEC = StreamCodec.of(CadBeamPayload::encode, CadBeamPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, CadBeamPayload payload) {
        buffer.writeVec3(payload.start());
        buffer.writeVec3(payload.end());
        buffer.writeVarInt(payload.durationTicks());
    }

    private static CadBeamPayload decode(RegistryFriendlyByteBuf buffer) {
        return new CadBeamPayload(buffer.readVec3(), buffer.readVec3(), buffer.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CadBeamPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientOnlyBridge.addBeam(payload);
        }
    }

    private static final class ClientOnlyBridge {
        private static void addBeam(CadBeamPayload payload) {
            cad.regulusxv.cad.client.renderer.CadBeamRenderer.addBeam(payload.start(), payload.end(), payload.durationTicks());
        }
    }
}
