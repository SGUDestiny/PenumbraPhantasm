package destiny.penumbra_phantasm.server.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.function.Supplier;

public class    ClientBoundParticlePacket {
    private final ResourceLocation particleId;
    private final double x;
    private final double y;
    private final double z;
    private final double vx;
    private final double vy;
    private final double vz;
    private final int count;
    public ClientBoundParticlePacket(ResourceLocation particleId, double x, double y, double z, double vx, double vy, double vz, int count) {
        this.particleId = particleId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.count = count;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.particleId);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeDouble(this.vx);
        buf.writeDouble(this.vy);
        buf.writeDouble(this.vz);
        buf.writeVarInt(this.count);
    }

    public static ClientBoundParticlePacket decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        double vx = buf.readDouble();
        double vy = buf.readDouble();
        double vz = buf.readDouble();
        int count = buf.readVarInt();
        return new ClientBoundParticlePacket(id, x, y, z, vx, vy, vz, count);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientboundPacketHandler.sendParticle(particleId, x, y, z, vx, vy, vz, count));
        return true;
    }
}