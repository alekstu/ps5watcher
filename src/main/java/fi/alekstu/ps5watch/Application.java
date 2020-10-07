package fi.alekstu.ps5watch;


public class Application {

    public static void main(String[] args) {

        final PS5Watcher ps5Watcher = new PS5Watcher();

        addShutdownHook(ps5Watcher);
        ps5Watcher.startPollingPrismaWebsite();

        while (ps5Watcher.isPolling()) {
            System.out.println("Polling.." + ps5Watcher.isPolling());
            try {
                Thread.sleep(60000L * 5); //sleep 5min here..
            } catch (InterruptedException e) {
                ps5Watcher.shutdownExecutor();
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Application finished.");

    }

    private static void addShutdownHook(final PS5Watcher ps5Watcher) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook called");
            ps5Watcher.shutdownExecutor();
        }));
    }


}
