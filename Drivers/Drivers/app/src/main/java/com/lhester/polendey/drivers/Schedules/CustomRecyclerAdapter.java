package com.lhester.polendey.drivers.Schedules;



import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.lhester.polendey.drivers.R;
import com.squareup.picasso.Picasso;

import java.io.Console;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomRecyclerAdapter extends RecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<SchedUtils> SchedUtils;

    public CustomRecyclerAdapter(Context context, List SchedUtils) {
        this.context = context;
        this.SchedUtils = SchedUtils;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.scheds, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(SchedUtils.get(position));

        SchedUtils pu = SchedUtils.get(position);
        long d=0,h=0,m=0;
        holder.dname.setText(pu.getFull_name());
        holder._from.setText("From:"+pu.get_from());
        holder._to.setText("To:"+pu.get_to());
        holder._date.setText("Date:"+pu.getDd());
        holder._distance.setText("Distance:"+pu.getDistance());
        holder._time.setText("Time:"+pu.getTt());
        holder._fare.setText("Fare:"+pu.getFare());
        holder.SID.setText(pu.getSID());
        holder._stat.setText(pu.getStatus());

        d= pu.getMinutes_remaining() / (60 *24);
        h=(pu.getMinutes_remaining() / 60) % 24;
        m=pu.getMinutes_remaining() % 60;
        holder.rminnutes.setText( d+"d "+h +"h "+m+ "m");
        //   holder.imglink.setText(pu.getImagelink());
        //    holder.img.setImageURI(Uri.parse( pu.getImagelink()));
        //   Log.e("Link",pu.getImagelink());
        Picasso.get()
                .load(pu.getImagelink())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return SchedUtils.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView SID;
        public TextView dname;
        public TextView _from;
        public TextView _to;
        public TextView _distance;
        public TextView _fare;
        public TextView _date;
        public TextView _time;
        public TextView _stat;
        public TextView rminnutes;
        public CircleImageView img;

        public ViewHolder(View itemView) {
            super(itemView);


            SID = itemView.findViewById(R.id.SID);
            dname = itemView.findViewById(R.id.dname);
            // imglink =  itemView.findViewById(R.id.fm);
            img = itemView.findViewById(R.id.userImg);
            _date = itemView.findViewById(R.id._date);
            _time = itemView.findViewById(R.id._time);
            _distance = itemView.findViewById(R.id._distance);
            _fare = itemView.findViewById(R.id._Fare);
            _from = itemView.findViewById(R.id.fm);
            _to = itemView.findViewById(R.id.to);
            _stat = itemView.findViewById(R.id.Status);
            rminnutes = itemView.findViewById(R.id.rminutes);
            itemView.setOnClickListener(view -> {


                SchedUtils cpu = (SchedUtils) view.getTag();
                Intent intent =new Intent(context,ViewSched.class);
                intent.putExtra("SID",cpu.getSID());
                intent.putExtra("cname",cpu.getFull_name());
                intent.putExtra("pickup",cpu.get_from());
                intent.putExtra("destination",cpu.get_to());
                intent.putExtra("distance",cpu.getDistance());
                intent.putExtra("fare",cpu.getFare());
                intent.putExtra("dd",cpu.getDd());
                intent.putExtra("tt",cpu.getTt());


                context.startActivity(intent);
              //  Toast.makeText(view.getContext(), cpu.getFull_name()+" is "+ cpu.get_from(), Toast.LENGTH_SHORT).show();

            });

        }
    }

}