import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.table.TableCellRenderer;

public class ClientMenu
{
    private JFrame mainFrame; // New main frame
    private JFrame stockFrame;
    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JTextField stockSymbolField;
    private JButton subscribeButton;
    private ArrayList<String> subscribedStocks; // To track subscribed stock symbols

    private Socket serverSocket;
    private DataInputStream input;
    private DataOutputStream output;
    
    private Thread stockFetchThread;
    private  boolean showSubscribedOnly;
    private boolean stopFetching;
    public ClientMenu(DataInputStream inputStream, DataOutputStream outputStream, Socket socket)
    {
        System.out.println("Client Menu");
        this.input = inputStream;
        this.output = outputStream;
        this.serverSocket = socket;

        // Initialize the set of subscribed stocks
        subscribedStocks = new ArrayList<>();
        initializeSubscribedList();
             
        showSubscribedOnly = false;
        stopFetching = false;
        createMainPage();
    }

    private void initializeSubscribedList()
    {
        try 
        {
            String line=input.readUTF();
            while(!line.equals("Over"))
            {
                subscribedStocks.add(line);
                line = input.readUTF();
            }
            System.out.println("Subscribed Stocks: "+ subscribedStocks);
        } 
        catch (IOException ex) 
        {
            System.out.println(ex);
        }
    }
    
    private void createMainPage() 
    {
        mainFrame = new JFrame("Stock Client - Main Page");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 500);

        // Set main frame background color
        mainFrame.getContentPane().setBackground(new Color(240, 248, 255));

        JButton overallStocksButton = new JButton("Overall Stocks");
        JButton subscribedStocksButton = new JButton("Subscribed Stocks");

        // Style buttons with rounded borders
        styleButton(overallStocksButton, new Color(30, 144, 255), Color.WHITE);
        styleButton(subscribedStocksButton, new Color(50, 205, 50), Color.WHITE);

