package eu.epicpvp.log;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SystemLogger extends Logger {
	private final Formatter formatter = new ConciseFormatter();
	private final LogDispatcher dispatcher = new LogDispatcher(this);

	public SystemLogger() {
		super("Logger", null);
		setLevel(Level.ALL);

		try {
			new File("log/").mkdirs();
			FileHandler fileHandler = new FileHandler("log/proxy-%g.log", 1 << 24, 8, false);
			fileHandler.setFormatter(formatter);
			addHandler(fileHandler);
		} catch (IOException ex) {
			System.err.println("Could not register logger!");
			ex.printStackTrace();
		}
		dispatcher.start();
	}

	@Override
	public void log(LogRecord record) {
		dispatcher.queue(record);
	}

	protected void doLog(LogRecord record) {
		super.log(record);
	}
}
