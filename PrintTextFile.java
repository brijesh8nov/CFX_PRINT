/* Print Text File 
* created by BRIJESH CHAUHAN : brijesh@brijeshradhika.com
* version 1.0
* created Date : 29 MAR 2012
* This tag prints a TEXT file to the DEFAULT printer
*/
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PrinterJob;
import javax.print.SimpleDoc;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

import com.allaire.cfx.*;

public class PrintTextFile implements CustomTag {

  public void processRequest( Request request, Response response ) 
    throws Exception { 
	String FILE_NAME = request.getAttribute("FILENAME");
	response.writeDebug(FILE_NAME);
	PrintService pservice[] = PrinterJob.lookupPrintServices();
	String defaultPrinter = pservice[1].getName();
    //String defaultPrinter = PrintServiceLookup.lookupDefaultPrintService().getName();
    //System.out.println("Default printer: " + defaultPrinter);
	response.writeDebug("Default printer: " + defaultPrinter);

    //PrintService service = PrintServiceLookup.lookupDefaultPrintService();
	
	PrintService service[] = PrinterJob.lookupPrintServices();

    FileInputStream in = new FileInputStream(new File(FILE_NAME));

    PrintRequestAttributeSet  pras = new HashPrintRequestAttributeSet();
    pras.add(new Copies(1));


    DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
    Doc doc = new SimpleDoc(in, flavor, null);
    DocPrintJob job = pservice[1].createPrintJob();
    PrintJobWatcher pjw = new PrintJobWatcher(job);
    job.print(doc, pras);
    pjw.waitForDone();
    in.close();

    // send FF to eject the page
    InputStream ff = new ByteArrayInputStream("\f".getBytes());
    Doc docff = new SimpleDoc(ff, flavor, null);
    DocPrintJob jobff = pservice[1].createPrintJob();
    pjw = new PrintJobWatcher(jobff);
    jobff.print(docff, null);
    pjw.waitForDone();
	response.write( FILE_NAME + "SENT TO PRINTER"  );
  }
}

class PrintJobWatcher {
  boolean done = false;

  PrintJobWatcher(DocPrintJob job) {
    job.addPrintJobListener(new PrintJobAdapter() {
      public void printJobCanceled(PrintJobEvent pje) {
        allDone();
      }
      public void printJobCompleted(PrintJobEvent pje) {
        allDone();
      }
      public void printJobFailed(PrintJobEvent pje) {
        allDone();
      }
      public void printJobNoMoreEvents(PrintJobEvent pje) {
        allDone();
      }
      void allDone() {
        synchronized (PrintJobWatcher.this) {
          done = true;
          //System.out.println("Printing done ...");
		   //response.writeDebug("Printing done ...");
          PrintJobWatcher.this.notify();
        }
      }
    });
  }
  public synchronized void waitForDone() {
    try {
      while (!done) {
        wait();
      }
    } 
		catch (InterruptedException e) {
    }
  }
}

