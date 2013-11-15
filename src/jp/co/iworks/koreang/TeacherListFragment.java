package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.URL_TEACHER_INDEX;

import java.util.ArrayList;
import java.util.List;

import jp.co.iworks.koreang.dto.Teacher;
import jp.co.iworks.koreang.util.ImageGridViewAdapter;
import jp.co.iworks.koreang.web.KoreangHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * 先生一覧表示画面
 * @author tryumura
 *
 */
public class TeacherListFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.teacher_list, container, false);
		setupDisplay(view);
		return view;
	}
    private void openProfile(Teacher teacher) {
    	Intent intent = new Intent(getActivity(), TeacherProfileActivity.class);
    	intent.putExtra("teacher_id", teacher.getId());
    	startActivity(intent);
    }
    private void setupDisplay(final View view) {
    	KoreangHttpClient.get(getActivity(), URL_TEACHER_INDEX, null, new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(JSONObject response) {
				super.onSuccess(response);
				List<String> urlList = new ArrayList<String>();
				try {
					JSONObject info = response.getJSONObject("info");
					boolean status = info.getBoolean("status");
					if (status) {
						JSONArray list = response.getJSONArray("list");
						final List<Teacher> teacherList = new ArrayList<Teacher>();
						for (int i=0; i<list.length(); i++) {
							JSONObject teacherJson = list.getJSONObject(i);
							String id = teacherJson.getString("id");
							String url = teacherJson.getString("url");
							if (url != null) {
								urlList.add(url);
	 							Teacher teacher = new Teacher();
	 							teacher.setId(id);
	 							teacher.setUrl(url);
	 							teacherList.add(teacher);
							}
						}
 						GridView gridView = (GridView)view.findViewById(R.id.gvTeacher);
 				    	gridView.setAdapter(new ImageGridViewAdapter(getActivity(), urlList));
 				    	gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

 							@Override
 							public void onItemClick(AdapterView<?> parent,
 									View view, int position, long id) {
 								Teacher teacher = teacherList.get(position);
 								openProfile(teacher);
 							}
 						});
 				    	gridView.invalidate();
					}
				} catch (JSONException e) {
					
				}
			}
    		
    	});
     }
}
