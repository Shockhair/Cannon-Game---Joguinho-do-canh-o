package br.edu.uemg.cannongame;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/** Mantém a organização do capítulo. Fragment nativo não exige bibliotecas extras. */
@SuppressWarnings("deprecation")
public final class MainActivityFragment extends Fragment {
    private CannonView cannonView;
    @Override public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle state) {
        cannonView=new CannonView(getActivity()); return cannonView;
    }
    @Override public void onResume() { super.onResume(); cannonView.resumeGame(); }
    @Override public void onPause() { cannonView.pauseGame(); super.onPause(); }
    @Override public void onDestroyView() {
        cannonView.releaseResources(); cannonView=null; super.onDestroyView();
    }
}
