package BITalino; // O services, según tu paquete

import com.fazecast.jSerialComm.SerialPort;
import pojos.Signal;
import pojos.TypeSignal;

public class BitalinoService {

    private final String macAddress;
    private final int samplingRate;

    public BitalinoService(String macAddress, int samplingRate) {
        this.macAddress = macAddress;
        this.samplingRate = samplingRate;
    }

    public Signal acquireSignal(TypeSignal type, int clientId, int seconds) throws Throwable {
        Signal signal = new Signal(type, clientId);
        BITalino bitalino = null;

        try {
            String foundPort = null;
            if (this.macAddress != null && !this.macAddress.trim().isEmpty()) {
                foundPort = this.macAddress;
            } else {
                System.out.println("Searching BITalino...");
                SerialPort[] allPorts = SerialPort.getCommPorts();
                for (SerialPort p : allPorts) {
                    if (p.getSystemPortName().contains("BITalino") && p.getSystemPortName().contains("cu.")) {
                        foundPort = p.getSystemPortName();
                        break;
                    }
                }
                if (foundPort == null) {
                    for (SerialPort p : allPorts) {
                        if (p.getSystemPortName().contains("BITalino")) {
                            foundPort = p.getSystemPortName();
                            break;
                        }
                    }
                }
            }

            if (foundPort == null) {
                throw new Exception("Bitalino not found, check the connection.");
            }

            bitalino = new BITalino();
            System.out.println("Connecting to: " + foundPort);
            bitalino.open(foundPort, samplingRate);

            int[] channelsToStart;
            int channelIndexToRead;

            if (type == TypeSignal.EMG) {
                // Si es EMG, activamos solo Canal 0 (A1)
                channelsToStart = new int[]{0};
                channelIndexToRead = 0;
                System.out.println("Configured: EMG (channel A1)");
            } else {
                // Si es ACC (u otro), activamos solo Canal 4 (A5)
                channelsToStart = new int[]{4};
                channelIndexToRead = 4;
                System.out.println("Configured: ACC (channel A5)");
            }

            bitalino.start(channelsToStart);

            try { Thread.sleep(3000); } catch (InterruptedException e) {}

            int totalSamples = samplingRate * seconds;
            System.out.println("Recording " + totalSamples + " samples...");

            for (int i = 0; i < totalSamples; i++) {
                Frame[] frames = bitalino.read(1);

                if (frames != null && frames.length > 0) {
                    int val = frames[0].analog[channelIndexToRead];
                    signal.addSample(val);

                    if (i % 50 == 0) System.out.print(".");
                }
            }

            System.out.println("\nLecture finished correctly.");
            bitalino.stop();

        } catch (Exception e) {
            System.err.println("Error BitalinoService: " + e.getMessage());
            if (bitalino != null) { try { bitalino.stop(); } catch (Exception ignored) {} }
            throw e;
        } finally {
            if (bitalino != null) {
                try { bitalino.stop(); } catch (Exception ignored) {}
                try { bitalino.close(); } catch (Exception ignored) {}
            }
        }

        return signal;
    }
}