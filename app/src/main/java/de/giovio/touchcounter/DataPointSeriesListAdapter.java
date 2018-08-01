package de.giovio.touchcounter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;

public class DataPointSeriesListAdapter extends RecyclerView.Adapter<DataPointSeriesListAdapter.DataPointSeriesViewHolder> {

    class DataPointSeriesViewHolder extends RecyclerView.ViewHolder {
        private final TextView textSeriesNameView;
        private final TextView textSeriesCountView;
        private final TextView textSeriesDateTimeView;

        private DataPointSeriesViewHolder(View itemView) {
            super(itemView);
            textSeriesNameView = itemView.findViewById(R.id.textSeriesName);
            textSeriesCountView = itemView.findViewById(R.id.textSeriesCount);
            textSeriesDateTimeView = itemView.findViewById(R.id.textSeriesDateTime);
        }


        public void bind(final DataPointSeries item, final OnItemClickListener listener) {
            if (item != null) {
                textSeriesNameView.setText(item.getName());
                textSeriesCountView.setText(String.valueOf(mPointCounts.get(item.getId())));
                textSeriesDateTimeView.setText(mDateFormat.format(item.getStartTime()));
            } else {
                // Covers the case of data not being ready yet.
                textSeriesNameView.setText("No data recorded yet");
                textSeriesCountView.setText("0");
                textSeriesDateTimeView.setText(" - ");
            }
            itemView.setLongClickable(true);
            itemView.setClickable(true);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DataPointSeries item);
    }


    private final LayoutInflater mInflater;
    private final OnItemClickListener mListener;


    private List<DataPointSeries> mSeries; // Cached copy of DataPoint Series
    private Map<Integer, Integer> mPointCounts;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    DataPointSeriesListAdapter(Context context, OnItemClickListener listener) {
        mInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    @Override
    public DataPointSeriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.datapointseriesview_item, parent, false);
        return new DataPointSeriesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DataPointSeriesViewHolder holder, int position) {
        DataPointSeries current = null;
        if (mSeries != null) {
            current = mSeries.get(position);
        }
        holder.bind(current, mListener);
    }

    void setSeries(List<DataPointSeries> series, Map<Integer, Integer> pointCounts) {
        mSeries = series;
        mPointCounts = pointCounts;
        notifyDataSetChanged();
    }

    public DataPointSeries getItem(int position) {
        return mSeries.get(position);
    }

    @Override
    public int getItemCount() {
        if (mSeries != null) {
            return mSeries.size();
        } else {
            return 0;
        }
    }
}
