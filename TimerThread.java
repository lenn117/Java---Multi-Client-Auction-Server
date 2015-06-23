import java.net.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
/**************************************************
Student Name: John Lennon
Student Number: C10321265
Code for the Timer Thread and it's functionality

***************************************************
*/

public class TimerThread extends Thread
{  
	private AuctionServer server; 
	private int bidTime = 60; //Inital bidding time
	
	public TimerThread(AuctionServer _server)
	{  
		server = _server;
	}
   
	public void run()
	{
		while(bidTime > 0)
		{  
			System.out.println(bidTime); //Prints the time to the server
			if(bidTime == 30)
			{
				server.timerBroadCast(getTime()); //Broadcasts the time to the clients
			}
			try
			{
				Thread.sleep(1000);//puts the timer thread to sleep
			}
			catch (InterruptedException e)
			{ 
				break; 
			}
			   
			bidTime = bidTime - 1; //Takes away a second i.e. counting down
		   
			if(bidTime == 0) //When the auction is over
			{
				System.out.println("\nTimer has finished");
				System.out.println("\nThe auction is over");
				bidTime = 60; //Resets the timer
				server.winnerBroadCast();
				server.startAuction(); //Restarts the auction
			}		
		}
	}
	public int getTime()
	{
		return bidTime;
	}
	
	public void restart() //When a new bid is accepted, the bid time is reset to 40
	{
		bidTime = 60;
		System.out.println("\nBid accepted, timer has been reset.");
	}
}