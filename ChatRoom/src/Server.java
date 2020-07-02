

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
 * 运行在服务端的SeverSocket主要负责；
 * 1 向系统申请服务端口号客户端是通过这个端口号与之连接的
 * 2 监听申请的服务端口号，当一个客户端通过该端口号尝试建立联系连接时，
 *      SeverSocket会在服务端创建一个Socket与客户端建立连接
 *       服务端正对不同客户端建立多个Socket
 * 
 * @author bhk 
 *
 */
public class Server {
    
    public ServerSocket server;
    /*
     * 用来保存客户端输出流的集合，
     *因为线程安全的也不和遍历互斥，要自己维护也可以保证安全
     */
    private List<PrintWriter> allOut;    
    private List<String> online_list=new ArrayList<>();
    
    /*
     *初始化 服务端
     */
    public Server()throws Exception  {
        /*
         * 初始化的同时申请端口号
         */
        server = new ServerSocket(8780);
     
        allOut = new ArrayList<PrintWriter>();
    }
    
    /**
     * 将给定的输出流存入共享集合
     * @param out
     */
    private synchronized void addOut(PrintWriter out) {
        allOut.add(out);
    }
    /**
     * 将给定的输出流从共享集合中删除
     * @param out
     */
    private synchronized void removeOut(PrintWriter out) {
        allOut.remove(out);
    }
    /**
     * 将给定的消息发送给所有客户端
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
    
    /*服务端开始工作的方法
     * 
     */
    public void start() {
        try {
            /*
             * ServerSocket的accept的方法
             * 是一个阻塞的方法，作用是监听服务端口号，知道一个客户端；连接并创建一个Socket，使用该Socket
             *     即可与刚才链接的客户端进行交互
             */
            
            while(true) {
                System.out.println("等待客户端连接...");
                Socket socket = server.accept();
                System.out.println("一个客户端连接了！");
                
                /*
                 * 启动一个线程，来完成与该客户端的交互
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
            System.out.println("服务端建立联系失败！");
        }
    }
    
    /**
     *该线程负责处理一个客户端的交互
     * 
     */
    class ClientHandler implements Runnable{
        /*
         * 该线程处理的客户端的Socket
         */
        private Socket socket ;
         // 客户端的地址信息，区分不同客户端
        private String host;
        
        //用户的昵称
        private String nickName;
        private String password;
        Map<String,String> map= new HashMap<>();
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
            /*
             * 通过Socket可以获取远端计算机的地址信息
             */
            InetAddress address = socket.getInetAddress();
            //获取IP地址
            host = address.getHostAddress();
            //添加用户
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
                 * Socket 提供的方法
                 * InputStream getInputStream()
                 * 该方法可以获取一个输入流，从该方法读取的数据就是从远端计算机发送来的 
                 */
                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in,"UTF-8");
                BufferedReader br = new BufferedReader(isr);
                
                //首先读取一行字符串为昵称
                nickName =  br.readLine();
                password=br.readLine();
                System.out.println("account:"+nickName+" password:"+password);
                /*
                 * 通过Socket创建输出流用于将消息发送给客户端
                 */
                OutputStream out = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(out,"UTF-8");
                 pw = new PrintWriter(osw,true);
                /*
                 * 将该客户端对的输出流存入到共享集合中
                 */
                //System.out.println(host+"上线了");
                if(!map.containsKey(nickName)||!map.get(nickName).equals(password)) {
                	pw.println("bad1");
                	//socket.close();
                	System.out.println("test");
                }else {
                	if(online_list.contains(nickName)) {
                		pw.println("bad2");//账户已经登录了，排斥多个账户同时登陆
                		 for(String online_name:online_list)
             		    	System.out.println("online_name:"+online_name);
                		
                	}else {
                		    for(String online_name:online_list)
                		    	System.out.println("online_name:"+online_name);
			                pw.println("good");//登录成功
			                System.out.println("test!!!!");
			                sendMessage("0,"+nickName,pw);
			                online_list.add(nickName);
			                addOut(pw);
			                //将现有的在线用户发送给客户端
			                for(String online_name:online_list) {
			                	System.out.println("online_name:"+online_name);
			                	pw.println("4,"+online_name);
			                }
			                
			                
			                String message = null;
			                /*
			                 * br.readLine()在读取客户户端发送过来的消息时，由于客户端断线，
			                 * 而操作系统的不同，这里读取后的结果不同：
			                 */
			                
			                while((message = br.readLine())!=null) {
			                    System.out.println(nickName+"说："+ message);
			                    //pw.println(host+"说："+message);
			                     if(message.equals("CLOSE_KKchat_@")) {
			                    	 break;
			                     }
			                    //广播消息
			                    sendMessage("3,"+nickName+"说："+message,pw);
			                }
			              	System.out.println("一个用户下线了1");
			                online_list.remove(nickName);
			                sendMessage("1,"+nickName,pw);
                	}
                }
            } catch (Exception e) {
            	System.out.println("一个用户下线了2");
                online_list.remove(nickName);
                sendMessage("1,"+nickName,pw);
               e.printStackTrace();
            }finally {
                /*
                 * 处理当前客户端断开后的逻辑
                 */
                //将该客户端的输出流从共享集合中删除
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