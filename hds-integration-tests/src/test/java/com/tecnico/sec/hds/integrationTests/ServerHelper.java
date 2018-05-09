package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.server.app.Application;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerHelper {
  private final List<ConfigurableApplicationContext> serversApps;

  public ServerHelper() {
    serversApps = new LinkedList<>();
  }

  public List<String> startServers(int servers) {
    return IntStream.range(0, servers)
        .mapToObj(i -> {
          try {
            String port = "808" + i;
            System.setProperty("server.port", port);
            String ip = InetAddress.getLocalHost().getCanonicalHostName();

            ConfigurableApplicationContext app = Application.runApplication();
            serversApps.add(app);

            return "http://" + ip + ":" + port;
          } catch (UnknownHostException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toList());
  }

  public void stopServers() {
    serversApps.stream().parallel().forEach(ConfigurableApplicationContext::close);
  }

  public String startByzantineServer(int i) throws Exception {
    String port = "808" + i;
    System.setProperty("server.port", port);
    String ip = InetAddress.getLocalHost().getCanonicalHostName();
    ConfigurableApplicationContext app = Application.runApplication();
    app.getEnvironment().addActiveProfile("ByzantineReliableBroadCast");
    serversApps.add(app);
    return "http://" + ip + ":" + port;
  }
}
