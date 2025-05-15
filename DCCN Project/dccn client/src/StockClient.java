
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;

public class StockClient {

    private static final String SERVER_ADDRESS = "127.0.0.1"; // Change to your server address if needed
    private static final int PORT = 8000;
    private Socket serverSocket;
    protected DataInputStream input;
    protected DataOutputStream output;

    private JFrame mainFrame;
    
    public StockClient()
    {
        try 
        {
            serverSocket = new Socket(SERVER_ADDRESS, PORT);
            input = new DataInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            output = new DataOutputStream(serverSocket.getOutputStream());
            System.out.println("Connected to the Stock Server.");
            
            mainFrame = new JFrame();
            mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            mainFrame.setSize(500, 500);
            
            mainFrame.addWindowListener(new java.awt.event.WindowAdapter() 
            {
                @Override
                public void windowClosing(WindowEvent windowEvent) 
                {
                    // Inform the server of closing
                    try 
                    {
                        if (output != null) 
                        {
                            output.writeUTF("OVER");
                        }
                    } 
                    catch (IOException e) 
                    {
                        System.out.println(e);
                    }
                    closeSocket();
                    // Exit the application
                    System.out.println("Socket closed");
                    System.exit(0);
                }
            });
        
            mainFrame.setVisible(true);
            createLoginPage(); 
        }       
        catch (IOException ex) 
        {
            System.out.println(ex);
        }
    }

