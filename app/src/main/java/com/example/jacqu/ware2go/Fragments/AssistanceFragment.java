package com.example.jacqu.ware2go.Fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.jacqu.ware2go.MainActivity;
import com.example.jacqu.ware2go.R;
import com.example.jacqu.ware2go.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.view.Gravity.CENTER;

/**
 * Fragment to handle the area administrator Assistance View
 */

public class AssistanceFragment extends Fragment {
    View mapFrame;
    JSONArray users;
    PopupWindow popup;
    ViewGroup c;
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        c = container;
        mapFrame = inflater.inflate(R.layout.map_small, container, false);
        popup = new PopupWindow(this.getActivity());
        popup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popup.setContentView(mapFrame);
        popup.setWidth(800);
        popup.setHeight(1000);
        popup.setFocusable(true);
       return inflater.inflate(R.layout.assistance, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        final ListView myListView = (ListView) view.findViewById(R.id.pick_user);
        final MainActivity ma = (MainActivity) this.getActivity();
        ArrayAdapter<String> userlist = new ArrayAdapter<String>(ma, android.R.layout.simple_list_item_1);
        myListView.setAdapter(userlist);

        //Get assistance users, add users to listView with their assistance msg
        ma.get_users(new VolleyCallback() {
            @Override
            public void onSuccessResponse(Object result) {
                ArrayAdapter<String> userlist = new ArrayAdapter<String>(ma, android.R.layout.simple_list_item_1);
                users = (JSONArray) result;
                for(int i = 0; i < users.length(); i++){
                    JSONObject t;
                    String user_id = "";
                    String comment = "";
                    try {
                        t = users.getJSONObject(i);
                        user_id = t.getString("user_id");
                        comment = t.getString("comment");
                    }
                    catch (Exception JSONException){
                        break;
                    }
                    userlist.add("User " + user_id + ": " + comment);
                }
                myListView.setAdapter(userlist);
            }
        });

        //When an assistance user is selected, show popup of the position on map
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                try {
                    ma.setAssistanceUser(users.getJSONObject(position).getInt("user_id"));
                } catch (Exception JSONException) {
                    ma.setAssistanceUser(-1);
                }
                popup.showAtLocation(c, CENTER, 0, 200);
            }});

    }
}
