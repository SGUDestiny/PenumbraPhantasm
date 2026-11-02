package destiny.penumbra_phantasm.client.render.particle;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

public class ShieldParryParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public ShieldParryParticle(ClientLevel level, double x, double y, double z, SpriteSet sprite, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = sprite;
        this.friction = 1f;
        this.lifetime = 10;
        this.setSpriteFromAge(sprite);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
        this.gravity = 1f;
        this.quadSize = 1f;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        if (this.age == 1) {
            level.playLocalSound(x, y, z, SoundRegistry.SHIELD_PARRY.get(), SoundSource.PLAYERS, 1f, 1f, false);
        }

        int sprite = Math.min((this.age / (this.lifetime / 8)), 8);
        this.setSprite(sprites.get(sprite, 8));
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
            return new ShieldParryParticle(clientLevel, v, v1, v2, this.spriteSet, v3, v4, v5);
        }
    }
}
