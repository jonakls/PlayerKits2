package pk.ajneb97.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlayerUtils {

    public static boolean passCondition(Player player,String condition) {
        String[] sep = condition.split(" ");
        String variable = sep[0];
        variable = PlaceholderAPI.setPlaceholders(player, variable);
        String conditional = sep[1];

        if(conditional.equals(">=")) {
            String[] conditionMiniSep = condition.split(" >= ");
            String value = conditionMiniSep[1];
            try {
                double valueFinal = Double.valueOf(value);
                double valueFinalVariable = Double.valueOf(variable);
                if(valueFinalVariable >= valueFinal) {
                    return true;
                }
            }catch(NumberFormatException e) {
                return true;
            }
        }else if(conditional.equals("<=")) {
            String[] conditionMiniSep = condition.split(" <= ");
            String value = conditionMiniSep[1];
            try {
                double valueFinal = Double.valueOf(value);
                double valueFinalVariable = Double.valueOf(variable);
                if(valueFinalVariable <= valueFinal) {
                    return true;
                }
            }catch(NumberFormatException e) {
                return true;
            }
        }else if(conditional.equals("==")) {
            String[] conditionMiniSep = condition.split(" == ");
            String value = conditionMiniSep[1];
            if(value.equals(variable)) {
                return true;
            }
        }else if(conditional.equals("!=")) {
            String[] conditionMiniSep = condition.split(" != ");
            String value = conditionMiniSep[1];
            if(!value.equals(variable)) {
                return true;
            }
        }else if(conditional.equals(">")) {
            String[] conditionMiniSep = condition.split(" > ");
            String value = conditionMiniSep[1];
            try {
                double valueFinal = Double.valueOf(value);
                double valueFinalVariable = Double.valueOf(variable);
                if(valueFinalVariable > valueFinal) {
                    return true;
                }
            }catch(NumberFormatException e) {
                return true;
            }
        }else if(conditional.equals("<")) {
            String[] conditionMiniSep = condition.split(" < ");
            String value = conditionMiniSep[1];
            try {
                double valueFinal = Double.valueOf(value);
                double valueFinalVariable = Double.valueOf(variable);
                if(valueFinalVariable < valueFinal) {
                    return true;
                }
            }catch(NumberFormatException e) {
                return true;
            }
        }

        return false;
    }

}
