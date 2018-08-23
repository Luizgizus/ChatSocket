package Chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Servidor extends Thread {
	private static HashMap<String, BufferedWriter> clientes;
	private static ServerSocket server; 
	private String nome;
	private Socket con;
	private InputStream in;  
	private InputStreamReader inr;  
	private BufferedReader bfr;
	
	public Servidor(Socket con){
        this.con = con;
        try {
            in  = con.getInputStream();
            inr = new InputStreamReader(in);
            bfr = new BufferedReader(inr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String msg;
            OutputStream ou =  this.con.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw); 
            nome = msg = bfr.readLine();
            clientes.put(nome, bfw);

            while(!"Sair".equalsIgnoreCase(msg) && msg != null) {
                msg = bfr.readLine();
                if(msg.startsWith("@")) {
                    int indexOfSpace = msg.indexOf(" ");
                    String name = msg.substring(1, indexOfSpace);
                    String message = msg.substring(indexOfSpace, msg.length());

                    sendEspecifc(bfw, message, name);
                } else if (msg.startsWith("getListUsers")) {
                	String nome = msg.split("@")[1].trim();
                	sendUsersName(nome);
                } else {
                    sendToAll(bfw, msg);
                }
            }
            
            clientes.remove(nome);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToAll(BufferedWriter bwSaida, String msg) throws  IOException {
        clientes.forEach((k,v) -> {
            BufferedWriter bwS;
            bwS = (BufferedWriter)v;
            if(!(bwSaida == bwS)) {
                try {
                    v.write(nome + " -> " + msg+"\r\n");
                    v.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
        });
    }

    public void sendUsersName(String nome) throws  IOException {
    	BufferedWriter bw = clientes.get(nome);
    	bw.write("nomes: " + clientes.keySet().toString());
    	bw.flush(); 
    }
    
    public void sendEspecifc(BufferedWriter bwSaida, String msg, String nome) throws  IOException {
        BufferedWriter bw = clientes.get(nome);
        bw.write(nome + " -> " + msg+"\r\n");
        bw.flush(); 
    }

    public static void main(String []args) {
        try{
            server = new ServerSocket(8000);
            clientes = new HashMap<String, BufferedWriter>();

            while(true) {
                Socket con = server.accept();
                Thread t = new Servidor(con);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}