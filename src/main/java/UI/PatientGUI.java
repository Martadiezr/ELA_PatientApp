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
    private JPanel connectPanel;
    private JPanel authPanel;
    private JPanel menuPanel;

    // Componentes de connectPanel
    private JTextField ipField;

    // Estado (currentPatientId ya no es necesario aquí si se usa PatientUI.getLoggedInPatient)
    // private Integer currentPatientId = null;

    // ===== COLORES Y FUENTES DE AdminGUI =====
    private static final Color BG_COLOR = new Color(238, 244, 255);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 220, 240);
    private static final Color TEXT_DARK = new Color(30, 30, 30);
    private static final Color BLUE_BUTTON = new Color(86, 132, 225); // Color principal de acción
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 26);
    private static final Font SUBTITLE_FONT = new Font("SansSerif", Font.BOLD, 20);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.PLAIN, 15);
    // =========================================


    public PatientGUI() {
        super("Telemedicine - Patient");

        // ===== Look&Feel Nimbus (Copiado de AdminGUI) =====
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
        // ===================================================

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(900, 600));

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_COLOR);

        // 1. Crear los paneles
        connectPanel = createConnectPanel();
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

    // ===== Helper UI (Copiado de AdminGUI) =====

    /** Aplica estilo de tarjeta blanca con borde suave. */
    private void styleCard(JPanel panel) {
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
    }

    /** Aplica estilo de botón de menú (claro, con borde). */
    private void styleMenuButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBackground(new Color(245, 248, 255)); // Fondo muy claro
        button.setForeground(TEXT_DARK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    /** Aplica estilo de botón principal (azul). */
    private void stylePrimaryButton(JButton button) {
        button.setBackground(BLUE_BUTTON);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    // ===================== PANTALLA 0: CONEXIÓN AL SERVIDOR =====================

    private JPanel createConnectPanel() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(BG_COLOR);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        styleCard(panel); // <--- Estilo de tarjeta aplicado
        panel.setPreferredSize(new Dimension(380, 260)); // Reducido para ser similar a AdminGUI

        JLabel title = new JLabel("Welcome to Telemedicine");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(TITLE_FONT); // <--- Fuente de título aplicada

        JLabel ipLabel = new JLabel("Enter Server IP Address:");
        ipLabel.setFont(new Font("SansSerif", Font.PLAIN, 14)); // Fuente simple
        ipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campo de texto con "localhost" por defecto
        ipField = new JTextField("localhost", 18);
        ipField.setMaximumSize(new Dimension(260, 32)); // Tamaño fijo
        ipField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton connectButton = new JButton("Connect");
        stylePrimaryButton(connectButton); // <--- Estilo de botón principal aplicado

        connectButton.addActionListener(e -> attemptConnection());

        panel.add(Box.createVerticalStrut(10));
        panel.add(title);
        panel.add(Box.createVerticalStrut(25));
        panel.add(ipLabel);
        panel.add(Box.createVerticalStrut(8));
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
        styleCard(panel); // <--- Estilo de tarjeta aplicado
        panel.setPreferredSize(new Dimension(380, 260)); // Reducido para ser similar a AdminGUI

        JLabel title = new JLabel("Patient Login");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(SUBTITLE_FONT); // <--- Fuente de subtítulo aplicada

        JButton loginButton = new JButton("Log in");
        JButton registerButton = new JButton("Register");

        // Ambos con el mismo estilo de botón de menú
        styleMenuButton(loginButton); // <--- Estilo de botón de menú aplicado
        styleMenuButton(registerButton); // <--- Estilo de botón de menú aplicado


        loginButton.addActionListener(e -> showLoginForm());
        registerButton.addActionListener(e -> showRegisterForm());

        panel.add(Box.createVerticalStrut(15));
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
        stylePrimaryButton(loginBtn);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        content.add(loginBtn, gbc);

        loginBtn.addActionListener(ev -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            // 1. Desactivar botón para indicar carga y evitar clics repetidos
            loginBtn.setEnabled(false);
            loginBtn.setText("Connecting...");

            // 2. Crear un Hilo nuevo para la operación de red
            new Thread(() -> {
                try {
                    boolean ok = context.getPatientUI().logInFromGUI(
                            email,
                            password,
                            context.getSocket(),
                            context.getSendData(),
                            context.getReceiveData()
                    );

                    // 3. Volver al hilo de la interfaz (Swing) para mostrar resultados
                    SwingUtilities.invokeLater(() -> {
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Log in");

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
                    });

                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> {
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Log in");
                        JOptionPane.showMessageDialog(dialog,
                                "Connection error: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start(); // <--- IMPORTANTE: Iniciar el hilo
        });

        dialog.getContentPane().add(content);
        dialog.pack();
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

        // --- Definición de campos (igual que antes) ---
        JTextField nameField = new JTextField(15);
        JTextField surnameField = new JTextField(15);
        JTextField dniField = new JTextField(15);
        JTextField dobField = new JTextField(15);
        JTextField sexField = new JTextField(5);
        JTextField phoneField = new JTextField(10);
        JTextField insuranceField = new JTextField(10);
        JTextField emailField = new JTextField(18);
        JPasswordField passwordField = new JPasswordField(18);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; content.add(new JLabel("Name:"), gbc); gbc.gridx = 1; content.add(nameField, gbc);
        row++; gbc.gridx = 0; gbc.gridy = row; content.add(new JLabel("Surname:"), gbc); gbc.gridx = 1; content.add(surnameField, gbc);
        row++; gbc.gridx = 0; gbc.gridy = row; content.add(new JLabel("DNI:"), gbc); gbc.gridx = 1; content.add(dniField, gbc);
        row++; gbc.gridx = 0; gbc.gridy = row; content.add(new JLabel("Birth date:"), gbc); gbc.gridx = 1; content.add(dobField, gbc);
        row++; gbc.gridx = 0; gbc.gridy = row; content.add(new JLabel("Sex (M/F):"), gbc); gbc.gridx = 1; content.add(sexField, gbc);
        row++; gbc.gridx = 0; gbc.gridy = row; content.add(new JLabel("Phone:"), gbc); gbc.gridx = 1; content.add(phoneField, gbc);
        row++; gbc.gridx = 0; gbc.gridy = row; content.add(new JLabel("Insurance:"), gbc); gbc.gridx = 1; content.add(insuranceField, gbc);
        row++; gbc.gridx = 0; gbc.gridy = row; content.add(new JLabel("Email:"), gbc); gbc.gridx = 1; content.add(emailField, gbc);
        row++; gbc.gridx = 0; gbc.gridy = row; content.add(new JLabel("Password:"), gbc); gbc.gridx = 1; content.add(passwordField, gbc);

        JButton registerBtn = new JButton("Register");
        stylePrimaryButton(registerBtn);
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        content.add(registerBtn, gbc);

        // --- LÓGICA DEL BOTÓN MODIFICADA ---
        registerBtn.addActionListener(ev -> {
            String name = nameField.getText();
            String surname = surnameField.getText();
            String dni = dniField.getText();
            String dob = dobField.getText();
            String sex = sexField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String phoneStr = phoneField.getText();
            String insuranceStr = insuranceField.getText();

            // Bloquear botón visualmente
            registerBtn.setEnabled(false);
            registerBtn.setText("Registering...");

            new Thread(() -> {
                try {
                    int phone = Integer.parseInt(phoneStr);
                    int insurance = Integer.parseInt(insuranceStr);

                    boolean ok = context.getPatientUI().registerFromGUI(
                            name, surname, dni, dob, sex, phone, insurance, email, password,
                            context.getSocket(), context.getSendData(), context.getReceiveData()
                    );

                    SwingUtilities.invokeLater(() -> {
                        registerBtn.setEnabled(true);
                        registerBtn.setText("Register");

                        if (ok) {
                            JOptionPane.showMessageDialog(dialog, "Registered successfully. Logging in...");
                            dialog.dispose(); // Cierra ventana de registro

                            // === CORRECCIÓN CRÍTICA: RECONEXIÓN ===
                            try {
                                // 1. Cerramos el socket antiguo porque el servidor quizás dejó de escuchar
                                context.getSocket().close();

                                // 2. Recuperamos la IP que usó el usuario al principio
                                String ip = ipField.getText().trim();
                                if(ip.isEmpty()) ip = "localhost";

                                // 3. Creamos una conexión NUEVA y LIMPIA
                                context = new PatientClientContext(ip, 8888);

                                // 4. Ahora sí mostramos el Login con la conexión nueva
                                showLoginForm();

                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(mainPanel,
                                        "Registration successful, but failed to reconnect for login.\n" + e.getMessage(),
                                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                                // Si falla reconectar, volvemos a la pantalla de Auth
                                cardLayout.show(mainPanel, "AUTH");
                            }
                            // ======================================

                        } else {
                            JOptionPane.showMessageDialog(dialog, "Registration failed", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });

                } catch (NumberFormatException nfe) {
                    SwingUtilities.invokeLater(() -> {
                        registerBtn.setEnabled(true);
                        registerBtn.setText("Register");
                        JOptionPane.showMessageDialog(dialog, "Check Phone/Insurance fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    });
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> {
                        registerBtn.setEnabled(true);
                        registerBtn.setText("Register");
                        JOptionPane.showMessageDialog(dialog, "Connection error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });

        dialog.getContentPane().add(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    //===================== PANTALLA 3: MENU 4 OPCIONES =====================

    private JPanel createMenuPanel() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(BG_COLOR);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        styleCard(panel); // <--- Estilo de tarjeta aplicado
        panel.setPreferredSize(new Dimension(380, 320));

        JLabel title = new JLabel("Patient Menu");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(SUBTITLE_FONT); // <--- Fuente de subtítulo aplicada
        title.setForeground(TEXT_DARK);

        JButton insertMedicalInfoButton = new JButton("Insert medical info");
        JButton signalButton = new JButton("Record signal and send signal");
        JButton seeFeedbackButton = new JButton("See feedback");
        JButton modifyDataButton = new JButton("Change patient data");

        // Aplicamos el estilo de menú a todos los botones
        styleMenuButton(insertMedicalInfoButton);
        styleMenuButton(signalButton);
        styleMenuButton(seeFeedbackButton);
        styleMenuButton(modifyDataButton);

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
            JOptionPane.showMessageDialog(this, "You are not connected to the server.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PatientUI patientUI = context.getPatientUI();
        Patient loggedInPatient = patientUI.getLoggedInPatient();

        if (loggedInPatient == null) {
            JOptionPane.showMessageDialog(this, "You must log in first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ejecutamos en un hilo aparte para que la grabación no congele la ventana visualmente
        new Thread(() -> {
            patientUI.recordAndSendSignalGUI(
                    loggedInPatient,
                    context.getSocket(),
                    context.getSendData(),
                    context.getReceiveData(),
                    PatientGUI.this
            );
            // ¡YA NO HACEMOS RECONEXIÓN AQUÍ!
            // Como hemos arreglado PatientUI para leer el "OK", la conexión sigue sincronizada.
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
