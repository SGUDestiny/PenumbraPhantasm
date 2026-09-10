package destiny.penumbra_phantasm.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import destiny.penumbra_phantasm.server.registry.FluidTypeRegistry;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LiquidBlockRenderer.class, priority = 2000)
public class LiquidBlockRendererMixin {

}