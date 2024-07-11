package sockets;

import javax.swing.*;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Servidor  {

	public static void main(String[] args) {
	
		MarcoServidor mimarco=new MarcoServidor();		
		mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
	}	
}

class MarcoServidor extends JFrame implements Runnable {
	
	public MarcoServidor(){		
		setBounds(1200,300,280,350);						
		JPanel milamina= new JPanel();	
		milamina.setLayout(new BorderLayout());	
		areatexto=new JTextArea();	
		milamina.add(areatexto,BorderLayout.CENTER);	
		add(milamina);		
		setVisible(true);
		
		Thread mihilo=new Thread(this);
		mihilo.start();
		
		}
	
	private	JTextArea areatexto;

	//Hilo que se ejecuta continuamente en segundo plano
	@Override
	public void run() {

		try {
			//Colocamos el servidor a la escucha
			ServerSocket servidor = new ServerSocket(9999);
			
			//Variables para almacenar lo que llega por la red
			String nick, ip, mensaje;
			
			//Se lee una vez
			ArrayList <String> listaIp=new ArrayList<String>();
			
			//Instancia del paquete enviado
			PaqueteEnvio paquete_recibido;
			
			//Ejecucion en segunda plano para los mensajes
			while(true) {

				Socket misocket=servidor.accept();
				
				//Recibir objetos hacia adentro
				ObjectInputStream paquete_datos = new ObjectInputStream(misocket.getInputStream());
				//Guardamos lo recibido
				paquete_recibido = (PaqueteEnvio) paquete_datos.readObject();

				//Asignamos lo capturado a las variables
				nick=paquete_recibido.getNick();
				ip=paquete_recibido.getIp();
				mensaje=paquete_recibido.getMensaje();
				
				//Si no esta online
				if(!mensaje.equals(" online")) {
					
				//Insertamos en el servidor
				areatexto.append("\n" + nick + ": " + mensaje + " para " + ip);

				//Socket para reenviar al destino
				Socket enviaDestinatario = new Socket(ip, 9090);
				
				//Reenvio de paquete
				ObjectOutputStream paqueteReenvio = new ObjectOutputStream(enviaDestinatario.getOutputStream());
				//Insetamos la informacion para el reenvio
				paqueteReenvio.writeObject(paquete_recibido);
				
				//Cerramos el flujo de datos
				paqueteReenvio.close();
				
				//Cerramos el socket
				enviaDestinatario.close();
				
				//Si es la primera vez que el cliente se conecta
				misocket.close();} else {
					
					//-------------DETECTA ONLINE---------------//
					
					//Almacenamos la direccion de quien se conecta
					//Aqui detectamos el IP de quien entro en linea
					InetAddress localizacion=misocket.getInetAddress();
					String IpRemota=localizacion.getHostAddress();
					
					System.out.println("Online " + IpRemota);
					
					listaIp.add(IpRemota);
					
					//Ingresamos el arraylist
					paquete_recibido.setIps(listaIp);
					
					for(String z:listaIp) {
						System.out.print("Array: "+ z);
						
						Socket enviaDestinatario = new Socket(z, 9090);
						
						ObjectOutputStream paqueteReenvio = new ObjectOutputStream(enviaDestinatario.getOutputStream());
						paqueteReenvio.writeObject(paquete_recibido);
						paqueteReenvio.close();
						enviaDestinatario.close();
						misocket.close();
					}
				}
			}

			//Capturamos las esepciones
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
