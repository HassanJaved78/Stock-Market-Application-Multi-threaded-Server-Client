import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientHandler implements Runnable
{
    private Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private User currentUser;
    
    private static ArrayList<User> usersList = new ArrayList<>();  //shared user list as static means all instanceas can access this list
    
    private boolean stopSending = false;
    
    public ClientHandler(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
        
        //initilaizing with initial users data
        User newUser = new User("hassan", "1234");
        newUser.subscribeStock("AAPL");
        usersList.add(newUser);
    }
    
    //client will send server following requests
    //1- for Registeration: REGISTER USERNAME PASSWORD
    //2- for login: LOGIN USERNAME PASSWORD
    //3- for getting stocks: SENDSTOCKLIST
    //4- for sunscribing: SUBSCRIBE STOCKSYMBOL
    
    @Override
    public void run() 
    {
        try
        {
            input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            output = new DataOutputStream(clientSocket.getOutputStream());
            
            String line = "";
            while(!line.equals("OVER"))
            {
                line = input.readUTF();
                System.out.println(line);
                
                if(line.startsWith("REGISTER"))
                {
                    String [] parts = line.split(" ");
                    String username = parts[1];
                    String password = parts[2];
                    registerUser(username, password);
                }
                if(line.startsWith("LOGIN"))
                {
                    String [] parts = line.split(" ");
                    String username = parts[1];
                    String password = parts[2];
                    currentUser = loginUser(username, password);
                    sendSubscribedList();
                }
                if(line.equals("REQUEST STOCKS"))
                {
                    sendStockData();
                }
                if(line.startsWith("SUBSCRIBE"))
                {
                    String [] parts = line.split(" ");
                    String symbol = parts[1];
                    currentUser.subscribeStock(symbol);
                }
            }
            stopSending = true;  //signal the stock sending function to stop
            
            System.out.println("Client disconnected");
            clientSocket.close();
            input.close();
            output.close();
        } 
        catch (IOException ex) 
        {
            System.out.println(ex);
        }
    }
    
    private synchronized void registerUser(String username, String password)
    {
        for(User user : usersList)
        {
            if(user.getUsername().equals(username))
            {
                sendMessage("USERNAME ALREADY EXISTS");
                return;
            }
        }
        usersList.add(new User(username, password));
        System.out.println("New user with "+username+" added");
        sendMessage("REGISTER SUCCESSFUL");
    }
    
    private synchronized User loginUser(String username, String password)
    {
        for(User user : usersList)
        {
            if(user.getUsername().equals(username) && user.getPassword().equals(password))
            {
                sendMessage("LOGIN SUCCESSFUL");
                System.out.println(username + " logged in");
                return user;
            }
        }
        sendMessage("INCORRECT LOGIN");
        return null;
    }
    
    private void sendMessage(String message)
    {
        try 
        {
            output.writeUTF(message);
        } 
        catch (IOException ex) 
        {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }
    
    private void sendSubscribedList()
    {
        ArrayList subscribedStocksList = currentUser.getSubscribedStocks();
        try 
        {
            if(subscribedStocksList.isEmpty())
            {
                output.writeUTF("Over");
            } 
            else
            {
                for(int i=0; i<subscribedStocksList.size(); i++)
                {
                    output.writeUTF((String) subscribedStocksList.get(i));
                    System.out.println("Sending " + subscribedStocksList.get(i));
                }
                output.writeUTF("Over");
            }
        }
        catch (IOException ex) 
        {
            System.out.println(ex);
        }
    }
    
    private void sendStockData() {
    new Thread(() -> {
        try {
            Random random = new Random();
            List<String> symbols = Arrays.asList("AAPL", "GOOGL", "AMZN", "MSFT", "TSLA", "SAMG", "HUAWEI", "STATE", "CUST", "META", "OPENAI", "NESTLE", "NVDA", "PLUG", "PCG", "UBER", "VERZN");

            while (!stopSending) {
                // Simulate real-time stock data
                String symbol = symbols.get(random.nextInt(symbols.size()));
                double price = 100 + random.nextDouble() * 900;  // Random stock price between 100 and 1000

                // Create a simple JSON-like string for stock data
                String stockUpdate = String.format("{\"symbol\": \"%s\", \"price\": \"%.2f\"}", symbol, price);
                output.writeUTF(stockUpdate);

                // Wait for a second before sending the next update
                TimeUnit.SECONDS.sleep(1);
            }
        } 
        catch (IOException | InterruptedException e) 
        {
            System.out.println(e);
        }
    }).start();
}
}

class User 
{
    private String username;
    private String password;
    private ArrayList<String> subscribedStocks = new ArrayList<>();
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList getSubscribedStocks()
    {
        return subscribedStocks;
    }
    
    public void subscribeStock(String stockSymbol)
    {
        subscribedStocks.add(stockSymbol);
    }
}

public class StockServer 
{
    private static final int PORT = 8000;
    private static final ExecutorService clientHandlerExecutor = Executors.newCachedThreadPool();

    public static void main(String[] args) 
    {
        try 
        (
            ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Stock Server started. Waiting for clients...");
            
            while (true) 
            {
                try 
                {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    
                    // Handle each client in a separate thread
                    clientHandlerExecutor.submit(new ClientHandler(clientSocket));
                } 
                catch (IOException e) 
                {
                    System.out.println(e);
                }
            }
        }
        catch (IOException e) 
        {
            System.out.println(e);
        }
    }
}
