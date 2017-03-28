package cn.edu.ldu.util;

import java.net.DatagramPacket;

/**
 * User类，定义用户对象，包含用户名和收到的报文
 * @author 董相志，版权所有2016--2018，upsunny2008@163.com
 */
public class User {
    private String userId=null; //用户id
    private DatagramPacket packet=null; //报文
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public DatagramPacket getPacket() {
        return packet;
    }
    public void setPacket(DatagramPacket packet) {
        this.packet = packet;
    }   
}
