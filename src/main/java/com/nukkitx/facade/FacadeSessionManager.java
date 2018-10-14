package com.nukkitx.facade;

import com.nukkitx.network.SessionManager;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

public class FacadeSessionManager implements SessionManager<FacadeSession> {
    private static final int SESSIONS_PER_THREAD = 50;
    private final ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentMap<InetSocketAddress, FacadeSession> sessions = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor sessionTicker = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(), new DefaultThreadFactory("Session Ticker - #%d", true));

    public FacadeSessionManager() {
        ticker.schedule(this::onTick, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean add(InetSocketAddress address, FacadeSession session) {
        boolean added = sessions.putIfAbsent(address, session) == null;
        if (added) {
            adjustPoolSize();
        }
        return added;
    }

    @Override
    public boolean remove(FacadeSession session) {
        boolean removed = sessions.values().remove(session);
        if (removed) {
            adjustPoolSize();
        }
        return removed;
    }

    @Override
    public FacadeSession get(InetSocketAddress address) {
        return sessions.get(address);
    }

    @Override
    public Collection<FacadeSession> all() {
        return new ArrayList<>(sessions.values());
    }

    @Override
    public int getCount() {
        return sessions.size();
    }

    private void adjustPoolSize() {
        int threads = sessions.size() / SESSIONS_PER_THREAD;
        final int processors = Runtime.getRuntime().availableProcessors();
        if (threads < 1) {
            threads = 1;
        }
        if (threads > processors) {
            threads = processors;
        }
        if (sessionTicker.getMaximumPoolSize() != threads) {
            sessionTicker.setMaximumPoolSize(threads);
        }
    }

    public void onTick() {
        for (FacadeSession session : sessions.values()) {
            sessionTicker.execute(session::onTick);
        }
    }
}
