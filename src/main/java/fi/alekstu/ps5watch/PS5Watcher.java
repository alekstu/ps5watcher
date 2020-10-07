package fi.alekstu.ps5watch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PS5Watcher {

    private static final String URL = "https://www.prisma.fi/fi/prisma/osasto/elektroniikka-gaming-pelikonsolit#facet:-100283111110121&productBeginIndex:0&facetLimit:&orderBy:9&pageView:&minPrice:&maxPrice:&pageSize:&";
    private final static String DATE_FORMAT = "dd.MM.yyyy HH:mm:ssZ";

    private static final List<String> TAG_LIST = Arrays.asList(
            "ps 5",
            "ps5",
            "ps-5",
            "playstation 5",
            "playstation5",
            "playstation-5"
    );

    private final ScheduledExecutorService scheduledExecutorService;
    private final AtomicBoolean running;
    private final AtomicLong pollCount;
    private final Emailer emailer;

    public PS5Watcher() {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(); // Separated Executor to handle polling in another Thread
        emailer = new Emailer();
        running = new AtomicBoolean(true);
        pollCount = new AtomicLong(1L);
    }


    /**
     * Start polling Prisma site to see if PS5 is listed on the website.
     */
    public void startPollingPrismaWebsite() {
        emailer.sendInfoMail("Started polling Prisma consoles for PS5 " + new SimpleDateFormat(DATE_FORMAT).format(new Date()));

        // call method pollPrismaWebsite every 31 seconds.
        // milliseconds
        final long SLEEP_BETWEEN_POLL = 31000L;
        scheduledExecutorService.scheduleWithFixedDelay(this::pollPrismaWebsite, 0L, SLEEP_BETWEEN_POLL, TimeUnit.MILLISECONDS);

        System.out.println("Polling for PS5 Started - sleep between polls is: " + SLEEP_BETWEEN_POLL + " ms.");
    }

    /**
     * Do a single poll. Send email if PS5 is released
     */
    private void pollPrismaWebsite() {
        List<ListedConsole> consoles;
        try {
            consoles = getPrismaListedConsoles();
        } catch (final IOException e) {
            System.out.println("Received exception getting consoleList " + e);
            emailer.sendCrashedMail("Getting consoles failed", e);
            return;
        }

        final ListedConsole listedPS5 = getListedPS5(consoles);

        if (listedPS5 == null) {
            System.out.println(
                    "PS5 not listed yet, try # " + pollCount.getAndAdd(1L)
                            + " at "
                            + new SimpleDateFormat(DATE_FORMAT).format(new Date()));
        } else {
            System.out.println("Console found: " + listedPS5.getName() + " - " + listedPS5.getUrl());
            sendConsoleListedMail(listedPS5);
            quitPolling();
        }

    }


    /**
     * Connect to Prisma website and parse all Consoles from HTML.
     * @return List of consoles
     * @throws IOException if connecting to website fails.
     */
    private List<ListedConsole> getPrismaListedConsoles() throws IOException {
        final Document doc = Jsoup.connect(URL).get(); // connect and get site HTML
        final List<ListedConsole> consoles = new ArrayList<>();

        final Elements products = doc.select("div.details"); // find div with class "details"

        for (final Element product : products) {
            final Element productLink = product.select("h3.product_name > a").first(); // get product name
            final String url = productLink.attr("href");  // get link address
            consoles.add(new ListedConsole(productLink.text(), url));
        }

        System.out.println(doc.title() + ": (" + consoles.size() + ") total consoles found.");
        return consoles;
    }

    /**
     * Check if List of Console contains PS5.
     * @param consoles list of Consoles
     * @return Console if it's PS5.
     */
    private static ListedConsole getListedPS5(final List<ListedConsole> consoles) {
        if (consoles == null || consoles.isEmpty()) {
            return null;
        }
        return consoles.stream()
                .filter(c -> c.getName() != null)
                .filter(console -> TAG_LIST.stream() // check tags
                        .anyMatch(console.getName()::equalsIgnoreCase)) // ignore casing
                .findFirst()
                .orElse(null);
    }

    /**
     * Send mail when PS5 is found.
     * @param listedPS5 PS5 console.
     */
    private void sendConsoleListedMail(final ListedConsole listedPS5) {
        emailer.sendMail(listedPS5);
    }


    /**
     * Stop polling and close
     */
    private void quitPolling() {
        this.setDone();
        this.shutdownExecutor();
        System.out.println("Exiting polling Prisma website.." + new SimpleDateFormat(DATE_FORMAT).format(new Date()));
    }

    /**
     * Shutdown executor and separated polling.
     */
    public void shutdownExecutor() {
        scheduledExecutorService.shutdown();
        System.out.println("Shutdown scheduled executor: " + scheduledExecutorService.isShutdown());
    }


    /**
     * Check if PS5 is being polled from Prisma.
     * @return true if is polling.
     */
    public boolean isPolling() {
        return running.get();
    }

    /**
     * Set polling done.
     */
    public void setDone() {
        running.set(false);
    }




}
