import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.lang.Integer;
import java.net.*;
import java.util.*;
/**************************************************
Student Name: John Lennon
Student Number: C10321265
Code for the Auction Server and it's functionality

***************************************************
*/


public class AuctionServer implements Runnable
{  
	private AuctionServerThread clients[] = new AuctionServerThread[50]; // Array of clients
   	private ServerSocket server = null; //socket for server connection
   	private Thread       thread = null;
   	private int clientCount = 0;
	private static int bid = 0;
	private static String startingBid = "";
	private static String item = "";
	private static int itemOnSale = 0;
	private static int clNum = 0;
	private static TimerThread countdownTimer;
	private static int winnerBid = 0;


   	public AuctionServer(int port)
   	{
		try //Bringing server online
		{
			System.out.println("\nBinding to port " + port + ", please wait  ...");
			server = new ServerSocket(port);
			start(); //Starts the server by calling run()
			
			System.out.println("Server started: " + server.getInetAddress());
			startAuction(); //Starts the auction
			itemOnSale = 1;	
      	}

		catch(IOException ioe)
		{
		System.out.println("Error cannot bind to port\n");

		}
   	}

   	public void run() //
   	{
	 	while (thread != null)
      		{
				try
				{
					System.out.println("Waiting for a client ...");
					addThread(server.accept()); //Wait for a client request

					int pause = (int)(Math.random()*3000);
					Thread.sleep(pause);
				}

				catch(IOException ioe)
				{
					System.out.println("Server accept error: " + ioe);
					stop();
				}

				catch (InterruptedException e)
				{
					System.out.println(e);
				}
      		}
   	}
	
	public void startAuction()
	{
		BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in)); //Buffer reader for user input

		try //Bringing server online
		{
			System.out.println("\nPlease enter the name the item for sale: ");
			item = userEntry.readLine();
			System.out.println("\nPlease enter the starting price in Euro: ");
			startingBid = userEntry.readLine();
			
			String messageAll = ("\nItem currently on sale is " +item+ " at " +startingBid);
			
			for( int i = 0; i < clientCount; i++)
			{
				clients[i].send(messageAll);
			}
			
			bid = Integer.decode(startingBid);
			itemOnSale = 1;	
		}
		catch(IOException ioe)
		{
		System.out.println("Error cannot restart");

		}
	}

  	public void start()
    	{
		if (thread == null) 
		{
		 	thread = new Thread(this);
          		thread.start();
       		}
    	}

   	public void stop()
	{
	  	thread = null;

   	}

   	private int findClient(int ID)
   	{
	  	for (int i = 0; i < clientCount; i++)
         	if (clients[i].getID() == ID)
            	return i;
      		return -1;
		
   	}

	public void timerBroadCast(int bidTime) //Sending the time to all clients
	{
		int t = bidTime;
		
		String messageAll = Integer.toString(t);
		
		for (int i = 0; i < clientCount; i++)
		{
			clients[i].send(messageAll);
		}
	}
	
   	public synchronized void broadcast(int ID, String input) //Handling sending messages to clients
   	{
		String messageAll = "", messageSender = "" ;
		if ( itemOnSale == 1 )
		{
			System.out.println("Item currently on sale is " +item+ " at " +bid);
			
			if (input.equals(".bye")) //Client leaving the application
			{
		  		clients[findClient(ID)].send(".bye");
          		remove(ID); //Removes client from client thread
       		}
       		
			else
			{
				int client_bid = Integer.decode(input); //Changes input from String to int

				if(client_bid > bid) //Successful bid entry
				{
					bid = client_bid;
					messageAll = "New highest bid is: ";
					messageSender = "You have placed a new highest bid of: ";
					clients[findClient(ID)].send(messageAll + bid); //Sends new bid to all clients
					clients[findClient(ID)].send(messageSender + bid); //Sends highest bid to bidder
					System.out.println(bid); //Prints client bid to server
					
					winnerBid = findClient(ID); //Gets the id of the winning bid
					countdownTimer.restart(); //Resets the timer if bid is successful
				}

				else //Unsuccessful bid entry
				{
					messageSender = "The bid you try to place is lower/same as current bid at: ";
					clients[findClient(ID)].send(messageSender + bid); //Sends message to client if bid unsuccessful.
					System.out.println(client_bid); //Prints client bid to server
				}
			}
		}
		
		else 
		{
			messageSender = "No items onsale. Please Press Enter to refresh";
			clients[findClient(ID)].send(messageSender);
		}

		notifyAll();
		
   	}
	public void winnerBroadCast() //Informing the winner they've won
	{
		String message = ("\nYou have won this auction. Well done! You won "+item);
		clients[winnerBid].send(message); //Sends message to winner
	}
   	public synchronized void remove(int ID) //Removing a client from the client thread
   	{
	  	int pos = findClient(ID);

      	if (pos >= 0)
		{
		 	AuctionServerThread toTerminate = clients[pos];
         	System.out.println("Removing client thread " + ID + " at " + pos);

			if (pos < clientCount-1)
			{
				for (int i = pos+1; i < clientCount; i++)
					clients[i-1] = clients[i];
					clientCount--;
			}
         	try
			{
			 	toTerminate.close(); //Client connection closed
	     	}

			catch(IOException ioe)
			{
			 	System.out.println("Error closing thread: " + ioe);
		 	}

		 	toTerminate = null;
		 	System.out.println("Client " + pos + " removed");
		 	notifyAll();
		}
   	}

   	private void addThread(Socket socket)
   	{
	  	if (clientCount < clients.length)
		{
			System.out.println("Client accepted: " + socket);
         	clients[clientCount] = new AuctionServerThread(this, socket);

			try //Sending the item and bid info to the clients
			{
				clients[clientCount].open();
				clients[clientCount].start();
				clients[clientCount].send("New Item "+item+", bid = "+bid); //notify of current bid item
				clientCount++;
			}

			catch(IOException ioe)
			{
				System.out.println("Error opening thread: " + ioe);
			}
		}

		else //Incase of too many clients
		System.out.println("Client refused: maximum " + clients.length + " reached.");
   	}

	public static void main(String args[]) 
	{
	   	AuctionServer server = null;

      		if (args.length != 1)
			{
         	System.out.println("Usage: java ChatServer port");
			}
			
      		else
			{
			countdownTimer = new TimerThread(server = new AuctionServer(Integer.parseInt(args[0])));//New instance for timer
			countdownTimer.start(); //Starts the countdown timer
			}
   	}

}