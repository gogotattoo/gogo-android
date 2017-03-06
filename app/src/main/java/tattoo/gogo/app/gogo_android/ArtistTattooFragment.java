package tattoo.gogo.app.gogo_android;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tattoo.gogo.app.gogo_android.api.GogoApi;
import tattoo.gogo.app.gogo_android.model.ArtWork;
import tattoo.gogo.app.gogo_android.model.Tattoo;

import static android.content.ContentValues.TAG;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnArtistTattooFragmentInteractionListener}
 * interface.
 */
public class ArtistTattooFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_ARTIST_NAME = "artist-name";

    private int mColumnCount = 1;
    private ArtistArtworkFragment.OnArtistArtworkFragmentInteractionListener mListener;
    private List<Tattoo> mTattoos = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private String mArtistMame;
    private ImageView ivLoading;
    private TextView tvNothingHere;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistTattooFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ArtistTattooFragment newInstance(int columnCount, String artistName) {
        ArtistTattooFragment fragment = new ArtistTattooFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ARG_ARTIST_NAME, artistName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mArtistMame = getArguments().getString(ARG_ARTIST_NAME, "gogo");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tattoo_list, container, false);

        // Set the adapter
        Context context = view.getContext();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        ivLoading = (ImageView) view.findViewById(R.id.iv_loading);
        tvNothingHere = (TextView) view.findViewById(R.id.tv_nothing_here_yet);
        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ivLoading.setVisibility(View.VISIBLE);
        GogoApi.getApi().tattoo(mArtistMame).enqueue(new Callback<List<Tattoo>>() {
            @Override
            public void onResponse(Call<List<Tattoo>> call, Response<List<Tattoo>> response) {
                ivLoading.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "onResponse: " + response.errorBody());
                    tvNothingHere.setVisibility(View.VISIBLE);
                    return;
                }
                for (Tattoo tat : response.body()) {
                    if (!tat.getImageIpfs().isEmpty()) {
                        mTattoos.add(tat);
                    }
                }
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setItemViewCacheSize(20);
                mRecyclerView.setDrawingCacheEnabled(true);
                mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                mRecyclerView.setAdapter(new ArtistTattooRecyclerViewAdapter(mTattoos, mListener));
            }

            @Override
            public void onFailure(Call<List<Tattoo>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ArtistArtworkFragment.OnArtistArtworkFragmentInteractionListener) {
            mListener = (ArtistArtworkFragment.OnArtistArtworkFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnArtistTattooFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}