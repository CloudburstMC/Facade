package com.nukkitx.facade;

import com.nukkitx.network.NetworkSession;
import com.nukkitx.network.raknet.session.RakNetSession;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;

@Log4j2
public class FacadeSession implements NetworkSession<RakNetSession> {
    private final Facade facade;
    private final RakNetSession rakNetSession;

    public FacadeSession(Facade facade, RakNetSession rakNetSession) {
        this.facade = facade;
        this.rakNetSession = rakNetSession;

        log.info("{} attempted to connect", rakNetSession.getRemoteAddress().map(InetSocketAddress::toString).orElse("UNKNOWN"));
    }

    @Override
    public void disconnect() {
        close();
    }

    @Override
    public RakNetSession getConnection() {
        return rakNetSession;
    }

    @Override
    public void onTick() {
        if (rakNetSession.isClosed()) {
            close();
        }
        rakNetSession.onTick();
    }

    public void close() {
        facade.getSessionManager().remove(this);
    }
}
