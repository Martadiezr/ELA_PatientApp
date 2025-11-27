package UI;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import SendData.SendDataViaNetwork;
import SendData.ReceiveDataViaNetwork;
import pojos.*;

public class PatientApp {


    public static void main(String[] args) {
        boolean running = true;
        Scanner scanner = new Scanner(System.in);

        // Establecer conexión con el servidor
        while (running) {
            String ipAddress = Utilities.readString("Enter the IP address of the server to connect to:\n");
            try {
                Socket socket = new Socket("localhost", 8000);
                SendDataViaNetwork sendDataViaNetwork = new SendDataViaNetwork(socket);
                ReceiveDataViaNetwork receiveDataViaNetwork = new ReceiveDataViaNetwork(socket);
                sendDataViaNetwork.sendInt(1);  // Se asume que se está enviando un código para verificar la conexión
                String message = receiveDataViaNetwork.receiveString();
                System.out.println(message);

                if (message.equals("PATIENT")) {
                    // Proceder con las opciones del paciente
                    showPatientMenu(socket, sendDataViaNetwork, receiveDataViaNetwork);
                } else {
                    System.out.println("Server response invalid. Try again.");
                }
            } catch (IOException e) {
                System.out.println("Connection failed: " + e.getMessage());
                running = false;  // Salir si no se puede conectar
            }
        }
    }

    // Menú principal del paciente
    public static void showPatientMenu(Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) {
        boolean running = true;
        Scanner scanner = new Scanner(System.in);

        // Crear un único objeto Patient que será usado en todos los casos
        PatientUI patientUI = new PatientUI();

        while (running) {
            System.out.println("1- Log in");
            System.out.println("2- Sign up");
            System.out.println("0- Exit");
            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    try {
                        patientUI.logIn(socket, sendDataViaNetwork, receiveDataViaNetwork);  // Llama al método logIn() en la clase Paciente
                    } catch (IOException e) {
                        System.out.println("Error during login: " + e.getMessage());
                    }
                    break;
                case 2:
                    try {
                        patientUI.register(socket, sendDataViaNetwork, receiveDataViaNetwork);  // Llama al método register() en la clase Paciente
                    } catch (IOException e) {
                        System.out.println("Error during registration: " + e.getMessage());
                    }
                    break;
                case 0:
                    System.out.println("Exiting...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

    // Método para mostrar el menú de opciones del paciente después de un inicio de sesión exitoso
    public static void menuPaciente(Patient patientInServer, SendDataViaNetwork sendDataViaNetwork,ReceiveDataViaNetwork receiveDataViaNetwork, Socket socket) throws IOException {
        boolean running = true;
        Scanner scanner = new Scanner(System.in);
        PatientUI patient = new PatientUI();

        while (running) {
            System.out.println("1- Insert medical information");
            System.out.println("2- Record signal");
            System.out.println("3- Send signal");
            System.out.println("4- See doctor’s feedback");
            System.out.println("0- Exit");
            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    patient.insertMedicalInformation(patientInServer, socket, sendDataViaNetwork, receiveDataViaNetwork);  // Llama a insertMedicalInformation() en la clase Paciente
                    break;
                case 2:
                    patient.recordSignal();  // Llama a recordSignal() en la clase Paciente
                    break;
                case 3:
                    patient.sendSignal();  // Llama a sendSignal() en la clase Paciente
                    break;
                case 4:
                    patient.seeDoctorFeedback(patientInServer, socket, sendDataViaNetwork, receiveDataViaNetwork);  // Llama a seeDoctorFeedback() en la clase Paciente
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option, try again.");
                    break;
            }
        }
    }

}
