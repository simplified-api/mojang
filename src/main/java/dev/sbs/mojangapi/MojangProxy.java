package dev.sbs.minecraftapi.client.mojang;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.PrimitiveUtil;
import dev.sbs.api.util.StringUtil;
import dev.sbs.minecraftapi.client.mojang.exception.MojangApiException;
import dev.sbs.minecraftapi.client.mojang.profile.MojangProfile;
import dev.sbs.minecraftapi.client.mojang.request.MinecraftServicesRequest;
import dev.sbs.minecraftapi.client.mojang.request.MojangApiRequest;
import dev.sbs.minecraftapi.client.mojang.request.MojangSessionRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@NoArgsConstructor
public final class MojangProxy {

    // API: http://wiki.vg/Mojang_API

    // Skin Renderer
    // https://visage.surgeplay.com/index.html
    // https://github.com/unascribed-archive/Visage

    private final @NotNull ConcurrentList<MojangApiClient> apiClients = Concurrent.newList();
    private final @NotNull ConcurrentList<MojangSessionClient> sessionClients = Concurrent.newList();
    private final @NotNull ConcurrentList<MinecraftServicesClient> servicesClients = Concurrent.newList();
    @Getter private @NotNull Optional<Integer[]> inet6NetworkPrefix = Optional.empty();

    /**
     * Retrieves an instance of {@link MojangApiClient} to handle API communication.
     * <p>
     * This method ensures that there is always at least one default client present
     * and returns the first available client that is not rate-limited. If no such client exists,
     * a new client with a randomized IPv6 address is created and added to the clients pool.
     *
     * @return An instance of {@link MojangApiClient}, prioritized to avoid rate limitations.
     */
    public @NotNull MojangApiClient getApiClient() {
        // Add Default Client
        this.apiClients.addIf(this.apiClients::isEmpty, new MojangApiClient());

        return this.apiClients.stream()
            .filter(MojangClient::notRateLimited)
            .findFirst()
            .or(() -> Optional.of(new MojangApiClient(this.getRandomInet6Address())))
            .filter(this.apiClients::add)
            .orElse(this.apiClients.get(0));
    }

    public @NotNull MojangApiRequest getApiRequest() {
        return this.getApiClient().getRequest();
    }

    /**
     * Gets the {@link MojangProfile} for the given username.
     *
     * @param username Unique profile username (case-insensitive).
     */
    public @NotNull MojangProfile getMojangProfile(@NotNull String username) throws MojangApiException {
        return this.getMojangProfile(this.getApiRequest().getUniqueId(username).getUniqueId());
    }

    /**
     * Gets the {@link MojangProfile} for the given unique id.
     *
     * @param uniqueId Unique profile identifier.
     */
    public @NotNull MojangProfile getMojangProfile(@NotNull UUID uniqueId) throws MojangApiException {
        return new MojangProfile(this.getSessionRequest().getProperties(uniqueId));
    }

    /**
     * Generates a random {@link Inet6Address} by appending randomized groups to the configured IPv6 network prefix.
     * <p>
     * This method uses the existing IPv6 network prefix provided by {@link #getInet6NetworkPrefix()}
     * to construct a full IPv6 address. Random values are filled into any remaining groups
     * to complete the address. The final address is validated and returned as an {@link Inet6Address Optional&lt;Inet6Address>}.
     * <p>
     * If the network prefix is not available, an empty {@link Optional} is returned.
     *
     * @return An {@link Optional} containing a randomized {@link Inet6Address}, or an empty {@link Optional}
     *         if the network prefix is not defined.
     */
    private @NotNull Optional<Inet6Address> getRandomInet6Address() {
        return this.getInet6NetworkPrefix()
            .map(networkPrefix -> {
                String inet6NetworkPrefix = StringUtil.join(networkPrefix, ":");
                String inet6NetworkTail = StringUtil.repeat(String.format("%04x", getRandomInet6Group()), ":", 8 - networkPrefix.length);

                try {
                    return Inet6Address.getByName(inet6NetworkPrefix + inet6NetworkTail);
                } catch (UnknownHostException uhex) {
                    throw new RuntimeException(uhex);
                }
            })
            .map(Inet6Address.class::cast);
    }

    private static int getRandomInet6Group() {
        return ThreadLocalRandom.current().nextInt() & 0xFFFF;
    }

    /**
     * Retrieves an instance of {@link MinecraftServicesClient} to handle API communication.
     * <p>
     * This method ensures that there is always at least one default client present
     * and returns the first available client that is not rate-limited. If no such client exists,
     * a new client with a randomized IPv6 address is created and added to the clients pool.
     *
     * @return An instance of {@link MinecraftServicesClient}, prioritized to avoid rate limitations.
     */
    public @NotNull MinecraftServicesClient getServicesClient() {
        // Add Default Client
        this.servicesClients.addIf(this.servicesClients::isEmpty, new MinecraftServicesClient());

        return this.servicesClients.stream()
            .filter(MojangClient::notRateLimited)
            .findFirst()
            .or(() -> Optional.of(new MinecraftServicesClient(this.getRandomInet6Address())))
            .filter(this.servicesClients::add)
            .orElse(this.servicesClients.get(0));
    }

