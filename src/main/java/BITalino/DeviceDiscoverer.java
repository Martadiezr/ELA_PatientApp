package BITalino;
import com.fazecast.jSerialComm.SerialPort;
import java.util.Vector;

public class DeviceDiscoverer {

    private SerialPort serialPort;

    public DeviceDiscoverer() {
        // Puedes buscar los dispositivos aquí si lo deseas
    }

    /**
     * Find BITalino device via serial port
     *
     * @return List of serial ports that match the BITalino device
     */
    public Vector<String> findBitalinoDevices() {
        Vector<String> ports = new Vector<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            String sys = port.getSystemPortName();  // e.g., /dev/cu.BITalino-XX or COM3 on Windows
            String desc = port.getDescriptivePortName();  // description that may include "BITalino"

            // Search for ports with names or descriptions containing "BITalino"
            if (sys != null && sys.toLowerCase().contains("bitalino")) {
                ports.add(sys);  // Add the serial port
            }
        }
        return ports;
    }

    /**
     * Connect to BITalino via the first found serial port
     *
     * @throws Exception
     */
    public void connectToBitalino() throws Exception {
        Vector<String> bitalinoPorts = findBitalinoDevices();

        if (bitalinoPorts.isEmpty()) {
            throw new Exception("No BITalino devices found.");
        }

        // Choose the first BITalino port found (assuming only one device is needed)
        String portPath = bitalinoPorts.firstElement();
        serialPort = SerialPort.getCommPort(portPath);
        serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 0);

        if (!serialPort.openPort()) {
            throw new Exception("Failed to open the BITalino serial port.");
        }

        System.out.println("Connected to BITalino on port: " + portPath);
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void closeConnection() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
    }
}
