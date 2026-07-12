package com.tjtechy.security_webflux.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "rsa")
public class RsaKeyProperties {
  private Resource publicKey;
  private Resource privateKey;

  public Resource getPublicKey(){
    return publicKey;
  }

  public void setPublicKey(Resource publicKey){
    this.publicKey = publicKey;
  }

  public Resource getPrivateKey(){
    return privateKey;
  }

  public void setPrivateKey(Resource privateKey){
    this.privateKey = privateKey;
  }
}
