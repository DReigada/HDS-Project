package com.tecnico.sec.hds.integrationTests.util;

import com.tecnico.sec.hds.app.ServerProxyApp;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerHelper {
  private final List<ConfigurableApplicationContext> serversApps;

  public ServerHelper() {
    serversApps = new LinkedList<>();
  }


  public void writeConfig(int servers){
    Path file = Paths.get("Tests.conf");
    try {
      Files.createFile(file);

    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("This should never happen");
    }
    IntStream.range(0, servers).forEach( i -> {
      String url = "http://localhost:" + "818" + i + "\n";
      try {
        Files.write(file, url.getBytes(), StandardOpenOption.APPEND);
      } catch (IOException e) {
        e.printStackTrace();
        System.err.println("This should never happen");
      }
    });

    System.setProperty("servers.urls.file", "Tests.conf");
  }

  public void deleteConfig(){
    Path file = Paths.get("Tests.conf");

      file.toFile().delete();
      //Files.delete(file);

    System.clearProperty("servers.urls.file");
  }

  public List<String> startServers(int start, int servers,ServerTypeWrapper.ServerType type) {
    return IntStream.range(start, servers)
        .mapToObj(i -> {
          String port = "818" + i;
          System.setProperty("server.port", port);

          ConfigurableApplicationContext app = ServerProxyApp.runApplication(type);
          serversApps.add(app);

          return "http://localhost:" + port;
        })
        .collect(Collectors.toList());
  }

  public void stopServers() {
    serversApps.stream().parallel().forEach(ConfigurableApplicationContext::close);
    ServerTypeWrapper.cleanServers();
  }
}
