package hizkifw.localchat;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
	public static EditText txtChat;
	public static ListView listChat;
	public static Button btnSend;
	public static ChatAdapter adapter;
	public static byte roomNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		txtChat = (EditText) findViewById(R.id.etChat);
		listChat = (ListView) findViewById(R.id.listMessages);
		btnSend = (Button) findViewById(R.id.btnSend);

		int location = 0;
		if(savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			roomNumber = extras.getByte("room");
		} else roomNumber = (byte) savedInstanceState.getSerializable("room");

		for(int i = 0; i < CurrentUser.activeRooms.size(); i++) {
			if(CurrentUser.activeRooms.get(i).roomNumber == roomNumber) {
				location = i;
				break;
			}
		}

		adapter = new ChatAdapter(this, (ArrayList<ChatEntry>) CurrentUser.activeRooms.get(location).messages);
		listChat.setAdapter(adapter);

		btnSend.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					String snick = CurrentUser.thisUser.nickname;
					int nlen = snick.length();
					byte[] nick = (nlen > 255 ? snick.substring(0, 255) : snick).getBytes();
					nlen = nlen > 255 ? 255 : nlen;

					byte[] msg = txtChat.getText().toString().getBytes();
					int mlen = msg.length;
					byte[] blen = {
							(byte)(mlen >>> 8),
							(byte) mlen
					};

					ByteArrayOutputStream data = new ByteArrayOutputStream();

					data.write(roomNumber);
					data.write(nlen);
					data.write(nick);
					data.write(Constants.LC_MSG_TYPE_TEXT);
					data.write(blen);
					data.write(msg);

					byte[] bytes = data.toByteArray();
					UDPListener.broadcastPacket(Constants.LC_MSG_SND, bytes);

					txtChat.setText("");
				} catch(Exception e) {
					Snackbar.make(findViewById(R.id.activity_chat), "Error sending message. Please try again.", Snackbar.LENGTH_LONG)
							.setAction("Action", null).show();
				}
			}
		});
	}
}
