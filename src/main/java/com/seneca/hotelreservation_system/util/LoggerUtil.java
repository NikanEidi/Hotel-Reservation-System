package com.seneca.hotelreservation_system.util;

import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {
    private static final Logger logger = Logger.getLogger(LoggerUtil.class.getName());

    static {
        try {
            // Pattern: system_logs.%g.log | Limit: 1MB (1024*1024) | Count: 10 files | Append: true
            FileHandler fileHandler = new FileHandler("system_logs.%g.log", 1024 * 1024, 10, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Optional: Prevent logs from also showing in console to keep it clean
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize logger", e);
        }
    }

    public static void logInfo(String message) {
        logger.info(message);
    }

    public static void logWarning(String message) {
        logger.warning(message);
    }

    public static void logAction(String actor, String action, String entityType, String entityId, String message) {
        String formattedMessage = String.format("Actor: %s | Action: %s | Entity: %s (%s) | Message: %s",
                actor, action, entityType, entityId, message);
        logger.info(formattedMessage);
    }

    public static void logError(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
}