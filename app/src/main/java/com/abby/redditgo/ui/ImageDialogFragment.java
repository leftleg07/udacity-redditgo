package com.abby.redditgo.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abby.redditgo.R;
import com.bumptech.glide.Glide;

import net.dean.jraw.models.Submission;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageDialogFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_TITLE = "_arg_param_title";
    private static final String ARG_PARAM_IMAGE_URL = "_arg_param_image_url";

    // TODO: Rename and change types of parameters
    private String mImageUrl;
    private String mTitle;

    @BindView(R.id.image)
    ImageView mImageView;

    @BindView(R.id.textView_title)
    TextView mTitleText;

    public ImageDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param submission Parameter 1.
     * @return A new instance of fragment ImageDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImageDialogFragment newInstance(Submission submission) {
        ImageDialogFragment fragment = new ImageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_TITLE, submission.getTitle());
        args.putString(ARG_PARAM_IMAGE_URL, submission.getUrl());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //팝업 외부영역 반투명 설정
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Translucent_NoTitleBar);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_PARAM_TITLE);
            mImageUrl = getArguments().getString(ARG_PARAM_IMAGE_URL);
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mTitleText.setText(mTitle);
        Glide.with(getContext()).load(mImageUrl).placeholder(R.drawable.img_no).into(mImageView);

    }

    @OnClick(R.id.button_close)
    public void onClose() {
        dismiss();
    }
}
