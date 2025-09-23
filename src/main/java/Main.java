import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");

    //  Uncomment this block to pass the first stage
    ServerSocket serverSocket = null;
    int port = 6379;
    try {
      serverSocket = new ServerSocket(port);
      // Since the tester restarts your program quite often, setting
      // SO_REUSEADDR ensures that we don't run into 'Address already in use'
      // errors
      serverSocket.setReuseAddress(true);
      // Wait for connection from client.
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
      String content;

      while ((content = clientInput.readLine()) != null) {
        if (content.equalsIgnoreCase("ping")) {
          clientOutput.write("+PONG\r\n");
          clientOutput.flush();
        } else if (content.equalsIgnoreCase("echo")) {
          String numBytes = clientInput.readLine();
          clientOutput.write(numBytes + "\r\n" + clientInput.readLine() +
                             "\r\n");
          clientOutput.flush();
        } else if (content.equalsIgnoreCase("set")){
          // Implement set commend here
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}