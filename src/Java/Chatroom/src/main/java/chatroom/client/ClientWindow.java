package chatroom.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public final class ClientWindow extends JFrame {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT    = 3443;

    private Socket clientSocket;

    private Scanner     inMessage;
    private PrintWriter outMessage;

    private final JTextField jtfMessage;
    private final JTextField jtfName;
    private final JTextArea jtaTextAreaMessage;

    private String clientName;

    public ClientWindow() {
        try {
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            inMessage    = new Scanner(clientSocket.getInputStream());
            outMessage   = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        setBounds(600, 300, 600, 500);
        setTitle("Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        jtaTextAreaMessage = new JTextArea();
        jtaTextAreaMessage.setEditable(false);
        jtaTextAreaMessage.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jtaTextAreaMessage);
        add(jsp, BorderLayout.CENTER);

        JLabel jlNumberOfClients = new JLabel("Clients count: ");
        add(jlNumberOfClients, BorderLayout.NORTH);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);

        JButton jbSendMessage = new JButton("Send");
        bottomPanel.add(jbSendMessage, BorderLayout.EAST);
        jtfMessage = new JTextField("Enter your message: ");
        bottomPanel.add(jtfMessage, BorderLayout.CENTER);
        jtfName = new JTextField("Enter your name: ");
        bottomPanel.add(jtfName, BorderLayout.WEST);

        jbSendMessage.addActionListener(event -> {
            if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty()) {
                clientName = jtfName.getText();
                sendMsg();
                jtfMessage.grabFocus();
            }
        });

        jtfMessage.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                jtfMessage.setText("");
            }
        });

        jtfName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                jtfName.setText("");
            }
        });

        // Starting work with server async
        new Thread(() -> {
            try {
                while (true) {
                    if (inMessage.hasNext()) {
                        String inMes = inMessage.nextLine();
                        String clientsInChat = "Clients count: ";
                        if (inMes.indexOf(clientsInChat) == 0) {
                            jlNumberOfClients.setText(inMes);
                        } else {
                            jtaTextAreaMessage.append(inMes + '\n');
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }).start();

        // On exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                super.windowClosing(event);
                try {
                    if (!clientName.isEmpty() && !clientName.equals("Enter your name: ")) {
                        outMessage.println(clientName + " exit the chat!");
                    } else {
                        outMessage.println("The participant left the chat without introducing himself");
                    }
                    // Sending a service message, which is a sign that the client has left the chat
                    outMessage.println("##session##end##");
                    outMessage.flush();
                    outMessage.close();
                    inMessage.close();
                    clientSocket.close();
                } catch (IOException exception) {

                }
            }
        });
        setVisible(true);
    }

    public void sendMsg() {
        String messageStr = jtfName.getText() + ": " + jtfMessage.getText();
        outMessage.println(messageStr);
        outMessage.flush();
        jtfMessage.setText("");
    }
}