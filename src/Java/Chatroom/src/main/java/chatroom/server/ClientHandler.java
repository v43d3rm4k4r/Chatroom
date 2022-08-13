package chatroom.server;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public final class ClientHandler implements Runnable {

    private Server server;

    private PrintWriter outMessage;
    private Scanner     inMessage;

    private static final String HOST = "localhost";
    private static final int    PORT = 3443;

    private Socket     clientSocket = null;
    private static int clientsCount = 0;

    public ClientHandler(@NotNull Socket socket, Server server) {
        try {
            ++clientsCount;
            this.server = server;
            this.clientSocket = socket;
            this.outMessage = new PrintWriter(socket.getOutputStream());
            this.inMessage  = new Scanner(socket.getInputStream());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                server.sendMessageToAllClients("A new participant has entered the chat!");
                server.sendMessageToAllClients("Clients count: " + ++clientsCount);
                break;
            }

            while (true) {
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    if (clientMessage.equalsIgnoreCase("##session##end##")) {
                        break;
                    }
                    System.out.println(clientMessage);
                    server.sendMessageToAllClients(clientMessage);
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        } finally {
            this.close();
        }
    }

    public void sendMsg(String message) {
        try {
            outMessage.println(message);
            outMessage.flush();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void close() {
        server.removeClient(this);
        --clientsCount;
        server.sendMessageToAllClients("Clients count: " + clientsCount);
    }
}