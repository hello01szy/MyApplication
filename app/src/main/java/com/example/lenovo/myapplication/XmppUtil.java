package com.example.lenovo.myapplication;

import android.util.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XmppUtil {
    private static final String host = "192.168.43.208";
    private static final int port = 5222;
    private static XMPPTCPConnection connection = null;
    private static final String serveName = "192.168.43.208";

    //用于连接服务器
    public static boolean connect()
    {
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        try {
            configBuilder.setXmppDomain(serveName);
            configBuilder.setHost(host);
            configBuilder.setHostAddress(InetAddress.getByName("192.168.43.208"));
            configBuilder.setPort(port);
            configBuilder.setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled);
            configBuilder.enableDefaultDebugger();
            configBuilder.setUsernameAndPassword("admin", "openfireadmin");
            connection = new XMPPTCPConnection(configBuilder.build());
            connection.connect();
            return true;
        }  catch (UnknownHostException e) {
            e.printStackTrace();
            Log.v("XMPPUTIL","InetAddress");
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException e) {
            e.printStackTrace();
            return false;
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
    }
    //判断是否连接
    public static boolean checkConnection() {
        if(connection == null)
            return false;
        else if(!connection.isConnected())
        {
            return false;
        }
        return true;
    }
    //获取connection实例
    public static XMPPTCPConnection getConnection()
    {
        return connection;
    }
    //获取聊天管理器
    static ChatManager getChatManager()
    {
        if(XmppUtil.checkConnection())
        {
            ChatManager chatManager = ChatManager.getInstanceFor(XmppUtil.getConnection());
            return chatManager;
        }
        return null;
    }
    //获取所有分组名称
    public static void getAllGroup()
    {
        Roster roster = Roster.getInstanceFor(connection);
        Collection<RosterGroup> entriesGroup = roster.getGroups();
        for(RosterGroup group: entriesGroup){
            Log.v("Xmpp", group.getName());
            for(RosterEntry entry : group.getEntries())
            {
                Log.v("Xmpp", entry.getName() + " " + entry.getJid());
            }
        }
    }
}
