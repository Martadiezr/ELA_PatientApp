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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Patient {
    Socket socket = null;
    public void register() throws IOException {
        // Crear un objeto Patient y obtener los datos del paciente
        Scanner scanner = new Scanner(System.in);
        pojos.Patient patient = new pojos.Patient();

        System.out.println("Enter your name: ");
        patient.setName(scanner.nextLine());

        System.out.println("Enter your surname: ");
        patient.setSurname(scanner.nextLine());

        System.out.println("Enter your DNI: ");
        patient.setDni(scanner.nextLine());

        System.out.println("Enter your date of birth (YYYY-MM-DD): ");
        String dateOfBirthStr = scanner.nextLine();
        Date dateOfBirth = Date.valueOf(dateOfBirthStr);
        patient.setDateOfBirth(dateOfBirth);

        System.out.println("Enter your sex (M/F): ");
        patient.setSex(scanner.nextLine());

        System.out.println("Enter your phone number: ");
        patient.setPhone(scanner.nextInt());

        scanner.nextLine(); // Consume newline character

        System.out.println("Enter your email: ");
        patient.setEmail(scanner.nextLine());

        System.out.println("Enter your insurance number: ");
        patient.setInsurance(scanner.nextInt());

        // Ahora, usar la clase SendDataViaNetwork para enviar los datos del paciente al servidor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        sendData.sendPatient(patient);  // Enviar los datos del paciente al servidor

        System.out.println("Registration successful!");
    }


    public void logIn() throws IOException {
        // Crear un objeto Scanner para obtener las credenciales
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your username: ");
        String username = scanner.nextLine();

        System.out.println("Enter your password: ");
        String password = scanner.nextLine();

        // Usar la clase SendDataViaNetwork para enviar las credenciales al servidor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        sendData.sendStrings(username);  // Enviar nombre de usuario
        sendData.sendStrings(password);  // Enviar contraseña

        // Ahora, recibir la respuesta del servidor sobre el login
        ReceiveDataViaNetwork receiveData = new ReceiveDataViaNetwork(socket);
        int loginResponse = receiveData.receiveInt();  // Recibir respuesta del servidor (1 para éxito, 0 para error)

        if (loginResponse == 1) {
            System.out.println("Login successful!");
            PatientApp.menuPaciente();  // Llamar al menú de opciones del paciente
        } else {
            System.out.println("Invalid credentials, please try again.");
        }
    }

    public void insertMedicalInformation() throws IOException {
        // Crear una instancia del objeto MedicalInformation
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the report date (YYYY-MM-DD): ");
        String reportDateStr = scanner.nextLine();
        java.sql.Date reportDate = java.sql.Date.valueOf(reportDateStr);

        // Ingresar síntomas
        System.out.println("Enter symptoms (comma-separated): ");
        String symptomsInput = scanner.nextLine();
        List<Symptom> symptoms = new ArrayList<>();
        for (String symptomName : symptomsInput.split(",")) {
            Symptom symptom = new Symptom();  // Crear un nuevo objeto Symptom
            symptom.setDescription(symptomName.trim());  // Suponiendo que Symptom tiene un campo `name`
            symptoms.add(symptom);
        }

        // Ingresar medicamentos
        System.out.println("Enter medications (comma-separated): ");
        String medicationInput = scanner.nextLine();
        List<String> medication = Arrays.asList(medicationInput.split(","));

        // Crear el objeto MedicalInformation con los datos proporcionados
        MedicalInformation medicalInfo = new MedicalInformation(null, symptoms, reportDate, medication, null);  // Feedback es null al principio

        // Usar la clase SendDataViaNetwork para enviar la información médica al servidor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        sendData.sendMedicalInformation(medicalInfo);  // Enviar la información médica al servidor

        System.out.println("Medical information successfully sent!");
    }

    public void seeDoctorFeedback() throws IOException {
        // Usar la clase ReceiveDataViaNetwork para recibir la retroalimentación del doctor desde el servidor
        ReceiveDataViaNetwork receiveData = new ReceiveDataViaNetwork(socket);
        String feedback = receiveData.receiveString();  // Recibir el feedback como una cadena

        // Mostrar el feedback recibido
        System.out.println("Doctor's feedback: " + feedback);
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
            SendDataViaNetwork sendData = new SendDataViaNetwork(socket);

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
            SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
            sendData.sendSignal(frames);  // Pasar los frames grabados al método sendSignal

            // Detener el dispositivo BITalino después de grabar la señal
            bitalinoDevice.stop();
            deviceDiscoverer.closeConnection();  // Cerrar la conexión con el dispositivo
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Error al grabar la señal: " + e.getMessage());
        }
    }



}
