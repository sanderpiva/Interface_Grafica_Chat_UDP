package br.edu.ifsuldeminas.sd.chat.client;

import br.edu.ifsuldeminas.sd.chat.MessageContainer;
public class SysOutContainer implements MessageContainer {
	public void newMessage(String message) {
		if (message == null || message.equals(""))
			return;
		
		String[] messageParts = message.split(MessageContainer.FROM);

		System.out.print(messageParts[0] + " => ");
		System.out.println(messageParts[1]);
	}
}