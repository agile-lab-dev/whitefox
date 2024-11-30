package io.whitefox.core;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;

public interface WhitefoxAuthorization {

  Boolean authorize(Share share, Principal principal);

  @Data
  @ApplicationScoped
  class WhitefoxSimpleAuthorization implements WhitefoxAuthorization {

    @Override
    public Boolean authorize(Share share, Principal principal) {
      return share.recipients().contains(principal) || share.owner().equals(principal);
    }
  }
}
