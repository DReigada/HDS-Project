package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.server.app.Application;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerHelper {

  public static List<String> startServers(int servers) {
    return IntStream.range(0, servers)
        .mapToObj(i -> {
          System.setProperty("server.port", "808" + i);
          System.setProperty("spring.config.name", "808" + i);
          Application.main(new String[]{});
          try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            return "http://" + ip + ":808" + i;
          } catch (UnknownHostException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toList());
  }
}
