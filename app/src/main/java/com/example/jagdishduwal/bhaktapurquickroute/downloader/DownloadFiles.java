package com.example.jagdishduwal.bhaktapurquickroute.downloader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;


public class DownloadFiles {
    private static DownloadFiles downloadFiles;

    private DownloadFiles() {
    }

    public static DownloadFiles getDownloader() {
        if (downloadFiles == null) {
            downloadFiles = new DownloadFiles();
        }
        return downloadFiles;
    }
    
  /**
   * @param mapUrl
   * @return json string
   */
  public String downloadTextfile(String textFileUrl)
  {
    StringBuilder json = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(textFileUrl).openStream())))
    {
      String lineUrl;
      while ((lineUrl = in.readLine()) != null)
      {
        json.append(lineUrl);
      }
      in.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return json.toString();
  }

}
