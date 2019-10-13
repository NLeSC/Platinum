/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.channels.impl;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Slf4j
public class StderrWatcher implements Runnable {

    private final InputStream errorStream;

    public StderrWatcher(InputStream errs) {
        this.errorStream = errs;
        new Thread(this).start();
    }

    @Override
    public void run() {
        log.info(("starting stderr watcher..."));
        byte[] buffer = new byte[1024];
        boolean error = false;

        do {
            try {
                int numRead = errorStream.read(buffer);
                if (numRead < 0) {
                    break;
                }
                String errStr = new String(buffer, 0, numRead);
                System.err.printf("%s", errStr);
                Arrays.stream(errStr.split("\n")).forEach(err -> log.error("{}", err));
            } catch (IOException e) {
                error = true;
                e.printStackTrace();
            }
        } while (!error);
        log.info("stopped watching process");
    }
}
