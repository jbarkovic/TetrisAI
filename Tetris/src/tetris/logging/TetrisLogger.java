package tetris.logging;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TetrisLogger {
	  static private FileHandler fileTxt;
	  static private SimpleFormatter formatterTxt;

	  static public void setup() throws IOException {
		  setup(Level.OFF, true, true);
	  }
	  static public void setup(Level loggingLevel) throws IOException {
		  setup(loggingLevel, true, true);
	  }
	  static public void setup(Level loggingLevel, boolean useConsole, boolean useLogFile) throws IOException {
		    // get the global logger to configure it
		    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

		    if (!useConsole) {
		    	// suppress the logging output to the console
		    	Logger rootLogger = Logger.getLogger("");
		    	Handler[] handlers = rootLogger.getHandlers();
		    	if (handlers[0] instanceof ConsoleHandler) {
		    		rootLogger.removeHandler(handlers[0]);
		    	}
		    }

		    logger.setLevel(loggingLevel);
		    
		    if (useLogFile) { 
		    	fileTxt = new FileHandler("MegaTetrisLogging.txt");

		    	// create a TXT formatter
		    	formatterTxt = new SimpleFormatter();
		    	fileTxt.setFormatter(formatterTxt);
		    	logger.addHandler(fileTxt);
		    }
	  }
}