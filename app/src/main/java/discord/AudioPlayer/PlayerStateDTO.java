package discord.audioPlayer;

import java.util.List;
import java.util.Map;

public record PlayerStateDTO(
                List<TrackDTO> queue,
                int currentIndex,
                boolean playing,
                long positionMs,
                boolean paused) {
        public record TrackDTO(
                        String id,
                        String title,
                        String author,
                        long durationMs,
                        String source,
                        Map<String, String> artwork) {
        }
}