    private void closeSocket() 
    {
        // Close the input and output streams, and then the socket
        try 
        {
            if (input != null) 
            {
                input.close();
            }
            if (output != null) 
            {
                output.close();
            }
            serverSocket.close();
        } 
        catch (IOException e) 
        {
            System.out.println(e);
        }
    }
    
    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
//        button.setFont(new Font("Arial", Font.BOLD, 16));
//        button.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(Color.BLACK, 2),
//                BorderFactory.createEmptyBorder(10, 20, 10, 20)
//        ));
        button.setContentAreaFilled(false);
        button.setOpaque(true); // To make the background visible
    }
    
    private void createLoginPage() 
    {
        mainFrame.getContentPane().removeAll();
        mainFrame.setTitle("Stock Client - Login");
        
        // Set background color
        mainFrame.getContentPane().setBackground(new Color(240, 248, 255));

        // Create panel with null layout
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(null);
        loginPanel.setBackground(new Color(240, 248, 255));

        // Title label
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(200, 30, 100, 30);  // Centered at the top of the panel

        // Username and password fields
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordFieldLogin = new JPasswordField(20);
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // Set bounds for components to center them
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 100, 150, 30);  // Label for username
        usernameField.setBounds(200, 100, 250, 30);  // Username text field

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 150, 150, 30);  // Label for password
        passwordFieldLogin.setBounds(200, 150, 250, 30);  // Password field

        loginButton.setBounds(125, 200, 250, 40);  // Login button
        registerButton.setBounds(125, 260, 250, 40);  // Register button

        // Style buttons
        styleButton(loginButton, new Color(30, 144, 255), Color.WHITE);
        styleButton(registerButton, new Color(50, 205, 50), Color.WHITE);

        // Add components to the panel
        loginPanel.add(titleLabel);
        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordFieldLogin);
        loginPanel.add(loginButton);
        loginPanel.add(registerButton);

        // Add panel to the frame
        mainFrame.add(loginPanel, BorderLayout.CENTER);

        // Add action listeners
        loginButton.addActionListener(e -> login(usernameField, passwordFieldLogin));
        registerButton.addActionListener(e -> createRegistrationPage());

        mainFrame.setVisible(true);
    }

    // Create a registration form
    private void createRegistrationPage() 
    {
        mainFrame.getContentPane().removeAll();
        mainFrame.setTitle("Stock Client - Register");

        // Create registration panel with null layout
        JPanel registrationPanel = new JPanel();
        registrationPanel.setLayout(null);
        registrationPanel.setBackground(new Color(240, 248, 255));

        // Title label
        JLabel titleLabel = new JLabel("Register");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(200, 30, 100, 30);

        // Username, password, and confirm password fields
        JTextField regUsernameField = new JTextField(20);
        JPasswordField regPasswordField = new JPasswordField(20);
        JPasswordField regConfirmPasswordField = new JPasswordField(20);
        JButton registerSubmitButton = new JButton("Register");
        JButton registerBackButton = new JButton("Go to login page");
        
        // Set bounds for components to center them
        JLabel regUsernameLabel = new JLabel("Username:");
        regUsernameLabel.setBounds(50, 100, 150, 30);  // Label for username
        regUsernameField.setBounds(200, 100, 250, 30);  // Username text field

        JLabel regPasswordLabel = new JLabel("Password:");
        regPasswordLabel.setBounds(50, 150, 150, 30);  // Label for password
        regPasswordField.setBounds(200, 150, 250, 30);  // Password field

        JLabel regConfirmPasswordLabel = new JLabel("Confirm Password:");
        regConfirmPasswordLabel.setBounds(50, 200, 150, 30);  // Label for confirm password
        regConfirmPasswordField.setBounds(200, 200, 250, 30);  // Confirm password field

        registerSubmitButton.setBounds(125, 260, 250, 40);  // Register button
        registerBackButton.setBounds(125, 320, 250, 40);
        
        // Style the register button
        styleButton(registerSubmitButton, new Color(255, 165, 0), Color.WHITE);
        styleButton(registerBackButton, new Color(155, 0, 50), Color.WHITE);
        
        // Add components to the registration panel
        registrationPanel.add(titleLabel);
        registrationPanel.add(regUsernameLabel);
        registrationPanel.add(regUsernameField);
        registrationPanel.add(regPasswordLabel);
        registrationPanel.add(regPasswordField);
        registrationPanel.add(regConfirmPasswordLabel);
        registrationPanel.add(regConfirmPasswordField);
        registrationPanel.add(registerSubmitButton);
        registrationPanel.add(registerBackButton);
        
        // Add panel to the frame
        mainFrame.add(registrationPanel, BorderLayout.CENTER);

        // Add action listener for registration
        registerSubmitButton.addActionListener(e -> register(regUsernameField, regPasswordField, regConfirmPasswordField));
        registerBackButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                createLoginPage();
            }
        });
        
        // Revalidate and repaint to update the frame UI
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setVisible(true);
    }

    // Login logic
    private void login(JTextField usernameField, JPasswordField passwordFieldLogin) 
    {
        try 
        {
            System.out.println("login");
            String username = usernameField.getText().trim();
            String password = new String(passwordFieldLogin.getPassword()).trim();
            
            if (username.isEmpty() || password.isEmpty())
            {
                JOptionPane.showMessageDialog(mainFrame, "Username and password cannot be empty", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            output.writeUTF("LOGIN " + username + " " + password);
            
            String response = input.readUTF();
            if(response.equals("LOGIN SUCCESSFUL"))
            {
                mainFrame.dispose();
                new ClientMenu(input, output, serverSocket);
                
            }
            else
            {
                JOptionPane.showMessageDialog(mainFrame, "Username and password not correct. Please try again.", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } 
        catch (IOException ex) 
        {
            System.out.println(ex);
        }
    }

    // Register logic
    private void register(JTextField regUsernameField, JPasswordField regPasswordField, JPasswordField regConfirmPasswordField) 
    {
        System.out.println("register");
        String username = regUsernameField.getText().trim();
        String password = new String(regPasswordField.getPassword()).trim();
        String confirmPassword = new String(regConfirmPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty())
        {
            JOptionPane.showMessageDialog(mainFrame, "You cannot leave any field empty. Please fill al the fields and try again.", "Registeration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.equals(confirmPassword)) 
        {
            try 
            {
                output.writeUTF("REGISTER " + username + " " + password);
                
                String response = input.readUTF();
                if(response.equals("REGISTER SUCCESSFUL"))
                {
                    JOptionPane.showMessageDialog(mainFrame, "Registeration Successful.", "User registered", JOptionPane.INFORMATION_MESSAGE);
                    createLoginPage(); // After registering, go back to the login page
                }
                else if(response.equals("USERNAME ALREADY EXISTS"))
                {
                    JOptionPane.showMessageDialog(mainFrame, "Username is already being used. Please try again with other username.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    JOptionPane.showMessageDialog(mainFrame, "Registeration unsuccesful. Please try again.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            } 
            catch (IOException ex) 
            {
                System.out.println(ex);
            }
        } 
        else 
        {
            JOptionPane.showMessageDialog(mainFrame, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StockClient client = new StockClient();
//            client.createLoginPage();
        });
    }
}
