package chatroom.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public final class Server {

    private static final int PORT = 3443;
    private final LinkedList<ClientHandler> clients = new LinkedList<>();

    public Server() {
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("The server is running!");
            while (true) {
                clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                    System.out.println("The server is stopped");
                    serverSocket.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void sendMessageToAllClients(String message) {
        for (ClientHandler client : clients) {
            client.sendMsg(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}