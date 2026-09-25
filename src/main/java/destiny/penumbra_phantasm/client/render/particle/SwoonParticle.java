package destiny.penumbra_phantasm.client.render.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class SwoonParticle extends TextureSheetParticle {
    public SwoonParticle(ClientLevel level, double x, double y, double z, SpriteSet sprite, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, 0, 0, 0);
        this.friction = 1f;
        this.lifetime = 45;
        this.setSprite(sprite.get(1, 1));
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
        this.gravity = 1f;
        this.quadSize = 0f;
        this.alpha = 0f;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            if (this.age >= 15) {
                this.xd = 0;
                this.yd = 0;
                this.zd = 0;

                if (this.age >= 30) {
                    float fadeDelta = (this.age - 30) / 15f;
                    this.alpha = 1 - fadeDelta;
                    this.quadSize = Mth.lerp(fadeDelta, 1f, 1.25f);
                }
            } else {
                this.yd -= 0.04 * (double)this.gravity;
                this.move(this.xd, this.yd, this.zd);

                if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
                    this.xd *= 1.1;
                    this.zd *= 1.1;
                }

                this.xd *= this.friction;
                this.yd *= this.friction;
                this.zd *= this.friction;

                if (this.onGround) {
                    this.xd *= 0.7F;
                    this.zd *= 0.7F;
                }

                if (this.age <= 5) {
                    float fadeDelta = this.age / 5f;
                    this.alpha = fadeDelta;
                    this.quadSize = Mth.lerp(fadeDelta, 0f, 1f);
                }
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
            return new SwoonParticle(clientLevel, v, v1, v2, this.spriteSet, v3, v4, v5);
        }
    }
}
