package dev.wolveringer.dataserver.terminal;

public class ConsoleWriter {
	private Terminal terminal;

	public ConsoleWriter(Terminal t) {
		this.terminal = t;
	}

	public void clear() {
	}

	public void write(String string) {
		terminal.write(string);
	}

	public void sendMessage(String message) {
		terminal.write(message);
	}
}
