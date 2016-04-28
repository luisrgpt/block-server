package pt.ulisboa.tecnico.sec.filesystem.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FileSystemLogger {
	
	//Configuration constant
	private static final Level loggingLevel = Level.FINEST;
	
	private static Logger logger = null;
	
	public static void initLogger(Object object)
			throws FileSystemLoggerException {
		initLogger(object.getClass());
	}
	
	public static <Generic> void initLogger(Class<Generic> object)
			throws FileSystemLoggerException {
		try {
			String loggerName = object.getName();
			
			FileHandler fileHandler;
			fileHandler = new FileHandler(loggerName + ".txt");
			fileHandler.setFormatter(new FileSystemFormatter());
			
			logger = Logger.getLogger(loggerName);
			
			logger.setUseParentHandlers(false);
			
			logger.setLevel(loggingLevel);
			logger.addHandler(fileHandler);
		} catch (SecurityException | IOException exception) {
			throw new FileSystemLoggerException(exception.getMessage(), exception);
		}
	}

	public static void log(String message)
			throws FileSystemLoggerException {
		if(logger == null) {
			throw new FileSystemLoggerException(message);
		}
		logger.info(message);
	}
	

	public static void log()
			throws FileSystemLoggerException {
		log("");
	}
	
	public static void logDescription(String message)
			throws FileSystemLoggerException {
		if(logger == null) {
			throw new FileSystemLoggerException(message);
		}
		logger.finest(message);
	}
	
	public static void logDescription()
			throws FileSystemLoggerException {
		logDescription("");
	}
	
	public static void logWarning(String message)
			throws FileSystemLoggerException {
		if(logger == null) {
			throw new FileSystemLoggerException(message);
		}
		logger.warning(message);
	}
	
	public static void logWarning()
			throws FileSystemLoggerException {
		logWarning("");
	}
	
	public static void logError(String message)
			throws FileSystemLoggerException {
		if(logger == null) {
			throw new FileSystemLoggerException(message);
		}
		logger.severe(message);
	}
	
	public static void logError()
			throws FileSystemLoggerException {
		logError("");
	}
}
