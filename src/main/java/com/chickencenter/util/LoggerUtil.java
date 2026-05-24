package com.chickencenter.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {

    private static final String LOG_FILE = "log.txt";
    private static final long MAX_LOG_SIZE = 5L * 1024 * 1024;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
    private static final Object LOCK = new Object();
    private static boolean initialized = false;

    public static void initialize() {
        synchronized (LOCK) {
            if (initialized) return;
            try {
                createLogFileIfNeeded();
                rotateLogsIfNeeded();
                logInfo("SYSTEM", "Application started");
                installGlobalExceptionHandler();
                initialized = true;
            } catch (Exception e) {
                System.err.println("[LoggerUtil] Failed to initialize: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void logInfo(String module, String message) {
        writeLog("INFO", module, message, null);
    }

    public static void logShutdown() {
        logInfo("SYSTEM", "Application shutting down");
    }

    private static void writeLog(String level, String module, String message, Exception e) {
        synchronized (LOCK) {
            rotateLogsIfNeeded();
            try (FileWriter fw = new FileWriter(LOG_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter pw = new PrintWriter(bw)) {

                pw.println("--------------------------------------------------");
                pw.println("[" + LocalDateTime.now().format(DT_FMT) + "]");
                pw.println("LEVEL: " + level);
                pw.println("MODULE: " + (module != null ? module : "Unknown"));
                pw.println("MESSAGE: " + (message != null ? message : "No message"));

                if (e != null) {
                    pw.println("EXCEPTION: " + e.getClass().getName());
                    pw.println("STACKTRACE:");
                    e.printStackTrace(pw);
                }

                pw.println("--------------------------------------------------");
                pw.flush();
            } catch (IOException ex) {
                System.err.println("[LoggerUtil] Failed to write log: " + ex.getMessage());
            }
        }
    }

    private static void createLogFileIfNeeded() {
        try {
            Path path = Paths.get(LOG_FILE);
            if (Files.notExists(path)) {
                Files.createFile(path);
                System.out.println("[LoggerUtil] Created log file: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("[LoggerUtil] Failed to create log file: " + e.getMessage());
        }
    }

    private static void rotateLogsIfNeeded() {
        try {
            Path path = Paths.get(LOG_FILE);
            if (Files.exists(path) && Files.size(path) > MAX_LOG_SIZE) {
                String backupName = "log_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                Files.move(path, path.resolveSibling(backupName), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[LoggerUtil] Log rotated to: " + backupName);
            }
        } catch (IOException e) {
            System.err.println("[LoggerUtil] Failed to rotate logs: " + e.getMessage());
        }
    }

    private static void installGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String module = "Global";
            String msg = throwable.getMessage() != null ? throwable.getMessage() : "Uncaught " + throwable.getClass().getName();

            System.err.println("[GlobalException] " + msg);
            throwable.printStackTrace();

            writeLog("UNCAUGHT", module, msg,
                    throwable instanceof Exception ? (Exception) throwable
                            : new RuntimeException("Uncaught: " + throwable.getClass().getName(), throwable));
        });
    }
}
