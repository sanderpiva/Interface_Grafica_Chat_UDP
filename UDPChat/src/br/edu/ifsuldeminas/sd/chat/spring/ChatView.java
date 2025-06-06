package br.edu.ifsuldeminas.sd.chat.spring; 

import br.edu.ifsuldeminas.sd.chat.ChatException;
import br.edu.ifsuldeminas.sd.chat.ChatFactory;
import br.edu.ifsuldeminas.sd.chat.MessageContainer;
import br.edu.ifsuldeminas.sd.chat.Sender;
import br.edu.ifsuldeminas.sd.chat.client.Chat; 

import javax.swing.*;
import javax.swing.text.*; 
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Objects;

public class ChatView extends JFrame implements MessageContainer {
    private JTextField localPortField;
    private JTextField remotePortField;
    private JTextPane chatArea; 
    private JTextField messageField;
    private JButton sendButton;
    private JButton connectButton;
    private Sender sender;
    private String userName;

    
    private SimpleAttributeSet emissorStyle;
    private SimpleAttributeSet receptorStyle;
    private SimpleAttributeSet infoStyle; 

    public ChatView() {
        super("Chat UDP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 400); 
        setLayout(new BorderLayout());

        
        createStyles();

        JPanel connectionPanel = new JPanel(new FlowLayout()); 
        connectionPanel.add(new JLabel("Porta Local:"));
        localPortField = new JTextField("2000", 5);
        connectionPanel.add(localPortField);
        connectionPanel.add(new JLabel("Porta Remota:"));
        remotePortField = new JTextField("2001", 5);
        connectionPanel.add(remotePortField);
        connectionPanel.add(new JLabel("Nome:"));
        JTextField userNameField = new JTextField("User", 10);
        connectionPanel.add(userNameField);

        connectButton = new JButton("Conectar");
        connectionPanel.add(connectButton);
        add(connectionPanel, BorderLayout.NORTH);

        
        chatArea = new JTextPane(); 
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messagePanel.add(messageField, BorderLayout.CENTER);
        sendButton = new JButton("Enviar");
        messagePanel.add(sendButton, BorderLayout.EAST);
        add(messagePanel, BorderLayout.SOUTH);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int localPort = Integer.parseInt(localPortField.getText());
                    int remotePort = Integer.parseInt(remotePortField.getText());
                    userName = userNameField.getText();

                    sender = ChatFactory.build("localhost", remotePort, localPort, ChatView.this);
                    connectButton.setEnabled(false); 
                    localPortField.setEditable(false);
                    remotePortField.setEditable(false);
                    userNameField.setEditable(false);
                    appendMessageToChatArea("Conectado como: " + userName, infoStyle); 
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ChatView.this, "Por favor, insira números válidos para as portas.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
                } catch (ChatException ex) {
                    appendMessageToChatArea("Erro ao conectar: " + ex.getCause().getMessage(), infoStyle); 
                    JOptionPane.showMessageDialog(ChatView.this, "Erro ao conectar: " + ex.getCause().getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        messageField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        setVisible(true);
    }

    
    private void createStyles() {
        emissorStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(emissorStyle, Color.BLUE);
        StyleConstants.setBold(emissorStyle, true); 

        receptorStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(receptorStyle, Color.RED);
        StyleConstants.setBold(receptorStyle, true); 

        infoStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(infoStyle, Color.DARK_GRAY);
        StyleConstants.setItalic(infoStyle, true);
    }

    private void sendMessage() {
        if (sender == null) {
            JOptionPane.showMessageDialog(this, "Por favor, conecte-se antes de enviar mensagens.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = messageField.getText();
        if (!message.trim().isEmpty()) {
            if (message.equalsIgnoreCase(Chat.KEY_TO_EXIT)) {
                System.exit(0);
            }
            try {
                
                String fullMessage = String.format("%s%s%s", message, MessageContainer.FROM, userName);
                sender.send(fullMessage);
                
                appendMessageToChatArea(message + " => " + userName, emissorStyle);
                messageField.setText("");
            } catch (ChatException ex) {
                appendMessageToChatArea("Erro ao enviar mensagem: " + ex.getCause().getMessage(), infoStyle);
            }
        }
    }

    private void appendMessageToChatArea(String message, AttributeSet style) {
        StyledDocument doc = chatArea.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), message + "\n", style);
            
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void newMessage(String message) {
        if (message == null || message.equals(""))
            return;

        String[] messageParts = message.split(MessageContainer.FROM);
        String displayedMessageContent;
        String senderName;

        if (messageParts.length > 1) {
            displayedMessageContent = messageParts[0];
            senderName = messageParts[1].trim();
        } else {
            displayedMessageContent = message.trim();
            senderName = "Desconhecido"; 
        }

        if (userName != null && !userName.isEmpty() && !senderName.equalsIgnoreCase(userName)) {
            appendMessageToChatArea(displayedMessageContent + " => " + senderName, receptorStyle);
        }
        
        else if (userName != null && !userName.isEmpty() && senderName.equalsIgnoreCase(userName)) {
            // Não faz nada, pois a mensagem já foi exibida pelo sendMessage()
        }
        
        else {
             appendMessageToChatArea(displayedMessageContent + " de " + senderName, infoStyle);
        }
    }
}