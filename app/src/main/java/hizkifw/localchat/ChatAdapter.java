package hizkifw.localchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by HizkiFW on 23/12/2016.
 */

public class ChatAdapter extends ArrayAdapter<ChatEntry> {
	public ChatAdapter(Context context, ArrayList<ChatEntry> rooms) {
		super(context, 0, rooms);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		ChatEntry entry = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		if(convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat, parent, false);
		}
		// Lookup view for data population
		TextView txtName = (TextView) convertView.findViewById(R.id.txtNick);
		TextView txtMsg = (TextView) convertView.findViewById(R.id.txtMessage);
		// Populate the data into the template view using the data object
		try {
			txtName.setText(entry.senderNick);
			if(entry.messageType == Constants.LC_MSG_TYPE_TEXT) {
				txtMsg.setText(new String(entry.data, "UTF-8"));
			} else if(entry.messageType == Constants.LC_MSG_TYPE_IMG) {
				txtMsg.setText("[Image]");
				//TODO: Implement image display
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		// Return the completed view to render on screen
		return convertView;
	}
}
