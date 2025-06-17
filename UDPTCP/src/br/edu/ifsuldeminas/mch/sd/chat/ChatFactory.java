package br.edu.ifsuldeminas.mch.sd.chat;

import java.net.InetAddress;
import java.net.UnknownHostException;

import br.edu.ifsuldeminas.mch.sd.chat.client.Chat;

public class ChatFactory {

    public static Sender build(boolean isUdp, String remoteIp, int remotePort,
                               int localPort, MessageContainer container) throws ChatException {
        try {
            InetAddress receiverAddress = InetAddress.getByName(remoteIp);

            if (isUdp) {
                new UDPReceiver(localPort, Chat.RECEIVER_BUFFER_SIZE, container); 
                
                return new UDPSender(receiverAddress, remotePort);
            } else {
                new TCPReceiver(localPort, container);
                return new TCPSender(receiverAddress, remotePort);
            }
        } catch (UnknownHostException e) {
            throw new ChatException("Invalid remote IP address.", e);
        }
    }

    public static Sender build(String remoteIp, int remotePort,
                               int localPort, MessageContainer container) throws ChatException {
        return build(true, remoteIp, remotePort, localPort, container);
    }
}