package hizkifw.localchat;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.ListView;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.RunnableFuture;

/**
 * Created by HizkiFW on 20/12/2016.
 */
public class UDPListener {
	private static Thread t;
	private static DatagramSocket sock;
	private static List<Long> receivedPackets;
	private static InetAddress broadcastAddr;
	public static Context context;
	public static int port = 65222;
	public static boolean listen;

	public static InetAddress getBroadcastAddress() throws Exception {
		Log.d("UDP", "Fetching broadcast address...");
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		while(en.hasMoreElements()) {
			NetworkInterface ni = en.nextElement();
			Log.d("UDP", "Interface: " + ni.getDisplayName());

			List<InterfaceAddress> list = ni.getInterfaceAddresses();
			Iterator<InterfaceAddress> it = list.iterator();

			while(it.hasNext()) {
				InterfaceAddress ia = it.next();
				Log.d("UDP", "Found address: " + ia.getBroadcast());
				if(ia.getBroadcast() != null && ia.getBroadcast().toString() != "")
					return ia.getBroadcast();
				//return ia.getBroadcast();
				//System.out.println(" Broadcast = " + ia.getBroadcast());
			}
		}
		return InetAddress.getByName("255.255.255.255");
	}

	public static void startServer() {
		Log.d("UDP", "Starting server");
		listen = true;
		receivedPackets = new ArrayList<>();

		t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// Initialize UDP listener
					sock = new DatagramSocket(port);
					sock.setBroadcast(true);
					byte[] rcvData;

					broadcastAddr = getBroadcastAddress();
					fetchActiveRooms();

					Log.d("UDP", "Server initialized, starting listening");
					while(listen) {
						DatagramPacket pRcv = new DatagramPacket(new byte[65800], 65800);
						sock.receive(pRcv);
						rcvData = pRcv.getData();
						Log.d("UDP", "Received some stuff");

						if(rcvData[0] == Constants.LC_START_PACKET[0] && rcvData[1] == Constants.LC_START_PACKET[1]) {
							// Current packet is a LocalChat packet, check if already received
							Log.d("UDP", "--- LC Packet ---");

							Long packetID = (long) (rcvData[2] << 24) + (rcvData[3] << 16) + (rcvData[4] << 8) + rcvData[5];
							if(receivedPackets.contains(packetID)) continue;
							else receivedPackets.add(packetID);

							Log.d("UDP", "Unique packet with ID " + packetID);

							// Get sender details
							InetAddress sender = pRcv.getAddress();
							int sPort = pRcv.getPort();
							Log.d("UDP", "Sender: " + sender.getHostAddress() + ", Port: " + port);

							// Generate random packet ID
							byte[] newID = new byte[4];
							new Random().nextBytes(newID);

							// Prepare response buffer
							boolean needsReply = false;
							ByteArrayOutputStream response = new ByteArrayOutputStream();
							response.write(Constants.LC_START_PACKET);
							response.write(newID);

							String snick = CurrentUser.thisUser.nickname;
							int len = snick.length();
							byte[] nick = (len > 255 ? snick.substring(0, 255) : snick).getBytes();
							len = len > 255 ? 255 : len;

							switch(rcvData[6]) {
								// Requests
								case Constants.LC_REQ_LIST_ALL:
									Log.d("UDP", "LC_REQ_LIST_ALL");
									needsReply = true;
									response.write(Constants.LC_RES_LIST_ALL);
									response.write(CurrentUser.thisUser.roomNumber);
									response.write(len);
									response.write(nick);
									break;

								/*case Constants.LC_REQ_LIST_ROOM:
									if(rcvData[7] == CurrentUser.thisUser.roomNumber) {
										needsReply = true;
										response.write(Constants.LC_RES_LIST_ROOM);
										response.write(CurrentUser.thisUser.roomNumber);
										response.write(len);
										response.write(nick);
									}
									break;*/

								case Constants.LC_REQ_INFO_USER:
									Log.d("UDP", "LC_REQ_INFO_USER");
									if(Arrays.equals(CurrentUser.thisUser.address.getAddress(), Arrays.copyOfRange(rcvData, 7, 11))) {
										needsReply = true;
										response.write(Constants.LC_RES_INFO_USER);
										response.write(len);
										response.write(nick);
									}
									break;

								case Constants.LC_REQ_INFO_ROOM:
									Log.d("UDP", "LC_REQ_INFO_ROOM");
									if(CurrentUser.roomExists(rcvData[7])) {
										for(int i = 0; i < CurrentUser.activeRooms.size(); i++) {
											if(CurrentUser.activeRooms.get(i).roomNumber == rcvData[7]) {
												String sroom = CurrentUser.activeRooms.get(i).roomName;
												int lroom = sroom.length();
												byte[] broom = (lroom > 255 ? sroom.substring(0, 255) : sroom).getBytes();
												lroom = lroom > 255 ? 255 : lroom;

												needsReply = true;
												response.write(Constants.LC_RES_INFO_ROOM);
												response.write(rcvData[7]);
												response.write(lroom);
												response.write(broom);
											}
										}
									}
									break;

								// Responses
								case Constants.LC_RES_LIST_ALL:
									Log.d("UDP", "LC_RES_LIST_ALL");
									int index = 0;
									if(!CurrentUser.roomExists(rcvData[7])) {
										CurrentUser.activeRooms.add(new Room(rcvData[7], "Room " + (rcvData[7] & 0xFF)));
										index = CurrentUser.activeRooms.size() - 1;
										broadcastPacket(Constants.LC_REQ_INFO_ROOM, new byte[] {rcvData[7]});
									} else {
										for(int i = 0; i < CurrentUser.activeRooms.size(); i++) {
											if(CurrentUser.activeRooms.get(i).roomNumber == rcvData[7]) {
												index = i;
												break;
											}
										}
									}

									String rspNick = new String(Arrays.copyOfRange(rcvData, 9, 9 + (rcvData[8] & 0xFF)), "UTF-8");
									User respondent = new User(rspNick);
									respondent.address = sender;

									boolean respIsNew = true;
									for(int i = 0; i < CurrentUser.activeRooms.get(index).users.size(); i++) {
										if(CurrentUser.activeRooms.get(index).users.get(i).address.equals(sender)) {
											respIsNew = false;
											break;
										}
									}
									if(respIsNew)
										CurrentUser.activeRooms.get(index).users.add(respondent);

									// Update UI Room List
									updateRoomList();
									break;

								/*case Constants.LC_RES_LIST_ROOM:


									break;*/

								case Constants.LC_RES_INFO_USER:
									Log.d("UDP", "LC_RES_INFO_USER");
									//TODO
									break;

								case Constants.LC_RES_INFO_ROOM:
									Log.d("UDP", "LC_RES_INFO_ROOM");
									//if(CurrentUser.roomExists(rcvData[7])) {
									for(int i = 0; i < CurrentUser.activeRooms.size(); i++) {
										if(CurrentUser.activeRooms.get(i).roomNumber == rcvData[7]) {
											CurrentUser.activeRooms.get(i).roomName =
													new String(Arrays.copyOfRange(rcvData, 9, 9 + rcvData[8]), "UTF-8");
											break;
										}
									}
									//}

									updateRoomList();
									break;

								// Chat Messages
								case Constants.LC_MSG_SND:
									//needsReply = true;

									byte mroom = rcvData[7];
									int mnlen = rcvData[8] & 0xFF;
									String msender = new String(Arrays.copyOfRange(rcvData, 9, 9 + mnlen), "UTF-8");
									byte mType = rcvData[9 + mnlen];
									int msgLen = (rcvData[10 + mnlen] << 8) + rcvData[11 + mnlen];
									byte[] mdata = Arrays.copyOfRange(rcvData, 12 + mnlen, 12 + mnlen + msgLen);

									//if(CurrentUser.roomExists(mroom)) {
									for(int i = 0; i < CurrentUser.activeRooms.size(); i++) {
										if(CurrentUser.activeRooms.get(i).roomNumber == mroom) {
											ChatEntry entry = new ChatEntry(msender, mType, mdata);
											CurrentUser.activeRooms.get(i).messages.add(entry);
											break;
										}
									}
									//}
									updateChatList();
									sendPacket(sender, Constants.LC_MSG_RCV, Arrays.copyOfRange(rcvData, 2, 6));
									break;

								default:
									Log.d("UDP", "LC_PACKET_UNDEFINED");
									break;
							}

							if(needsReply) {
								byte[] responseBytes = response.toByteArray();
								DatagramPacket reply = new DatagramPacket(responseBytes, responseBytes.length, sender, sPort);

								// Send twice to ensure delivery
								sock.send(reply);
								sock.send(reply);
							}

							Log.d("UDP", "/// LC Packet ///");
						}
					}
				} catch(Exception e) {
					e.printStackTrace();

					sock.close();
					startServer();
				}
			}
		});
		t.start();
	}

	private static void updateRoomList() {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				MainActivity.adapter.notifyDataSetChanged();
			}
		});
	}
	private static void updateChatList() {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ChatActivity.adapter.notifyDataSetChanged();
			}
		});
	}

	public static void stopServer() {
		listen = false;
	}

	public static void sendPacket(InetAddress dest, byte packetType, byte[] data) throws Exception {
		Log.d("UDP", "Sending packet to " + dest);
		if(!listen) startServer();

		byte[] newID = new byte[4];
		new Random().nextBytes(newID);
		//receivedPackets.add(newID);

		ByteArrayOutputStream bArr = new ByteArrayOutputStream();
		bArr.write(Constants.LC_START_PACKET);
		bArr.write(newID);
		bArr.write(packetType);
		bArr.write(data);

		byte[] bytes = bArr.toByteArray();
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, dest, port);

		sock.send(packet);
		sock.send(packet);
		Log.d("UDP", "Sent " + bytes.length + " bytes");
	}

	public static void broadcastPacket(final byte packetType, final byte[] data) {
		try {
			if(Looper.myLooper() == Looper.getMainLooper()) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							sendPacket(broadcastAddr, packetType, data);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			} else sendPacket(broadcastAddr, packetType, data);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void fetchActiveRooms() {
		broadcastPacket(Constants.LC_REQ_LIST_ALL, new byte[0]);
	}
}
