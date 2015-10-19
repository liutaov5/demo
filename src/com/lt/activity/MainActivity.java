package com.lt.activity;

import java.util.ArrayList;
import java.util.List;

import com.lt.bean.Group;
import com.lt.bean.People;
import com.lt.view.PinnedHeaderExpandableListView;
import com.lt.view.StickyLayout;
import com.lt.view.PinnedHeaderExpandableListView.OnHeaderUpdateListener;
import com.lt.view.StickyLayout.OnGiveUpTouchEventListener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnChildClickListener,
		OnGroupClickListener, OnHeaderUpdateListener,
		OnGiveUpTouchEventListener {

	private PinnedHeaderExpandableListView mExpandableListView;
	private StickyLayout mStickyLayout;
	private ArrayList<Group> mGroupList;
	private ArrayList<List<People>> mChildList;
	private MyAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mExpandableListView = (PinnedHeaderExpandableListView) findViewById(R.id.expandablelist);
		mStickyLayout = (StickyLayout) findViewById(R.id.sticky_layout);
		initData();
		mAdapter = new MyAdapter(this);
		mExpandableListView.setAdapter(mAdapter);

		for (int i = 0, count = mExpandableListView.getCount(); i < count; i++) {
			mExpandableListView.expandGroup(i);
		}

		mExpandableListView.setOnHeaderUpdateListener(this);
		mExpandableListView.setOnChildClickListener(this);
		mExpandableListView.setOnGroupClickListener(this);
		mStickyLayout.setOnGiveUpTouchEventListener(this);

	}

	void initData() {
		mGroupList = new ArrayList<Group>();
		Group group = null;
		for (int i = 0; i < 3; i++) {
			group = new Group();
			group.setTitle("group-" + i);
			mGroupList.add(group);
		}

		mChildList = new ArrayList<List<People>>();
		for (int i = 0; i < mGroupList.size(); i++) {
			ArrayList<People> childTemp;
			if (i == 0) {
				childTemp = new ArrayList<People>();
				for (int j = 0; j < 13; j++) {
					People people = new People();
					people.setName("yy-" + j);
					people.setAge(30);
					people.setAddress("sh-" + j);

					childTemp.add(people);
				}
			} else if (i == 1) {
				childTemp = new ArrayList<People>();
				for (int j = 0; j < 8; j++) {
					People people = new People();
					people.setName("ff-" + j);
					people.setAge(40);
					people.setAddress("sh-" + j);

					childTemp.add(people);
				}
			} else {
				childTemp = new ArrayList<People>();
				for (int j = 0; j < 23; j++) {
					People people = new People();
					people.setName("hh-" + j);
					people.setAge(20);
					people.setAddress("sh-" + j);

					childTemp.add(people);
				}
			}
			mChildList.add(childTemp);
		}

	}

	class MyAdapter extends BaseExpandableListAdapter {

		private Context mContext;
		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mContext = context;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getGroupCount() {
			return mGroupList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mChildList.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mGroupList.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mChildList.get(groupPosition).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			GroupHolder groupHolder = null;
			if (convertView == null) {
				groupHolder = new GroupHolder();
				convertView = mInflater.inflate(R.layout.group, null);
				groupHolder.textView = (TextView) convertView
						.findViewById(R.id.group);
				groupHolder.imageView = (ImageView) convertView
						.findViewById(R.id.image);
				convertView.setTag(groupHolder);
			} else {
				groupHolder = (GroupHolder) convertView.getTag();
			}
			groupHolder.textView.setText(((Group) getGroup(groupPosition))
					.getTitle());
			if (isExpanded) {
				groupHolder.imageView.setImageResource(R.drawable.expanded);
			} else {
				groupHolder.imageView.setImageResource(R.drawable.collapse);
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			ChildHolder childHolder = null;
			if (convertView == null) {
				childHolder = new ChildHolder();
				convertView = mInflater.inflate(R.layout.child, null);
				childHolder.textName = (TextView) convertView
						.findViewById(R.id.name);
				childHolder.textAge = (TextView) convertView
						.findViewById(R.id.age);
				childHolder.textAddress = (TextView) convertView
						.findViewById(R.id.address);
				childHolder.button = (Button) convertView
						.findViewById(R.id.button1);
				convertView.setTag(childHolder);
			} else {
				childHolder = (ChildHolder) convertView.getTag();
			}

			childHolder.textName.setText(((People) getChild(groupPosition,
					childPosition)).getName());
			childHolder.textAge.setText(String.valueOf(((People) getChild(
					groupPosition, childPosition)).getAge()));
			childHolder.textAddress.setText(((People) getChild(groupPosition,
					childPosition)).getAddress());
			childHolder.button.setTag(childPosition);
			childHolder.button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(mContext,
							"childPos=" + v.getTag().toString(),
							Toast.LENGTH_SHORT).show();
				}
			});
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	class GroupHolder {
		TextView textView;
		ImageView imageView;
	}

	class ChildHolder {
		TextView textName;
		TextView textAge;
		TextView textAddress;
		Button button;
	}

	@Override
	public boolean giveUpTouchEvent(MotionEvent event) {
		if (mExpandableListView.getFirstVisiblePosition() == 0) {
			View view = mExpandableListView.getChildAt(0);
			if (view != null && view.getTop() >= 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public View getPinnedHeader() {
		View headerView = (ViewGroup) getLayoutInflater().inflate(
				R.layout.group, null);
		headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		return headerView;
	}

	@Override
	public void updatePinnedHeader(View headerView, int firstVisibleGroupPos) {
		Group firstVisibleGroup = (Group) mAdapter
				.getGroup(firstVisibleGroupPos);
		TextView textView = (TextView) headerView.findViewById(R.id.group);
		textView.setText(firstVisibleGroup.getTitle());
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Toast.makeText(MainActivity.this,
				mChildList.get(groupPosition).get(childPosition).getName(),
				Toast.LENGTH_SHORT).show();
		return false;
	}
}
