package UI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PatientGUI {
    public static void main(String[] args) {
        // Crear la ventana principal
        JFrame frame = new JFrame("Telemedicina - Paciente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        // Crear panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Crear menú de opciones
        JButton registerButton = new JButton("Registrar Información Médica");
        JButton recordSignalButton = new JButton("Grabar Señal");
        JButton sendReportButton = new JButton("Enviar Reporte");
        JButton feedbackButton = new JButton("Ver Feedback y Recetas");

        // Agregar los botones al panel
        panel.add(registerButton);
        panel.add(recordSignalButton);
        panel.add(sendReportButton);
        panel.add(feedbackButton);

        // Acciones de los botones
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRegistrationForm();
            }
        });

        recordSignalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRecordingForm();
            }
        });

        sendReportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendReport();
            }
        });

        feedbackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewFeedback();
            }
        });

        // Agregar panel a la ventana
        frame.add(panel);
        frame.setVisible(true);
    }

    // Métodos para mostrar cada pantalla (opciones del menú)

    private static void showRegistrationForm() {
        JFrame frame = new JFrame("Registrar Información Médica");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel symptomsLabel = new JLabel("Síntomas:");
        JTextField symptomsField = new JTextField();

        JLabel medicineLabel = new JLabel("Medicamentos:");
        JTextField medicineField = new JTextField();

        JLabel dateLabel = new JLabel("Fecha:");
        JTextField dateField = new JTextField();

        JButton sendButton = new JButton("Enviar");

        panel.add(symptomsLabel);
        panel.add(symptomsField);
        panel.add(medicineLabel);
        panel.add(medicineField);
        panel.add(dateLabel);
        panel.add(dateField);

        panel.add(sendButton);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Aquí puedes agregar la lógica para enviar los datos a la base de datos
                System.out.println("Información enviada: " + symptomsField.getText());
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void showRecordingForm() {
        JFrame frame = new JFrame("Grabar Señal");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        JLabel instructionLabel = new JLabel("Conecte los electrodos y presione 'Grabar'");

        JButton startRecordingButton = new JButton("Grabar Señal");
        JButton stopRecordingButton = new JButton("Detener Grabación");

        panel.add(instructionLabel);
        panel.add(startRecordingButton);
        panel.add(stopRecordingButton);

        startRecordingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Grabando señal...");
            }
        });

        stopRecordingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Señal detenida.");
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void sendReport() {
        // Aquí podrías agregar la lógica para enviar el reporte del paciente
        System.out.println("Reporte enviado.");
    }

    private static void viewFeedback() {
        // Aquí puedes agregar la lógica para mostrar el feedback del doctor o las recetas
        System.out.println("Mostrando feedback y recetas.");
    }
}
