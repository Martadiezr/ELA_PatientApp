package SendData;

// Import necessary classes for signal, POJOs, I/O, and logging
import BITalino.Frame; // Assuming this is used for some signal data
import pojos.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

/**
 * Class responsible for sending data over a network connection.
 * It uses a DataOutputStream to write primitive data types and objects
 * to the other end of the socket.
 */
public class SendDataViaNetwork {
    // Stream to write primitive Java data types to an underlying output stream
    private DataOutputStream dataOutputStream;

    /**
     * Constructor for SendDataViaNetwork.
     * Initializes the DataOutputStream from the provided socket's output stream.
     * @param socket The connected network socket.
     */
    public SendDataViaNetwork(Socket socket) {
        try {
            // Get the output stream from the socket and wrap it in a DataOutputStream
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            // Error handling if initialization fails
            System.err.println("Error al inicializar el flujo de salida: " + ex.getMessage());
            Logger.getLogger(SendDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Writes a String to the network using the UTF format and flushes the buffer.
     * @param message The String to send.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void sendStrings(String message) throws IOException{
        dataOutputStream.writeUTF(message);
        dataOutputStream.flush(); // Forces any buffered output bytes to be written out
    }

    /**
     * Writes an integer (int) to the network and flushes the buffer.
     * @param message The int to send.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void sendInt(int message) throws IOException{
        dataOutputStream.writeInt(message);
        dataOutputStream.flush();
    }

    /**
     * Sends the essential fields of a User object (Email, Role Name, Encrypted Password).
     * NOTE: The receiver must read these fields in the same order.
     * @param user The User object to send.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void sendUser(User user) throws IOException{
        dataOutputStream.writeUTF(user.getEmail());
        // Writes the role name as a String
        dataOutputStream.writeUTF(String.valueOf(user.getRole().getName()));
        // Writes the encrypted password bytes as a String (assuming it's a safe encoding)
        byte[] password = user.getPasswordEncripted();
        dataOutputStream.writeUTF(new String(password));
    }

    /**
     * Sends the fields of a Patient object.
     * The order of writing must be strictly matched by the reading order on the receiver side.
     * @param patient The Patient object to send.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void sendPatient(Patient patient) throws IOException{

        dataOutputStream.writeUTF(patient.getName());
        dataOutputStream.writeUTF(patient.getSurname());
        dataOutputStream.writeUTF(patient.getDni());
        // Converts the Date object to a String before sending
        dataOutputStream.writeUTF(String.valueOf(patient.getDateOfBirth()));
        dataOutputStream.writeUTF(patient.getSex());
        dataOutputStream.writeInt(patient.getPhone());
        dataOutputStream.writeUTF(patient.getEmail());
        dataOutputStream.writeInt(patient.getInsurance());
        dataOutputStream.flush();
    }

    /**
     * Closes the DataOutputStream to release system resources.
     * This should always be called when the communication is finished.
     */
    public void releaseResources() {
        try {
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        } catch (IOException ex) {
            System.err.println("Error with resources: " + ex.getMessage());
            Logger.getLogger(SendDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sends a MedicalInformation object, including nested lists.
     * @param medicalInformation The MedicalInformation object to send.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void sendMedicalInformation(MedicalInformation medicalInformation) throws IOException {

        // Sends the report date as a String
        dataOutputStream.writeUTF(String.valueOf(medicalInformation.getReportDate()));
        // Calls a helper method to send the list of symptoms
        sendSymptoms(medicalInformation.getSymptoms());

        // Calls a helper method to send the list of medications
        sendMedications(medicalInformation.getMedication());

        // NOTE: The ID and Feedback seem to be missing here compared to the receiver.
        // The receiver side expects ID and Feedback, so consider adding them here.

        dataOutputStream.flush();
    }

    /**
     * Sends a list of Symptom objects.
     * Crucial step: The size of the list is sent first, followed by the elements.
     * @param symptoms The List of Symptom objects.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void sendSymptoms(List<Symptom> symptoms) throws IOException {
        // 1. Send how many symptoms are coming (the size of the list)
        dataOutputStream.writeInt(symptoms.size());

        // 2. Send each symptom's fields in a loop
        for (Symptom symptom : symptoms) {
            dataOutputStream.writeInt(symptom.getId());
            dataOutputStream.writeUTF(symptom.getDescription());
            // If Symptom had more fields, they would be written here in order
        }

        dataOutputStream.flush();
    }

    /**
     * Sends a list of medication names (Strings).
     * Crucial step: The size of the list is sent first, followed by the elements.
     * @param medications The List of medication Strings.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void sendMedications(List<String> medications) throws IOException {
        // 1. Send how many medications are coming (the size of the list)
        dataOutputStream.writeInt(medications.size());

        // 2. Send each medication name (String) in a loop
        for (String medication : medications) {
            dataOutputStream.writeUTF(medication);
        }

        dataOutputStream.flush();
    }

    /**
     * Sends a Signal object, typically used for biological or physical data (like from BITalino).
     * It sends metadata (type, client ID, size) followed by the actual data values.
     * @param signal The Signal object to send.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void sendSignal(Signal signal) throws IOException {
        // 1. Send the signal TYPE (e.g., EMG or ACC) converted to String
        dataOutputStream.writeUTF(signal.getType().toString());

        // 2. Send the CLIENT ID (patient)
        dataOutputStream.writeInt(signal.getClientId());

        // 3. Send the SIZE of the list of values (so the receiver knows how many to read)
        List<Integer> values = signal.getValues();
        dataOutputStream.writeInt(values.size());

        // 4. Send the VALUES one by one in a loop
        for (Integer value : values) {
            dataOutputStream.writeInt(value);
        }

        // Ensure the data is sent immediately
        dataOutputStream.flush();
    }


}
