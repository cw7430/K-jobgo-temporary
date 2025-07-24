/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.core.io.support.SpringFactoriesLoader
 *  org.springframework.util.ClassUtils
 */
package org.springframework.boot.loader.launch;

import java.util.List;
import org.springframework.boot.loader.jarmode.JarMode;
import org.springframework.boot.loader.jarmode.JarModeErrorException;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;

final class JarModeRunner {
    static final String DISABLE_SYSTEM_EXIT = JarModeRunner.class.getName() + ".DISABLE_SYSTEM_EXIT";
    static final String SUPPRESSED_SYSTEM_EXIT_CODE = JarModeRunner.class.getName() + ".SUPPRESSED_SYSTEM_EXIT_CODE";

    private JarModeRunner() {
    }

    static void main(String[] args) {
        String mode = System.getProperty("jarmode");
        boolean disableSystemExit = Boolean.getBoolean(DISABLE_SYSTEM_EXIT);
        try {
            JarModeRunner.runJarMode(mode, args);
            if (disableSystemExit) {
                System.setProperty(SUPPRESSED_SYSTEM_EXIT_CODE, "0");
            }
        }
        catch (Throwable ex) {
            JarModeRunner.printError(ex);
            if (disableSystemExit) {
                System.setProperty(SUPPRESSED_SYSTEM_EXIT_CODE, "1");
                return;
            }
            System.exit(1);
        }
    }

    private static void runJarMode(String mode, String[] args) {
        List candidates = SpringFactoriesLoader.loadFactories(JarMode.class, (ClassLoader)ClassUtils.getDefaultClassLoader());
        for (JarMode candidate : candidates) {
            if (!candidate.accepts(mode)) continue;
            candidate.run(mode, args);
            return;
        }
        throw new JarModeErrorException("Unsupported jarmode '" + mode + "'");
    }

    private static void printError(Throwable ex) {
        if (ex instanceof JarModeErrorException) {
            String message = ex.getMessage();
            System.err.println("Error: " + message);
            System.err.println();
            return;
        }
        ex.printStackTrace();
    }
}

