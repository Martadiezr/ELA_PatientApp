package BITalino;


import com.fazecast.jSerialComm.SerialPort;
import pojos.Signal;
import pojos.TypeSignal;

/**
 * Service class responsible for handling the connection, configuration,
 * and data acquisition from a BITalino device.
 */
public class BitalinoServiceWindows {

    private final String macAddress; // The MAC Address or specific port name of the BITalino (optional)
    private final int samplingRate; // The frequency at which data is collected (e.g., 100 Hz)

    /**
     * Constructor. Initializes the service with connection parameters.
     * @param macAddress The MAC address or port name (can be null/empty for auto-search).
     * @param samplingRate The desired sampling rate (e.g., 100).
     */
    public BitalinoServiceWindows(String macAddress, int samplingRate) {
        this.macAddress = macAddress;
        this.samplingRate = samplingRate;
    }

    /**
     * Acquires a physiological signal (EMG or ECG) from the BITalino device.
     * @param type The type of signal to acquire (EMG or ECG).
     * @param clientId The ID of the patient recording the signal.
     * @param seconds The duration of the recording in seconds.
     * @return The collected Signal object containing the samples.
     * @throws Throwable If connection, configuration, or reading fails.
     */
    public Signal acquireSignal(TypeSignal type, int clientId, int seconds) throws Throwable {
        // Initialize the Signal object to store the acquired data
        Signal signal = new Signal(type, clientId);
        BITalinoWindows bitalino = null; // BITalino object to manage the device connection

        try {
            // --- 1. DEVICE SEARCH AND PORT DETECTION ---
            // --- 2. CONNECTION SETUP ---

            bitalino = new BITalinoWindows();
            System.out.println("Connecting to: " + macAddress);
            // Open the connection specifying the port and the desired sampling rate
            bitalino.open(macAddress, samplingRate);

            // --- 3. CHANNEL CONFIGURATION ---

            // CRUCIAL: Activate all channels (0, 1, 2, 3, 4, 5) to ensure stable packet size
            // This prevents read errors by having a consistent frame structure.
            int[] channelsToStart = {0, 1, 2, 3, 4, 5};

            // Determine which analog channel index to read based on the requested signal type
            int channelIndexToRead;

            if (type == TypeSignal.EMG) {
                channelIndexToRead = 0; // EMG is typically connected to Analog Input 1 (A1), which is index 0
                System.out.println("Configurado: EMG (Leyendo canal A1 del flujo total)");
            } else {
                channelIndexToRead = 1; // ECG is typically connected to Analog Input 2 (A2), which is index 1
                System.out.println("Configurado: ECG (Leyendo canal A2/1)");
            }

            // Start the acquisition process with the defined channels
            bitalino.start(channelsToStart);

            // Wait briefly for the stream to stabilize before reading data
            try { Thread.sleep(3000); } catch (InterruptedException e) {}

            // Calculate the total number of samples needed based on rate and duration
            int totalSamples = samplingRate * seconds;
            System.out.println("Recording " + totalSamples + " samples...");

            // --- 4. DATA ACQUISITION LOOP ---

            for (int i = 0; i < totalSamples; i++) {
                try {
                    // Read exactly 1 frame (one packet of data)
                    Frame[] frames = bitalino.read(1);

                    if (frames != null && frames.length > 0) {
                        // Extract the relevant analog value from the frame
                        int val = 0;

                        // Safety check: Ensure the analog array is long enough for the target channel
                        if (frames[0].analog.length > channelIndexToRead) {
                            val = frames[0].analog[channelIndexToRead];
                        } else {
                            // Fallback: If the array is unexpectedly short, grab the first available sample
                            val = frames[0].analog[0];
                        }

                        signal.addSample(val); // Add the read value to the Signal object

                        // Provide simple visual feedback to the user
                        if (i % 10 == 0) System.out.print(".");
                    }
                } catch (Exception e) {
                    // Handle errors during sample reading (e.g., brief communication issues)
                    System.out.println(" [ERROR Muestra " + i + ": " + e.getMessage() + "] ");

                    // Slow down the loop to prevent console flooding during errors
                    try { Thread.sleep(100); } catch (Exception ignored) {}
                    signal.addSample(0); // Add a zero sample to maintain time consistency
                }
            }

            System.out.println("\nLecture finished correctly.");
            bitalino.stop(); // Stop the BITalino acquisition stream

        } catch (Exception e) {
            System.err.println("Error BitalinoService: " + e.getMessage());
            // Attempt to stop the stream if an error occurred during acquisition
            if (bitalino != null) { try { bitalino.stop(); } catch (Exception ignored) {} }
            throw e; // Re-throw the exception for upper layers to handle
        } finally {
            // --- 5. RESOURCE CLEANUP ---
            if (bitalino != null) {
                // Ensure both stop and close are called, regardless of success
                try { bitalino.stop(); } catch (Exception ignored) {}
                try { bitalino.close(); } catch (Exception ignored) {} // Close the serial connection
            }
        }

        return signal;
    }
}
