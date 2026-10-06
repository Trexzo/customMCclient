package dev.trexzo.custommc.launcher.process;

import java.io.IOException;

interface ProcessStarter {
    Process start(ProcessBuilder builder)
            throws IOException;
}
