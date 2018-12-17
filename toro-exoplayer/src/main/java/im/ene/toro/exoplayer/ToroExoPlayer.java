/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.toro.exoplayer;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroPlayer.VolumeChangeListeners;
import im.ene.toro.media.VolumeInfo;

import static im.ene.toro.ToroUtil.checkNotNull;

/**
 * A custom {@link SimpleExoPlayer} that also notify the change of Volume.
 *
 * @author eneim (2018/03/27).
 */
public class ToroExoPlayer extends SimpleExoPlayer implements VolumeInfoController {

  public ToroExoPlayer(Context context, RenderersFactory renderersFactory,
      TrackSelector trackSelector, LoadControl loadControl, BandwidthMeter bandwidthMeter,
      @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, Looper looper) {
    super(
        context,
        renderersFactory,
        trackSelector,
        loadControl,
        bandwidthMeter,
        drmSessionManager,
        looper
    );
  }

  private VolumeChangeListeners listeners;

  @Override
  public final void addOnVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener) {
    if (this.listeners == null) this.listeners = new VolumeChangeListeners();
    this.listeners.add(checkNotNull(listener));
  }

  @Override
  public final void removeOnVolumeChangeListener(ToroPlayer.OnVolumeChangeListener listener) {
    if (this.listeners != null) this.listeners.remove(listener);
  }

  @Override
  public final void clearOnVolumeChangeListener() {
    if (this.listeners != null) this.listeners.clear();
  }

  @CallSuper @Override public final void setVolume(float audioVolume) {
    this.setVolumeInfo(new VolumeInfo(audioVolume == 0, audioVolume));
  }

  private final VolumeInfo volumeInfo = new VolumeInfo(false, 1f);

  @Override
  public final boolean setVolumeInfo(@NonNull VolumeInfo volumeInfo) {
    boolean changed = !this.volumeInfo.equals(volumeInfo);
    if (changed) {
      this.volumeInfo.setTo(volumeInfo.isMute(), volumeInfo.getVolume());
      // Must be super, to prevent infinite loop.
      super.setVolume(volumeInfo.isMute() ? 0 : volumeInfo.getVolume());
      if (listeners != null) {
        for (ToroPlayer.OnVolumeChangeListener listener : this.listeners) {
          listener.onVolumeChanged(volumeInfo);
        }
      }
    }

    return changed;
  }

  @Override @NonNull public final VolumeInfo getVolumeInfo() {
    return volumeInfo;
  }

  @NonNull @Override public String toString() {
    return "TORO:EXP:" + Integer.toHexString(hashCode());
  }
}
