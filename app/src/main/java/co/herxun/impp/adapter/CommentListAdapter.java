package co.herxun.impp.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.activity.UserDetailActivity;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Comment;
import co.herxun.impp.utils.Constant;

public class CommentListAdapter extends BaseAdapter {
	private Context ct;
	private List<Comment> commentList;
	
	public CommentListAdapter(Context ct){
		this.ct = ct;
		commentList = new ArrayList<Comment>();
	}
	
	public void applyData(List<Comment> msgs){
		commentList.clear();
		commentList.addAll(msgs);
		
		notifyDataSetChanged();
	}
	
	public void addComment(Comment comment){
		commentList.add(comment);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return commentList.size();
	}

	@Override
	public Comment getItem(int position) {
		return commentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CommentListItem view = (CommentListItem) convertView;
		if (convertView == null) {
			view = new CommentListItem(parent.getContext());
		}
		view.setData(getItem(position));
		
		return view;
	}
	
	public class CommentListItem extends RelativeLayout{
		private ImageView imgUserIcon;
		private TextView textUserName,textContent;
		public CommentListItem(Context context) {
			super(context);
	        inflate(getContext(), R.layout.view_comment_list_item, this);
	        imgUserIcon = (ImageView) findViewById(R.id.comment_list_item_icon);
	        textUserName = (TextView) findViewById(R.id.comment_list_item_name);
	        textContent = (TextView) findViewById(R.id.comment_list_item_content);
		}
		
		public void setData(final Comment data){
			String strUserName = "";
			if(data.owner!=null){
				ImageLoader.getInstance(getContext()).DisplayImage(data.owner.userPhotoUrl, imgUserIcon, R.drawable.friend_default, true);
				strUserName += data.owner.userName;
			}else{
				imgUserIcon.setImageResource(R.drawable.friend_default);
			}
			if(data.replyUser!=null && !data.replyUser.userId.equals(data.post.owner.userId)){
				strUserName += " "+getContext().getString(R.string.wall_comment_reply)+" "+data.replyUser.userName;
			}
			
			textUserName.setText(strUserName);
			textContent.setText(data.content);
			imgUserIcon.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent i = new Intent(v.getContext(),UserDetailActivity.class);
					i.putExtra(Constant.INTENT_EXTRA_KEY_CLIENT, data.owner.clientId);
					getContext().startActivity(i);
					((Activity) getContext()).overridePendingTransition(R.anim.push_up_in,android.R.anim.fade_out);
				}
			});
		}
	}
	
}
