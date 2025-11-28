package BITalino;

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
                System.out.println("Buscando BITalino automáticamente...");
                SerialPort[] allPorts = SerialPort.getCommPorts();

                for (SerialPort p : allPorts) {
                    String name = p.getSystemPortName();
                    if (name.contains("BITalino") && name.contains("cu.")) {
                        foundPort = name;
                        break;
                    }
                }

                if (foundPort == null) {
                    for (SerialPort p : allPorts) {
                        String name = p.getSystemPortName();
                        if (name.contains("BITalino")) {
                            foundPort = name;
                            break;
                        }
                    }
                }
            }

            if (foundPort == null) {
                throw new Exception("No se encontró dispositivo BITalino. Asegúrate de que está emparejado.");
            }

            bitalino = new BITalino();
            System.out.println("Conectando a: " + foundPort);

            bitalino.open(foundPort, samplingRate);

            int[] channels = { (type == TypeSignal.EMG) ? 0 : 4 };
            bitalino.start(channels);

            System.out.println("Dispositivo iniciado. Estabilizando (3s)...");
            try { Thread.sleep(3000); } catch (InterruptedException e) {}

            int totalSamples = samplingRate * seconds;

            System.out.println("Grabando " + totalSamples + " muestras...");

            for (int i = 0; i < totalSamples; i++) {
                // Leemos 1 solo frame. Si falla, el timeout de 4s nos protege.
                Frame[] frames = bitalino.read(1);

                if (frames != null && frames.length > 0) {
                    int val = (type == TypeSignal.EMG) ? frames[0].analog[0] : frames[0].analog[4];
                    signal.addSample(val);

                    // Feedback visual cada 10 muestras para saber que está vivo
                    if (i % 10 == 0) System.out.print(".");
                }
            }

            System.out.println("\nLectura finalizada.");
            bitalino.stop();

        } catch (Exception e) {
            System.err.println("Error BitalinoService: " + e.getMessage());
            if (bitalino != null) {
                try { bitalino.stop(); } catch (Exception ignored) {}
            }
            throw e;
        } finally {
            if (bitalino != null) {
                try {
                    bitalino.stop();
                } catch (Exception ignored) {}

                try {
                    bitalino.close();
                } catch (Exception ignored) {}
            }
        }

        return signal;
    }
}