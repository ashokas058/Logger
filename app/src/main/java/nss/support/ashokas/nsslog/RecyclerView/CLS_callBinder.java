package nss.support.ashokas.nsslog.RecyclerView;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


import mva2.adapter.ItemBinder;
import mva2.adapter.ItemViewHolder;
import nss.support.ashokas.nsslog.R;
import mva2.adapter.util.Mode;

/**
 * Created by DARK-DEVIL on 5/6/2020.
 */

public class CLS_callBinder  extends ItemBinder<CLS_callsModel,CLS_callBinder.callViewHolder> {


    @Override
    public callViewHolder createViewHolder(ViewGroup parent) {
        return new callViewHolder(inflate(parent,R.layout.rcy_lyt_call_view));
    }
    @Override
    public void bindViewHolder(callViewHolder holder, CLS_callsModel item) {
        holder.txtCallNum.setText(item.getCallernumber());
        String[] date=getDateTimeCall(Long.valueOf(item.getCallDate()));
        String name= item.getCallerName()==null?",":item.getCallerName();
        holder.txtCalldetails.setText(item.getCallType()==null?"":item.getCallType()+" "+date[0]+" "+date[1]
                +" "+name);
        int bgColor = ContextCompat.getColor(holder.txtCalldetails.getContext(),
                holder.isItemSelected() ? R.color.colorAccent : R.color.lightcustomcolorblack);
        holder.layout.setBackgroundColor(bgColor);
    }
    @Override
    public boolean canBindData(Object item) {
        return item instanceof CLS_callsModel;
    }

    static  class callViewHolder extends ItemViewHolder<CLS_callsModel> {
        TextView txtCallNum,txtCalldetails;
        LinearLayout layout;
        public callViewHolder(View itemView) {
            super(itemView);
            txtCalldetails=itemView.findViewById(R.id.xml_txt_rcy_calldetail);
            txtCallNum=itemView.findViewById(R.id.xml_txt_rcy_callnum);
            layout=itemView.findViewById(R.id.xml_rcy_lyt_parent);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleItemSelection();
                }
            });

        }
    }
    public String[] getDateTimeCall(long seconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy hh:mm a");
        String dateString = formatter.format(new Date(seconds));
        return dateString.split(" ", 2);
    }
}
