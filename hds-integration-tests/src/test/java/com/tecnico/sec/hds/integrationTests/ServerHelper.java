package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.server.app.Application;
import org.springframework.context.ConfigurableApplicationContext;

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
          String port = "808" + i;
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
}
