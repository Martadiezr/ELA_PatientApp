package UI;

import BITalino.*;
import SendData.ReceiveDataViaNetwork;
import SendData.SendDataViaNetwork;
import pojos.*;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientUI {
    private Patient loggedInPatient;
    private Patient registerPatient;

    public void register(Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {
        // Crear un objeto Patient y obtener los datos del paciente
        try {
            sendDataViaNetwork.sendInt(2); // Indicar al servidor que se va a registrar un paciente

            Patient patient = new Patient();
            Role role = new Role("Patient");

            String name = Utilities.readString("Enter your name: ");
            patient.setName(name);

            String surname = Utilities.readString("Enter your surname: ");
            patient.setSurname(surname);

            String dni = Utilities.readString("Enter your dni: ");
            patient.setDni(dni);

            String dob = Utilities.readString("Enter your date of birth (YYYY-MM-DD): ");
            Date dateOfBirth = Date.valueOf(dob);
            patient.setDateOfBirth(dateOfBirth);

            String sex = Utilities.readString("Enter your sex (M/F): ");
            patient.setSex(sex);

            int phone = Utilities.readInteger("Enter your phone: ");
            patient.setPhone(phone);

            String email = Utilities.readString("Enter your email: ");
            patient.setEmail(email);

            int insurance = Utilities.readInteger("Enter your insurance: ");
            patient.setInsurance(insurance);

            String password = Utilities.readString("Enter your password: ");
            byte[] passwordBytes = password.getBytes(); // Convertir la contraseña a bytes

            if (passwordBytes != null) {
                sendDataViaNetwork.sendStrings("OK");
                User user = new User(email, passwordBytes, role);
                System.out.println(patient);
                System.out.println(user);
                sendDataViaNetwork.sendPatient(patient);
                sendDataViaNetwork.sendUser(user);

                if(receiveDataViaNetwork.receiveString().equals("SUCCESS")){
                    System.out.println("Patient registered successfully.");
                    PatientApp.menuPaciente(patient, sendDataViaNetwork, receiveDataViaNetwork, socket);
                } else {
                    System.out.println("Registration failed. Please try again.");
                    return; // Salir del metodo si el registro falla
                }
            } else {
                sendDataViaNetwork.sendStrings("ERROR");
            }

        }catch(IOException e){
            System.out.println("Error in connection");
            releaseResources(socket, sendDataViaNetwork,receiveDataViaNetwork);
            System.exit(0);
        }
    }


    public void logIn(Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {
        try {
            sendDataViaNetwork.sendInt(1);
            System.out.println(receiveDataViaNetwork.receiveString());

            String username = Utilities.readString("Enter your username: ");

            String password = Utilities.readString("Enter your password: ");

            byte[] passwordBytes = password.getBytes();

            Role role = new Role("Patient");

            if(passwordBytes != null) {
                sendDataViaNetwork.sendStrings("OK");
                User user = new User(username, passwordBytes, role);
                sendDataViaNetwork.sendUser(user);
                String response = receiveDataViaNetwork.receiveString();
                System.out.println(response);

                if(response.equals("SUCCESS")) {
                    try{
                    Patient patient = receiveDataViaNetwork.recievePatient();
                    System.out.println(patient.toString());
                        if (patient != null) {
                            System.out.println("Log in successful");
                            PatientApp.menuPaciente(patient, sendDataViaNetwork, receiveDataViaNetwork, socket);
                        } else {
                            System.out.println("Patient not found");
                        }
                    } catch (IOException e) {
                        System.out.println("Log in problem");
                    }
                } else if (response.equals("ERROR")) {
                    System.out.println("User or password is incorrect");
                } else {
                    System.out.println("Login failed. Please check your credentials.");
                }


            }else {
                sendDataViaNetwork.sendStrings("ERROR");
            }

        }catch(IOException e){
            System.out.println("Error in connection");
            releaseResources(socket, sendDataViaNetwork,receiveDataViaNetwork);
            System.exit(0);
        }
    }


    private static void releaseResources(Socket socket,SendDataViaNetwork sendDataViaNetwork,ReceiveDataViaNetwork receiveDataViaNetwork){
        if(sendDataViaNetwork != null && receiveDataViaNetwork != null) {
            sendDataViaNetwork.releaseResources();
            receiveDataViaNetwork.releaseResources();
        }
        try {
            if(socket != null){
                socket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(PatientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void insertMedicalInformation(Patient patient, Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {
        try {
            sendDataViaNetwork.sendInt(1); // Indicar al servidor que se va a registrar medical information
            //opcion 1 en menuPaciente

            MedicalInformation medicalInformation = new MedicalInformation();

            Date dateReport = Date.valueOf(java.time.LocalDate.now());
            medicalInformation.setReportDate(dateReport);
            // Ingresar síntomas
            sendDataViaNetwork.sendStrings("SEND SYMPTOMS");
            String message = receiveDataViaNetwork.receiveString();
            if(message.equals("OK")){
                List<Symptom> symptoms = receiveDataViaNetwork.receiveSymptoms();
                List<Symptom> symptomsOfPatient = new ArrayList<>();
                System.out.println("Please select your symptoms");
                System.out.println(symptoms);

                System.out.println("Insert the number of your symptoms! Enter 0 to finish");
                int selection = -1;
                while(selection != 0){
                    selection = Utilities.readInteger("");
                    symptomsOfPatient.add(symptoms.get(selection));
                }

                System.out.println("Insert the medication you have been using recently, then ENTER, if yur are not medicated, enter NOTHING");
                List<String> medicaments = new ArrayList<>();
                System.out.println("To finish, please enter OK");
                String medicament = "";

                while(!medicament.equals("OK")){
                    medicament = Utilities.readString("");
                    if(medicament.equals("OK")){
                        break;
                    }
                    medicaments.add(medicament);
                }


                System.out.println("Sending now the medical information!");
                //Now we send the final Medical Information
                medicalInformation.setSymptoms(symptomsOfPatient);
                medicalInformation.setMedication(medicaments);

                sendDataViaNetwork.sendMedicalInformation(medicalInformation);
                String succesfull = receiveDataViaNetwork.receiveString();

                if(succesfull.equals("RECEIVED MEDICAL INFORMATION")){
                    System.out.println("Medical information successfully sent!");
                    PatientApp.menuPaciente(patient,sendDataViaNetwork,receiveDataViaNetwork,socket);
                }else{
                    System.out.println("ERROR");
                }


            }else{
                System.out.println("Could not fetch the symptoms from the server");
            }
        } catch (Exception e) {
            System.out.println("Error in connection");
            releaseResources(socket, sendDataViaNetwork,receiveDataViaNetwork);
            System.exit(0);
        }
    }

    public void seeDoctorFeedback(Patient patient, Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {
        try{
            sendDataViaNetwork.sendInt(3); // Indicar al servidor que se va a registrar medical information

            String feedback = null;

            String message = "REQUEST FEEDBACK";
            sendDataViaNetwork.sendStrings(message);

            String response = receiveDataViaNetwork.receiveString();
            System.out.println("response from server : "+response);
            if(response.equals("OK")){
                String dateString = Utilities.readString("Please write the date when you sent the medical report (YYYY-MM-DD): ");
                sendDataViaNetwork.sendStrings(dateString);

                MedicalInformation medicalInformation = receiveDataViaNetwork.receiveMedicalInformation();

                if(medicalInformation != null){
                    sendDataViaNetwork.sendStrings("RECEIVED MEDICAL INFORMATION");
                    feedback = medicalInformation.getFeedback();
                    System.out.println("The feedback from the doctor is:");
                    System.out.println(feedback);
                    PatientApp.menuPaciente(patient, sendDataViaNetwork, receiveDataViaNetwork , socket);
                }else{
                    sendDataViaNetwork.sendStrings("ERROR");
                }
            }else{
                System.out.println("Could not fetch the feedback from the server");
            }

        }catch (Exception e) {
            System.out.println("Error in connection");
            releaseResources(socket, sendDataViaNetwork,receiveDataViaNetwork);
            System.exit(0);
        }
    }



    public boolean logInFromGUI(
            String username,
            String password,
            Socket socket,
            SendDataViaNetwork sendDataViaNetwork,
            ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {

        sendDataViaNetwork.sendInt(1); // login

        // mensaje inicial del servidor, lo leemos y lo ignoramos o mostramos en consola
        String serverMsg = receiveDataViaNetwork.receiveString();
        System.out.println("Server says: " + serverMsg);

        byte[] passwordBytes = password.getBytes();
        Role role = new Role("Patient");
        User user = new User(username, passwordBytes, role);

        sendDataViaNetwork.sendStrings("OK");
        sendDataViaNetwork.sendUser(user);

        String response = receiveDataViaNetwork.receiveString(); // "SUCCESS" o "ERROR"
        if (!response.equals("SUCCESS")) {
            return false;
        }

        Patient patient = receiveDataViaNetwork.recievePatient();
        System.out.println("Patient logged in: " + patient);
        return patient != null;
    }
    public boolean registerFromGUI(
            String name,
            String surname,
            String dni,
            String dob,
            String sex,
            int phone,
            int insurance,
            String email,
            String password,
            Socket socket,
            SendDataViaNetwork sendDataViaNetwork,
            ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {

        sendDataViaNetwork.sendInt(2); // registrar doctor

        Patient patient = new Patient();
        patient.setName(name);
        patient.setSurname(surname);
        patient.setDni(dni);
        patient.setDateOfBirth(Date.valueOf(dob));
        patient.setSex(sex);
        patient.setPhone(phone);
        patient.setInsurance(insurance);
        patient.setEmail(email);

        byte[] passwordBytes = password.getBytes();
        Role role = new Role("Patient");
        User user = new User(email, passwordBytes, role);

        sendDataViaNetwork.sendStrings("OK");
        sendDataViaNetwork.sendPatient(patient);
        sendDataViaNetwork.sendUser(user);

        String response = receiveDataViaNetwork.receiveString(); // "SUCCESS" o "ERROR"
        return response.equals("SUCCESS");
    }
    // Versión pensada para estar dentro de PatientGUI (por ejemplo)
// Si está en otra clase, cambia "this" por un parámetro Component parent
    /**public void insertMedicalInformationGUI(Patient patient,
                                            Socket socket,
                                            SendDataViaNetwork sendDataViaNetwork,
                                            ReceiveDataViaNetwork receiveDataViaNetwork) {
        try {
            // 1. Indicamos al servidor que vamos a registrar medical information
            sendDataViaNetwork.sendInt(1);

            MedicalInformation medicalInformation = new MedicalInformation();
            Date dateReport = Date.valueOf(java.time.LocalDate.now());
            medicalInformation.setReportDate(dateReport);

            // 2. Pedimos síntomas al servidor
            sendDataViaNetwork.sendStrings("SEND SYMPTOMS");
            String message = receiveDataViaNetwork.receiveString();

            if (!"OK".equals(message)) {
                JOptionPane.showMessageDialog(this,
                        "Could not fetch the symptoms from the server",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Recibimos lista de síntomas
            List<Symptom> symptoms = receiveDataViaNetwork.receiveSymptoms();
            if (symptoms == null || symptoms.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No symptoms received from server",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 4. Mostramos lista de síntomas en un JList multiselección
            String[] symptomNames = new String[symptoms.size()];
            for (int i = 0; i < symptoms.size(); i++) {
                symptomNames[i] = (i + 1) + " - " + symptoms.get(i).getName(); // o toString()
            }

            JList<String> symptomJList = new JList<>(symptomNames);
            symptomJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            int option = JOptionPane.showConfirmDialog(
                    this,
                    new JScrollPane(symptomJList),
                    "Please select your symptoms (Ctrl+click para varios)",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (option != JOptionPane.OK_OPTION) {
                // El usuario canceló
                return;
            }

            int[] selectedIndices = symptomJList.getSelectedIndices();
            if (selectedIndices == null || selectedIndices.length == 0) {
                JOptionPane.showMessageDialog(this,
                        "You must select at least one symptom",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<Symptom> symptomsOfPatient = new ArrayList<>();
            for (int idx : selectedIndices) {
                // Aquí ya NO usamos ni Integer.parseInt ni Utilities.readInteger
                symptomsOfPatient.add(symptoms.get(idx));
            }

            // 5. Pedimos medicación en un input (separada por comas)
            String medsInput = JOptionPane.showInputDialog(
                    this,
                    "Insert the medication you have been using recently,\n" +
                            "separated by commas. Leave empty if you are not medicated:",
                    "Medication",
                    JOptionPane.PLAIN_MESSAGE
            );

            List<String> medicaments = new ArrayList<>();
            if (medsInput != null && !medsInput.trim().isEmpty()) {
                String[] parts = medsInput.split(",");
                for (String p : parts) {
                    String med = p.trim();
                    if (!med.isEmpty()) {
                        medicaments.add(med);
                    }
                }
            }

            // 6. Enviamos la información médica
            medicalInformation.setSymptoms(symptomsOfPatient);
            medicalInformation.setMedication(medicaments);

            sendDataViaNetwork.sendMedicalInformation(medicalInformation);
            String succesfull = receiveDataViaNetwork.receiveString();

            if ("RECEIVED MEDICAL INFORMATION".equals(succesfull)) {
                JOptionPane.showMessageDialog(this,
                        "Medical information successfully sent!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error sending medical information",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error in connection: " + e.getMessage(),
                    "Connection error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            // Aquí, si quieres, puedes llamar a tu releaseResources(...)
            // releaseResources(socket, sendDataViaNetwork, receiveDataViaNetwork);
        }
    }**/


    public void recordAndSendSignal(Patient patient, Socket socket, SendDataViaNetwork sendData, ReceiveDataViaNetwork receiveData) {
        try {
            // 1. Configuración de la grabación
            System.out.println("--- RECORD NEW SIGNAL ---");
            System.out.println("Select signal type:");
            System.out.println("1. Electromyography (EMG)");
            System.out.println("2. Accelerometer (ACC)");
            int typeOption = Utilities.readInteger("Option: ");

            TypeSignal typeSignal = null;
            if (typeOption == 1) {
                typeSignal = TypeSignal.EMG;
            } else if (typeOption == 2) {
                typeSignal = TypeSignal.ACC;
            } else {
                System.out.println("Invalid option. Cancelling.");
                return;
            }

            int seconds = Utilities.readInteger("Enter duration in seconds (e.g., 10): ");

            // Opcional: Pedir MAC o dejar vacío para búsqueda automática
            System.out.println("Enter BITalino MAC address (e.g., 20:17:...) or press ENTER to auto-search:");
            String macAddress = Utilities.readString("");
            if (macAddress.trim().isEmpty()) {
                macAddress = null; // Para que BitalinoService use DeviceDiscoverer
            }

            // 2. Usar el servicio para adquirir la señal
            // Instanciamos el servicio con la MAC (o null) y 100 Hz
            BitalinoService service = new BitalinoService(macAddress, 100);

            System.out.println("Starting acquisition... Please wait.");
            // Nota: Aquí pasamos patient.getId(). Asegúrate de que el objeto Patient tenga el ID cargado tras el login.
            // Si tu POJO Patient no tiene getId(), tendrás que añadirlo o gestionarlo.
            // Asumo que tu Patient tiene un método getId() o similar.
            int patientId = 0;
            // patientId = patient.getId(); // DESCOMENTAR SI TU CLASE PATIENT TIENE ID
            // Si no tiene ID en memoria, usa un valor temporal o arréglalo en el login.

            Signal signal = service.acquireSignal(typeSignal, patientId, seconds);

            System.out.println("Signal acquired! Samples recorded: " + signal.getValues().size());

            // 3. Enviar al servidor
            System.out.println("Sending to server...");

            // Enviamos el código de operación para "Enviar Señal" (asumimos que es el 3 en tu menú del servidor)
            sendData.sendInt(2);

            // Usamos el método que creamos en SendDataViaNetwork
            sendData.sendSignal(signal);

            System.out.println("Signal sent successfully!");

        } catch (Throwable e) {
            System.err.println("Error capturing/sending signal: " + e.getMessage());
            e.printStackTrace();
        }
    }








}
