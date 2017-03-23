package com.example.jacqu.ware2go.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.jacqu.ware2go.MainActivity;
import com.example.jacqu.ware2go.R;
import com.example.jacqu.ware2go.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */

public class AssistanceFragment extends Fragment {
    String pid;
    String idnum;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
       return inflater.inflate(R.layout.assistance, container, false);

    }

    JSONArray users;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        final ListView myListView = (ListView) view.findViewById(R.id.pick_user);
        final MainActivity ma = (MainActivity) this.getActivity();
        ma.get_users(new VolleyCallback() {
            @Override
            public void onSuccessResponse(Object result) {
                ArrayAdapter<String> userlist = new ArrayAdapter<String>(ma, android.R.layout.simple_list_item_1);
                users = (JSONArray) result;
                for(int i = 0; i < users.length(); i++){
                    JSONObject t;
                    try {
                        t = users.getJSONObject(i);
                    }
                    catch (Exception JSONException){
                        break;
                    }
                    userlist.add(t.toString());
                }

                myListView.setAdapter(userlist);
            }
        });

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //TODO: finish onItemClickListener
            }
        });

    }
}
