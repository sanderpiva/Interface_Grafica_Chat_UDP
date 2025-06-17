package br.edu.ifsuldeminas.mch.sd.chat;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
class UDPReceiver implements Receiver {
	private static int MIN_BUFFER_SIZE = 1000;
	private static int MIN_PORT_NUMBER = 1024;
	private int portNumber;
	private int bufferSize;
	private DatagramSocket receiverSocket = null;
	private byte[] incomingBuffer = null;
	private boolean isRunning = false;
	private MessageContainer container;
	public UDPReceiver(int portNumber, int bufferSize,
			MessageContainer container) throws ChatException {
		validateAttributes(portNumber, bufferSize, container);
		asignAttributes(portNumber, bufferSize, container);
		try {
			prepare();
		} catch (SocketException socketException) {
			throw new ChatException(
					"There was some errors starting yor receiver.",
					socketException);
		}
		new Thread(this).start();
	}
	public void run() {
		if (!isRunning) {
			isRunning = true;
			while (true) {

				try {
					receive();
				} catch (IOException ioException) {
					container
					.newMessage("There was some errors when receiving messages.");
				}
			}
		}
	}
	private void validateAttributes(int portNumber, int bufferSize,
			MessageContainer container) {
		if (portNumber <= MIN_PORT_NUMBER)
			throw new IllegalArgumentException(
					"The Receiver can’t use reserved ports.");
		if (container == null)
			throw new IllegalArgumentException(
					"The message container can’t be null.");
		if (bufferSize < MIN_BUFFER_SIZE)
			throw new IllegalArgumentException(String.format(
					"The buffers size needs to be greater than %d",
					MIN_BUFFER_SIZE));
	}
	private void asignAttributes(int portNumber, int bufferSize,
			MessageContainer container) {
		this.portNumber = portNumber;
		this.bufferSize = bufferSize;
		this.container = container;
	}
	private void prepare() throws SocketException {
		receiverSocket = new DatagramSocket(portNumber);
	}
	private void receive() throws IOException {
		incomingBuffer = new byte[bufferSize];
		DatagramPacket received = new DatagramPacket(incomingBuffer,
				incomingBuffer.length);
		receiverSocket.receive(received);
		container.newMessage(new String(received.getData()));
	}
}