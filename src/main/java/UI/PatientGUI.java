package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class PatientGUI extends JFrame {

    private PatientClientContext context;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Paneles
    private JPanel authPanel;
    private JPanel menuPanel;

    private Integer currentPatientId = null;

    public PatientGUI(PatientClientContext context) {
        super("Telemedicine - Paciente");
        this.context = context;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        // CardLayout para manejar las pantallas
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        authPanel = createAuthPanel();
        menuPanel = createMenuPanel();

        // Añadir ambos paneles al layout
        mainPanel.add(authPanel, "AUTH");
        mainPanel.add(menuPanel, "MENU");  // Asegúrate de que el nombre sea "MENU"

        // Establecer el panel de contenido
        setContentPane(mainPanel);

        // Inicialmente mostramos el panel de autenticación
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

    // Crear el panel de autenticación (Login y Register)
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

    // Mostrar el formulario de login
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
                    // Asignar el ID del paciente logueado
                    currentPatientId = context.getPatientUI().getLoggedInPatient().getId();
                    JOptionPane.showMessageDialog(dialog, "Log in successful");
                    dialog.dispose();
                    cardLayout.show(mainPanel, "MENU");  // Cambiar a la pantalla de menú
                } else {
                    JOptionPane.showMessageDialog(dialog, "Incorrect user or password", "Login error", JOptionPane.ERROR_MESSAGE);
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

    // Mostrar el formulario de registro
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
                        Integer.parseInt(phoneField.getText()),  // Convertimos teléfono a entero
                        Integer.parseInt(insuranceField.getText()),
                        new String(passwordField.getPassword()),
                        context.getSocket(),
                        context.getSendData(),
                        context.getReceiveData()
                );
                if (ok) {
                    // Asignar el ID del paciente registrado
                    currentPatientId = context.getPatientUI().getLoggedInPatient().getId();
                    JOptionPane.showMessageDialog(dialog, "Patient registered successfully");
                    dialog.dispose();
                    cardLayout.show(mainPanel, "MENU");  // Cambiar a la pantalla de menú
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

    // Crear el panel del menú de opciones del paciente
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
        // Aquí va la lógica para registrar la información médica
        JOptionPane.showMessageDialog(this, "Here you can register medical information");
    }

    private void onRecordSignal() {
        // Aquí va la lógica para grabar la señal
        JOptionPane.showMessageDialog(this, "Here you can record a signal");
    }

    private void onSendReport() {
        // Aquí va la lógica para enviar el reporte
        JOptionPane.showMessageDialog(this, "Here you can send a report");
    }

    private void onViewFeedback() {
        // Aquí va la lógica para ver el feedback del doctor
        JOptionPane.showMessageDialog(this, "Here you can view doctor feedback");
    }
}
