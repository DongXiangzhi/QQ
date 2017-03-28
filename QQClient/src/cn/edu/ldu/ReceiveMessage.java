package cn.edu.ldu;

import cn.edu.ldu.util.Message;
import cn.edu.ldu.util.Translate;
import java.applet.Applet;
import java.applet.AudioClip;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.URL;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

/**
 * 功能ReceiveMessage客户机接收消息和处理消息的线程类
 * @author  董相志，版权所有2016--2018，upsunny2008@163.com
 */
public class ReceiveMessage extends Thread{
    private DatagramSocket clientSocket; //会话套接字
    private ClientUI parentUI; //父类
    private byte[] data=new byte[8096]; //8K字节数组
    private DefaultListModel listModel=new DefaultListModel(); //列表Model
    //构造函数
    public ReceiveMessage(DatagramSocket socket,ClientUI parentUI) {
        clientSocket=socket; //会话套接字
        this.parentUI=parentUI; //父类
    }   
    @Override
    public void run() {
        while (true) { //无限循环，处理收到的各类消息
          try {
            DatagramPacket packet=new DatagramPacket(data,data.length); //构建接收报文
            clientSocket.receive(packet); //接收           
            Message msg=(Message)Translate.ByteToObject(data);//还原消息对象
            String userId=msg.getUserId(); //当前用户id
            //根据消息类型分类处理
            if (msg.getType().equalsIgnoreCase("M_LOGIN")) { //是其他用户的登录消息
                playSound("/cn/edu/ldu/sound/fadeIn.wav");//上线提示音  
                //更新消息窗口
                parentUI.txtArea.append(userId+" 昂首挺胸进入聊天室...\n");
                //新上线用户加入列表
                listModel.add(listModel.getSize(), userId);
                parentUI.userList.setModel(listModel);
            }else if (msg.getType().equalsIgnoreCase("M_ACK")) { //是服务器确认消息
                //登录成功，将自己加入用户列表
                listModel.add(listModel.getSize(), userId);
                parentUI.userList.setModel(listModel);
            }else if (msg.getType().equalsIgnoreCase("M_MSG")) { //是普通会话消息
                playSound("/cn/edu/ldu/sound/msg.wav");//消息提示音  
                //更新消息窗口
                parentUI.txtArea.append(userId+" 说："+msg.getText()+"\n");
            }else if (msg.getType().equalsIgnoreCase("M_QUIT")) { //是其他用户下线消息
                playSound("/cn/edu/ldu/sound/leave.wav");//消息提示音  
                //更新消息窗口
                parentUI.txtArea.append(userId+" 大步流星离开聊天室...\n");
                //下线用户从列表删除
                listModel.remove(listModel.indexOf(userId));
                parentUI.userList.setModel(listModel);
            }//end if  
          }catch (Exception ex) {
              JOptionPane.showMessageDialog(null, ex.getMessage(),"错误提示",JOptionPane.ERROR_MESSAGE);
          }//end try
        } //end while
    }//end run
    /**
     * 播放声音文件
     * @param filename 声音文件路径和名称
     */
    private void playSound(String filename) {
        URL url = AudioClip.class.getResource(filename);
        AudioClip sound;
        sound = Applet.newAudioClip(url);
        sound.play();
    }
}//end class
