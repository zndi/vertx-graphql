package com.exemple.starter.service.account;

public class Account {

  private String id;
  private String label;

  public Account(String id, String label) {
    this.id = id;
    this.label = label;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }
}
