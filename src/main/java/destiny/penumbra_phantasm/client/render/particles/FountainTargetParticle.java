package destiny.penumbra_phantasm.client.render.particles;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class FountainTargetParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public FountainTargetParticle(ClientLevel level, double x, double y, double z, SpriteSet sprite, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprite;
        this.friction = 1f;
        this.lifetime = 4;
        this.setSpriteFromAge(sprite);
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
        this.gravity = 0f;
        this.quadSize = 0.2f;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            if (this.age == 1) {
                level.playLocalSound(x, y, z, SoundRegistry.FOUNTAIN_TARGET.get(), SoundSource.AMBIENT, 1f, 1f, false);
            }
            if (this.age == 2) {
                this.setSprite(sprites.get(1, 1));
            }
            if (this.age == 3) {
                this.setSprite(sprites.get(2, 2));
            }
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float p_107086_) {
        int $$1 = super.getLightColor(p_107086_);
        int $$2 = 240;
        int $$3 = $$1 >> 16 & 255;
        return 240 | $$3 << 16;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double v, double v1, double v2, double v3, double v4, double v5) {
            return new FountainTargetParticle(clientLevel, v, v1, v2, this.spriteSet, v3, v4, v5);
        }
    }
}
