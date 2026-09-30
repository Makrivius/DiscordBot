package discord.audioPlayer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import discord.audioPlayer.interfaces.PlayerInterface;
import discord.audioPlayer.search.SearchProvider;

public class ManagerHolder {

    private final HashSet<PlayerInterface> audioPlayers = new HashSet<>();
    private final HashSet<SearchProvider> searchProviders = new HashSet<>();

    // public ManagerHolder(PlayerInterface audioPlayer, SearchProvider
    // searchProvider) {
    // audioPlayers.add(audioPlayer);
    // searchProviders.add(searchProvider);
    // }

    // Player
    public Set<PlayerInterface> getAudioPlayers() {
        return Collections.unmodifiableSet(audioPlayers);
    }

    public PlayerInterface getActiveAudioPlayer() {
        return audioPlayers.isEmpty() ? null : audioPlayers.iterator().next();
    }

    public void addAudioPlayer(PlayerInterface audioPlayer) {
        audioPlayers.add(audioPlayer);
    }

    // Search
    public Set<SearchProvider> getSearchProviders() {
        return Collections.unmodifiableSet(searchProviders);
    }

    public SearchProvider getSearchProvider() {
        return searchProviders.isEmpty() ? null : searchProviders.iterator().next();
    }

    public void addSearchProvider(SearchProvider searchProvider) {
        searchProviders.add(searchProvider);
    }
}
