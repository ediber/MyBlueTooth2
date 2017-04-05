package com.example.gilharap.mybluetooth2;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Gil Harap on 03/04/2017.
 */

public class PairedAdapter extends RecyclerView.Adapter<PairedAdapter.CustomViewHolder>{



    public interface SelectListener{
        void onSelect(BluetoothDevice devices);
    }


    private List<BluetoothDevice> devices;
    private SelectListener listener;

    public PairedAdapter(List<BluetoothDevice> devices, SelectListener selectListener) {
        this.devices = devices;
        this.listener = selectListener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.paired_adapter_row, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        final BluetoothDevice device = devices.get(position);
        holder.name.setText(device.getName());
        holder.pairedParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSelect(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected View pairedParent;
        protected TextView name;

        public CustomViewHolder(View view) {
            super(view);
            this.name = (TextView)view.findViewById(R.id.name);
            this.pairedParent = view.findViewById(R.id.pairedParent);
        }
    }
}
