package UI;

import BITalino.*;
import SendData.ReceiveDataViaNetwork;
import SendData.SendDataViaNetwork;
import pojos.*;

import javax.swing.*;
import java.awt.*;
import java.awt.Frame;
import java.io.IOException;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Handles the business logic and network communication for the Patient side.
 * Contains methods for registration, login, data insertion, signal recording, and feedback retrieval.
 */
public class PatientUI {
    // Holds the Patient object after a successful login
    private Patient loggedInPatient;

    /**
     * Console-based method for patient registration.
     * @param socket The network socket.
     * @param sendDataViaNetwork The data sender.
     * @param receiveDataViaNetwork The data receiver.
     * @throws IOException If a network error occurs.
     */
    public void register(Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {
        try {
            sendDataViaNetwork.sendInt(2); // Signal the server that registration is about to start

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
            byte[] passwordBytes = password.getBytes();


            if (passwordBytes != null) {
                sendDataViaNetwork.sendStrings("OK");
                User user = new User(email, passwordBytes, role);
                System.out.println(patient);
                System.out.println(user);
                // 1. Send patient details
                sendDataViaNetwork.sendPatient(patient);
                // 2. Send user/login details
                sendDataViaNetwork.sendUser(user);
                // Wait for server response
                if(receiveDataViaNetwork.receiveString().equals("SUCCESS")){
                    System.out.println("Patient registered successfully.");
                    // Move to the patient menu (assuming PatientApp.menuPaciente exists)
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


    /**
     * Console-based method for patient login.
     * @throws IOException If a network error occurs.
     */
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
                System.out.println(response); // "SUCCESS" or "ERROR"

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

    /** Helper method to close streams and the socket. */
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

    /**
     * Console-based method to insert medical information (symptoms and medication).
     * @throws IOException If a network error occurs.
     */
    public void insertMedicalInformation(Patient patient, Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {
        try {
            sendDataViaNetwork.sendInt(1); // Operation code: Register medical information

            MedicalInformation medicalInformation = new MedicalInformation();

            Date dateReport = Date.valueOf(java.time.LocalDate.now());
            medicalInformation.setReportDate(dateReport);
            // 1. Request available symptoms from the server
            sendDataViaNetwork.sendStrings("SEND SYMPTOMS");
            String message = receiveDataViaNetwork.receiveString();
            if(message.equals("OK")){
                // 2. Receive the list of all available symptoms
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

    /**
     * Console-based method to see doctor's feedback on a selected report.
     * @throws IOException If a network error occurs.
     */
public void seeDoctorFeedback(Patient patient, Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {
    try {
        sendDataViaNetwork.sendInt(3); // Operation code: Request feedback

        String message = "REQUEST FEEDBACK";
        sendDataViaNetwork.sendStrings(message);

        String response = receiveDataViaNetwork.receiveString();
        System.out.println("response from server : " + response);

        if (response.equals("OK")) {

            // 1. Receive number of reports
            int count = receiveDataViaNetwork.receiveInt();
            if (count == 0) {
                System.out.println("You do not have any medical reports yet.");
                PatientApp.menuPaciente(patient, sendDataViaNetwork, receiveDataViaNetwork, socket);
                return;
            }

            // 2. Receive the list of report dates (Strings)
            List<String> dates = new ArrayList<>();
            System.out.println("Available medical reports:");
            for (int i = 0; i < count; i++) {
                String dateStr = receiveDataViaNetwork.receiveString();
                dates.add(dateStr);
                System.out.println((i + 1) + ". " + dateStr);
            }

            // 3. Console-based selection of report index
            int choice = Utilities.readInteger("Select a report number to view its feedback (1-" + count + "): ");

            if (choice < 1 || choice > count) {
                System.out.println("Invalid selection, operation cancelled.");
                sendDataViaNetwork.sendInt(-1);
                PatientApp.menuPaciente(patient, sendDataViaNetwork, receiveDataViaNetwork, socket);
                return;
            }

            // 4. Send the selection (1-based index) to the server
            sendDataViaNetwork.sendInt(choice);

            // 5. Receive the selected MedicalInformation object
            MedicalInformation medicalInformation = receiveDataViaNetwork.receiveMedicalInformation();

            if (medicalInformation != null) {
                sendDataViaNetwork.sendStrings("RECEIVED MEDICAL INFORMATION");
                String feedback = medicalInformation.getFeedback();
                System.out.println("The feedback from the doctor is:");
                System.out.println(feedback);
            } else {
                sendDataViaNetwork.sendStrings("ERROR");
                System.out.println("Error: medical information could not be received.");
            }

            // Volver al menú
            PatientApp.menuPaciente(patient, sendDataViaNetwork, receiveDataViaNetwork, socket);

        } else {
            System.out.println("Could not fetch the feedback from the server");
        }

    } catch (Exception e) {
        System.out.println("Error in connection");
        releaseResources(socket, sendDataViaNetwork, receiveDataViaNetwork);
        System.exit(0);
    }
}




    public Patient getLoggedInPatient() {
        return loggedInPatient;
    }

    /**
     * GUI-based logic for patient login.
     * @return true if login succeeds, false otherwise.
     * @throws IOException If a network error occurs.
     */
    public boolean logInFromGUI(
            String username,
            String password,
            Socket socket,
            SendDataViaNetwork sendDataViaNetwork,
            ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {

        sendDataViaNetwork.sendInt(1); // login

        String serverMsg = receiveDataViaNetwork.receiveString();
        System.out.println("Server says: " + serverMsg);

        // Prepare User object
        byte[] passwordBytes = password.getBytes();
        Role role = new Role("Patient");
        User user = new User(username, passwordBytes, role);

        sendDataViaNetwork.sendStrings("OK");
        sendDataViaNetwork.sendUser(user); // Send login credentials

        String response = receiveDataViaNetwork.receiveString(); // "SUCCESS" o "ERROR"
        if (!"SUCCESS".equals(response)) {
            return false;
        }

        // Receive Patient object and set the loggedInPatient field
        Patient patient = receiveDataViaNetwork.recievePatient();
        System.out.println("Patient logged in: " + patient);

        if (patient != null) {
            // AQUÍ ES DONDE REALMENTE “NACE” loggedInPatient
            this.loggedInPatient = patient;
            return true;
        } else {
            return false;
        }
    }

    /**
     * GUI-based logic for patient registration.
     * @return true if registration succeeds, false otherwise.
     * @throws IOException If a network error occurs.
     */
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

        // Create and populate Patient and User objects
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

    /**
     * GUI-based method to insert medical information (symptoms and medication) using JOptionPane.
     */
   public void insertMedicalInformationGUI(Patient patient, Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) {
        try {
            // 1. Request and receive list of all available symptoms
            sendDataViaNetwork.sendInt(1);

            MedicalInformation medicalInformation = new MedicalInformation();
            Date dateReport = Date.valueOf(java.time.LocalDate.now());
            medicalInformation.setReportDate(dateReport);

            // 2. Show JList for multi-selection of symptoms
            sendDataViaNetwork.sendStrings("SEND SYMPTOMS");
            String message = receiveDataViaNetwork.receiveString();

            if (!"OK".equals(message)) {
                JOptionPane.showMessageDialog(null,
                        "Could not fetch the symptoms from the server",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Process selected symptoms
            List<Symptom> symptoms = receiveDataViaNetwork.receiveSymptoms();
            if (symptoms == null || symptoms.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "No symptoms received from server",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 4. Request medication input via JOptionPane
            String[] symptomNames = new String[symptoms.size()];
            for (int i = 0; i < symptoms.size(); i++) {
                symptomNames[i] = (i + 1) + " - " + symptoms.get(i).getDescription(); // o toString()
            }

            JList<String> symptomJList = new JList<>(symptomNames);
            symptomJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            int option = JOptionPane.showConfirmDialog(
                    null,
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
                JOptionPane.showMessageDialog(null,
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

                    // 5. Process medication input (split by comma)
                    String medsInput = JOptionPane.showInputDialog(
                    null,
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

            // 6. Send the medical information to the server
            medicalInformation.setSymptoms(symptomsOfPatient);
            medicalInformation.setMedication(medicaments);

            sendDataViaNetwork.sendMedicalInformation(medicalInformation);
            String succesfull = receiveDataViaNetwork.receiveString();

            if ("RECEIVED MEDICAL INFORMATION".equals(succesfull)) {
                JOptionPane.showMessageDialog(null,
                        "Medical information successfully sent!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Error sending medical information",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error in connection: " + e.getMessage(),
                    "Connection error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            // Aquí, si quieres, puedes llamar a tu releaseResources(...)
            // releaseResources(socket, sendDataViaNetwork, receiveDataViaNetwork);
        }
    }


    public void recordAndSendSignal(Patient patient, Socket socket, SendDataViaNetwork sendData, ReceiveDataViaNetwork receiveData) {
        try {
            System.out.println("--- RECORD NEW SIGNAL ---");
            System.out.println("Select signal type:");
            System.out.println("1. Electromyography (EMG)");
            System.out.println("2. Electrocardiogram (ECG)");
            int typeOption = Utilities.readInteger("Option: ");

            TypeSignal typeSignal = null;
            if (typeOption == 1) {
                typeSignal = TypeSignal.EMG;
            } else if (typeOption == 2) {
                typeSignal = TypeSignal.ECG;
            } else {
                System.out.println("Invalid option. Cancelling.");
                return;
            }

            int seconds = Utilities.readInteger("Enter duration in seconds (e.g., 10): ");

            System.out.println("Enter BITalino MAC address (e.g., 20:17:...) or press ENTER to auto-search:");
            String macAddress = Utilities.readString("");
            if (macAddress.trim().isEmpty()) {
                macAddress = null;
            }

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                System.out.println("WINDOWS");
                // 5. Acquire signal data using BitalinoService
                BitalinoServiceWindows service = new BitalinoServiceWindows(macAddress, 100);
                int patientId = patient.getId();

                Signal signal = service.acquireSignal(typeSignal, patientId, seconds);


                if (signal == null || signal.getValues() == null || signal.getValues().isEmpty()) {
                    System.out.println("ERROR sending signal");
                    return;
                }else{
                    System.out.println("Signal acquired! Samples recorded: " + signal.getValues().size());
                    System.out.println("Sending to server...");
                    sendData.sendInt(2);
                    sendData.sendSignal(signal);
                    System.out.println("Signal sent successfully!");
                }




            }else if(os.contains("mac") || os.contains("nix") || os.contains("nux")) {
                System.out.println("MAC/LINUX");
                // 5. Acquire signal data using BitalinoService
                BitalinoService service = new BitalinoService(macAddress, 100);
                int patientId = patient.getId();

                Signal signal = service.acquireSignal(typeSignal, patientId, seconds);


                if (signal == null || signal.getValues() == null || signal.getValues().isEmpty()) {
                    System.out.println("ERROR sending signal");
                    return;
                }else{
                    System.out.println("Signal acquired! Samples recorded: " + signal.getValues().size());
                    System.out.println("Sending to server...");
                    sendData.sendInt(2);
                    sendData.sendSignal(signal);
                    System.out.println("Signal sent successfully!");
                }




            }


        } catch (Throwable e) {
            System.err.println("Error capturing/sending signal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * GUI-based method to see doctor's feedback, showing selection via JOptionPane.
     */
    public void seeDoctorFeedbackGUI(Patient patient,
                                     Socket socket,
                                     SendDataViaNetwork sendDataViaNetwork,
                                     ReceiveDataViaNetwork receiveDataViaNetwork) {
        try {
            // 1. Indicamos al servidor que queremos ver feedback del médico
            sendDataViaNetwork.sendInt(3); // mismo código que ya usas

            String message = "REQUEST FEEDBACK";
            sendDataViaNetwork.sendStrings(message);

            String response = receiveDataViaNetwork.receiveString();
            System.out.println("response from server : " + response);

            if (!"OK".equals(response)) {
                JOptionPane.showMessageDialog(null,
                        "Could not fetch the feedback from the server.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Receive number of reports and the list of report lines
            int count = receiveDataViaNetwork.receiveInt();
            if (count == 0) {
                JOptionPane.showMessageDialog(null,
                        "You do not have any medical reports yet.",
                        "No reports",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 3. Show selection dialog
            List<String> reportLines = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String line = receiveDataViaNetwork.receiveString();
                reportLines.add(line);
            }

            // 4. Send the selection (1-based index)
            String[] options = reportLines.toArray(new String[0]);

            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Select a report to see its feedback:",
                    "Medical reports",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice < 0) {
                // Usuario cerró o canceló
                sendDataViaNetwork.sendInt(-1);
                return;
            }

            // El servidor espera índice 1..count
            sendDataViaNetwork.sendInt(choice + 1);

            // 5. Receive the selected MedicalInformation object
            MedicalInformation medicalInformation = receiveDataViaNetwork.receiveMedicalInformation();

            if (medicalInformation == null) {
                sendDataViaNetwork.sendStrings("ERROR");
                JOptionPane.showMessageDialog(null,
                        "Error: medical information could not be received.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Confirmamos recepción al servidor
            sendDataViaNetwork.sendStrings("RECEIVED MEDICAL INFORMATION");

            StringBuilder sb = new StringBuilder();
            sb.append("==== DOCTOR FEEDBACK ====\n\n");
            sb.append("Report date: ").append(medicalInformation.getReportDate()).append("\n\n");

            List<Symptom> symptoms = medicalInformation.getSymptoms();
            if (symptoms != null && !symptoms.isEmpty()) {
                sb.append("Symptoms:\n");
                for (Symptom s : symptoms) {
                    sb.append(" - ").append(s.getDescription()).append("\n");
                }
                sb.append("\n");
            } else {
                sb.append("Symptoms: none\n\n");
            }

            String feedback = medicalInformation.getFeedback();
            if (feedback == null || feedback.trim().isEmpty()) {
                feedback = "No feedback available yet for this report.";
            }

            sb.append("Doctor feedback:\n");
            sb.append(feedback);
            sb.append("\n\n==========================\n");

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(450, 250));

            JOptionPane.showMessageDialog(
                    null,
                    scrollPane,
                    "Doctor's feedback",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error in connection: " + e.getMessage(),
                    "Connection error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Console-based method to change patient data.
     * Allows selection of a single field to modify.
     */
    public void changePatientData(Patient patient, Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Mostrar las opciones al doctor
        System.out.println("What information would you like to change?");
        System.out.println("1 - Name");
        System.out.println("2 - Surname");
        System.out.println("3 - Phone");
        System.out.println("4 - Email");
        System.out.println("5 - DNI");
        System.out.println("6 - Sex");
        System.out.println("7- Insurance");
        System.out.println("0 - Exit");

        // Leer la opción seleccionada por el doctor
        int choice = scanner.nextInt();
        scanner.nextLine();

        // Variables para los nuevos valores
        String newName = null, newSurname = null,  newEmail = null;
        String newdni= null, newSex=null;
        Integer newInsurance = null, newPhone= null;

        switch (choice) {
            case 1:
                System.out.print("Enter new name: ");
                newName = scanner.nextLine();
                break;
            case 2:
                System.out.print("Enter new surname: ");
                newSurname = scanner.nextLine();
                break;
            case 3:
                System.out.print("Enter new phone number: ");
                String newPhoneStr = scanner.nextLine();
                try {
                    newPhone = Integer.parseInt(newPhoneStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid insurance number. Please enter a valid number.");
                    return;
                }
                break;
            case 4:
                System.out.print("Enter new email: ");
                newEmail = scanner.nextLine();
                break;
            case 5:
                System.out.print("Enter new DNI: ");
                newdni= scanner.nextLine();
                break;
            case 6:
                System.out.print("Enter new Sex: ");
                newSex= scanner.nextLine();
                break;
            case 7:
                System.out.print("Enter new insurance number: ");
                String newInsuranceStr = scanner.nextLine();
                try {
                    newInsurance = Integer.parseInt(newInsuranceStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid insurance number. Please enter a valid number.");
                    return;
                }
                break;
            case 0:
                System.out.println("Exiting...");
                return;
            default:
                System.out.println("Invalid choice, please try again.");
                return;
        }

        sendDataViaNetwork.sendInt(patient.getId());

        if (newName != null) {
            sendDataViaNetwork.sendStrings(newName);
        } else {
            sendDataViaNetwork.sendStrings("");
        }

        if (newSurname != null) {
            sendDataViaNetwork.sendStrings(newSurname);
        } else {
            sendDataViaNetwork.sendStrings("");
        }

        if (newPhone != null) {
            sendDataViaNetwork.sendInt(newPhone);  // Solo enviar el nuevo teléfono si fue modificado
        } else {
            sendDataViaNetwork.sendInt(-1);  // Enviar una cadena vacía si no se modificó
        }

        if (newEmail != null) {
            sendDataViaNetwork.sendStrings(newEmail);  // Solo enviar el nuevo email si fue modificado
        } else {
            sendDataViaNetwork.sendStrings("");  // Enviar una cadena vacía si no se modificó
        }
        if(newdni != null) {
            sendDataViaNetwork.sendStrings(newdni);
        }else{
            sendDataViaNetwork.sendStrings("");
        }
        if(newSex != null) {
            sendDataViaNetwork.sendStrings(newSex);
        }else{
            sendDataViaNetwork.sendStrings("");
        }

        if (newInsurance != null) {
            sendDataViaNetwork.sendInt(newInsurance);  // Solo enviar el nuevo seguro si fue modificado
        } else {
            sendDataViaNetwork.sendInt(-1);  // Enviar -1 si no se modificó
        }

        // Recibir la respuesta del servidor
        String response = receiveDataViaNetwork.receiveString();
        System.out.println(response);  // Mostrar la respuesta del servidor
    }

    /**
     * GUI-based method to change patient data using JOptionPane.
     * @return The response string from the server (success or error message).
     * @throws IOException If a network error occurs.
     */
    public String changePatientDataFromGUI(
            Patient patient,
            Socket socket,
            ReceiveDataViaNetwork receiveDataViaNetwork,
            SendDataViaNetwork sendDataViaNetwork,
            java.awt.Component parent) throws IOException {

        // 1. Show a selection dialog for the field to change
        String[] options = { "Name", "Surname", "Phone", "DNI", "Sex", "Insurance" };
        String choice = (String) JOptionPane.showInputDialog(
                parent,
                "What information would you like to change?",
                "Change patient data",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == null) return "Operation cancelled";

        String newName = null, newSurname = null, newDni = null, newSex = null;
        Integer newPhone = null, newInsurance = null;

        // 2. Read the new value based on the selected field
        switch (choice) {
            case "Name":
                newName = JOptionPane.showInputDialog(parent, "Enter new name:");
                if (newName == null) return "Operation cancelled";
                break;
            case "Surname":
                newSurname = JOptionPane.showInputDialog(parent, "Enter new surname:");
                if (newSurname == null) return "Operation cancelled";
                break;
            case "Phone":
                String phoneStr = JOptionPane.showInputDialog(parent, "Enter new phone:");
                if (phoneStr == null) return "Operation cancelled";
                try {
                    newPhone = Integer.parseInt(phoneStr);
                } catch (NumberFormatException e) {
                    return "Invalid phone number.";
                }
                break;
            case "DNI":
                newDni = JOptionPane.showInputDialog(parent, "Enter new DNI:");
                if (newDni == null) return "Operation cancelled";
                break;
            case "Sex":
                newSex = JOptionPane.showInputDialog(parent, "Enter new sex:");
                if (newSex == null) return "Operation cancelled";
                break;
            case "Insurance":
                String insuranceStr = JOptionPane.showInputDialog(parent, "Enter new insurance:");
                if (insuranceStr == null) return "Operation cancelled";
                try {
                    newInsurance = Integer.parseInt(insuranceStr);
                } catch (NumberFormatException e) {
                    return "Invalid insurance number.";
                }
                break;
        }

        // 3. Send operation code and patient ID
        sendDataViaNetwork.sendInt(4);
        sendDataViaNetwork.sendInt(patient.getId());

        // 4. Send all fields: modified value or placeholder (empty string / -1)
        if (newName != null) sendDataViaNetwork.sendStrings(newName); else sendDataViaNetwork.sendStrings("");
        if (newSurname != null) sendDataViaNetwork.sendStrings(newSurname); else sendDataViaNetwork.sendStrings("");
        if (newPhone != null) sendDataViaNetwork.sendInt(newPhone); else sendDataViaNetwork.sendInt(-1);
        if (newDni != null) sendDataViaNetwork.sendStrings(newDni); else sendDataViaNetwork.sendStrings("");
        if (newSex != null) sendDataViaNetwork.sendStrings(newSex); else sendDataViaNetwork.sendStrings("");
        if (newInsurance != null) sendDataViaNetwork.sendInt(newInsurance); else sendDataViaNetwork.sendInt(-1);

        // 5. Receive and return the server response
        String response = receiveDataViaNetwork.receiveString();
        return response;
    }

    /**
     * GUI-based method to record and send a signal, showing progress and options via JOptionPane.
     */
    public void recordAndSendSignalGUI(Patient patient,
                                       Socket socket,
                                       SendDataViaNetwork sendData,
                                       ReceiveDataViaNetwork receiveData,
                                       Component parent) {
        try {
            // 1. Select signal type (EMG/ECG) via dialog
            String[] options = { "Electromyography (EMG)", "Electrocardiogram (ECG)" };
            int typeOption = JOptionPane.showOptionDialog(
                    parent,
                    "Select signal type:",
                    "Record new signal",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (typeOption < 0) return;

            TypeSignal typeSignal = (typeOption == 0) ? TypeSignal.EMG : TypeSignal.ECG;

            // 2. Enter duration
            String secondsStr = JOptionPane.showInputDialog(
                    parent,
                    "Enter duration in seconds (e.g., 10):",
                    "Duration",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (secondsStr == null || secondsStr.trim().isEmpty()) return;

            int seconds;
            try {
                seconds = Integer.parseInt(secondsStr.trim());
                if (seconds <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parent, "Invalid duration.", "Input error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Enter optional BITalino MAC
            String macAddress = JOptionPane.showInputDialog(
                    parent,
                    "Enter BITalino MAC address (or leave empty):",
                    "BITalino MAC",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (macAddress != null) {
                macAddress = macAddress.trim();
                if (macAddress.isEmpty()) macAddress = null;
            }

            JOptionPane.showMessageDialog(parent, "Recording will start for " + seconds + " seconds.", "Recording", JOptionPane.INFORMATION_MESSAGE);

            // 4. Show recording progress dialog
            JDialog recordingDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Recording...", false);
            JLabel statusLabel = new JLabel("Recording... " + seconds + " s remaining");
            JProgressBar progressBar = new JProgressBar(0, seconds);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.add(statusLabel, BorderLayout.NORTH);
            panel.add(progressBar, BorderLayout.CENTER);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            recordingDialog.getContentPane().add(panel);
            recordingDialog.pack();
            recordingDialog.setLocationRelativeTo(parent);

            final int[] elapsed = {0};
            javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
                elapsed[0]++;
                int remaining = seconds - elapsed[0];
                if (remaining < 0) remaining = 0;
                statusLabel.setText("Recording... " + remaining + " s remaining");
                progressBar.setValue(elapsed[0]);
                if (elapsed[0] >= seconds) ((javax.swing.Timer) e.getSource()).stop();
            });

            SwingUtilities.invokeLater(() -> {
                recordingDialog.setVisible(true);
                timer.start();
            });

            // 5. Acquire signal
            Signal signal = null;
            String os = System.getProperty("os.name").toLowerCase();

            try {
                if (os.contains("win")) {
                    BitalinoServiceWindows service = new BitalinoServiceWindows(macAddress, 100);
                    signal = service.acquireSignal(typeSignal, patient.getId(), seconds);
                } else {
                    BitalinoService service = new BitalinoService(macAddress, 100);
                    signal = service.acquireSignal(typeSignal, patient.getId(), seconds);
                }
            } catch (Exception ex) {
                // Si falla bitalino, cerramos dialogo
                SwingUtilities.invokeLater(() -> {
                    timer.stop();
                    recordingDialog.dispose();
                });
                throw ex;
            }

            // 6. Close dialog
            SwingUtilities.invokeLater(() -> {
                timer.stop();
                recordingDialog.dispose();
            });

            if (signal == null || signal.getValues() == null || signal.getValues().isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No signal was recorded.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 7. SEND SIGNAL TO SERVER
            // Enviamos OpCode (2) y la señal
            sendData.sendInt(2);
            sendData.sendSignal(signal);

            // === CAMBIO IMPORTANTE AQUI ===
            // Debemos ESPERAR la confirmación del servidor ("OK")
            // Si no leemos esto, el socket se queda desincronizado para la siguiente acción.
            String response = receiveData.receiveString();

            if ("OK".equals(response)) {
                JOptionPane.showMessageDialog(parent, "Signal sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parent, "Server error saving signal.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            // ==============================

        } catch (Throwable e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error capturing/sending signal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}




