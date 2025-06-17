package br.edu.ifsuldeminas.mch.sd.chat.swing; 

import br.edu.ifsuldeminas.mch.sd.chat.ChatException;
import br.edu.ifsuldeminas.mch.sd.chat.ChatFactory;
import br.edu.ifsuldeminas.mch.sd.chat.MessageContainer;
import br.edu.ifsuldeminas.mch.sd.chat.Sender;
import br.edu.ifsuldeminas.mch.sd.chat.client.Chat; // Importação necessária para acessar KEY_TO_EXIT

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
    private JTextField remoteIpField;
    private JRadioButton udpRadioButton;
    private JRadioButton tcpRadioButton;
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
        super("Chat UDP/TCP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 450); 
        setLayout(new BorderLayout());

        createStyles();

        JPanel connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); 
        gbc.fill = GridBagConstraints.HORIZONTAL; 

        gbc.gridx = 0; 
        gbc.gridy = 0; 
        connectionPanel.add(new JLabel("Porta Local:"), gbc);
        gbc.gridx = 1; 
        localPortField = new JTextField("2000", 5);
        connectionPanel.add(localPortField, gbc);

        gbc.gridx = 2; 
        connectionPanel.add(new JLabel("IP Remoto:"), gbc);
        gbc.gridx = 3; 
        remoteIpField = new JTextField("localhost", 10);
        connectionPanel.add(remoteIpField, gbc);

        gbc.gridx = 4; 
        connectionPanel.add(new JLabel("Porta Remota:"), gbc);
        gbc.gridx = 5; 
        remotePortField = new JTextField("2001", 5);
        connectionPanel.add(remotePortField, gbc);

        gbc.gridx = 0; 
        gbc.gridy = 1; 
        connectionPanel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; 
        JTextField userNameField = new JTextField("User", 10);
        gbc.gridwidth = 2; 
        connectionPanel.add(userNameField, gbc);
        gbc.gridwidth = 1; 

        
        ButtonGroup protocolGroup = new ButtonGroup();
        udpRadioButton = new JRadioButton("UDP", true); 
        tcpRadioButton = new JRadioButton("TCP");
        protocolGroup.add(udpRadioButton);
        protocolGroup.add(tcpRadioButton);

        JPanel protocolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        protocolPanel.setBorder(BorderFactory.createTitledBorder("Protocolo"));
        protocolPanel.add(udpRadioButton);
        protocolPanel.add(tcpRadioButton);

        gbc.gridx = 3; 
        gbc.gridy = 1; 
        gbc.gridwidth = 2; 
        connectionPanel.add(protocolPanel, gbc);
        gbc.gridwidth = 1; 

        gbc.gridx = 5; 
        gbc.gridy = 1; 
        connectButton = new JButton("Conectar");
        connectionPanel.add(connectButton, gbc);

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
                    String remoteIp = remoteIpField.getText();
                    int remotePort = Integer.parseInt(remotePortField.getText());
                    userName = userNameField.getText();
                    boolean isUdp = udpRadioButton.isSelected();

                    sender = ChatFactory.build(isUdp, remoteIp, remotePort, localPort, ChatView.this);
                    connectButton.setEnabled(false);
                    localPortField.setEditable(false);
                    remotePortField.setEditable(false);
                    remoteIpField.setEditable(false); 
                    userNameField.setEditable(false);
                    udpRadioButton.setEnabled(false); 
                    tcpRadioButton.setEnabled(false);

                    appendMessageToChatArea("Conectado como: " + userName + " via " + (isUdp ? "UDP" : "TCP"), infoStyle);
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
        } else if (userName != null && !userName.isEmpty() && senderName.equalsIgnoreCase(userName)) {
            // Não faz nada, pois a mensagem já foi exibida pelo sendMessage()
        } else {
            appendMessageToChatArea(displayedMessageContent + " de " + senderName, infoStyle);
        }
    }
}