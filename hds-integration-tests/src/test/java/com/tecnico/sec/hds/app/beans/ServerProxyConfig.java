package com.tecnico.sec.hds.app.beans;

import com.tecnico.sec.hds.ServersWrapper;
import io.swagger.client.ApiClient;
import io.swagger.client.api.DefaultApi;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;


@Configuration
public class ServerProxyConfig {
  private static final Logger logger = LoggerFactory.getLogger(ServerProxyConfig.class);


  @Bean
  public DefaultApi server(){
    return getServer();
  }

  public DefaultApi getServer(){
    String url = "";
    ApiClient client = new ApiClient().setBasePath(url);
    DefaultApi server = new DefaultApi(client);
    return server;
  }

  @Bean
  public ServersWrapper serversWrapper() {
    return createServersWrapper();
  }

  private ServersWrapper createServersWrapper() {
    try {
      boolean useLocalhost = Boolean.valueOf(System.getProperty("hds.coin.crypto.useLocalhost", "true"));

      System.out.println("hds.coin.crypto.useLocalhost = " + useLocalhost);

      String ip = InetAddress.getLocalHost().getCanonicalHostName().replace(".", "_");
      String hostName = useLocalhost ? "localhost" : ip;
      String port = System.getProperty("server.port", "8080");
      String fileName = "bank" + hostName + "_" + port;

      logger.info("Creating keystore on file: " + fileName);

      return new ServersWrapper(fileName, fileName);
    } catch (GeneralSecurityException | OperatorCreationException | IOException e) {
      e.printStackTrace();
      System.exit(1);
      return null;
    }
  }

}
