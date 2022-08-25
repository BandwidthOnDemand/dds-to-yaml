package net.es.nsi.dds.yaml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *
 * @author hacksaw
 */
@SpringBootApplication
@EnableSwagger2
public class Application {
  // For logging.
  private static final Logger LOG = LogManager.getLogger(Application.class);

  public static final String ARGNAME_PIDFILE = "pidFile";
  public static final String SYSTEM_PROPERTY_PIDFILE = "pidFile";

  // Keep running while true.
  private static boolean keepRunning = true;

  /**
   * This is the Springboot main for this application.
   *
   * @param args
   * @throws java.util.concurrent.ExecutionException
   * @throws java.lang.InterruptedException
   * @throws org.apache.commons.cli.ParseException
   * @throws java.io.IOException
   */
  public static void main(String[] args) throws ExecutionException, InterruptedException, ParseException, IOException {
    LOG.info("[NSI-DDS-YAML] Starting...");

    // Parse the command line options.
    CommandLineParser parser = new DefaultParser();
    Options options = getOptions();
    CommandLine cmd;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println("Error: You did not provide the correct arguments, see usage below.");
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar dds-yaml.jar [-pidFile <filename>]", options);
      throw e;
    }

    // Write the process id out to file if specified.
    processPidFile(cmd);

    // Initialize the Spring application context.
    ApplicationContext context = SpringApplication.run(Application.class, args);

    // Dump some runtime information.
    RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
    LOG.info("Name: {}, {}", context.getApplicationName(), mxBean.getName());
    LOG.info("Pid: {}", getPid());
    /*
    try {
      LOG.info("Pid: {}", getProcessId(mxBean));
    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException |
            NoSuchMethodException | InvocationTargetException ex) {
      LOG.error("[NSI-DDS-YAML] Could not determine Pid", ex);
    }
    */
    LOG.info("Uptime: {} ms", mxBean.getUptime());
    //LOG.info("BootClasspath: {}", mxBean.getBootClassPath());
    LOG.info("Classpath: {}", mxBean.getClassPath());
    LOG.info("Library Path: {}", mxBean.getLibraryPath());

    for (String argument : mxBean.getInputArguments()) {
      LOG.info("Input Argument: {}", argument);
    }

    // Listen for a shutdown event so we can clean up.
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        LOG.info("[NSI-DDS-YAML] Shutting down RM...");
        Application.setKeepRunning(false);
      }
    }
    );

    // Loop until we are told to shutdown.
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    while (keepRunning) {
      LOG.info("[NSI-DDS-YAML] {}", memStats(memoryBean));
      Thread.sleep(10000);
    }

    LOG.info("[NSI-DDS-YAML] Shutdown complete with uptime: {} ms", mxBean.getUptime());
    System.exit(0);
  }

  /**
   * Build supported command line options for parsing of parameter input.
   *
   * @return List of supported command line options.
   */
  private static Options getOptions() {
    // Create Options object to hold our command line options.
    Options options = new Options();

    Option pidFileOption = new Option(ARGNAME_PIDFILE, true, "The file in which to write the process pid");
    pidFileOption.setRequired(false);
    options.addOption(pidFileOption);
    return options;
  }

  /**
   * Processes the "pidFile" command line and system property option.
   *
   * @param cmd Commands entered by the user.
   * @return The specified pidFile.
   * @throws IOException
   */
  private static void processPidFile(CommandLine cmd) throws IOException {
    // Get the application base directory.
    String pidFile = System.getProperty(SYSTEM_PROPERTY_PIDFILE);
    pidFile = cmd.getOptionValue(ARGNAME_PIDFILE, pidFile);
    long pid = getPid();
    if (pidFile == null || pidFile.isEmpty() || pid == -1) {
      return;
    }

    BufferedWriter out = null;
    try {
      FileWriter fstream = new FileWriter(pidFile, false);
      out = new BufferedWriter(fstream);
      out.write(String.valueOf(pid));
    } catch (IOException e) {
      System.err.printf("Error: %s\n", e.getMessage());
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Get our process pid.
   *
   * @return pid
   */
  private static long getPid() {
    return ProcessHandle.current().pid();
    //RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
    //try {
    //  return getProcessId(mxBean);
    //} catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException
    //        | NoSuchMethodException | InvocationTargetException ex) {
    //  System.err.printf("Error: Could not determine pid, ex = %s\n", ex.toString());
    //  return -1;
    //}
  }

  private static final int MEGABYTE = (1024 * 1024);

  private static String memStats(MemoryMXBean memoryBean) {
    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
    long maxMemory = heapUsage.getMax() / MEGABYTE;
    long usedMemory = heapUsage.getUsed() / MEGABYTE;
    return "Memory use :" + usedMemory + "M/" + maxMemory + "M";
  }

  /**
   *
   * @return
   * @throws NoSuchFieldException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   */
  private static int getProcessId(RuntimeMXBean mxBean) throws NoSuchFieldException, IllegalArgumentException,
          IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    java.lang.reflect.Field jvm = mxBean.getClass().getDeclaredField("jvm");
    jvm.setAccessible(true);
    Object mgmt = jvm.get(mxBean);
    java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
    pid_method.setAccessible(true);
    return (int) pid_method.invoke(mgmt);
  }

  /**
   * Returns a boolean indicating whether the PCE should continue running (true) or should terminate (false).
   *
   * @return true if the PCE should be running, false otherwise.
   */
  public static boolean isKeepRunning() {
    return keepRunning;
  }

  /**
   * Set whether the PCE should be running (true) or terminated (false).
   *
   * @param keepRunning true if the PCE should be running, false otherwise.
   */
  public static void setKeepRunning(boolean keepRunning) {
    Application.keepRunning = keepRunning;
  }
}