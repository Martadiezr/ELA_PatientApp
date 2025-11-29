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

            // --- 3. CONFIGURACIÓN DE CANALES ---

            // ACTIVAMOS TODOS LOS CANALES (0, 1, 2, 3, 4, 5)
            // Esto es crucial para que el tamaño del paquete sea estable y no falle la lectura.
            int[] channelsToStart = {0, 1, 2, 3, 4, 5};

            // Determinamos qué canal nos interesa leer (EMG=0, ACC=4)
            int channelIndexToRead;

            if (type == TypeSignal.EMG) {
                channelIndexToRead = 0; // A1
                System.out.println("Configurado: EMG (Leyendo canal A1 del flujo total)");
            } else {
                channelIndexToRead = 4; // A5
                System.out.println("Configurado: ACC (Leyendo canal A5 del flujo total)");
            }

            bitalino.start(channelsToStart);

            try { Thread.sleep(3000); } catch (InterruptedException e) {}

            int totalSamples = samplingRate * seconds;
            System.out.println("Recording " + totalSamples + " samples...");

            for (int i = 0; i < totalSamples; i++) {
                try {
                    // Leemos 1 solo frame
                    Frame[] frames = bitalino.read(1);

                    if (frames != null && frames.length > 0) {
                        // OJO: Si activamos solo 1 canal, a veces el array analog se reduce.
                        // Usamos una lógica segura para evitar "IndexOutOfBounds"
                        int val = 0;
                        if (frames[0].analog.length > channelIndexToRead) {
                            val = frames[0].analog[channelIndexToRead];
                        } else {
                            // Fallback: si el array es corto, cogemos el primero disponible
                            val = frames[0].analog[0];
                        }

                        signal.addSample(val);

                        // Feedback visual cada 10 muestras
                        if (i % 10 == 0) System.out.print(".");
                    }
                } catch (Exception e) {

                    System.out.println(" [ERROR Muestra " + i + ": " + e.getMessage() + "] ");

                    // Añadimos un pequeño freno para que no inunde la consola
                    try { Thread.sleep(100); } catch (Exception ignored) {}
                    signal.addSample(0);
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