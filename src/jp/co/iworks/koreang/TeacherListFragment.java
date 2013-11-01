package jp.co.iworks.koreang;

import java.util.ArrayList;
import java.util.List;

import jp.co.iworks.koreang.web.WebAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;

public class TeacherListFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.teacher_list, container, false);
		setupDisplay(view);
		return view;
	}
    private void openTimeTable(Teacher teacher) {
    	Intent intent = new Intent(getActivity(), TimeTableActivity.class);
    	intent.putExtra("teacher_id", teacher.getId());
    	intent.putExtra("nickname", teacher.getNickname());
    	intent.putExtra("message", teacher.getMessage());
    	intent.putExtra("url", teacher.getUrl());
    	startActivity(intent);
    }
    private void setupDisplay(final View view) {
    	((MainActivity)getActivity()).showProgress();
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
 							String nickname = teacherJson.getString("nickname");
 							String message = teacherJson.getString("message");
 							String url = teacherJson.getString("url");
 							if (url != null) {
 								urlList.add(url);
 							}

 							Teacher teacher = new Teacher();
 							teacher.setId(id);
 							teacher.setNickname(nickname);
 							teacher.setMessage(message);
 							teacher.setUrl(url);
 							teacherList.add(teacher);
 						}

 				    	((MainActivity)getActivity()).hideProgress();
 						GridView gridView = (GridView)view.findViewById(R.id.gvTeacher);
 				    	gridView.setAdapter(new ImageGridViewAdapter(getActivity(), urlList));
 				    	gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

 							@Override
 							public void onItemClick(AdapterView<?> parent,
 									View view, int position, long id) {
 								Teacher teacher = teacherList.get(position);
 								openTimeTable(teacher);
 							}
 						});
 				    	gridView.invalidate();
 				    	Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.motion);
 				    	gridView.setAnimation(animation);
 				    	animation.start();
 					}
 				} catch (JSONException e) {
 					((MainActivity)getActivity()).hideProgress();
 					e.printStackTrace();
 					((MainActivity)getActivity()).showDialogMessage("システムエラー", e.getMessage(), null);
 				}
 			}
     	});
     }

	private class Teacher {
		private String id;
		private String uuid;
		private String email;
		private String nickname;
		private String url;
		private String message;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getNickname() {
			return nickname;
		}
		public void setNickname(String nickname) {
			this.nickname = nickname;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		
	}
}
