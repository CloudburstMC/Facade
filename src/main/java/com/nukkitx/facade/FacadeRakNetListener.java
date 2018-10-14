package com.nukkitx.facade;

import com.nukkitx.network.raknet.RakNetServerEventListener;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class FacadeRakNetListener implements RakNetServerEventListener {
    private final Advertisement advertisement;

    @Override
    public Action onConnectionRequest(InetSocketAddress address, int protocolVersion) {
        return Action.NO_INCOMING_CONNECTIONS;
    }

    @Override
    public Advertisement onQuery(InetSocketAddress address) {
        return advertisement;
    }
}
