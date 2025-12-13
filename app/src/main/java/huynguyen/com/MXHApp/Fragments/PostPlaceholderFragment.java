package huynguyen.com.MXHApp.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import huynguyen.com.MXHApp.R;

public class PostPlaceholderFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // This is just a placeholder, it doesn't need any specific logic.
        return inflater.inflate(R.layout.fragment_post_placeholder, container, false);
    }
}
