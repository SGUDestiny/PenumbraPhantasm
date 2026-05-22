package destiny.penumbra_phantasm.client.render.particle;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

public class FriendDisappearParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public FriendDisappearParticle(ClientLevel level, double x, double y, double z, SpriteSet sprite, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprite;
        this.friction = 1f;
        this.lifetime = 20;
        this.setSpriteFromAge(sprite);
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
        this.gravity = 0f;
        this.quadSize = 0.5f;
    }

    public void tick() {
        this.setSpriteFromAge(this.sprites);

        if (this.age == 1) {
            level.playLocalSound(x, y, z, SoundRegistry.CHESHIRE_CHEST_LAUGH.get(),
                    SoundSource.AMBIENT, 0.25f, 1f, false);
        }
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public int getLightColor(float p_107086_) {
        int $$1 = super.getLightColor(p_107086_);
        int $$2 = 240;
        int $$3 = $$1 >> 16 & 255;
        return 240 | $$3 << 16;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double v, double v1, double v2, double v3, double v4, double v5) {
            return new FriendDisappearParticle(clientLevel, v, v1, v2, this.spriteSet, v3, v4, v5);
        }
    }
}
