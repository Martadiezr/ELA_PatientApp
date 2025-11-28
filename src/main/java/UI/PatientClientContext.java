package UI;

import SendData.ReceiveDataViaNetwork;
import SendData.SendDataViaNetwork;

import java.io.IOException;
import java.net.Socket;

public class PatientClientContext {
    private Socket socket;
    private SendDataViaNetwork sendData;
    private ReceiveDataViaNetwork receiveData;
    private PatientUI patientUI;

    public PatientClientContext(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.sendData = new SendDataViaNetwork(socket);
        this.receiveData = new ReceiveDataViaNetwork(socket);
        this.patientUI = new PatientUI();

        // Identificar al servidor que somos DOCTOR
        sendData.sendInt(1);
        String msg = receiveData.receiveString();

        if (!"PATIENT".equals(msg)) {
            throw new IOException("Server did not accept patient client. Response: " + msg);
        }
    }

    public Socket getSocket() { return socket; }
    public SendDataViaNetwork getSendData() { return sendData; }
    public ReceiveDataViaNetwork getReceiveData() { return receiveData; }
    public PatientUI getPatientUI() { return patientUI; }
}
