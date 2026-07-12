package com.aguardientes.azarcafetero.parques_service.infrastructure.backplane;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion del backplane de sockets (Redis pub/sub, Cluster #2).
 *
 * Redis SEPARADO al que se use para cache/estado: este cluster lo COMPARTEN
 * todos los MS con sockets (Location, Notification, Communication, Parques,
 * Board) y solo transporta broadcasts efimeros entre pods. Cada MS publica y
 * se suscribe unicamente a SU canal, namespaced por servicio:
 * {@code patricia:backplane:<servicio>}.
 */
@Configuration
@ConfigurationProperties(prefix = "backplane")
public class BackplaneProperties {

    /** Apagado por defecto: un solo pod no necesita backplane (dev local). */
    private boolean enabled = false;

    /** Canal propio de este MS dentro del cluster compartido. */
    private String channel = "patricia:backplane:parques";

    private Redis redis = new Redis();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public Redis getRedis() { return redis; }
    public void setRedis(Redis redis) { this.redis = redis; }

    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password = "";

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
