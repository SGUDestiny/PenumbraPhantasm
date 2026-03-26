package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MusicSuppressionMixin {
    @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
    private void penumbraPhantasm$suppressDarkWorldMusic(CallbackInfoReturnable<Music> cir) {
        Minecraft mc = (Minecraft) (Object) this;
        LocalPlayer player = mc.player;
        if (player != null && DarkWorldUtil.isDarkWorld(player.level())) {
            cir.setReturnValue(new Music(SoundEvents.MUSIC_GAME, Integer.MAX_VALUE, Integer.MAX_VALUE, true));
        }
    }
}
