package destiny.penumbra_phantasm.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class DarkWorldMusicReloadListener implements ResourceManagerReloadListener {
    public static final DarkWorldMusicReloadListener INSTANCE = new DarkWorldMusicReloadListener();

    private DarkWorldMusicReloadListener() {}

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.execute(() -> {
                MusicManager.getInstance().stopImmediately();
                SoundAccess.refreshFountainAmbientsAfterReload();
            });
        }
    }
}
