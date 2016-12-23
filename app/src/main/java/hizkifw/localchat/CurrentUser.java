package hizkifw.localchat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by HizkiFW on 21/12/2016.
 */
public class CurrentUser {
	public static User thisUser;
	public static List<Room> activeRooms;
	public static List<Long> activeUsers;

	private static boolean initialized = false;

	public static void init() {
		if(initialized) return;

		thisUser = new User("User");
		try {
			thisUser.address = InetAddress.getLocalHost();
		} catch(Exception e) {
			e.printStackTrace();
		}

		activeRooms = new ArrayList<>();
		activeRooms.add(new Room((byte) 0, "Global"));
		activeUsers = new ArrayList<>();

		initialized = true;
	}

	public static boolean roomExists(byte roomId) {
		for(int i = 0; i < CurrentUser.activeRooms.size(); i++) {
			if(CurrentUser.activeRooms.get(i).roomNumber == roomId) {
				return true;
			}
		}
		return false;
	}
}

class User {
	public InetAddress address;
	public String nickname;
	public byte roomNumber = 0;

	public User(String nick) {
		nickname = nick;
	}
}
class Room {
	public byte roomNumber;
	public String roomName;
	public String lastMessage;
	public List<User> users = new ArrayList<>();
	public List<ChatEntry> messages = new ArrayList<>();

	public Room(byte roomNumber, String roomName) {
		this.roomNumber = roomNumber;
		this.roomName = roomName;
		this.lastMessage = "";
		//this.users = new ArrayList<>();
		//this.messages = new ArrayList<>();
	}
}
class ChatEntry {
	public String senderNick;
	public byte messageType;
	public byte[] data;

	public ChatEntry(String senderNick, byte messageType, byte[] data) {
		this.senderNick = senderNick;
		this.messageType = messageType;
		this.data = data;
	}
}