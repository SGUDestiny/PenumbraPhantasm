package destiny.penumbra_phantasm.client.render.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class FountainDarknessParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected FountainDarknessParticle(ClientLevel level, double x, double y, double z, SpriteSet sprite, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprite;
        this.friction = 1f;
        this.lifetime = 60;
        this.setSpriteFromAge(sprite);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
        this.gravity = 1f;
        this.quadSize = 0.1f;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float reduction = 0.00001f * this.age;

            if (this.xd > 0) {
                this.xd = Math.max(this.xd - reduction, 0);
            } else if (this.xd < 0) {
                this.xd = Math.min(this.xd + reduction, 0);
            }

            this.yd += 0.0005f * this.age;

            if (this.zd > 0) {
                this.zd = Math.max(this.zd - reduction, 0);
            } else if (this.zd < 0) {
                this.zd = Math.min(this.zd + reduction, 0);
            }

            this.quadSize += Math.min(this.age * 0.001f, 3f);
            this.move(this.xd, this.yd, this.zd);

            if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
                this.xd *= 1.1f;
                this.zd *= 1.1f;
            }

            if (this.age > 50) {
                this.alpha -= 0.1f;
            }

            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
        }
    }

    @Override
    public boolean shouldCull() {
        return false;
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
            return new FountainDarknessParticle(clientLevel, v, v1, v2, this.spriteSet, v3, v4, v5);
        }
    }
}
