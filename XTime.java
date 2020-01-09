package hw8;

/* XTime.java - Simple benchmarking for driver program in pure Java.
 *
 * Based on JHU's 226 xtime script for ubuntu systems:
 *
 *     #!/bin/sh
 *     /usr/bin/time -f '%e seconds %M kilobytes %C' "$@"
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.util.Arrays;

/**
 * XTime class for simple single-run benchmarking of a class with a main
 * method. Can use public methods to start/stop/reset statistics and get
 * a String including the statistics in {@link XTime#getStatistics()}, or
 * use XTime main method to print those statistics.
 *
 * Invoke by passing the class with a main method as the first argument.
 * Arguments after the first argument will be passed as the arguments to
 * the main method. For example:
 *
 * $ java XTime SomeDriver "blah" < "blah.txt"
 *
 * Will report statistics on running SomeDriver.main(), passing "blah" as
 * an argument to SomeDriver.main(), and "blah.txt" input.
 */
@SuppressWarnings("CheckStyle")
public class XTime {

    private static final int NOT_RUNNING = -1;
    private static final long KB = 1024;

    private Runtime runtime;

    private boolean isRunning;

    // Time (in milliseconds)
    private long netTime;
    private long startTime;

    // Memory used (in bytes, printed as kilobytes)
    private long netMemory;
    private long startMemory;


    /** Create new XTime to collect stats. */
    public XTime() {
        this.runtime = Runtime.getRuntime();
        this.startTime = NOT_RUNNING;
        this.startMemory = NOT_RUNNING;
        this.isRunning = false;
    }

    /**
     * Start collecting statistics for JVM runtime / heap usage.
     * @throws RuntimeException If already running when started.
     */
    public void start() {
        if (this.isRunning) {
            throw new RuntimeException("xtime start() when already running");
        }

        this.isRunning = true;
        this.startTime = this.getCurrentTime();
        this.startMemory = this.getCurrentMemory();
    }

    /**
     * Stop collecting statistics for JVM runtime / heap usage.
     * @throws RuntimeException If not running when stopped.
     */
    public void stop() {
        if (!this.isRunning) {
            throw new RuntimeException("xtime stop() when not running");
        }

        this.netTime += this.getCurrentTime() - this.startTime;
        this.netMemory += this.getCurrentMemory() - this.startMemory;
        this.isRunning = false;
    }

    /** Reset statistics. */
    public void reset() {
        if (this.isRunning) {
            this.startMemory = this.getCurrentMemory();
            this.startTime = this.getCurrentTime();
        }
        this.netTime = 0;
        this.netMemory = 0;
    }

    /**
     * Get string of statistics.
     * @return String containing time in milliseconds and heap usage in
     * kilobytes.
     */
    public String getStatistics() {
        return String.format("%d ms %d kb", this.netTime, this.netMemory / KB);
    }

    /**
     * Main method.
     * @param args See usage.
     * @throws MalformedURLException If could not load class.
     * @throws ClassNotFoundException If could not load class.
     * @throws InvocationTargetException If could not invoke main.
     * @throws IllegalAccessException If could not invoke main.
     */
    public static void main(String[] args) throws MalformedURLException,
            ClassNotFoundException, InvocationTargetException,
            IllegalAccessException {

        if (args.length < 1) {
            System.out.println("Usage: java XTime <main_class> [...]");
            return;
        }

        // Get driver main method.
        Class<?> c = load(args[0]);
        Method m = getMainMethod(c);

        if (m == null) {
            return;
        }

        // Invoke main and collect stats.
        String[] params = Arrays.copyOfRange(args, 1, args.length);
        XTime x = new XTime();
        x.start();
        m.invoke(null, (Object) params);
        x.stop();

        // Print stats.
        System.out.println(x.getStatistics());
    }


    // Get current memory used.
    private long getCurrentMemory() {
        return (runtime.totalMemory() - runtime.freeMemory());
    }

    // Get current time.
    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    // Get main method of class c.
    private static Method getMainMethod(Class<?> c) {
        Class[] mainParams = new Class[1];
        mainParams[0] = String[].class;
        try {
            return c.getMethod("main", mainParams);
        } catch (NoSuchMethodException e) {
            System.err.println("No main method found");
        }
        return null;
    }

    // Load class by name
    private static Class<?> load(String className) throws
            ClassNotFoundException,
            MalformedURLException {
        URL url = FileSystems.getDefault().getPath("").toUri().toURL();
        URLClassLoader loader = new URLClassLoader(new URL[]{url});
        return loader.loadClass(className);
    }
}
