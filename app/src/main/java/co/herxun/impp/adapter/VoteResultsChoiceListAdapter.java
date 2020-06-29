package co.herxun.impp.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.model.Choice;

import com.github.mikephil.charting.utils.ColorTemplate;

public class VoteResultsChoiceListAdapter extends BaseAdapter {
    private List<Choice> choiceList;
    private Context ct;
    private String fragType = "";
    private String voteSelectedChoices;
    private List<Integer> colors;

    public VoteResultsChoiceListAdapter(Context ct, String fragType, String voteSelectedChoices) {
        this.ct = ct;
        this.fragType = fragType;
        this.voteSelectedChoices = voteSelectedChoices;
        choiceList = new ArrayList<Choice>();
        colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
    }

    public List<Choice> getList() {
        return choiceList;
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

        view.setData(choiceList.get(position), position);

        return view;
    }

    public class VoteListItem extends RelativeLayout {
        private TextView tvChoice;
        private ImageView ivOption;

        public VoteListItem(Context ct) {
            super(ct);
            inflate(getContext(), R.layout.view_results_choice_item, this);
            tvChoice = (TextView) findViewById(R.id.tv_choice);
            ivOption = (ImageView) findViewById(R.id.iv_option);
        }

        public void setData(final Choice choice, final int position) {
            ivOption.setBackgroundColor(colors.get(position % 26));
            tvChoice.setText(choice.getValue());
            if (voteSelectedChoices != null && !"".equals(voteSelectedChoices)) {
                if (voteSelectedChoices.indexOf(",") != -1) {
                    String[] voteSelectedChoicesArray = voteSelectedChoices.split(",");
                    boolean isExist = false;
                    for (int i = 0; i < voteSelectedChoicesArray.length; i++) {
                        if (choice.getKey().equals(voteSelectedChoicesArray[i])) {
                            isExist = true;
                            break;
                        }
                    }
                    if (isExist) {
                        tvChoice.setTextColor(Color.rgb(76, 76, 76));
//                        tvChoice.setBackgroundColor(ct.getResources().getColor(R.color.no1));
//                        tvChoice.setTextColor(getResources().getColor(R.color.no5));
                    } else {
                        tvChoice.setTextColor(Color.rgb(182, 182, 182));
//                        tvChoice.setBackgroundColor(ct.getResources().getColor(R.color.no5));
//                        tvChoice.setTextColor(getResources().getColor(R.color.no10));
                    }
                } else {
                    if (choice.getKey().equals(voteSelectedChoices)) {
                        tvChoice.setTextColor(Color.rgb(76, 76, 76));
//                        tvChoice.setBackgroundColor(ct.getResources().getColor(R.color.no1));
//                        tvChoice.setTextColor(getResources().getColor(R.color.no5));
                    } else {
                        tvChoice.setTextColor(Color.rgb(182, 182, 182));
//                        tvChoice.setBackgroundColor(ct.getResources().getColor(R.color.no5));
//                        tvChoice.setTextColor(getResources().getColor(R.color.no10));
                    }
                }
            } else {
                tvChoice.setTextColor(Color.rgb(182, 182, 182));
//                tvChoice.setBackgroundColor(ct.getResources().getColor(R.color.no5));
//                tvChoice.setTextColor(getResources().getColor(R.color.no10));
            }
        }
    }
}
