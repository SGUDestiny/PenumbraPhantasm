package destiny.penumbra_phantasm.client.render.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SwoonParticle extends TextureSheetParticle {
    public float xSize = 1f;
    public float ySize = 1f;

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
                    this.ySize = Mth.lerp(fadeDelta, 1f, 2.5f);
                    this.xSize = Mth.lerp(fadeDelta, 1f, 0.5f);
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
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        Vec3 cameraPos = camera.getPosition();
        float renderX = (float)(Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float renderY = (float)(Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float renderZ = (float)(Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());

        Quaternionf quaternion = new Quaternionf(camera.rotation());
        if (this.roll != 0f) {
            quaternion.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll));
        }

        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-1f, -1f, 0f),
                new Vector3f(-1f,  1f, 0f),
                new Vector3f( 1f,  1f, 0f),
                new Vector3f( 1f, -1f, 0f)
        };

        float baseSize = this.getQuadSize(partialTick);
        for(int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];

            //Strech And Squish here.
            vertex.mul(xSize, ySize, 1f);
            vertex.mul(baseSize);

            vertex.rotate(quaternion);
            vertex.add(renderX, renderY, renderZ);
        }

        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();
        int light = this.getLightColor(partialTick);

        consumer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).uv(maxU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        consumer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).uv(maxU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        consumer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).uv(minU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        consumer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).uv(minU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
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
