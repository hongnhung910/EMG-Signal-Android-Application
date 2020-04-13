package emgsignal.v3.SavedDataProcessing.ListFolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import emgsignal.v3.R;

public class FolderAdapter extends BaseAdapter {
    Context context;
    int layout;
    List<FolderItem> listFolder;

    public FolderAdapter(Context context, int layout, List<FolderItem> listFolder) {
        this.context = context;
        this.layout = layout;
        this.listFolder = listFolder;
    }


    @Override
    public int getCount() {
        return listFolder.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(layout, null);

        ImageView icon = convertView.findViewById(R.id.iconFolder);
        TextView name = convertView.findViewById(R.id.tv_nameFolder);

        FolderItem item = listFolder.get(position);
        name.setText(item.getName());

        return convertView;
    }
}
