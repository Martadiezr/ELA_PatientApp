package SendData;

// Import necessary POJOs (Plain Old Java Objects) for data representation
import pojos.Doctor;
import pojos.MedicalInformation;
import pojos.Patient;
import pojos.Symptom;

// Import Java I/O and Networking classes
import java.io.*;
import java.net.Socket;
import java.sql.Date; // For handling SQL Date objects
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for receiving data over a network connection.
 * It uses a DataInputStream to read primitive data types and objects
 * sent from the other end of the socket.
 */
public class ReceiveDataViaNetwork {
    // Stream to read primitive Java data types from an underlying input stream
    private DataInputStream dataInputStream;
    // The network socket connection (although not used directly in most methods, it defines the connection)
    private Socket socket;

    /**
     * Constructor for ReceiveDataViaNetwork.
     * Initializes the DataInputStream from the provided socket's input stream.
     * @param socket The connected network socket.
     */
    public ReceiveDataViaNetwork(Socket socket) {
        try {
            // Get the input stream from the socket and wrap it in a DataInputStream
            this.dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            // Error handling if initialization fails (e.g., failed to get input stream)
            System.err.println("Error inicializing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reads a String from the network using the UTF format.
     * @return The received String.
     * @throws IOException If an I/O error occurs during reading.
     */
    public String receiveString() throws IOException{
        return dataInputStream.readUTF(); // Reads a String encoded in modified UTF-8
    }

    /**
     * Receives and reconstructs a Patient object by reading its fields in a specific order.
     * The order must match the sending process exactly.
     * @return The reconstructed Patient object, or null if an error occurs.
     */
    public Patient recievePatient(){
        Patient patient = null;
        try {
            System.out.println("Receiving patient data...");
            // Read each field of the Patient object in the expected order
            int id = dataInputStream.readInt();
            String name = dataInputStream.readUTF();
            String surname = dataInputStream.readUTF();
            String dni = dataInputStream.readUTF();
            // Reads the date as a String and converts it to a java.sql.Date object
            Date birthDate = Date.valueOf(dataInputStream.readUTF());
            String sex = dataInputStream.readUTF();
            int phone = dataInputStream.readInt();
            String email = dataInputStream.readUTF();
            int insurance = dataInputStream.readInt();
            // Create the new Patient object
            patient = new Patient(id,name, surname, dni, birthDate, sex, phone, email, insurance);
            System.out.println(patient);
            return patient;
        } catch (EOFException ex) {
            // Specifically catches End-Of-File exception, which can happen if the sender closes the connection
            System.out.println("Data not correctly read.");
        } catch (IOException ex) {
            System.err.println("Error receiving patient data: " + ex.getMessage());
            ex.printStackTrace();
        }
        return patient; // Returns null if an exception occurred
    }

    /**
     * Receives and reconstructs a Doctor object by reading its fields.
     * @return The reconstructed Doctor object.
     * @throws IOException If an I/O error occurs during reading.
     */
    public Doctor receiveDoctor() throws IOException{
        Doctor doctor = null;
        // Read each field of the Doctor object in the expected order
        int id = dataInputStream.readInt();
        String name = dataInputStream.readUTF();
        String surname = dataInputStream.readUTF();
        String dni = dataInputStream.readUTF();
        // Reads the date as a String and converts it to a java.sql.Date object
        Date birthDate = Date.valueOf(dataInputStream.readUTF());
        String sex = dataInputStream.readUTF();
        String email = dataInputStream.readUTF();
        // Create the new Doctor object
        doctor = new Doctor(id, name, surname, dni, birthDate, sex, email);

        return doctor;
    }


    /**
     * Receives a list of Symptom objects from the network.
     * It first reads the size of the list, then reads each Symptom object one by one.
     * @return A List of Symptom objects.
     * @throws IOException If an I/O error occurs during reading.
     */
    public List<Symptom> receiveSymptoms() throws IOException {
        // 1. Read how many symptoms are coming (the size of the list)
        int size = dataInputStream.readInt();

        List<Symptom> symptoms = new ArrayList<Symptom>();

        // 2. Read each symptom in the same order it was sent
        for (int i = 0; i < size; i++) {
            // Read the fields of a single Symptom
            int id = dataInputStream.readInt();
            String description = dataInputStream.readUTF();

            // Create the Symptom object
            Symptom symptom = new Symptom(id,description);
            symptoms.add(symptom);
        }

        return symptoms;
    }

    /**
     * Receives a list of medication names (Strings) from the network.
     * It first reads the size of the list, then reads each String.
     * @return A List of Strings representing medications.
     * @throws IOException If an I/O error occurs during reading.
     */
    public List<String> receiveMedications() throws IOException {
        // 1. Read how many medications are coming (the size of the list)
        int size = dataInputStream.readInt();

        List<String> medications = new ArrayList<String>();

        // 2. Read each medication name (String)
        for (int i = 0; i < size; i++) {
            String medication = dataInputStream.readUTF();
            medications.add(medication);
        }

        return medications;
    }

    /**
     * Receives and reconstructs a MedicalInformation object, including nested Symptom and Medication lists.
     * @return The reconstructed MedicalInformation object, or null if an error occurs.
     */
    public MedicalInformation receiveMedicalInformation() {
        MedicalInformation medicalInformation = null;
        try {
            // Read the individual fields
            int id = dataInputStream.readInt();

            // Read the report date as a String and convert it
            Date reportDate = Date.valueOf(dataInputStream.readUTF());

            // Calls a helper method to receive the list of symptoms
            List<Symptom> symptoms = receiveSymptoms();

            // Calls a helper method to receive the list of medications
            List<String> medication = receiveMedications();

            // Read the feedback String
            String feedback = dataInputStream.readUTF();

            // Create the MedicalInformation instance with all the received data
            medicalInformation = new MedicalInformation(id, symptoms, reportDate, medication, feedback);

        } catch (IOException ex) {
            System.err.println("Error receiving medical information: " + ex.getMessage());
            ex.printStackTrace();
        }
        return medicalInformation;
    }

    /**
     * Reads a single integer (int) from the network.
     * @return The received integer.
     */
    public int receiveInt() {
        int message = 0;
        try {
            message = dataInputStream.readInt();
        } catch (IOException ex) {
            System.err.println("Error receiving int: " + ex.getMessage());
            ex.printStackTrace();
        }
        return message;
    }

    /**
     * Closes the DataInputStream to release system resources.
     * This should always be called when the communication is finished.
     */
    public void releaseResources() {
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
        } catch (IOException ex) {
            System.err.println("Error with resources: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
