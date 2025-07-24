package me.contaria.seedqueue;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.contaria.seedqueue.keybindings.SeedQueueKeyBindings;
import me.contaria.seedqueue.keybindings.SeedQueueMultiKeyBinding;
import net.minecraft.client.MinecraftClient;

/**
 * Config class based on SpeedrunAPI, initialized on prelaunch.
 * <p>
 * When implementing new options, make sure no Minecraft classes are loaded during initialization!
 */
@SuppressWarnings("FieldMayBeFinal")
public class SeedQueueConfig {
    public int maxCapacity = 9;
    public int maxConcurrently = 1;
    public int maxConcurrently_onWall = 9;
    public int maxWorldGenerationPercentage = 100;
    public boolean resumeOnFilledQueue = false;

    public boolean useWall = true;
    public int rows = 3;
    public int columns = 3;
    public final WindowSize simulatedWindowSize = new WindowSize();
    public int resetCooldown = 150;
    public boolean waitForPreviewSetup = true;
    public boolean bypassWall = false;
    public boolean smartSwitch = false;

    public int wallFPS = 60;
    public int previewFPS = 15;
    public int preparingPreviews = -1; // auto
    public boolean freezeLockedPreviews = false;
    public boolean reduceLevelList = true;

    public boolean showAdvancedSettings = false;

    public int seedQueueThreadPriority = Thread.NORM_PRIORITY;
    public int serverThreadPriority = 4;

    public boolean showDebugMenu = true;
    public int benchmarkResets = 1000;
    public boolean useWatchdog = false;

    public boolean alwaysRedrawPreview = false;

    public final SeedQueueMultiKeyBinding[] keyBindings = new SeedQueueMultiKeyBinding[]{
            SeedQueueKeyBindings.play,
            SeedQueueKeyBindings.focusReset,
            SeedQueueKeyBindings.reset,
            SeedQueueKeyBindings.lock,
            SeedQueueKeyBindings.resetAll,
            SeedQueueKeyBindings.resetColumn,
            SeedQueueKeyBindings.resetRow,
            SeedQueueKeyBindings.playNextLock,
            SeedQueueKeyBindings.scheduleJoin,
            SeedQueueKeyBindings.scheduleAll,
            SeedQueueKeyBindings.startBenchmark,
            SeedQueueKeyBindings.cancelBenchmark
    };

    // see Window#calculateScaleFactor
    public int calculateSimulatedScaleFactor(int guiScale, boolean forceUnicodeFont) {
        int scaleFactor = 1;
        while (scaleFactor != guiScale && scaleFactor < this.simulatedWindowSize.width() && scaleFactor < this.simulatedWindowSize.height() && this.simulatedWindowSize.width() / (scaleFactor + 1) >= 320 && this.simulatedWindowSize.height() / (scaleFactor + 1) >= 240) {
            scaleFactor++;
        }
        if (forceUnicodeFont) {
            scaleFactor += guiScale % 2;
        }
        return scaleFactor;
    }

    public static class WindowSize {
        private int width;
        private int height;

        public int width() {
            if (this.width == 0) {
                this.width = MinecraftClient.getInstance().width;
            }
            return this.width;
        }

        public void setWidth(int width) {
            this.width = Math.max(0, Math.min(16384, width));
        }

        public int height() {
            if (this.height == 0) {
                this.height = MinecraftClient.getInstance().height;
            }
            return this.height;
        }

        public void setHeight(int height) {
            this.height = Math.max(0, Math.min(16384, height));
        }

        public void init() {
            this.width();
            this.height();
        }

        public void fromJson(JsonObject jsonObject) {
            this.setWidth(jsonObject.get("width").getAsInt());
            this.setHeight(jsonObject.get("height").getAsInt());
        }

        public JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("width", new JsonPrimitive(this.width));
            jsonObject.add("height", new JsonPrimitive(this.height));
            return jsonObject;
        }
    }
}
