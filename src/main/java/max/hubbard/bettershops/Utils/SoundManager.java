package max.hubbard.bettershops.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;

/**
 * Created by root on 07.03.2016.
 */
public class SoundManager {

    public static Sound getSound(String newSound, String oldSound) {
        Sound sound = null;

        try {
            // set known sounds to make sure Enum isn't changing on us
            if (newSound.equals("BLOCK_NOTE_BASS")) {
                sound = Sound.BLOCK_NOTE_BASS;
            } else if (newSound.equals("BLOCK_NOTE_PING")) {
                sound = Sound.BLOCK_NOTE_PLING;
            } else {
                sound = Sound.valueOf(newSound);
            }
        } catch (IllegalArgumentException e) {
            try {
                sound = Sound.valueOf(oldSound);
            } catch (IllegalArgumentException e2) {
                // Sound is missing
            }
        }
        return sound;
    }
}
