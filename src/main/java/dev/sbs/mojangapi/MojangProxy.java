package dev.sbs.minecraftapi.client.mojang;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.StringUtil;
import dev.sbs.minecraftapi.client.mojang.exception.MojangApiException;
import dev.sbs.minecraftapi.client.mojang.request.MojangEndpoint;
import dev.sbs.minecraftapi.client.mojang.response.MojangProfile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NoArgsConstructor
public final class MojangProxy {

    // API: http://wiki.vg/Mojang_API

    // Skin Renderer
    // https://visage.surgeplay.com/index.html
    // https://github.com/unascribed-archive/Visage

    private final @NotNull ConcurrentList<MojangClient> clients = Concurrent.newList();
    @Getter private @NotNull Optional<Integer[]> inet6NetworkPrefix = Optional.empty();

    /**
     * Retrieves an instance of {@link MojangClient} to handle API communication.
     *
     * <p>Ensures at least one client is present in the pool. When an IPv6 network prefix is
     * configured, every client - including the initial default - uses a randomized IPv6 source
     * address. Returns the first available client that is not rate-limited. If all clients are
     * rate-limited, a new client with a fresh randomized IPv6 address is created and added to
     * the pool.
     *
     * @return an instance of {@link MojangClient}, prioritized to avoid rate limitations
     */
    public @NotNull MojangClient getApiClient() {
        // Add Default Client
        this.clients.addIf(this.clients::isEmpty, new MojangClient(this.getRandomInet6Address()));

        return this.clients.stream()
            .filter(client -> client.notRateLimited(MojangClient.Domain.MINECRAFT_SERVICES))
            .findFirst()
            .or(() -> Optional.of(new MojangClient(this.getRandomInet6Address())))
            .filter(this.clients::add)
            .orElse(this.clients.getFirst());
    }

    public @NotNull MojangEndpoint getEndpoint() {
        return this.getApiClient().getEndpoint();
    }

    /**
     * Gets the {@link MojangProfile} for the given username.
     *
     * @param username Unique profile username (case-insensitive).
     */
    public @NotNull MojangProfile getMojangProfile(@NotNull String username) throws MojangApiException {
        return this.getMojangProfile(this.getEndpoint().getPlayer(username).getUniqueId());
    }

    /**
     * Gets the {@link MojangProfile} for the given unique id.
     *
     * @param uniqueId Unique profile identifier.
     */
    public @NotNull MojangProfile getMojangProfile(@NotNull UUID uniqueId) throws MojangApiException {
        return new MojangProfile(this.getEndpoint().getProperties(uniqueId));
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
    public @NotNull Optional<Inet6Address> getRandomInet6Address() {
        return this.getInet6NetworkPrefix()
            .map(networkPrefix -> {
                String prefix = Arrays.stream(networkPrefix)
                    .map(group -> String.format("%04x", group))
                    .collect(Collectors.joining(":"));
                String tail = IntStream.range(0, 8 - networkPrefix.length)
                    .mapToObj(i -> String.format("%04x", getRandomInet6Group()))
                    .collect(Collectors.joining(":"));

                try {
                    return Inet6Address.getByName(prefix + ":" + tail);
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
     * Sets the IPv6 network prefix used to generate randomized source addresses for web requests.
     *
     * <p>Accepts a standard CIDR notation string (e.g., {@code "2000:444:ffff::/48"}).
     * The prefix length suffix is stripped, and the address portion is parsed into groups.
     * Trailing empty groups from {@code "::"} shorthand are excluded.
     *
     * <h5>Create Hurricane Electric IPv6 Tunnel</h5>
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
     * <h5>Variables</h5>
     * <pre><code>
     * SERVER_IPV4 = Server IPv4 Address
     * CLIENT_IPV4 = Client IPv4 Address
     * CLIENT_IPV6 = Client IPv6 Address
     * ROUTED_48   = Routed /48 prefix
     * </code></pre>
     *
     * <h5>Create Routing Table</h5>
     * <pre><code>
     * grep -q '^100 he' /etc/iproute2/rt_tables || echo "100 he" >> /etc/iproute2/rt_tables
     * </code></pre>
     *
     * <h5>Enable IPv6 Non-Local Binding & Forwarding and TCP Optimizations</h5>
     * <pre><code>
     * cat > /etc/sysctl.d/99-he-tunnel.conf << 'EOF'
     * # Enable nonlocal bind
     * net.ipv6.ip_nonlocal_bind = 1
     *
     * # Enable ipv6 forwarding
     * net.ipv6.conf.all.forwarding = 1
     *
     * # Enable tcp optimizations
     * net.ipv4.tcp_fastopen = 3
     * net.core.default_qdisc = fq
     * net.ipv4.tcp_congestion_control = bbr
     * net.ipv4.tcp_slow_start_after_idle = 0
     * EOF
     * sysctl -p /etc/sysctl.d/99-he-tunnel.conf
     * </code></pre>
     *
     * <h5>Enable Non-Local IPv6 Binding</h5>
     * <pre><code>
     * cat > /etc/systemd/system/he-ipv6.service << 'EOF'
     * [Unit]
     * Description=Hurricane Electric IPv6 Tunnel
     * After=network-online.target
     * Wants=network-online.target
     *
     * [Service]
     * Type=oneshot
     * RemainAfterExit=yes
     *
     * ExecStart=/usr/sbin/modprobe ipv6
     * ExecStart=/usr/sbin/modprobe sit
     * ExecStart=/usr/sbin/ip tunnel add he-ipv6 mode sit remote SERVER_IPV4 local CLIENT_IPV4 ttl 255
     * ExecStart=/usr/sbin/ip link set he-ipv6 up
     * ExecStart=/usr/sbin/ip link set he-ipv6 mtu 1480
     * ExecStart=/usr/sbin/ip -6 addr add CLIENT_IPV6 dev he-ipv6
     * ExecStart=/usr/sbin/ip -6 addr add ROUTED_48::2/48 dev he-ipv6
     * ExecStart=/usr/sbin/ip -6 route add local ROUTED_48::/48 dev lo
     * ExecStart=/usr/sbin/ip -6 route add default dev he-ipv6 table he
     * ExecStart=/usr/sbin/ip -6 rule add pref 1000 from ROUTED_48::/48 lookup he
     *
     * ExecStop=/usr/sbin/ip -6 rule del pref 1000 from ROUTED_48::/48 lookup he
     * ExecStop=/usr/sbin/ip tunnel del he-ipv6
     *
     * [Install]
     * WantedBy=multi-user.target
     * EOF
     * </code></pre>
     *
     * <h5>Launch Service</h5>
     * <pre><code>
     * systemctl daemon-reload
     * systemctl enable he-ipv6
     * systemctl start he-ipv6
     * </code></pre>
     *
     * <h5>JVM Requirement</h5>
     * <p>The JVM must be started with {@code -Djava.net.preferIPv6Addresses=true}.
     * Without this, Java resolves hostnames to IPv4 addresses first, and an IPv6-bound
     * socket cannot connect to an IPv4 destination ({@code Network unreachable}).
     *
     * @param cidr an IPv6 network prefix in CIDR notation (e.g., {@code "2000:444:33ff::/48"})
     */
    public void setInet6NetworkPrefix(@NotNull String cidr) {
        String address = cidr.split("/")[0];
        Integer[] groups = Arrays.stream(address.split(":"))
            .filter(StringUtil::isNotEmpty)
            .map(group -> Integer.parseInt(group, 16))
            .toArray(Integer[]::new);
        this.inet6NetworkPrefix = Optional.of(groups);
    }

}
