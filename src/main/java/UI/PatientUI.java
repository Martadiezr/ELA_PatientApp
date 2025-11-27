package UI;

import BITalino.BITalino;
import BITalino.DeviceDiscoverer;
import BITalino.Frame;
import SendData.ReceiveDataViaNetwork;
import SendData.SendDataViaNetwork;
import pojos.*;

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
    Socket socket2 = null;

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



    public void sendSignal() {
        // Crear una instancia del objeto BITalino
        BITalino bitalinoDevice = new BITalino();
        DeviceDiscoverer deviceDiscoverer = new DeviceDiscoverer();
        try {
            // Buscar y conectar al dispositivo BITalino
            deviceDiscoverer.connectToBitalino();

            // Establecer los canales que deseas leer (por ejemplo, los primeros dos canales analógicos)
            int[] analogChannels = {0, 1};  // Modifica según los canales que necesites
            bitalinoDevice.start(analogChannels);

            // Leer la señal, por ejemplo, leer 100 muestras
            Frame[] frames = bitalinoDevice.read(100);  // Aquí se obtiene el array de frames (señal grabada)

            // Procesar los datos de la señal y enviarlos al servidor
            // Aquí puedes extraer la señal de los frames y enviarla
            sendRecordedSignalToServer(frames);  // Usamos 'frames' en lugar de 'signalData'

            // Detener el dispositivo BITalino después de grabar la señal
            bitalinoDevice.stop();
            deviceDiscoverer.closeConnection();  // Cerrar la conexión con el dispositivo
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Error al registrar y enviar la señal: " + e.getMessage());
        }
    }

    private void sendRecordedSignalToServer(Frame[] frames) {
        // Aquí necesitarás convertir los datos del Frame en un formato que el servidor pueda entender
        // Por ejemplo, puedes extraer los valores de los canales analógicos y enviarlos al servidor
        try {
            // Crear un objeto de SendDataViaNetwork para enviar los datos al servidor
            SendDataViaNetwork sendData = new SendDataViaNetwork(socket2);

            // Convertir los datos del Frame a un formato que el servidor pueda manejar (por ejemplo, un array de int o float)
            // Aquí estamos enviando solo los valores de los canales analógicos
            for (Frame frame : frames) {
                for (int i = 0; i < frame.analog.length; i++) {
                    sendData.sendInt(frame.analog[i]);  // Enviar cada valor de los canales analógicos
                }
            }
        } catch (IOException e) {
            System.err.println("Error al enviar la señal al servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void recordSignal() {
        // Crear una instancia del objeto BITalino
        BITalino bitalinoDevice = new BITalino();
        DeviceDiscoverer deviceDiscoverer = new DeviceDiscoverer();
        try {
            // Buscar y conectar al dispositivo BITalino
            deviceDiscoverer.connectToBitalino();

            // Establecer los canales que deseas leer (por ejemplo, los primeros dos canales analógicos)
            int[] analogChannels = {0, 1};  // Modifica según los canales que necesites
            bitalinoDevice.start(analogChannels);

            // Leer la señal, por ejemplo, leer 100 muestras
            Frame[] frames = bitalinoDevice.read(100);  // Aquí se obtiene el array de frames (señal grabada)

            // Enviar la señal grabada al servidor
            SendDataViaNetwork sendData = new SendDataViaNetwork(socket2);
            sendData.sendSignal(frames);  // Pasar los frames grabados al método sendSignal

            // Detener el dispositivo BITalino después de grabar la señal
            bitalinoDevice.stop();
            deviceDiscoverer.closeConnection();  // Cerrar la conexión con el dispositivo
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Error al grabar la señal: " + e.getMessage());
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
            sendDataViaNetwork.sendInt(4); // Indicar al servidor que se va a registrar medical information

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



}
