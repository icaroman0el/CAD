package cad.regulusxv.cad.network;

import cad.regulusxv.cad.CAD;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CadBeamPayload(Vec3 start, Vec3 end, int durationTicks) implements CustomPacketPayload {
    public static final Type<CadBeamPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CAD.MODID, "cad_beam"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CadBeamPayload> STREAM_CODEC = StreamCodec.of(CadBeamPayload::encode, CadBeamPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, CadBeamPayload payload) {
        writeVec3(buffer, payload.start());
        writeVec3(buffer, payload.end());
        buffer.writeVarInt(payload.durationTicks());
    }

    private static CadBeamPayload decode(RegistryFriendlyByteBuf buffer) {
        return new CadBeamPayload(readVec3(buffer), readVec3(buffer), buffer.readVarInt());
    }

    private static void writeVec3(RegistryFriendlyByteBuf buffer, Vec3 vec) {
        buffer.writeDouble(vec.x);
        buffer.writeDouble(vec.y);
        buffer.writeDouble(vec.z);
    }

    private static Vec3 readVec3(RegistryFriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CadBeamPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientOnlyBridge.addBeam(payload));
    }

    private static final class ClientOnlyBridge {
        private static void addBeam(CadBeamPayload payload) {
            cad.regulusxv.cad.client.renderer.CadBeamRenderer.addBeam(payload.start(), payload.end(), payload.durationTicks());
        }
    }
}
