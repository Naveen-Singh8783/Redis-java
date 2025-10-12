import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

  // Shared map for all clients
  private static ConcurrentHashMap<String, ValueWithExpiry> map = new ConcurrentHashMap<>();
  private static String dbfilename;
  private static String dir;

  public static void main(String[] args) {
    System.out.println("Logs from your program will appear here!");

    //adding dir command command support and filename
    if(args.length>0 && args[0] !=null && args[2] != null && args[0].equals("--dir") && args[2].equals("--dbfilename")){
        dir = args[1];
        dbfilename = args[3];

    }

    ServerSocket serverSocket = null;
    int port = 6379;
    try {
      serverSocket = new ServerSocket(port);
      serverSocket.setReuseAddress(true);

      while (true) {
        Socket clientSocket = serverSocket.accept();
        Thread clientThread = new Thread(() -> { 
          try {
            processBuffer(clientSocket);
          } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
          }
        });
        clientThread.start();
      }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }

  private static void processBuffer(Socket clientSocket) {
    try (BufferedReader clientInput = new BufferedReader(
             new InputStreamReader(clientSocket.getInputStream()));
         BufferedWriter clientOutput = new BufferedWriter(
             new OutputStreamWriter(clientSocket.getOutputStream()));) {
      
      String line;
      while ((line = clientInput.readLine()) != null) {
        String[] parts = line.trim().split(" ");
        String command = parts[0].toUpperCase();

        switch (command) {
          case "PING":
            clientOutput.write("+PONG\r\n");
            clientOutput.flush();
            break;

          case "ECHO":
            if (parts.length > 1) {
              String msg = line.substring(5); // everything after "echo "
              clientOutput.write("+" + msg + "\r\n");
              clientOutput.flush();
            } else {
              clientOutput.write("-ERR wrong number of arguments for 'echo'\r\n");
              clientOutput.flush();
            }
            break;

          case "SET":
            if (parts.length >= 3) {
              String key = parts[1];
              String value = parts[2];
              long expiryTime = -1; //default: -1 means no expiry
              
              if(parts.length >= 5){
                String option = parts[3].toUpperCase();
                String expiryStr = parts[4];
                try {
                  if("EX".equals(option)){
                      long seconds = Long.parseLong(expiryStr);
                      expiryTime = System.currentTimeMillis() + (seconds * 100);
                  }else if("PX".equals(option)){
                    long millis = Long.parseLong(expiryStr);
                    expiryTime = System.currentTimeMillis() + millis;
                  }else{
                    clientOutput.write("-ERR syntax error\r\n");
                    clientOutput.flush();
                    break;
                  }
                } catch (NumberFormatException e) {
                  clientOutput.write("-ERR invalid expire time\r\n");
                  clientOutput.flush();
                  break;
                }
              }


              map.put(key, new ValueWithExpiry(value, expiryTime));
              clientOutput.write("+OK\r\n");
              clientOutput.flush();
            } else {
              clientOutput.write("-ERR wrong number of arguments for 'set'\r\n");
              clientOutput.flush();
            }
            break;

          case "GET":
            if (parts.length == 2) {
              String key = parts[1];
              ValueWithExpiry value = map.get(key);
              if (value == null || value.isExpired()) {
                map.remove(key);
                clientOutput.write("$-1\r\n");
              } else {
                clientOutput.write("$"+ value.getValue().length() + "\r\n" + value.getValue() + "\r\n"); 
              }
              clientOutput.flush();
            } else {
              clientOutput.write("-ERR wrong number of arguments for 'get'\r\n");
              clientOutput.flush();
            }
            break;

          case "CONFIG":
            String cmd = parts[1].toUpperCase();
            if(cmd.equals("GET")){
              String folderOrFile = parts[2];
              if(folderOrFile.equals("dir")){
                clientOutput.write(dir+"\r\n");
              }else if(folderOrFile.equals("dbfilename")){
                clientOutput.write(dbfilename+"\r\n");
              }else{
                clientOutput.write("-ERR No file or directory exist with this name\r\n");
                clientOutput.flush();
              }
            }else{
              clientOutput.write("-ERR unknown command\r\n");
              clientOutput.flush();
            }
            break;

          default:
            clientOutput.write("-ERR unknown command\r\n");
            clientOutput.flush();
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
