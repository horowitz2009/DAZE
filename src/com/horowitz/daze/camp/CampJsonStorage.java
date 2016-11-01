package com.horowitz.daze.camp;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;

import com.google.gson.*;

public class CampJsonStorage {
  private String campPath = "storage/camp";
  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public List<Option> loadCaravanOptions() throws IOException {
    return loadOptions("caravanOptions");
  }

  public List<Option> loadKitchenOptions() throws IOException {
    return loadOptions("kitchenOptions");
  }

  public List<Option> loadFoundryOptions() throws IOException {
    return loadOptions("foundryOptions");
  }

  public List<Option> loadOptions(String filename) throws IOException {

    String json = FileUtils.readFileToString(new File(campPath + "/" + filename + ".json"));

    Option[] options = gson.fromJson(json, Option[].class);

    return new ArrayList<Option>(Arrays.asList(options));
  }

  public String getCampPath() {
    return campPath;
  }

}
