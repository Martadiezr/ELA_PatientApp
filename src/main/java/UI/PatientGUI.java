package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class PatientGUI extends JFrame {

    // Contexto de conexión (ahora se inicializa más tarde)
    private PatientClientContext context;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Paneles
    private JPanel connectPanel; // <--- Nuevo panel
    private JPanel authPanel;
    private JPanel menuPanel;

    // Componentes de connectPanel
    private JTextField ipField;

    // Componentes de selectPatient
    private JTextArea patientListArea;
    private JTextField patientIdField;

    // Estado
    private Integer currentPatientId = null;
    public PatientGUI() {
        super("Telemedicine - Doctor");
        // Nota: Ya no pedimos el 'context' en el constructor

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 1. Crear los paneles
        connectPanel = createConnectPanel(); // <--- Creamos el panel de conexión
        authPanel = createAuthPanel();
        menuPanel = createMenuPanel();

        // 2. Añadirlos al CardLayout
        mainPanel.add(connectPanel, "CONNECT");
        mainPanel.add(authPanel, "AUTH");
        mainPanel.add(menuPanel, "MENU");

        setContentPane(mainPanel);

        // 3. Mostrar primero la pantalla de conexión
        cardLayout.show(mainPanel, "CONNECT");

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Ya no conectamos aquí, solo lanzamos la interfaz
            new PatientGUI();
        });
    }

// ===================== PANTALLA 0: CONEXIÓN AL SERVIDOR =====================

    private JPanel createConnectPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome to Telemedicine");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel ipLabel = new JLabel("Enter Server IP Address:");
        ipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campo de texto con "localhost" por defecto
        ipField = new JTextField("localhost");
        ipField.setMaximumSize(new Dimension(200, 30));
        ipField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton connectButton = new JButton("Connect");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        connectButton.addActionListener(e -> attemptConnection());

        panel.add(Box.createVerticalStrut(50));
        panel.add(title);
        panel.add(Box.createVerticalStrut(40));
        panel.add(ipLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(ipField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(connectButton);

        return panel;
    }

    private void attemptConnection() {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an IP address.");
            return;
        }

        try {
            // Intentamos conectar creando el contexto
            // Asumimos puerto 8888 fijo, pero podrías poner otro campo para el puerto
            this.context = new PatientClientContext(ip, 8888);

            // Si no da error, pasamos a la siguiente pantalla
            JOptionPane.showMessageDialog(this, "Connected to server successfully!");
            cardLayout.show(mainPanel, "AUTH");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error connecting to server at " + ip + ":\n" + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createAuthPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Doctor Login");
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
                    cardLayout.show(mainPanel, "MENU");
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
        JTextField phoneField = new JTextField();
        JTextField insuranceField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        dialog.add(new JLabel("Name:")); dialog.add(nameField);
        dialog.add(new JLabel("Surname:")); dialog.add(surnameField);
        dialog.add(new JLabel("DNI:")); dialog.add(dniField);
        dialog.add(new JLabel("Birth date (YYYY-MM-DD):")); dialog.add(dobField);
        dialog.add(new JLabel("Sex (M/F):")); dialog.add(sexField);
        dialog.add(new JLabel("Phone:")); dialog.add(phoneField);
        dialog.add(new JLabel("Insurance:")); dialog.add(insuranceField);
        dialog.add(new JLabel("Email:")); dialog.add(emailField);
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
                        Integer.parseInt(phoneField.getText()),
                        Integer.parseInt(insuranceField.getText()),
                        emailField.getText(),
                        new String(passwordField.getPassword()),
                        context.getSocket(),
                        context.getSendData(),
                        context.getReceiveData()
                );
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Patient registered successfully");
                    dialog.dispose();
                    cardLayout.show(mainPanel, "MENU");

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


    //===================== PANTALLA 3: MENU 4 OPCIONES =====================

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Patient Menu");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JButton insertMedicalInfoButton = new JButton("Insert medical info");
        JButton updateFeedbackButton = new JButton("Record signal and send signal");
        JButton viewSignalButton = new JButton("See feedbakc");
        JButton modifyDataButton = new JButton("Change patient data");

        insertMedicalInfoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateFeedbackButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewSignalButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        modifyDataButton.setAlignmentX(Component.CENTER_ALIGNMENT);

       // insertMedicalInfoButton.addActionListener(e -> oninsertMedicalInfoButton());
        // updateFeedbackButton.addActionListener(e -> onUpdateFeedback());
        // viewSignalButton.addActionListener(e -> onViewSignal());
       // modifyDataButton.addActionListener(e -> onChangePatientData());

        panel.add(Box.createVerticalStrut(30));
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(insertMedicalInfoButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(updateFeedbackButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(viewSignalButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(modifyDataButton);

        return panel;
    }
    /**public void oninsertMedicalInfoButton() {
        // 1. Comprobamos que hay contexto (estás conectado al servidor)
        if (context == null) {
            JOptionPane.showMessageDialog(this,
                    "You are not connected to the server.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Conseguimos el PatientUI del contexto
        PatientUI patientUI = context.getPatientUI();

        // 3. Obtenemos el paciente logueado
       // pojos.Patient loggedInPatient = patientUI.getLoggedInPatient();
        if (loggedInPatient == null) {
            JOptionPane.showMessageDialog(this,
                    "You must log in first.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4. Llamamos al método que envía la información médica (versión GUI)
        try {
            patientUI.insertMedicalInformationGUI(
                    loggedInPatient,
                    context.getSocket(),
                    context.getSendData(),
                    context.getReceiveData()
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error sending medical information:\n" + ex.getMessage(),
                    "Connection error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }**/





}