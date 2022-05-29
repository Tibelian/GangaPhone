package com.tibelian.gangaphone.user.profile;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.api.ImageLoadTask;
import com.tibelian.gangaphone.database.model.ProductPicture;

import java.util.ArrayList;
import java.util.List;

public class ImagesFragment extends Fragment {

    private final int IMAGES_PER_ROW = 3;

    private RecyclerView mImagesRcyclerView;
    private ImagesListAdapter mImagesAdapter;
    private TextView mNoImagesFound;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_images, container, false);

        // bind xml elements
        mImagesRcyclerView = view.findViewById(R.id.images_recycler);
        mNoImagesFound = view.findViewById(R.id.edit_pNoImages);

        // init recycler
        mImagesRcyclerView.setLayoutManager(new GridLayoutManager(getActivity(), IMAGES_PER_ROW));
        mImagesAdapter = new ImagesListAdapter();
        mImagesRcyclerView.setAdapter(mImagesAdapter);

        return view;
    }

    public void loadImages(ArrayList<ProductPicture> list) {
        // obtain pictures form database
        mImagesAdapter.setImages(list);
        mNoImagesFound.setVisibility(mImagesAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        mImagesAdapter.notifyDataSetChanged();
    }




    private class ImagesListAdapter extends RecyclerView.Adapter<ImagesListAdapter.ViewHolder> {

        private List<ProductPicture> mImages = new ArrayList<>();
        private Context context;
        public void setImages(List<ProductPicture> images) {
            mImages.clear();
            Log.e("ImagesListAdapter", "setting " + images.size() + " images");
            for (ProductPicture pp:images)
                mImages.add(pp);
        }
        public List<ProductPicture> getImages() { return mImages; }
        public void setContext(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ImagesListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ImagesListAdapter.ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_image, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(ImagesListAdapter.ViewHolder viewHolder, final int position) {
            viewHolder.picture = mImages.get(position);
            viewHolder.bind();
        }

        @Override
        public int getItemCount() {
            return mImages.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ProductPicture picture;
            private ImageView mImage;
            private TextView mRemoveBtn;

            public ViewHolder(View v) {
                super(v);
                // bind xml textview
                mImage = v.findViewById(R.id.imageBitmap);
                mRemoveBtn = v.findViewById(R.id.imageDeleteBtn);
            }

            public void bind()
            {
                if (picture.getUri() != null)
                    mImage.setImageURI(picture.getUri());
                else
                    new ImageLoadTask(picture.getUrl(), mImage)
                            .execute();

                mRemoveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (picture.getId() > 0)
                            removePictureFromMainObject(picture.getId());

                        mImages.remove(picture);
                        mNoImagesFound.setVisibility(mImagesAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
                        mImagesAdapter.notifyDataSetChanged();
                    }
                });


            }

        }

    }

    private void removePictureFromMainObject(int id)
    {
        // delete image from the product object
        List<Fragment> fragments = getParentFragmentManager().getFragments();
        for (Fragment frag:fragments) {
            if (frag instanceof ProductEditFragment) {
                ProductEditFragment pef = (ProductEditFragment) frag;
                pef.picturesToDelete.add(id);
                return;
            }
        }
    }



}
