package destiny.penumbra_phantasm.client.sound;

import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MusicManager {
    public static final MusicManager INSTANCE = new MusicManager();
    public static final float MUSIC_VOLUME = 0.2F;
    public static final double FOUNTAIN_MUSIC_RANGE = 24.0;

    public final Minecraft minecraft = Minecraft.getInstance();
    public final Map<ResourceLocation, BiomeMusic> biomeMusicMap = new HashMap<>();
    public final RandomSource random = RandomSource.create();
    public boolean initialized = false;

    @Nullable
    public ManagedMusicSound currentSound;
    @Nullable
    public SoundEvent currentSoundEvent;
    public MusicPriority currentPriority = MusicPriority.BIOME;

    public State state = State.SILENT;
    public int waitTimer = 0;
    public int fadeInTicks = 0;
    public boolean currentLooping = true;

    @Nullable
    public SoundEvent pendingSoundEvent;
    public MusicPriority pendingPriority = MusicPriority.BIOME;
    public boolean pendingLooping = true;

    private float lastMusicSlider = Float.NaN;

    public static MusicManager getInstance() {
        return INSTANCE;
    }

    private void ensureInitialized() {
        if (!initialized) {
            initialized = true;
            biomeMusicMap.put(
                    new ResourceLocation(PenumbraPhantasm.MODID, "field_of_hopes_and_dreams"),
                    new BiomeMusic(() -> SoundRegistry.FIELD_OF_HOPES_AND_DREAMS.get())
            );
            biomeMusicMap.put(
                    new ResourceLocation(PenumbraPhantasm.MODID, "scarlet_forest"),
                    new BiomeMusic(() -> SoundRegistry.EVERLASTING_AUTUMN.get())
            );
        }
    }

    public void tick() {
        ensureInitialized();

        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            stopImmediately();
            return;
        }

        if (!DarkWorldUtil.isDarkWorld(level)) {
            stopImmediately();
            return;
        }

        float musicSlider = minecraft.options.getSoundSourceVolume(SoundSource.MUSIC);
        boolean musicUnmuted = !Float.isNaN(lastMusicSlider) && lastMusicSlider <= 1.0E-4F && musicSlider > 1.0E-4F;
        lastMusicSlider = musicSlider;

        if (currentSound != null && (state == State.PLAYING || state == State.FADING_IN)
                && !minecraft.getSoundManager().isActive(currentSound)) {
            if (!currentSound.isStopped()) {
                currentSound.stopSound();
                minecraft.getSoundManager().stop(currentSound);
            }
            currentSound = null;
            currentSoundEvent = null;
            state = State.SILENT;
            fadeInTicks = 0;
        }

        SoundEvent desiredSound = null;
        MusicPriority desiredPriority = MusicPriority.BIOME;
        boolean desiredLooping = true;

        SoundEvent fountainMusic = fountainMusic(player, level);
        if (fountainMusic != null) {
            desiredSound = fountainMusic;
            desiredPriority = MusicPriority.FOUNTAIN;
        }

        if (desiredPriority.ordinal() < MusicPriority.FOUNTAIN.ordinal()) {
            BiomeMusic biomeMusic = biomeMusic(player, level);

            if (biomeMusic != null) {
                desiredSound = biomeMusic.sound();
                desiredLooping = biomeMusic.looping();
            }
        }

        if (desiredSound == null) {
            if (state != State.SILENT && state != State.FADING_OUT) {
                beginFadeOut();
            }

            tickFade();
            return;
        }

        if (musicUnmuted && desiredSound.equals(currentSoundEvent)
                && desiredPriority == currentPriority
                && (state == State.PLAYING || state == State.FADING_IN)) {
            startTrack(desiredSound, desiredPriority, desiredLooping);
            tickFade();
            return;
        }

        boolean sameTrack = desiredSound.equals(currentSoundEvent) && desiredPriority == currentPriority;

        if (sameTrack && (state == State.PLAYING || state == State.FADING_IN)) {
            tickFade();
            return;
        }

        if (sameTrack && state == State.WAITING) {
            tickWaiting(desiredSound, desiredPriority, desiredLooping);
            return;
        }

        if (!sameTrack || state == State.SILENT) {
            if (state == State.PLAYING || state == State.FADING_IN) {
                pendingSoundEvent = desiredSound;
                pendingPriority = desiredPriority;
                pendingLooping = desiredLooping;
                beginFadeOut();
            } else if (state == State.FADING_OUT) {
                pendingSoundEvent = desiredSound;
                pendingPriority = desiredPriority;
                pendingLooping = desiredLooping;
            } else {
                startTrack(desiredSound, desiredPriority, desiredLooping);
            }
        }

        tickFade();
    }

    public void requestMusic(SoundEvent sound, MusicPriority priority, boolean looping) {
        if (priority.ordinal() >= currentPriority.ordinal()) {
            pendingSoundEvent = sound;
            pendingPriority = priority;
            pendingLooping = looping;
            if (state == State.PLAYING || state == State.FADING_IN) {
                beginFadeOut();
            } else if (state == State.SILENT || state == State.WAITING) {
                startTrack(sound, priority, looping);
            }
        }
    }

    public void stopMusic(MusicPriority priority) {
        if (currentPriority == priority && state != State.SILENT) {
            pendingSoundEvent = null;
            beginFadeOut();
        }
    }

    private void tickFade() {
        if (currentSound == null) return;

        if (currentSound.isStopped()) {
            if (state == State.PLAYING && currentSoundEvent != null) {
                BiomeMusic bm = findBiomeMusic(currentSoundEvent);
                if (bm != null && !bm.looping()) {
                    state = State.WAITING;
                    waitTimer = bm.minDelay() + random.nextInt(Math.max(1, bm.maxDelay() - bm.minDelay()));
                    currentSound = null;
                    return;
                }
            }
            state = State.SILENT;
            currentSound = null;
            currentSoundEvent = null;
            checkPendingMusic();
            return;
        }

        if (state == State.FADING_OUT && currentSound.isFadedOut()) {
            currentSound.stopSound();
            minecraft.getSoundManager().stop(currentSound);
            currentSound = null;
            currentSoundEvent = null;
            state = State.SILENT;
            checkPendingMusic();
            return;
        }

        if (state == State.FADING_IN) {
            fadeInTicks++;
            if (fadeInTicks > 10 && !minecraft.getSoundManager().isActive(currentSound)) {
                startTrack(currentSoundEvent, currentPriority, currentLooping);
                return;
            }
            if (currentSound.getTargetVolume() > 0
                    && currentSound.getLinearVolume() >= currentSound.getTargetVolume() - 0.005F) {
                state = State.PLAYING;
            }
        }
    }

    private void tickWaiting(SoundEvent desiredSound, MusicPriority desiredPriority, boolean desiredLooping) {
        waitTimer--;
        if (waitTimer <= 0) {
            startTrack(desiredSound, desiredPriority, desiredLooping);
        }
    }

    private void beginFadeOut() {
        if (currentSound != null && !currentSound.isStopped()) {
            currentSound.setTargetVolume(0.0F);
            state = State.FADING_OUT;
        } else {
            state = State.SILENT;
            checkPendingMusic();
        }
    }

    private void startTrack(SoundEvent sound, MusicPriority priority, boolean looping) {
        if (currentSound != null && !currentSound.isStopped()) {
            currentSound.stopSound();
            minecraft.getSoundManager().stop(currentSound);
        }

        currentSoundEvent = sound;
        currentPriority = priority;
        currentLooping = looping;
        currentSound = new ManagedMusicSound(sound, looping);
        currentSound.setTargetVolume(MUSIC_VOLUME);
        minecraft.getSoundManager().queueTickingSound(currentSound);
        state = State.FADING_IN;
        fadeInTicks = 0;
    }

    private void checkPendingMusic() {
        if (pendingSoundEvent != null) {
            SoundEvent sound = pendingSoundEvent;
            MusicPriority priority = pendingPriority;
            boolean looping = pendingLooping;
            pendingSoundEvent = null;
            startTrack(sound, priority, looping);
        }
    }

    public void stopImmediately() {
        if (currentSound != null && !currentSound.isStopped()) {
            currentSound.stopSound();
            minecraft.getSoundManager().stop(currentSound);
        }
        currentSound = null;
        currentSoundEvent = null;
        state = State.SILENT;
        pendingSoundEvent = null;
        waitTimer = 0;
    }

    @Nullable
    private BiomeMusic biomeMusic(LocalPlayer player, ClientLevel level) {
        Holder<Biome> biomeHolder = level.getBiome(player.blockPosition());
        ResourceLocation biomeId = biomeHolder.unwrapKey()
                .map(ResourceKey::location)
                .orElse(null);
        if (biomeId == null) return null;
        return biomeMusicMap.get(biomeId);
    }

    @Nullable
    private SoundEvent fountainMusic(LocalPlayer player, ClientLevel level) {
        if (!Config.darkFountainMusic) return null;

        LazyOptional<DarkFountainCapability> lazyCap = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if (!lazyCap.isPresent()) return null;

        DarkFountainCapability cap = lazyCap.resolve().orElse(null);
        if (cap == null) return null;

        for (Map.Entry<BlockPos, DarkFountain> entry : cap.darkFountains.entrySet()) {
            DarkFountain fountain = entry.getValue();
            if (fountain.animationTimer != -1) continue;

            Vec3 playerPos = player.position();
            BlockPos fountainPos = fountain.getFountainPos();
            double distance;

            if (!DarkWorldUtil.isDarkWorld(minecraft.level)) {
                distance = fountainPos.getCenter().distanceTo(playerPos);
            } else {
                if (playerPos.y < fountainPos.getY()) {
                    distance = fountainPos.getCenter().distanceTo(playerPos);
                } else {
                    Vec3 playerPos2d = new Vec3(playerPos.x, 0f, playerPos.z);
                    Vec3 fountainPos2d = new Vec3(fountainPos.getX(), 0, fountainPos.getZ());

                    distance = fountainPos2d.distanceTo(playerPos2d);
                }
            }

            if (distance <= FOUNTAIN_MUSIC_RANGE) {
                return SoundAccess.getFountainMusic();
            }
        }
        return null;
    }

    @Nullable
    private BiomeMusic findBiomeMusic(SoundEvent sound) {
        for (BiomeMusic bm : biomeMusicMap.values()) {
            if (bm.sound().equals(sound)) return bm;
        }
        return null;
    }

    public boolean isPlayingPriority(MusicPriority priority) {
        return currentPriority == priority && (state == State.PLAYING || state == State.FADING_IN);
    }

    private enum State {
        SILENT,
        FADING_IN,
        PLAYING,
        FADING_OUT,
        WAITING
    }
}
