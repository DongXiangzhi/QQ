package cn.edu.ldu;

import cn.edu.ldu.util.Message;
import cn.edu.ldu.util.Translate;
import cn.edu.ldu.util.User;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * ReceiveMessage，服务器接收消息和处理消息的线程类
 * @author 董相志，版权所有2016--2018，upsunny2008@163.com
 */
public class ReceiveMessage extends Thread {
    private DatagramSocket serverSocket; //服务器套接字
    private DatagramPacket packet;  //报文
    private List<User> userList=new ArrayList<User>(); //用户列表
    private byte[] data=new byte[8096]; //8K字节数组
    private ServerUI parentUI; //消息窗口  
    
    /**
     * 构造函数
     * @param socket 会话套接字
     * @param parentUI 父类
     */
    public ReceiveMessage(DatagramSocket socket,ServerUI parentUI) {
        serverSocket=socket;
        this.parentUI=parentUI;
    }
    @Override
    public void run() {  
        while (true) { //循环处理收到的各种消息
            try {
            packet=new DatagramPacket(data,data.length);//构建接收报文
            serverSocket.receive(packet);//接收客户机数据
            //收到的数据转为消息对象
            Message msg=(Message)Translate.ByteToObject(packet.getData());
            String userId=msg.getUserId();//当前消息来自用户的id            
            if (msg.getType().equalsIgnoreCase("M_LOGIN")) { //是M_LOGIN消息 
                Message backMsg=new Message();
                //假定只有2000、3000、8000三个帐号可以登录
                if (!userId.equals("2000") && !userId.equals("3000") && !userId.equals("8000")) {//登录不成功
                    backMsg.setType("M_FAILURE");
                    byte[] buf=Translate.ObjectToByte(backMsg);
                    DatagramPacket backPacket=new DatagramPacket(buf,buf.length,packet.getAddress(),packet.getPort());//向登录用户发送的报文
                    serverSocket.send(backPacket); //发送                  
                }else { //登录成功
                    backMsg.setType("M_SUCCESS");
                    byte[] buf=Translate.ObjectToByte(backMsg);
                    DatagramPacket backPacket=new DatagramPacket(buf,buf.length,packet.getAddress(),packet.getPort());//向登录用户发送的报文
                    serverSocket.send(backPacket); //发送   
                    
                    User user=new User();
                    user.setUserId(userId); //用户名
                    user.setPacket(packet); //保存收到的报文
                    userList.add(user); //将新用户加入用户列表
                    
                    //更新服务器聊天室大厅
                    parentUI.txtArea.append(userId+" 登录！\n");
                    
                    //向所有其他在线用户发送M_LOGIN消息，向新登录者发送整个用户列表
                    for (int i=0;i<userList.size();i++) { //遍历整个用户列表                                       
                        //向其他在线用户发送M_LOGIN消息
                        if (!userId.equalsIgnoreCase(userList.get(i).getUserId())){
                            DatagramPacket oldPacket=userList.get(i).getPacket(); 
                            DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort());//向其他用户发送的报文
                            serverSocket.send(newPacket); //发送
                        }//end if
                        //向当前用户回送M_ACK消息，将第i个用户加入当前用户的用户列表
                        Message other=new Message();
                        other.setUserId(userList.get(i).getUserId());
                        other.setType("M_ACK");
                        byte[] buffer=Translate.ObjectToByte(other);
                        DatagramPacket newPacket=new DatagramPacket(buffer,buffer.length,packet.getAddress(),packet.getPort());
                        serverSocket.send(newPacket);
                    }//end for                  
                }//end if                           
            }else if (msg.getType().equalsIgnoreCase("M_MSG")) { //是M_MSG消息
                //更新显示
                parentUI.txtArea.append(userId+" 说："+msg.getText()+"\n");
                //转发消息
                for (int i=0;i<userList.size();i++) { //遍历用户
                    DatagramPacket oldPacket=userList.get(i).getPacket();
                    DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort()); 
                    serverSocket.send(newPacket); //发送
                }
            }else if (msg.getType().equalsIgnoreCase("M_QUIT")) { //是M_QUIT消息
                //更新显示
                parentUI.txtArea.append(userId+" 下线！\n");
                //删除用户
                for(int i=0;i<userList.size();i++) {
                    if (userList.get(i).getUserId().equals(userId)) {
                        userList.remove(i);
                        break;
                    }
                }//end for
                //向其他用户转发下线消息
                for (int i=0;i<userList.size();i++) {
                    DatagramPacket oldPacket=userList.get(i).getPacket();
                    DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort());
                    serverSocket.send(newPacket);
                }//end for 
            }//end if
            } catch (IOException | NumberFormatException ex) {  }
        }//end while
    }//end run
}//end class
