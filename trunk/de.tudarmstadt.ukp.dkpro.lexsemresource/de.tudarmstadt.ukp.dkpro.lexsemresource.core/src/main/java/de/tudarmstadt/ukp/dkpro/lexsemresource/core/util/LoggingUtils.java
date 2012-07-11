package de.tudarmstadt.ukp.dkpro.lexsemresource.core.util;

public class LoggingUtils {
    
    /**
     * DOTS - print progress dots.
     * TEXT - print a message with progress in percent.
     * @author zesch
     *
     */
    public enum ProgressInfoMode { DOTS, TEXT };

    /**
     * Prints a progress counter.
     * @param counter Indicates the position in the task.
     * @param size Size of the overall task.
     * @param step How many parts should the progress counter have?
     * @param mode Sets the output mode.
     * @param text The text that should be print along with the progress indicator.
     */
    public static void printProgressInfo(int counter, int size, int step, ProgressInfoMode mode, String text) {
        if (size < step) {
            return;
        }
        
        if (counter % (size / step) == 0) {
            double progressPercent = counter * 100 / size;
            progressPercent = 1 + Math.round(progressPercent * 100) / 100.0;
            if (mode.equals(ProgressInfoMode.TEXT)) {
                System.out.println(text + ": " + progressPercent + " - " + getUsedMemory() + " MB");
            }
            else if (mode.equals(ProgressInfoMode.DOTS)) {
                System.out.print(".");
                if (progressPercent >= 100) {
                    System.out.println();
                }
            }
        }
    }

    /** Gets the memory used by the JVM in MB. 
     * @return Returns how much memory (in MB) is used by the JVM at the moment.
     */
    public static double getUsedMemory() {
        Runtime rt = Runtime.getRuntime();
        
        long memLong = rt.totalMemory() - rt.freeMemory();
        double memDouble = memLong / (1024.0 * 1024.0);
        memDouble = Math.round(memDouble * 100) / 100.0;
        return memDouble;
    }

}
