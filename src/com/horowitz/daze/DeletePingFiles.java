package com.horowitz.daze;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeletePingFiles {

  /**
   * @param args
   */
  public static void main(String[] args) {
    int hours = 24;
    String prefix = "diggy ";
    File f = new File("ping");
    File[] files = f.listFiles();
    List<File> targetFiles = new ArrayList<File>(6);
    int cnt = 0;
    for (File file : files) {
      if (!file.isDirectory() && file.getName().startsWith(prefix)) {
        if (hours < 0 || (hours > 0 && System.currentTimeMillis() - file.lastModified() > hours * 60 * 60000)) {
          targetFiles.add(file);
          cnt++;
        }
      }
    }
    System.err.println("" + cnt);
    for (int i = 0; i < targetFiles.size(); i++) {
      File fd = targetFiles.get(i);
      fd.delete();
    }
    System.err.println("DONE");

  }

}
