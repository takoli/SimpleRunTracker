package net.takoli.simpleruntracker.adapter.animator;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;


public class FadeInUpAnimator extends BaseItemAnimator {

    @Override
    protected void animateRemoveImpl(final RecyclerView.ViewHolder holder) {
        ViewCompat.animate(holder.itemView)
                .translationY(holder.itemView.getHeight() * .75f)
                .alpha(0)
                .setDuration(getRemoveDuration())
                .setListener(new DefaultRemoveVpaListener(holder))
                .start();
    }

    @Override
    protected void preAnimateAddImpl(RecyclerView.ViewHolder holder) {
        ViewCompat.setTranslationY(holder.itemView, holder.itemView.getHeight() * .75f);
        ViewCompat.setAlpha(holder.itemView, 0);
    }

    @Override
    protected void animateAddImpl(final RecyclerView.ViewHolder holder) {
        ViewCompat.animate(holder.itemView)
                .translationY(0)
                .alpha(1)
                .setDuration(getAddDuration())
                .setListener(new DefaultAddVpaListener(holder))
                .start();
    }
}