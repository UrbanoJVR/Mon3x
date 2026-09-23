package com.urbanojvr.monex.desktop;

import java.io.IOException;
import java.net.ServerSocket;

final class PortFinder {

    private PortFinder() {
    }

    static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }
}
