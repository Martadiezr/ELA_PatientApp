package UI;
import SendData.SendDataViaNetwork;
import pojos.Symptom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PatientGUI extends JFrame {
    private PatientClientContext context;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Paneles
    private JPanel authPanel;
    private JPanel selectPatientPanel;
    private JPanel menuPanel;

    private JTextArea patientListArea;
    private JTextField patientIdField;

    private Integer currentPatientId = null;

    public PatientGUI(PatientClientContext context) {
        super("Telemedicine - Paciente");
        this.context = context;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        authPanel = createAuthPanel();
        menuPanel = createMenuPanel();

        mainPanel.add(authPanel, "AUTH");
        mainPanel.add(menuPanel, "MENU");

        setContentPane(mainPanel);
        cardLayout.show(mainPanel, "AUTH");

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                PatientClientContext context = new PatientClientContext("localhost", 8888);
                new PatientGUI(context);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Error connecting to server: " + e.getMessage(),
                        "Connection error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private JPanel createAuthPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Paciente - Telemedicina");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JButton loginButton = new JButton("Log in");
        JButton registerButton = new JButton("Register");

        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton.addActionListener(e -> showLoginForm());
        registerButton.addActionListener(e -> showRegisterForm());

        panel.add(Box.createVerticalStrut(40));
        panel.add(title);
        panel.add(Box.createVerticalStrut(40));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(registerButton);

        return panel;
    }

    private void showLoginForm() {
        JDialog dialog = new JDialog(this, "Log in", true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(3, 2));

        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Password:"));
        dialog.add(passwordField);

        JButton loginBtn = new JButton("Log in");
        dialog.add(new JLabel());
        dialog.add(loginBtn);

        loginBtn.addActionListener(ev -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            try {
                boolean ok = context.getPatientUI().logInFromGUI(
                        email,
                        password,
                        context.getSocket(),
                        context.getSendData(),
                        context.getReceiveData()
                );
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Log in successful");
                    dialog.dispose();
                    cardLayout.show(mainPanel, "MENU");  // Cambiar a la pantalla de opciones del paciente
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Incorrect user or password",
                            "Login error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Connection error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private void showRegisterForm() {
        JDialog dialog = new JDialog(this, "Register", true);
        dialog.setSize(450, 350);
        dialog.setLayout(new GridLayout(8, 2));

        JTextField nameField = new JTextField();
        JTextField surnameField = new JTextField();
        JTextField dniField = new JTextField();
        JTextField dobField = new JTextField();  // YYYY-MM-DD
        JTextField sexField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField insuranceField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        dialog.add(new JLabel("Name:")); dialog.add(nameField);
        dialog.add(new JLabel("Surname:")); dialog.add(surnameField);
        dialog.add(new JLabel("DNI:")); dialog.add(dniField);
        dialog.add(new JLabel("Birth date (YYYY-MM-DD):")); dialog.add(dobField);
        dialog.add(new JLabel("Sex (M/F):")); dialog.add(sexField);
        dialog.add(new JLabel("Email:")); dialog.add(emailField);
        dialog.add(new JLabel("Phone:")); dialog.add(phoneField);
        dialog.add(new JLabel("Insurance:")); dialog.add(insuranceField);
        dialog.add(new JLabel("Password:")); dialog.add(passwordField);

        JButton registerBtn = new JButton("Register");
        dialog.add(new JLabel());
        dialog.add(registerBtn);

        registerBtn.addActionListener(ev -> {
            try {
                boolean ok = context.getPatientUI().registerFromGUI(
                        nameField.getText(),
                        surnameField.getText(),
                        dniField.getText(),
                        dobField.getText(),
                        sexField.getText(),
                        emailField.getText(),
                        new String(passwordField.getPassword()),
                        Integer.parseInt(phoneField.getText()),  // Convertimos teléfono a entero
                        Integer.parseInt(insuranceField.getText()),
                        context.getSocket(),
                        context.getSendData(),
                        context.getReceiveData()
                );
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Patient registered successfully");
                    dialog.dispose();
                    cardLayout.show(mainPanel, "MENU"); // Cambiar a la pantalla de opciones del paciente
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Registration failed",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Connection error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }







    // ===================== PANTALLA 3: MENU 4 OPCIONES =====================

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Paciente menu");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JButton registerInfoButton = new JButton("Register Medical Information");
        JButton recordSignalButton = new JButton("Record Signal");
        JButton sendReportButton = new JButton("Send Report");
        JButton feedbackButton = new JButton("View Doctor Feedback");

        registerInfoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        recordSignalButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sendReportButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        feedbackButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Aquí llamamos a los métodos correspondientes cuando el paciente elige una opción
        registerInfoButton.addActionListener(e -> onRegisterMedicalInfo());
        recordSignalButton.addActionListener(e -> onRecordSignal());
        sendReportButton.addActionListener(e -> onSendReport());
        feedbackButton.addActionListener(e -> onViewFeedback());

        panel.add(Box.createVerticalStrut(30));
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(registerInfoButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(recordSignalButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(sendReportButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(feedbackButton);

        return panel;
    }

    private void onRegisterMedicalInfo() {
        if (currentPatientId == null) {
            JOptionPane.showMessageDialog(this, "No patient selected");
            return;
        }

        // Crear un formulario para capturar los síntomas
        JDialog dialog = new JDialog(this, "Register Medical Information", true);
        dialog.setSize(450, 400);
        dialog.setLayout(new GridLayout(10, 2));  // Ajustamos el tamaño del formulario

        JTextArea symptomsListArea = new JTextArea(10, 30);  // Área de texto para mostrar la lista de síntomas
        symptomsListArea.setEditable(false);  // Solo lectura, para mostrar los síntomas

        dialog.add(new JLabel("Symptoms List (Select by ID):"));
        dialog.add(new JScrollPane(symptomsListArea));  // Hacemos que el área de texto sea desplazable

        JButton selectSymptomsButton = new JButton("Select Symptoms");
        dialog.add(new JLabel());  // Espacio vacío
        dialog.add(selectSymptomsButton);

        // Enviar la solicitud al servidor para obtener los síntomas
        try {
            context.getSendData().sendStrings("SEND SYMPTOMS");
            String message = context.getReceiveData().receiveString();

            if (message.equals("OK")) {
                List<Symptom> symptoms = context.getReceiveData().receiveSymptoms();
                StringBuilder symptomsText = new StringBuilder("ID - Symptom Name\n");

                for (int i = 0; i < symptoms.size(); i++) {
                    symptomsText.append(symptoms.get(i).getId())
                            .append(" - ").append(symptoms.get(i).getDescription())
                            .append("\n");
                }

                symptomsListArea.setText(symptomsText.toString());

                selectSymptomsButton.addActionListener(e -> {
                    String selectedSymptomsIds = JOptionPane.showInputDialog(dialog,
                            "Enter the IDs of the symptoms you are experiencing (comma separated):");

                    if (selectedSymptomsIds != null && !selectedSymptomsIds.isEmpty()) {
                        String[] selectedIds = selectedSymptomsIds.split(",");
                        List<Symptom> selectedSymptoms = new ArrayList<>();

                        // Seleccionar los síntomas según los ID proporcionados por el paciente
                        for (String id : selectedIds) {
                            try {
                                int symptomId = Integer.parseInt(id.trim());  // Convertir ID a entero
                                for (Symptom symptom : symptoms) {
                                    if (symptom.getId() == symptomId) {
                                        selectedSymptoms.add(symptom);  // Agregar el síntoma seleccionado
                                    }
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(dialog, "Invalid ID format.");
                                return;
                            }
                        }

                        // Ahora enviamos los síntomas seleccionados y la información médica al servidor
                        try {
                            // Aquí estamos llamando al método para registrar la información médica
                            context.getPatientUI().insertMedicalInformationFromGUI(
                                    currentPatientId,  // ID del paciente
                                    selectedSymptoms,   // Lista de síntomas seleccionados
                                    context.getSocket(),
                                    context.getSendData(),
                                    context.getReceiveData()
                            );
                            dialog.dispose();  // Cerrar el formulario después de enviar
                            JOptionPane.showMessageDialog(this, "Medical Information Registered Successfully");
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

            } else {
                JOptionPane.showMessageDialog(dialog, "Could not fetch symptoms from the server");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(dialog, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }







    private void onRecordSignal() {
        if (currentPatientId == null) {
            JOptionPane.showMessageDialog(this, "No patient selected");
            return;
        }
        // Aquí iría la lógica para grabar la señal
        JOptionPane.showMessageDialog(this, "Recording Signal");
    }

    private void onSendReport() {
        if (currentPatientId == null) {
            JOptionPane.showMessageDialog(this, "No patient selected");
            return;
        }
        // Aquí iría la lógica para enviar el reporte
        JOptionPane.showMessageDialog(this, "Sending Report");
    }

    private void onViewFeedback() {
        if (currentPatientId == null) {
            JOptionPane.showMessageDialog(this, "No patient selected");
            return;
        }
        // Aquí iría la lógica para ver el feedback del doctor
        JOptionPane.showMessageDialog(this, "Viewing Doctor Feedback");
    }
}

