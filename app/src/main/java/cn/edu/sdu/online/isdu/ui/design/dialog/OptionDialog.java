package cn.edu.sdu.online.isdu.ui.design.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;

import cn.edu.sdu.online.isdu.R;
import cn.edu.sdu.online.isdu.ui.design.ScheduleTable;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/14
 *
 * 选项对话框
 ****************************************************
 */

public class OptionDialog extends AbstractDialog {

    private RecyclerView recyclerView;
    private TextView txtMsg;
    private String message;
    private List<String> optionList;
    private OnItemSelectListener onItemSelectListener;
    private Context mContext;
    private MyAdapter myAdapter;
    private View blankView;
    private boolean cancelOnTouchOutside = true;

    public OptionDialog(Context context, List<String> optionList) {
        this(context);
        this.optionList = optionList;
    }

    private OptionDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
        mContext = context;
    }

    private OptionDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design_dialog_option);

        txtMsg = findViewById(R.id.txt_msg);
        recyclerView = findViewById(R.id.option_list_view);
        blankView = findViewById(R.id.blank_view);

        txtMsg.setText(message);
        initRecyclerView();
        setCancelOnTouchOutside(cancelOnTouchOutside);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        myAdapter = new MyAdapter(optionList);
        recyclerView.setAdapter(myAdapter);
    }

    public void setCancelOnTouchOutside(boolean b) {
        cancelOnTouchOutside = b;
        if (blankView != null) {
            if (cancelOnTouchOutside)
                blankView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
             else
                blankView.setOnClickListener(null);
        }
    }

    public void setMessage(String message) {
        this.message = message;
        if (txtMsg != null) txtMsg.setText(message);
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<String> itemList;

        MyAdapter(List<String> itemList) {
            super();
            this.itemList = itemList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.design_dialog_option_item, parent, false
            );

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (itemList == null) {
                holder.textView.setText("");
                holder.textView.setOnClickListener(null);
                return;
            }
            holder.textView.setText(itemList.get(position));
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemSelectListener != null)
                        onItemSelectListener.onSelectItem(((TextView) v).getText().toString());
                    dismiss();
                }
            });

            if (position == 0) {
                holder.divider.setVisibility(View.GONE);
            } else {
                holder.divider.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            View divider;
            ViewHolder(View v) {
                super(v);
                textView = v.findViewById(R.id.option_item_layout);
                divider = v.findViewById(R.id.divider);
            }
        }

    }


    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }

    public interface OnItemSelectListener {
        void onSelectItem(String itemName);
    }
}
