package jp.co.iworks.koreang;

import java.util.ArrayList;
import java.util.List;

import jp.co.iworks.koreang.dto.Teacher;
import jp.co.iworks.koreang.util.ImageGridViewAdapter;
import jp.co.iworks.koreang.web.APIResponseHandler;
import jp.co.iworks.koreang.web.WebAPI;

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
     	new WebAPI(getActivity()).getTeacherList(new APIResponseHandler() {

 			@Override
 			public void onRespond(Object result) {
 				List<String> urlList = new ArrayList<String>();
 				try {
 					JSONObject json = new JSONObject(result.toString());
 					JSONObject info = json.getJSONObject("info");
 					boolean status = info.getBoolean("status");
 					if (status) {
 						JSONArray list = json.getJSONArray("list");
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
// 				    	Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.motion);
// 				    	gridView.setAnimation(animation);
// 				    	animation.start();
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
     	});
     }


}
