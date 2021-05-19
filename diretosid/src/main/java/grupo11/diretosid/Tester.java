package grupo11.diretosid;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Tester {
   public static void main(String[] args) {
      try {
         URL url = new URL("http://www.google.com");
         URLConnection connection = url.openConnection();
         connection.connect();
         System.out.println("Internet is connected");
      } catch (IOException e) {
         System.out.println("Internet is not connected");
      }
   }
}