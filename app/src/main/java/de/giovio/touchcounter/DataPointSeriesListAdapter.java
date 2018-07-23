package de.giovio.touchcounter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    }

    private final LayoutInflater mInflater;
    private List<DataPointSeries> mSeries; // Cached copy of DataPoint Series
    private Map<Integer, Integer> mPointCounts;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    DataPointSeriesListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public DataPointSeriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.datapointseriesview_item, parent, false);
        return new DataPointSeriesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DataPointSeriesViewHolder holder, int position) {
        if (mSeries != null) {
            DataPointSeries current = mSeries.get(position);
            holder.textSeriesNameView.setText(current.getName());
            holder.textSeriesCountView.setText(String.valueOf(mPointCounts.get(current.getId())));
            holder.textSeriesDateTimeView.setText(mDateFormat.format(current.getStartTime()));
        } else {
            // Covers the case of data not being ready yet.
            holder.textSeriesNameView.setText("No data recorded yet");
            holder.textSeriesCountView.setText("0");
            holder.textSeriesDateTimeView.setText(" - ");
        }
        holder.itemView.setLongClickable(true);
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