        overallStocksButton.addActionListener(e -> showStockPage(false));
        subscribedStocksButton.addActionListener(e -> showStockPage(true));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(175, 100, 175, 100));
        buttonPanel.add(overallStocksButton);
        buttonPanel.add(subscribedStocksButton);

        mainFrame.add(buttonPanel, BorderLayout.CENTER);
        
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
    }

    private void closeSocket() 
    {
        stopFetching = true;  // Stop the stock fetch thread
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
    
    private void styleButton(JButton button, Color background, Color foreground) 
    {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setContentAreaFilled(false);
        button.setOpaque(true); // To make the background visible
    }

    private void showStockPage(boolean showSubscribed)
    {
        showSubscribedOnly = showSubscribed; //change to true so table only updates for subscribed stocks
        
        mainFrame.setVisible(false); // Hide main frame

        stockFrame = new JFrame(showSubscribedOnly ? "Subscribed Stocks" : "Overall Stocks");
        stockFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        stockFrame.setSize(500, 500);

        // Set background color for stockFrame
        stockFrame.getContentPane().setBackground(new Color(245, 245, 245));

        tableModel = new DefaultTableModel(new String[]{"Symbol", "Price"}, 0);
        stockTable = new JTable(tableModel) {
            // Override table cell renderer for custom colors
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) 
            {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column == 0) { // Symbol column
                    c.setForeground(new Color(255, 99, 71)); // Tomato red color for stock symbols
                } else if (column == 1) { // Price column
                    c.setForeground(new Color(0, 128, 0)); // Green color for prices
                }
                return c;
            }
        };

        stockSymbolField = new JTextField(10);
        subscribeButton = new JButton("Subscribe");
        JButton goToMenuButton = new JButton("Go to Menu");

        // Style the subscribe button
        styleButton(subscribeButton, new Color(255, 165, 0), Color.WHITE);
        styleButton(goToMenuButton, new Color(70, 130, 180), Color.WHITE); // Style the button

        subscribeButton.addActionListener(e -> subscribeToStock());
        goToMenuButton.addActionListener(e -> 
        {
            gotoMenu();
        });

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(new Color(245, 245, 245));
        inputPanel.add(new JLabel("Stock Symbol:"));
        inputPanel.add(stockSymbolField);
        inputPanel.add(subscribeButton);
        inputPanel.add(goToMenuButton);

        stockFrame.setLayout(new BorderLayout());
        stockFrame.add(new JScrollPane(stockTable), BorderLayout.CENTER);
        stockFrame.add(inputPanel, BorderLayout.SOUTH);

        if(showSubscribedOnly)
        {
            stockSymbolField.hide();
            subscribeButton.hide();
        }
        
        stockFrame.setVisible(true);

        if (stockFetchThread == null)
        {
            // Connect to the server in a separate thread
            stockFetchThread = new Thread(() -> getStocksFromServer());
            stockFetchThread.start();
        }
    }

    private void gotoMenu()
    {
        showSubscribedOnly = false; //change to true so table only updates for subscribed stocks
        stockFrame.dispose(); // Close the current stock frame
        mainFrame.setVisible(true); // Show the main menu
    }
    
    private void getStocksFromServer() 
    {
        try 
        {
            output.writeUTF("REQUEST STOCKS");
            String stockUpdate;
            while (!stopFetching && (stockUpdate = input.readUTF()) != null) 
            {
                System.out.println("Received stock update: " + stockUpdate);
                updateStockTable(stockUpdate);
            }
        } 
        catch (IOException e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }

private void subscribeToStock() 
{
    String stockSymbol = stockSymbolField.getText().trim();
    java.util.List<String> savedsymbols = Arrays.asList("AAPL", "GOOGL", "AMZN", "MSFT", "TSLA","SAMG","HUAWEI","STATE","CUST","META","OPENAI","NESTLE","NVDA","PLUG","PCG","UBER","VERZN");
    if (!stockSymbol.isEmpty() && output != null) 
    {
        if(subscribedStocks.contains(stockSymbol))
        {
            JOptionPane.showMessageDialog(stockFrame, 
                "The entered stock is already subscribed.", 
                "Already Subscribed Stock Symbol", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (savedsymbols.contains(stockSymbol)) 
        { // Check if the entered stock symbol matches the saved list
            subscribedStocks.add(stockSymbol);
            try 
            {
                output.writeUTF("SUBSCRIBE " + stockSymbol);
            } 
            catch (IOException ex) 
            {
                System.out.println(ex);
            }

            JOptionPane.showMessageDialog(stockFrame, 
                "You have succesfully subscriber to \""+stockSymbol+"\".", 
                "Subscription Successfull", 
                JOptionPane.INFORMATION_MESSAGE);

            stockSymbolField.setText("");
        }
        else 
        {
            // If the stock symbol is not in the saved list
            JOptionPane.showMessageDialog(stockFrame, 
                "The entered stock symbol is not recognized. Please check and try again.", 
                "Invalid Stock Symbol", 
                JOptionPane.ERROR_MESSAGE);
        }
    } 
    else {
        JOptionPane.showMessageDialog(stockFrame, 
            "Please enter a valid stock symbol.", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}
    private void updateStockTable(String stockUpdate) {
        SwingUtilities.invokeLater(() -> {
            try {
                String[] data = stockUpdate.replace("{", "").replace("}", "").replace("\"", "").split(",");
                String symbol = null;
                String price = null;

                for (String entry : data) 
                {
                    String[] keyValue = entry.split(":");
                    if (keyValue.length == 2) {
                        if (keyValue[0].trim().equals("symbol")) {
                            symbol = keyValue[1].trim();
                        } else if (keyValue[0].trim().equals("price")) {
                            price = keyValue[1].trim();
                        }
                    }
                }

                if (symbol == null || price == null) 
                {
                    System.err.println("Failed to parse stock update: " + stockUpdate);
                    return;
                }

                if (showSubscribedOnly) 
                {
                    if (subscribedStocks.contains(symbol)) 
                    {
                        updateOrAddRow(symbol, price);
                    }
                } 
                else 
                {
                    updateOrAddRow(symbol, price);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateOrAddRow(String symbol, String price) 
    {
        boolean updated = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(symbol)) {
                tableModel.setValueAt(price, i, 1);
                updated = true;
                break;
            }
        }

        if (!updated) {
            tableModel.addRow(new Object[]{symbol, price});
        }
    }
}

