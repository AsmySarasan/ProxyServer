/**
 * @(#)MyProxy.java
 * @author Asmy Sarasan, 100462413
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.sql.Timestamp;

public class MyProxy
{
	// The port number on which the server will be listening on
	public static final int HTTP_PORT = 7890;

	//A constructor of our class
	public MyProxy() { }
    
    public ServerSocket getServer() throws Exception
    {
      return new ServerSocket(HTTP_PORT);
    }

    // multi-threading -- create a new connection for each request
    public void run()
    {
     ServerSocket listen;
    	try
    	{
       		listen = getServer();
       		while(true)
       		{
      			Socket client = listen.accept();
       			Connects cc = new Connects(client);
     		}

     	} catch(Exception e)
     		{
       			System.out.println("Exception..."+e);
     		}
    }

    // main program
    public static void main(String argv[]) throws Exception {
      //System.setSecurityManager(new OurHttpdSecurityManager());
      MyProxy mp = new MyProxy();
      mp.run();
    }
}

class Connects extends Thread {
	Socket client;
	Socket server;

	//Creating input and output stream instances for client side
	BufferedReader ic;
	DataOutputStream oc;

	//Creating input and output stream instances for server side
	DataInputStream is;
	DataOutputStream os;

	List<String> buffer;
	//Creating and defining a constructor for this class
	public Connects(Socket s)
	{ 
		client = s;
		String domain = " "; //Initializing a string to store the host name which will be used to print logs
		try //Processing the request and parsing through it
		{
			ic = new BufferedReader(new InputStreamReader(client.getInputStream()));
			oc = new DataOutputStream(client.getOutputStream());
			buffer = new ArrayList<String>();
			String request = ic.readLine();
			String[] hostLine;
			while (ic.ready())
			{
				System.out.println(request);
				buffer.add(request);
				hostLine = request.split(" ");
				if (hostLine[0].equals("Host:"))
				{
					domain = hostLine[1];
					server = new Socket(hostLine[1], 80);
				}
				request = ic.readLine();
			}

		}catch (IOException e)
		 {
			try
			{
				client.close();
			} catch (IOException ex)
				{
					System.out.println("Error while getting socket streams.."+ex);
				}
				return;
		}
	
		try //Blocking and allowing sites along with creating logs
		{
			if(domain.contains("hello"))
			{
				oc.writeBytes("403 FORBIDDEN \n You don't have permission to access on this server");
				
				try(FileWriter fw = new FileWriter("log.txt", true);
    			BufferedWriter bw = new BufferedWriter(fw);
   				PrintWriter log = new PrintWriter(bw))
				{
					Date date = new Date();
				    log.println(domain + "    " + new Timestamp(date.getTime()) + " <SITE WAS BLOCKED>\n");
				} catch (IOException e1)
				  {
    				e1.printStackTrace();
			      }
				oc.close();
			}
			else
			{
				try(FileWriter fw = new FileWriter("log.txt", true);
    			BufferedWriter bw = new BufferedWriter(fw);
   				PrintWriter log = new PrintWriter(bw))
				{
					Date date = new Date();
				    log.println(domain + "    " + new Timestamp(date.getTime()) + " <ALLOWED>\n");
				} catch (IOException e2)
				  {
    				e2.printStackTrace();
				  }
				this.start(); // Thread starts here...this start() will call run()
			}
		}catch (Exception e3)
		 {
			System.out.println("Error received: "+ e3);
		 }
	}

	//Defining the processes taking place in server
	public void run()
	{
		try
		{			
			is = new DataInputStream(server.getInputStream());
			os = new DataOutputStream(server.getOutputStream());

			String data = "";
			for (String str : buffer)
			{
				System.out.println("SENDING..... " + str);
				data += str + "\r\n";
			}

			os.writeBytes(data + "\r\n");
			byte[] buf = new byte[9000];
			int counter = is.read(buf);
			
			while(counter != -1)
			{
				System.out.println("Byte count: " + counter);
				oc.write(buf,0,counter);
				counter = is.read(buf);
			}
			
			client.close();
			server.close();
			is.close();
			os.close();
		} catch ( IOException e ) {
			System.out.println( "I/O error " + e);
		} catch (Exception ex) {
			System.out.println("Exception: "+ ex);
		}       
	}
}