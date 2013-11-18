/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.chatroomslist;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.main.chat.*;
import net.java.sip.communicator.impl.gui.main.chat.conference.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.plugin.desktoputil.*;
import net.java.sip.communicator.plugin.desktoputil.chat.*;
import net.java.sip.communicator.service.muc.ChatRoomWrapper;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.resources.*;
import net.java.sip.communicator.util.*;

import org.jitsi.service.resources.*;

/**
 * The <tt>ChatRoomsListRightButtonMenu</tt> is the menu, opened when user clicks
 * with the right mouse button on the chat rooms list panel. It's the one that
 * contains the create chat room item.
 *
 * @author Yana Stamcheva
 * @author Lubomir Marinov
 */
public class ChatRoomRightButtonMenu
    extends SIPCommPopupMenu
    implements ActionListener
{
    /**
     * The current chat room wrapper.
     */
    private ChatRoomWrapper chatRoomWrapper;

    /**
     * Creates an instance of <tt>ChatRoomsListRightButtonMenu</tt>.
     * @param chatRoomWrapper the chat room wrapper, corresponding to the
     * selected chat room
     */
    public ChatRoomRightButtonMenu(ChatRoomWrapper chatRoomWrapper)
    {
        this.chatRoomWrapper = chatRoomWrapper;

        this.setLocation(getLocation());

        createMenuItem(
                "service.gui.OPEN",
                ImageLoader.CHAT_ROOM_OFFLINE_ICON,
                "openChatRoom");
        JMenuItem joinChatRoomItem
            = createMenuItem(
                    "service.gui.JOIN",
                    ImageLoader.JOIN_ICON,
                    "joinChatRoom");
        JMenuItem joinAsChatRoomItem
            = createMenuItem(
                    "service.gui.JOIN_AS",
                    ImageLoader.JOIN_AS_ICON,
                    "joinAsChatRoom");
        JMenuItem leaveChatRoomItem
            = createMenuItem(
                    "service.gui.LEAVE",
                    ImageLoader.LEAVE_ICON,
                    "leaveChatRoom");
        createMenuItem(
                "service.gui.REMOVE",
                ImageLoader.REMOVE_CHAT_ICON,
                "removeChatRoom");
        JMenuItem nickNameChatRoomItem
            = createMenuItem(
                    "service.gui.CHANGE_NICK",
                    ImageLoader.RENAME_16x16_ICON,
                    "nickNameChatRoom");

        ChatRoom chatRoom = chatRoomWrapper.getChatRoom();

        if ((chatRoom != null) && chatRoom.isJoined())
        {
            joinAsChatRoomItem.setEnabled(false);
            joinChatRoomItem.setEnabled(false);
        }
        else
            leaveChatRoomItem.setEnabled(false);
    }

    /**
     * Handles the <tt>ActionEvent</tt>. Determines which menu item was
     * selected and makes the appropriate operations.
     * @param e the event.
     */
    public void actionPerformed(ActionEvent e)
    {
        JMenuItem menuItem = (JMenuItem) e.getSource();
        String itemName = menuItem.getName();

        ConferenceChatManager conferenceManager
            = GuiActivator.getUIService().getConferenceChatManager();
        String[] joinOptions;
        String subject = null;

        if (itemName.equals("removeChatRoom"))
        {
            conferenceManager.removeChatRoom(chatRoomWrapper);
        }
        else if (itemName.equals("leaveChatRoom"))
        {
            conferenceManager.leaveChatRoom(chatRoomWrapper);
        }
        else if (itemName.equals("joinChatRoom"))
        {
            String nickName = null;

            nickName =
                ConfigurationUtils.getChatRoomProperty(
                    chatRoomWrapper.getParentProvider()
                        .getProtocolProvider(), chatRoomWrapper
                        .getChatRoomID(), "userNickName");
            if(nickName == null)
            {
                joinOptions = ChatRoomJoinOptionsDialog.getJoinOptions(
                    chatRoomWrapper.getParentProvider().getProtocolProvider(), 
                    chatRoomWrapper.getChatRoomID());
                nickName = joinOptions[0];
                subject = joinOptions[1];
            }

            if (nickName != null)
                GuiActivator.getMUCService().joinChatRoom(chatRoomWrapper, nickName, null,
                    subject);
        }
        else if (itemName.equals("openChatRoom"))
        {
            if(chatRoomWrapper.getChatRoom() == null)
            {
                // this is not a server persistent room we must create it
                // and join
                chatRoomWrapper =
                    GuiActivator.getMUCService().createChatRoom(
                            chatRoomWrapper.getChatRoomName(),
                            chatRoomWrapper.getParentProvider()
                                .getProtocolProvider(),
                            new ArrayList<String>(),
                            "",
                            false,
                            false,
                            true);
            }
            
            if(!chatRoomWrapper.getChatRoom().isJoined())
            {
                String nickName = null;

                nickName =
                    ConfigurationUtils.getChatRoomProperty(
                        chatRoomWrapper.getParentProvider()
                            .getProtocolProvider(), chatRoomWrapper
                            .getChatRoomID(), "userNickName");
                if(nickName == null)
                {
                    joinOptions = ChatRoomJoinOptionsDialog.getJoinOptions(
                        chatRoomWrapper.getParentProvider()
                            .getProtocolProvider(), 
                        chatRoomWrapper.getChatRoomID());
                    nickName = joinOptions[0];
                    subject = joinOptions[1];
                }

                if (nickName != null)
                    GuiActivator.getMUCService().joinChatRoom(chatRoomWrapper,
                        nickName, null, subject);
                else
                    return;
            }

            ChatWindowManager chatWindowManager
                = GuiActivator.getUIService().getChatWindowManager();
            ChatPanel chatPanel
                = chatWindowManager.getMultiChat(chatRoomWrapper, true);

            chatWindowManager.openChat(chatPanel, true);
        }
        else if(itemName.equals("joinAsChatRoom"))
        {
            joinOptions = ChatRoomJoinOptionsDialog.getJoinOptions(
                chatRoomWrapper.getParentProvider().getProtocolProvider(), 
                chatRoomWrapper.getChatRoomID());
            if(joinOptions[0] == null)
                return;
            GuiActivator.getMUCService()
                .joinChatRoom(chatRoomWrapper, joinOptions[0], null,
                    joinOptions[1]);
        }
        else if(itemName.equals("nickNameChatRoom"))
        {
            ChatRoomJoinOptionsDialog.getJoinOptions(true,
                chatRoomWrapper.getParentProvider().getProtocolProvider(), 
                chatRoomWrapper.getChatRoomID());
        }
    }

    /**
     * Creates a new <tt>JMenuItem</tt> and adds it to this <tt>JPopupMenu</tt>.
     *
     * @param textKey the key of the internationalized string in the resources
     * of the application which represents the text of the new
     * <tt>JMenuItem</tt>
     * @param iconID the <tt>ImageID</tt> of the image in the resources of the
     * application which represents the icon of the new <tt>JMenuItem</tt>
     * @param name the name of the new <tt>JMenuItem</tt>
     * @return a new <tt>JMenuItem</tt> instance which has been added to this
     * <tt>JPopupMenu</tt>
     */
    private JMenuItem createMenuItem(
            String textKey,
            ImageID iconID,
            String name)
    {
        ResourceManagementService resources = GuiActivator.getResources();
        JMenuItem menuItem
            = new JMenuItem(
                    resources.getI18NString(textKey),
                    new ImageIcon(ImageLoader.getImage(iconID)));

        menuItem.setMnemonic(resources.getI18nMnemonic(textKey));
        menuItem.setName(name);

        menuItem.addActionListener(this);

        add(menuItem);

        return menuItem;
    }
}
