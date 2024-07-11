package sockets;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;;

public class Cliente {

	public static void main(String[] args) {
		MarcoCliente mimarco=new MarcoCliente();
		mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}


class MarcoCliente extends JFrame{
	public MarcoCliente(){	
		setBounds(600,300,280,350);			
		LaminaMarcoCliente milamina=new LaminaMarcoCliente();	
		add(milamina);	
		setVisible(true);	
		addWindowListener(new EnvioOnline());	
		}	
	
}

//Evento de ventana
//-------- Enviamos de la señal online ------------//
class EnvioOnline extends WindowAdapter{
	public void windowOpened(WindowEvent e) {
		try {
			//Creamos socket
			Socket misocket= new Socket("192.168.0.11", 9999);	
			PaqueteEnvio datos=new PaqueteEnvio();		
			datos.setMensaje(" online");		
			ObjectOutputStream paquete_datos=new ObjectOutputStream(misocket.getOutputStream());		
			paquete_datos.writeObject(datos);		
			misocket.close();
			
		}catch(Exception e2) {}
	}
}

//Para que esta clase este a la escucha constanteme de los mensajes del servidor creamos un hilo
class LaminaMarcoCliente extends JPanel implements Runnable {
	
	public LaminaMarcoCliente(){	
		//Preguntar al iniciar tu nick
		String nick_usuario=JOptionPane.showInputDialog("Nickname: ");		
		
		//Nick
		JLabel n_nick=new JLabel("Nick: ");		
		add(n_nick);		
		nick=new JLabel();	
		nick.setText(nick_usuario);		
		add(nick);
		
		//Online
		JLabel texto=new JLabel("/ Online: ");
		add(texto);
		
		//IPs
		ip=new JComboBox();	
		//Añadimos la IPs
		add(ip);
		
		
		//Campo de texto
		campochat=new JTextArea(12,20);	
		add(campochat);
	
		campo1=new JTextField(20);
		add(campo1);			
		miboton=new JButton("Enviar");		
		EnviaTexto mievento=new EnviaTexto();	
		miboton.addActionListener(mievento);	
		add(miboton);	
		
		//Ejeccion del Hilo en segundo plano
		Thread mihilo=new Thread(this);
		mihilo.start();
		
	}
	
	
	private class EnviaTexto implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	//Concatenamos los mensajes
	    	campochat.append("\n" + campo1.getText());
	    	
	    	try {
	    	    //Apertura del Socket
	    		Socket misocket = new Socket("192.168.0.11", 9999);
	    	    
	    	    //Envio de datos (Nick, IP, Mensaje)
	    	    PaqueteEnvio datos = new PaqueteEnvio();
	    	    
	    	    //Obteniendo datos
	    	    datos.setNick(nick.getText());
	    	    datos.setIp(ip.getSelectedItem().toString());
	    	    datos.setMensaje(campo1.getText());
	    	    
	    	    //Envio de objetos para el flujo de datos
	    	    ObjectOutputStream paquete_datos = new ObjectOutputStream(misocket.getOutputStream());
	    	    //Escribir en el objeto
	    	    paquete_datos.writeObject(datos);
	    	    
	    	    //Cierre del Socket
	    	    misocket.close();

	    	  	    	    
	    	} catch (UnknownHostException e1) {
	    	    e1.printStackTrace();
	    	    
	    	} catch (IOException e1) {
	    	    System.out.println(e1.getMessage());
	    	}
	    	
	    }
	}	

	
	
	private JTextField campo1;	
	private JComboBox ip;	
	private JLabel nick;	
	private JTextArea campochat;	
	private JButton miboton;
	
	
	@Override
	//Codificacion para estar a la escucha
	public void run() {
		// TODO Auto-generated method stub
		try {
			//Socket de escucha
		    ServerSocket servidor_cliente = new ServerSocket(9090);
		    //Socket por donde recibe el paquete
		    Socket cliente;
		    //Almacenar paquete recibido
		    PaqueteEnvio paqueteRecibido;
		    
		    //Para la escucha permamente
		    while (true) {
		    	//Acepta todas las conecciones
		        cliente = servidor_cliente.accept();
		        //Flujo de entrada 
		        ObjectInputStream flujoentrada = new ObjectInputStream(cliente.getInputStream());
		        paqueteRecibido = (PaqueteEnvio) flujoentrada.readObject();
		        
		        if(!paqueteRecibido.getMensaje().equals(" online")) {
		        	//Poner el mensaje
		        	campochat.append("\n" + paqueteRecibido.getNick()+": "+paqueteRecibido.getMensaje());
		        } else {
		        	//campochat.append("\n" + paqueteRecibido.getIps());
		        	
		        	ArrayList<String> IpsMenu=new ArrayList<String>();
		        	IpsMenu=paqueteRecibido.getIps();        	
		        	ip.removeAllItems();
		        	
		        	for(String z:IpsMenu) {
		        		ip.addItem(z);
		        	}
		        }    
		    }
		} catch (Exception e) {
		    System.out.println(e.getMessage());
		}
	}	
}

//Serializamos esta clase para el envio de paquetes en la red
//Objeto de Nick, IP, Mensaje (Empapetamiento)
class PaqueteEnvio implements Serializable {
	//Atributos
    private String nick, ip, mensaje;
    private ArrayList<String> Ips;

    //Setter y Getters
	public ArrayList<String> getIps() {
		return Ips;
	}

	public void setIps(ArrayList<String> ips) {
		Ips = ips;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
}
