package co.herxun.impp.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.model.Choice;

public class VoteChoiceListAdapter extends BaseAdapter {
    private List<Choice> choiceList;
    private Context ct;
    private int fragType;
    private List<CheckBox> cbList = new ArrayList<CheckBox>();

    public VoteChoiceListAdapter(Context ct, int fragType) {
        this.ct = ct;
        this.fragType = fragType;
        choiceList = new ArrayList<Choice>();
    }

    public List<Choice> getList() {
        return choiceList;
    }

    public List<CheckBox> getCheckBoxList() {
        return cbList;
    }

    public void applyData(List<Choice> newChoiceList) {
        choiceList.clear();
        choiceList.addAll(newChoiceList);

        notifyDataSetChanged();
    }

    public void updateItem(int index, Choice choice) {
        choiceList.remove(index);
        choiceList.add(index, choice);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return choiceList.size();
    }

    @Override
    public Choice getItem(int position) {
        return choiceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VoteListItem view = (VoteListItem) convertView;
        if (convertView == null) {
            view = new VoteListItem(parent.getContext());
        }

        view.setData(choiceList.get(position));

        return view;
    }

    public class VoteListItem extends RelativeLayout {
        private CheckBox cbChoice;
        private TextView tvChoice;

        public VoteListItem(Context ct) {
            super(ct);
            inflate(getContext(), R.layout.view_choice_item, this);
            cbChoice = (CheckBox) findViewById(R.id.cb_choice);
            cbList.add(cbChoice);
            tvChoice = (TextView) findViewById(R.id.tv_choice);
        }

        public void setData(final Choice choice) {
            cbChoice.setTag(choice);
            tvChoice.setText(choice.getValue());
        }
    }
}
