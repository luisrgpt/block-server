package pt.ulisboa.tecnico.sec.filesystem.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

class FileSystemFormatter extends Formatter {
	// this method is called for every log records

	@Override
	public synchronized String format(LogRecord rec) {
		return rec.getMessage() + System.getProperty("line.separator");
	}

	@Override
	public synchronized String formatMessage(LogRecord rec) {
		return rec.getMessage() + System.getProperty("line.separator");
	}

	/*private String calcDate(long millisecs) {

	    SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm");

	    Date resultdate = new Date(millisecs);

	    return date_format.format(resultdate);

	  }*/

	@Override
	public String getHead(Handler h) {
		return "";
	}

	@Override
	public String getTail(Handler h) {
		return "";
	}

}
