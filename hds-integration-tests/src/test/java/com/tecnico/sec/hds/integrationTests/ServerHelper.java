package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.integrationTests.byzantineApp.ByzantineApplication;
import com.tecnico.sec.hds.server.app.Application;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.InetAddress;
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
          String port = "818" + i;
          System.setProperty("server.port", port);

          ConfigurableApplicationContext app = Application.runApplication();
          serversApps.add(app);

          return "http://localhost:" + port;
        })
        .collect(Collectors.toList());
  }

  public void stopServers() {
    serversApps.stream().parallel().forEach(ConfigurableApplicationContext::close);
  }

  public String startByzantineServer(int i) throws Exception{
    String port = "818" + i;
    System.setProperty("server.port", port);
    String ip = InetAddress.getLocalHost().getCanonicalHostName();
    ConfigurableApplicationContext app = ByzantineApplication.runApplication();
    serversApps.add(app);
    return "http://" + ip + ":" + port;
  }
}
