package net.zgreatone.app.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;

/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistFragment extends Fragment {

    private String LOG_TAG = ArtistFragment.class.getSimpleName();

    private ArrayAdapter<Artist> mArtistAdapter;


    public ArtistFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.artist_fragment, container, false);

        final ArrayList<Artist> artistResult = new ArrayList<>();

        mArtistAdapter = new ArrayAdapter<Artist>(
                // the context
                getActivity(),
                //the layout for each list item
                R.layout.list_item_artist,
                //the initial list
                artistResult) {
            /**
             * Overriden to customize each list item in order be able to laod the images
             */
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // get the layout inflater
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                // inflate the list item
                View rowView = inflater.inflate(R.layout.list_item_artist, parent, false);

                // get the current artist at position
                Artist artist = mArtistAdapter.getItem(position);

                // check to make sure artist has thumbnail image
                if (artist.images != null && artist.images.size() > 0) {
                    ImageView imageView = (ImageView) rowView.findViewById(R.id.list_item_artist_image_view);

                    Picasso.with(getActivity())
                            .load(
                                    artist.images.get(0).url
                            ).into(imageView);
                }

                // set the artist name
                TextView textView = (TextView) rowView.findViewById(R.id.list_item_artist_textview);
                textView.setText(artist.name);

                return rowView;
            }

        };

        ListView listView = (ListView) rootView.findViewById(R.id.list_view_artist);
        listView.setAdapter(mArtistAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = mArtistAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), SelectedArtistActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artist.toString());
                intent.putExtra("ARTIST_NAME", artist.name);
                intent.putExtra("ARTIST_ID", artist.id);
                intent.putExtra("ARTIST_HREF", artist.href);
                startActivity(intent);
            }
        });

        final EditText searchBox = (EditText) rootView.findViewById(R.id.search_input);
        searchBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if keydown and "enter" is pressed
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String searchText = searchBox.getText().toString();
                    // display a floating message
                    searchArtist(searchText);
                    return true;

                }

                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final EditText searchBox = (EditText) getActivity().findViewById(R.id.search_input);
        if (searchBox != null) {
            String searchText = searchBox.getText().toString();
            if (!"".equalsIgnoreCase(searchText)) {
                searchArtist(searchText);
            } else {
                Log.d(LOG_TAG, "search text was empty");
            }
        }
    }

    private void searchArtist(String searchText) {
        FetchArtistTask fetchArtistTask = new FetchArtistTask();
        fetchArtistTask.execute(searchText);
    }

    public class FetchArtistTask extends AsyncTask<String, Void, Artist[]> {

        @Override
        protected Artist[] doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(params[0]);

            Pager<Artist> artistPager = results.artists;

            Artist[] resultStrs = new Artist[artistPager.items.size()];
            int index = 0;
            for (Artist artist : artistPager.items) {
                resultStrs[index] = artist;
                index++;
            }
            return resultStrs;
        }

        @Override
        protected void onPostExecute(Artist[] result) {
            Log.v(LOG_TAG, "post execute");
            if (result != null) {
                mArtistAdapter.clear();
                for (Artist artist : result) {
                    mArtistAdapter.add(artist);
                }
            }
        }
    }
}