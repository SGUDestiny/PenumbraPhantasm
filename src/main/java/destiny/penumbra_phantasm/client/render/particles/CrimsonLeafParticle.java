package destiny.penumbra_phantasm.client.render.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class CrimsonLeafParticle extends TextureSheetParticle {
    public CrimsonLeafParticle(ClientLevel level, double x, double y, double z, SpriteSet sprite, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.friction = 0.1f + level.getRandom().nextFloat() * 0.4f;
        this.lifetime = level.getRandom().nextInt(60, 200);
        this.setSprite(sprite.get(this.random.nextInt(4), 4));
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
        this.gravity = 1f;
        this.quadSize = 0.5f;
        this.getLightColor(15);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double v, double v1, double v2, double v3, double v4, double v5) {
            return new CrimsonLeafParticle(clientLevel, v, v1, v2, this.spriteSet, v3, v4, v5);
        }
    }
}
