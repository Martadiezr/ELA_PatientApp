package UI;

import SendData.ReceiveDataViaNetwork;
import SendData.SendDataViaNetwork;

import java.io.IOException;
import java.net.Socket;

/**
 * Manages the network connection context for the Patient Client.
 * It initializes the socket, data streams, and performs the initial
 * identification handshake with the server.
 */
public class PatientClientContext {
    private Socket socket;
    // Data sender wrapper
    private SendDataViaNetwork sendData;
    // Data receiver wrapper
    private ReceiveDataViaNetwork receiveData;
    // UI logic handler
    private PatientUI patientUI;

    /**
     * Constructor: Establishes connection and performs client identification.
     * @param host The server's IP address or hostname.
     * @param port The server's port number.
     * @throws IOException If connection fails or server rejects the client type.
     */
    public PatientClientContext(String host, int port) throws IOException {
        // 1. Establish the network connection
        this.socket = new Socket(host, port);
        // 2. Initialize the data streams using the established socket
        this.sendData = new SendDataViaNetwork(socket);
        this.receiveData = new ReceiveDataViaNetwork(socket);
        // 3. Initialize the Patient UI Logic handler
        this.patientUI = new PatientUI();

        // Initial Handshake: Identify ourselves to the server as a Patient Client

        // Send '1' (or the corresponding code) to the server to identify as PATIENT
        sendData.sendInt(1);
        // Wait for the server's response message
        String msg = receiveData.receiveString();

        // Check if the server confirmed our identity
        if (!"PATIENT".equals(msg)) {
            // If the server response is not 'PATIENT', throw an exception
            throw new IOException("Server did not accept patient client. Response: " + msg);
        }
    }

    // --- Getters to access connection resources from the GUI ---
    public Socket getSocket() { return socket; }
    public SendDataViaNetwork getSendData() { return sendData; }
    public ReceiveDataViaNetwork getReceiveData() { return receiveData; }
    public PatientUI getPatientUI() { return patientUI; }
}
