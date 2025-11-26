package SendData;

import pojos.Doctor;
import pojos.MedicalInformation;
import pojos.Patient;
import pojos.Symptom;

import java.io.*;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ReceiveDataViaNetwork {
    private DataInputStream dataInputStream;
    private Socket socket;

    public ReceiveDataViaNetwork(Socket socket) {
        try {
            this.dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error inicializing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String receiveString() throws IOException{
        return dataInputStream.readUTF();
    }

    public Patient recievePatient(){
        Patient patient = null;
        try {
            System.out.println("Receiving patient data...");
            int id = dataInputStream.readInt();
            String name = dataInputStream.readUTF();
            String surname = dataInputStream.readUTF();
            String dni = dataInputStream.readUTF();
            Date birthDate = Date.valueOf(dataInputStream.readUTF());
            String sex = dataInputStream.readUTF();
            int phone = dataInputStream.readInt();
            String email = dataInputStream.readUTF();
            int insurance = dataInputStream.readInt();
            patient = new Patient(id,name, surname, dni, birthDate, sex, phone, email, insurance);
            System.out.println(patient);
            return patient;
        } catch (EOFException ex) {
            System.out.println("Data not correctly read.");
        } catch (IOException ex) {
            System.err.println("Error receiving patient data: " + ex.getMessage());
            ex.printStackTrace();
        }
        return patient;
    }

    public Doctor receiveDoctor() throws IOException{
        Doctor doctor = null;
        int id = dataInputStream.readInt();
        String name = dataInputStream.readUTF();
        String surname = dataInputStream.readUTF();
        String dni = dataInputStream.readUTF();
        Date birthDate = Date.valueOf(dataInputStream.readUTF());
        String sex = dataInputStream.readUTF();
        String email = dataInputStream.readUTF();
        doctor = new Doctor(id, name, surname, dni, birthDate, sex, email);

        return doctor;
    }



    //Obtiene los sintomas desde el servidor, se solicita la informacion.
    public List<Symptom> receiveSymptoms() throws IOException {
        // 1. Leer cuántos síntomas vienen
        int size = dataInputStream.readInt();

        List<Symptom> symptoms = new ArrayList<Symptom>();

        // 2. Leer cada síntoma en el mismo orden en el que se envió
        for (int i = 0; i < size; i++) {
            int id = dataInputStream.readInt();
            String description = dataInputStream.readUTF();

            // Ajusta esto al constructor/setters que tengas en tu clase Symptom
            Symptom symptom = new Symptom(id,description);
            symptoms.add(symptom);
        }

        return symptoms;
    }

    //Obtiene los sintomas desde el servidor, se solicita la informacion.
    public List<String> receiveMedications() throws IOException {
        // 1. Leer cuántos síntomas vienen
        int size = dataInputStream.readInt();

        List<String> medications = new ArrayList<String>();

        // 2. Leer cada síntoma en el mismo orden en el que se envió
        for (int i = 0; i < size; i++) {
            String medication = dataInputStream.readUTF();
            medications.add(medication);
        }

        return medications;
    }

    public MedicalInformation receiveMedicalInformation() {
        MedicalInformation medicalInformation = null;
        try {
            Date reportDate = Date.valueOf(dataInputStream.readUTF());  // Recibe la fecha del informe

            List<Symptom> symptoms = receiveSymptoms();

            List<String> medication = receiveMedications();

            String feedback = dataInputStream.readUTF();

            // Crea la instancia de MedicalInformation con todos los datos
            medicalInformation = new MedicalInformation(symptoms, reportDate, medication, feedback);

        } catch (IOException ex) {
            System.err.println("Error receiving medical information: " + ex.getMessage());
            ex.printStackTrace();
        }
        return medicalInformation;
    }

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
