package UI;
import com.fazecast.jSerialComm.SerialPort;
import BITalino.BITalino;
import BITalino.BITalinoException;
import pojos.*;
import SendData.*;
import SendData.SendDataViaNetwork.*;
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;


public class Main {

    public static void main(String[] args) {
        Socket socket = null;
        SendDataViaNetwork sendDataViaNetwork = null;
        ReceiveDataViaNetwork receiveDataViaNetwork = null;
        boolean running = true;

        while (running) {
            String ipAdress = Utilities.readString("Write the IP address of the server you want to connect to:\n");
            try {
                socket = new Socket(ipAdress, 8000);
                sendDataViaNetwork = new SendDataViaNetwork(socket);
                receiveDataViaNetwork = new ReceiveDataViaNetwork(socket);
                sendDataViaNetwork.sendInt(1);
                String message = receiveDataViaNetwork.receiveString();
                System.out.println(message);

                if (message.equals("PATIENT")) {
                    while (running) {
                        switch (printLogInMenu()) {
                            case 1: {
                                //registerPatient(sendDataViaNetwork, receiveDataViaNetwork, socket);
                                break;
                            }
                            case 2: {
                                //logInMenu(sendDataViaNetwork, receiveDataViaNetwork, socket);
                                break;
                            }
                            case 3: {
                                sendDataViaNetwork.sendInt(3);
                                running = false;
                                break;
                            }
                            default: {
                                System.out.println("That number is not an option, try again");
                                break;
                            }
                        }
                    }
                } else {
                    System.out.println("Error in connection");
                }
            } catch (IOException e) {
                System.out.println("Invalid IP Address");
            }
        }

        System.out.println("Exiting...");
        //releaseResources(socket, sendDataViaNetwork, receiveDataViaNetwork);
        System.exit(0);
    }
    private static int printLogInMenu() {
        System.out.println("\n\nPatient Menu:\n"
                + "\n1. Register"
                + "\n2. Log In"
                + "\n3. Exit"
        );
        return Utilities.readInteger("What would you want to do?\n");
    }

    public static void clientPatientMenu(Patient patient_logedIn, Doctor assignedDoctor,SendDataViaNetwork sendDataViaNetwork,ReceiveDataViaNetwork receiveDataViaNetwork, Socket socket) {
        LocalDate date = LocalDate.now();
        //MedicalInformation medicalInformation = new MedicalInformation();
        boolean menu = true;
        while (menu) {
            switch (printClientMenu()) {
                case 1: {
                    //readSymptoms(interpretation, sendDataViaNetwork, receiveDataViaNetwork, socket);
                    break;
                }
                case 2: {
                    //readBITalino(interpretation, sendDataViaNetwork, receiveDataViaNetwork, socket);
                    break;
                }
                case 3: {
                    System.out.println(patient_logedIn);
                    break;
                }
                case 4: {
                    //seeInterpretations(sendDataViaNetwork, receiveDataViaNetwork, socket);
                    break;
                }
                case 5: {
                    menu = false;
                    //sendInterpretationAndLogOut(interpretation, sendDataViaNetwork, receiveDataViaNetwork, socket);
                    break;
                }
            }
        }

    }

    private static int printClientMenu(){
        System.out.println("\n\nDiagnosis Menu:\n"
                + "\n1. Input your symptoms"
                + "\n2. Record Signal with BITalino"
                + "\n3. See your data"
                + "\n4. See your reports"
                + "\n5. Log out"
        );
        return Utilities.readInteger("What would you want to do?\n");
    }


}
