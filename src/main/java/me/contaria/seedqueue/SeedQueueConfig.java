package me.contaria.seedqueue;

/**
 * Config class based on SpeedrunAPI, initialized on prelaunch.
 * <p>
 * When implementing new options, make sure no Minecraft classes are loaded during initialization!
 */
@SuppressWarnings("FieldMayBeFinal")
public class SeedQueueConfig {
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    public int maxCapacity = 4;
    public int maxConcurrently = 2;
    public int maxConcurrently_onWall = 1;
    public int maxWorldGenerationPercentage = 100;
    public boolean resumeOnFilledQueue = false;

    public boolean reduceLevelList = true;

    public int seedQueueThreadPriority = Thread.NORM_PRIORITY;
    public int serverThreadPriority = 4;
    private int backgroundExecutorThreads = 0;
    public int backgroundExecutorThreadPriority = 3;
    private int wallExecutorThreads = 0;
    public int wallExecutorThreadPriority = 4;

    /**
     * Returns the amount of threads the Background Executor should use according to {@link SeedQueueConfig#backgroundExecutorThreads}.
     * Calculates a good default based on {@link SeedQueueConfig#maxConcurrently} if set to {@code 0}.
     *
     * @return The parallelism to be used for the Background Executor Service.
     */
    public int getBackgroundExecutorThreads() {
        if (this.backgroundExecutorThreads == 0) {
            return Math.max(1, Math.min(this.maxConcurrently + 1, PROCESSORS));
        }
        return this.backgroundExecutorThreads;
    }

    /**
     * Returns the amount of threads the Wall Executor should use according to {@link SeedQueueConfig#wallExecutorThreads}.
     * The amount of available processors is used if set to {@code 0}.
     *
     * @return The parallelism to be used for the Background Executor Service.
     */
    public int getWallExecutorThreads() {
        if (this.wallExecutorThreads == 0) {
            return Math.max(1, PROCESSORS);
        }
        return this.wallExecutorThreads;
    }
}
