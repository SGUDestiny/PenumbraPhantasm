package destiny.penumbra_phantasm.client.render.particles;

import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class LuminescentParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final boolean rollDirection;
    private final float rollOffset;

    public LuminescentParticle(ClientLevel level, double x, double y, double z, SpriteSet sprite, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprite;
        this.friction = 1f;
        this.lifetime = 70 + level.random.nextInt(-5, 10);
        this.setSpriteFromAge(sprite);
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
        this.yd = 0.05f + ModUtil.getBoundRandomFloatStatic(level, -0.01f, 0.01f);
        this.gravity = 1f;
        this.quadSize = 0.2f;
        this.rollDirection = level.random.nextBoolean();
        this.rollOffset = ModUtil.getBoundRandomFloatStatic(level, -0.1f, 0.1f);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            int sprite = this.age / (this.lifetime / 3);
            this.setSprite(sprites.get(sprite, 3));
            this.move(0, this.yd, 0);
            this.quadSize -= 0.2f / this.lifetime;
            if (this.rollDirection) {
                this.roll += (1.5f + this.rollOffset) / this.lifetime;
            } else {
                this.roll -= (1.5f + this.rollOffset) / this.lifetime;
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
            return new LuminescentParticle(clientLevel, v, v1, v2, this.spriteSet, v3, v4, v5);
        }
    }
}
