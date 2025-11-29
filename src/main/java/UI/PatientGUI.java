package UI;

import pojos.Patient;

import javax.swing.*;
import java.awt.*;
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

    // Componentes de selectPatient (aunque ahora no los uses aquí)
    private JTextArea patientListArea;
    private JTextField patientIdField;

    // Estado
    private Integer currentPatientId = null;

    // Colores / estética
    private static final Color BG_COLOR = new Color(238, 244, 255);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 220, 240);
    private static final Color TEXT_DARK = new Color(30, 30, 30);

    public PatientGUI() {
        super("Telemedicine - Patient");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);                 // Ventana principal más grande
        setMinimumSize(new Dimension(900, 600));

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_COLOR);

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
        SwingUtilities.invokeLater(PatientGUI::new);
    }

    // ===================== PANTALLA 0: CONEXIÓN AL SERVIDOR =====================

    private JPanel createConnectPanel() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(BG_COLOR);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setPreferredSize(new Dimension(380, 300)); // tarjeta más grande

        JLabel title = new JLabel("Welcome to Telemedicine");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JLabel ipLabel = new JLabel("Enter Server IP Address:");
        ipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campo de texto con "localhost" por defecto
        ipField = new JTextField("localhost");
        ipField.setMaximumSize(new Dimension(250, 30));
        ipField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton connectButton = new JButton("Connect");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.setFocusPainted(false);
        connectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        connectButton.addActionListener(e -> attemptConnection());

        panel.add(Box.createVerticalStrut(10));
        panel.add(title);
        panel.add(Box.createVerticalStrut(25));
        panel.add(ipLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(ipField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(connectButton);
        panel.add(Box.createVerticalStrut(10));

        background.add(panel);
        return background;
    }

    private void attemptConnection() {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an IP address.");
            return;
        }

        try {
            // Intentamos conectar creando el contexto
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

    // ===================== PANTALLA 1: LOGIN / REGISTER =====================

    private JPanel createAuthPanel() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(BG_COLOR);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setPreferredSize(new Dimension(380, 300)); // tarjeta blanca más grande

        JLabel title = new JLabel("Patient Login");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JButton loginButton = new JButton("Log in");
        JButton registerButton = new JButton("Register");

        // Ambos con el mismo estilo neutro (no azul)
        for (JButton b : new JButton[]{loginButton, registerButton}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setFocusPainted(false);
            b.setBackground(new Color(245, 248, 255));
            b.setForeground(TEXT_DARK);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR),
                    BorderFactory.createEmptyBorder(8, 18, 8, 18)
            ));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        loginButton.addActionListener(e -> showLoginForm());
        registerButton.addActionListener(e -> showRegisterForm());

        panel.add(Box.createVerticalStrut(20));
        panel.add(title);
        panel.add(Box.createVerticalStrut(25));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(registerButton);
        panel.add(Box.createVerticalStrut(15));

        background.add(panel);
        return background;
    }

    private void showLoginForm() {
        JDialog dialog = new JDialog(this, "Log in", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(CARD_COLOR);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = new JTextField(18);
        JPasswordField passwordField = new JPasswordField(18);

        gbc.gridx = 0; gbc.gridy = 0;
        content.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        content.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        content.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        content.add(passwordField, gbc);

        JButton loginBtn = new JButton("Log in");
        loginBtn.setFocusPainted(false);
        loginBtn.setBackground(new Color(245, 248, 255));
        loginBtn.setForeground(TEXT_DARK);
        loginBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        content.add(loginBtn, gbc);

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

        dialog.getContentPane().add(content);
        dialog.pack(); // cuadro más pequeño / fino
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    //show register form
    private void showRegisterForm() {
        JDialog dialog = new JDialog(this, "Register", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(CARD_COLOR);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 8, 3, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        JTextField surnameField = new JTextField(15);
        JTextField dniField = new JTextField(15);
        JTextField dobField = new JTextField(15);  // YYYY-MM-DD
        JTextField sexField = new JTextField(5);
        JTextField phoneField = new JTextField(10);
        JTextField insuranceField = new JTextField(10);
        JTextField emailField = new JTextField(18);
        JPasswordField passwordField = new JPasswordField(18);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        content.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        content.add(nameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        content.add(new JLabel("Surname:"), gbc);
        gbc.gridx = 1;
        content.add(surnameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        content.add(new JLabel("DNI:"), gbc);
        gbc.gridx = 1;
        content.add(dniField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        content.add(new JLabel("Birth date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        content.add(dobField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        content.add(new JLabel("Sex (M/F):"), gbc);
        gbc.gridx = 1;
        content.add(sexField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        content.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        content.add(phoneField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        content.add(new JLabel("Insurance:"), gbc);
        gbc.gridx = 1;
        content.add(insuranceField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        content.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        content.add(emailField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        content.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        content.add(passwordField, gbc);

        JButton registerBtn = new JButton("Register");
        registerBtn.setFocusPainted(false);
        registerBtn.setBackground(new Color(245, 248, 255));
        registerBtn.setForeground(TEXT_DARK);
        registerBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        content.add(registerBtn, gbc);

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

        dialog.getContentPane().add(content);
        dialog.pack(); // más fino y compacto
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    //===================== PANTALLA 3: MENU 4 OPCIONES =====================

    private JPanel createMenuPanel() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(BG_COLOR);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setPreferredSize(new Dimension(380, 320));

        JLabel title = new JLabel("Patient Menu");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(TEXT_DARK);

        JButton insertMedicalInfoButton = new JButton("Insert medical info");
        JButton signalButton = new JButton("Record signal and send signal");
        JButton seeFeedbackButton = new JButton("See feedback");
        JButton modifyDataButton = new JButton("Change patient data");

        for (JButton b : new JButton[]{insertMedicalInfoButton, signalButton, seeFeedbackButton, modifyDataButton}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setFocusPainted(false);
            b.setBackground(new Color(245, 248, 255));
            b.setForeground(TEXT_DARK);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR),
                    BorderFactory.createEmptyBorder(8, 18, 8, 18)
            ));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        insertMedicalInfoButton.addActionListener(e -> oninsertMedicalInfoButton());
        signalButton.addActionListener(e -> onsignalButton());
        seeFeedbackButton.addActionListener(e -> onseeFeedback());
        modifyDataButton.addActionListener(e -> onChangePatientData());

        panel.add(Box.createVerticalStrut(15));
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(insertMedicalInfoButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(signalButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(seeFeedbackButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(modifyDataButton);
        panel.add(Box.createVerticalStrut(10));

        background.add(panel);
        return background;
    }

    // ===================== MÉTODOS DE BOTONES (SIN CAMBIOS DE LÓGICA) =====================

    public void oninsertMedicalInfoButton() {
        if (context == null) {
            JOptionPane.showMessageDialog(this,
                    "You are not connected to the server.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        PatientUI patientUI = context.getPatientUI();
        Patient loggedInPatient = patientUI.getLoggedInPatient();

        if (loggedInPatient == null) {
            JOptionPane.showMessageDialog(this,
                    "You must log in first.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        patientUI.insertMedicalInformationGUI(
                loggedInPatient,
                context.getSocket(),
                context.getSendData(),
                context.getReceiveData()
        );
    }


    private void onsignalButton() {
        if (context == null) {
            JOptionPane.showMessageDialog(this,
                    "You are not connected to the server.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        PatientUI patientUI = context.getPatientUI();
        Patient loggedInPatient = patientUI.getLoggedInPatient();

        if (loggedInPatient == null) {
            JOptionPane.showMessageDialog(this,
                    "You must log in first.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            patientUI.recordAndSendSignalGUI(
                    loggedInPatient,
                    context.getSocket(),
                    context.getSendData(),
                    context.getReceiveData(),
                    PatientGUI.this
            );
        }).start();
    }

    public void onseeFeedback() {
        if (context == null) {
            JOptionPane.showMessageDialog(this,
                    "You are not connected to the server.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        PatientUI patientUI = context.getPatientUI();
        Patient loggedInPatient = patientUI.getLoggedInPatient();

        if (loggedInPatient == null) {
            JOptionPane.showMessageDialog(this,
                    "You must log in first.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                patientUI.seeDoctorFeedbackGUI(
                        loggedInPatient,
                        context.getSocket(),
                        context.getSendData(),
                        context.getReceiveData()
                );
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this,
                        "Error fetching doctor feedback: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                ));
            }
        }).start();
    }


    private void onChangePatientData() {
        if (context == null) {
            JOptionPane.showMessageDialog(this,
                    "You are not connected to the server.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        PatientUI patientUI = context.getPatientUI();
        Patient loggedInPatient = patientUI.getLoggedInPatient();

        try {
            String result = context.getPatientUI().changePatientDataFromGUI(
                    loggedInPatient,
                    context.getSocket(),
                    context.getReceiveData(),
                    context.getSendData(),
                    this
            );
            JOptionPane.showMessageDialog(this, result);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error changing patient data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