    /**
     * Retrieves an instance of {@link MojangSessionClient} to handle API communication.
     * <p>
     * This method ensures that there is always at least one default client present
     * and returns the first available client that is not rate-limited. If no such client exists,
     * a new client with a randomized IPv6 address is created and added to the clients pool.
     *
     * @return An instance of {@link MojangSessionClient}, prioritized to avoid rate limitations.
     */
    public @NotNull MojangSessionClient getSessionClient() {
        // Add Default Client
        this.sessionClients.addIf(this.sessionClients::isEmpty, new MojangSessionClient());

        return this.sessionClients.stream()
            .filter(MojangClient::notRateLimited)
            .findFirst()
            .or(() -> Optional.of(new MojangSessionClient(this.getRandomInet6Address())))
            .filter(this.sessionClients::add)
            .orElse(this.sessionClients.get(0));
    }

    public @NotNull MinecraftServicesRequest getServicesRequest() {
        return this.getServicesClient().getRequest();
    }

    public @NotNull MojangSessionRequest getSessionRequest() {
        return this.getSessionClient().getRequest();
    }

    /**
     * Set your assigned IPv6 network prefix to cycle through for web requests.
     * <br><br>
     * <h5>Create Hurricane Election IPv6 Tunnel</h5>
     * <ol>
     *     <li>Go to <a href="https://tunnelbroker.net/">TunnelBroker</a></li>
     *     <li>Create an account or login</li>
     *     <li>Click on Create Regular Tunnel</li>
     *     <ul>
     *         <li>Enter ipv4 address of your server</li>
     *         <ul>
     *             <li>If it gives an error, use the pingable IP of nginx.com</li>
     *         </ul>
     *         <li>Select an origin city for your tunnel</li>
     *         <li>Click Create</li>
     *     </ul>
     *     <li>Click on your tunnel name</li>
     *     <ul>
     *         <li>If you entered the nginx.com IP, change it to the ipv4 address of your server</li>
     *     </ul>
     *     <li>Click on Generate /48</li>
     * </ol>
     *
     * <h5>Create Server IPv6 Tunnel (Requires Root Access)</h5>
     * <pre><code>
     * modprobe ipv6
     * modprobe sit
     * ip tunnel add he-ipv6 mode sit remote SERVER_IPV4_ADDRESS local CLIENT_IPV4_ADDRESS ttl 255
     * ip link set he-ipv6 up
     * ip link set he-ipv6 mtu 1480
     * </code></pre>
     *
     * <h5>Setup Routing</h5>
     * <pre><code>
     * ip addr add ROUTED_48::2/48 dev he-ipv6
     * ip -6 route add local ROUTED_48::/48 dev lo
     * echo "100 he" >> /etc/iproute2/rt_tables
     * ip -6 route add default dev he-ipv6 table he
     * ip -6 rule add pref 1000 from ROUTED_48::/48 lookup he
     * </code></pre>
     *
     * <h5>Enable Non-Local IPv6 Binding</h5>
     * <pre><code>
     * echo "net.ipv6.ip_nonlocal_bind = 1" > /etc/sysctl.d/99-nonlocal-bind.conf
     * sysctl -p /etc/sysctl.d/99-nonlocal-bind.conf
     * </code></pre>
     *
     * <h5>Enable IPv6 Forwarding</h5>
     * <pre><code>
     * echo "net.ipv6.conf.all.forwarding = 1" > /etc/sysctl.d/99-ipv6-forwarding.conf
     * sysctl -p /etc/sysctl.d/99-ipv6-forwarding.conf
     * </code></pre>
     *
     * <h5>Enable TCP Optimizations</h5>
     * <pre><code>
     * echo "net.ipv4.tcp_fastopen = 3" >> /etc/sysctl.d/99-tcp-optimizations.conf
     * echo "net.core.default_qdisc = fq" >> /etc/sysctl.d/99-tcp-optimizations.conf
     * echo "net.ipv4.tcp_congestion_control = bbr" >> /etc/sysctl.d/99-tcp-optimizations.conf
     * echo "net.ipv4.tcp_slow_start_after_idle = 0" >> /etc/sysctl.d/99-tcp-optimizations.conf
     * sysctl -p /etc/sysctl.d/99-tcp-optimizations.conf
     * </code></pre>
     *
     * @param networkPrefix Your IPv6 Network Prefix
     */
    public void setInet6NetworkPrefix(int[] networkPrefix) {
        this.inet6NetworkPrefix = Optional.ofNullable(PrimitiveUtil.wrap(networkPrefix));
    }

}
