package com.smartcity360;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Application {

    public static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
     
        Logger.getLogger("org.apache.jasper.servlet.TldScanner").setLevel(Level.WARNING);

        int activePort = acquirePort(PORT);

        String webappDirLocation = "src/main/webapp/";
        File webappDir = new File(webappDirLocation);
        if (!webappDir.exists()) {
            webappDir = new File("src/main/webapp");
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(activePort);
        tomcat.getConnector();

        String contextPath = "";
        StandardContext ctx = (StandardContext) tomcat.addWebapp(contextPath, webappDir.getAbsolutePath());
        ctx.setParentClassLoader(Application.class.getClassLoader());

      
        File additionWebInfClasses = new File("target/classes");
        if (!additionWebInfClasses.exists()) {
            additionWebInfClasses = new File("out/production/SmartCity360-New");
        }

        WebResourceRoot resources = new StandardRoot(ctx);
        if (additionWebInfClasses.exists()) {
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(), "/"));
        }
        ctx.setResources(resources);

        System.out.println("==================================================================");
        System.out.println(" SmartCity360 Java Web Application starting on http://localhost:" + activePort);

        System.out.println("==================================================================");

        tomcat.start();
        tomcat.getServer().await();
    }


    private static int acquirePort(int preferredPort) {
        if (isPortFree(preferredPort)) {
            return preferredPort;
        }

        System.out.println("Port " + preferredPort + " is currently in use. Attempting to stop previous instance...");
        freePortIfPossible(preferredPort);

        for (int i = 0; i < 15; i++) {
            if (isPortFree(preferredPort)) {
                return preferredPort;
            }
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        for (int candidate = preferredPort + 1; candidate <= preferredPort + 50; candidate++) {
            if (isPortFree(candidate)) {
                System.out.println("Port " + preferredPort + " is occupied by another service; switching to port " + candidate + ".");
                return candidate;
            }
        }
        return preferredPort;
    }

    private static boolean isPortFree(int port) {
        try (ServerSocket socket = new ServerSocket()) {
            socket.setReuseAddress(false);
            socket.bind(new InetSocketAddress(port));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void freePortIfPossible(int port) {
        long currentPid = ProcessHandle.current().pid();
        Set<Long> pids = findListeningPids(port);
        for (long pid : pids) {
            if (pid <= 4 || pid == currentPid) {
                continue;
            }
            try {
                ProcessHandle.of(pid).ifPresent(ProcessHandle::destroyForcibly);
                Process p = new ProcessBuilder("taskkill", "/F", "/PID", String.valueOf(pid))
                        .redirectErrorStream(true)
                        .start();
                p.waitFor(2, TimeUnit.SECONDS);
            } catch (Exception ignored) {
              
            }
        }
    }

    private static Set<Long> findListeningPids(int port) {
        Set<Long> pids = new HashSet<>();
        String portToken = ":" + port;
        try {
            Process proc = new ProcessBuilder("cmd.exe", "/c", "netstat -ano")
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.startsWith("TCP") || !trimmed.contains("LISTENING")) {
                        continue;
                    }
                    String[] parts = trimmed.split("\\s+");
                    if (parts.length >= 5) {
                        String localAddr = parts[1];
                        if (localAddr.endsWith(portToken)) {
                            try {
                                pids.add(Long.parseLong(parts[parts.length - 1]));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            }
            proc.waitFor(2, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
        return pids;
    }
}
