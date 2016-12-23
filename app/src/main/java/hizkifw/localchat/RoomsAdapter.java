package hizkifw.localchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by HizkiFW on 22/12/2016.
 */

public class RoomsAdapter extends ArrayAdapter<Room> {
	public RoomsAdapter(Context context, ArrayList<Room> rooms) {
		super(context, 0, rooms);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		Room room = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		if(convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_room, parent, false);
		}
		// Lookup view for data population
		LinearLayout lRoom = (LinearLayout) convertView.findViewById(R.id.lRoom);
		TextView txtName = (TextView) convertView.findViewById(R.id.txtRoomName);
		TextView txtLMsg = (TextView) convertView.findViewById(R.id.txtLastMsg);
		// Populate the data into the template view using the data object
		lRoom.setTag(room.roomNumber);
		txtName.setText(room.roomName);
		txtLMsg.setText(room.lastMessage);
		// Return the completed view to render on screen
		return convertView;
	}
}
