package io.delta.sharing.encoders;

import java.io.Serializable;

public class PageToken implements Serializable {
  String id;

  public PageToken(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
