package net.zgreatone.app.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.zgreatone.app.spotifystreamer.util.SpotifyStreamerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A placeholder fragment containing a simple view.
 */
public class SelectedArtistTracksActivityFragment extends Fragment {

    private String LOG_TAG = SelectedArtistTracksActivityFragment.class.getSimpleName();

    private ArrayAdapter<Track> mTrackAdapter;

    private ArrayList<Track> trackResult;

    private String artistId;

    public SelectedArtistTracksActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedinstanceSate) {
        super.onCreate(savedinstanceSate);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        Intent intent = getActivity().getIntent();
        artistId = intent.getStringExtra("ARTIST_ID");
        if (savedinstanceSate != null) {
        } else {
            trackResult = new ArrayList<>();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_selected_artist_tracks, container, false);
        if (artistId != null) {//start intent null block


            mTrackAdapter = new ArrayAdapter<Track>(
                    // the context
                    getActivity(),
                    //the layout for each list item
                    R.layout.list_item_track,
                    //the initial list
                    trackResult) {
                /**
                 * Overriden to customize each list item in order be able to laod the images
                 */
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                    // get the layout inflater
                    LayoutInflater inflater = (LayoutInflater) getActivity()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    ViewHolder holder;

                    if(convertView == null) {
                        convertView = inflater.inflate(R.layout.list_item_track, parent, false);
                        holder = new ViewHolder();
                        holder.trackName = (TextView) convertView.findViewById(R.id.list_item_track_textview);
                        holder.albumName = (TextView) convertView.findViewById(R.id.list_item_album_textview);
                        holder.albumImage = (ImageView) convertView.findViewById(R.id.list_item_track_image_view);
                        convertView.setTag(holder);
                    }
                    else {
                        holder = (ViewHolder) convertView.getTag();
                    }

                    // get the current artist at position
                    Track track = mTrackAdapter.getItem(position);

                    // check to make sure artist has thumbnail image
                    if (track.album.images != null && track.album.images.size() > 0) {
                        ImageView imageView = holder.albumImage;

                        Picasso.with(getActivity())
                                .load(
                                        track.album.images.get(0).url
                                ).into(imageView);
                    }
                    // set the track album
                    TextView albumTextView = holder.albumName;
                    albumTextView.setText(track.album.name);

                    // set the track name
                    TextView textView = holder.trackName;
                    textView.setText(track.name);

                    return convertView;
                }

            };

            ListView listView = (ListView) rootView.findViewById(R.id.list_view_track);
            listView.setAdapter(mTrackAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Track track = mTrackAdapter.getItem(position);
                    //TODO crete intent here to play song P2
                }
            });


            if (trackResult.size() == 0) {
                searchArtist(artistId);
            }

        }//end intent null block
        return rootView;

    }


    private void searchArtist(String artistId) {
        if (SpotifyStreamerUtil.isNetworkAvailable(getActivity())) {
            FetchArtistTrackTask fetchArtistTrackTask = new FetchArtistTrackTask();
            fetchArtistTrackTask.execute(artistId);
        } else {
            Toast toast = Toast.makeText(getActivity(), "no network connection", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public class FetchArtistTrackTask extends AsyncTask<String, Void, Track[]> {

        @Override
        protected Track[] doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Track[] top10Track;
            try {
                Map<String, Object> options = new HashMap<>();
                options.put("country", "US");
                Tracks tracks = spotify.getArtistTopTrack(params[0], options);

                List<Track> trackList = tracks.tracks;


                int trackLength;
                if (trackList.size() > 10) {
                    trackLength = 10;
                } else {
                    trackLength = trackList.size();
                }

                top10Track = new Track[trackLength];

                for (int index = 0; index < trackLength; index++) {
                    top10Track[index] = trackList.get(index);
                }
            } catch (Exception e) {
                top10Track = new Track[0];
            }

            return top10Track;
        }

        @Override
        protected void onPostExecute(Track[] result) {
            Log.v(LOG_TAG, "post execute");
            if (result != null) {
                if (result.length == 0) {
                    Toast toast = Toast.makeText(getActivity(), "no track found", Toast.LENGTH_SHORT);
                    toast.show();
                }
                trackResult.clear();
                mTrackAdapter.clear();
                for (Track track : result) {
                    trackResult.add(track);
                }
            }
        }
    }

    static class ViewHolder {
        TextView trackName;
        TextView albumName;
        ImageView albumImage;
    }
}
