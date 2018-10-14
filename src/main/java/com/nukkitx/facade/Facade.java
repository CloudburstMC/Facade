package com.nukkitx.facade;

import com.nukkitx.network.raknet.RakNetServer;
import com.nukkitx.network.raknet.RakNetServerEventListener;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
@Getter
public class Facade {
    private static final Path workingDir = Paths.get("");

    private final FacadeSessionManager sessionManager = new FacadeSessionManager();
    private final Lock loop = new ReentrantLock();

    public static void main(String[] args) {
        Facade facade = new Facade();

        try {
            facade.boot();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void boot() throws IOException {
        log.info("Facade is starting up...");

        Path propertiesPath = workingDir.resolve("facade.properties");

        if (Files.notExists(propertiesPath) || !Files.isRegularFile(propertiesPath)) {
            Files.deleteIfExists(propertiesPath);
            Files.copy(Facade.class.getResourceAsStream("/facade.properties"), propertiesPath, StandardCopyOption.REPLACE_EXISTING);
        }

        Properties properties = new Properties();
        properties.load(Files.newBufferedReader(propertiesPath));

        RakNetServerEventListener.Advertisement advertisement = new RakNetServerEventListener.Advertisement(
                "MCPE",
                properties.getProperty("motd", "Facade"),
                Integer.parseInt(properties.getProperty("protocol-version", "123")),
                properties.getProperty("version", "1.2.3"),
                Integer.parseInt(properties.getProperty("player-count", "0")),
                Integer.parseInt(properties.getProperty("max-player-count", "0")),
                properties.getProperty("sub-motd", "https://github.com/NukkitX/Facade"),
                properties.getProperty("gamemode", "SMP")
        );

        String host = properties.getProperty("host", "0.0.0.0");
        int port = Integer.parseInt(properties.getProperty("port", "19132"));
        int serverId = Integer.parseInt(properties.getProperty("server-id", "0"));

        RakNetServer<FacadeSession> rakNetServer = RakNetServer.<FacadeSession>builder()
                .address(host, port)
                .id(serverId)
                .eventListener(new FacadeRakNetListener(advertisement))
                .sessionFactory(rakNetSession -> new FacadeSession(this, rakNetSession))
                .sessionManager(sessionManager)
                .build();

        rakNetServer.bind();

        log.info("Facade has successfully started!");

        while (true) {
            try {
                loop.lock();
                loop.newCondition().await(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                break;
            } finally {
                loop.unlock();
            }
        }

        rakNetServer.close();
    }
}
