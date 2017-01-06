package com.cleveroad.audiowidget;

import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class for managing playback state.
 */
class PlaybackState {

	private int state = Configuration.STATE_STOPPED;

	private int position;
	private int duration;

	private final Set<PlaybackStateListener> stateListeners;

	PlaybackState() {
		stateListeners = new HashSet<>();
	}

	boolean addPlaybackStateListener(@NonNull PlaybackStateListener playbackStateListener) {
		return stateListeners.add(playbackStateListener);
	}

	public boolean removePlaybackStateListener(@NonNull PlaybackStateListener playbackStateListener) {
		return stateListeners.remove(playbackStateListener);
	}

	public int state() {
		return state;
	}

	public int position() {
		return position;
	}

	public int duration() {
		return duration;
	}

	public PlaybackState position(int position) {
		this.position = position;
		notifyProgressChanged(position);
		return this;
	}

	public PlaybackState duration(int duration) {
		this.duration = duration;
		return this;
	}

	public void start(Object initiator) {
		state(Configuration.STATE_PLAYING, initiator);
	}

	void pause(Object initiator) {
		state(Configuration.STATE_PAUSED, initiator);
	}

	void stop(Object initiator) {
		state(Configuration.STATE_STOPPED, initiator);
		position(0);
	}

	private void state(int state, Object initiator) {
		if (this.state == state)
			return;
		int oldState = this.state;
		this.state = state;
		for (PlaybackStateListener listener : stateListeners) {
			listener.onStateChanged(oldState, state, initiator);
		}
	}

	private void notifyProgressChanged(int position) {
		float progress = 1f * position / duration;
		for (PlaybackStateListener listener : stateListeners) {
			listener.onProgressChanged(position, duration, progress);
		}
	}

    /**
     * Playback state listener.
     */
	interface PlaybackStateListener {

        /**
         * Called when playback state is changed.
         * @param oldState old playback state
         * @param newState new playback state
         * @param initiator who initiate changes
         */
		void onStateChanged(int oldState, int newState, Object initiator);

        /**
         * Called when playback progress changed.
         * @param position current position of track
         * @param duration duration of track
         * @param percentage value equals to {@code position / duration}
         */
		void onProgressChanged(int position, int duration, float percentage);
	}
}
