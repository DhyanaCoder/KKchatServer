

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * �����ڷ���˵�SeverSocket��Ҫ����
 * 1 ��ϵͳ�������˿ںſͻ�����ͨ������˿ں���֮���ӵ�
 * 2 ��������ķ���˿ںţ���һ���ͻ���ͨ���ö˿ںų��Խ�����ϵ����ʱ��
 *      SeverSocket���ڷ���˴���һ��Socket��ͻ��˽�������
 *       ��������Բ�ͬ�ͻ��˽������Socket
 * 
 * @author bhk 
 *
 */
public class Server {
    
    public ServerSocket server;
    /*
     * ��������ͻ���������ļ��ϣ�
     *��Ϊ�̰߳�ȫ��Ҳ���ͱ������⣬Ҫ�Լ�ά��Ҳ���Ա�֤��ȫ
     */
    private List<PrintWriter> allOut;    
    private List<String> online_list=new ArrayList<>();
    
    /*
     *��ʼ�� �����
     */
    public Server()throws Exception  {
        /*
         * ��ʼ����ͬʱ����˿ں�
         */
        server = new ServerSocket(8780);
     
        allOut = new ArrayList<PrintWriter>();
    }
    
    /**
     * ����������������빲����
     * @param out
     */
    private synchronized void addOut(PrintWriter out) {
        allOut.add(out);
    }
    /**
     * ��������������ӹ�������ɾ��
     * @param out
     */
    private synchronized void removeOut(PrintWriter out) {
        allOut.remove(out);
    }
    /**
     * ����������Ϣ���͸����пͻ���
     * @param out
     */
    private synchronized void  sendMessage(String message,PrintWriter pw) {
        for(PrintWriter out : allOut) {
         if(out.equals(pw)) {
       	   continue;
         }
            out.println(message);
        }
    }
    
    /*����˿�ʼ�����ķ���
     * 
     */
    public void start() {
        try {
            /*
             * ServerSocket��accept�ķ���
             * ��һ�������ķ����������Ǽ�������˿ںţ�֪��һ���ͻ��ˣ����Ӳ�����һ��Socket��ʹ�ø�Socket
             *     ������ղ����ӵĿͻ��˽��н���
             */
            
            while(true) {
                System.out.println("�ȴ��ͻ�������...");
                Socket socket = server.accept();
                System.out.println("һ���ͻ��������ˣ�");
                
                /*
                 * ����һ���̣߳��������ÿͻ��˵Ľ���
                 */
                ClientHandler handler= new ClientHandler(socket);
                Thread t = new Thread(handler);
                t.start();    
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("����˽�����ϵʧ�ܣ�");
        }
    }
    
    /**
     *���̸߳�����һ���ͻ��˵Ľ���
     * 
     */
    class ClientHandler implements Runnable{
        /*
         * ���̴߳���Ŀͻ��˵�Socket
         */
        private Socket socket ;
         // �ͻ��˵ĵ�ַ��Ϣ�����ֲ�ͬ�ͻ���
        private String host;
        
        //�û����ǳ�
        private String nickName;
        private String password;
        Map<String,String> map= new HashMap<>();
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
            /*
             * ͨ��Socket���Ի�ȡԶ�˼�����ĵ�ַ��Ϣ
             */
            InetAddress address = socket.getInetAddress();
            //��ȡIP��ַ
            host = address.getHostAddress();
            //����û�
            map.put("bhk", "123456");
            map.put("u1", "123456");
            map.put("u2", "123456");
            map.put("u3","123456");
            map.put("u4", "123456");
        }
        public void run() {
            PrintWriter pw = null;
            try {
            	System.out.println("test");
                /*
                 * Socket �ṩ�ķ���
                 * InputStream getInputStream()
                 * �÷������Ի�ȡһ�����������Ӹ÷�����ȡ�����ݾ��Ǵ�Զ�˼������������ 
                 */
                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in,"UTF-8");
                BufferedReader br = new BufferedReader(isr);
                
                //���ȶ�ȡһ���ַ���Ϊ�ǳ�
                nickName =  br.readLine();
                password=br.readLine();
                System.out.println("account:"+nickName+" password:"+password);
                /*
                 * ͨ��Socket������������ڽ���Ϣ���͸��ͻ���
                 */
                OutputStream out = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(out,"UTF-8");
                 pw = new PrintWriter(osw,true);
                /*
                 * ���ÿͻ��˶Ե���������뵽��������
                 */
                //System.out.println(host+"������");
                if(!map.containsKey(nickName)||!map.get(nickName).equals(password)) {
                	pw.println("bad1");
                	//socket.close();
                	System.out.println("test");
                }else {
                	if(online_list.contains(nickName)) {
                		pw.println("bad2");//�˻��Ѿ���¼�ˣ��ų����˻�ͬʱ��½
                		 for(String online_name:online_list)
             		    	System.out.println("online_name:"+online_name);
                		
                	}else {
                		    for(String online_name:online_list)
                		    	System.out.println("online_name:"+online_name);
			                pw.println("good");//��¼�ɹ�
			                System.out.println("test!!!!");
			                sendMessage("0,"+nickName,pw);
			                online_list.add(nickName);
			                addOut(pw);
			                //�����е������û����͸��ͻ���
			                for(String online_name:online_list) {
			                	System.out.println("online_name:"+online_name);
			                	pw.println("4,"+online_name);
			                }
			                
			                
			                String message = null;
			                /*
			                 * br.readLine()�ڶ�ȡ�ͻ����˷��͹�������Ϣʱ�����ڿͻ��˶��ߣ�
			                 * ������ϵͳ�Ĳ�ͬ�������ȡ��Ľ����ͬ��
			                 */
			                
			                while((message = br.readLine())!=null) {
			                    System.out.println(nickName+"˵��"+ message);
			                    //pw.println(host+"˵��"+message);
			                     if(message.equals("CLOSE_KKchat_@")) {
			                    	 break;
			                     }
			                    //�㲥��Ϣ
			                    sendMessage("3,"+nickName+"˵��"+message,pw);
			                }
			              	System.out.println("һ���û�������1");
			                online_list.remove(nickName);
			                sendMessage("1,"+nickName,pw);
                	}
                }
            } catch (Exception e) {
            	System.out.println("һ���û�������2");
                online_list.remove(nickName);
                sendMessage("1,"+nickName,pw);
               e.printStackTrace();
            }finally {
                /*
                 * ����ǰ�ͻ��˶Ͽ�����߼�
                 */
                //���ÿͻ��˵�������ӹ�������ɾ��
            	System.out.println("!!!!!!");
            	removeOut(pw);
              
               
                
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }    
        }
        
    }
    
}