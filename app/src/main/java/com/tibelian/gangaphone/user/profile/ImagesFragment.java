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

/**
 * On the creating product form
 * each uploaded image is generated here
 */
public class ImagesFragment extends Fragment {

    // const control variable. num of images per row
    private final int IMAGES_PER_ROW = 3;

    // member variables - xml elements
    private RecyclerView mImagesRcyclerView;
    private ImagesListAdapter mImagesAdapter;
    private TextView mNoImagesFound;

    /**
     * Creating visual elements from the layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // layout is loaded here
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

    /**
     * Update the adapter list with the received list as param
     * @param list
     */
    public void loadImages(ArrayList<ProductPicture> list) {
        mImagesAdapter.setImages(list);
        mNoImagesFound.setVisibility(mImagesAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        mImagesAdapter.notifyDataSetChanged();
    }

    /**
     * The custom adapter list
     */
    private class ImagesListAdapter extends RecyclerView.Adapter<ImagesListAdapter.ViewHolder> {

        // member variable - list of images
        private List<ProductPicture> mImages = new ArrayList<>();

        // setter mImages
        public void setImages(List<ProductPicture> images) {
            mImages.clear();
            for (ProductPicture pp:images)
                mImages.add(pp);
        }

        /**
         * load the layout and generate the main view
         * @param parent
         * @param viewType
         * @return
         */
        @NonNull
        @Override
        public ImagesListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ImagesListAdapter.ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_image, parent, false)
            );
        }

        /**
         * Data setter of the view
         * @param viewHolder
         * @param position
         */
        @Override
        public void onBindViewHolder(ImagesListAdapter.ViewHolder viewHolder, final int position) {
            viewHolder.picture = mImages.get(position);
            viewHolder.bind();
        }

        /**
         * obtain the images num
         * @return int
         */
        @Override
        public int getItemCount() {
            return mImages.size();
        }

        /**
         * Each image item's template
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            // the object we will show
            public ProductPicture picture;

            // the member variables - xml elements
            private ImageView mImage;
            private TextView mRemoveBtn;

            // constructor needs the main view
            public ViewHolder(View v) {
                super(v);
                // bind xml textview
                mImage = v.findViewById(R.id.imageBitmap);
                mRemoveBtn = v.findViewById(R.id.imageDeleteBtn);
            }

            /**
             * set image's bitmap
             */
            public void bind()
            {
                // load picture's bitmap
                if (picture.getUri() != null)
                    mImage.setImageURI(picture.getUri());
                else
                    new ImageLoadTask(picture.getUrl(), mImage)
                            .execute();

                // on click remove image remove it from the main object
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

    /**
     * delete picture from the ProductEditFragment
     * @param id
     */
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
