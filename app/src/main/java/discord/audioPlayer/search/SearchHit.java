package discord.audioPlayer.search;

public record SearchHit(String id, String url, String title, String author, long durationMs, String thumbnail) {
}