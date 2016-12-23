package hizkifw.localchat;

/**
 * Created by HizkiFW on 20/12/2016.
 */
public class Constants {
	// Identifier
	public static final byte[] LC_START_PACKET = {0x6C, 0x63};	// [2] "lc", [4] PacketID, [?] Data

	// Control
	public static final byte LC_REQ_LIST_ALL  = (byte) 0x00; // -
	//public static final byte LC_REQ_LIST_ROOM = (byte) 0x01; // [1] RoomNo
	public static final byte LC_REQ_INFO_USER = (byte) 0x02; // [4] UserIP
	public static final byte LC_REQ_INFO_ROOM = (byte) 0x03; // [1] RoomNo

	public static final byte LC_RES_LIST_ALL  = (byte) 0x20; // [1] RoomNo, [1] Len, [?] Nick
	//public static final byte LC_RES_LIST_ROOM = (byte) 0x21; // [1] RoomNo, [1] Len, [?] Nick
	public static final byte LC_RES_INFO_USER = (byte) 0x22; // [1] Len, [?] Nick
	public static final byte LC_RES_INFO_ROOM = (byte) 0x23; // [1] RoomNo, [1] Len, [?] RoomName

	public static final byte LC_MSG_SND       = (byte) 0x40; // [1] RoomNo, [1] Len, [?] Nick, [1] Type, [2] Len, [?] Msg
	public static final byte LC_MSG_RCV       = (byte) 0x41; // [4] PacketID

	// Message Type
	public static final byte LC_MSG_TYPE_TEXT = (byte) 0x00;
	public static final byte LC_MSG_TYPE_IMG  = (byte) 0x01; // TODO: Implement TCP p2p image transfer
}
