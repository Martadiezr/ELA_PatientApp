package SendData;

import BITalino.Frame;
import pojos.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

public class SendDataViaNetwork {
    private DataOutputStream dataOutputStream;
    public SendDataViaNetwork(Socket socket) {
        try {
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.err.println("Error al inicializar el flujo de salida: " + ex.getMessage());
            Logger.getLogger(SendDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendStrings(String message) throws IOException{
        dataOutputStream.writeUTF(message);
        dataOutputStream.flush();
    }

    public void sendInt(int message) throws IOException{
        dataOutputStream.writeInt(message);
        dataOutputStream.flush();
    }

    public void sendUser(User user) throws IOException{
        dataOutputStream.writeUTF(user.getEmail());
        dataOutputStream.writeUTF(String.valueOf(user.getRole().getName()));
        byte[] password = user.getPasswordEncripted();
        dataOutputStream.writeUTF(new String(password));
    }

    public void sendPatient(Patient patient) throws IOException{

        dataOutputStream.writeUTF(patient.getName());
        dataOutputStream.writeUTF(patient.getSurname());
        dataOutputStream.writeUTF(patient.getDni());
        dataOutputStream.writeUTF(String.valueOf(patient.getDateOfBirth()));
        dataOutputStream.writeUTF(patient.getSex());
        dataOutputStream.writeInt(patient.getPhone());
        dataOutputStream.writeUTF(patient.getEmail());
        dataOutputStream.writeInt(patient.getInsurance());
        dataOutputStream.flush();
    }

    public void sendDoctor(Doctor doctor) throws IOException {
        dataOutputStream.writeInt(doctor.getId());
        dataOutputStream.writeUTF(doctor.getName());
        dataOutputStream.writeUTF(doctor.getSurname());
        dataOutputStream.writeUTF(doctor.getDNI());
        dataOutputStream.writeUTF(String.valueOf(doctor.getBirthDate()));
        dataOutputStream.writeUTF(doctor.getSex());
        dataOutputStream.writeUTF(doctor.getEmail());
        dataOutputStream.flush();

    }


    // Método para enviar la señal grabada al servidor
    public void sendSignal(Frame[] frames) throws IOException {
        for (Frame frame : frames) {
            // Enviar cada valor de los canales analógicos (puedes elegir cómo procesar los datos)
            for (int i = 0; i < frame.analog.length; i++) {
                // Aquí, enviamos los valores de cada canal analógico. Puedes convertirlo si es necesario
                dataOutputStream.writeInt(frame.analog[i]);  // Enviar valor de cada canal analógico
            }
        }
        dataOutputStream.flush();  // Asegurarse de que los datos se envíen
    }

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

    public void sendMedicalInformation(MedicalInformation medicalInformation) throws IOException {

        dataOutputStream.writeUTF(String.valueOf(medicalInformation.getReportDate()));
        // Enviar la lista de síntomas
        sendSymptoms(medicalInformation.getSymptoms());

        // Enviar la lista de medicamentos
        sendMedications(medicalInformation.getMedication());

        dataOutputStream.flush();
    }

    public void sendSymptoms(List<Symptom> symptoms) throws IOException {
        // 1. Enviar cuántos síntomas vienen
        dataOutputStream.writeInt(symptoms.size());

        // 2. Enviar cada síntoma (ajusta los campos a lo que tenga tu clase Symptom)
        for (Symptom symptom : symptoms) {
            dataOutputStream.writeInt(symptom.getId());
            dataOutputStream.writeUTF(symptom.getDescription());
            // si tu Symptom tiene más cosas, las vas escribiendo aquí en el mismo orden
        }

        dataOutputStream.flush();
    }

    public void sendMedications(List<String> medications) throws IOException {
        // 1. Enviar cuántos síntomas vienen
        dataOutputStream.writeInt(medications.size());

        // 2. Enviar cada síntoma (ajusta los campos a lo que tenga tu clase Symptom)
        for (String medication : medications) {
            dataOutputStream.writeUTF(medication);
            // si tu Symptom tiene más cosas, las vas escribiendo aquí en el mismo orden
        }

        dataOutputStream.flush();
    }


}
