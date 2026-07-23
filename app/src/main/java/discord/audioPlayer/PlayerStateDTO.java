package discord.audioPlayer;

import java.util.List;

public record PlayerStateDTO(
                String type,
                String channelName,
                long positionMs,
                boolean paused,
                boolean shuffle,
                String repeat,
                List<TrackDTO> queue,
                int currentIndex

) {
        public record TrackDTO(
                        String id,
                        String queueId,
                        String title,
                        String author,
                        long durationMs,
                        Artwork artwork) {
        }

        public record SearchResultDTO(String id, String title, String author, long durationMs,
                        PlayerStateDTO.Artwork artwork) {
        }

        public record Artwork(String low, String high, String highDef) {
        }
}